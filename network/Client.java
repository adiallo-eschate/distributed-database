import java.io.*;
import java.net.*;
import java.util.*;



class Client {

    int nodeId;
    int seqNum = 0;

    ArrayList<Node1> members = null;

    //RaftNode raft;


    Client(int nodeId, ArrayList<Node1> members /**, RaftNode raft */) {
        this.nodeId = nodeId;
        this.members = members;
    }


    String discover(Message msgType, int nodeId, ArrayList<Node1> members) {

        Packet p = new Packet(msgType, nodeId, seqNum++, members);

        return Packet.stringPacket(p);
    }


    void connect(String packet, String dstAddress, int dstPort) {

        try (Socket socket = new Socket(dstAddress, dstPort)) {

            socket.setSoTimeout(3000);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            out.println(packet);

            String response = in.readLine();

            System.out.println("Server said: " + response);

            /**
             * if (response != null && response.startsWith("VOTE GRANTED")) {

                        raft.handleVoteResponse(
                                raft.currentTerm,
                                dstPort,  
                                1
                        );
                    }
                    
                    if (response != null && response.startsWith("VOTE DENIED")) {
                    
                        raft.handleVoteResponse(
                                raft.currentTerm,
                                dstPort,
                                0
                        );
                    }
             */


        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}