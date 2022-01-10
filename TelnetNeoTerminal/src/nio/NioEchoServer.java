package nio;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NioEchoServer {

    /**
     * Сделать терминал, которые умеет обрабатывать команды:
     * ls - список файлов в директории
     * cd dir_name - переместиться в директорию
     * cat file_name - распечатать содержание файла на экран
     * mkdir dir_name - создать директорию в текущей
     * touch file_name - создать пустой файл в текущей директории
     */

    private ServerSocketChannel serverChannel;
    private Selector selector;
    private ByteBuffer buf;
    private String currentDir;
    private String[] token;

    public NioEchoServer() throws IOException {
        currentDir = System.getProperty("user.dir");
        buf = ByteBuffer.allocate(5);
        serverChannel = ServerSocketChannel.open();
        selector = Selector.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(8189));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started...");
        while (serverChannel.isOpen()) {
            selector.select(); // block
            System.out.println("Keys selected...");
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept();
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder s = new StringBuilder();
        int read = 0;
        while (true) {
            read = channel.read(buf);
            if (read == 0) {
                break;
            }
            if (read < 0) {
                channel.close();
                return;
            }
            buf.flip();
            while (buf.hasRemaining()) {
                s.append((char) buf.get());
            }
            buf.clear();
        }
        String commandInput = s.toString();
        byte[] bytes = commandInput.getBytes(StandardCharsets.UTF_8);
        String command = new String(bytes, StandardCharsets.UTF_8);
        token = command.split("\\s+");

        if (command.startsWith("ls\r")) {
            System.out.println("Получена команда: ls");
            filesList(channel, command);
            return;
        }
        if (command.startsWith("cd ")) {
            System.out.println("Получена команда: cd");
            moveToDirectory(channel, command);
            return;
        }
        if (command.startsWith("cat ")) {
            System.out.println("Получена команда: cat");
            printFileContent(channel);
            return;
        }
        if (command.startsWith("mkdir ")) {
            System.out.println("Получена команда: mkdir");
            createDirectory(channel);
            return;
        }
        if (command.startsWith("touch ")) {
            System.out.println("Получена команда: touch");
            emptyFile(channel);
            return;
        }
        System.out.println("Received: " + s);
        sendCommand(channel, s.toString());
    }

    private void filesList(SocketChannel channel, String command) throws IOException {
        String[] token = command.split("\\s+");
        if (!token[0].equals("ls")) {
            currentDir = token[1];
        }
        String commandMessage = "..\n\r";
        File dir = new File(currentDir);
        File[] arrFiles = dir.listFiles();
        List<File> lst = Arrays.asList(arrFiles);
        sendCommand(channel, commandMessage);
        for (File file : lst) {
            commandMessage = file.getName();
            sendCommand(channel, commandMessage);
            sendCommand(channel, "\n\r");
            System.out.println(file.getName());
        }
        sendCommand(channel, "\n\r");
    }

    private void moveToDirectory(SocketChannel channel, String command) throws IOException {
        if (token.length < 2) {
            return;
        }
        Path dir = Paths.get(token[1]);
        if (Files.exists(dir)) {
            filesList(channel, command);
        } else {
            System.out.println("ТАКОЙ ПАПКИ НЕ СУЩЕСТВУЕТ!");
            sendCommand(channel, "ТАКОЙ ПАПКИ НЕ СУЩЕСТВУЕТ!\n\r");
        }
    }

    private void printFileContent(SocketChannel channel) throws IOException {
        if (token.length < 2) {
            return;
        }
        String dir = currentDir + token[1];
        Path fileDir = Paths.get(dir);
        if (Files.exists(fileDir)) {
            if (Files.isRegularFile(fileDir)) {
                byte[] bytes = Files.readAllBytes(fileDir);
                String fileText = new String(bytes, StandardCharsets.UTF_8);
                sendCommand(channel, fileText);
                sendCommand(channel, "\n\r");
                System.out.println(fileText);
            } else {
                sendCommand(channel, "НЕВОЗМОЖНО ПРОЧИТАТЬ ФАЙЛ!\n\r");
                System.out.println("НЕВОЗМОЖНО ПРОЧИТАТЬ ФАЙЛ!");
            }

        } else {
            sendCommand(channel, "ТАКОГО ФАЙЛА НЕ СУЩЕСТВУЕТ!\n\r");
            System.out.println("ТАКОГО ФАЙЛА НЕ СУЩЕСТВУЕТ!");
        }
    }

    private void createDirectory(SocketChannel channel) throws IOException {
        if (token.length < 2) {
            return;
        }
        String dir = currentDir + token[1];
        Path currDir = Paths.get(dir);
        if (!Files.exists(currDir)) {
            Files.createDirectory(currDir);
            filesList(channel, "ls");
            sendCommand(channel, "СОЗДАНА НОВАЯ ПАПКА!\n\r");
            System.out.println("СОЗДАНА НОВАЯ ПАПКА!");
        } else {
            sendCommand(channel, "ПАПКА УЖЕ СОЗДАНА!\n\r");
            System.out.println("ПАПКА УЖЕ СОЗДАНА!");
        }
    }

    private void emptyFile(SocketChannel channel) throws IOException {
        if (token.length < 2) {
            return;
        }
        String dir = currentDir + token[1];
        Path currDir = Paths.get(dir);
        if (!Files.exists(currDir)) {
            Files.createFile(currDir);
            filesList(channel, "ls");
            sendCommand(channel, "СОЗДАН НОВЫЙ ФАЙЛ!\n\r");
            System.out.println("СОЗДАН НОВЫЙ ФАЙЛ!");
        } else {
            sendCommand(channel, "ФАЙЛ УЖЕ СОЗДАН!\n\r");
            System.out.println("ФАЙЛ УЖЕ СОЗДАН!");
        }
    }

    private void handleAccept() throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        sendCommand(channel, "Hello user. Welcome to our terminal\n\r");
        System.out.println("Client accepted...");
    }

    private void sendCommand(SocketChannel channel, String message) throws IOException {
        channel.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
    }

    public static void main(String[] args) throws IOException {
        new NioEchoServer();
    }
}
