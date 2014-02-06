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
        

        Node(double x1, double y1, double x2, double y2, Record r) {
        	count = 0;
            this.latiLL = x1;
            this.longiLL = y1;
            this.latiUR = x2;
            this.longiUR = y2;
            this.records = new Record[capacity];
            this.records[count] = r;
        }
        
        
        public boolean insertable(Record r) {
        	return (latiLL <= r.latitude && longiLL <= r.longitude && latiUR >= r.latitude && longiUR >= r.latitude && count < capacity);
        }
    }


  /***********************************************************************
    *  Insert (x, y) into appropriate quadrant
    ***********************************************************************/
    public void insert(double x, double y, Record r) {
        root = insert(root, x, y, r);
    }

    private Node insert(Node h, double x, double y, Record value) {
        if (h == null)
        	return new Node(-180, -90, 180, 90, value);
        else if ( root.insertable(value) ) h.SW = insert(h.SW, x, y, value);
        else if ( less(x, h.x) && !less(y, h.y)) h.NW = insert(h.NW, x, y, value);
        else if (!less(x, h.x) &&  less(y, h.y)) h.SE = insert(h.SE, x, y, value);
        else if (!less(x, h.x) && !less(y, h.y)) h.NE = insert(h.NE, x, y, value);
        return h;
    }
    
   


  /***********************************************************************
    *  Range search.
    ***********************************************************************/

    public void query2D(Interval2D<double> rect) {
        query2D(root, rect);
    }

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