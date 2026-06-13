import java.util.*;

class RBNode {
    Integer key;
    RBNode left;
    RBNode right;
    RBNode parent;
    String color; 

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

    // RB Properties:
    // 1. Every node is Red or Black.
    // 2. Root is always Black.
    // 3. NIL sentinel is Black.
    // 4. Red nodes cannot have Red children.
    // 5. Every path from root to leaf has same number of Black nodes.

    RedBlackTree(Integer key){
        Nil = new RBNode(null);
        Nil.color = "Black";
        Nil.left = Nil.right = Nil.parent = Nil;

        RBNode r = new RBNode(key);
        r.color = "Black";
        r.left = r.right = r.parent = Nil;
        this.root = r;
    }


    void leftRotate(RBNode x){
        RBNode y = x.right;
        x.right = y.left;

        if (y.left != Nil){
            y.left.parent = x;
        }

        y.parent = x.parent;

        if (x.parent == Nil){
            this.root = y;
        } else if (x == x.parent.left){
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }

        y.left = x;
        x.parent = y;
    }

    void rightRotate(RBNode x){
        RBNode y = x.left;
        x.left = y.right;

        if (y.right != Nil){
            y.right.parent = x;
        }

        y.parent = x.parent;

        if (x.parent == Nil){
            this.root = y;
        } else if (x == x.parent.right){
            x.parent.right = y;
        } else {
            x.parent.left = y;
        }

        y.right = x;
        x.parent = y;
    }


    void insert(RBNode root, Integer key){
        RBNode newNode = new RBNode(key);
        newNode.left = newNode.right = newNode.parent = Nil;

        RBNode y = Nil;
        RBNode x = this.root;

        while (x != Nil){
            y = x;
            if (newNode.key < x.key){
                x = x.left;
            } else {
                x = x.right;
            }
        }

        newNode.parent = y;

        if (y == Nil){
            this.root = newNode;
        } else if (newNode.key < y.key){
            y.left = newNode;
        } else {
            y.right = newNode;
        }

        newNode.color = "Red";

        insertFixup(newNode);
    }

    void insertFixup(RBNode z){
        while (z.parent.color.equals("Red")){
            if (z.parent == z.parent.parent.left){
                RBNode y = z.parent.parent.right;

                if (y.color.equals("Red")){
                    // Case 1: Red uncle
                    z.parent.color = "Black";
                    y.color = "Black";
                    z.parent.parent.color = "Red";
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.right){
                        // Case 2: Left-Right
                        z = z.parent;
                        leftRotate(z);
                    }
                    // Case 3: Left-Left
                    z.parent.color = "Black";
                    z.parent.parent.color = "Red";
                    rightRotate(z.parent.parent);
                }
            } else {
                RBNode y = z.parent.parent.left;

                if (y.color.equals("Red")){
                    // Mirror Case 1
                    z.parent.color = "Black";
                    y.color = "Black";
                    z.parent.parent.color = "Red";
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.left){
                        // Mirror Case 2
                        z = z.parent;
                        rightRotate(z);
                    }
                    // Mirror Case 3
                    z.parent.color = "Black";
                    z.parent.parent.color = "Red";
                    leftRotate(z.parent.parent);
                }
            }
        }

        this.root.color = "Black";
    }


    void delete(RBNode node, Integer key){
        RBNode z = searchNode(this.root, key);
        if (z == Nil){
            System.out.println("Key not found");
            return;
        }

        RBNode y = z;
        RBNode x;
        String yOriginalColor = y.color;

        if (z.left == Nil){
            x = z.right;
            transplant(z, z.right);
        } else if (z.right == Nil){
            x = z.left;
            transplant(z, z.left);
        } else {
            y = minimum(z.right);
            yOriginalColor = y.color;
            x = y.right;

            if (y.parent == z){
                x.parent = y;
            } else {
                transplant(y, y.right);
                y.right = z.right;
                y.right.parent = y;
            }

            transplant(z, y);
            y.left = z.left;
            y.left.parent = y;
            y.color = z.color;
        }

        if (yOriginalColor.equals("Black")){
            deleteFixup(x);
        }
    }

    void deleteFixup(RBNode x){
        while (x != this.root && x.color.equals("Black")){
            if (x == x.parent.left){
                RBNode w = x.parent.right;

                if (w.color.equals("Red")){
                    w.color = "Black";
                    x.parent.color = "Red";
                    leftRotate(x.parent);
                    w = x.parent.right;
                }

                if (w.left.color.equals("Black") && w.right.color.equals("Black")){
                    w.color = "Red";
                    x = x.parent;
                } else {
                    if (w.right.color.equals("Black")){
                        w.left.color = "Black";
                        w.color = "Red";
                        rightRotate(w);
                        w = x.parent.right;
                    }

                    w.color = x.parent.color;
                    x.parent.color = "Black";
                    w.right.color = "Black";
                    leftRotate(x.parent);
                    x = this.root;
                }
            } else {
                RBNode w = x.parent.left;

                if (w.color.equals("Red")){
                    w.color = "Black";
                    x.parent.color = "Red";
                    rightRotate(x.parent);
                    w = x.parent.left;
                }

                if (w.right.color.equals("Black") && w.left.color.equals("Black")){
                    w.color = "Red";
                    x = x.parent;
                } else {
                    if (w.left.color.equals("Black")){
                        w.right.color = "Black";
                        w.color = "Red";
                        leftRotate(w);
                        w = x.parent.left;
                    }

                    w.color = x.parent.color;
                    x.parent.color = "Black";
                    w.left.color = "Black";
                    rightRotate(x.parent);
                    x = this.root;
                }
            }
        }

        x.color = "Black";
    }


    RBNode searchNode(RBNode root, Integer key){
        while (root != Nil){
            if (key.equals(root.key)){
                return root;
            } else if (key < root.key){
                root = root.left;
            } else {
                root = root.right;
            }
        }
        return Nil;
    }

    RBNode minimum(RBNode node){
        while (node.left != Nil){
            node = node.left;
        }
        return node;
    }

    void transplant(RBNode u, RBNode v){
        if (u.parent == Nil){
            this.root = v;
        } else if (u == u.parent.left){
            u.parent.left = v;
        } else {
            u.parent.right = v;
        }
        v.parent = u.parent;
    }


    void printTree(RBNode node){
        System.out.println("--------- Tree Print Begin ------------");

        if (node == null){
            node = this.root;
        }

        ArrayList<RBNode> arr = new ArrayList<>();
        arr.add(node);

        for (int i = 0; i < arr.size(); i++){
            RBNode n = arr.get(i);
            if (n == Nil) continue;

            n.printNode();

            if (n.left != Nil) arr.add(n.left);
            if (n.right != Nil) arr.add(n.right);
        }

        System.out.println("--------- Tree Print End --------------");
    }

    public static void main(String[] args){
        RedBlackTree tree = new RedBlackTree(20);
        tree.insert(tree.root, 10);
        tree.insert(tree.root, 30);
        tree.insert(tree.root, 25);
        tree.insert(tree.root, 40);
        tree.insert(tree.root, 5);
        tree.insert(tree.root, 1);

        tree.printTree(tree.root);

        tree.delete(tree.root, 10);
        tree.delete(tree.root, 1);

        tree.printTree(tree.root);
    }
}
