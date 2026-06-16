import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;

public class Engine {
    
    // memtable each string ---> "keylength-key-valuelength- value"
    ArrayList<String> memtable = new ArrayList<>();
    String filePath = "temp.sst";
    BufferedWriter w;
    BufferedReader r;

    Engine(){

        try {
            w = new BufferedWriter(new FileWriter(this.filePath));
            r = new BufferedReader(new FileReader(this.filePath));
        } catch (IOException e){
            e.printStackTrace();
        }

    }


    void get(String key){
        if (memtable.contains(key)){
            r.readLine()
        }

    }

    void set(String key, String value){

    }

    void delete(String key){

    }
}