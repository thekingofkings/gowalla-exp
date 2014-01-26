package probRela;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;

public class CasePlot {
	
	private BufferedImage img;
	private Graphics g;
	private int uaid;
	private int ubid;
	
	public CasePlot() {
		img = new BufferedImage(1024, 768, BufferedImage.TYPE_3BYTE_BGR);
		try {
			BufferedReader fin = new BufferedReader(new FileReader("remoteFriend.txt"));
			String l = fin.readLine();
			String[] ls = l.split("\\s+");
			uaid = Integer.parseInt(ls[0]);
			ubid = Integer.parseInt(ls[1]);
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
