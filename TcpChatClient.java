import java.io.*;
import java.net.*;
import javax.swing.*;

public class TcpChatClient {
    private static PrintWriter toServer;
    private static BufferedReader fromServer;
    private static JTextArea chatArea;
    private static JTextField inputField;
    private static String clientName;
    private static FileInputStream fis;
    private static ObjectInputStream ois;

    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {
        clientName = getClientName();
        JFrame frame = new JFrame("Client Chat - " + clientName);
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        inputField = new JTextField();

        frame.setLayout(new java.awt.BorderLayout());
        frame.add(new JScrollPane(chatArea), java.awt.BorderLayout.CENTER);
        frame.add(inputField, java.awt.BorderLayout.SOUTH);

        inputField.addActionListener(e -> {
            String message = inputField.getText();
            toServer.println(clientName + ": " + message);
            inputField.setText("");
            if (message.equals(".")) {
                try {
                    fromServer.close();
                    toServer.close();
                } catch (IOException ex) {
                    chatArea.append("Error: " + ex.getMessage() + "\n");
                }
            }
        });

        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        try {
            Socket Clt;
            if (args.length > 1) {
                chatArea.append("Usage: java TcpChatClient [hostipaddr]\n");
                System.exit(-1);
            }
            if (args.length == 0)
                Clt = new Socket(InetAddress.getLocalHost(), 5555);
            else
                Clt = new Socket(InetAddress.getByName(args[0]), 5555);

            toServer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(Clt.getOutputStream())), true);
            fromServer = new BufferedReader(new InputStreamReader(Clt.getInputStream()));

            File backupFile = new File("chat_backup.bin");
            if (backupFile.exists()) {
                fis = new FileInputStream(backupFile);
                ois = new ObjectInputStream(fis);
                try {
                    while (true) {
                        String message = (String) ois.readObject();
                        chatArea.append(message + "\n");
                    }
                } catch (EOFException e) {
                    // End of file reached
                } finally {
                    ois.close();
                    fis.close();
                }
            }

            String SrvMsg;
            while (true) {
                SrvMsg = fromServer.readLine();
                if (SrvMsg == ".") {
                    break;
                }
                chatArea.append(SrvMsg + "\n");
            }
            chatArea.append("Server Disconnected\n");
            fromServer.close();
            toServer.close();
            Clt.close();
        } catch (Exception E) {
            chatArea.append("Error: " + E.getMessage() + "\n");
        }
    }

    private static String getClientName() {
        return JOptionPane.showInputDialog("Enter your name:");
    }
}