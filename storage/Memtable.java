import java.util.TreeMap;


public class Memtable {

    TreeMap<Integer, String> map;

    Memtable(){
        this.map = new TreeMap<>();
    }


    void printMap(){
        System.out.println(map);
    }

    void setKey(Integer key, String value){

        if ((key == null) || (value == null)){
            System.out.println("Null key or value");
            return;
        }

        map.put(key,value);
        
    }

    String getKey(Integer key){
        if (map.containsKey(key)){
            return map.get(key);
        } else {
            return "null";
        }
    }

    int memtableSize(){
        return map.size();
    }

    TreeMap<Integer, String> flushMemtable(){
        return map;
    }


    public static void main(String[] args){
        System.out.println("Hello memtable!");
        Memtable t = new Memtable();
        t.setKey(10, "Alice");
        t.setKey(20, "Bob");
        t.setKey(30, "John");
        t.setKey(5, "Kastark");
        t.printMap();
        System.out.println(t.getKey(30));
        TreeMap<Integer, String> f = t.flushMemtable();
        System.out.println(f);
    }
}