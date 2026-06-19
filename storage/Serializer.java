



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

         Record r = new Record(key, value, opType);

         System.out.println("Decoded Record: " + r.key + " " + r.value + " op: " + r.opType);


         return r;
      }

}


