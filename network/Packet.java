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
