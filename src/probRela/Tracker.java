package probRela;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

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

	// results on three features
	static LinkedList<Double> renyiDiversity = new LinkedList<Double>();
	static LinkedList<Double> weightedFreq = new LinkedList<Double>();
	static LinkedList<Double> mutualInfo = new LinkedList<Double>();
	static LinkedList<Double> interestingness = new LinkedList<Double>();
	static LinkedList<Integer> frequency = new LinkedList<Integer>();
	static LinkedList<Double> mutualEntroColoc = new LinkedList<Double>();
	
	/*
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
		User.findFrequentUsers(1500);
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
				if (colocations.size() >= 80) {
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
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("%d frequent pairs are found in %d seconds", FrequentPair.size(), (t_end - t_start)/1000));
		return numOfColocations;
	}
	
	/*
	 * ===============================================================
	 * The first feature: diversity
	 * ===============================================================
	 * 
	 */
	
	/*
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
			Object[] clarray = coloc.toArray();
			int coloc_num = coloc.size();
			int[] cnt = new int[coloc_num];
			int sum = 0;
			for (int j = 0; j < coloc_num; j++ ) {
				cnt[j] = coLocationFreq (uaid, ubid, (Long) clarray[j]);
				sum += cnt[j];
			}
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
	
	/*
	 * Assistant function for Renyi entropy based diversity
	 * calculat the colocation frequency given two user IDs and the location
	 */
	private static int coLocationFreq( int user_a_id, int user_b_id, Long loc_id ) {
		LinkedList<Record> ra = User.frequentUserSet.get(user_a_id).records;
		LinkedList<Record> raco = new LinkedList<Record>();
		LinkedList<Record> rb = User.frequentUserSet.get(user_b_id).records;
		LinkedList<Record> rbco = new LinkedList<Record>();
		int cnt = 0;
		// find records of user_a in colocating places
		for (Record r : ra ) {
			if (r.locID == loc_id)
				raco.add(r);
		}
		// find records of user b in colocating places
		for (Record r : rb) {
			if (r.locID == loc_id)
				rbco.add(r);
		}
		// judge their colocating events with time
		for (Record r1 : raco) {
			for (Record r2 : rbco) {
				// We identify the colocating event with a 30 minutes time window
				if (Math.abs(r1.timestamp - r2.timestamp) <= 60 * 30)
					cnt ++;
			}
		}
		return cnt;
	}
	
	
	/*
	 * ===============================================================
	 * The second feature: weighted frequency
	 * ===============================================================
	 * 
	 */
	
	/*
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
		HashMap<Long, Double> locsEntro = locationEntropy();
		for (int i = 0; i < FrequentPair.size(); i++) {
			int uaid = FrequentPair.get(i)[0];
			int ubid = FrequentPair.get(i)[1];
			// 1. calculate the frequency of co-occurrence on each co-locating places
			HashSet<Long> coloc = FrequentPair_CoLocations.get(i);
			Object[] clarray = coloc.toArray();
			int coloc_num = coloc.size();
			int[] freq = new int[coloc_num];
			int frequen = 0;
			
			for (int j = 0; j < coloc_num; j++ ) {
				freq[j] = coLocationFreq (uaid, ubid, (Long) clarray[j]);
				frequen += freq[j];
			}
			frequency.add(frequen);
			
			// 2. calculate location entropy
			double weightedFrequency = 0;
			
			for (int j = 0; j < clarray.length; j++) {
				long locid = (long) clarray[j];
				weightedFrequency += freq[j] * Math.exp(- locsEntro.get(locid));
			}
			weightedFreq.add(weightedFrequency);
			
			
			// monitor the process
			if (i % (FrequentPair.size()/10) == 0)
				System.out.println(String.format("Process - weightedFrequency finished %d0%%", i/(FrequentPair.size()/10)));
		}
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("Weighted frequency found in %d seconds", (t_end - t_start)/1000));
		return weightedFreq;
	}

	
	/*
	 * Assistant function
	 * -- calculate location entropy for one specific location (Shannon Entropy)
	 */
	private static HashMap<Long, Double> locationEntropy() {
		HashMap<Long, Double> loc_entro = new HashMap<Long, Double>();
		HashMap<Long, HashMap<Integer, Integer>> loc_user_visit = new HashMap<Long, HashMap<Integer, Integer>>();
		HashMap<Long, Integer> loc_total_visit = new HashMap<Long, Integer>();
		
		// 1. get the location visiting frequency
		for (User u : User.frequentUserSet.values()) {
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
				if (loc_user_visit.get(locid).get(uid) > 0) {
					if (loc_total_visit.get(locid) > 0) {
						double prob = (double) loc_user_visit.get(locid).get(uid) / loc_total_visit.get(locid);
						locEntropy += - prob * Math.log(prob);
					}
				}
			}
			loc_entro.put(locid, locEntropy);
		}
		// 3. return the entropy
		System.out.println("Size of loc_entropy" + Integer.toString(loc_entro.size()));
		return loc_entro;
	}

	
	
	/*
	 * ===============================================================
	 * The third feature: mutual information
	 * ===============================================================
	 * 
	 */
	
	/*
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
	
	/*
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
	
	/*
	 * Assistant function for mutual entropy -- calculate joint entropy
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
				if (Math.abs(ar.timestamp - br.timestamp) <= 4 * 3600) {
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
				}
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
	
	/*
	 * ==========================================================================
	 * The fourth feature: Fei's interestingness
	 * ==========================================================================
	 * 
	 */
	
	/*
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
				System.out.println(String.format("Process - interestingnessPAKDD finished %d%%", i/(FrequentPair.size()/10)));	
		}
		long t_end = System.currentTimeMillis();
		System.out.println(String.format("Interestingness score found in %d seconds", (t_end - t_start)/1000));
		return interestingness;
	}
	
	/*
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
					// We identify the colocating event with a 30 minutes time window
					if (r1.timestamp - r2.timestamp <= 60 * 30)
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
	
	/* 
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
	
	/*
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
				if (Math.abs(ar.timestamp-br.timestamp) <= 4 * 3600) {
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
				}
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
	
	
	/*
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
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String argv[]) {
		// 1. find frequent user pair
		shareLocationCount();
		// 2. calculate feature one -- Renyi entropy based diversity
		RenyiEntropyDiversity();
		// 3. calculate feature two -- weighted frequency, and frequency
		weightedFrequency();
		// 4. calculate feature three -- mutual information
		mutualInformation();
		// 5. calculate feature four -- interestingness
		interestingnessPAKDD();
		// 6. calculate mutual information over colocations
		mutualEntropyOnColocation();
		// 6. write the results
		writeThreeMeasures("feature-vectors.txt");
		
		
		
	/*	try {
			BufferedReader fin = new BufferedReader(new FileReader("colocation-cnt.txt"));
			BufferedWriter fout = new BufferedWriter(new FileWriter("colocation-cnt2.txt"));
			int c = 0;
			while (( c = fin.read()) != -1) {
				fout.write(Integer.toString(c) + "\n");				
			}
			fin.close();
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

}
