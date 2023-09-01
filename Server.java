
import java.io.*; 
import java.net.*;

public class Server { //1
    private File[] files;
    private int clientNo;
    private final String folder = "C:/Users/Natchaya Chantem/OneDrive - Silpakorn University/Desktop/OS PQ";
    public static final int PORT = 3300;
    public static final int DOWNLOAD_PORT = 3301;
    
    private void startServer() {
        files = new File(folder).listFiles();
        
        new Thread(() -> {
            try {
                System.out.println("Server is listening on port" +PORT);
                ServerSocket serverSocket = new ServerSocket(PORT);
                ServerSocket uploadServerSocket = new ServerSocket(DOWNLOAD_PORT);
                while(true) {
                    Socket socket = serverSocket.accept();
                    new Thread(new ClientThread(socket, ++clientNo, uploadServerSocket)).start();
                }
            } catch(IOException ex) {
                System.out.println("1");
                System.out.println(ex);
            } 
        }).start();
    } 
    
    class ClientThread implements Runnable { 
        private final Socket socket;
        private final int no;
        private ServerSocket uploadServer = null;

        public ClientThread(Socket socket, int no, ServerSocket uploadServer) {
            this.socket = socket;
            this.no = no;
            this.uploadServer = uploadServer;
        }

        @Override
        public void run() {
            try {
                System.out.println("Client " + clientNo + " is connected in port " + PORT);
                DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
                DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
                
                outputToClient.writeInt(files.length);
                
                for(int i=0; i<files.length; i++) {
                    outputToClient.writeUTF(files[i].getName());
                } 
                
                while(true) { 
                    try { 
                        int fileIndex = inputFromClient.readInt();
                        System.out.println(files[fileIndex].getName());
                        sendFile(fileIndex);
                    } catch(IOException ex) {}
                }
                
            } catch (IOException ex) {}
        }
        
        private void sendFile(int fileIndex) {
            try {   
                for (int i=0; i<10; i++) {
                    Socket uploadSocket = uploadServer.accept();
                    long size = files[fileIndex].length()/10;
                    long start = i * size;
                    int index = i;
                    new Thread(() -> {
                        try {
                            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(files[fileIndex].getAbsolutePath()));
                            DataOutputStream outputToClient = new DataOutputStream(uploadSocket.getOutputStream());
                            
                            outputToClient.writeLong(start); 
                            
                            byte[] buffer = new byte[1024 * 1024]; 
                            int read;
                            long currentRead = 0;
                            
                            bufferedInputStream.skip(start);
                            
                            while((read = bufferedInputStream.read(buffer)) != -1 && currentRead < size) {
                                outputToClient.write(buffer, 0, read);
                                currentRead += read;
                            }
                            
                            bufferedInputStream.close();
                            outputToClient.close();
                            socket.close();
                            
                            System.out.println("Thread " + (index+1) + " send file successfully");
                        } catch(IOException ex) {}
                    }).start(); //3
                }
            } catch(IOException ex) {} 
        }
    }
    
    public static void main(String[] args) {
        new Server().startServer();
    }
}

