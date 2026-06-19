

class Record implements Comparable<Record>{

    public static final int PUT = 1;
    public static final int DELETE = 2;
    
    int key;
    String value;
    int opType;
    
    Record(int key, String value, int opType){
        this.key = key;
        this.value = value;
        this.opType = opType;
    }

    void printRecord(){
        System.out.println("Key:{" + this.key + "}," + "Value:{" + this.value + "}" + "opType:{" + this.opType + "}");
    }

    @Override
    public int compareTo(Record other){
        return Integer.compare(this.key, other.key);
    }

}