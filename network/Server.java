import java.util.*;
import java.net.*;
import java.net.concurrent.*;
import java.io.*;



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
