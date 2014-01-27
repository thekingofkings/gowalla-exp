package probRela;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.HashSet;

public class User {
	static HashMap<Integer, User> allUserSet = new HashMap<Integer, User>();
	static HashMap<Integer, User> frequentUserSet = new HashMap<>();
	
	
	int userID;
	LinkedList<Record> records;
	
	User( Record r ) {
		userID = r.userID;
		records = new LinkedList<Record>();
		records.add(r);
		if ( ! allUserSet.containsKey(r.userID))
			allUserSet.put(r.userID, this);
	}
	
	HashSet<Long> getLocations() {
		HashSet<Long> locs = new HashSet<Long>();
		for (Record r : records) {
			if ( ! locs.contains(r.locID) )
				locs.add( r.locID );
		}
		return locs;
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
	
	
	public static void main (String argv[] ) {
		TDD_allUserConstruct();
		TDD_records_sorting();
		findFrequentUsers(400);
		System.out.println(String.format("Active user number: %d", User.frequentUserSet.size()));
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
	
	private static void TDD_records_sorting() {		
		for (Record r : allUserSet.get(0).records){
			System.out.println(r);
		}
	}
}
