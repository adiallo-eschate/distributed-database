import java.util.ArrayList;
import java.util.Collections;
import java.util.*;



/**
 *  A b-tree implementation to check my understand
 * 
 * 
 * @author Abdoul Diallo
 * @version 1.1
 * 
 */

// using a minimum of 2 degrees

class TreeNode {

    private Integer minDegree;
    private Boolean isALeafNode = false;
    private ArrayList<Integer> keys;
    private ArrayList<TreeNode> children;

    TreeNode(Integer minDegree, Boolean isALeafNode){
            if (!(minDegree instanceof Integer) || (minDegree < 2)){
                System.out.println("A minimum of 2 degrees required. Exiting program....");
                System.exit(0);
            }

            this.minDegree = minDegree;
            this.keys = new ArrayList<>();
            this.children = new ArrayList<>();
            this.isALeafNode = isALeafNode;
    }

    void printNode(){
        System.out.print("keys = [");
        for (int  i = 0; i < keys.size(); ++i){
            System.out.print(keys.get(i));
        }

        System.out.print(" ]");

        System.out.print("  isALeafNode = ");
        System.out.print(this.isALeafNode);
    }

    void addToKeys(Integer key){
        this.keys.add(key);
    }

    void removeKey(Integer key){
        this.keys.remove(key);
    }

    Integer getKey(Integer key){
        return this.keys.get(key);
    }

    ArrayList<Integer> retrieveKeys(){
        return this.keys;
    }

    void addToChildren(TreeNode child){
        this.children.add(child);
    }

    void removeChild(TreeNode child){
        this.children.remove(child);
    }

    TreeNode getChild(TreeNode child){
        Integer indexOfChild = this.children.indexOf(child);
        return this.children.get(indexOfChild);
    }

    ArrayList<TreeNode> retrieveChildren(){
        return this.children;
    }

    Boolean getIsALeafNode(){
        return this.isALeafNode;
    }



}


class Btree {

    private Integer minDegree;
    private TreeNode rootNode;

    Btree(Integer minDegree){
        if (!(minDegree instanceof Integer) || (minDegree < 2)){
            System.out.println("A minimum of 2 degrees required. Exiting program....");
            System.exit(0);
        }

        this.minDegree = minDegree;
        this.rootNode = new TreeNode(minDegree, true);

    }


    TreeNode getRootNode(){
        return this.rootNode;
    }

    Integer getMinDegree(){
        return this.minDegree;
    }


    Integer search(Integer target, TreeNode node){
        if (node == null){
            node = this.rootNode;
        }

        ArrayList<Integer> keys = node.retrieveKeys();
        ArrayList<TreeNode> children = node.retrieveChildren();

        Integer i;
        for (i = 0; i < keys.size(); ++i){
            if (target >= keys.get(i)){
                break;
            }
        }

        if ((i < keys.size()) && (target == keys.get(i))){
            System.out.print("Key Found: ");
            node.printNode();
            System.out.print("Index: " + i);
            return 1;
        }

        if (node.getIsALeafNode()){
            return -1;
        }

        return search(target, children.get(i));

    }

    void traversal(TreeNode node){
        if (node == null){
            node = this.rootNode;
        }
 
        ArrayList<Integer> keys = node.retrieveKeys();
        ArrayList<TreeNode> children = node.retrieveChildren();

        for (int i = 0; i < keys.size(); ++i){
            if (!(node.getIsALeafNode())){
                traversal(children.get(i));
            }

            System.out.println(keys.get(i));
        }

        if (!(node.getIsALeafNode())){
            traversal(children.get(keys.size()));
        }
    }

    void insert(Integer key) {
        TreeNode root = this.rootNode;
        ArrayList<Integer> rootKeys = root.retrieveKeys();
    
        if (rootKeys.size() == (2 * minDegree - 1)) {
            TreeNode newRootNode = new TreeNode(this.minDegree, false);
            ArrayList<TreeNode> newRootChildren = newRootNode.retrieveChildren();
            newRootChildren.add(root);
            splitChildNode(newRootNode, 0);
            this.rootNode = newRootNode;
            insertIntoNonFullNode(this.rootNode, key);
        } else {
            insertIntoNonFullNode(root, key);
        }
    }
    
    void insertIntoNonFullNode(TreeNode node, Integer key) {
        ArrayList<Integer> keys = node.retrieveKeys();
        ArrayList<TreeNode> children = node.retrieveChildren();
    
        int i = keys.size() - 1;
    
        if (node.getIsALeafNode()) {
            keys.add(0);
            while (i >= 0 && key < keys.get(i)) {
                keys.set(i + 1, keys.get(i));
                i--;
            }
            keys.set(i + 1, key);
        } else {
            while (i >= 0 && key < keys.get(i)) {
                i--;
            }
            i++;
    
            TreeNode child = children.get(i);
            ArrayList<Integer> childKeys = child.retrieveKeys();
    
            if (childKeys.size() == (2 * this.minDegree - 1)) {
                splitChildNode(node, i);
                keys = node.retrieveKeys();
                children = node.retrieveChildren();
                if (key > keys.get(i)) {
                    i++;
                }
                child = children.get(i);
            }
    
            insertIntoNonFullNode(child, key);
        }
    }
    
    void splitChildNode(TreeNode parentNode, Integer childNodeIndex) {
        ArrayList<Integer> parentKeys = parentNode.retrieveKeys();
        ArrayList<TreeNode> parentChildren = parentNode.retrieveChildren();
    
        int t = this.minDegree;
        TreeNode fullNode = parentChildren.get(childNodeIndex);
    
        ArrayList<Integer> fullKeys = fullNode.retrieveKeys();
        ArrayList<TreeNode> fullChildren = fullNode.retrieveChildren();
    
        TreeNode newChildNode = new TreeNode(t, fullNode.getIsALeafNode());
    
        Integer median = fullKeys.get(t - 1);
    
        for (int i = t; i < fullKeys.size(); i++) {
            newChildNode.retrieveKeys().add(fullKeys.get(i));
        }
        while (fullKeys.size() > t - 1) {
            fullKeys.remove(fullKeys.size() - 1);
        }
    
        if (!fullNode.getIsALeafNode()) {
            for (int i = t; i < fullChildren.size(); i++) {
                newChildNode.retrieveChildren().add(fullChildren.get(i));
            }
            while (fullChildren.size() > t) {
                fullChildren.remove(fullChildren.size() - 1);
            }
        }
    
        parentChildren.add(childNodeIndex + 1, newChildNode);
        parentKeys.add(childNodeIndex, median);
    }


    void delete(Integer key) {
        if (rootNode == null) return;

        deleteKey(rootNode, key);

        if (rootNode.retrieveKeys().isEmpty() && !rootNode.getIsALeafNode()) {
            rootNode = rootNode.retrieveChildren().get(0);
        }
    }


    private void deleteKey(TreeNode node, Integer key) {

        ArrayList<Integer> keys = node.retrieveKeys();
        ArrayList<TreeNode> children = node.retrieveChildren();
        int t = this.minDegree;

        int idx = 0;
        while (idx < keys.size() && key > keys.get(idx)) {
            idx++;
        }

        if (idx < keys.size() && keys.get(idx).equals(key)) {

            if (node.getIsALeafNode()) {
                keys.remove(idx);
            } else {
                deleteFromInternal(node, key, idx);
            }

        } else {

            if (node.getIsALeafNode()) {
                return;
            }

            if (children.get(idx).retrieveKeys().size() < t) {
                fixChild(node, idx);
            }

            if (idx > keys.size()) {
                deleteKey(children.get(idx - 1), key);
            } else {
                deleteKey(children.get(idx), key);
            }
        }
    }

    
    private void deleteFromInternal(TreeNode node, Integer key, int idx) {

        int t = this.minDegree;
        ArrayList<TreeNode> children = node.retrieveChildren();

        TreeNode leftChild = children.get(idx);
        TreeNode rightChild = children.get(idx + 1);

        if (leftChild.retrieveKeys().size() >= t) {

            Integer pred = getPredecessor(leftChild);
            node.retrieveKeys().set(idx, pred);
            deleteKey(leftChild, pred);

        } else if (rightChild.retrieveKeys().size() >= t) {

            Integer succ = getSuccessor(rightChild);
            node.retrieveKeys().set(idx, succ);
            deleteKey(rightChild, succ);

        } else {

            mergeNodes(node, idx);
            deleteKey(leftChild, key);
        }
    }


    private Integer getPredecessor(TreeNode node) {
        while (!node.getIsALeafNode()) {
            ArrayList<TreeNode> children = node.retrieveChildren();
            node = children.get(children.size() - 1);
        }
        ArrayList<Integer> keys = node.retrieveKeys();
        return keys.get(keys.size() - 1);
    }


    private Integer getSuccessor(TreeNode node) {
        while (!node.getIsALeafNode()) {
            ArrayList<TreeNode> children = node.retrieveChildren();
            node = children.get(0);
        }
        return node.retrieveKeys().get(0);
    }


    private void mergeNodes(TreeNode parent, int idx) {

        ArrayList<Integer> parentKeys = parent.retrieveKeys();
        ArrayList<TreeNode> parentChildren = parent.retrieveChildren();

        TreeNode left = parentChildren.get(idx);
        TreeNode right = parentChildren.get(idx + 1);

        left.retrieveKeys().add(parentKeys.remove(idx));

        left.retrieveKeys().addAll(right.retrieveKeys());

        if (!left.getIsALeafNode()) {
            left.retrieveChildren().addAll(right.retrieveChildren());
        }

        parentChildren.remove(idx + 1);
    }


    private void fixChild(TreeNode node, int idx) {

        int t = this.minDegree;
        ArrayList<TreeNode> children = node.retrieveChildren();

        if (idx > 0 && children.get(idx - 1).retrieveKeys().size() >= t) {

            borrowFromLeft(node, idx);

        } else if (idx < children.size() - 1 &&
                   children.get(idx + 1).retrieveKeys().size() >= t) {

            borrowFromRight(node, idx);

        } else {

            if (idx < children.size() - 1) {
                mergeNodes(node, idx);
            } else {
                mergeNodes(node, idx - 1);
            }
        }
    }


    private void borrowFromLeft(TreeNode parent, int idx) {

        TreeNode child = parent.retrieveChildren().get(idx);
        TreeNode leftSibling = parent.retrieveChildren().get(idx - 1);

        ArrayList<Integer> childKeys = child.retrieveKeys();
        ArrayList<Integer> leftKeys = leftSibling.retrieveKeys();
        ArrayList<Integer> parentKeys = parent.retrieveKeys();

        childKeys.add(0, parentKeys.get(idx - 1));

        parentKeys.set(idx - 1, leftKeys.remove(leftKeys.size() - 1));

        if (!leftSibling.getIsALeafNode()) {
            ArrayList<TreeNode> childChildren = child.retrieveChildren();
            ArrayList<TreeNode> leftChildren = leftSibling.retrieveChildren();
            childChildren.add(0, leftChildren.remove(leftChildren.size() - 1));
        }
    }


    private void borrowFromRight(TreeNode parent, int idx) {

        TreeNode child = parent.retrieveChildren().get(idx);
        TreeNode rightSibling = parent.retrieveChildren().get(idx + 1);

        ArrayList<Integer> childKeys = child.retrieveKeys();
        ArrayList<Integer> rightKeys = rightSibling.retrieveKeys();
        ArrayList<Integer> parentKeys = parent.retrieveKeys();

        childKeys.add(parentKeys.get(idx));

        parentKeys.set(idx, rightKeys.remove(0));

        if (!rightSibling.getIsALeafNode()) {
            ArrayList<TreeNode> childChildren = child.retrieveChildren();
            ArrayList<TreeNode> rightChildren = rightSibling.retrieveChildren();
            childChildren.add(rightChildren.remove(0));
        }
    }


   
// program entry 
    public static void main(String[] args) {
        Btree tree = new Btree(3);

        tree.insert(10);
        tree.insert(20);
        tree.insert(5);
        tree.insert(6);
        tree.insert(12);
        tree.insert(30);
        tree.insert(7);
        tree.insert(17);

        System.out.println("Traversing...");
        tree.traversal(tree.getRootNode());

        System.out.println("Search 6:");
        tree.search(6, tree.getRootNode());

        System.out.println("Search 15:");
        tree.search(15, tree.getRootNode());

        tree.delete(6);
        tree.delete(7);
        tree.delete(17);

        System.out.println("Traversal after deletes:");
        tree.traversal(tree.getRootNode());
    }

}



