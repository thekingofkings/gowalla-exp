package probRela;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;



/**
 * Class Tracker
 * ==================================
 * 
 * This class handles the various measures calculated from the location history of users.
 * The meeting event in this tracker is decided on the granularity of Gowalla check-in IDs.
 * This will provide higher accuracy when decide whether two users are meeting or not.
 * 
 * All these measures are used to represent the closeness of two users. More specifically
 * we have the following measures:
 * 		1. Renyi entropy based co-locating places diversity
 * 		2. Frequency weighted by the location entropy
 * 		3. Mutual information between two users' historic locations
 * 		4. Interestingness score calculated by product of individual probability
 * 		5. Frequency	(the dominant factor)
 * 		6. Mutual entropy on co-locating places
 * 		7. Relative mutual entropy
 * 
 * 	
 *  @author Hongjian	
 */
public class Tracker {

	// overall measure
	static LinkedList<Integer> numOfColocations = new LinkedList<Integer>();
	
	/// frequent measure, filtered from above
	/*
	 * FrequentPair has three integer fields:
	 * 		user_a_id, user_b_id, colocation_size
	 */
	static ArrayList<int[]> FrequentPair = new ArrayList<int[]>();
	static ArrayList<HashSet<Long>> FrequentPair_CoLocations = new ArrayList<HashSet<Long>>();
	static HashMap<Long, Double> locationEntropy = new HashMap<Long, Double>();
	static HashMap<String, Double> GPSEntropy = new HashMap<String, Double>();

	// results on three features
	static LinkedList<Double> renyiDiversity = new LinkedList<Double>();
	static LinkedList<Double> weightedFreq = new LinkedList<Double>();
	static LinkedList<Double> mutualInfo = new LinkedList<Double>();
	static LinkedList<Double> interestingness = new LinkedList<Double>();
	static LinkedList<Integer> frequency = new LinkedList<Integer>();
	static LinkedList<Double> mutualEntroColoc = new LinkedList<Double>();
	static LinkedList<Double> mutualEntroColoc_v3 = new LinkedList<Double>();
	static LinkedList<Double> relaMutualEntro = new LinkedList<Double>();
	
	/**
	 * Count the number of co-locations for each pair.
	 * Initialize two packet private static field:
	 * 			FrequentPair
	 * 			FrequentPair_Colocation
	 */
	public static LinkedList<Integer> shareLocationCount() {
		long t_start = System.currentTimeMillis();
		User.addAllUser();
		long t_mid = System.currentTimeMillis();
		System.out.println(String.format("Initialize all users in %d seconds", (t_mid - t_start)/1000));
		// User.findFrequentUsersByFreq(637);	
		// top 1000 users
		User.findFrequentUsersTopK(1000);
		HashMap<Integer, User> users = User.frequentUserSet;
		int cnt = 0;
		
		// get the first level iterator
		Object[] array = users.values().toArray();
		for (int i = 0; i < array.length; i++) {
			cnt ++;
			User ui = (User) array[i];

			// get the second level iterator
			for (int j = i + 1; j < array.length; j++) {
				User uj = (User) array[j];
				
				HashSet<Long> ui_loc = ui.getLocations();
				HashSet<Long> uj_loc = uj.getLocations();
				// get intersection of two sets
				HashSet<Long> colocations = new HashSet<Long>(ui_loc);
				colocations.retainAll(uj_loc);
				numOfColocations.add(colocations.size());
				
				// record the pair that share more than 10 common locations
				if (colocations.size() >= 1) {
					int[] p = new int[3];
					p[0] = (int) ui.userID;
					p[1] = (int) uj.userID;
					p[2] = colocations.size();
					FrequentPair.add(p);
					FrequentPair_CoLocations.add(colocations);
				}
				
			}
			
			// monitor the process
			if (cnt % (users.size()/10) == 0)
				System.out.println(String.format("Process - shareLocationCount finished %d0%%", cnt/(users.size()/10)));
		}
		
		// output
		try {
			BufferedWriter fout = new BufferedWriter(new FileWriter("colocation-cnt.txt"));
			BufferedWriter foutpair = new BufferedWriter(new FileWriter("frequent-pair.txt"));
		
			for (int i : numOfColocations) {
				fout.write(Integer.toString(i) + "\n");
			}
			
			for (int[] j : FrequentPair) {
				foutpair.write(Integer.toString(j[0]) + " " + Integer.toString(j[1]) + " "
						+ Integer.toString(j[2]) + "\n" );
			}
			fout.close();
			foutpair.close();
			} catch (IOException e) {
				e.printStackTrace();
		}
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("%d frequent pairs are found in %d seconds", FrequentPair.size(), (t_end - t_start)/1000));
		return numOfColocations;
	}
	
	
	private static void writeOutPairColocations() {
		System.out.println("Start writeOutPairColocations.");
		try {
			BufferedReader fin = new BufferedReader(new FileReader("topk_freqgt1-5000.txt"));
			BufferedWriter fout = new BufferedWriter(new FileWriter("topk_colocations-5000.txt"));
			String l = null;
			while ( (l=fin.readLine()) != null ) {
				String[] ls = l.split("\\s+");
				int uaid = Integer.parseInt(ls[0]);
				int ubid = Integer.parseInt(ls[1]);
				User ua = new User(uaid);
				User ub = new User(ubid);
				// get intersection of two sets
				HashSet<Long> ua_loc = ua.getLocations();
				HashSet<Long> ub_loc = ub.getLocations();
				ua_loc.retainAll(ub_loc);
				for (long loc : ua_loc)
					fout.write(loc + "\t");
				fout.write("\n");
			}
			fin.close();
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Process writeOutPairColocations ends.");
	}
	
	/*
	 * ===============================================================
	 * The first feature: diversity
	 * ===============================================================
	 * 
	 */
	
	/**
	 * Implement the first feature in SIGMOD'13
	 * The Renyi Entropy based diversity.
	 */
	public static LinkedList<Double> RenyiEntropyDiversity() {
		long t_start = System.currentTimeMillis();
		for (int i = 0; i < FrequentPair.size(); i++) {
			int uaid = FrequentPair.get(i)[0];
			int ubid = FrequentPair.get(i)[1];
			// 1. calculate the number of co-occurrence on each co-locating places
			HashSet<Long> coloc = FrequentPair_CoLocations.get(i);
			ArrayList<Long> coloc_list = new ArrayList<Long>(coloc);
			int coloc_num = coloc.size();
			int[] cnt = new int[coloc_num];
			// call the assistant function to calcualte the meeting frequency
			int sum = coLocationFreq (uaid, ubid, coloc_list, cnt);

			// 2. calculate the probability of co-occurrence
			double[] prob = new double[coloc_num];
			for (int j = 0; j < coloc_num; j++) {
				if (sum > 0)
					prob[j] = (double) cnt[j] / sum;
				//else
					//System.out.println("Sum is 0");
			}
			// 3. calculate the Renyi Entropy (q = 0.1)
			double renyiEntropy = 0;
			for (int j = 0; j < coloc_num; j++) {
					renyiEntropy += Math.pow(prob[j], 0.1);
			}
			renyiEntropy = - Math.log(renyiEntropy) / (-0.9);
			// 4. calculate diversity
			double divs = Math.exp(renyiEntropy);
			renyiDiversity.add(divs);
			// System.out.println(divs);
		}
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("Renyi entropy based diversity found in %d seconds!", (t_end - t_start)/1000));
		return renyiDiversity;
	}
	
	
	/**
	 * Assistant function for Renyi entropy based diversity.
	 * calculate the colocation frequency given two user IDs and the location.
	 * 
	 * the cnt[] is the meeting frequency at each different locations
	 * the return value (sum) is the totoal meeting frequency
	 */
	private static int coLocationFreq( int user_a_id, int user_b_id, ArrayList<Long> loc_list, int cnt[] ) {
		LinkedList<Record> ras = User.allUserSet.get(user_a_id).records;
		LinkedList<Record> rbs = User.allUserSet.get(user_b_id).records;
		int sum = 0;
		// find records of user_a in colocating places
		int aind = 0;
		int bind = 0;
		long last_Meet = 0;
		while (aind < ras.size() && bind < rbs.size()) {
			Record ra = ras.get(aind);
			Record rb = rbs.get(bind);
			
			// count the frequency
			if (ra.timestamp - rb.timestamp > 3600 * 4) {
				bind ++;
				continue;
			} else if (rb.timestamp - ra.timestamp > 3600 * 4) {
				aind ++;
				continue;
			} else {
				if (ra.locID == rb.locID && ra.timestamp  - last_Meet >= 3600) {
					int lid = loc_list.indexOf(ra.locID);
					cnt[lid] ++;
					sum ++;
					last_Meet = ra.timestamp;
				}
				aind ++;
				bind ++;
			}
			
		}
		return sum;
	}
	
	
	/*
	 * ===============================================================
	 * The second feature: weighted frequency
	 * ===============================================================
	 * 
	 */
	
	
	private static void initialTopKPair() {
		System.out.println("Start initialTopKPair.");
		try {
			BufferedReader fin = new BufferedReader(new FileReader("topk_freq-1000.txt"));
			String l = null;
			BufferedReader fin2 = new BufferedReader(new FileReader("topk_colocations-1000.txt"));
			String l2 = null;
			int c1 = 0;
			int c2 = 0;
			while ( (l=fin.readLine()) != null ) {
				c1 ++;
				String[] ls = l.split("\\s+");
				if (Integer.parseInt(ls[2]) > 0) {
					int uaid = Integer.parseInt(ls[0]);
					int ubid = Integer.parseInt(ls[1]);
					new User(uaid);
					new User(ubid);
					int friFlag = Integer.parseInt(ls[3]);
					int[] fp = {uaid, ubid, friFlag};
					FrequentPair.add(fp);
					
					while (c2 < c1) {
						c2 ++;
						l2 = fin2.readLine();
						if (c2 == c1) {
							HashSet<Long> colocs = new HashSet<Long>();
							String[] ls2 = l2.split("\\s+");
							for (String s : ls2)
								colocs.add(Long.parseLong(s));
							FrequentPair_CoLocations.add(colocs);
						}
					}
				}
			}
			fin.close();
			fin2.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Process initialTopKPair ends.");
	}
	
	
	
	/**
	 * Implement the second feature of SIGMOD'13
	 * The weighted frequency (weight by location entropy)
	 */
	public static LinkedList<Double> weightedFrequency() {
		long t_start = System.currentTimeMillis();
		/*
		 * The calculation of location entropy is not efficient.
		 * The original execution time in HP laptop is 470s.
		 * After we factor out the location entropy calculation, 
		 * now the execution time in HP is 4s.
		 */
		locationEntropy = readLocationEntropyIDbased(1000);
		for (int i = 0; i < FrequentPair.size(); i++) {
			int uaid = FrequentPair.get(i)[0];
			int ubid = FrequentPair.get(i)[1];
			// 1. calculate the frequency of co-occurrence on each co-locating places
			HashSet<Long> coloc = FrequentPair_CoLocations.get(i);
			
			// only consider people with meeting events
			if (coloc.size() > 0) {
				double weightedFrequency = 0;
				ArrayList<Long> clarray = new ArrayList<Long>(coloc);
				int coloc_num = coloc.size();
				int[] freq = new int[coloc_num];
				int frequen = coLocationFreq (uaid, ubid, clarray, freq);
				frequency.add(frequen);
				
				// 2. calculate location entropy
				for (int j = 0; j < coloc_num; j++) {
					long locid = clarray.get(j);
					weightedFrequency += freq[j] * Math.exp(- locationEntropy.get(locid));
				}
				weightedFreq.add(weightedFrequency);
			}
			
				
			// monitor the process
			if (i % (FrequentPair.size()/10) == 0)
				System.out.println(String.format("Process - weightedFrequency finished %d0%%", i/(FrequentPair.size()/10)));
		}
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("Weighted frequency found in %d seconds", (t_end - t_start)/1000));
		return weightedFreq;
	}

	
	
	private static void writePairMeasure() {
		System.out.println("Start writeWeightedFreq");
		try {
			BufferedWriter fout = new BufferedWriter(new FileWriter("weightedFrequency.txt"));
			for (int i = 0; i < FrequentPair.size(); i++) {
				int uaid = FrequentPair.get(i)[0];
				int ubid = FrequentPair.get(i)[1];
				// id_a, id_b, weighted frequency, frequency, co-locatoin entropy, friends flag
				fout.write(String.format("%d\t%d\t%g\t%d\t%g\t%d\n", uaid, ubid, weightedFreq.get(i), frequency.get(i), renyiDiversity.get(i), FrequentPair.get(i)[2]));
			}
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Start writeWeightedFreq");
	}

	
	//===============================location entropy begins===================================
	/**
	 * Assistant function
	 * -- calculate location entropy for one specific location (Shannon Entropy)
	 */
	private static HashMap<Long, Double> locationEntropyIDbased() {
		long t_start = System.currentTimeMillis();
		HashMap<Long, Double> loc_entro = new HashMap<Long, Double>();
		HashMap<Long, HashMap<Integer, Integer>> loc_user_visit = new HashMap<Long, HashMap<Integer, Integer>>();
		HashMap<Long, Integer> loc_total_visit = new HashMap<Long, Integer>();
		
		// 1. get the location visiting frequency
		for (User u : User.allUserSet.values()) {
			for (Record r : u.records) {
				// count individual user visiting
				if (loc_user_visit.containsKey(r.locID)) {
					if (loc_user_visit.get(r.locID).containsKey(u.userID)) {
						int freq = loc_user_visit.get(r.locID).get(u.userID);
						loc_user_visit.get(r.locID).put(u.userID, freq + 1);
					} else {
						loc_user_visit.get(r.locID).put(u.userID, 1);
					}
				} else {
					loc_user_visit.put(r.locID, new HashMap<Integer, Integer>());
					loc_user_visit.get(r.locID).put(u.userID, 1);
				}
				// count total visiting for one location
				if (loc_total_visit.containsKey(r.locID)) {
					int f = loc_total_visit.get(r.locID);
					loc_total_visit.put(r.locID, f+1);
				} else {
					loc_total_visit.put(r.locID, 1);
				}
			}
		}
		// 2. calculate the per user probability
		for (Long locid : loc_user_visit.keySet()) {
			double locEntropy = 0;
			for (int uid : loc_user_visit.get(locid).keySet()) {
				if (loc_user_visit.get(locid).size() > 1) {	// if there is only one user visit this locatin, then its entropy is 0, and there won't be any meeting events.
					if (loc_user_visit.get(locid).get(uid) > 0) {
						double prob = (double) loc_user_visit.get(locid).get(uid) / loc_total_visit.get(locid);
						locEntropy += - prob * Math.log(prob);
					}
				}
			}
			loc_entro.put(locid, locEntropy);
		}
		// 3. return the entropy
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("Size of loc_entropy: %d.\n locationEntropyIDbased finished in %d seconds.", loc_entro.size(), (t_end - t_start)/1000));
		return loc_entro;
	}
	
	
	private static HashMap<String, Double> locationEntropyGPSbased() {
		long t_start = System.currentTimeMillis();
		HashMap<String, Double> loc_entro = new HashMap<String, Double>();
		HashMap<String, HashMap<Integer, Integer>> loc_user_visit = new HashMap<String, HashMap<Integer, Integer>>();
		HashMap<String, Integer> loc_total_visit = new HashMap<String, Integer>();

		// 1. get the GPS visiting frequency
		for (User u : User.allUserSet.values()) {
			for (Record r : u.records) {
				// count individual user visiting
				if (loc_user_visit.containsKey(r.GPS())) {
					if (loc_user_visit.get(r.GPS()).containsKey(u.userID)) {
						int freq = loc_user_visit.get(r.GPS()).get(u.userID);
						loc_user_visit.get(r.GPS()).put(u.userID, freq + 1);
					} else {
						loc_user_visit.get(r.GPS()).put(u.userID, 1);
					}
				} else {
					loc_user_visit.put(r.GPS(), new HashMap<Integer, Integer>());
					loc_user_visit.get(r.GPS()).put(u.userID, 1);
				}
				// count total visiting for on location
				if (loc_total_visit.containsKey(r.GPS())) {
					int f = loc_total_visit.get(r.GPS());
					loc_total_visit.put(r.GPS(), f+1);
				} else {
					loc_total_visit.put(r.GPS(), 1);
				}
			}
		}
		// 2. calculate the per user probability
		for (String gps : loc_user_visit.keySet()) {
			double locEntropy = 0;
			for (int uid : loc_user_visit.get(gps).keySet()) {
				if (loc_user_visit.get(gps).size() > 1)
					if (loc_user_visit.get(gps).get(uid) > 0) {
						double prob = (double) loc_user_visit.get(gps).get(uid) / loc_total_visit.get(gps);
						locEntropy += - prob * Math.log(prob);
					}
			}
			loc_entro.put(gps, locEntropy);
		}
		// 3. return the entropy
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("Size of loc_entropy: %d.\n locationEntropyGPSbased finished in %d seconds.", loc_entro.size(), (t_end - t_start)/1000));
		return loc_entro;
	}
	
	
	/**
	 * calculate the location entropy using the records of given number of top users
	 * @param numUser
	 * @param IDflag -- true to use location ID, false to use GPS 
	 */
	public static void writeLocationEntropy(int numUser, boolean IDflag) {
		// initialize users
		int c = 0;
		try {
			BufferedReader fin = new BufferedReader(new FileReader("../../dataset/userCount.txt"));
			String l = null;
			while ( (l=fin.readLine()) != null && c < numUser) {
				c++;
				String[] ls = l.split("\\s+");
				int uid = Integer.parseInt(ls[0]);
				new User(uid);
			}
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (IDflag == true) {
			// calculate location entropy
			locationEntropy = locationEntropyIDbased();
		} else {
			GPSEntropy = locationEntropyGPSbased();
		}
		// write out location entropy
		try {
			BufferedWriter fout;
			if (IDflag == true) {
				fout = new BufferedWriter(new FileWriter(String.format("locationEntropy-%d.txt", c)));
				for (long loc : locationEntropy.keySet())
					fout.write(String.format("%d\t%g\n", loc, locationEntropy.get(loc)));
			} else {
				fout = new BufferedWriter(new FileWriter(String.format("GPSEntropy-%d.txt", c)));
				for (String gps : GPSEntropy.keySet())
					fout.write(String.format("%s\t%g\n", gps, GPSEntropy.get(gps)));
			}
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * read in the location entropy from corresponding file</br>
	 * If we have the location entropy file of given number of users, then this function will work, otherwise
	 * it will throw an exception.
	 * @param numUser
	 * @return
	 */
	public static HashMap<Long, Double> readLocationEntropyIDbased(int numUser) {
		if (locationEntropy.isEmpty()) {
			try {
				BufferedReader fin = new BufferedReader( new FileReader(String.format("locationEntropy-%d.txt", numUser)));
				String l = null;
				while ((l = fin.readLine()) != null) {
					String[] ls = l.split("\\s+");
					long loc = Long.parseLong(ls[0]);
					double entropy = Double.parseDouble(ls[1]);
					if (!locationEntropy.containsKey(loc))
						locationEntropy.put(loc, entropy);
				}
				fin.close();
			} catch (FileNotFoundException e) {
				System.out.println("No location entropy file found. Generate new one ...");
				writeLocationEntropy(numUser, true);	// true use location ID, false to use GPS
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println(String.format("Location entropy size %d.", locationEntropy.size()));
		}
		
		return locationEntropy;
	}
	
	
	public static HashMap<String, Double> readLocationEntropyGPSbased( int numUser ) {
		if (GPSEntropy.isEmpty()) {
			try {
				BufferedReader fin = new BufferedReader( new FileReader(String.format("GPSEntropy-%d.txt", numUser)));
				String l = null;
				while ( (l=fin.readLine()) != null) {
					String[] ls = l.split("\\s+");
					String gps = ls[0];
					double entropy = Double.parseDouble(ls[1]);
					if (! GPSEntropy.containsKey(gps))
						GPSEntropy.put(gps, entropy);
				}
				fin.close();
			} catch (FileNotFoundException e) {
				System.out.println("No GPS entropy file found. Generate a new one ...");
				writeLocationEntropy(numUser, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println(String.format("GPS location size %d.", GPSEntropy.size()));
		}
		return GPSEntropy;
	}
	//===================================location entropy ends=======================================
	
	/*
	 * ===============================================================
	 * The third feature: mutual information
	 * ===============================================================
	 * 
	 */
	
	/**
	 * use mutual information between two users to measure their correlation
	 * Here we use the formula:
	 * 		I(x,y) = H(x) + H(y) - H(x,y)
	 * to calculate the mutual entropy
	 * 
	 * This mutual information is calculated w.r.t. the complete location set.
	 */
	public static LinkedList<Double> mutualInformation() {
		long t_start = System.currentTimeMillis();
		/*
		 * Speed boost
		 * the old method is not efficient, because it is not necessary to calculate the 
		 * marginal entropy repeatedly.
		 */
		HashMap<Integer, Double> entro = new HashMap<Integer,Double>();
		for (int i = 0; i < FrequentPair.size(); i++) {
			int uaid = FrequentPair.get(i)[0];
			int ubid = FrequentPair.get(i)[1];
			// 1. calculate the marginal entropy of user a
			double entroA;
			if (entro.containsKey(uaid))
				entroA = entro.get(uaid);
			else {
				entroA = marginalEntropy(uaid);
				entro.put(uaid, entroA);
			}
			// 2. calculate the marginal entropy of user b
			double entroB;
			if (entro.containsKey(ubid))
				entroB = entro.get(ubid);
			else {
				entroB = marginalEntropy(ubid);
				entro.put(ubid, entroB);
			}
			// 3. joint entropy of A and B
			double jointEntro = jointEntropy(uaid, ubid);
			// 4. mutual information
			mutualInfo.add(entroA + entroB - jointEntro);
			
			// monitor the process
			if (i % (FrequentPair.size()/10) == 0)
				System.out.println(String.format("Process - mutualInformation finished %d0%%", i/(FrequentPair.size()/10)));
		}
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("Calculate mutual inforamtion in %d seconds", (t_end - t_start)/1000));
		return mutualInfo;
	}
	
	/**
	 * Assistant function for mutual entropy 
	 * -- calculate marginal entropy w.r.t. the complete historical locations
	 */
	private static double marginalEntropy(int uid) {
		User u = User.allUserSet.get(uid);
		HashMap<Long, Integer> locFreq = new HashMap<Long, Integer>();
		// 1. count frequency
		int totalLocationNum = u.records.size();
		for (Record r : u.records) {
			if (locFreq.containsKey(r.locID))
				locFreq.put(r.locID, locFreq.get(r.locID) + 1);
			else
				locFreq.put(r.locID, 1);
		}
		// 2. probability and entropy
		double prob = 0;
		double entro = 0;
		for (Long i : locFreq.keySet()) {
			prob = (double) locFreq.get(i) / totalLocationNum;
			entro += - prob * Math.log(prob);
		}
		return entro;
	}
	
	/**
	 * Assistant function for mutual entropy -- calculate joint entropy
	 * 
	 * One problem is whether use the synchronized time series to index the records.
	 * This dataset is the checkin data from gowalla, which is really sparse.
	 * The record interpolation is impossible. Generally, each user have less than 1
	 * check-in record each day.
	 * 
	 * My solution is to go through all records, then determine whether their timestamp
	 * are within one timeslot.
	 * 
	 * Even though, the observation on both users at the same time slot is an rare events.
	 * Therefore, I have to use the permutation to produce more fundamental event to 
	 * approximate the ground truth.
	 */
	private static double jointEntropy( int uaid, int ubid ) {
		User a = User.allUserSet.get(uaid);
		User b = User.allUserSet.get(ubid);
		HashMap<Long, HashMap<Long, Integer>> locFreq = new HashMap<>();
		
		// 1. count frequency of multi-variables distribution
		int totalCase = 0;
		for (Record ar : a.records) {
			for (Record br : b.records) {
				// ar and br are in the same time slots
				//if (Math.abs(ar.timestamp - br.timestamp) <= 4 * 3600) {
					// put that observation at the same timeslot into the two level maps
					if (locFreq.containsKey(ar.locID) && locFreq.get(ar.locID).containsKey(br.locID)) {
						int f = locFreq.get(ar.locID).get(br.locID) + 1;
						locFreq.get(ar.locID).put(br.locID, f);
						totalCase ++;
					}
					else if (locFreq.containsKey(ar.locID) && ! locFreq.get(ar.locID).containsKey(br.locID)) {
						locFreq.get(ar.locID).put(br.locID, 1);
						totalCase ++;
					}
					else if (!locFreq.containsKey(ar.locID)) {
						locFreq.put(ar.locID, new HashMap<Long, Integer>());
						locFreq.get(ar.locID).put(br.locID, 1);
						totalCase ++;
					}
				//}
			}
		}
		//System.out.println(String.format("Total case of joint entropy %d", totalCase));
		// 2. probability and entropy
		double prob = 0;
		double entro = 0;
		for (Long i : locFreq.keySet()) {
			for (Long j : locFreq.get(i).keySet()) {
				prob = (double) locFreq.get(i).get(j) / totalCase;
				entro += - prob * Math.log(prob);
			}
		}
		return entro;
	}
	
	
	/**
	 * Calculate mutual information from the definition:
	 * 		I(x,y) = \sum \sum p(x,y) log (p(x,y) / (p(x)p(y)) )
	 */
	public static LinkedList<Double> mutualInformation_v2() {
		long t_start = System.currentTimeMillis();
		// 1. calculate the individual probability
		HashMap<Integer, HashMap<Long, Double>> user_loc_prob = new HashMap<Integer, HashMap<Long, Double>>();
		for (int[] p : FrequentPair) {
			for (int i = 0; i < 2; i++ ) {
				int uid = p[i];
				for (Record ra : User.frequentUserSet.get(uid).records) {
					if (user_loc_prob.containsKey(uid)) {
						if (user_loc_prob.get(uid).containsKey(ra.locID)) {
							double f = user_loc_prob.get(uid).get(ra.locID);
							user_loc_prob.get(uid).put(ra.locID, f + 1);
						} else {
							user_loc_prob.get(uid).put(ra.locID, 1.0);
						}
					} else {
						user_loc_prob.put(uid, new HashMap<Long, Double>());
						user_loc_prob.get(uid).put(ra.locID, 1.0);
					}
				}
				int cnt = User.frequentUserSet.get(uid).records.size();
				for (Long locid : user_loc_prob.get(uid).keySet()) {
					double freq = user_loc_prob.get(uid).get(locid);
					user_loc_prob.get(uid).put(locid, freq / cnt);
				}
			}
		}
		
		for (int i = 0; i < FrequentPair.size(); i++ ) {
			// 2. calculate the pair frequency
			HashMap<Long, HashMap<Long, Double>> pairLocProb = new HashMap<>();
			int uaid = FrequentPair.get(i)[0];
			int ubid = FrequentPair.get(i)[1];
			int totalCase = 0;
			for (Record ar : User.frequentUserSet.get(uaid).records) {
				for (Record br : User.frequentUserSet.get(ubid).records) {
					if (pairLocProb.containsKey(ar.locID) && pairLocProb.get(ar.locID).containsKey(br.locID)) {
						double f = pairLocProb.get(ar.locID).get(br.locID) + 1;
						pairLocProb.get(ar.locID).put(br.locID, f);
						totalCase ++;
					}
					else if (pairLocProb.containsKey(ar.locID) && ! pairLocProb.get(ar.locID).containsKey(br.locID)) {
						pairLocProb.get(ar.locID).put(br.locID, 1.0);
						totalCase ++;
					}
					else if (!pairLocProb.containsKey(ar.locID)) {
						pairLocProb.put(ar.locID, new HashMap<Long, Double>());
						pairLocProb.get(ar.locID).put(br.locID, 1.0);
						totalCase ++;
					}
				}
			}
			double mutualE = 0;
			for (Long l1 : pairLocProb.keySet()) {
				for (Long l2: pairLocProb.get(l1).keySet()) {
					// 3. calculate the pair probability
					double f = pairLocProb.get(l1).get(l2);
					double pairProb = f / totalCase;
					// 4. calculate the mutual information
					mutualE += pairProb * Math.log(pairProb / user_loc_prob.get(uaid).get(l1) / user_loc_prob.get(ubid).get(l2));
				}
			}
			mutualInfo.add(mutualE);
			// monitor the process
			if (i % (FrequentPair.size()/10) == 0)
				System.out.println(String.format("Process - mutualInformation_v2 finished %d0%%", i/(FrequentPair.size()/10)));
		}
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("mutualInformation_v2 executed for %d seconds", (t_end-t_start)/1000));
		return mutualInfo;
	}
	
	/*
	 * ==========================================================================
	 * The fourth feature: Fei's interestingness
	 * ==========================================================================
	 * 
	 */
	
	/**
	 * Calculate the interestingness score defined by Fei's PAKDD submission.
	 */
	public static LinkedList<Double> interestingnessPAKDD() {
		long t_start = System.currentTimeMillis();
		for (int i = 0; i < FrequentPair.size(); i++ ) {
			int uaid = FrequentPair.get(i)[0];
			int ubid = FrequentPair.get(i)[1];
			HashSet<Long> colocs = FrequentPair_CoLocations.get(i);
			// 1. calculate the colocating interestness
			double Finterest = coLocationScore(uaid, ubid, colocs);
			interestingness.add(Finterest);
			// monitor the process
			if (i % (FrequentPair.size()/10) == 0)
				System.out.println(String.format("Process - interestingnessPAKDD finished %d0%%", i/(FrequentPair.size()/10)));	
		}
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("Interestingness score found in %d seconds", (t_end - t_start)/1000));
		return interestingness;
	}
	
	/**
	 * calculate the colocation score directly
	 */
	private static double coLocationScore( int user_a_id, int user_b_id, HashSet<Long> colocs ) {
		LinkedList<Record> ra = User.frequentUserSet.get(user_a_id).records;
		HashMap<Long, LinkedList<Record>> raco = new HashMap<Long, LinkedList<Record>>();
		LinkedList<Record> rb = User.frequentUserSet.get(user_b_id).records;
		HashMap<Long, LinkedList<Record>> rbco = new HashMap<Long, LinkedList<Record>>();
		double interest = 0;
		// System.out.println(String.format("Colocs size %d", colocs.size()));
		// find the reverse map of location count
		for (Record r : ra) {
			if (raco.containsKey(r.locID)) {
				raco.get(r.locID).add(r);
			} else {
				raco.put(r.locID, new LinkedList<Record>());
				raco.get(r.locID).add(r);
			}
		}
		for (Record r : rb) {
			if (rbco.containsKey(r.locID)) {
				rbco.get(r.locID).add(r);
			} else {
				rbco.put(r.locID, new LinkedList<Record>());
				rbco.get(r.locID).add(r);
			}
		}
		for (Long loc_id : colocs) {
			// judge their colocating events with time
			for (Record r1 : raco.get(loc_id)) {
				for (Record r2 : rbco.get(loc_id)) {
					// We identify the colocating event with a 4-hour time window
					if (r1.timestamp - r2.timestamp <= 3600 * 4)
						interest += - Math.log( (double) raco.get(loc_id).size() / ra.size()) 
							- Math.log( (double) rbco.get(loc_id).size() / rb.size() );
				}
			}
		}
		return interest;
	}
	
	
	
	/*
	 * ======================================================
	 * The fifth feature: Mutual entropy on Co-locations
	 * ======================================================
	 * 
	 * We still use the mutual entropy:
	 * 		I(X,Y) = H(X) + H(Y) - H(X,Y)
	 * 
	 * But, the difference between this feature and the third feature is that
	 * we only focuse on the set of locations where two users co-locate.
	 * 
	 */
	
	public static LinkedList<Double> mutualEntropyOnColocation() {
		long t_start = System.currentTimeMillis();
		// calculate the marginal entropy of each user only once.
		HashMap<Integer, Double> entro = new HashMap<Integer, Double>();
		for (int i = 0; i < FrequentPair.size(); i++) {
			// get two user
			User a = User.frequentUserSet.get(FrequentPair.get(i)[0]);
			User b = User.frequentUserSet.get(FrequentPair.get(i)[1]);
			HashSet<Long> locs = FrequentPair_CoLocations.get(i);
			// 1. calculate the marginal entropy of U_a over colocations
			double entroA = 0;
			if (entro.containsKey(a.userID)) {
				entroA = entro.get(a.userID);
			} else {
				entroA = marginalEntropy(a.userID, locs );
				entro.put(a.userID, entroA);
			}
			// 2. calculate the marginal entropy of U_b over colocations
			double entroB = 0;
			if (entro.containsKey(b.userID)) {
				entroB = entro.get(b.userID);
			} else {
				entroB = marginalEntropy(b.userID, locs);
				entro.put(b.userID, entroB);
			}
			// 3. calculate the joint entropy of U_a and U_b over 
			double joint_entro = jointEntropy(a.userID, b.userID, locs);
			mutualEntroColoc.add(entroA + entroB - joint_entro);
			
			// monitor the process
			if (i % (FrequentPair.size()/10) == 0)
				System.out.println(String.format("Process - mutualEntropyOnColocation finished %d0%%", i/(FrequentPair.size()/10)));
		}
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("mutualEntropyOnColocation executed %d seconds", (t_end - t_start)/1000));
		return mutualEntroColoc;
	}
	
	/** 
	 * Assistant function for mutual information calculation
	 * 
	 * -- calculate the marginal entropy of one user over the given location set
	 */
	private static double marginalEntropy(int uid, HashSet<Long> locs) {
		User u = User.allUserSet.get(uid);
		HashMap<Long, Integer> locFreq = new HashMap<Long, Integer>();
		// 1. count frequency on each locations in location set
		int totalLocationNum = 0;
		for (Record r : u.records) {
			if (locs.contains(r.locID)) {
				if (locFreq.containsKey(r.locID)) {
					totalLocationNum ++;
					locFreq.put(r.locID, locFreq.get(r.locID) + 1);
				}
				else {
					totalLocationNum ++;
					locFreq.put(r.locID, 1);
				}
			}
		}
		
		// 2. probability and entropy
		double prob = 0;
		double entro = 0;
		for (Long i : locFreq.keySet()) {
			prob = (double) locFreq.get(i) / totalLocationNum;
			entro += - prob * Math.log(prob);
		}
		return entro;
	}
	
	/**
	 * Assistant function for joint information calculation
	 * 
	 * -- calculate the joint entropy of two users over the given location set
	 */
	private static double jointEntropy( int uaid, int ubid, HashSet<Long> locs ) {
		User a = User.allUserSet.get(uaid);
		User b = User.allUserSet.get(ubid);
		HashMap<Long, HashMap<Long, Integer>> locFreq = new HashMap<>();
		// 1. count frequency over given location set
		int totalCase = 0;
		for (Record ar : a.records) {
			for (Record br : b.records) {
				// Two records in same timeslot
				//if (Math.abs(ar.timestamp-br.timestamp) <= 4 * 3600) {
					// count frequency only on the target set
					if (locs.contains(ar.locID) && locs.contains(br.locID)) {
						if (locFreq.containsKey(ar.locID) && locFreq.get(ar.locID).containsKey(br.locID)) {
							int f = locFreq.get(ar.locID).get(br.locID) + 1;
							locFreq.get(ar.locID).put(br.locID, f);
							totalCase ++;
						}
						else if (locFreq.containsKey(ar.locID) && ! locFreq.get(ar.locID).containsKey(br.locID)) {
							locFreq.get(ar.locID).put(br.locID, 1);
							totalCase ++;
						}
						else if (!locFreq.containsKey(ar.locID)) {
							locFreq.put(ar.locID, new HashMap<Long, Integer>());
							locFreq.get(ar.locID).put(br.locID, 1);
							totalCase ++;
						}
					}
				//}
			}
		}
		// 2. probability and entropy
		double prob = 0;
		double entro = 0;
		for (Long i : locFreq.keySet()) {
			for (Long j : locFreq.get(i).keySet()) {
				prob = (double) locFreq.get(i).get(j) / totalCase;
				entro += - prob * Math.log(prob);
			}
		}
		return entro;
	}
	
	
	
	/**
	 * Calculate mutual information over co-locations given set from the definition:
	 * 		I(x,y) = \sum \sum p(x,y) log (p(x,y) / (p(x)p(y)) )
	 */
	public static LinkedList<Double> mutualEntropyOnColocation_v2() {
		long t_start = System.currentTimeMillis();
		// 1. calculate the individual probability
		HashMap<Integer, HashMap<Long, Double>> user_loc_prob = new HashMap<Integer, HashMap<Long, Double>>();
		for (int i = 0; i < FrequentPair.size(); i++) {
			int[] p = FrequentPair.get(i);
			// get given target locatoin set
			HashSet<Long> locs = FrequentPair_CoLocations.get(i);
			for (int j = 0; j < 2; j++ ) {
				int uid = p[j];
				int cnt = 0;
				for (Record ra : User.frequentUserSet.get(uid).records) {
					if (locs.contains(ra.locID)) {
						if (user_loc_prob.containsKey(uid)) {
							if (user_loc_prob.get(uid).containsKey(ra.locID)) {
								double f = user_loc_prob.get(uid).get(ra.locID);
								user_loc_prob.get(uid).put(ra.locID, f + 1);
							} else {
								user_loc_prob.get(uid).put(ra.locID, 1.0);
							}
						} else {
							user_loc_prob.put(uid, new HashMap<Long, Double>());
							user_loc_prob.get(uid).put(ra.locID, 1.0);
						}
						cnt ++;
					}
				}
				
				for (Long locid : user_loc_prob.get(uid).keySet()) {
					double freq = user_loc_prob.get(uid).get(locid);
					user_loc_prob.get(uid).put(locid, freq / cnt);
				}
			}
		}
		
		for (int i = 0; i < FrequentPair.size(); i++ ) {
			// 2. calculate the pair frequency
			HashMap<Long, HashMap<Long, Double>> pairLocProb = new HashMap<>();
			int uaid = FrequentPair.get(i)[0];
			int ubid = FrequentPair.get(i)[1];
			HashSet<Long> locs = FrequentPair_CoLocations.get(i);
			int totalCase = 0;
			for (Record ar : User.frequentUserSet.get(uaid).records) {
				for (Record br : User.frequentUserSet.get(ubid).records) {
					if (locs.contains(ar.locID) && locs.contains(br.locID)) {
						if (pairLocProb.containsKey(ar.locID) && pairLocProb.get(ar.locID).containsKey(br.locID)) {
							double f = pairLocProb.get(ar.locID).get(br.locID) + 1;
							pairLocProb.get(ar.locID).put(br.locID, f);
							totalCase ++;
						}
						else if (pairLocProb.containsKey(ar.locID) && ! pairLocProb.get(ar.locID).containsKey(br.locID)) {
							pairLocProb.get(ar.locID).put(br.locID, 1.0);
							totalCase ++;
						}
						else if (!pairLocProb.containsKey(ar.locID)) {
							pairLocProb.put(ar.locID, new HashMap<Long, Double>());
							pairLocProb.get(ar.locID).put(br.locID, 1.0);
							totalCase ++;
						}
					}
				}
			}
			double mutualE = 0;
			for (Long l1 : pairLocProb.keySet()) {
				for (Long l2: pairLocProb.get(l1).keySet()) {
					// 3. calculate the pair probability
					double f = pairLocProb.get(l1).get(l2);
					double pairProb = f / totalCase;
					// 4. calculate the mutual information
					mutualE += pairProb * Math.log(pairProb / user_loc_prob.get(uaid).get(l1) / user_loc_prob.get(ubid).get(l2));
				}
			}
			mutualEntroColoc.add(mutualE);
			// monitor the process
			if (i % (FrequentPair.size()/10) == 0)
				System.out.println(String.format("Process - mutualEntroColocation_v2 finished %d0%%", i/(FrequentPair.size()/10)));
		}
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("mutualEntroColocation_v2 executed for %d seconds", (t_end-t_start)/1000));
		return mutualEntroColoc;
	}
	
	
	/**
	 * Calculate mutual information over colocation from the definition:
	 * 		I(x,y) = \sum \sum p(x,y) log (p(x,y) / (p(x)p(y)) )
	 */
	public static LinkedList<Double> mutualEntropyOnColocation_v3() {
		long t_start = System.currentTimeMillis();
		// 1. calculate the individual probability
		HashMap<Integer, HashMap<Long, Double>> user_loc_prob = new HashMap<Integer, HashMap<Long, Double>>();
		for (int i = 0; i < FrequentPair.size(); i++) {
			int[] p = FrequentPair.get(i);
			// get given target locatoin set
			HashSet<Long> locs = FrequentPair_CoLocations.get(i);
			for (int j = 0; j < 2; j++ ) {
				int uid = p[j];
				int cnt = 0;
				for (Record ra : User.frequentUserSet.get(uid).records) {
					if (locs.contains(ra.locID)) {
						if (user_loc_prob.containsKey(uid)) {
							if (user_loc_prob.get(uid).containsKey(ra.locID)) {
								double f = user_loc_prob.get(uid).get(ra.locID);
								user_loc_prob.get(uid).put(ra.locID, f + 1);
							} else {
								user_loc_prob.get(uid).put(ra.locID, 1.0);
							}
						} else {
							user_loc_prob.put(uid, new HashMap<Long, Double>());
							user_loc_prob.get(uid).put(ra.locID, 1.0);
						}
						cnt ++;
					}
				}
				
				for (Long locid : user_loc_prob.get(uid).keySet()) {
					double freq = user_loc_prob.get(uid).get(locid);
					user_loc_prob.get(uid).put(locid, freq / cnt);
				}
			}
		}
		
		for (int i = 0; i < FrequentPair.size(); i++ ) {
			// 2. calculate the pair frequency
			HashMap<Long, Double> pairLocProb = new HashMap<>();
			int uaid = FrequentPair.get(i)[0];
			int ubid = FrequentPair.get(i)[1];
			HashSet<Long> locs = FrequentPair_CoLocations.get(i);
			int totalCase = 0;
			for (Record ar : User.frequentUserSet.get(uaid).records) {
				for (Record br : User.frequentUserSet.get(ubid).records) {
					// ar and br are same location
					if (locs.contains(ar.locID) && ar.locID == br.locID) {
						if (pairLocProb.containsKey(ar.locID)) {
							double f = pairLocProb.get(ar.locID) + 1;
							pairLocProb.put(ar.locID, f);
							totalCase ++;
						} else {
							pairLocProb.put(ar.locID, 1.0);
							totalCase ++;
						}
					}
				}
			}
			double mutualE = 0;
			for (Long l : pairLocProb.keySet()) {
				// 3. calculate the pair probability
				double f = pairLocProb.get(l);
				double pairProb = f / totalCase;
				// 4. calculate the mutual information
				mutualE += pairProb * Math.log(pairProb / user_loc_prob.get(uaid).get(l) / user_loc_prob.get(ubid).get(l));
			}
			mutualEntroColoc_v3.add(mutualE);
			// monitor the process
			if (i % (FrequentPair.size()/10) == 0)
				System.out.println(String.format("Process - mutualEntroColocation_v3 finished %d0%%", i/(FrequentPair.size()/10)));
		}
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("mutualEntroColocation_v3 executed for %d seconds", (t_end-t_start)/1000));
		return mutualEntroColoc_v3;
	}
	
	
	public static LinkedList<Double> relativeMutualEntropy() {
		for (int i = 0; i < FrequentPair.size(); i++) {
			int uaid = FrequentPair.get(i)[0];
			int ubid = FrequentPair.get(i)[1];
			HashSet<Long> locs = FrequentPair_CoLocations.get(i);
			double entroA = marginalEntropy(uaid, locs);
			double entroB = marginalEntropy(ubid, locs);
			System.out.println(String.format("entro A %g, B %g", entroA, entroB));
			relaMutualEntro.add(mutualEntroColoc.get(i) / (entroA + entroB));
		}
		return relaMutualEntro;
	}
	
	
	/**
	 * Assistant function to write out the results
	 */
	private static void writeThreeMeasures(String filename) {
		try{
			BufferedWriter fout = new BufferedWriter(new FileWriter(filename));
			// output Renyi entropy diversity
			for (double d : renyiDiversity) {
				fout.write(Double.toString(d) + "\t");
			}
			fout.write("\n");
			// output weighted co-occurrence frequency
			for (double d : weightedFreq) {
				fout.write(Double.toString(d) + "\t");
			}
			fout.write("\n");
			// output mutual information
			for (double d : mutualInfo) {
				fout.write(Double.toString(d) + "\t");
			}
			fout.write("\n");
			// output interestingness from PAKDD
			for (double d : interestingness) {
				fout.write(Double.toString(d) + "\t");
			}
			fout.write("\n");
			// output frequency
			for (int i : frequency) {
				fout.write(Integer.toString(i) + "\t");
			}
			fout.write("\n");
			for (double i : mutualEntroColoc) {
				fout.write(Double.toString(i) + "\t");
			}
			fout.write("\n");
			for (double i : mutualEntroColoc_v3) {
				fout.write(Double.toString(i) + "\t");
			}
			fout.write("\n");
			for (double i : relaMutualEntro) {
				fout.write(Double.toString(i) + "\t");
			}
			fout.write("\n");
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String argv[]) {
		// 1. find frequent user pair
//		shareLocationCount();
//		initialTopKPair();
		// 2. calculate feature one -- Renyi entropy based diversity
//		RenyiEntropyDiversity();
//		// 3. calculate feature two -- weighted frequency, and frequency
//		weightedFrequency();
//		writePairMeasure();
//		// 4. calculate feature three -- mutual information
//		mutualInformation();
//		mutualInformation_v2();
//		// 5. calculate feature four -- interestingness
//		interestingnessPAKDD();
//		// 6. calculate mutual information over colocations
//		mutualEntropyOnColocation();
//		mutualEntropyOnColocation_v2();
//		mutualEntropyOnColocation_v3();
//		relativeMutualEntropy();
		// 6. write the results
//		writeThreeMeasures("feature-vectors-rme.txt");
		
//		writeOutPairColocations();
		writeLocationEntropy(Integer.MAX_VALUE, true);
	}

}
