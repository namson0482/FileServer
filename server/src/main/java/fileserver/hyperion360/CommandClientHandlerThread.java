package fileserver.hyperion360;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import fileserver.CommandDispatcherServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandClientHandlerThread extends Thread {

    private Socket socket;

    ObjectOutputStream oos;

    ObjectInputStream ois;

    String rootFolder;

    public CommandClientHandlerThread(Socket socket, String rootFolder) {
        this.socket = socket;
        this.rootFolder = rootFolder;
    }

    private CommandLine proceedCommandIndex() {
        List<String> files = FileUtils.getListFiles(rootFolder);
        return new CommandLine(CommandLine.TYPE_RESULT, "result", files);
    }

    private void proceedSendFileToClient(String msg) throws IOException {
        Path path = Paths.get(rootFolder + "/" + msg);
        if (!Files.exists(path)) {
            CommandLine clientCommand = new CommandLine(CommandLine.TYPE_INFORM, "FileNotExist", "File is not exist");
            oos.writeObject(clientCommand);
            log.info(msg + " File is not exist!!!");
            return;
        }

        CommandLine clientCommand = new CommandLine(CommandLine.TYPE_INFORM, "FILE_SIZE", Files.size(path));
        oos.writeObject(clientCommand);
    }

    private int proceedCommand(CommandLine clientCommandLine) throws IOException {

        CommandLine serverCommandLine;
        switch (clientCommandLine.getCommand()) {
            case "index":
                serverCommandLine = proceedCommandIndex();
                oos.writeObject(serverCommandLine);
                break;
            case "get":
                proceedSendFileToClient((String)clientCommandLine.getValue());
                break;
            case "quit":
            case "q":
                return -1;
            default:
                serverCommandLine = new CommandLine(CommandLine.TYPE_UNKNOWN, "Unknown", "Unknown Command");
                oos.writeObject(serverCommandLine);
                break;
        }
        return 1;
    }

    public void run() {
        System.out.println("Processing: " + socket);
        try {
            oos  = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            while (true) {
                CommandLine clientCommandLine = (CommandLine)ois.readObject();
                int value = proceedCommand(clientCommandLine);
                if(value == -1) {
                    break;
                }
                log.info("Client message: " + clientCommandLine.getCommand());
            }
        } catch (IOException e) {
            System.err.println("Request Processing Error: " + e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                oos.close();
                ois.close();
                socket.close();;
            } catch (IOException e) {
            }
        }
        System.out.println("Complete processing: " + socket);
    }
}