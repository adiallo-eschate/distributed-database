import java.io.*;
import java.util.*;


class WAL {

      ArrayList<String> walArr;

      WAL(){
         this.walArr = new ArrayList<>();

      }

      void addRecord(Record r){

         if ((r.opType > 2) || (r.opType < 0)){
            System.out.println("WAL: Invalid Record");
            return;
         }
            
         String walRecord = "";

         if (r.opType == Record.DELETE){

            walRecord = "delete," + r.key;
            walArr.add(walRecord);
            return;

         } else if (r.opType == Record.PUT){

            walRecord = "put," + r.key + "," + r.value;
            walArr.add(walRecord);
         } else {

            walRecord = "get," + r.key + "," + r.value;
            walArr.add(walRecord);
         }
      
         System.out.println("Add command to WAL: " + walRecord);
      }

      ArrayList<String> replay(){

         ArrayList<String> walArr = new ArrayList<>();
         
          try(BufferedReader br = new BufferedReader(new FileReader("WAL.txt"))){

            String line;

            while((line = br.readLine()) != null){
               walArr.add(line);
               System.out.println("Read line from WAL:" + line);
            }


          } catch (IOException e){
            e.printStackTrace();
          }
          
          return walArr;
      }

      void flush(){

         String fileName = "WAL.txt";
         
         try(BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true))){

            for(String record : walArr){
               bw.write(record);
               bw.write("\n");
            }

            this.walArr.clear();

         } catch (IOException e){
            e.printStackTrace();
         }
         
      }

      void clear(){
         
         try(BufferedWriter bw = new BufferedWriter(new FileWriter("WAL.txt"))){
               bw.write("");
         } catch (IOException e){
            e.printStackTrace();
         }
      }

}