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
        System.out.print("Left Child: ");
        System.out.print(this.left);
        if (this.left != null) System.out.println(this.left.key);
        System.out.print("Right Child: ");
        System.out.print(this.right);
        if (this.right != null) System.out.println(this.right.key);
        System.out.println("-----------------------");
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

    void printTree(RBNode node){
        root = this.root;
        System.out.println(root.key);
        if (root.left != null){
            node = root.left;
            printTree(root.left);
        } else if (root.right != null){
            node = root.right;
            printTree(root.right);
        }
    }



    public static void main(String[] args){

        RedBlackTree tree = new RedBlackTree(20);
        RBNode n1 = new RBNode(14);
        RBNode n2 = new RBNode(21);
        RBNode n3 = new RBNode(50);
        RBNode n4 = new RBNode(7);
        RBNode n5 = new RBNode(25);
        tree.addLeftChild(tree.root, n1);
        tree.addRightChild(tree.root, n2);
        tree.addRightChild(n2, n3);
        tree.addLeftChild(n2, n4);
        tree.addRightChild(n1, n5);
        tree.printTree(tree.root);
        tree.search(tree.root, 25);
        tree.root.printNode();
    }
}