
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;

public class bptree {
    static Node root = new Node();
    static int nodeSize = 0; //node size

    static List<commands> commands = new ArrayList<>();

    //////main//////
    public static void main(String[] args) throws Exception {
        String command = args[0]; //read command (c,i,d,s,r)
        String savedFile = args[1];//read file to save the tree info
        //create
        if (command.equals("-c")) {
            nodeSize = Integer.parseInt(args[2]);
            writeNodeSize(savedFile);//write the nodesize of the tree. ex) nodeSize:3
        } else {
            readSavedTree(savedFile); //read the commands given previously
            //insert
            if (command.equals("-i")) {
                readInsertion(args[2]);
                saveTree(savedFile);
            }
            //single key search
            else if (command.equals("-s")) {
                int search = Integer.parseInt(args[2]);
                single_search(search);
            }
            //ranged search
            else if (command.equals("-r")) {
                int start = Integer.parseInt(args[2]);
                int end = Integer.parseInt(args[3]);
                ranged_search(start, end);
            }
            //delete
            else if (command.equals("-d")) {
                readDeletion(args[2]);
                saveTree(savedFile);
            }
            //saveTree when there is modification (insert & delete)
        }
    }
    ////////FileRW////////
    //read key and value to insert and do the insertion
    public static void readInsertion(String file) throws Exception {
        File insert = new File(file);
        String line;
        try {
            BufferedReader b = new BufferedReader(new FileReader(insert));
            while ((line = b.readLine()) != null) {//read till the end
                String[] keyAndVal = line.split(","); //read csv(comma separated) file
                int key = Integer.parseInt(keyAndVal[0]);
                int val = Integer.parseInt(keyAndVal[1]);
                insert(key, val);//insert
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    //write the node size of the tree on the top of the index.dat file
    public static void writeNodeSize(String File) {
        try {
            FileWriter fw = new FileWriter(File, false);
            fw.write("nodeSize: " + nodeSize);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //read the commands given previously and make a new tree according to the history
    public static void readSavedTree(String file) {
        File savedFile = new File(file);
        Node n = new Node();
        String read;
        try {
            BufferedReader br = new BufferedReader(new FileReader(savedFile));
            read = br.readLine();
            read = read.substring(10);
            nodeSize = Integer.parseInt(read); //read the size of node
            Node now = null;
            while ((read = br.readLine()) != null) {
                if (read.substring(0, 1).equals("i")) { //insert
                    String[] keyAndVal = read.substring(8).split(", value: "); //the index of int value==8
                    int key = Integer.parseInt(keyAndVal[0]); //ex) i. key: 1, value: 9
                    int val = Integer.parseInt(keyAndVal[1]);
                    insert(key, val);

                } else {//delete
                    String keyString = read.substring(8);
                    int key = Integer.parseInt(keyString);//ex) d. key: 7
                    delete(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //save tree info in the file
    public static void saveTree(String file) {
        try {
            FileWriter fw = new FileWriter(file, false); //clean-and-write everytime
            fw.write("nodeSize: " + nodeSize + "\n");//size of node
            WriteNodes(fw);//write the commands made
            //ex) insert <1,9>  ->  i. key: 1, value: 9
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //save commands info
    public static void WriteNodes(FileWriter fw) {
        try {
            for (commands command : commands) { //all the commands saved
                if (command.com == 'i')
                    fw.write(command.com + ". key: " + command.key + ", value: " + command.val + "\n");
                else
                    fw.write(command.com + ". key: " + command.key + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ///////insert/////////
    //look for the node which the pair should be inserted in
    public static Node WhichNodeToInsert(int key) {
        Node now = root; //search from root
        while (true) {
            //if now has no child-nodes return now(leaf)
            if (now.m == 0)
                return now;
            if (now.p.get(0).child == null)
                return now;

            //check if now has to go down (to the child-node of it)
            boolean biggest = true;
            for (int i = 0; i < now.m; i++) {
                if (key < now.p.get(i).key) { //once the key is smaller than the pair in node,
                    now = now.p.get(i).child;//the index to inserted is found
                    biggest = false;
                    break;
                }
            }
            //if all the pairs in the node are bigger than key -> check the rightmost child (continue)
            if (biggest==true){
                now = now.r;
            }
        }
    }

    //returns the index where the key should be inserted
    public static int findInsertIndex(Node node, int key) {
        for (int i = 0; i < node.m; i++) { //from the start of node pairs to the end
            if (key < node.p.get(i).key) //once the key is smaller than the key in node
                return i; //the index is found
        }
        return node.m;
    }


    //solve leaf overflow problems by splitting
    public static void leafSplit(Node now, Node r) {
        //move the right half of 'now' to its right sibling
        for (int i = nodeSize / 2; i < nodeSize; i++) {
            r.p.add(new pair(now.p.get(i).key, now.p.get(i).val, null));
            r.m++;
        }
        //then remove the right half in 'now'
        for (int i = nodeSize / 2; i < nodeSize; i++) {
            now.p.remove(nodeSize / 2);
            now.m--;
        }
    }

    //solve non-leaf(parent) overflow problems by splitting
    public static void nonLeafSplit(Node now) {
        Node split = new Node();//node to place the split part
        //save the middle pair for later use
        pair newRootPair = new pair(now.p.get(now.m / 2).key, now.p.get(now.m / 2).val, now.p.get(now.m / 2).child);

        int M = now.m;
        //place pairs in the right half of the node in 'split'
        for (int i = M / 2 + 1; i < M; i++) {
            split.p.add(new pair(now.p.get(i).key, now.p.get(i).val, now.p.get(i).child));
            now.p.get(i).child.parent = split;
            split.m++;
        }
        //remove the pairs in 'now'
        for (int i = M / 2; i < M; i++) {
            now.m--;
            now.p.remove(M / 2);
        }
        split.r = now.r;//move the rightmost child too
        now.r = newRootPair.child;//the middle pair is now the child on the split bound
        split.r.parent = split;//link parent nodes and child nodes
        now.r.parent = now;
        //the split node is not the root of the tree
        if (now != root) {
            int nodeIndex = IndexInParent(now);
            now.parent.p.add(nodeIndex, new pair(newRootPair.key, newRootPair.val, now));
            now.parent.m++;
            //if the split node is not the leftmost child of it parent node
            if(nodeIndex<now.parent.m-1)
                now.parent.p.get(nodeIndex + 1).child = split;//the child node next to it is split
            //if it is the leftmost child
            else
                now.parent.r = split;//split goes down to the right child of split node
            split.parent = now.parent;//they share the same parent
            //if parent overflow occurs because of splitting
            if (now.parent.m >= nodeSize) {
                nonLeafSplit(now.parent);//call split function again
            }
        }
        //the split node is the root of the tree
        else {
            //make a new root for the tree
            Node newRoot = new Node();
            //the saved pair (the middle pair) is the new root
            pair rootPair=new pair(newRootPair.key, newRootPair.val, now);

            newRoot.p.add(rootPair);
            newRoot.m=1;
            newRoot.r = split; //the children of new root : now(left) and split(right)
            root = newRoot;//new root
            now.parent = root;//link root and its child nodes
            split.parent = root;

        }
    }
    //insert pair at the proper place (key and val)
    public static void insert(int key, int val) {
        //save the command in the array ex) i. key: 1, value: 9
        commands.add(new commands(key, val, 'i'));
        pair insertP = new pair(key, val, null);//pair to insert

        Node now = WhichNodeToInsert(key);//find the node which the key should be inserted in
        int insertIndex = findInsertIndex(now, key);//and the index
        now.p.add(insertIndex, insertP);//add it at the proper place

        if (++now.m >= nodeSize) { //needs splitting
            Node split = new Node(); //make a node to save the split part
            leafSplit(now, split); //split(leaf node)

            if (now.parent != null) {//not root
                int nodeIndex = findInsertIndex(now.parent, key);
                if (nodeIndex == now.parent.m) {//rightmost child
                    now.parent.p.add(new pair(split.p.get(0).key, split.p.get(0).val, now)); //add the first pair of split to now.parent
                    now.parent.r = split; //split is the rightmost of now.parent
                    now.parent.m++;
                    split.r = now.r;//the right sibling of now moves to the right of split (split goes between now and now.r)
                    now.r = split; //split is in the right to now
                    split.parent = now.parent;//share the same parent
                }
                else {
                    pair newPair = new pair(split.p.get(0).key, split.p.get(0).val, now);
                    now.parent.p.add(nodeIndex, newPair); //add the first pair of split to where the parent of now was
                    now.parent.p.get(nodeIndex + 1).child = split; // child node of origin pair now.parent.get(nodeIndex) == split
                    now.parent.m++;
                    split.r = now.r; //same as the process done when key was in the rightmost child
                    now.r = split;
                    split.parent = now.parent;
                }
                if (now.parent.m >= nodeSize)  //parent overflow
                    nonLeafSplit(now.parent);
            }
            else { //now ==root
                //new parent for now and split
                Node newParent = new Node();
                pair p = new pair(split.p.get(0).key, split.p.get(0).val,null);
                p.child=now;//left child is now. right child is split
                newParent.p.add(p);
                newParent.r = split;
                newParent.m = 1;

                root = newParent;//new root
                now.parent = newParent;//share the same parent
                split.parent = newParent;
                split.r = now.r; //split goes between now and now.r
                now.r = split;
            }
        }

    }
    //return the index of the parent pair of 'now'
    public static int IndexInParent(Node now) {
        if (now.parent != null) { //not root
            for (int i = 0; i < now.parent.m; i++) {//return index if now == child of i th pair of the parent node
                if (now == now.parent.p.get(i).child) {
                    return i;
                }
            }
            if (now.parent.r == now) { //found the parent pair in the rightmost node
                return now.parent.m;
            }
        }
        return -1;//roots don't have parent nodes.
    }

    ////////single key search///////
    public static void single_search(int key) {
        Node now;
        List<Node> path = new ArrayList<>();//save the inverse path of single key search
        now = root;//search from the root

        //if there is no nodes in the tree, you don't need to look for it (return)
        if(now.m==0){
            System.out.println("Nothing in the tree!");
            return;
        }

        int i;
        path.add(root); //the root is always on the path
        boolean pairIsFound = false;
        //search till the node is leaf node
        while (now.p.get(0).child != null) {
            pairIsFound = false;//find the pair bigger than the key you are searching
            for (i = 0; i < now.m; i++) {
                if (key < now.p.get(i).key) { //once the pair bigger than the key is found
                    now = now.p.get(i).child; //continue searching from the child of the node
                    pairIsFound = true;
                    break;
                }
            }

            if (pairIsFound == false) { //came to the end but did not find the pair bigger than the key
                now = now.r;//move on to the right child of the key
            }
            path.add(now);//add path
        }
        int value = 0;
        boolean keyIsFound = false;//look for the key
        int j;
        //search the pair that has the key as its key (in the node)
        for (j = 0; j < now.m; j++) {
            if (key == now.p.get(j).key) { //if the key is found
                value = now.p.get(j).val; //save its value to display on console
                for (Node node : path) {
                    printSingleSearchPath(node);
                } //and print the path
                keyIsFound = true;
            }
        }
        if (!keyIsFound) { //if the key is not found
            System.out.println("Not found!"); //print "not found"
        } else {
            System.out.println("\nPair Found:\nkey: " + key + " value: " + value); // print value of the key
        }
    }
    //print the path (single key search)
    public static void printSingleSearchPath(Node n) {
        //print all the keys in the list in inverse order
        for (int i = 0; i < n.m; i++) {
            System.out.print(n.p.get(i).key);
            if (i < n.m - 1)
                System.out.print(", "); // add comma if it is not the last one
        }
        System.out.println(); //enter
    }

    ////////single key search///////
    public static void ranged_search(int start, int end) {
        /*first, find the starting node
        same as the way we search for a single key.
        -> single_search(start) but don't have to save the path
         */
        Node now;
        now = root;
        boolean isFound = false;

        // if there is nothing in the tree, don't have to search for keys
        if(now.m==0){
            System.out.println("Nothing in the tree!");
            return;
        }

        //same as single key search
        while (now.p.get(0).child != null) {
            isFound = false;
            for (int i = 0; i < now.m; i++) {
                if (start < now.p.get(i).key) {
                    now = now.p.get(i).child;
                    isFound = true;
                    break;
                }
            }
            if (isFound == false) { //came to the end but not found
                now = now.r;
            }
        }
        //print the leaf keys on the right side of the found key
        //until the right sibling is bigger than end
        while (true) {
            if(now.m==0) {
                System.out.println(now.r.p.get(0).key); continue;}
            if(now.p.get(0).key > end)
                break;
            printRangedSearch(now, start, end);//print leaf keys
            if (now.r == null) //is the end of the tree (the biggest)
                break;
            now = now.r;//move on to its right sibling
        }
    }

    //print all the keys between start and end
    public static void printRangedSearch(Node n, int start, int end) {
        //in the node, both keys smaller than end and keys bigger than end can be there.
        for (int i = 0; i < n.m; i++) {
            if (n.p.get(i).key > end) { //if the key is bigger than end, stop
                return;
            }
            // if it is between start and end, print its key and value
            if (n.p.get(i).key <= end && n.p.get(i).key >= start) {
                System.out.println(n.p.get(i).key + ", " + n.p.get(i).val);
            }
        }
    }
    //read key to delete and do the deletion
    public static void readDeletion(String file) throws Exception {
        File delete = new File(file);
        String line;
        try {
            BufferedReader b = new BufferedReader(new FileReader(delete));
            while ((line = b.readLine()) != null) { //read line till the end
                String key = line;
                delete(Integer.parseInt(key));//delete
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //find its left sibling
    public static Node leftSibling(Node now) {
        int NodeIndex = IndexInParent(now);
        if (now.parent==null||NodeIndex == 0)//now==leftmost node
            return null;
        else
            return now.parent.p.get(NodeIndex - 1).child;
    }

    //find its right sibling
    public static Node rightSibling(Node now) {
        int NodeIndex = IndexInParent(now);
        if (now.parent==null||NodeIndex == now.parent.m) //now==rightmost node
            return null;

        if (NodeIndex == 0) {//now==leftmost child
            if (now.parent.m == 1) // sib is a rightmost child
                return now.parent.r;
            return now.parent.p.get(NodeIndex + 1).child;
        }
        if (now.parent.m == NodeIndex + 1) // sib is a rightmost child
            return now.parent.r;
        return now.parent.p.get(NodeIndex + 1).child;

    }
    //return the index of the key in its node
    public static int findIndex(Node now, int key) {
        int i;
        for (i = 0; i < now.m; i++) {
            if (key == now.p.get(i).key)
                return i;
        }
        return -1;
    }

    public static void delete(int key){
        Node now;
        //now=WhichNodeToInsert(key);
        now=root;
        int index;

        //find node where the key is & its index
        while (true) {
            index = now.m;
            for (int i = 0; i < now.m; i++) {
                if (key < now.p.get(i).key) {
                    index = i;
                    break;
                }
            }

            if (now.m == 0 || now.p.get(0).child == null)//has nothing or is leaf
                break;
            if (index == now.m)//is the rightmost node -> move on to its right child
                now = now.r;
            else //else move on to its left child
                now = now.p.get(index).child;

        }
        int indexInNode= findIndex(now,key);
        Node lefts=leftSibling(now);
        int parentKey = now.p.get(0).key; //node that can be swapped to be the parent if needed later

        if(indexInNode!=-1) { //if key is found in the tree
            now.p.remove(indexInNode); //delete
            now.m--;
            commands.add(new commands(key,0,'d'));
        }
        else{
            System.out.println(key+" is not in the tree.");
        }

        if(now.m<(nodeSize-1)/2) { //underflow
            Node right = rightSibling(now);
            Node left = leftSibling(now);  //save left and right nodes

            //borrow from left
            if (left != null && left.m > (nodeSize - 1) / 2) {
                int lastIndex=left.m-1;
                pair p= new pair(left.p.get(lastIndex).key,left.p.get(lastIndex).val,null);
                now.p.add(0,p);
                now.m++;  //add the last pair of 'left' to now (in the front)
                left.p.remove(left.m - 1);
                left.m--; //remove the pair from 'left'
                deleteIfNonLeaf(parentKey, now.p.get(0).key); //check non-leaf node
            }
            //borrow from right
            else if (right != null && right.m > (nodeSize - 1) / 2) {
                pair p=new pair(right.p.get(0).key,right.p.get(0).val,null);
                now.p.add(p);
                now.m++;//add the first pair of 'right' to now (at the end)
                right.p.remove(0);
                right.m--;
                deleteIfNonLeaf(now.p.get(now.m - 1).key, right.p.get(0).key);//check non-leaf node

                if (indexInNode == 0)//if now == the first child of its parent
                    deleteIfNonLeaf(parentKey, now.p.get(0).key);

            } else {//cannot borrow pairs from siblings

                if (now.parent != null) {//has parent Node

                    int indexinP = IndexInParent(now);

                    if (left != null) { //has left-sibling

                        for (int i = 0; i < now.m; i++) { //move all pairs and child of 'now' to 'left'
                            pair move = new pair(now.p.get(i).key, now.p.get(i).val, null);
                            left.m++;
                        }
                        left.r = now.r; //move the last child(r) to left

                        //put left where now were before
                        if (indexinP == now.parent.m) //if now=rightmost child of its parent node
                            now.parent.r = left;
                        else
                            now.parent.p.get(indexinP).child = left;

                        //remove pair on the left side of the parent of 'now'
                        now.parent.p.remove(indexinP - 1);
                        now.parent.m--;

                        if(now.parent!=null){
                            if (now.parent.m >= (nodeSize - 1) / 2)//has a parent node && no underflow
                                return;
                            else
                                parentUnderflow_Merge(now.parent); //has a parent node && underflow -> merge
                        }
                        else if (now.parent==null&&now.parent.m==0){ //now.parent==root&&root is empty -> 'left' is the new root
                            root = left;
                            root.parent = null;
                        }
                        else{
                            return; //good
                        }
                    }
                    else { //no left siblings
                        //move pairs in 'now' to 'right'(in the front)
                        for(int i=0;i<now.m;i++){

                            right.p.add(0,new pair(now.p.get(now.m-i-1).key,now.p.get(now.m-i-1).val,null));
                            right.m++;
                        }

                        if (indexInNode == 0) //leftmost node
                            deleteIfNonLeaf(key, right.p.get(0).key);

                        now.parent.p.remove(indexinP); //remove child node with the deleted key
                        now.parent.m--;

                        if (now.parent == root) {
                            if(root.m==0){ //root is empty
                                root = right; //right is the new root
                                root.parent = null; //roots don't have parents
                            }
                            else
                                return; //good

                        } else {
                            if (now.parent.m < (nodeSize - 1) / 2)//parent underflow
                                parentUnderflow_Merge(now.parent);

                        }

                    }

                }
            }
        }
        else { //no underflows -> now.m>=(nodeSize-1)/2
            deleteIfNonLeaf(parentKey, now.p.get(0).key); //check if key is in a non_leaf node and end deletion
            return;
        }

    }
    //delete/change key if the key is in a non-leaf Node
    public static void deleteIfNonLeaf(int delete, int newKey) {
        Node now = root;//look for 'delete' in a non-leaf node
        while (true) {
            boolean foundParent = false;//found parent node
            if (now.m == 0 || now.p.get(0).child == null)
                break;

            for (int i = 0; i < now.m; i++) {

                if (delete < now.p.get(i).key) {
                    now = now.p.get(i).child; //delete is child of new now
                    foundParent = true; //found parent
                    break;
                }
                if (delete== now.p.get(i).key) {
                    now.p.get(i).key = newKey; //delete is a nonleaf node
                    return;

                }
            }
            if (foundParent==false)
                now = now.r;//check rightmost child of 'now'(while loop)
        }
    }

    //solve parent underflow problems (by combining with sibling nodes)
    public static void parentUnderflow_Merge(Node now) {
        int indexinP = IndexInParent(now);// now is idx.th child of now.parent

        if (now.parent != null) {
            Node leftSib = leftSibling(now);//left sib
            Node rightSib = rightSibling(now);//right sib

            if (rightSib != null) {
                //add the pair with 'now' as child  -> to now
                pair p = new pair(now.parent.p.get(indexinP).key, now.parent.p.get(indexinP).val, now.r); // and its new child is now.r
                now.p.add(p);
                now.m++;
                //remove the pair with the key
                now.parent.p.remove(indexinP);
                now.parent.m--;

                //add all pairs in right to 'now'
                for (int i = 0; i < rightSib.m; i++) {
                    now.p.add(rightSib.p.get(i));
                    now.m++;
                    rightSib.p.get(i).child.parent = now;
                }
                now.r = rightSib.r; // move rightmost child to rightmost child of 'now'
                now.r.parent = now;

                //link the children and parents
                if (rightSib.parent.m > 0) {
                    if (rightSib.parent.m != indexinP) {
                        rightSib.parent.p.get(indexinP).child = now;
                    }
                }
                else
                    rightSib.parent.r = now;
                if (now.m >= nodeSize) //parent-overflow occurred after merging
                    nonLeafSplit(now); //then split parents

                //check parent node
                if (rightSib.parent != root) {
                    if (rightSib.parent.m < (nodeSize - 1) / 2) { //underflow occurred in rightSib.parent
                        parentUnderflow_Merge(rightSib.parent); //call merge again
                    }
                } else {
                    if (rightSib.parent.m == 0) { //rightSib.parent is root and is empty.
                        root = now;
                        root.parent = null; //now is the new root
                    }
                }

            } else if (leftSib != null) {
                int leftIdx = indexinP - 1;//index of leftSib in the parent's node

                //add pair with left as child
                pair p = new pair(now.parent.p.get(leftIdx).key, now.parent.p.get(leftIdx).val, leftSib.r);//and its new child is leftSib.r
                leftSib.p.add(p);
                leftSib.m++;
                //remove the pair
                now.parent.p.remove(leftIdx);
                now.parent.m--;

                //add all pairs in now to leftSib
                for (int i = 0; i < now.m; i++) {
                    leftSib.p.add(now.p.get(i));
                    leftSib.m++;
                    now.p.get(i).child.parent = leftSib;
                }

                leftSib.r = now.r;//move the rightmost child to the rightmost child of now
                leftSib.parent = leftSib;


                //link the children and parents
                if (now.parent.m > 0) {
                    if (leftIdx != now.parent.m) {
                        now.parent.p.get(leftIdx).child = leftSib;
                    }
                } else
                    now.parent.r = leftSib;

                if (leftSib.m >= nodeSize) //parent overflow occurred after merging
                    nonLeafSplit(leftSib);//split


                if (now.parent!=null) {
                    if (now.parent.m < (nodeSize - 1) / 2) { //underflow occurred in rightSib.parent(except when parent==root)
                        parentUnderflow_Merge(now.parent);
                    }
                }
                else { //parent==root
                    if (now.parent.m == 0) {  //needs new root
                        root = leftSib; //leftSib is the new root
                        root.parent = null;
                    }
                }
            }
        }
    }
}