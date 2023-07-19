package bg.sofia.uni.fmi.mjt.crypto;

import bg.sofia.uni.fmi.mjt.crypto.command.Command;
import bg.sofia.uni.fmi.mjt.crypto.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.crypto.command.CommandParser;
import bg.sofia.uni.fmi.mjt.crypto.storage.DefaultAssetStorage;
import bg.sofia.uni.fmi.mjt.crypto.storage.DefaultCryptoUserStorage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class Server {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7777;
    private static final int BUFFER_CAPACITY = 4096;

    private static final String MESSAGE_WELCOME = "Welcome";

    private final String host;
    private final int port;
    private final ByteBuffer buffer;
    private final CommandExecutor executor;

    private Selector selector;
    private boolean running;

    public Server() {
        this(SERVER_HOST, SERVER_PORT, BUFFER_CAPACITY);
    }

    public Server(String host, int port) {
        this(host, port, BUFFER_CAPACITY);
    }

    public Server(String host, int port, int capacity) {
        this.host = host;
        this.port = port;
        this.buffer = ByteBuffer.allocateDirect(capacity);
        this.executor = new CommandExecutor(new DefaultCryptoUserStorage("user-database.csv"),
            new DefaultAssetStorage());
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            configureServerSocketChannel(serverSocketChannel);

            running = true;

            while (running) {
                int readyChannels = selector.select();

                if (readyChannels == 0) {
                    continue;
                }

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isReadable()) {
                        read(key);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the server socket", e);
        }
    }

    public void stop() {
        running = false;

        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel serverSocketChannel) throws IOException {
        selector = Selector.open();

        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(host, port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void configureSocketChannel(SocketChannel socketChannel) throws IOException {
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private String receiveRequest(SocketChannel socketChannel) throws IOException {
        buffer.clear();

        int bytes = socketChannel.read(buffer);

        if (bytes < 0) {
            socketChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void submitResponse(String response, SocketChannel socketChannel) throws IOException {
        buffer.clear();
        buffer.put(response.getBytes());
        buffer.flip();

        socketChannel.write(buffer);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        String request = receiveRequest(socketChannel);

        if (request == null) {
            return;
        }

        String username = (String) key.attachment();
        Command command = CommandParser.parseCommand(request);
        String response = executor.execute(username, command);

        if (username == null && Command.LOG_IN.equals(command.name()) && !response.contains(" ")) {
            key.attach(response);
            response = MESSAGE_WELCOME;
        } else if (Command.LOG_OUT.equals(command.name())) {
            key.attach(null);
        }

        submitResponse(response, socketChannel);
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        configureSocketChannel(socketChannel);
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
