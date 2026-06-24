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

    Packet decodeDiscover(byte[] data){
        
        int i = 0;
        int messageType = decodeInt(Arrays.copyOfRange(data, i, 4));
        i += 4;
        int sender = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        int networkStrLength = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        String network = new String(data, i, networkStrLength);
        i += networkStrLength;
        int port = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        int seqNum = decodeInt(Arrays.copyOfRange(data, i, i + 4));

        MessageType m = MessageType.DISCOVER;
        Packet p = new Packet(sender, network, port, seqNum, m);

        return p;
    }

    Packet decodeJoin(byte[] data){
        
        ArrayList<Node> members = new ArrayList<>();
        
        int i = 0;
        int messageType = decodeInt(Arrays.copyOfRange(data, i, 4));
        i += 4;
        int sender = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        int networkStrLength = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        String network = new String(data, i, networkStrLength);
        i += networkStrLength;
        int port = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        int seqNum = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;

        int j = i;

        int uniqueId = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        int ipLength = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        String ip = new String(data, i, ipLength);
        i += ipLength;
        int nodePort = decodeInt(Arrays.copyOfRange(data, i, i + 4));

        Node n = new Node(uniqueId, ip, nodePort);
        members.add(n);

        MessageType m = MessageType.JOIN;
        Packet p = new Packet(sender, network, port, seqNum, m, members);

        return p;      
    }

    Packet decodeJoinAck(byte[] data){
        return decodeDiscover(data);
    }

    Packet decodeHeartbeat(byte[] data){
        
        int i = 0;
        int messageType = decodeInt(Arrays.copyOfRange(data, i, 4));
        i += 4;
        int sender = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        int networkStrLength = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        String network = new String(data, i, networkStrLength);
        i += networkStrLength;
        int port = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        int seqNum = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;

        String heartbeatStr = new String(data, i, 9);
        heartbeatStr.toLowerCase();

        MessageType m = MessageType.HEARTBEAT;
        Packet p = new Packet(sender, network, port, seqNum, m, heartbeatStr);

        return p;
    }

    Packet decodeRPCExecutor(byte[] data){
        
        int i = 0;
        int messageType = decodeInt(Arrays.copyOfRange(data, i, 4));
        i += 4;
        int sender = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        int networkStrLength = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        String network = new String(data, i, networkStrLength);
        i += networkStrLength;
        int port = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        int seqNum = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        int procedure = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        int argsLength = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;

        byte[] args = new byte[argsLength];
        for (int j = 0; j < argsLength; j++){
            args[j] = data[i++];
        }


        MessageType m = MessageType.RPCEXECUTOR;
        Packet p = new Packet(sender, network, port, seqNum, m, procedure, args);

        return p;
    } 

    Packet decodeRPCResults(byte[] data){
        
        int i = 0;
        int messageType = decodeInt(Arrays.copyOfRange(data, i, 4));
        i += 4;
        int sender = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        int networkStrLength = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        String network = new String(data, i, networkStrLength);
        i += networkStrLength;
        int port = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        int seqNum = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;
        int resultsLength = decodeInt(Arrays.copyOfRange(data, i, i + 4));
        i += 4;

        byte[] results = new byte[resultsLength];
        for (int j = 0; j < resultsLength; j++){
            results[j] = data[i++];
        }


        MessageType m = MessageType.DISCOVER;
        Packet p = new Packet(sender, network, port, seqNum, m, results);

        return p;

    }
    
    
    Packet bytesToPacket(byte[] data){

        int i = 0;
        int messageType = decodeInt(Arrays.copyOfRange(data, i, 4));


        switch(messageType){
            case 0:
                return decodeDiscover(data);
            case 1:
                return decodeJoin(data);            
            case 2:
                return decodeJoinAck(data);
            case 3:
                return decodeHeartbeat(data);
            case 4:
                return decodeRPCExecutor(data);
            case 5:
                return decodeRPCResults(data);
            default:
                System.out.println("Decoder Error. Check packet parameters");
        }


        return null;
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