package probRela;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.imageio.ImageIO;

public class CasePlot {
	
	private BufferedImage img;
	private Graphics g;
	private int uaid;
	private int ubid;
	private double[] maxCoord;
	private static int imgWidth = 800;
	private static int imgHeight = 600;
	
	public CasePlot() {
		System.out.println("Start initializing");
		maxCoord = new double[4];
		User.addAllUser();
		System.out.println("Initializing finished");
	}
	
	
	public void drawEachPair() {
		int k = Integer.MAX_VALUE;
		drawEachPair(k);
	}

	public void drawEachPair(int k) {
		System.out.println("Start drawing");
		img = new BufferedImage(imgWidth,imgHeight,BufferedImage.TYPE_INT_ARGB);
		g = img.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		try {
			BufferedReader fin = new BufferedReader(new FileReader("remoteFriend.txt"));
			String l; 
			int c = 0;
			while ((l = fin.readLine()) != null) {
				String[] ls = l.split("\\s+");
				uaid = Integer.parseInt(ls[0]);
				ubid = Integer.parseInt(ls[1]);

				findMaxCoord();
				paint();
				saveImg(String.format("friend%d-%d.png", uaid, ubid));
				c++;
				if (c == k)
					break;
			}
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Drawing finished");
	}
	
	private void findMaxCoord() {
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
	
	public void paint() {
		// paint the first user
		g.setColor(Color.blue);
		for (Record r : User.allUserSet.get(uaid).records) {
			int[] point = mapCoord(r);
			g.fillOval(point[0], point[1], 3, 3);
		}
		// paint the second user
		g.setColor(Color.red);
		for (Record r : User.allUserSet.get(ubid).records) {
			int[] point = mapCoord(r);
			g.fillOval(point[0], point[1], 3, 3);
		}
		// mark distance
		g.drawString(String.format("H: %g, W: %g", (maxCoord[3]-maxCoord[1]) * 110, (maxCoord[2] - maxCoord[0]) * 110), 360, 380);
	}
	
	public void saveImg(String fn) {
		try {
			File outImg = new File(fn);
			ImageIO.write(img, "png", outImg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String argv[]) {
		CasePlot cp = new CasePlot();
		cp.drawEachPair(1);
	}
}
