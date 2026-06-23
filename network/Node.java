import java.util.*;


class Packet {
    int sender;
    int seqNum;
    String ipAddress;
    int port;

    Packet(int sender, int seqNum, String ipAddress, int port){
        this.sender = sender;
        this.seqNum = seqNum;
        this.ipAddress = ipAddress;
        this.port = port;
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
        
        Node n = new Node(65, "127.0.0.1", 9090);
        n.addSelf();
        System.out.println(n.memshipList.get(0).ipAddress);

        TCPClient c = new TCPClient();

        ServerRunner runnable = new ServerRunner(9090);
        Thread thread = new Thread(runnable);
        thread.start();

        c.connect("localhost", 9090);

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