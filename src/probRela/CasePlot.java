package probRela;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;

import javax.imageio.ImageIO;


/**
 * class CasePlot </br>
 * ===============================
 * Plot user visiting locations in pair
 * 
 * 
 * @author Hongjian
 *
 */
public class CasePlot {
	
	private BufferedImage img;
	private Graphics g;
	private int uaid;
	private int ubid;
	private int meetFreq;
	// maxCoord will store min_longi, min_lati, max_longi, max_lati.
	private double[] maxCoord;
	private static int imgWidth = 800;
	private static int imgHeight = 600;
	
	private LinkedList<int[]> friendsPairs;
	private LinkedList<int[]> nonFriendsPairs;
	
	public CasePlot() {
		System.out.println("Start initializing");
		maxCoord = new double[4];
		// avoid initialize all the users
//		User.addAllUser();
		initializeFriendPair();
		initializeNonFriendPair();
		
		System.out.println("Initializing finished");
	}
	
	private void initializeFriendPair() {
		friendsPairs = new LinkedList<int[]>();
		try {
			BufferedReader fin = new BufferedReader(new FileReader("remoteFriend-200.txt"));
			String l;
			while ((l = fin.readLine()) != null) {
				// the four fields are uaid, ubid, distance, meeting frequency
				String[] ls = l.split("\\s+");
				// if there is a meeting event
				if (Integer.parseInt(ls[3]) > 0) {
					int[] p = new int[3];
					p[0] = Integer.parseInt(ls[0]);	// ua id
					p[1] = Integer.parseInt(ls[1]);	// ub id
					p[2] = Integer.parseInt(ls[3]); // meeting frequency
					friendsPairs.add(p);
				}
			}
			fin.close();
		} catch (Exception e) {
			System.out.println("Exception in initializeFriendPair.");
			e.printStackTrace();
		}
	}
	

	/**
	 * Draw each friend pair
	 */
	public void drawEachFriendPair() {
		int k = friendsPairs.size();
		drawEachFriendPair(k);
	}

	public void drawEachFriendPair(int k) {
		System.out.println("Start drawing distant friends");
		int c = 0;
		for (int i = 0; i < friendsPairs.size(); i++) {
			uaid = friendsPairs.get(i)[0];
			ubid = friendsPairs.get(i)[1];
			if (uaid < ubid) {
				meetFreq = friendsPairs.get(i)[2];
				// initialize user and reading file
				new User(uaid);
				new User(ubid);
	
				findMaxCoord();
				paint();
				saveImg(String.format("friend%d-%d.png", uaid, ubid));
				c++;
				if (c == k)
					break;
			}
		}
		System.out.println("Drawing distant friends finished");
	}
	
	private void findMaxCoord() {
		// reset maxCoord
		maxCoord[0] = 180;
		maxCoord[1] = 90;
		maxCoord[2] = -180;
		maxCoord[3] = -90;
		// iterate through User a
		for (Record r : User.allUserSet.get(uaid).records) {
			if (r.longitude < maxCoord[0])
				maxCoord[0] = r.longitude;
			else if (r.longitude > maxCoord[2])
				maxCoord[2] = r.longitude;
			
			if (r.latitude < maxCoord[1])
				maxCoord[1] = r.latitude;
			else if (r.latitude > maxCoord[3])
				maxCoord[3] = r.latitude;
		}
		// iterate through User b
		for (Record r : User.allUserSet.get(ubid).records) {
			if (r.longitude < maxCoord[0])
				maxCoord[0] = r.longitude;
			else if (r.longitude > maxCoord[2])
				maxCoord[2] = r.longitude;
			
			if (r.latitude < maxCoord[1])
				maxCoord[1] = r.latitude;
			else if (r.latitude > maxCoord[3])
				maxCoord[3] = r.latitude;
		}
	}
	
	private int[] mapCoord(Record r) {
		return mapCoord(r.longitude, r.latitude);
	}
	
	private int[] mapCoord(double longi, double lati) {
		int x = (int) ((imgWidth-5) * (longi - maxCoord[0]) / (maxCoord[2] - maxCoord[0]));
		int y = imgHeight -5 - (int) ((imgHeight-5) * (lati - maxCoord[1]) / (maxCoord[3] - maxCoord[1]));
		int[] res = {x, y};
		return res;
	}
	
	private void paint() {
		// initialize the image and graphics
		img = new BufferedImage(imgWidth,imgHeight,BufferedImage.TYPE_INT_ARGB);
		g = img.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		// paint the first user
		g.setColor(Color.blue);
		for (Record r : User.allUserSet.get(uaid).records) {
			int[] point = mapCoord(r);
			g.fillOval(point[0], point[1], 5, 5);
		}
		// paint the second user
		g.setColor(Color.red);
		for (Record r : User.allUserSet.get(ubid).records) {
			int[] point = mapCoord(r);
			g.fillOval(point[0], point[1], 5, 5);
		}
		// mark distance
		g.drawString(String.format("From %g, %g to %g, %g (Latitude, Longitude); meet freq: %d", 
				maxCoord[1], maxCoord[0], maxCoord[3], maxCoord[2], meetFreq), 360, 380);
	}

	
	
	private void initializeNonFriendPair() {
		nonFriendsPairs = new LinkedList<int[]>();
		try {
			BufferedReader fin = new BufferedReader(new FileReader("nonFriendsMeeting-200.txt"));
			String l;
			while ((l = fin.readLine()) != null) {
				// the four fields are uaid, ubid, meeting frequency
				String[] ls = l.split("\\s+");
				int[] p = new int[3];
				p[0] = Integer.parseInt(ls[0]);	// ua id
				p[1] = Integer.parseInt(ls[1]);	// ub id
				p[2] = Integer.parseInt(ls[2]); // meeting frequency
				nonFriendsPairs.add(p);
			}
			fin.close();
		} catch (Exception e) {
			System.out.println("Exception in initializeFriendPair.");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Draw each nonfriend pair
	 */
	public void drawEachNonFriendPair() {
		int k = nonFriendsPairs.size();
		drawEachNonFriendPair(k);
	}

	public void drawEachNonFriendPair(int k) {
		System.out.println("Start drawing non-friends");
		int c = 0;
		for (int i = 0; i < nonFriendsPairs.size(); i++) {
			uaid = nonFriendsPairs.get(i)[0];
			ubid = nonFriendsPairs.get(i)[1];
			if (uaid < ubid) {
				meetFreq = nonFriendsPairs.get(i)[2];
				// initialize user and reading file
				new User(uaid);
				new User(ubid);
	
				findMaxCoord();
				paint();
				saveImg(String.format("nonfriend%d-%d.png", uaid, ubid));
				c++;
				if (c == k)
					break;
			}
		}
		System.out.println("Drawing non-friends finished");
	}
	
	
	/**
	 * Save image
	 */
	public void saveImg(String fn) {
		try {
			File outImg = new File(fn);
			ImageIO.write(img, "png", outImg);
		} catch (Exception e) {
			System.out.println("Exception in save image");
			e.printStackTrace();
		}
	}
	
	
	public static void main(String argv[]) {
		CasePlot cp = new CasePlot();
		cp.drawEachFriendPair();
		cp.drawEachNonFriendPair();
	}
}
