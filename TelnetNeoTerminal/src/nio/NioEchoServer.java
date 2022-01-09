package nio;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

    public NioEchoServer() throws IOException {
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
            filesList(channel);
            return;
        }
        if (command.startsWith("cd ")) {
            moveToDirectory(channel, command);
            return;
        }
        if (command.startsWith("cat ")) {
            printFileContent(command);
            return;
        }
        if (command.startsWith("mkdir ")) {
            createDirectory(command);
            return;
        }
        if (command.startsWith("touch ")) {
            emptyFile(command);
            return;
        }
        System.out.println("Received: " + s);
        channel.write(ByteBuffer.wrap(s.toString().getBytes(StandardCharsets.UTF_8)));
    }

    private void filesList(SocketChannel channel) throws IOException {
        System.out.println("Получена команда: ls");
        String currentDir = System.getProperty("user.dir");
        String fileName = "..\n\r";
        File dir = new File(currentDir);
        File[] arrFiles = dir.listFiles();
        List<File> lst = Arrays.asList(arrFiles);
        channel.write(ByteBuffer.wrap(fileName.getBytes(StandardCharsets.UTF_8)));
        for (File file : lst) {
            fileName = file.getName() + "\n\r";
            channel.write(ByteBuffer.wrap(fileName.getBytes(StandardCharsets.UTF_8)));
            System.out.println(file.getName());
        }
    }

    //перегрузка метода filesList(), для использования в других методах
    private void filesList(SocketChannel channel, Path command) throws IOException {
        System.out.println("Получена команда: ls");
        String currentDir = command.toString();
        String fileName = "..\n\r";
        File dir = new File(currentDir);
        File[] arrFiles = dir.listFiles();
        List<File> lst = Arrays.asList(arrFiles);
        channel.write(ByteBuffer.wrap(fileName.getBytes(StandardCharsets.UTF_8)));
        for (File file : lst) {
            fileName = file.getName() + "\n\r";
            channel.write(ByteBuffer.wrap(fileName.getBytes(StandardCharsets.UTF_8)));
            System.out.println(file.getName());
        }
    }

    private void moveToDirectory(SocketChannel channel, String command) throws IOException {
        System.out.println("Получена команда: cd");
        String[] token = command.split("\\s+");
        if (token.length < 2) {
            return;
        }
        Path dir = Paths.get(token[1]);
        if (Files.exists(dir)) {
            filesList(channel, dir);
            return;
        } else {
            System.out.println("НЕТ ТАКОЙ ПАПКИ");
        }
    }

    private void printFileContent(String command) {
        System.out.println("Получена команда: cat");
    }

    private void createDirectory(String command) {
        System.out.println("Получена команда: mkdir");
    }

    private void emptyFile(String command) {
        System.out.println("Получена команда: touch");
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
