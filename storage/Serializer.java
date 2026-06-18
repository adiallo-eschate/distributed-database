


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


