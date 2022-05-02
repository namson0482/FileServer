package fileserver;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fileserver.hyperion360.CommandClientHandlerThread;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandDispatcherServer {

    public static final int NUM_OF_THREAD = 4;
    int portCommand = 3001;

    String rootFolder;

    DownloadDispatcherServer downloadDispatcherServer;

    public CommandDispatcherServer(int portCommand, String rootFolder) {
        this.portCommand = portCommand;
        this.rootFolder = rootFolder;
    }

    public void startCommandServer() throws IOException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREAD);
        ServerSocket serverSocket = null;
        try {
            log.info("Binding to port " + portCommand + ", please wait  ...");
            serverSocket = new ServerSocket(portCommand);
            log.info("Command Server started: " + serverSocket);
            log.info("Waiting for a client ...");
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("Client accepted: " + socket);

                    CommandClientHandlerThread handler = new CommandClientHandlerThread(socket, rootFolder);
                    executor.execute(handler);
                } catch (IOException e) {
                    log.error("Connection Error: " + e);
                }
            }
        } catch (IOException e1) {
            log.error(e1.getMessage());
        } finally {
            executor.shutdown();
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }

    private static boolean validateArgsValue(String []args) {
        boolean result = true;
        if(args == null || args.length != 3) {
            log.error("Arguments are invalid");
            result = false;
        }
        log.info("--------------------------------------------------");
        log.info("Syntax: java fileserver.Client 127.0.0.1 3001 3002 /home/son.vunam/Projects/workspace_intellij/FileServer/downloads");
        log.info("First Arg: Server Port Command.");
        log.info("Second Arg: Server Port Data.");
        log.info("Third Arg: Server will list all files in this directory");
        log.info("--------------------------------------------------");
        return result;
    }

    public static void main(String[] args) throws Exception {
        if(!validateArgsValue(args)) {
            return;
        }
        int portCommand = 3001;
        int portDownload = 3002;
        if (args.length > 0) {

            portCommand = Integer.parseInt(args[0].trim());
        }
        if (args.length > 1) {
            portDownload = Integer.parseInt(args[1].trim());
        }

        CommandDispatcherServer commandDispatcherServer = new CommandDispatcherServer(portCommand, args[2].trim());
        commandDispatcherServer.startDownloadServer(portDownload);
        commandDispatcherServer.startCommandServer();
    }

    public void startDownloadServer(int portDownload) throws InterruptedException {
        downloadDispatcherServer = new DownloadDispatcherServer(portDownload, rootFolder);
        Thread thread = new Thread(downloadDispatcherServer);
        thread.start();
        Thread.sleep(50);
    }

}