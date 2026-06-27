


import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;

enum Message {
    DISCOVER,
    HEARTBEAT,

    RPC,
    RPCRESULTS,

    APPENDENTRIES,
    APPENDENTRIESRESULTS,

    REQUESTVOTE,
    REQUESTVOTERESULTS
}

class Packet {

    int nodeId;
    int seqNum;

    Message msgType;

    ArrayList<Node1> members = null;

    String procedure;
    String argsOrResults;
    String results;

    // raft shit

    int term = 0;
    int leaderId = -1;

    int prevLogIndex = -1;
    int prevLogTerm = -1;

    int commitIndex = -1;

    int voteGranted = 0;


    Packet(Message msgType, int nodeId, int seqNum, ArrayList<Node1> members) {
        this.msgType = msgType;
        this.nodeId = nodeId;
        this.seqNum = seqNum;
        this.members = members;
    }


    Packet(Message msgType) {
        this.msgType = msgType;
    }


    Packet(Message msgType, String procedure, String argsOrResults) {
        this.msgType = msgType;
        this.procedure = procedure;
        this.argsOrResults = argsOrResults;
    }


    Packet(Message msgType, int nodeId, int term, int leaderId,
           int prevLogIndex, int prevLogTerm, int commitIndex) {

        this.msgType = msgType;
        this.nodeId = nodeId;

        this.term = term;
        this.leaderId = leaderId;

        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;

        this.commitIndex = commitIndex;
    }


    Packet(Message msgType, int nodeId, int term, int voteGranted) {
        this.msgType = msgType;
        this.nodeId = nodeId;

        this.term = term;
        this.voteGranted = voteGranted;
    }


    public static String stringPacket(Packet p) {

        String s = "";

        s += p.msgType + ":";
        s += p.nodeId + ":";
        s += p.seqNum + ":";

        s += p.term + ":";
        s += p.leaderId + ":";

        s += p.prevLogIndex + ":";
        s += p.prevLogTerm + ":";

        s += p.commitIndex;

        if (p.members != null) {

            s += "-";

            for (Node1 n : p.members) {

                s += n.nodeId + ":";
                s += n.ipAddress + ":";
                s += n.port + ",";
            }

            s = s.substring(0, s.length() - 1);
        }

        return s;
    }


    public static String stringPacketMsg(Packet p) {
        return "" + p.msgType;
    }


    public static String stringPacketRPC(Packet p) {

        String s = "";

        s += p.msgType + ":";
        s += p.procedure + ":";
        s += p.argsOrResults;

        return s;
    }


    public static Packet parsePacket(String s) {

        String[] str = s.split(":");

        Packet p = new Packet(Message.valueOf(str[0]));

        p.nodeId = Integer.parseInt(str[1]);
        p.seqNum = Integer.parseInt(str[2]);

        p.term = Integer.parseInt(str[3]);
        p.leaderId = Integer.parseInt(str[4]);

        p.prevLogIndex = Integer.parseInt(str[5]);
        p.prevLogTerm = Integer.parseInt(str[6]);

        p.commitIndex = Integer.parseInt(str[7]);

        return p;
    }
}


public class Node1 {

    int nodeId;
    String ipAddress;
    int port;

    int seqNum = 0;

    Client client = null;
    Server server = null;

    ArrayList<Node1> members = null;


    // raft state

    int currentTerm = 0;

    int votedFor = -1;

    int commitIndex = -1;

    int lastApplied = -1;

    int leaderId = -1;

    ArrayList<LogEntry> log = new ArrayList<>();


    Node1(int nodeId, String ipAddress, int port, boolean addNetwork) {

        this.nodeId = nodeId;
        this.port = port;
        this.ipAddress = ipAddress;

        if (addNetwork) {

            this.members = new ArrayList<>();

            this.client = new Client(this.nodeId, this.members);
            this.server = new Server(this.nodeId, this.members);

            this.server.serve(this.port);

            System.out.println("Node " + this.nodeId + " live");
        }
    }


    void addSelfToMembers() {

        Node1 self = new Node1(this.nodeId, this.ipAddress, this.port, false);

        members.add(self);
    }


    public static void main(String[] args) {

        Node1 n = new Node1(65, "localhost", 9090, true);
        n.addSelfToMembers();

        Node1 n1 = new Node1(66, "localhost", 9091, true);
        n1.addSelfToMembers();


        String np = n.client.discover(Message.DISCOVER, n.nodeId, n.members);

        n.client.connect(np, "localhost", 9091);


        Packet votePacket = new Packet(
                Message.REQUESTVOTE,
                n.nodeId,
                n.currentTerm,
                0
        );

        n.client.connect(
                Packet.stringPacket(votePacket),
                "localhost",
                9091
        );
    }
}


class Client {

    int nodeId;
    int seqNum = 0;

    ArrayList<Node1> members = null;


    Client(int nodeId, ArrayList<Node1> members) {
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

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}


class Server {

    int nodeId;

    ArrayList<Node1> members = null;

    ExecutorService pool = Executors.newFixedThreadPool(10);


    Server(int nodeId, ArrayList<Node1> members) {

        this.nodeId = nodeId;
        this.members = members;
    }


    void serve(int port) {

        new Thread(() -> {

            try (ServerSocket ss = new ServerSocket(port)) {

                while (true) {

                    Socket s = ss.accept();

                    pool.submit(() -> handleClient(s));
                }

            } catch (Exception e) {

                e.printStackTrace();
            }

        }).start();
    }


    private void handleClient(Socket clientSocket) {

        try (

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                );

                PrintWriter out = new PrintWriter(
                        clientSocket.getOutputStream(),
                        true
                )

        ) {

            String message = in.readLine();

            System.out.println(message);

            if (message.startsWith("DISCOVER:")) {

                handleDiscover(message);

                out.println("DISCOVER ACK");

            } else if (message.startsWith("REQUESTVOTE:")) {

                out.println(handleVote(message));

            } else if (message.startsWith("APPENDENTRIES:")) {

                out.println(handleAppendEntries(message));

            } else if (message.startsWith("HEARTBEAT:")) {

                out.println("HEARTBEAT ACK");
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    String handleVote(String message) {

        Packet p = Packet.parsePacket(message);

        return "VOTE ACK TERM " + p.term;
    }


    String handleAppendEntries(String message) {

        Packet p = Packet.parsePacket(message);

        return "APPEND ACK TERM " + p.term;
    }


    String handleDiscover(String packetMessage) {

        String[] full = packetMessage.split("-");

        if (full.length < 2 || full[1].isEmpty()) {
            return "NAN";
        }

        String[] members = full[1].split(",");

        int newMembers = 0;

        for (String member : members) {

            if (member == null || member.isEmpty()) continue;

            String[] info = member.split(":");

            if (info.length < 3) continue;  

            int nodeId = Integer.parseInt(info[0]);
            String ipAddress = info[1];
            int port = Integer.parseInt(info[2]);

            boolean exists = false;

            for (Node1 m : this.members) {

                if (m.nodeId == nodeId) {

                    exists = true;
                    break;
                }
            }

            if (!exists) {

                Node1 n = new Node1(nodeId, ipAddress, port, false);

                this.members.add(n);

                ++newMembers;
            }
        }

        if (newMembers > 0) {
            return "ACK";
        }

        return "NAN";
    }
}


class LogEntry {

    int index;

    int term;

    String command;


    LogEntry(int index, int term, String command) {

        this.index = index;
        this.term = term;
        this.command = command;
    }

}



