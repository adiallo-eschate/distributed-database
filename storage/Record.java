
class Record {

    int key;
    String value;
    
    Record(int key, String value){
        this.key = key;
        this.value = value;
    }

    void printRecord(){
        System.out.println("Key:{" + this.key + "}," + "Value:{" + this.value + "}");
    }

}