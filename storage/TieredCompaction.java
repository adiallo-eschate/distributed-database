import java.io.File;
import java.nio.*;
import java.util.*;




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



