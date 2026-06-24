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

    byte[] encodeDiscover(Packet p){
        
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

    byte[] encodeJoin(Packet p){
        
        int totalLength = 20 + p.ipAddress.length();

        for (Node node : p.members){
            if (node.uniqueId == p.sender){
                continue;
            }

            byte[] bi = node.ipAddress.getBytes();
            totalLength += 4;  // sender id
            totalLength += 4;   // sender ip length;
            totalLength += bi.length;  
        }

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

        for (Node node : p.members){
            for (byte t : encodeInt(node.uniqueId)) temp[i++] = t;

            byte[] v = node.ipAddress.getBytes();
            for (byte q : encodeInt(v.length)) temp[i++] = q;
            for (byte r : v) temp[i++] = r;
            for (byte s : encodeInt(node.portNumber)) temp[i++] = s;
        }

        return temp;
    }

    byte[] encodeJoinAck(Packet p){
        return encodeDiscover(p);
    }

    byte[] encodeHeartbeat(Packet p){
        
        byte[] heartbeatStr = p.heartBeat.getBytes();
        int totalLength = 20 + p.ipAddress.length();
        totalLength += heartbeatStr.length;

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

        for (byte w : heartbeatStr) temp[i++] = w;

        return temp;
    }

    byte[] encodeRPCExecutor(Packet p){
        
        int totalLength = 20 + p.ipAddress.length();
        totalLength += 4;       // for procedure number;
        totalLength += 4;       // encode args length;
        totalLength += p.args.length;
        
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

        for (byte t : encodeInt(p.procedure)) temp[i++] = t;
        for (byte a : encodeInt(p.args.length)) temp[i++] = a;
        for (byte s : p.args) temp[i++] = s;

        return temp;
    }

    byte[] encodeRPCResults(Packet p){
        
        int totalLength = 20 + p.ipAddress.length();
        totalLength += 4;       // bytes for results length
        totalLength += p.results.length;
        
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

        for (byte n : encodeInt(p.results.length)) temp[i++] = n;
        for (byte t : p.results) temp[i++] = t;

        return temp;
    }

    byte[] packetToBytes(Packet p){

        switch(p.messageType){
            case DISCOVER:
                return encodeDiscover(p);
            case JOIN:
                return encodeJoin(p);
            case JOIN_ACK:
                return encodeJoinAck(p);
            case HEARTBEAT:
                return encodeHeartbeat(p);
            case RPCEXECUTOR:
                return encodeRPCExecutor(p);
            case RPCRESULTS:
                return encodeRPCResults(p);
        }

        return null;
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