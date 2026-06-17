import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.*;
import java.util.*;


public class Engine {

 public static void main(String[] args){
    Record r = new Record(32, "Hello");
    Record r1 = new Record(40, "jon");
    Record r2 = new Record(25, "sansa");
    Record r3 = new Record(100, "rob");

   
   Mem m = new Mem();
   m.setKeyValue(r);
   m.setKeyValue(r1);
   m.setKeyValue(r2);
   m.setKeyValue(r3);

   System.out.println(m.getValue(r3));

   ArrayList<Record> k = m.flush();

   System.out.println("flushed values: ");
   for (Record rec : k){
      System.out.println("Key: "+ rec.key + "value: "+ rec.value);
   }
    
 }
}





