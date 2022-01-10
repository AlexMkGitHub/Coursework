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
        String command = s.toString();

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
            printFileContent(channel, command);
            return;
        }
        if (command.startsWith("mkdir ")) {
            System.out.println("Получена команда: mkdir");
            createDirectory(channel, command);
            return;
        }
        if (command.startsWith("touch ")) {
            System.out.println("Получена команда: touch");
            emptyFile(channel, command);
            return;
        }
        System.out.println("Received: " + s);
        channel.write(ByteBuffer.wrap(s.toString().getBytes(StandardCharsets.UTF_8)));
    }

    private void filesList(SocketChannel channel, String command) throws IOException {
        String[] token = command.split("\\s+");
        if (!token[0].equals("ls")) {
            currentDir = token[1];
        }
        String fileName = "..\n\r";
        File dir = new File(currentDir);
        File[] arrFiles = dir.listFiles();
        List<File> lst = Arrays.asList(arrFiles);
        channel.write(ByteBuffer.wrap(fileName.getBytes(StandardCharsets.UTF_8)));
        for (File file : lst) {
            fileName = file.getName();
            channel.write(ByteBuffer.wrap(fileName.getBytes(StandardCharsets.UTF_8)));
            channel.write(ByteBuffer.wrap("\n\r".getBytes(StandardCharsets.UTF_8)));
            System.out.println(file.getName());
        }
        channel.write(ByteBuffer.wrap("\n\r".getBytes(StandardCharsets.UTF_8)));
    }

    private void moveToDirectory(SocketChannel channel, String command) throws IOException {
        String[] token = command.split("\\s+");
        if (token.length < 2) {
            return;
        }
        byte bytes[] = token[1].getBytes("UTF-8");
        String value = new String(bytes, "UTF-8");
        Path dir = Paths.get(value);
        if (Files.exists(dir)) {
            filesList(channel, command);
        } else {
            System.out.println("ТАКОЙ ПАПКИ НЕ СУЩЕСТВУЕТ!");
            channel.write(ByteBuffer.wrap("ТАКОЙ ПАПКИ НЕ СУЩЕСТВУЕТ!\n\r".getBytes(StandardCharsets.UTF_8)));
        }
    }

    private void printFileContent(SocketChannel channel, String command) throws IOException {
        String[] token = command.split("\\s+");
        String dir = currentDir + token[1];
        Path fileDir = Paths.get(dir);
        if (Files.exists(fileDir)) {
            byte[] bytes = Files.readAllBytes(fileDir);
            String fileText = new String(bytes, StandardCharsets.UTF_8);
            channel.write(ByteBuffer.wrap(fileText.getBytes(StandardCharsets.UTF_8)));
            channel.write(ByteBuffer.wrap("\n\r".getBytes(StandardCharsets.UTF_8)));
            System.out.println(fileText);
        } else {
            channel.write(ByteBuffer.wrap("ТАКОГО ФАЙЛА НЕ СУЩЕСТВУЕТ!\n\r".getBytes(StandardCharsets.UTF_8)));
            System.out.println("ТАКОГО ФАЙЛА НЕ СУЩЕСТВУЕТ!");
        }
    }

    private void createDirectory(SocketChannel channel, String command) throws IOException {
        String[] token = command.split("\\s+");
        String dir = currentDir + token[1];
        Path currDir = Paths.get(dir);
        if (!Files.exists(currDir)) {
            Files.createDirectory(currDir);
            filesList(channel, "ls");
            channel.write(ByteBuffer.wrap("СОЗДАНА НОВАЯ ПАПКА!\n\r".getBytes(StandardCharsets.UTF_8)));
            System.out.println("СОЗДАНА НОВАЯ ПАПКА!");
        } else {
            channel.write(ByteBuffer.wrap("ПАПКА УЖЕ СОЗДАНА!\n\r".getBytes(StandardCharsets.UTF_8)));
            System.out.println("ПАПКА УЖЕ СОЗДАНА!");
        }
    }

    private void emptyFile(SocketChannel channel, String command) throws IOException {
        String[] token = command.split("\\s+");
        String dir = currentDir + token[1];
        Path currDir = Paths.get(dir);
        if (!Files.exists(currDir)) {
            Files.createFile(currDir);
            filesList(channel, "ls");
            channel.write(ByteBuffer.wrap("СОЗДАН НОВЫЙ ФАЙЛ!\n\r".getBytes(StandardCharsets.UTF_8)));
            System.out.println("СОЗДАН НОВЫЙ ФАЙЛ!");
        } else {
            channel.write(ByteBuffer.wrap("ФАЙЛ УЖЕ СОЗДАН!\n\r".getBytes(StandardCharsets.UTF_8)));
            System.out.println("ФАЙЛ УЖЕ СОЗДАН!");
        }
    }

    private void handleAccept() throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        channel.write(ByteBuffer.wrap(
                "Hello user. Welcome to our terminal\n\r".getBytes(StandardCharsets.UTF_8)
        ));
        System.out.println("Client accepted...");
    }

    public static void main(String[] args) throws IOException {
        new NioEchoServer();
    }
}
