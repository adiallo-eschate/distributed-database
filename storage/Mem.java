import java.util.*;



class Mem {

      TreeMap<Integer, String> map;

      Mem(){
         this.map = new TreeMap<>();
      }

      void setKeyValue(Record r){
         int key = r.key;
         String value = r.value;

            map.put(key,value);
      }

      String getValue(Record r){
         
         int key = r.key;
         
          return map.get(key);
      }

      ArrayList<Record> flush(){
            
         ArrayList<Record> records = new ArrayList<>();

            for (int key : map.keySet()){
               Record r = new Record(key, map.get(key));
               records.add(r);
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

