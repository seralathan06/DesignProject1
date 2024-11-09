import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class TcpChatServer {
    public static JTextArea chatArea;
    private static JTextField inputField;
    private static String serverName;
    private static FileOutputStream fos;
    private static ObjectOutputStream oos;

    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {
        serverName = getServerName();
        JFrame frame = new JFrame("Server Chat - " + serverName);
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        inputField = new JTextField();

        frame.setLayout(new java.awt.BorderLayout());
        frame.add(new JScrollPane(chatArea), java.awt.BorderLayout.CENTER);
        frame.add(inputField, java.awt.BorderLayout.SOUTH);

        inputField.addActionListener(e -> {
            String message = inputField.getText();
            broadcastMessage(serverName + " : " + message);
            chatArea.append(serverName + " : " + message + "\n");
            inputField.setText("");Tcp
            if (message == null) {
                System.exit(0);
            }
        });

        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        File backupFile = new File("chat_backup.bin");
        fos = new FileOutputStream(backupFile);
        oos = new ObjectOutputStream(fos);

        ServerSocket serverSocket = new ServerSocket(5555);
        chatArea.append("Server started. Waiting for clients...\n");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            chatArea.append("Client connected\n");
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            clientHandler.start();
        }
    }

    private static String getServerName() {
        return JOptionPane.showInputDialog("Enter server name:");
    }

    static void broadcastMessage(String message) {
        for (ClientHandler clientHandler : ClientHandler.getClientHandlers()) {
            clientHandler.sendMessage(message);
        }
        try {
            oos.writeObject(message);
            oos.flush();
        } catch (IOException e) {
            chatArea.append("Error: " + e.getMessage() + "\n");
        }
    }
}

class ClientHandler extends Thread {
    private static List<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket clientSocket;
    private PrintWriter toClient;
    private BufferedReader fromClient;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        clientHandlers.add(this);
    }

    public static List<ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    @Override
    public void run() {
        try {
            toClient = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())),
                    true);
            fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String clientMessage;
            while (true) {
                clientMessage = fromClient.readLine();
                if (clientMessage == ".") {
                    break;
                }
                TcpChatServer.chatArea.append(clientMessage + "\n");
                TcpChatServer.broadcastMessage(clientMessage);
            }
            TcpChatServer.chatArea.append("Client Disconnected\n");
            clientHandlers.remove(this);
            fromClient.close();
            toClient.close();
            clientSocket.close();
        } catch (IOException e) {
            TcpChatServer.chatArea.append("Error: " + e.getMessage() + "\n");
        }
    }

    public void sendMessage(String message) {
        toClient.println(message);
    }
}