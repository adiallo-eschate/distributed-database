import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;


public class RaftNode {

    enum State { FOLLOWER, CANDIDATE, LEADER }

    final int id;
    final List<Node1> peers;

    volatile int currentTerm = 0;
    volatile int votedFor = -1;
    volatile State state = State.FOLLOWER;

    volatile int leaderId = -1;

    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    final Random rand = new Random();

    volatile long lastHeartbeat = System.currentTimeMillis();

    // for elections
    int electionTimeoutMs = 3000;

    // track the votes for the candidate
    Set<Integer> votesReceived = ConcurrentHashMap.newKeySet();

    RaftNode(int id, List<Node1> peers) {
        this.id = id;
        this.peers = peers;

        startElectionTimer();
    }


    // timer
    void startElectionTimer() {
        scheduler.scheduleAtFixedRate(() -> {

            if (state == State.LEADER) return;

            long now = System.currentTimeMillis();

            if (now - lastHeartbeat > electionTimeoutMs) {
                startElection();
            }

        }, 500, 500, TimeUnit.MILLISECONDS);
    }


    // elction
    void startElection() {

        state = State.CANDIDATE;
        currentTerm++;
        votedFor = id;

        votesReceived.clear();
        votesReceived.add(id);

        System.out.println("Node " + id + " becomes CANDIDATE term " + currentTerm);

        for (Node1 peer : peers) {

            if (peer.nodeId == id) continue;

            sendRequestVote(peer);
        }
    }


    void sendRequestVote(Node1 peer) {

        Packet p = new Packet(
                Message.REQUESTVOTE,
                id,
                currentTerm,
                0
        );

        peer.client.connect(Packet.stringPacket(p), peer.ipAddress, peer.port);
    }

  
    synchronized void handleVoteResponse(int term, int voterId, int voteGranted) {

        if (term > currentTerm) {
            becomeFollower(term);
            return;
        }

        if (state != State.CANDIDATE) return;

        if (voteGranted == 1) {
            votesReceived.add(voterId);
        }

        if (votesReceived.size() > peers.size() / 2) {
            becomeLeader();
        }
    }

  
    void becomeLeader() {

        state = State.LEADER;
        leaderId = id;

        System.out.println("Node " + id + " becomes LEADER term " + currentTerm);
    }

 
    void becomeFollower(int newTerm) {

        state = State.FOLLOWER;
        currentTerm = newTerm;
        votedFor = -1;
    }

    // heartbeat
    void receivedHeartbeat(int term, int leaderId) {

        if (term >= currentTerm) {
            currentTerm = term;
            state = State.FOLLOWER;
            this.leaderId = leaderId;
            lastHeartbeat = System.currentTimeMillis();
        }
    }
}