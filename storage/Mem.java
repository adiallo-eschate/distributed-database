import java.util.*;



class Mem {

      TreeMap<Record, Integer> map;

      Mem(){
         this.map = new TreeMap<>();
      }

      void setKeyValue(Record r){

            map.put(r,r.opType);

      }

      Record getValue(int key){

         for (Record rec : map.keySet()){

            if ((rec.key == key) && (map.get(rec) != Record.DELETE)){
              
               return rec;
            
            }

         }

         return null;
      }

      ArrayList<Record> flush(){
            
         ArrayList<Record> records = new ArrayList<>();

            for (Record rec : map.keySet()){
                  
                  records.add(rec);

            }

         clear();
         System.out.println("Memtable Flushed");

         return records;
      }

      private void clear(){
         map.clear();
         System.out.println("Map cleared ");
      }

}

