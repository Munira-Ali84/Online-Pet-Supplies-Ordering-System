
import java.io.*;
import java.net.Socket;

public class ClientConnection {

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    // Connect to the server
    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);

            input = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );

            output = new PrintWriter(
                socket.getOutputStream(), true
            );

            System.out.println("Connected to server!");
            return true;

        } catch (IOException e) {
            System.out.println("Connection failed: " + e.getMessage());
            return false;
        }
    }

    // Send a message to the server
    public void sendMessage(String message) {
        if (output != null) {
            output.println(message);
        }
    }

    // Receive a message from the server
    public String receiveMessage() {
        try {
            if (input != null) {
                return input.readLine();
            }
        } catch (IOException e) {
            System.out.println("Error receiving message: " + e.getMessage());
        }
        return null;
    }

    // Close the connection
    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
            System.out.println("Disconnected from server.");

        } catch (IOException e) {
            System.out.println("Error closing connection.");
        }
    }
}

