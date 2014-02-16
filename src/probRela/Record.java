package probRela;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;


/**
 * Record class
 * 
 * Represent the check-in information. The check-in data has the following format:
 * 		[user]	[check-in time]		[latitude]	[longitude]	[location id]
 * 		196514  2010-07-24T13:45:06Z    53.3648119      -2.2723465833   145064
 * 		196514  2010-07-24T13:44:58Z    53.360511233    -2.276369017    1275991
 */
public class Record implements Comparable<Record> {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private static Calendar myCal = new GregorianCalendar();
	
	int userID;
	String time;
	long timestamp;
	double latitude;
	double longitude;
	long locID;
	 
	Record(String line){
		String ls[] = line.split("\\s+");
		try {
			userID = Integer.parseInt(ls[0]);
			time = ls[1];
			timestamp = getTimestamp();
			latitude = Double.parseDouble(ls[2]);
			longitude = Double.parseDouble(ls[3]);
			locID = Long.parseLong(ls[4]);
		} catch (Exception e) {
			System.out.println(ls);
			System.out.println(e.getMessage());
		}
	}
	
	
	/**
	 * Parse the time String. Return timestamp in seconds.
	 */
	private long getTimestamp() {
		long timestamp;
		// parse the time string
		try {
			myCal.setTime( sdf.parse( time ) );
		} catch (ParseException e) {
			e.printStackTrace();
		}
		timestamp = myCal.getTimeInMillis() / 1000;
		return timestamp;
	}
	
	@Override
	public String toString(){
		return String.format("%d %s %g %g %d", userID, time, latitude, longitude, locID);
	}
	

	@Override
	public int compareTo(Record o) {
		return (int) (this.timestamp - o.timestamp);
	}
	 
	public static void main(String argv[]) {
		TDD_entryParse();
	}
	
	/**
	 * Calculate the distance to another record
	 * @param o -- another record
	 * @return -- the distance between two records in km
	 */
	public double distanceTo(Record o) {
		double d2r = (Math.PI/180);
		double distance = 0;
		
		double longiE = o.longitude;
		double latiE = o.latitude;
		try {
			double dlong = (longiE - longitude) * d2r;
			double dlati = (latiE - latitude) * d2r;
			double a = Math.pow(Math.sin(dlati/2.0), 2)
					+ Math.cos(latitude * d2r)
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
	 * Hyper distance to another record
	 * @param o  -- another record 
	 * @return
	 */
	public double hyperDistanceTo(Record o) {
		double dist = this.distanceTo(o);
		double gapT = (double) ((this.timestamp - o.timestamp) / 3600.0 / 24.0);
		return Math.sqrt(dist * dist + gapT * gapT);
	}
	
	public String GPS() {
		return String.format("%.12f%.12f", latitude, longitude);
	}
	


	// TDD
	private static void TDD_entryParse() {
		BufferedReader fin = null;
		String filepath = "../../dataset/Gowalla_totalCheckins.txt";
		try {
			fin = new BufferedReader(new FileReader(filepath));
		} catch (FileNotFoundException e) {
			System.out.println(filepath);
			e.printStackTrace();
		}
		LinkedList<Record> recs = new LinkedList<Record>();
		for (int i = 0; i < 100; i++) {
			try {
				recs.add(new Record(fin.readLine()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(recs.getLast());
		}
		try {
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
