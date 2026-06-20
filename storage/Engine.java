import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.channels.*;
import java.nio.file.*;
import java.io.File;

// remember to check deserializeRecord()

public class Engine {

      WAL wal;
      Mem memtable;
      Hash hash;
      Serializer s;
      TieredCompaction compactor;



   Engine(WAL wal, Mem memtable){
      this.wal = new WAL();
      this.memtable = new Mem();
      this.s = new Serializer();
      this.compactor = new TieredCompaction();
      this.hash = new Hash();
   }


   Record searchTables(Path dirPath, int key){
         // load sstables and check blooms
      Path path = dirPath;
      
      if (Files.notExists(path)){
         return null;
      }

      try(DirectoryStream<Path> stream = Files.newDirectoryStream(path)){
         
         for(Path entry : stream){

            byte[] fileBytes = Files.readAllBytes(entry);

            int i = 0;
            for (byte b : fileBytes){
               if (b == (byte)('\\')){
                  break;
               }
               i++;
            }

            int j = 0;
            for (byte c : fileBytes){
               if ((c == (byte)('\\')) && (j != i)){
                  break;
               }
               j++;
            }

            int k = 0;
            for (byte d : fileBytes){
               if ((d == (byte)('\\')) && (k != i) && (k != j)){
                  break;
               }
               k++;
            }

            //System.out.println("Indices: " + i + " " + j + " " + k);

            //System.out.println(Arrays.toString(fileBytes));
            byte[] data = Arrays.copyOf(fileBytes, i);
            //System.out.println("searchTables: data - " + Arrays.toString(data));

            byte[] index = Arrays.copyOfRange(fileBytes, i + 1, j);
            //System.out.println("searchTables: index - " + Arrays.toString(index));

            byte[] bloom = Arrays.copyOfRange(fileBytes, j + 1, k);
            //System.out.println("searchTables: bloom - " + Arrays.toString(bloom));

            byte[] footer = Arrays.copyOfRange(fileBytes, k + 1, fileBytes.length);
            //System.out.println("searchTables: footer - " + Arrays.toString(footer));

         
            // search blooms
            int index1 = hash.hash1(key, bloom.length);
            int index2 = hash.hash2(key, bloom.length);
            int index3 = hash.hash3(key, bloom.length);

            if ((bloom[index1] == 1) && (bloom[index2] == 1) && (bloom[index3] == 1)){
               System.out.println("Bloom Success");
               ArrayList<Record> recs  = readDataBlock(data);

               for (Record r : recs){
                  if (r.key == key){
                     System.out.println("Key Found");
                     return r;
                  }
               }

            } else {
               System.out.println("Bloom Failed");
               return null;
            }
         
         }

      } catch (IOException e){
         e.printStackTrace();
      }

      return null;
   }

   Record read(int key){
      
      Mem mem = this.memtable;
      WAL wal = this.wal;

      Record record = mem.getValue(key);
      if (record != null) return record;

      Path path = Paths.get("level0");
      Record found = searchTables(path, key);

      if (found != null){
         Record rec = new Record(found.key, found.value, -1);
         wal.addRecord(rec);
         return found;
      } else {
         Path p = Paths.get("level1");
         Record f = searchTables(p, key);

         if (found != null){
            Record re = new Record(f.key, f.value, -1);
            wal.addRecord(re);
            return f;
         }
      }

      return null;
   }

   int write(int key, String value){

      if ((key < 0) || (value == null)){
         return -1;
      }

      Record r = new Record(key,value, Record.PUT);
      this.wal.addRecord(r);
      this.memtable.setKeyValue(r);

      System.out.println("Write Success");

      return 1;
   }

   int delete(int key){

      if (key < 0){
         return -1;
      }

      Record r = new Record(key, "null", Record.DELETE);
      this.wal.addRecord(r);
      this.memtable.setKeyValue(r);

      System.out.println("Delete Success");

      return 1;
   }


   void recovery(){

   }


   ArrayList<Record> readDataBlock(byte[] fileBytes){
      
      ArrayList<Record> records = new ArrayList<>();
         
            int k;
            for (k = 0; k < fileBytes.length; k++){
               if (fileBytes[k] == (byte)('\\')){
                  //System.out.println("End of Datablock: " + (byte)(fileBytes[k]));
                  break;
               }
            }

            byte[] dataBlock = Arrays.copyOf(fileBytes, k);


            //System.out.println("dataBlock: " + Arrays.toString(dataBlock));

            int i = 0;
            int j = 0;
            while (i < dataBlock.length){
               i += 3;  
               int valueLength = ((dataBlock[i++] & 0xFF) << 8) |
                                 ((dataBlock[i]));

               //System.out.println(valueLength);


               byte[] blockEntry = Arrays.copyOfRange(dataBlock, j, i + valueLength + 1);

               i = (i + valueLength + 1);
               j = i;

               //System.out.println("ith positions: " + i);

               //System.out.println("Block entry: " + Arrays.toString(blockEntry));

               Record r = s.deserializeRecord(blockEntry);
               records.add(r);
            }
      
      return records;
   }



 public static void main(String[] args){
    Record r = new Record(32, "Hello", Record.PUT);
    Record r1 = new Record(40, "jon", Record.PUT);
    Record r2 = new Record(25, "sansa", Record.DELETE);
    Record r3 = new Record(100, "rob", Record.PUT);

    ArrayList<Record> arr = new ArrayList<>(Arrays.asList(r,r1,r2,r3));
   
   //for (Record rec : a){
   //   rec.printRecord();
   //}
   TieredCompaction tc = new TieredCompaction();
   Timer timer = new Timer(true);
   TimerTask task = new CompactHelper(tc);

   timer.schedule(task, 43200000, 86400000);



   Mem m = new Mem();
   WAL w = new WAL();

   Engine e = new Engine(w,m);

   Record jon = e.read(40);   
   System.out.println("Returned Record: " + jon.key + " " + jon.value + " " + jon.opType);

   int success = e.write(200, "Ned Stark");
   int s = e.delete(200);

       
 }
}



















/*
class CompactHelper extends TimerTask {

   TieredCompaction compactor;
   public static int i = 0;

   CompactHelper(TieredCompaction compactor){
      this.compactor = compactor;
   }
    
   public void run(){
      System.out.println("Timer ran: " + i++);
      System.out.println("Compact Daemon Running...");
      this.compactor.compact();
    }

}
*/



/*

class TieredCompaction {

   Serializer s;
   int counter = 0;

   TieredCompaction(){
      s = new Serializer();
   }

      
   ArrayList<Record> readDataBlock(byte[] fileBytes){
      
      ArrayList<Record> records = new ArrayList<>();
         
            int k;
            for (k = 0; k < fileBytes.length; k++){
               if (fileBytes[k] == (byte)('\\')){
                  //System.out.println("End of Datablock: " + (byte)(fileBytes[k]));
                  break;
               }
            }

            byte[] dataBlock = Arrays.copyOf(fileBytes, k);


            //System.out.println("dataBlock: " + Arrays.toString(dataBlock));

            int i = 0;
            int j = 0;
            while (i < dataBlock.length){
               i += 3;  
               int valueLength = ((dataBlock[i++] & 0xFF) << 8) |
                                 ((dataBlock[i]));

               //System.out.println(valueLength);


               byte[] blockEntry = Arrays.copyOfRange(dataBlock, j, i + valueLength + 1);

               i = (i + valueLength + 1);
               j = i;

               //System.out.println("ith positions: " + i);

               //System.out.println("Block entry: " + Arrays.toString(blockEntry));

               Record r = s.deserializeRecord(blockEntry);
               records.add(r);
            }
      
      return records;
   }

   ArrayList<Record> removeTombs(ArrayList<Record> records){

      ArrayList<Record> withoutTombs = new ArrayList<>();

      for (Record rec : records){

         if (rec.opType == Record.DELETE){
            continue;
         }

         withoutTombs.add(rec);
      }


      for (Record r : withoutTombs){
         System.out.println("Without tomb: " + r.key + " value:" + r.value);
      }   

      return withoutTombs;
   }

   ArrayList<Record> merge(ArrayList<ArrayList<Record>> records){

      ArrayList<Record> merged = new ArrayList<>();

      for (ArrayList<Record> recs : records){
            for (Record r : recs){
               merged.add(r);
            }
      }
      
      return merged;
   }


   void compact(){

      ArrayList<ArrayList<Record>> arrRecs = new ArrayList<>();

      Path path = Paths.get("level0");

      try(DirectoryStream<Path> stream = Files.newDirectoryStream(path)){
         
         for(Path entry : stream){

            byte[] fileBytes = Files.readAllBytes(entry);
            ArrayList<Record> tmp = readDataBlock(fileBytes);
            ArrayList<Record> tmp1 = removeTombs(tmp);
            arrRecs.add(tmp1);
         }

         ArrayList<Record> newTableRecords = merge(arrRecs);

         // create new sstable in level2
         SSTable sst = new SSTable(newTableRecords);
         this.counter++;
         sst.write();
         sst.flushToDisk(this.counter, 1);

         File file = new File("level0");
         deleteDirectory(file);

         System.out.println("Level0 sstables deleted");

         for(Record rec : newTableRecords){
            System.out.println("Create new sstable here in level2");
         }

      } catch (IOException e){
         e.printStackTrace();
      }
   }

   public static void deleteDirectory(File file) {
      File[] contents = file.listFiles();
         if (contents != null) {
            for (File f : contents) {
               deleteDirectory(f);
            }
         }
         file.delete();
   }
}

*/









/*
 class SSTable {

       byte[] sstable;
       ArrayList<Record> records;
       Serializer s;
       int dataOffset;
       int indexOffset;
       int bloomOffset;
       int footerOffset;


      SSTable(ArrayList<Record> records){
         this.records = records;
         this.s = new Serializer();
      }


      byte[] writeDataBlock(ArrayList<Record> records){
         
         int totalLength = 0;
         for (Record r : records){
            totalLength += 1;
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

         //System.out.println("Data block bytes: "+ Arrays.toString(globalBytes));
         return globalBytes;
      }

      byte[] writeIndexBlock(ArrayList<Record> records, int positionInDataFile){
         
         byte[] globalBytes = new byte[records.size() * 6];
         int pos = 0; 

         for (Record rec : records){
            
            int totalLength = 6;
            byte[] entry = new byte[totalLength];

            int key = rec.key;
            int valueLength = rec.value.length();
            int i = 0;

            entry[i++] = (byte)(key >>> 8);
            entry[i++] = (byte)(key);


            entry[i++] = (byte)(positionInDataFile >>> 24);
            entry[i++] = (byte)(positionInDataFile >>> 16);
            entry[i++] = (byte)(positionInDataFile >>> 8);
            entry[i++] = (byte)(positionInDataFile);

            for (byte b : entry) globalBytes[pos++] = b;

            positionInDataFile += 2 + 2 + valueLength;    //length of a record: 2 bytes for key + 2 bytes for valueLen + valueLen(bytes) 
         }

         //System.out.println("Index bytes: " + Arrays.toString(globalBytes));
         return globalBytes;

      }

      byte[] writeBloomFilter(ArrayList<Record> records){
         
         Hash h = new Hash();
         final int ARRlENGTH = 107;

         byte[] bits = new byte[ARRlENGTH];

         for (Record rec : records){
            
            int key  = rec.key;

            int index1 = h.hash1(key, ARRlENGTH);
            int index2 = h.hash2(key, ARRlENGTH);
            int index3 = h.hash3(key, ARRlENGTH);
            bits[index1] = 1;
            bits[index2] = 1;
            bits[index3] = 1;
         }

         //System.out.println(Arrays.toString(bits));

         return bits;
      }

      byte[] writeFooter(){

         // [data offset(0), index offset, bloom offset, footer offset]

         String data = "data";
         int dataLength = data.length();
         byte[] d = data.getBytes();
         int dOff = this.dataOffset;

         String index = "index";
         int indexLength = index.length();
         byte[] in = index.getBytes();
         int iOff = this.indexOffset;

         String bloom = "bloom";
         int bloomLength = bloom.length();
         byte[] b = bloom.getBytes();
         int bOff = this.bloomOffset;

         String footer = "footer";
         int footerLength = footer.length();
         byte[] f = footer.getBytes();
         int fOff = this.footerOffset;

         // 4 bytes for each block offset + # of bytes for ascii chars
         int totalLength = 16 + dataLength + indexLength + bloomLength + footerLength;
         byte[] globalBytes = new byte[totalLength];

         int i = 0;

         // add data offset
         for (byte dByte : d) globalBytes[i++] = dByte;
   
         globalBytes[i++] = (byte)(dOff >>> 24);
         globalBytes[i++] = (byte)(dOff >>> 16);
         globalBytes[i++] = (byte)(dOff >>> 8);
         globalBytes[i++] = (byte)(dOff);

         // now index offset
         for (byte inByte : in) globalBytes[i++] = inByte;

         globalBytes[i++] = (byte)(iOff >>> 24);
         globalBytes[i++] = (byte)(iOff >>> 16);
         globalBytes[i++] = (byte)(iOff >>> 8);
         globalBytes[i++] = (byte)(iOff);

         // bloom
         for (byte bByte : b) globalBytes[i++] = bByte;

         globalBytes[i++] = (byte)(bOff >>> 24);
         globalBytes[i++] = (byte)(bOff >>> 16);
         globalBytes[i++] = (byte)(bOff >>> 8);
         globalBytes[i++] = (byte)(bOff);


         // footer
         for (byte fByte : f) globalBytes[i++] = fByte;

         globalBytes[i++] = (byte)(fOff >>> 24);
         globalBytes[i++] = (byte)(fOff >>> 16);
         globalBytes[i++] = (byte)(fOff >>> 8);
         globalBytes[i++] = (byte)(fOff);

         //System.out.println("Footer Bytes: " + Arrays.toString(globalBytes));
         return globalBytes;
      }

      void write(){

         // will at "\" between blocks to act as separator for each block

         ArrayList<Record> records = this.records;
         char backslash = '\\';
         byte bs = (byte)backslash;

         byte[] dataBlock = writeDataBlock(records);
         int dataLength = dataBlock.length;

         byte[] indexBlock = writeIndexBlock(records, 0);
         int indexLength = indexBlock.length;

         byte[] bloomBlock = writeBloomFilter(records);
         int bloomLength = bloomBlock.length;

         this.dataOffset = 0;
         this.indexOffset = dataLength;
         this.bloomOffset = indexLength;
         this.footerOffset = bloomLength;
         
         byte[] footerBlock = writeFooter();
         int footerLength = footerBlock.length;

         int totalLength = dataLength + indexLength + bloomLength + footerLength;

         this.sstable = new byte[totalLength + 3]; // 3 bytes for each "\" character
         


         int i = 0;

         for (byte dByte : dataBlock) this.sstable[i++] = dByte;
         this.sstable[i++] = bs;    // separator

         for (byte iByte : indexBlock) this.sstable[i++] = iByte;
         this.sstable[i++] = bs;

         for (byte bByte : bloomBlock) this.sstable[i++] = bByte;
         this.sstable[i++] = bs;

         for (byte fByte : footerBlock) this.sstable[i++] = fByte;

      
         System.out.println("SSTable written. Remember to Flush!");
         //System.out.println("SSTable in bytes: " + Arrays.toString(this.sstable));
      }


      void flushToDisk(int fileCounter, int dirCounter){

            if (this.sstable == null){
               System.out.println("sstable is empty. Call write() first");
               return;
            }

            try {
                  String dirName = "level" + dirCounter;
                  String fileName = "sst-" + fileCounter + ".sst";

                  Path dirPath = Paths.get(dirName);
                  Path filePath = dirPath.resolve(fileName);

                  if (Files.notExists(dirPath)){
                     Files.createDirectories(dirPath);
                     System.out.println("Dir created: " + dirPath.toAbsolutePath());
                  } 

                  if (Files.notExists(filePath)){
                     Files.createFile(filePath);
                     System.out.println("File created: " + filePath.toAbsolutePath());
                  }

                  FileChannel ch = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                  ByteBuffer bb = ByteBuffer.wrap(this.sstable);

                  while(bb.hasRemaining()){
                     ch.write(bb);
                  }

                  System.out.println("SSTable Flushed to Disk!");
            
            } catch (IOException e){
               e.printStackTrace();
            }
      }

}

class Hash {


   int hash1(int key, int arrLen){
            
            int MOD = 1_000_003;

            String keyString = String.valueOf(key);
            int hash = 0;

            for (char c : keyString.toCharArray()){
                  hash = (hash * 31 + c) % MOD;
            }

            return hash % arrLen;
         }

         int hash2(int key, int arrLen){

            int MOD = 104_729;
            
            String keyString = String.valueOf(key);
            int hash = 0;

            for (char c : keyString.toCharArray()){
               hash = (hash * 31 + c) % MOD;
            }

            return hash % arrLen;
         }

         int hash3(int key, int arrLen){

            int MOD = 4_999_967;
            
            String keyString = String.valueOf(key);
            int hash = 0;

            for (char c : keyString.toCharArray()){
                  hash = (hash * 31 + c) % MOD;
            }

            return hash % arrLen;
         }
}




class Serializer {


      byte[] serializeRecord(Record record){

         if ((record.opType < 0) || (record.opType > 2)){
            System.out.println("Serializer: Invalid record");
            return null;
         }

         int key = record.key;
         String value = record.value;
         int opType = record.opType;

         byte[] valueBytes = value.getBytes();
         int valueLength = value.length();

         int totalLength = 1 + 2 + 2 + valueLength; 

         byte[] entry = new byte[totalLength];

         int i = 0;

         entry[i++] = (byte)(opType);

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

         int opType = ((recordBytes[i++] & 0xFF));

         int key = 
                  ((recordBytes[i++] & 0xFF) << 8) | 
                  ((recordBytes[i++] & 0xFF));

         int valueLength = ((recordBytes[i++] & 0xFF) << 8) |
                           ((recordBytes[i++] & 0xFF));
         
         String value = new String(recordBytes, i, valueLength, StandardCharsets.UTF_8);

         // Fixme
         Record r = new Record(key, value, opType);

         System.out.println("Decoded Record: " + r.key + " " + r.value + " op: " + r.opType);


         return r;
      }

}


*/