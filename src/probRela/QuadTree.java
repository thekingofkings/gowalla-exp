package probRela;


/*************************************************************************
 *
 *  Region Quad tree.
 * 
 * @author Hongjian
 *************************************************************************/

public class QuadTree  {
    private Node root;

    // helper node data type
    private class Node {
    	static int capacity = 100;
    	
        double latiLL, longiLL, latiUR, longiUR;
        Node NW, NE, SE, SW;   // four subtrees
        Record[] records;           // associated data
        int count;
        boolean leafFlag;
        

        Node(double x1, double y1, double x2, double y2, Record r) {
        	count = 0;
            this.latiLL = x1;
            this.longiLL = y1;
            this.latiUR = x2;
            this.longiUR = y2;
            this.records = new Record[capacity];
            this.records[count] = r;
            count ++;
            leafFlag = true;
        }
        
        
        public boolean insertable(Record r) {
        	return (latiLL <= r.latitude && longiLL <= r.longitude && latiUR >= r.latitude && longiUR >= r.latitude && count < capacity);
        }
        
        public void insert(Record r) {
        	this.records[count] = r;
        	count ++;
        }
        
    }


  /***********************************************************************
    *  Insert (x, y) into appropriate quadrant
    ***********************************************************************/
    public void insert(Record r) {
        root = insert(root, r);
    }

    private Node insert(Node h, Record r) {
        if (h == null)
        	return new Node(-180, -90, 180, 90, r);
        if (h.insertable(r))
        	h.insert(r);
        else if ( h.SW.insertable(r) ) 
        	h.SW.insert(r);
        else if ( h.NW.insertable(r) )
        	h.NW.insert(r);
        else if ( h.SE.insertable(r) ) 
        	h.SE.insert(r);
        else if ( h.NE.insertable(r) ) 
        	h.NE.insert(r);
        return h;
    }
    
   


  /***********************************************************************
    *  Range search.
    ***********************************************************************/

 

    private void query2D(Node h, Interval2D<double> rect) {
        if (h == null) return;
        double xmin = rect.intervalX.low;
        double ymin = rect.intervalY.low;
        double xmax = rect.intervalX.high;
        double ymax = rect.intervalY.high;
        if (rect.contains(h.x, h.y))
            System.out.println("    (" + h.x + ", " + h.y + ") " + h.value);
        if ( less(xmin, h.x) &&  less(ymin, h.y)) query2D(h.SW, rect);
        if ( less(xmin, h.x) && !less(ymax, h.y)) query2D(h.NW, rect);
        if (!less(xmax, h.x) &&  less(ymin, h.y)) query2D(h.SE, rect);
        if (!less(xmax, h.x) && !less(ymax, h.y)) query2D(h.NE, rect);
    }


   /*************************************************************************
    *  test client
    *************************************************************************/
    public static void main(String[] args) {
        int M = Integer.parseInt(args[0]);   // queries
        int N = Integer.parseInt(args[1]);   // points

        QuadTree<Integer, String> st = new QuadTree<Integer, String>();

        // insert N random points in the unit square
        for (int i = 0; i < N; i++) {
            Integer x = (int) (100 * Math.random());
            Integer y = (int) (100 * Math.random());
            // System.out.println("(" + x + ", " + y + ")");
            st.insert(x, y, "P" + i);
        }
        System.out.println("Done preprocessing " + N + " points");

        // do some range searches
        for (int i = 0; i < M; i++) {
            Integer xmin = (int) (100 * Math.random());
            Integer ymin = (int) (100 * Math.random());
            Integer xmax = xmin + (int) (10 * Math.random());
            Integer ymax = ymin + (int) (20 * Math.random());
            Interval<Integer> intX = new Interval<Integer>(xmin, xmax);
            Interval<Integer> intY = new Interval<Integer>(ymin, ymax);
            Interval2D<Integer> rect = new Interval2D<Integer>(intX, intY);
            System.out.println(rect + " : ");
            st.query2D(rect);
        }
    }

}