import java.io.*;
import java.net.*;
import java.util.*;



public class TCPClient {

    
    byte[] encodeInt(int num){  
        
        byte[] temp = new byte[4];

        int i = 0;
        temp[i++] = (byte)(num >> 24);
        temp[i++] = (byte)(num >> 16);
        temp[i++] = (byte)(num >> 8);
        temp[i++] = (byte)(num);

        return temp;
    }

    byte[] packetToBytes(Packet p){

        int totalLength = 4 + 4 + 4 + 4 + 4 + p.ipAddress.length();
        int pads = (4 - totalLength % 4) % 4;



        byte[] temp = new byte[totalLength + pads];
        int i = 0;

        for(byte h : encodeInt(p.messageType.ordinal())) temp[i++] = h; 
        for (byte b : encodeInt(p.sender)) temp[i++] = b;
        
        int networkStrLength = p.ipAddress.length();
        byte[] networkStrBytes = p.ipAddress.getBytes();

        for (byte f : encodeInt(networkStrBytes.length)) temp[i++] = f;

        for (byte d : networkStrBytes) temp[i++] = d;

        for (byte c : encodeInt(p.port)) temp[i++] = c;
        
        for (byte e : encodeInt(p.seqNum)) temp[i++] = e;


        return temp;
    }

    void connect(String dstIpAddress, int dstPort){

        try(Socket socket = new Socket(dstIpAddress, dstPort)){

            OutputStream out = socket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Packet p = new Packet(65, "localhost", 9090, 123, MessageType.DISCOVER);

            byte[] bytes = packetToBytes(p);
            System.out.println("In Client: " + Arrays.toString(bytes));


            out.write(bytes);
            out.flush();
            socket.shutdownOutput();


            String response = in.readLine();
            System.out.println("Server says: " + response);

            socket.close();

        } catch (IOException e){
            e.printStackTrace();
        }
    }



    public static void main(String[] args) throws IOException {

    Socket socket = new Socket("localhost", 9090);

    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    out.println("Hello from client!");

    String response = in.readLine();
    System.out.println("Server says: " + response);

    socket.close();
}
}