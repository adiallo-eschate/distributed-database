import java.util.ArrayList;

class BPlusTreeNode {

    private Integer minDegree;
    private Boolean isALeafNode = false;
    private ArrayList<Integer> keys;
    private ArrayList<BPlusTreeNode> children;
    private BPlusTreeNode nextLeaf;

    BPlusTreeNode(Integer minDegree, Boolean isALeafNode){
        if (!(minDegree instanceof Integer) || (minDegree < 2)){
            System.out.println("A minimum of 2 degrees required. Exiting program....");
            System.exit(0);
        }

        this.minDegree = minDegree;
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
        this.isALeafNode = isALeafNode;
        this.nextLeaf = null;
    }

    void printNode(){
        System.out.print("keys = [");
        for (int  i = 0; i < keys.size(); ++i){
            System.out.print(keys.get(i));
            if (i < keys.size() - 1) System.out.print(", ");
        }
        System.out.print(" ]");
        System.out.print("  isALeafNode = ");
        System.out.print(this.isALeafNode);
        System.out.println();
    }

    void addToKeys(Integer key){
        this.keys.add(key);
    }

    void removeKey(Integer key){
        this.keys.remove(key);
    }

    Integer getKey(Integer index){
        return this.keys.get(index);
    }

    ArrayList<Integer> retrieveKeys(){
        return this.keys;
    }

    void addToChildren(BPlusTreeNode child){
        this.children.add(child);
    }

    void removeChild(BPlusTreeNode child){
        this.children.remove(child);
    }

    BPlusTreeNode getChild(Integer index){
        return this.children.get(index);
    }

    ArrayList<BPlusTreeNode> retrieveChildren(){
        return this.children;
    }

    Boolean getIsALeafNode(){
        return this.isALeafNode;
    }

    BPlusTreeNode getNextLeaf(){
        return this.nextLeaf;
    }

    void setNextLeaf(BPlusTreeNode nextLeaf){
        this.nextLeaf = nextLeaf;
    }
}


class BPlusTree {

    private Integer minDegree;
    private BPlusTreeNode rootNode;

    BPlusTree(Integer minDegree){
        if (!(minDegree instanceof Integer) || (minDegree < 2)){
            System.out.println("A minimum of 2 degrees required. Exiting program....");
            System.exit(0);
        }

        this.minDegree = minDegree;
        this.rootNode = new BPlusTreeNode(minDegree, true);
    }

    BPlusTreeNode getRootNode(){
        return this.rootNode;
    }

    Integer getMinDegree(){
        return this.minDegree;
    }

    Integer search(Integer target){
        BPlusTreeNode node = this.rootNode;

        while (!node.getIsALeafNode()){
            ArrayList<Integer> keys = node.retrieveKeys();
            ArrayList<BPlusTreeNode> children = node.retrieveChildren();

            int i = 0;
            while (i < keys.size() && target >= keys.get(i)){
                i++;
            }
            node = children.get(i);
        }

        ArrayList<Integer> leafKeys = node.retrieveKeys();
        for (int i = 0; i < leafKeys.size(); ++i){
            if (leafKeys.get(i).equals(target)){
                System.out.print("Key Found: ");
                node.printNode();
                System.out.println("Index: " + i);
                return 1;
            }
        }

        System.out.println("Key not found: " + target);
        return -1;
    }

    void traversal(){
        BPlusTreeNode node = this.rootNode;

        while (!node.getIsALeafNode()){
            node = node.retrieveChildren().get(0);
        }

        while (node != null){
            ArrayList<Integer> keys = node.retrieveKeys();
            for (int i = 0; i < keys.size(); ++i){
                System.out.println(keys.get(i));
            }
            node = node.getNextLeaf();
        }
    }

    void insert(Integer key){
        BPlusTreeNode root = this.rootNode;

        if (root.retrieveKeys().size() == (2 * minDegree - 1)){
            BPlusTreeNode newRootNode = new BPlusTreeNode(this.minDegree, false);
            newRootNode.retrieveChildren().add(root);
            splitChildNode(newRootNode, 0);
            this.rootNode = newRootNode;
            insertIntoNonFullNode(this.rootNode, key);
        } else {
            insertIntoNonFullNode(root, key);
        }
    }

    void insertIntoNonFullNode(BPlusTreeNode node, Integer key){
        ArrayList<Integer> keys = node.retrieveKeys();
        ArrayList<BPlusTreeNode> children = node.retrieveChildren();

        int i = keys.size() - 1;

        if (node.getIsALeafNode()){
            keys.add(0);
            while (i >= 0 && key < keys.get(i)){
                keys.set(i + 1, keys.get(i));
                i--;
            }
            keys.set(i + 1, key);
        } else {
            while (i >= 0 && key < keys.get(i)){
                i--;
            }
            i++;

            BPlusTreeNode child = children.get(i);
            ArrayList<Integer> childKeys = child.retrieveKeys();

            if (childKeys.size() == (2 * this.minDegree - 1)){
                splitChildNode(node, i);
                keys = node.retrieveKeys();
                children = node.retrieveChildren();
                if (key >= keys.get(i)){
                    i++;
                }
                child = children.get(i);
            }

            insertIntoNonFullNode(child, key);
        }
    }

    void splitChildNode(BPlusTreeNode parentNode, Integer childNodeIndex){
        ArrayList<Integer> parentKeys = parentNode.retrieveKeys();
        ArrayList<BPlusTreeNode> parentChildren = parentNode.retrieveChildren();

        int t = this.minDegree;
        BPlusTreeNode fullNode = parentChildren.get(childNodeIndex);

        ArrayList<Integer> fullKeys = fullNode.retrieveKeys();
        ArrayList<BPlusTreeNode> fullChildren = fullNode.retrieveChildren();

        BPlusTreeNode newChildNode = new BPlusTreeNode(t, fullNode.getIsALeafNode());

        int splitIndex;
        if (fullNode.getIsALeafNode()){
            splitIndex = fullKeys.size() / 2;
        } else {
            splitIndex = t - 1;
        }

        if (fullNode.getIsALeafNode()){
            for (int i = splitIndex; i < fullKeys.size(); i++){
                newChildNode.retrieveKeys().add(fullKeys.get(i));
            }
            while (fullKeys.size() > splitIndex){
                fullKeys.remove(fullKeys.size() - 1);
            }

            newChildNode.setNextLeaf(fullNode.getNextLeaf());
            fullNode.setNextLeaf(newChildNode);

            Integer promoteKey = newChildNode.retrieveKeys().get(0);
            parentChildren.add(childNodeIndex + 1, newChildNode);
            parentKeys.add(childNodeIndex, promoteKey);

        } else {
            Integer median = fullKeys.get(splitIndex);

            for (int i = splitIndex + 1; i < fullKeys.size(); i++){
                newChildNode.retrieveKeys().add(fullKeys.get(i));
            }
            while (fullKeys.size() > splitIndex){
                fullKeys.remove(fullKeys.size() - 1);
            }

            for (int i = splitIndex + 1; i < fullChildren.size(); i++){
                newChildNode.retrieveChildren().add(fullChildren.get(i));
            }
            while (fullChildren.size() > splitIndex + 1){
                fullChildren.remove(fullChildren.size() - 1);
            }

            parentChildren.add(childNodeIndex + 1, newChildNode);
            parentKeys.add(childNodeIndex, median);
        }
    }

    public static void main(String[] args){
        BPlusTree n = new BPlusTree(3);

        n.insert(10);
        n.insert(20);
        n.insert(5);
        n.insert(6);
        n.insert(12);
        n.insert(30);
        n.insert(7);
        n.insert(17);
        n.insert(3);
        n.insert(25);

        System.out.println("Traversal:");
        n.traversal();

        System.out.println("Search 6:");
        n.search(6);

        System.out.println("Search 15:");
        n.search(15);
    }
}
