import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;



class Serializer {
   
ArrayList<byte[]> serialize(ArrayList<Integer> keyData, ArrayList<String> valueData) {

    ArrayList<byte[]> kvList = new ArrayList<>();

    for (int i = 0; i < keyData.size(); i++) {

        int key = keyData.get(i);
        byte[] value = valueData.get(i).getBytes(); 
        int valueLength = value.length;

        int keyLength = 4;

        int total = 4 + keyLength + 4 + valueLength;
        byte[] entry = new byte[total];

        int pos = 0;

        entry[pos++] = (byte) (keyLength >>> 24);
        entry[pos++] = (byte) (keyLength >>> 16);
        entry[pos++] = (byte) (keyLength >>> 8);
        entry[pos++] = (byte) (keyLength);

        entry[pos++] = (byte) (key >>> 24);
        entry[pos++] = (byte) (key >>> 16);
        entry[pos++] = (byte) (key >>> 8);
        entry[pos++] = (byte) (key);

        entry[pos++] = (byte) (valueLength >>> 24);
        entry[pos++] = (byte) (valueLength >>> 16);
        entry[pos++] = (byte) (valueLength >>> 8);
        entry[pos++] = (byte) (valueLength);

        for (byte b : value) entry[pos++] = b;

        kvList.add(entry);
    }

    return kvList;
}



    ArrayList<String> deserialize(ArrayList<byte[]> list) {
        ArrayList<String> decoded = new ArrayList<>();

        for (byte[] entry : list) {

            int pos = 0;

            int keyLength =
                ((entry[pos++] & 0xFF) << 24) |
                ((entry[pos++] & 0xFF) << 16) |
                ((entry[pos++] & 0xFF) << 8)  |
                (entry[pos++] & 0xFF);

            int key =
                ((entry[pos++] & 0xFF) << 24) |
                ((entry[pos++] & 0xFF) << 16) |
                ((entry[pos++] & 0xFF) << 8)  |
                (entry[pos++] & 0xFF);

            int valueLength =
                ((entry[pos++] & 0xFF) << 24) |
                ((entry[pos++] & 0xFF) << 16) |
                ((entry[pos++] & 0xFF) << 8)  |
                (entry[pos++] & 0xFF);

            String value = new String(entry, pos, valueLength, StandardCharsets.UTF_8);

            decoded.add(key + ":" + value);
        }

        return decoded;
    }


    String deserializeOne(byte[] entry) {

        int pos = 0;

        int keyLength =
            ((entry[pos++] & 0xFF) << 24) |
            ((entry[pos++] & 0xFF) << 16) |
            ((entry[pos++] & 0xFF) << 8)  |
            (entry[pos++] & 0xFF);

        int key =
            ((entry[pos++] & 0xFF) << 24) |
            ((entry[pos++] & 0xFF) << 16) |
            ((entry[pos++] & 0xFF) << 8)  |
            (entry[pos++] & 0xFF);

        int valueLength =
            ((entry[pos++] & 0xFF) << 24) |
            ((entry[pos++] & 0xFF) << 16) |
            ((entry[pos++] & 0xFF) << 8)  |
            (entry[pos++] & 0xFF);

        String value = new String(entry, pos, valueLength, StandardCharsets.UTF_8);

        return key + ":" + value;
    }



    byte[] serializeOne(int key, String value) {

        byte[] keyBytes = new byte[4];
        keyBytes[0] = (byte)(key >>> 24);
        keyBytes[1] = (byte)(key >>> 16);
        keyBytes[2] = (byte)(key >>> 8);
        keyBytes[3] = (byte)(key);

        int keyLength = 4;

        byte[] valueBytes = value.getBytes();
        int valueLength = valueBytes.length;

        int total = 4 + keyLength + 4 + valueLength;
        byte[] out = new byte[total];

        int pos = 0;

        out[pos++] = (byte)(keyLength >>> 24);
        out[pos++] = (byte)(keyLength >>> 16);
        out[pos++] = (byte)(keyLength >>> 8);
        out[pos++] = (byte)(keyLength);

        out[pos++] = keyBytes[0];
        out[pos++] = keyBytes[1];
        out[pos++] = keyBytes[2];
        out[pos++] = keyBytes[3];

        out[pos++] = (byte)(valueLength >>> 24);
        out[pos++] = (byte)(valueLength >>> 16);
        out[pos++] = (byte)(valueLength >>> 8);
        out[pos++] = (byte)(valueLength);

        for (byte b : valueBytes) out[pos++] = b;

        return out;
    }


}



public class CreateSSTable {

    public static int fileNum = 1;

    CreateSSTable(TreeMap<Integer, String> memtable, Integer key, String value){
        Serializer s = new Serializer();

        try{

            String dirName = "level0/tmp" + this.fileNum;
            String fileName = "tmp" + this.fileNum + ".sst";
        
            Path dirPath = Paths.get(dirName);
            Path filePath = dirPath.resolve(fileName);

            if (Files.notExists(dirPath)){
                Files.createDirectories(dirPath);
                System.out.println("Dir created: " + dirPath.toAbsolutePath());
            }

            if (Files.notExists(filePath)){
                Files.createFile(filePath);
                System.out.println("File Created: " + filePath.toAbsolutePath());
            }

            
            String dirNameIdx = "level0/tmp" + this.fileNum;
            String fileNameIdx = "tmp" + this.fileNum + ".idx";
        
            Path dirPathIdx = Paths.get(dirNameIdx);
            Path filePathIdx = dirPath.resolve(fileNameIdx);

            if (Files.notExists(dirPathIdx)){
                Files.createDirectories(dirPathIdx);
                System.out.println("Dir created: " + dirPathIdx.toAbsolutePath());
            }

            if (Files.notExists(filePathIdx)){
                Files.createFile(filePathIdx);
                System.out.println("File Created: " + filePathIdx.toAbsolutePath());
            }

            Path pathData = Paths.get(dirName + "/" + fileName);            
            Path pathIdx = Paths.get(dirNameIdx + "/" + fileNameIdx);

            this.fileNum++;

            FileChannel chD = FileChannel.open(pathData, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            FileChannel chIdx = FileChannel.open(pathIdx, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            for (Integer k : memtable.keySet()){
                writeEntryToData(chD, k, memtable.get(k));
                writeEntryToIndex(chIdx, k);
            }

        } catch(IOException e){
            e.printStackTrace();
        }

    }

    void writeEntryToData(FileChannel chData, int key, String value){
        Serializer s = new Serializer();
        try{ 

            byte[] o = s.serializeOne(key, value);
            ByteBuffer bd = ByteBuffer.wrap(o);
        
            while(bd.hasRemaining()){
                    chData.write(bd);
            }

            System.out.println("Wrote to sst table: " + Arrays.toString(o));
        
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    void writeEntryToIndex(FileChannel ch, int key){

        try{
                        
            byte[] entry = convertToIndexEntry(key,(long) ch.position());
            ByteBuffer bb = ByteBuffer.wrap(entry);

            while(bb.hasRemaining()){
                ch.write(bb);
            }

            ch.position((long)entry.length);
            
            //for (byte b : entry){
            //    System.out.print(b & 0xFF);
            //}
            
            System.out.println("Wrote to index file: " + Arrays.toString(entry));

        } catch (IOException e){
            e.printStackTrace();
        }      

    }

    byte[] convertToIndexEntry( int key, long position){
        
        byte[] keyBytes = String.valueOf(key).getBytes();
        int keyLength = keyBytes.length;


        byte[] entry = new byte[16];

        int i = 0;

        entry[i++] = (byte) (keyLength >>> 24);
        entry[i++] = (byte) (keyLength >>> 16);
        entry[i++] = (byte) (keyLength >>> 8);
        entry[i++] = (byte) (keyLength);

        entry[i++] = (byte) (key >>> 24);
        entry[i++] = (byte) (key >>> 16);
        entry[i++] = (byte) (key >>> 8);
        entry[i++] = (byte) (key);

        entry[i++] = (byte) (position >>> 56);
        entry[i++] = (byte) (position >>> 48);
        entry[i++] = (byte) (position >>> 40);
        entry[i++] = (byte) (position >>> 32);
        entry[i++] = (byte) (position >>> 24);
        entry[i++] = (byte) (position >>> 16);
        entry[i++] = (byte) (position >>> 8);
        entry[i++] = (byte) (position);

        return entry;
    }



    public static void main(String[] args){
        
        Serializer s = new Serializer();

        ArrayList<Integer> keyData = new ArrayList<>(Arrays.asList(1000,2,3,4,5));
        ArrayList<String> valueData = new ArrayList<>(Arrays.asList("rob", "sansa", "jon", "arya", "rickon"));
        TreeMap<Integer, String> test = new TreeMap<>();
        test.put(1000, "rob");
        test.put(200, "sansa");
        test.put(250, "jon");   

        ArrayList<byte[]> raw = s.serialize(keyData, valueData);
        s.deserialize(raw);

        new CreateSSTable(test, 10, "jon snow");
        
        System.out.println(test.keySet());
        System.out.println(test.values());
    }
}