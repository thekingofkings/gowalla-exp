package probRela;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;


public class CaseFinder {
	public static double distance_threshold = 0.03;	 // in km 

	static double event_time_exp_para_c = 0.2;	// 0.5 is better than 1, when pairwise
	static double event_space_exp_para_c = 1.5;	// bad choice 0.1, 10, 1.5
	static double alpha = 0.0011284;
	static double beta = 0.046567;
	
	
	int K;
	ArrayList<int[]> friendPair;
	ArrayList<int[]> distantFriend;
	HashMap<Integer, HashSet<Integer>> friendMap;
	ArrayList<int[]> nonFriendMeeting;
	ArrayList<Integer> topKUser;
	HashMap<Integer, Integer> uid_rank;
	// a k-by-k matrix, tracking the meeting frequency of top k users.
	HashMap<Integer, HashMap<Integer, Integer>> meetFreq;
	HashMap<Integer, HashMap<Integer, Double>> avgDistance;
	
	CaseFinder(int top_k) {
		long t_start = System.currentTimeMillis();
		System.out.println("Start create CaseFinder instance, and construct User instances.");
		K = top_k;
		User.addAllUser();
		friendPair = new ArrayList<int[]>();
		distantFriend = new ArrayList<int[]>();
		nonFriendMeeting = new ArrayList<int[]>();
		friendMap = new HashMap<Integer, HashSet<Integer>>();
		topKUser = new ArrayList<Integer>();
		uid_rank = new HashMap<Integer, Integer>();
		meetFreq = new HashMap<Integer, HashMap<Integer, Integer>>();
		avgDistance = new HashMap<Integer, HashMap<Integer, Double>>();
		
		try {
			// get the top friends
			BufferedReader fin1 = new BufferedReader( new FileReader("../../dataset/gowalla/userCount.txt"));
			for (int i = 0; i < K; i++) {
				String l = fin1.readLine();
				String[] ls = l.split("\\s+");
				int uid = Integer.parseInt(ls[0]);
				uid_rank.put(uid, i);
				topKUser.add(uid);
			}
			fin1.close();
			// get friends networks
			BufferedReader fin = new BufferedReader(new FileReader("../../dataset/gowalla/Gowalla_edges.txt"));
			String l;
			while ((l = fin.readLine()) != null) {
				String[] ls = l.split("\\s+");
				int[] friends = new int[2];
				friends[0] = Integer.parseInt(ls[0]);
				friends[1] = Integer.parseInt(ls[1]);
				// only put the friend pair of top k users into friendPair
				if (topKUser.contains(friends[0]) && topKUser.contains(friends[1])) {
					// eliminate duplication in friendPair
					if (friends[0] < friends[1])
						friendPair.add(friends);
					if (friendMap.containsKey(friends[0]))
						friendMap.get(friends[0]).add(friends[1]);
					else {
						friendMap.put(friends[0], new HashSet<Integer>());
						friendMap.get(friends[0]).add(friends[1]);
					}
				}
			}
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("Initailize case finder in %d seconds", (t_end-t_start)/1000));
	}
	
	
	
	public void locationDistancePowerLaw( ) {
		for (int id : topKUser) {
			CaseFinder.locationDistancePowerLaw(id);
		}
	}
	
	public static void locationDistancePowerLaw( int uid ) {
		try {
			User u = new User(uid);
			BufferedWriter fout = new BufferedWriter( new FileWriter (String.format("dists/distance-top200%d.txt", u.userID)));

			for (int i = 0; i < u.records.size(); i++) {
				for (int j = i + 1; j < u.records.size(); j++ ) {
					double d = u.records.get(i).distanceTo(u.records.get(j));
					fout.write(Double.toString(d) + "\n");
				}
			}
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * filter out duplicate friend pair
	 */
	private boolean inFriendPair(int uaid, int ubid) {
		if (friendMap.containsKey(uaid) && friendMap.get(uaid).contains(ubid))
			return true;
		else if (friendMap.containsKey(ubid) && friendMap.get(ubid).contains(uaid))
			return true;
		else
			return false;
	}
	
	
	/**
	 * Calculate the distance between two locations, with their Records
	 * @param start -- the first record
	 * @param end	-- the second record
	 * @return	the distance in km
	 */
	private static double distance(Record start, Record end) {
		double longiS = start.longitude;
		double latiS = start.latitude;
		double longiE = end.longitude;
		double latiE = end.latitude;
		return distance(longiS, latiS, longiE, latiE);
	}
	
	/**
	 * Calculate the distance between two locations, with their longitudes and latitudes
	 * @param longiS	-- longitude of first record
	 * @param latiS		-- latitude of first record
	 * @param longiE	-- longitude of second record
	 * @param latiE		-- latitude of second record
	 * @return the distance between two locations
	 */
	private static double distance(double longiS, double latiS, double longiE, double latiE) {
		double d2r = (Math.PI/180);
		double distance = 0;
		
		try {
			double dlong = (longiE - longiS) * d2r;
			double dlati = (latiE - latiS) * d2r;
			double a = Math.pow(Math.sin(dlati/2.0), 2)
					+ Math.cos(latiS * d2r)
					* Math.cos(latiE * d2r)
					* Math.pow(Math.sin(dlong / 2.0), 2);
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
			distance = 6367 * c;
			
		} catch (Exception e) {
			e.printStackTrace();	
		}
		return distance;
	}

	
	
	/**
	 * Calculate the meeting frequency of each user pair
	 * For the non-friends pair, we will focus on their meeting frequency
	 */
	public void allPairMeetingFreq() {
		long t_start = System.currentTimeMillis();
		System.out.println("allPairMeetingFreq starts");
		for (int i = 0; i < K; i++) {
			for (int j = i+1; j < K; j++) {
				User ua = User.allUserSet.get(topKUser.get(i));
				User ub = User.allUserSet.get(topKUser.get(j));
				// iterate through their records to find co-location event.
				int aind = 0;
				int bind = 0;
				while (aind < ua.records.size() && bind < ub.records.size()) {
					Record ra = ua.records.get(aind);
					Record rb = ub.records.get(bind);

					// 1. count the meeting frequency
					if (ra.timestamp - rb.timestamp > 4 * 3600) {
						bind++;
						continue;
					} else if (rb.timestamp - ra.timestamp > 4 * 3600) {
						aind++;
						continue;
					} else {
						if (ra.locID == rb.locID ) {
							int first = (i <= j) ? i : j;
							int second = (i > j) ? i : j;
							if (meetFreq.containsKey(first)) {
								if (meetFreq.get(first).containsKey(second)) {
									int k = meetFreq.get(first).get(second);
									meetFreq.get(first).put(second, k+1);
								} else {
									meetFreq.get(first).put(second, 1);
								}
							} else {
								meetFreq.put(first, new HashMap<Integer, Integer>());
								meetFreq.get(first).put(second, 1);
							}
						}
						aind ++;
						bind ++;
					}
				}
			}
			
			// monitor the process
			System.out.println(String.format("Process - allPairMeetingFreq finished %d out of %d", i, K));
		}
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("Finish in %d seconds", (t_end - t_start)/1000));
	}
	

	/**
	 * Write out the overall meeting frequency and average distance
	 * among the top k users.
	 */
	public void writeTopKFreq() {
		try {
			BufferedWriter fout = new BufferedWriter(new FileWriter("topk_freq.txt"));
			for (int i : meetFreq.keySet()) {
				for (int j : meetFreq.get(i).keySet()) {
					// write out id_1, id_2, meeting frequency, distance
					fout.write(String.format("%d\t%d\t%d\t", topKUser.get(i), topKUser.get(j), meetFreq.get(i).get(j) ));
					if (inFriendPair(topKUser.get(i), topKUser.get(j)))
						fout.write("1\n");
					else
						fout.write("0\n");
				}
			}
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * average distance between remote friends
	 */
	public ArrayList<int[]> remoteFriends() {
		// iterate over all user pair
		long t_start = System.currentTimeMillis();
		int c = 0;
		for (int a : friendMap.keySet()) {
			for (int b : friendMap.get(a)) {
				System.out.println(String.format("Calculating user with id %d and %d", a, b));
				int rank_a = uid_rank.get(a);
				int rank_b = uid_rank.get(b);
				int cnt = 0;
				if (rank_a <= rank_b) {
					for (Record ra : User.allUserSet.get(a).records) {
						for (Record rb : User.allUserSet.get(b).records) {
							// 1. calculate the average distance
							double d = distance(ra, rb);
							if (avgDistance.containsKey(rank_a)) {
								if (avgDistance.get(rank_a).containsKey(rank_b)) {
									double ext = avgDistance.get(rank_a).get(rank_b);
									avgDistance.get(rank_a).put(rank_b, ext + d);
								} else {
									avgDistance.get(rank_a).put(rank_b, d);
								}
							} else {
								avgDistance.put(rank_a, new HashMap<Integer, Double>() );
								avgDistance.get(rank_a).put(rank_b, d);
							}
							cnt ++;
						}
					}
					double total = avgDistance.get(rank_a).get(rank_b);
					avgDistance.get(rank_a).put(rank_b, total / cnt);
					
					
					if (total > 100) {
						int[] tuple = { a, b, (int) total };
						distantFriend.add(tuple);
					}
				}
			}
			// monitor process
			c++;
			if (c % (friendMap.size()/10) == 0)
				System.out.println(String.format("Process -- remoteFriends %d0%%", c / (friendMap.size()/10)));
		}
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("Found remote friends in %d seconds", (t_end-t_start)/1000));
		return distantFriend;
	}
	

	
	public void writeRemoteFriend() {
		try {
			BufferedWriter fout = new BufferedWriter(new FileWriter("remoteFriend.txt"));
			for (int i = 0; i < distantFriend.size(); i++) {
				// write out u_1, u_2, distance, meeting frequency
				int uaid = distantFriend.get(i)[0];
				int ubid = distantFriend.get(i)[1];
				fout.write(String.format("%d\t%d\t%d\t%d\n", distantFriend.get(i)[0], distantFriend.get(i)[1], distantFriend.get(i)[2], meetFreq.get(uid_rank.get(uaid)).get(uid_rank.get(ubid))));
			}
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * meeting frequency of non-friends
	 */
	public ArrayList<int[]> nonFriendsMeetingFreq() {
		for (int i = 0; i < K; i++ )
			for (int j = i+1; j < K; j++) {
				if (nonFriend(topKUser.get(i), topKUser.get(j)) && meetFreq.get(i).get(j) > 0) {
					int[] tuple = {topKUser.get(i), topKUser.get(j), meetFreq.get(i).get(j)};
					nonFriendMeeting.add(tuple);
				}
			}
		return nonFriendMeeting;
	}
	
	/**
	 * Assistant function for nonFriendsMeetingFreq
	 */
	private boolean nonFriend(int aid, int bid) {
		if (friendMap.containsKey(aid)) {
			if (friendMap.get(aid).contains(bid)) {
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}
	
	
	public void writeNonFriendsMeeting(){
		try {
			BufferedWriter fout2 = new BufferedWriter(new FileWriter("nonFriendsMeeting.txt"));
			for (int i = 0; i < nonFriendMeeting.size(); i++) {
				// write out u_1, u_2, meeting frequency
				fout2.write(String.format("%d\t%d\t%d\n", nonFriendMeeting.get(i)[0], nonFriendMeeting.get(i)[1],nonFriendMeeting.get(i)[2]));
			}
			fout2.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/*
	 * Analyze a pair of users
	 * 
	 * Calculate the following value:
	 * 		total meeting frequency
	 * 		locID -- where they meet
	 * 		meeting frequency at each location
	 * 		ranking of this place of user a
	 * 		how many times user a go there
	 * 		ranking of this place of user b
	 * 		how many times user b go there 
	 */
	
	/**
	 * Calculate the measures from a pair of users
	 * @param uaid
	 * @param ubid
	 */
	public static void pairAnalysis( int uaid, int ubid ) {
		User ua = new User(uaid);
		User ub = new User(ubid);
		// 1. get the co-locations and the corresponding meeting freq
		HashMap<Long, Integer> mf = meetingFreq(ua, ub);
		// 2. get the total meeting frequency
		int sum = 0;
		for (int i : mf.values()) {
			sum += i;
		}
		System.out.println(String.format("Total Meet %d times between user %d and %d.", sum, uaid, ubid));
		// 3. get the location distribution of two users
		TreeMap<Long, Integer> loca = locationDistribution(ua);
		TreeMap<Long, Integer> locb = locationDistribution(ub);
		// 4. get the ranking
		LinkedList<Integer> freqa = new LinkedList<Integer>(loca.values());
		Collections.sort(freqa);
		Collections.reverse(freqa);
		LinkedList<Integer> freqb = new LinkedList<Integer>(locb.values());
		Collections.sort(freqb);
		Collections.reverse(freqb);
		System.out.println("loc \t meeting frequency \t rank of A \t frequency of A \t rank of B \t frequency of B");
		for (Long l : mf.keySet()) {
			int fa = loca.get(l);
			int ranka = freqa.indexOf(fa) + 1;
			int fb = locb.get(l);
			int rankb = freqb.indexOf(fb) + 1;
			System.out.println(String.format("%d \t\t %d \t\t %d \t\t %d \t\t %d \t\t %d", l, mf.get(l), ranka, fa, rankb, fb));
		}
		
	}
	

	/**
	 * Calculate the meeting frequency between a pair of users
	 * The meeting event is based on location ID
	 * @param ua	user a
	 * @param ub	user b
	 * @return		a map with location ID as keys and meeting frequency at that location as values
	 */
	private static HashMap<Long, Integer> meetingFreq( User ua, User ub ) {
		HashMap<Long, Integer> colofreq = new HashMap<Long, Integer>();
		int aind = 0;
		int bind = 0;
		long lastMeet = 0;
		while (aind < ua.records.size() && bind < ub.records.size()) {
			Record ra = ua.records.get(aind);
			Record rb = ub.records.get(bind);
			
			if (ra.timestamp - rb.timestamp > 3600 * 4) {
				bind ++;
				continue;
			} else if (rb.timestamp - ra.timestamp > 3600 * 4 ) {
				aind ++;
				continue;
			} else {
				if (ra.locID == rb.locID && ra.timestamp - lastMeet >= 3600) {
					if (colofreq.containsKey(ra.locID)) {
						int tmp = colofreq.get(ra.locID);
						colofreq.put(ra.locID, tmp + 1);
					} else {
						colofreq.put(ra.locID, 1);
					}
					lastMeet = ra.timestamp;
				}
				aind ++;
				bind ++;
			}
		}
		return colofreq;
	}
	
	
	/**
	 * Calculate the total meeting frequency
	 * @param uaid
	 * @param ubid
	 * @return
	 */
	@SuppressWarnings("unused")
	private static int totalMeetingFreq( int uaid, int ubid ) {
		User ua = new User(uaid);
		User ub = new User(ubid);
		HashMap<Long, Integer> mf = meetingFreq(ua, ub);
		int sum = 0;
		for (int f : mf.values()) {
			sum += f;
		}
		return sum;
	}
	

	
	/**
	 * Calculate the sum of log measure of user
	 * meeting event is based on location ID
	 * @param ua 		user a
	 * @param ub		user b
	 * @param meetingEvent    	a map from meeting location ID to how many times meet here
	 * @return   the sum log measure and the total meeting frequency
	 */
	@SuppressWarnings("unused")
	private static double[] locIDBasedSumLogMeasure(int uaid, int ubid) {
		User ua = new User(uaid);
		User ub = new User(ubid);
		int aind = 0;
		int bind = 0;
		long lastMeet = 0;
		double freq = 0;
		double measure = 0;
		while (aind < ua.records.size() && bind < ub.records.size()) {
			Record ra = ua.records.get(aind);
			Record rb = ub.records.get(bind);
			
			if (ra.timestamp - rb.timestamp > 3600 * 4) {
				bind ++;
				continue;
			} else if (rb.timestamp - ra.timestamp > 3600 * 4 ) {
				aind ++;
				continue;
			} else {
				if (ra.locID == rb.locID && ra.timestamp - lastMeet >= 3600) {
					freq ++;
					measure -= Math.log10(ua.locationWeight(ra)) + Math.log10(ub.locationWeight(rb)); 
					lastMeet = ra.timestamp;
				}
				aind ++;
				bind ++;
			}
		}
		double[] rt = {measure, freq};
		return rt;
	}
	
	
	@SuppressWarnings("unused")
	private static double[] locIDBasedARCTANweightEvent(int uaid, int ubid) {
		User ua = new User(uaid);
		User ub = new User(ubid);
		LinkedList<Record> meetingEvent = new LinkedList<Record>();
		LinkedList<Double> meetingRawWeight = new LinkedList<Double>();
		int aind = 0;
		int bind = 0;
		long lastMeet = 0;
		double freq = 0;
		double measure = 0;
		while (aind < ua.records.size() && bind < ub.records.size()) {
			Record ra = ua.records.get(aind);
			Record rb = ub.records.get(bind);
			
			if (ra.timestamp - rb.timestamp > 3600 * 4) {
				bind ++;
				continue;
			} else if (rb.timestamp - ra.timestamp > 3600 * 4 ) {
				aind ++;
				continue;
			} else {
				if (ra.locID == rb.locID && ra.timestamp - lastMeet >= 3600) {
					freq ++;
					measure = -(Math.log10(ua.locationWeight(ra)) + Math.log10(ub.locationWeight(rb)));
					meetingEvent.add(ra);
					meetingRawWeight.add(measure);
					lastMeet = ra.timestamp;
				}
				aind ++;
				bind ++;
			}
		}
		
		double[] rt = new double[2];
		double w = 0;
		measure = 0;
		if (meetingEvent.size() == 1) {
			for (double m : meetingRawWeight)
				rt[0] += m;
			rt[1] = freq;
		} else if (meetingEvent.size() > 1) {
			for (int i = 0; i < meetingEvent.size(); i++) {
				for (int j = i+1; j < meetingEvent.size(); j++) {
					Record r1 = meetingEvent.get(i);
					Record r2 = meetingEvent.get(j);
					w = 2 / Math.PI * Math.atan(Math.abs(r2.timestamp - r1.timestamp) / 3600.0 / 24);
					measure += (meetingRawWeight.get(i) + meetingRawWeight.get(j)) * w;
				}
			}
			measure = measure * 2 / (meetingEvent.size() - 1);
			rt[0] = measure;
			rt[1] = freq;
		}
		return rt;
	}
	
	
	
	/**
	 * use 1 - exp(- c * x)
	 * event pairwise distance
	 * @param uaid
	 * @param ubid
	 * @param fout -- intermediate variable output file
	 * @param friend_flag -- 1: they are friends, 2: they are non-friends
	 * @param IDorDist -- define the meeting criteria. true: location ID / false: distance 30 m
	 * @param entroIDorDist -- true: location ID, false: GPS location
	 * @param RhoMethod -- min / prod
	 * @param weightMethod -- min / sum
	 * @param combMethod -- min / prod / wsum
	 * @param dependence -- 0: no dependence / 1: temporal dependence / 2: spatial dependence
	 * @param numUser -- how many user are used for entropy estimation. 1 means 10%, 2 means 20%, ....
	 * 
	 * @return
	 * @throws IOException 
	 */
	@SuppressWarnings("unused")
	private static double[] PAIRWISEweightEvent(int uaid, int ubid, BufferedWriter fout, int friend_flag, boolean IDorDist, 
			boolean entroIDorDist, String RhoMethod, String weightMethod, String combMethod, int dependence, int numUser
			) throws IOException {
		User ua = new User(uaid);
		User ub = new User(ubid);
		LinkedList<Record> meetingEvent = new LinkedList<Record>();
		LinkedList<Double> mw_pbg = new LinkedList<Double>();		// meeting weight _ personal background
		LinkedList<Double> mw_le = new LinkedList<Double>(); 		// meeting weight _ location entropy
		LinkedList<Double> mw_pbg_le = new LinkedList<Double>();  	// meeting weight _ personal background _ location entropy
		LinkedList<Double> probs = new LinkedList<Double>();
		LinkedList<Double> entros = new LinkedList<Double>();
		HashMap<Long, Double> locationEntropy = null;
		HashMap<String, Double> GPSEntropy = null;
		if (entroIDorDist) {
			locationEntropy = Tracker.readLocationEntropyIDbased(numUser, true);
		} else {
			GPSEntropy = Tracker.readLocationEntropyGPSbased(numUser, false);
		}
		
		int aind = 0;
		int bind = 0;
		long lastMeet = 0;
		double freq = 0;
		double personBg = 0;
		double loc_entro = 0;
		double comb = 0;
			
		while (aind < ua.records.size() && bind < ub.records.size()) {
			Record ra = ua.records.get(aind);
			Record rb = ub.records.get(bind);
			
			if (ra.timestamp - rb.timestamp > 3600 * 4) {
				bind ++;
				continue;
			} else if (rb.timestamp - ra.timestamp > 3600 * 4 ) {
				aind ++;
				continue;
			} else {
				// judge the meeting event by different criteria
				boolean isMeeting = false;
				if (IDorDist) {
					isMeeting = (ra.locID == rb.locID);
				} else {
					isMeeting = (ra.distanceTo(rb) < CaseFinder.distance_threshold);
				}
				
				if ( isMeeting && ra.timestamp - lastMeet >= 3600) {
					freq ++;
					double prob = 0;
					/** different methods to calculate rho **/
					if (RhoMethod == "min") {
						prob = Math.min( ua.locationWeight(ra) , ub.locationWeight(rb));
					} else if (RhoMethod == "prod") {
						// measure 1:  - log (rho_1 * rho_2)
						prob = ua.locationWeight(ra) * ub.locationWeight(rb);
					}
					probs.add(prob);
					personBg = -(Math.log10(prob));
					mw_pbg.add(personBg);
					// measure 2:  (1 - rho_1) * (1 - rho_2)
//					measure = ( 1 - ua.locationWeight(ra) ) * ( 1- ub.locationWeight(rb));
					/** different methods to calculate location entropy **/
					double entro = 0;
					if ( entroIDorDist ) {
						if (locationEntropy.containsKey(ra.locID))
							entro = (locationEntropy.get(ra.locID)); // + locationEntropy.get(rb.locID));
						else
							entro = 0;
					} else {
						if (GPSEntropy.containsKey(ra.GPS()))
							entro = GPSEntropy.get(ra.GPS());
						else
							entro = 0;
					}
					entros.add(entro);
					loc_entro = Math.exp( - entro );
					mw_le.add(loc_entro);
					
					/** different method to calculate the product of two **/
					comb = personBg *  loc_entro;
					mw_pbg_le.add(comb);
					meetingEvent.add(ra);
					lastMeet = ra.timestamp;
				}
				aind ++;
				bind ++;
			}
		}
		
		
		double[] rt = new double[6];

		personBg = 0;
		loc_entro = 0;
		double pbg_lcen = 0;
		double temp_dep = 0;
		double pbg_lcen_td = 0;
		
		double min_prob = Double.MAX_VALUE;
		double measure_sum = 0;
		double avg_entro = 0;
		double avg_le = 0;
		double avg_pbg = 0;
		
		for (double m : probs)
			if ( min_prob > m )
				min_prob = m;
		
		// calculate personal background
		if (weightMethod == "min") {
			personBg =  - Math.log10(min_prob) * probs.size();
		} else if (weightMethod == "sum") {
			for (double m : mw_pbg)
				personBg += m;
		}

		// calculate location entropy
		for (double m : mw_le) {
			loc_entro += m;
		}
		
		if (dependence == 0)
		{
			/** average measure **/
			for (double m : entros)
				avg_entro += m;
			avg_entro /= entros.size();
			for (double m : mw_pbg) {
				measure_sum += m;
			}
			avg_pbg = measure_sum / mw_pbg.size();
			if (combMethod == "min") {
				pbg_lcen = - Math.log10(min_prob) * loc_entro;
			} else if (combMethod == "prod") {
				for (double m : mw_pbg_le) {
					pbg_lcen += m;
				}
			} else if (combMethod == "wsum") {
				pbg_lcen = alpha * personBg + beta * loc_entro;
			}

//			fout.write(String.format("%g\t%g\t%d\n", measure, locent, friend_flag));
			avg_le = loc_entro / mw_le.size();
			fout.write(String.format("%g\t%d\t%d\n", avg_le, (int)freq, friend_flag));
		} else {
			double avg_w = 0;
			if (meetingEvent.size() == 1) {
				pbg_lcen = mw_pbg_le.get(0);
				temp_dep = 1;
				pbg_lcen_td = pbg_lcen;
			} else if (meetingEvent.size() > 1) {
				for (int i = 0; i < meetingEvent.size(); i++) {
					double w = 0;
					for (int j = 0; j < meetingEvent.size(); j++) {
						if (i != j) {
							Record r1 = meetingEvent.get(i);
							Record r2 = meetingEvent.get(j);
							if (dependence == 1) { 
								w += 1 - Math.exp(- event_time_exp_para_c * Math.abs(r2.timestamp - r1.timestamp) / 3600.0 / 24);
							} else if (dependence == 2) {
								double dist = r2.distanceTo(r1);
								w += 1 - Math.exp(- event_space_exp_para_c * dist);
	//							fout.write(String.format("%g\t%g\n", dist, w));
							}
						}
					}
					w /= (meetingEvent.size() - 1);
					temp_dep += w;
					double tmp = 0;
					if (combMethod == "min")
						tmp = - Math.log10(min_prob) * mw_le.get(i);
					else if (combMethod == "prod")
						tmp = mw_pbg_le.get(i);
					else if (combMethod == "wsum")
						tmp = alpha * mw_pbg.get(i) + beta * mw_le.get(i);
					pbg_lcen += tmp;
					pbg_lcen_td += tmp * w;
				}
				avg_w = temp_dep / meetingEvent.size();
			}
			
			fout.write(String.format("%g\t%d\t%d\n", avg_w, (int)freq, friend_flag));
			
			/** write out the distance / time between consecutive meeting
			if (meetingEvent.size() == 5) {
				fout.write(String.format("%d\t%d\t", uaid, ubid));
				for (int l = 0; l < 4; l++) {
					fout.write(String.format("%d\t", meetingEvent.get(l+1).timestamp - meetingEvent.get(l).timestamp));
				}
				fout.write(String.format("%d\n", friend_flag));
			}
			*/
		}
		rt[0] = personBg;
		rt[1] = freq;
		rt[2] = pbg_lcen;
		rt[3] = loc_entro;
		rt[4] = pbg_lcen_td;
		rt[5] = temp_dep;
		
		return rt;
	}
	
	
	/**
	 * w = 1 - exp( - c * x )
	 * only consecutive event is weighted </br>
	 * ====================================================================</br>
	 * It is proved to perform worse. Because when a pair of users consecutively meet for 100 times, the total 
	 * weight may be too small, due to the heavy penalize. </br>
	 * ====================================================================
	 * @param uaid
	 * @param ubid
	 * @return
	 */
	@SuppressWarnings("unused")
	private static double[] locIDBasedOneMinusExpCONSECUTIVEweightEvent(int uaid, int ubid) {
		User ua = new User(uaid);
		User ub = new User(ubid);
		LinkedList<Record> meetingEvent = new LinkedList<Record>();
		LinkedList<Double> meetingRawWeight = new LinkedList<Double>();
		int aind = 0;
		int bind = 0;
		long lastMeet = 0;
		double freq = 0;
		double measure = 0;
		// weight each meeting event by user personal mobility background
		while (aind < ua.records.size() && bind < ub.records.size()) {
			Record ra = ua.records.get(aind);
			Record rb = ub.records.get(bind);
			
			if (ra.timestamp - rb.timestamp > 3600 * 4) {
				bind ++;
				continue;
			} else if (rb.timestamp - ra.timestamp > 3600 * 4 ) {
				aind ++;
				continue;
			} else {
				if (ra.locID == rb.locID && ra.timestamp - lastMeet >= 3600) {
					freq ++;
					measure = -(Math.log10(ua.locationWeight(ra)) + Math.log10(ub.locationWeight(rb)));
					meetingEvent.add(ra);
					meetingRawWeight.add(measure);
					lastMeet = ra.timestamp;
				}
				aind ++;
				bind ++;
			}
		}
		
		
		
		// combine each meeting event into one measure by using the temporal dependence in the meeting events
		double[] rt = new double[2];
		double w = 0;
		measure = 0;
		int cnt = 0;
		if (meetingEvent.size() == 1) {
			for (double m : meetingRawWeight)
				rt[0] += m;
			rt[1] = freq;
		} else if (meetingEvent.size() > 1) {
			for (int i = 0; i < meetingEvent.size(); i++) {
					long t1 = 0, t2 = 0;
					if (i == 0) 
						t1 = Long.MAX_VALUE;
					else
						t1 = meetingEvent.get(i).timestamp - meetingEvent.get(i-1).timestamp;
					if (i == meetingEvent.size()-1)
						t2 = Long.MAX_VALUE;
					else
						t2 = meetingEvent.get(i+1).timestamp - meetingEvent.get(i).timestamp;
					long t = Math.min(t1, t2);
					w =  1 - Math.exp(- event_time_exp_para_c * t / 3600.0 / 24);
	//						w =  2 / Math.PI * Math.atan(t / 3600.0 / 24);
					System.out.println(Double.toString(w) + "\t" + Double.toString(t/3600.0 / 24));
					measure += meetingRawWeight.get(i) * w;
					cnt ++;
			}
			if  ( cnt != meetingRawWeight.size())
				System.out.println("Error in calculate events total weight, missing event");
			rt[0] = measure;
			rt[1] = freq;
		}
		return rt;
	}
	
	/**
	 * Analyze pair of users using distance to judge co-locating and eliminating consecutive meeting
	 * Our new measure is </br>
	 * 		measure = sum ( log(a) + log(b) )
	 * @param uaid
	 * @param ubid
	 * @return two numbers, the first is the distance based measure, the second is the meeting frequency
	 */
	public static double[] distanceBasedSumLogMeasure(int uaid, int ubid, boolean printFlag) {
		User ua = new User(uaid);
		User ub = new User(ubid);
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		
		// get the co-locating event
		ArrayList<double[]> coloEnt = meetingWeight(ua, ub, CaseFinder.distance_threshold);
		
		// aggregate the measure
		// location entropy
		HashMap<Long, Double> locationEntropy = Tracker.readLocationEntropyIDbased(5000);
		double M = Double.MAX_VALUE;
		double entro = 0;
		for (double[] a : coloEnt) {
			if ( M > a[0] * a[1]) 
				M = a[0] * a[1];
//			System.out.println(locationEntropy.get((long) a[4]));
			entro += Math.exp(- locationEntropy.get((long) a[4]));
		}
		double minMeasure = - Math.log10(M) * coloEnt.size();
		
		//  calculate the temporal correlation
		double minTC = 0;
		for (int i = 0; i < coloEnt.size(); i++) {
			double[] a = coloEnt.get(i);
			double[] b = null;
			double w = 0;
			for (int j = 0; j < coloEnt.size(); j++) {
				b = coloEnt.get(j);
				if (i != j) {
					double deltaT = Math.abs( b[7] - a[7] ) / 3600 / 24;
					w += 1 - Math.exp(- 0.3 * deltaT);
//					System.out.println(String.format("%g\t%g\t%g", w, 1-Math.exp(-1.5* deltaT) , deltaT));
				}
			}
			
			minTC += w * (- Math.log10(M)) / (coloEnt.size() - 1);
		}
		
		

		// print out the probability
		if (printFlag) {
			System.out.println(String.format("User %d and %d meet %d times.\nA.weight\t\tB.weight\t\tA.lati\t\tA.longi\t\tA.locID\t\tB.lati\t\tB.longi", 
					ua.userID, ub.userID, coloEnt.size()));
			for (double[] a : coloEnt) {
				cal1.setTimeInMillis((long) a[7] * 1000);
				cal2.setTimeInMillis((long) a[8] * 1000);
				System.out.println(String.format("%g\t\t%g\t\t%g\t\t%g\t\t%g\t\t%g\t\t%g\t\t%8$tF %8$tT\t\t%9$tF %9$tT", a[0], a[1], a[2], 
						a[3], a[4], a[5], a[6], cal1, cal2));
			}
			System.out.println(String.format("User pair %d and %d has min personal measure %g, global measure %g, temporal dependence measure %g", uaid, ubid, minMeasure, entro, minTC));
		}
		double[] rt = {minMeasure, (double) coloEnt.size()};
		return rt;
	}
	
	public static double[] distanceBasedSumLogMeasure(int uaid, int ubid) {
		return distanceBasedSumLogMeasure(uaid, ubid, false);
	}
	
	
	
	/**
	 * Calculate the distance based measure for all the top user pairs
	 */
	public static void writeOutDifferentMeasures(double para_c, int numUser) {
		System.out.println("==========================================\nStart writeOutDifferentMeasures");
		long t_start = System.currentTimeMillis();
		try {
			User.findFrequentUsersTopK(numUser);
			BufferedReader fin = new BufferedReader(new FileReader("data/topk_freqgt1-5000.txt"));
			BufferedWriter fout = new BufferedWriter(new FileWriter(String.format("data/distance-d30-u%d-c%.3f.txt", numUser, event_time_exp_para_c)));
			String l = null;
			double[] locidm = null;
			
			BufferedWriter fout2 = new BufferedWriter(new FileWriter("data/Pair-glob-measure.txt"));
				
			while ( (l = fin.readLine()) != null ) {
				String[] ls = l.split("\\s+");
				int uaid = Integer.parseInt(ls[0]);
				int ubid = Integer.parseInt(ls[1]);
				int freq = Integer.parseInt(ls[2]);
				int friflag = Integer.parseInt(ls[3]);
				if (freq > 0 && User.frequentUserSet.containsKey(uaid) && User.frequentUserSet.containsKey(ubid)) {
					// locidm contains:
					// 		personal bg; frequency; personal bg + location entropy; location entropy; 
					//		personal bg + location entro + temp dependency; temporal dependency
					locidm = PAIRWISEweightEvent(uaid, ubid, fout2, friflag, false, true,  "prod", "min", "min", 1, numUser);
					fout.write(String.format("%d\t%d\t%g\t%g\t%g\t%d\t%g\t%g\t%d%n", uaid, ubid, locidm[2], locidm[3], locidm[0], (int) locidm[1], locidm[4], locidm[5], friflag));
				}
			}
			Tracker.locationEntropy.clear();
			
			fin.close();
			fout.close();
			fout2.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("Process writeOutDifferentMeasures finished in %d seconds", (t_end - t_start) / 1000));
	}
	
	
	/**
	 * Calculate the weight for each meeting event
	 * @param ua	user a
	 * @param ub	user b
	 * @param dist_threshold   the threshold for distance measure in km
	 * @return		a list of meeting event. Each event is represented by probability, 
	 * 				location of a, and location of b
	 */
	private static ArrayList<double[]> meetingWeight( User ua, User ub, double dist_threshold ) {
		ArrayList<double[]> coloEnt = new ArrayList<double[]>();
		int aind = 0;
		int bind = 0;
		double time_lastMeet = 0;
		while (aind < ua.records.size() && bind < ub.records.size()) {
			Record ra = ua.records.get(aind);
			Record rb = ub.records.get(bind);
			
			if (ra.timestamp - rb.timestamp > 3600 * 4) {
				bind ++;
				continue;
			} else if (rb.timestamp - ra.timestamp > 3600 * 4) {
				aind ++;
				continue;
			} else {
				if (ra.distanceTo(rb) < dist_threshold && ra.timestamp - time_lastMeet >= 3600) {
					double wta = ua.locationWeight(ra);
					double wtb = ub.locationWeight(rb);
					double[] evnt = { wta,  wtb, ra.latitude, ra.longitude, ra.locID, rb.latitude, rb.longitude, ra.timestamp, rb.timestamp };
					coloEnt.add(evnt);
					time_lastMeet = ra.timestamp;
				}
//				if (ra.locID == rb.locID && ra.timestamp - time_lastMeet >= 3600 ) {
//					double wta = ua.locationWeight(ra);
//					double wtb = ub.locationWeight(rb);					
//					double[] evnt = {wta, wtb, ra.latitude, ra.longitude, rb.latitude, rb.longitude, ra.timestamp, rb.timestamp };
//					coloEnt.add(evnt);
//					time_lastMeet = ra.timestamp;
//				}
				aind ++;
				bind ++;
			}
		}
		
		return coloEnt;
	}
	
	@SuppressWarnings("unused")
	private static ArrayList<double[]> meetingWeight( User ua, User ub ) {
		return meetingWeight(ua, ub, CaseFinder.distance_threshold);
	}
	
	
	/**
	 * Get the location visiting distribution of a specific user
	 * @param ua  user a
	 * @return a map with each location ID as keys and the visiting frequency as values
	 * 
	 */
	private static TreeMap<Long, Integer> locationDistribution( User ua ) {
		TreeMap<Long, Integer> freq = new TreeMap<Long, Integer>();
		for (Record r : ua.records) {
			if (freq.containsKey(r.locID)) {
				int tmp = freq.get(r.locID);
				freq.put(r.locID, tmp + 1);
			} else {
				freq.put(r.locID, 1);
			}
		}
		return freq;
	}
	

	
	// TDD
	@SuppressWarnings("unused")
	private static void test_distance() {
		double d = distance(171.52, 47.45, -175.29, 47.31);
		System.out.println("Distance is " + Double.toString(d));

		d = distance(50.04, 5.42, 58.38, 3.04);
		System.out.println("Distance is " + Double.toString(d));
		
		d = distance(-105.09, 59.42, -68.57, -48.27);
		System.out.println("Distance is " + Double.toString(d));
	}
	
	
	
	public static void main(String argv[]) {
//		CaseFinder cf = new CaseFinder(107092);
//		cf.locationDistancePowerLaw();
//		cf.allPairMeetingFreq();
//		cf.writeTopKFreq();
		
//		cf.remoteFriends();
//		cf.writeRemoteFriend();
		
//		cf.nonFriendsMeetingFreq();
//		cf.writeNonFriendsMeeting();
		
		
//		int[][] friend_pair = {{19404, 350}, {3756, 4989}, {819, 3328}, {588, 401}, {551, 3340}};
//		int[][] nonfriend_pair = {{819, 956}, {267, 18898}, {19096, 3756}, {19404, 267}};
//		double m = 0;
//		for (int[] u : friend_pair) {
////			pairAnalysis(u[0], u[1]);
//			m = distanceBased_pairAnalysis(u[0], u[1]);
//			System.out.println(String.format("User pair %d and %d has measure %g", u[0], u[1], m));
//		}
//		
//		for (int[] u : nonfriend_pair) { 
////			pairAnalysis(u[0], u[1]);
//			m = distanceBased_pairAnalysis(u[0], u[1]);
//			System.out.println(String.format("User pair %d and %d has measure %g", u[0], u[1], m));
//		}
//		
		

//		distanceBasedSumLogMeasure(267 , 510 ,true);
//		distanceBasedSumLogMeasure(350 , 6138 ,true);
//		distanceBasedSumLogMeasure(39746, 39584, true);
		
//		for (int i = 0; i < 10; i++) {
//			User.para_c = 10 + i * 10;
//			writeOutDifferentMeasures(User.para_c);
//		}
		
//		for (int i = 1; i < 11; i++ )
//		CaseFinder.event_time_exp_para_c = 0.2;
//		User.recSampleRate = 1;
		int[] userN = new int[] { 100, 200, 300, 400, 500, 1000, 2000, 3000, 4000, 5000 };
		for (int i = 0; i < userN.length; i++)
			writeOutDifferentMeasures(User.para_c, userN[i]);
		
		
//		locationDistancePowerLaw(2241);
	}


}
