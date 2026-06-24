import java.io.*;
import java.net.*;
import java.util.*;


public class TCPServer {


    int decodeInt(byte[] num){
        
        int i = 0;
        int decodedNum = ((num[i++] & 0xFF) << 24) |
                         ((num[i++] & 0xFF) << 16) |
                         ((num[i++] & 0xFF) << 8)  |
                         ((num[i++] & 0xFF));

        return decodedNum;
    }
    
    
    Packet bytesToPacket(byte[] data){

        int i = 0;
        int messageType = decodeInt(Arrays.copyOfRange(data, i, 4));
        i += 4;
        int sender = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        //System.out.println(sender);
        int networkStrLength = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        //System.out.println(networkStrLength);
        String network = new String(data, i, networkStrLength);
        i += networkStrLength;
        //System.out.println(network);
        int port = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        //System.out.println(port);
        int seqNum = decodeInt(Arrays.copyOfRange(data, i, i + 4));

        Packet p = null;

        switch(messageType){
            case 0:
                MessageType m = MessageType.DISCOVER;
                p = new Packet(sender, network, port, seqNum, m);
            
                break;
            case 1:
                break;
            
            case 2:
                break;

            case 3:
                break;

            case 4:
                break;

            case 5:
                break;
            
        }


        return p;
    }



    Packet open(int localPort){

        Packet packet = null;
        
       try(ServerSocket serverSocket = new ServerSocket(localPort)){
            System.out.println("Server is running and waiting for client connection...");
    
            try(Socket clientSocket = serverSocket.accept()){
                System.out.println("Client connected!");
                InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                    System.out.println("Server received " + bytesRead + " bytes");
                }

                byte[] data = baos.toByteArray();
                Packet p = bytesToPacket(data);
/*global var*/  packet = p;
                
                System.out.println("Data received: " + Arrays.toString(data));
                System.out.println("Decoded: " + p.sender + " " + p.ipAddress + " " + p.port + " " + p.messageType);


                out.write("Message received by the server.\n".getBytes());
                out.flush();
    
                clientSocket.close();
            }
            
            serverSocket.close();
       } catch (IOException e){
           e.printStackTrace();
       }

        return packet;
    }


   public static void main(String[] args) throws IOException {

       try(ServerSocket serverSocket = new ServerSocket(9090)){
            System.out.println("Server is running and waiting for client connection...");
    
            try(Socket clientSocket = serverSocket.accept()){
                System.out.println("Client connected!");
    
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
    
                String message = in.readLine();
                System.out.println("Client says: " + message);
    
                out.println("Message received by the server.");
    
                clientSocket.close();
            }

            serverSocket.close();
       } catch (IOException e){
           e.printStackTrace();
       }
   }
}