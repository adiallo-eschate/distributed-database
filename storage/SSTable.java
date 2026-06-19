import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;



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

         System.out.println("Data block bytes: "+ Arrays.toString(globalBytes));
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

         System.out.println("Index bytes: " + Arrays.toString(globalBytes));
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

         System.out.println(Arrays.toString(bits));

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

         System.out.println("Footer Bytes: " + Arrays.toString(globalBytes));
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

      
         System.out.println("SSTable in bytes: " + Arrays.toString(this.sstable));
      }

      void flushToDisk(int fileCounter){

            if (this.sstable == null){
               System.out.println("sstable is empty. Call write() first");
               return;
            }

            try {
                  String dirName = "level0";
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


