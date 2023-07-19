package bg.sofia.uni.fmi.mjt.crypto;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private static final String DEFAULT_SERVER_HOST = "localhost";
    private static final int DEFAULT_SERVER_PORT = 7777;
    private static final int DEFAULT_BUFFER_CAPACITY = 4096;

    private static final String MESSAGE_CONNECTION_SUCCESSFUL = "Connected to the server";
    private static final String MESSAGE_NETWORK_ERROR = "An unexpected network error occurred";

    private static final String COMMAND_EXIT = "exit";

    private static final String COMMAND_PROMPT = "> ";

    private final String host;
    private final int port;
    private final ByteBuffer buffer;

    public Client() {
        this(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT, DEFAULT_BUFFER_CAPACITY);
    }

    public Client(String host, int port) {
        this(host, port, DEFAULT_BUFFER_CAPACITY);
    }

    public Client(String host, int port, int capacity) {
        this.host = host;
        this.port = port;
        this.buffer = ByteBuffer.allocateDirect(capacity);
    }

    public void start() {
        try (SocketChannel socketChannel = SocketChannel.open(); Scanner scanner = new Scanner(System.in)) {
            socketChannel.connect(new InetSocketAddress(host, port));

            System.out.println(MESSAGE_CONNECTION_SUCCESSFUL);

            while (true) {
                System.out.print(COMMAND_PROMPT);

                String request = scanner.nextLine();

                if (request == null || request.isEmpty()) {
                    continue;
                }

                submitRequest(request, socketChannel);

                String response = receiveResponse(socketChannel);
                System.out.println(response);

                if (COMMAND_EXIT.equals(request)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println(MESSAGE_NETWORK_ERROR);
        }
    }

    private void submitRequest(String request, SocketChannel socketChannel) throws IOException {
        buffer.clear();
        buffer.put(request.getBytes());
        buffer.flip();
        socketChannel.write(buffer);
    }

    private String receiveResponse(SocketChannel socketChannel) throws IOException {
        buffer.clear();
        socketChannel.read(buffer);
        buffer.flip();

        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}
