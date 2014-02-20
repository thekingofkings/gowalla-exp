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
    	static final int capacity = 100;
    	
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

 

    private void query2D(Node h) {
        
    }


   /*************************************************************************
    *  test client
    *************************************************************************/
    public static void main(String[] args) {
  
    }

}