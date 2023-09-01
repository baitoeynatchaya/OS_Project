import java.io.*;
import java.net.*;
import java.util.*;

public class Client { 
    private Socket socket;
    private DataInputStream inputFromServer;
    private DataOutputStream outputToServer;
    private String[] files;
    private String folder = "/home/natchaya/Desktop";

    public void startClient() {
        try {
            socket = new Socket("172.18.113.253", 3300);
            inputFromServer = new DataInputStream(socket.getInputStream());
            outputToServer = new DataOutputStream(socket.getOutputStream());
            
            int fileLength = inputFromServer.readInt();
            files = new String[fileLength];
            
            for(int i=0; i<fileLength; i++) {
                files[i] = inputFromServer.readUTF();
            }
            
            printFiles();
            
        } catch(IOException ex) {
            System.out.println("1");
          System.out.println(ex);
        }
    }    
    
    private void printFiles() {
        Scanner scan = new Scanner(System.in);
        try {
            while(true) {
                for(int i=0; i<files.length; i++) {
                    System.out.println("[" + (i+1) + "] " + files[i]);
                } 

                System.out.print("Enter the number of file to download: ");
                int fileIndex = scan.nextInt();
                if (fileIndex >= 1 && fileIndex <= files.length) {
                    outputToServer.writeInt(fileIndex-1); 
                    receiveFile(files[fileIndex-1]);
                    break;
                } else {
                    System.out.println("Error, please select file index again");
                } //2 
            }
        } catch(IOException ex) {
             System.out.println("2");
          System.out.println(ex);
     }
    }
    
    private void receiveFile(String fileName) { 
        for (int i=0; i<10; i++) {
            int index = i;
            new Thread(() -> {
                try {
                    Socket downloadSocket = new Socket("172.18.113.253", 3301);
                    DataInputStream dataFromServer = new DataInputStream(downloadSocket.getInputStream());
                    long start = dataFromServer.readLong();
                    
                    RandomAccessFile raf = new RandomAccessFile(folder+"/"+fileName, "rwd");
                    raf.seek(start);
                    
                    byte[] buffer = new byte[1024*1024];
                    int read = 0;
                    
                    while((read = dataFromServer.read(buffer)) != -1) {
                        raf.write(buffer, 0, read);
                    }
                    
                    raf.close();
                    dataFromServer.close();
                    downloadSocket.close();
                    
                    System.out.println("Thread "+ (index+1) + " download successfully");
                } catch (IOException ex) {
                 System.out.println("3");
          System.out.println(ex);         
 }
            }).start(); 
        }
    }
    
    public static void main(String[] args) {
        new Client().startClient();
    }
}