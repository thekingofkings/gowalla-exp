package probRela;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class CaseFinder {
	int K;
	ArrayList<int[]> friendPair;
	ArrayList<int[]> distantFriend;
	HashMap<Integer, HashSet<Integer>> friendMap;
	ArrayList<int[]> nonFriendMeeting;
	ArrayList<Integer> topKUser;
	HashMap<Integer, Integer> uid_rank;
	// a k-by-k matrix, tracking the meeting frequency of top k users.
	int[][] meetFreq;
	double[][] avgDistance;
	
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
		meetFreq = new int[K][K];
		avgDistance = new double[K][K];
		
		try {
			// get the top friends
			BufferedReader fin1 = new BufferedReader( new FileReader("../../dataset/userCount.txt"));
			for (int i = 0; i < K; i++) {
				String l = fin1.readLine();
				String[] ls = l.split("\\s+");
				int uid = Integer.parseInt(ls[0]);
				uid_rank.put(uid, i);
				topKUser.add(uid);
			}
			fin1.close();
			// get friends networks
			BufferedReader fin = new BufferedReader(new FileReader("../../dataset/Gowalla_edges.txt"));
			String l;
			while ((l = fin.readLine()) != null) {
				String[] ls = l.split("\\s+");
				int[] friends = new int[2];
				friends[0] = Integer.parseInt(ls[0]);
				friends[1] = Integer.parseInt(ls[1]);
				// only put the friend pair of top k users into friendPair
				if ( friends[0] < friends[1] && topKUser.contains(friends[0]) && topKUser.contains(friends[1])) {
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
	
	/*
	 * filterout duplicate friend pair
	 */
	@SuppressWarnings("unused")
	private boolean inFriendPair(int uaid, int ubid) {
		if (friendMap.containsKey(uaid) && friendMap.get(uaid).contains(ubid))
			return true;
		else if (friendMap.containsKey(ubid) && friendMap.get(ubid).contains(uaid))
			return true;
		else
			return false;
	}
	
	
	private static double distance(Record start, Record end) {
		double longiS = start.longitude;
		double latiS = start.latitude;
		double longiE = end.longitude;
		double latiE = end.latitude;
		return distance(longiS, latiS, longiE, latiE);
	}
	
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

	
	
	/*
	 * Calculate the meeting frequency of each user pair
	 * For the non-friends pair, we will focus on their meeting frequency
	 */
	public void allPairMeetingFreq() {
		long t_start = System.currentTimeMillis();
		for (int i = 0; i < K; i++) {
			for (int j = i+1; j < K; j++) {
				User ua = User.allUserSet.get(topKUser.get(i));
				User ub = User.allUserSet.get(topKUser.get(j));
				for (int aind = 0; aind < ua.records.size(); aind++)
					for (int bind = 0; bind < ub.records.size(); bind++) {
						Record ra = ua.records.get(i);
						Record rb = ub.records.get(j);

						// 1. count the meeting frequency
						if (ra.timestamp - rb.timestamp > 4 * 3600) {
							bind++;
							continue;
						} else if (rb.timestamp - ra.timestamp > 4 * 3600) {
							aind++;
							continue;
						} else {
							if (ra.locID == rb.locID ) {
								meetFreq[i][j] ++;
								meetFreq[j][i] ++;
							}
						}							
					}
			}
			
			// monitor the process
			if (i % (K/10) == 0)
				System.out.println(String.format("Process - freqAndDistance finished %d0%%", i/(K/10)));
		}
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("Finish in %d seconds", (t_end - t_start)/1000));
	}
	

	/*
	 * Write out the overall meeting frequency and average distance
	 * among the top k users.
	 */
	public void writeTopKFreq() {
		try {
			BufferedWriter fout = new BufferedWriter(new FileWriter("topk_freq.txt"));
			for (int i = 0; i < K; i++) {
				for (int j = i+1; j < K; j++) {
					// write out id_1, id_2, meeting frequency, distance
					fout.write(String.format("%d\t%d\t%d\n", topKUser.get(i), topKUser.get(j), meetFreq[i][j] ));
				}
			}
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/*
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
				for (Record ra : User.allUserSet.get(a).records) {
					for (Record rb : User.allUserSet.get(b).records) {
						// 1. calculate the average distance
						double d = distance(ra, rb);
						avgDistance[rank_a][rank_b] += d;
						avgDistance[rank_b][rank_a] += d;
						cnt ++;
					}
				}
				avgDistance[rank_a][rank_b] /= cnt;
				avgDistance[rank_b][rank_a] /= cnt;
				
				
				if (avgDistance[rank_a][rank_b] > 100) {
					int[] tuple = { a, b, (int) avgDistance[rank_a][rank_b] };
					distantFriend.add(tuple);
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
				// write out u_1, u_2, distance
				fout.write(String.format("%d\t%d\t%d\n", distantFriend.get(i)[0], distantFriend.get(i)[1], distantFriend.get(i)[2]));
			}
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * meeting frequency of non-friends
	 */
	public ArrayList<int[]> nonFriendsMeetingFreq() {
		for (int i = 0; i < K; i++ )
			for (int j = i+1; j < K; j++) {
				if (nonFriend(topKUser.get(i), topKUser.get(j)) && meetFreq[i][j] > 0) {
					int[] tuple = {topKUser.get(i), topKUser.get(j), meetFreq[i][j]};
					nonFriendMeeting.add(tuple);
				}
			}
		return nonFriendMeeting;
	}
	
	/*
	 * Assitant function for nonFriendsMeetingFreq
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
		CaseFinder cf = new CaseFinder(100);
		cf.allPairMeetingFreq();
		cf.writeTopKFreq();
		
		cf.remoteFriends();
		cf.writeRemoteFriend();
		
		cf.nonFriendsMeetingFreq();
		cf.writeNonFriendsMeeting();
//		test_distance();
	}


}
