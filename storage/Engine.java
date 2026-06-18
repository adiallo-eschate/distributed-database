import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.*;
import java.util.*;
import java.nio.charset.StandardCharsets;



public class Engine {

 public static void main(String[] args){
    Record r = new Record(32, "Hello");
    Record r1 = new Record(40, "jon");
    Record r2 = new Record(25, "sansa");
    Record r3 = new Record(100, "rob");

    ArrayList<Record> arr = new ArrayList<>(Arrays.asList(r,r1,r2,r3));

   SSTable s = new SSTable(arr);

   s.writeDataBlock(arr);

  /* Serializer e = new Serializer();
   byte[] encoded = e.serializeRecord(r);
   Record decoded = e.deserializeRecord(encoded);*/


   
 /*  Mem m = new Mem();
   m.setKeyValue(r);
   m.setKeyValue(r1);
   m.setKeyValue(r2);
   m.setKeyValue(r3);

   System.out.println(m.getValue(r3));

   ArrayList<Record> k = m.flush();

   System.out.println("flushed values: ");
   for (Record rec : k){
      System.out.println("Key: "+ rec.key + "value: "+ rec.value);
   }*/
    
 }
}



 class SSTable {

       byte[] sstable;
       ArrayList<Record> records;
       Serializer s;


      SSTable(ArrayList<Record> records){
         this.records = records;
         this.s = new Serializer();
      }


      byte[] writeDataBlock(ArrayList<Record> records){
         
         int totalLength = 0;
         for (Record r : records){
            totalLength += 2;   // 2 bytes for key's length
            totalLength += 2;    // and 2 bytes for value's length
            totalLength += r.value.length();
         }

         byte[] globalBytes = new byte[totalLength];

         int i = 0;

         for (Record rec : records){

            byte[] localBytes = this.s.serializeRecord(rec);

            for (byte b : localBytes) globalBytes[i++] = b;
         }

         System.out.println(Arrays.toString(globalBytes));
         return globalBytes;
      }

      void writeIndexBlock(){

      }

      void writeBloomFilter(){

      }

      void writeFooter(){

      }

      void write(){

      }
      void flushToDisk(){

      }

}

class Serializer {


      byte[] serializeRecord(Record record){

         int key = record.key;
         String value = record.value;

         byte[] valueBytes = value.getBytes();
         int valueLength = value.length();

         int totalLength = 2 + 2 + valueLength; 

         byte[] entry = new byte[totalLength];

         int i = 0;

         entry[i++] = (byte)(key >>> 8);
         entry[i++] = (byte)(key);

         entry[i++] = (byte)(valueLength >>> 8);
         entry[i++] = (byte)(valueLength);

         for (byte b : valueBytes) entry[i++] = b;

         System.out.println(Arrays.toString(entry));

         return entry;
      }

      Record deserializeRecord(byte[] recordBytes){

         int i = 0;

         int key = 
                  ((recordBytes[i++] & 0xFF) << 8) | 
                  ((recordBytes[i++] & 0xFF));

         int valueLength = ((recordBytes[i++] & 0xFF) << 8) |
                           ((recordBytes[i++] & 0xFF));
         
         String value = new String(recordBytes, i, valueLength, StandardCharsets.UTF_8);


         Record r = new Record(key, value);

         System.out.println("Decoded Record: " + r.key + " " + r.value);


         return r;
      }

}


