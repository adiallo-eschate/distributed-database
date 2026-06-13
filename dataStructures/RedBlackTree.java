import java.util.*;

class RBNode {
    Integer key;
    RBNode left;
    RBNode right; 
    RBNode parent;
    String color;    /// red = 0 and black = 1;

    RBNode(Integer key){
        this.key = key;
        this.left = this.right = this.parent = null;
        this.color = "Red";
    }

    void printNode(){
        System.out.printf("Key={%d} ", this.key);
        System.out.printf("Color={%s}\n", this.color);
        System.out.print("Parent ref: ");
        System.out.println(this.parent);
        if (this.parent != null) System.out.printf("parentKey={%d}\n", this.parent.key);
        System.out.print("Left Child: ");
        System.out.println(this.left);
        System.out.print("Right Child: ");
        System.out.println(this.right);
        System.out.println("-----------------------------------------");
    }
}




public class RedBlackTree {
    RBNode root;
    RBNode Nil;

    RedBlackTree(Integer key){
        if (key <= 0){
            System.out.println("Key cannot be 0 or less. Exit...");
            System.exit(0);
        }

        RBNode root = new RBNode(key);
        root.color = "Red";
        this.root = root;

        RBNode nil = new RBNode(0);
        nil.color = "Black";
        this.Nil = nil;
    }

    void addLeftChild(RBNode parentNode, RBNode child){
        if (child.key >= parentNode.key){
            System.out.println("Cannot Add Key Greater Than Parent to Left Side");
            return;
        }

        parentNode.left = child;
    }

    void addRightChild(RBNode parentNode, RBNode child){
        if (child.key <= parentNode.key){
            System.out.println("Cannot Add Key Less Than Parent to Right Side");
            return;
        }

        parentNode.right = child;
    }

    void search(RBNode root, Integer target){
        if(target == root.key){
            System.out.println("Target Found");
            root.printNode();
        } else if ((target > root.key) && (root.right != null)){
             search(root.right, target);
        } else if ((target < root.key) && (root.left != null)){
            search(root.left, target);
        } else {

            System.out.println("Target Does Not Exist");
            return;
        }

    }

    void insert(RBNode root, Integer key){
        int levelsSearched = 0;
        if (root == null){
            root = this.root;
        }


        if ((key > root.key) && (root.right == null)){
            RBNode insertNode = new RBNode(key);
            root.right = insertNode;
            System.out.println("Inserted On Right " + key);
        } else if ((key < root.key) && (root.left == null)){
            RBNode insertNode = new RBNode(key);
            root.left = insertNode;
            System.out.println("Inserted On Left " + key);
        } else if ((key > root.key) && (root.right != null)){
            insert(root.right, key);
        } else if ((key < root.key) && (root.left != null)){
            insert(root.left, key);
        } else {
            System.out.println("Could Not Insert Key");
        }

        levelsSearched++;

    }

    void printTree(RBNode node){
        System.out.println("--------- Tree Print Begin ------------");
        if (node == null){
            node = this.root;
        }

        ArrayList<RBNode> arr = new ArrayList<>();

        if (node.left != null){
            arr.add(node.left);
        }

        if (node.right != null){
            arr.add(node.right);
        }
        
        node.printNode();

        for (int i = 0; i < arr.size(); i++){
            arr.get(i).printNode();
            
            if (arr.get(i).left != null){
                arr.add(arr.get(i).left);
            } else if(arr.get(i).right != null){
                arr.add(arr.get(i).right);
            }
        }

        System.out.println("--------- Tree Print End --------------");
    }



    public static void main(String[] args){

        RedBlackTree tree = new RedBlackTree(20);
        tree.printTree(tree.root);
        tree.search(tree.root, 15);
        tree.insert(tree.root, 13);
        tree.insert(tree.root, 100);
        tree.printTree(tree.root);
    }
}