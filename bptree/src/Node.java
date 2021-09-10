import java.util.ArrayList;
import java.util.List;

public class Node {
    int m; //#of keys
    List<pair> p; //array of pairs
    Node r;//right-sibling node or right-most child node
    Node parent;
    public Node(){
        super();
        this.m=0;
        this.p=new ArrayList<>();
        this.r=null;
        this.parent=null;
    }
}
