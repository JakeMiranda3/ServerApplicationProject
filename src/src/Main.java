import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Main {
    public static final String DATA_FOLDER = "data";

    public static void main(String[] args) {
        var server = new Main();
        server.createDataFolder();
        server.start_listening(5135);

    }

    private void createDataFolder() {
        File dataFolder = new File(DATA_FOLDER);

        try {
            if (!dataFolder.exists()) {
                var folderCreated = dataFolder.mkdir();

                if (!folderCreated) {
                    throw new IOException("Unable to create data folder: " + DATA_FOLDER);
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void start_listening(int portNumber) {
        System.out.println("Listening on port " + portNumber);

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Server: Client connected: " + clientSocket.getInetAddress());

                Thread worker = new Thread(() -> handleClient(clientSocket));
                worker.start();
            }

        } catch (IOException e) {
            System.out.println("Server: ERROR " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        DataInputStream input = null;
        DataOutputStream output = null;
        try {
            input = new DataInputStream(clientSocket.getInputStream());
            output = new DataOutputStream(clientSocket.getOutputStream());

            byte[] availableBytes = new byte[1024];
            int bytes = input.read(availableBytes);
            String encodedBranch = new String(availableBytes, 0, bytes);
            System.out.println("Server Received (branch): " + encodedBranch);

            String branch = Lib.decode_from_base64(encodedBranch);

            System.out.println("Server Decoded branch: " + branch);

            if (!branch.startsWith("bcode~")) {
                System.out.println("Server ERROR: Invalid branch format");
                return;
            }

            String branchCode = branch.split("~")[1];
            File dir = new File("data/" + branchCode);
            dir.mkdir();

            output.writeUTF("OK");
            output.flush();

            byte[] availableBytes2 = new byte[10240];
            int bytes2 = input.read(availableBytes2);
            String base64Data = new String(availableBytes2, 0, bytes2);

            System.out.println("Server Received (file): " + base64Data);

            String decoded = Lib.decode_from_base64(base64Data);

            System.out.println("Server Decoded (file): " + decoded);

            File outFile = new File("data/" + branchCode + "/branch_weekly_sales.txt");
            try (FileWriter writer = new FileWriter(outFile)) {
                writer.write(decoded);
            }

            output.writeUTF("OK");
            output.flush();

        } catch (IOException e) {
            System.out.println("Server: ERROR (client): " + e.getMessage());
        } finally {
            try {
                if (input != null) input.close();
                if (output != null) output.close();
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Server: ERROR closing client: " + e.getMessage());
            }
        }
    }
}

