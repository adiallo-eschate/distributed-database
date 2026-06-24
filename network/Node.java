import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;



enum MessageType {
    DISCOVER,
    JOIN,
    JOIN_ACK,
    HEARTBEAT,
    RPCEXECUTOR,
    RPCRESULTS
}


class Packet {
    int sender;
    int seqNum;
    String ipAddress = null;
    int port;
    MessageType messageType;
    ArrayList<Node> members = null;
    String ack = null;
    String heartBeat = null;
    int procedure;
    byte[] args = null;
    byte[] results = null;


    // discover
    Packet(int sender, String ipAddress, int port, int seqNum, MessageType messageType){
        this.sender = sender;
        this.ipAddress = ipAddress;
        this.port = port;
        this.seqNum = seqNum;
        this.messageType = messageType;
    }

    // join
    Packet(int sender, String ipAddress, int port, int seqNum, 
    MessageType messageType, ArrayList<Node> members){
        this.sender = sender;
        this.ipAddress = ipAddress;
        this.port = port;
        this.seqNum = seqNum;
        this.messageType = messageType;
        this.members = members;
    }

    // join_ack
    /*Packet(int sender, String ipAddress, int port, int seqNum, 
    MessageType messageType, String ack){
        this.sender = sender;
        this.ipAddress = ipAddress;
        this.port = port;
        this.seqNum = seqNum;
        this.messageType = messageType;
        this.ack = ack;
    }*/

    // heartbeat
    Packet(int sender, String ipAddress, int port, int seqNum, 
    MessageType messageType, String heartBeat){
        this.sender = sender;
        this.ipAddress = ipAddress;
        this.port = port;
        this.seqNum = seqNum;
        this.messageType = messageType;
        
        if (!(heartBeat.equalsIgnoreCase("heartbeat"))){
            System.out.println("Heartbeat variable must be 'heartbeat'");
            System.exit(0);
        }
        this.heartBeat = heartBeat.toLowerCase();
    }

    // rpc sender
    Packet(int sender, String ipAddress, int port, int seqNum, 
    MessageType messageType, int procedure, byte[] args){
        this.sender = sender;
        this.ipAddress = ipAddress;
        this.port = port;
        this.seqNum = seqNum;
        this.messageType = messageType;
        this.procedure = procedure;
        this.args = args;
    }

    // rpc reply
    Packet(int sender, String ipAddress, int port, int seqNum, 
    MessageType messageType, byte[] results){
        this.sender = sender;
        this.ipAddress = ipAddress;
        this.port = port;
        this.seqNum = seqNum;
        this.messageType = messageType;
    }

}




public class Node {

    int uniqueId;
    ArrayList<Node> memshipList;
    String ipAddress;
    int portNumber;
    boolean isCoordinator;
    boolean isCoordinatorCanidate;
    Node seed_peer;
    TCPClient client;
    TCPServer server;   


    Node(int uniqueId, String ipAddress, int portNumber){
        
        this.uniqueId = uniqueId;
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.memshipList = new ArrayList<>();
        this.isCoordinator = false;
        this.isCoordinatorCanidate = false;
        this.seed_peer = null;

        // discover seedPeer


    }
/*
    void discover(Node sender){

        Packet info = new Packet();


        TCPClient c = new TCPClient();
        TCPServer s = this.server;

        c.open(this.ipAddress, this.portNumber, this.seed_peer.ipAddress, this.seed_peer.portNumber);


        c.encode(sender, memshipList);
        Packet p  = c.send();

    }

    void joinAcknowledge(){

    }

    void heartBeat(){

    }*/



    void addSelf(){
        
        Node n = new Node(this.uniqueId, this.ipAddress, this.portNumber);
        n.memshipList = this.memshipList;
        n.isCoordinator = this.isCoordinator;
        n.isCoordinatorCanidate = this.isCoordinatorCanidate;
        n.seed_peer = this.seed_peer;
        this.memshipList.add(n);

        return;
    }



    public static void main(String[] args){
        System.out.println("Node!");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        Node n = new Node(65, "127.0.0.1", 9090);
        n.addSelf();
        System.out.println(n.memshipList.get(0).ipAddress);

        TCPClient c = new TCPClient();

        Future<Packet> future = executor.submit(() -> {
            
            int port = n.portNumber;

            System.out.println("Thread is Running Successfully" + port);
            TCPServer s = new TCPServer();
            return s.open(port);

        });


        /*ServerRunner runnable = new ServerRunner(9090);
        Thread thread = new Thread(runnable);
        thread.start();*/
        
        c.connect("localhost", 9090);

        Packet pac = null;

        try {
            pac = future.get();
            executor.shutdown();
        } catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
        }

       switch(pac.messageType){
            case DISCOVER:
                System.out.println("Discover Packet");
                // callJoin();
                break;
            case JOIN:
                System.out.println("Join Packet");
                // callJoin_ack()
                break;
            case JOIN_ACK:
                System.out.println("Join_Ack Packet");

                break;
            case HEARTBEAT:
                System.out.println("Heartbeat Packet");
                // callheartBeat();
                break;
            case RPCEXECUTOR:
                System.out.println("RPC Packet");
                // callExecuteProgram
                break;
            case RPCRESULTS:
                System.out.println("RPC results");
                break;
       }
        

    }
}



class ServerRunner implements Runnable {

    int serverPort;

    ServerRunner(int serverPort){
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        System.out.println("Thread is Running Successfully" + this.serverPort);
        TCPServer s = new TCPServer();
        s.open(this.serverPort);

    }

}