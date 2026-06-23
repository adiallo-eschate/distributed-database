import java.io.*;
import java.net.*;
import java.util.*;


public class TCPServer {


    void open(int localPort){
        
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
                System.out.println("Data received: " + Arrays.toString(data));

                out.write("Message received by the server.\n".getBytes());
                out.flush();
    
                clientSocket.close();
            }
            
            serverSocket.close();
       } catch (IOException e){
           e.printStackTrace();
       }

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