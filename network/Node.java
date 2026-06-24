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
    MEMBERSHIP_UPDATE,
    RPC
}


class Packet {
    int sender;
    int seqNum;
    String ipAddress;
    int port;
    MessageType messageType;
    /**
     * int program;
     * int procedure;
     */

    Packet(int sender, String ipAddress, int port, int seqNum, MessageType messageType){
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

        System.out.println("We Did It: "+ pac.messageType);
        System.out.print(" " + pac.sender);
        System.out.print(" "+ pac.ipAddress);
        System.out.print(" "+ pac.port);
        

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