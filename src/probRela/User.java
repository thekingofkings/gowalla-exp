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

public class User {
	static HashMap<Integer, User> allUserSet = new HashMap<Integer, User>();
	static HashMap<Integer, User> frequentUserSet = new HashMap<>();
	static String dirPath = "../../dataset/sorteddata";
	
	
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
				while ((l = fin.readLine()) != null) {
					records.add(new Record(l));
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
		
		for (Record r : records) {
			dist = rt.distanceTo(r);
			dist = Math.exp(- dist);
			weight += dist;
		}
		
		weight /= records.size();
		
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
	public static void findFrequentUsers( int freq ) {
		for (int i : allUserSet.keySet()) {
			User cur = allUserSet.get(i);
			if (cur.records.size() >= freq)
				frequentUserSet.put(i, cur);
		}
	}
	
	/*
	 * Initialize all the users from file.
	 */
	public static void addAllUser() {
		BufferedReader fin = null;
		String filepath = "../../dataset/Gowalla_totalCheckins.txt";
		try {
			fin = new BufferedReader(new FileReader(filepath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
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
	}
	
	
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
		TDD_allUserConstruct();
//		TDD_records_sorting();
		findFrequentUsers(400);
		System.out.println(String.format("Active user number: %d", User.frequentUserSet.size()));
		writeOutSortedResult();
	}
	
	
	
	
	
	
	// TDD
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
