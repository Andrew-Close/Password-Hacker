import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerHack implements Runnable {
  HackingTests hacking;

  boolean stopThread = true;
  boolean connected;

  ServerSocket server;
  Socket socket;
  List<String> message = new ArrayList<>();

  DataInputStream inputStream;
  DataOutputStream outputStream;

  public ServerHack(HackingTests hacking) {
    this.hacking = hacking;
  }

  @Override
  public void run() {
    String address = "localhost";
    int port = 9090;
    try {
      server = new ServerSocket(port, 50, InetAddress.getByName(address));
    } catch (IOException ignored) {
    }
    hacking.ready = true;
    try {
      socket = server.accept();
      connected = true;
      socket.setSoTimeout(16000);

      inputStream = new DataInputStream(socket.getInputStream());
      outputStream = new DataOutputStream(socket.getOutputStream());
      while (stopThread) {
        String msg = inputStream.readUTF();
        message.add(msg);
        if(message.size()>1_000_000){
          outputStream.writeUTF("Too many attempts");
          break;
        }
        if(msg.equals(hacking.password)){
          outputStream.writeUTF("Connection success!");
        }else{
          outputStream.writeUTF("Wrong password!");
        }
      }
      disconnect();
    } catch (IOException ignored) {
    }
  }

  public void disconnect() {
    stopThread = false;
    try {
      inputStream.close();
      socket.close();
      server.close();
    } catch (IOException ignored) {
    }
  }
}
