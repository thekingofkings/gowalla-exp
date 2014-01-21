package probRela;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Tracker {

	// overall measure
	static LinkedList<Integer> numOfColocations = new LinkedList<Integer>();
	
	// frequent measure, filtered from above
	static LinkedList<int[]> FrequentPair = new LinkedList<int[]>();
	static LinkedList<HashSet<Long>> FrequentPair_CoLocations = new LinkedList<HashSet<Long>>();

	// results on three features
	static LinkedList<Double> renyiDiversity = new LinkedList<Double>();
	static LinkedList<Double> weightedFreq = new LinkedList<Double>();
	static LinkedList<Double> mutualInfo = new LinkedList<Double>();
	static LinkedList<Double> interestingness = new LinkedList<Double>();
	static LinkedList<Integer> frequency = new LinkedList<Integer>();
	
	/*
	 * Count the number of co-locations for each pair.
	 * Initialize two packet private static field:
	 * 			FrequentPair
	 * 			FrequentPair_Colocation
	 */
	public static LinkedList<Integer> shareLocationCount() {
		
		User.addAllUser();
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
		System.out.println(String.format("%d frequent pairs are found!", FrequentPair.size()));
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
				prob[j] = (double) cnt[j] / (double) sum;
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
		System.out.println("Renyi entropy based diversity found!");
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
				if (r1.getTimestamp() - r2.getTimestamp() <= 60 * 30)
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
			double locentry = 0;
			double weightedFrequency = 0;
			for (int j = 0; j < clarray.length; j++) {
				locentry = locationEntropy( (Long) clarray[j] );
				weightedFrequency += freq[j] * Math.exp(- locentry);
			}
			weightedFreq.add(weightedFrequency);
			
			
			// monitor the process
			if (i % (FrequentPair.size()/10) == 0)
				System.out.println(String.format("Process - weightedFrequency finished %d0%%", i/(FrequentPair.size()/10)));
		}
		System.out.println("Weighted frequency found!");
		return weightedFreq;
	}
	
	/*
	 * location entropy for one specific location (Shannon Entropy)
	 */
	private static double locationEntropy(long locid) {
		double[] prob = new double[User.frequentUserSet.size()];
		int total_visit = 0;
		int[] user_visit = new int[User.frequentUserSet.size()];
		double locEntropy = 0;
		
		// 1. get the probability
		for (int i = 0; i < prob.length; i++) {
			Object[] users = User.frequentUserSet.values().toArray();
			
			for (Record r : ((User)users[i]).records) {
				if (r.locID == locid)
					user_visit[i] ++;
			}
			total_visit += user_visit[i];
		}
		// 2. calculate the per user probability
		for (int i = 0; i < prob.length; i++) {
			if (user_visit[i] > 0) {
				prob[i] = (double) user_visit[i] / total_visit;
				locEntropy += - prob[i] * Math.log(prob[i]);
			}
		}
		// 3. return the entropy
		return locEntropy;
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
	 */
	public static LinkedList<Double> mutualInformation() {
		for (int i = 0; i < FrequentPair.size(); i++) {
			int uaid = FrequentPair.get(i)[0];
			int ubid = FrequentPair.get(i)[1];
			// 1. calculate the marginal entropy of user a
			double entroA = marginalEntropy(uaid);
			// 2. calculate the marginal entropy of user b
			double entroB = marginalEntropy(ubid);
			// 3. joint entropy of A and B
			double jointEntro = jointEntropy(uaid, ubid);
			// 4. mutual information
			mutualInfo.add(entroA + entroB - jointEntro);
			
			// monitor the process
			if (i % (FrequentPair.size()/10) == 0)
				System.out.println(String.format("Process - mutualInformation finished %d0%%", i/(FrequentPair.size()/10)));
		}
		return mutualInfo;
	}
	
	/*
	 * Assistant function for mutual entropy -- calculate marginal entropy
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
		// 1. count frequency
		for (Record ar : a.records) {
			for (Record br : b.records) {
				if (locFreq.containsKey(ar.locID) && locFreq.get(ar.locID).containsKey(br.locID)) {
					int f = locFreq.get(ar.locID).get(br.locID) + 1;
					locFreq.get(ar.locID).put(br.locID, f);
				}
				else if (locFreq.containsKey(ar.locID) && ! locFreq.get(ar.locID).containsKey(br.locID)) {
					locFreq.get(ar.locID).put(br.locID, 1);
				}
				else if (!locFreq.containsKey(ar.locID)) {
					locFreq.put(ar.locID, new HashMap<Long, Integer>());
					locFreq.get(ar.locID).put(br.locID, 1);
				}
			}
		}
		// 2. probability and entropy
		double prob = 0;
		double entro = 0;
		int totalCase = a.records.size() * b.records.size();
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
		for (int i = 0; i < FrequentPair.size(); i++ ) {
			int uaid = FrequentPair.get(i)[0];
			int ubid = FrequentPair.get(i)[1];
			HashSet<Long> colocs = FrequentPair_CoLocations.get(i);
			// 1. calculate the colocating interestness
			double Finterest = coLocationScore(uaid, ubid, colocs);
			interestingness.add(Finterest);
			// monitor the process
			if (i % (FrequentPair.size()/20) == 0)
				System.out.println(String.format("Process - interestingnessPAKDD finished %d%%", 5 * i/(FrequentPair.size()/20)));	
		}
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
					if (r1.getTimestamp() - r2.getTimestamp() <= 60 * 30)
						interest += - Math.log( (double) raco.get(loc_id).size() / ra.size()) 
							- Math.log( (double) rbco.get(loc_id).size() / rb.size() );
				}
			}
		}
		return interest;
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
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String argv[]) {
		// 1. find frequent user pair
		shareLocationCount();
		// 2. calculate feature one -- Renyi entropy based diversity
		// RenyiEntropyDiversity();
		// 3. calculate feature two -- weighted frequency, and frequency
		weightedFrequency();
		// 4. calculate feature three -- mutual information
		// mutualInformation();
		// 5. calculate feature four -- interestingness
		// interestingnessPAKDD();
		// 6. write the results
		writeThreeMeasures("feature-vectors-freq.txt");
		
		
		
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
