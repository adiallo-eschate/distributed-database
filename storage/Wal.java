import java.io.*;
import java.util.*;


class WAL {

      ArrayList<String> walArr;

      WAL(){
         this.walArr = new ArrayList<>();

      }

      void addRecord(String command, Record r){
            
         command.toLowerCase();

         String walRecord = "";
         walRecord += command + ",";
         walRecord += String.valueOf(r.key) + ",";
         walRecord += r.value;
         
         this.walArr.add(walRecord);
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