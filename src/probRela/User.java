package probRela;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Random;

public class User {
	static HashMap<Integer, User> allUserSet = new HashMap<Integer, User>();
	static HashMap<Integer, User> frequentUserSet = new HashMap<Integer, User>();
	static String dirPath = "../../dataset/gowalla/sorteddata";
	static double para_c = 1.5;
	static double recSampleRate = 1;
//	static double para_amp = 1;
	
	
	double totalweight;
	int userID;
	LinkedList<Record> records;
	
	/*
	 * Construct the allUserSet (static field) in User class
	 */
	User( Record r ) {
		userID = r.userID;
		records = new LinkedList<Record>();
		records.add(r);
		if ( ! allUserSet.containsKey(r.userID))
			allUserSet.put(r.userID, this);
	}
	
	/*
	 * Initialize a single instance
	 */
	User (int uid) {
		if (! allUserSet.containsKey(uid)) {
			userID = uid;
			records = new LinkedList<Record>();
			
			try {
				BufferedReader fin = new BufferedReader(new FileReader(String.format("%s/%d", dirPath, uid)));
				String l;
				if (recSampleRate >= 1.0) {
					while ((l = fin.readLine()) != null) {
						records.add(new Record(l));
					}
				} else {
					Random rnd = new Random();
					while ((l = fin.readLine()) != null) {
						if (rnd.nextDouble() < recSampleRate) {
							records.add(new Record(l));
						}
					}
				}
				fin.close();
			} catch (Exception e) {
				System.out.println("Exception in User constructor");
				e.printStackTrace();
			}
			allUserSet.put(uid, this);
		} else {
			this.userID = uid;
			this.records = allUserSet.get(uid).records;
		}
		
		//totalweight = totalWeight();
	}

	HashSet<Long> getLocations() {
		HashSet<Long> locs = new HashSet<Long>();
		for (Record r : records) {
			if ( ! locs.contains(r.locID) )
				locs.add( r.locID );
		}
		return locs;
	}
	
	/**
	 * Calculate the weight of one location (represented by record) in this user's movement. </br>
	 * ==========================</br>
	 * The weight is calculated by </br>
	 * 			Prob_weight (loc_i ) = sum( e^(- distance ( loc_i, loc_j)) / n.
	 * @param rt  target record rt
	 * @return   the weight of this location
	 */
	public double locationWeight( Record rt ) {
		double weight = 0;
		double dist = 0;
//		int cnt = 0;
		
		for (Record r : records) {
			dist = rt.distanceTo(r);
			dist = Math.exp(- User.para_c * dist);
//			if (dist > 0)
//				cnt ++;
			weight += dist;
		}
//		System.out.println(String.format("User %d  Cnt: %d, records size %d, weight %g", userID, cnt, records.size(), weight));
		weight /= records.size();

//		weight = weight / totalweight;
		
		return weight;
	}
	
	public double totalWeight( ) {
		double weight = 0;
		double dist = 0;
		for (int i = 0; i < records.size(); i++ ) {
			for (int j = i+1; j < records.size(); j++) {
				dist = records.get(i).distanceTo(records.get(j));
				dist = Math.exp( - User.para_c * dist);
				weight += dist;
			}
		}
		return weight;
	}
	
	public static User feedRecord( Record r ) {
		if ( allUserSet.containsKey(r.userID) ) {
			User u = allUserSet.get(r.userID);
			u.records.add(r);
			return u;
		} else {
			User u = new User( r );
			return u;
		}
	}
	
	public String toString() {
		return String.format("User: %d has %d records", userID, records.size());
	}

	/*
	 * Calculate the active user from all users.
	 */
	public static void findFrequentUsersByFreq( int freq ) {
		for (int i : allUserSet.keySet()) {
			User cur = allUserSet.get(i);
			if (cur.records.size() >= freq)
				frequentUserSet.put(i, cur);
		}
	}
	
	
	/*
	 * Calculate the active user from all users.
	 */
	public static void findFrequentUsersTopK( int k ) {
		System.out.format("Initiate %d user again.%n", k);
		frequentUserSet.clear();
		allUserSet.clear();
		try {
			BufferedReader fin = new BufferedReader(new FileReader("../../dataset/Gowalla/userCount.txt"));
			String l = null;
			int c = 0;
			while ( (l=fin.readLine()) != null) {
				if (c==k)
					break;
				String[] ls = l.split("\\s+");
				int uid = Integer.parseInt(ls[0]);
				User u = new User(uid);
				frequentUserSet.put(uid, u);
				c++;
			}
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Initialize all the users from file.
	 */
	public static void addAllUser() {
		BufferedReader fin = null;
		String filepath = "../../dataset/gowalla/Gowalla_totalCheckins.txt";
		try {
			fin = new BufferedReader(new FileReader(filepath));
		} catch (FileNotFoundException e) {
			System.out.println(filepath);
			e.printStackTrace();
		}
		String l = null;
		try {
			while ((l = fin.readLine()) != null) {
				//User u = 
				User.feedRecord(new Record(l));
				// System.out.println(u);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try {
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// sort 
		for (User u : allUserSet.values()) {
			Collections.sort(u.records);
		}
		System.out.println("Add All users finished");
	}

	
	// generate trajectory file for individual users.
	public static void writeOutSortedResult() {
		File dir = new File(dirPath);
		dir.mkdir();
		
		// write each user's records into one file
		for (User u : allUserSet.values()) {
			try {
				BufferedWriter fout = new BufferedWriter(new FileWriter(String.format("%s/%d", dirPath, u.userID)));
				for (Record r : u.records) {
					fout.write(r.toString() + "\n");
				}
				fout.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	

	public static void main (String argv[] ) {
//		TDD_allUserConstruct();
//		TDD_records_sorting();
//		findFrequentUsers(400);
//		System.out.println(String.format("Active user number: %d", User.frequentUserSet.size()));
//		writeOutSortedResult();
		
		User u = new User(1000);
		for (Record r : u.records)
			System.out.println(String.format("%.10f%.10f", r.latitude, r.longitude));
	}
	
	
	
	
	
	
	// TDD
	@SuppressWarnings("unused")
	private static void TDD_allUserConstruct() { 
		addAllUser();
		// statistics
		int sum = 0;
		for (int i : User.allUserSet.keySet()) {
			sum += User.allUserSet.get(i).records.size();
		}
		System.out.println(String.format("Total number of users: %d%nTotal line number: %d", 
				User.allUserSet.size(), sum));
	}
	
	@SuppressWarnings("unused")
	private static void TDD_records_sorting() {		
		for (Record r : allUserSet.get(0).records){
			System.out.println(r);
		}
	}
}
