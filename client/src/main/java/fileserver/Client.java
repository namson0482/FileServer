package fileserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fileserver.hyperion360.CommandLine;
import fileserver.hyperion360.ValidateIPV4;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Son Vu
 * We use 2 ports here. 3001 for command line, 3002 for data
 */
@Slf4j
public class Client {

    String serverIpAddress;
    int portCommand;

    int portData;

    private Socket clientSocketCommand;

    ObjectOutputStream oosCommand;

    ObjectInputStream oisCommand;

    Map<String, String> mapFilesDownload;

    String rootFolderDownload;

    public Client() {

    }

    public Client(Socket socket, String serverIp, int portCommand, int portData, String rootFolderDownload) {
        clientSocketCommand = socket;
        this.serverIpAddress = serverIp;
        this.portCommand = portCommand;
        this.portData = portData;
        this.rootFolderDownload = rootFolderDownload;
    }

    private void proceedDownloadFile(String msg) throws IOException, ClassNotFoundException, InterruptedException {
        mapFilesDownload = new HashMap();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        String[] listFiles = msg.substring(3).trim().split(",");
        CountDownLatch countDownLatch = new CountDownLatch(listFiles.length);
        for (String fileName : listFiles) {
            fileName = fileName.trim();
            CommandLine clientCommandLine = new CommandLine(CommandLine.TYPE_EXEC, "get", fileName);
            oosCommand.writeObject(clientCommandLine);
            CommandLine serverCommandLine = (CommandLine) oisCommand.readObject();
            if (serverCommandLine.getCommand().equalsIgnoreCase("FileNotExist")) {
                log.info(fileName + " " + serverCommandLine.getValue());
                continue;
            }
            long length = (Long) serverCommandLine.getValue();
            DownloadFileHandlerThread downloadFileHandlerThread =
                new DownloadFileHandlerThread(fileName, length, mapFilesDownload, countDownLatch, portData, rootFolderDownload);
            executor.execute(downloadFileHandlerThread);
        }
        countDownLatch.await();
        executor.shutdown();
        System.out.println("");
    }

    public String sendMessage(String msg) throws IOException, ClassNotFoundException, InterruptedException {

        CommandLine clientCommandLine;
        StringBuilder value = new StringBuilder();
        String result = "1";
        String[] tempMsg = msg.toLowerCase().split(" ");
        if (tempMsg[0].equalsIgnoreCase("index")) {
            clientCommandLine = new CommandLine(CommandLine.TYPE_EXEC, msg, null);
            oosCommand.writeObject(clientCommandLine);
            CommandLine serverCommandLine = (CommandLine) oisCommand.readObject();
            log.info("\n");
            log.info("----------------------------------------------");
            log.info("List files on server");
            log.info("----------------------------------------------");
            List<String> files = (List) serverCommandLine.getValue();
            value.append("\n");
            for (String temp : files) {
                value.append(temp).append("\n");
            }
            log.info(value.toString());
            log.info("----------------------------------------------");
            log.info("----------------------------------------------");
            log.info("\n");
        } else if (tempMsg[0].equalsIgnoreCase("quit") || tempMsg[0].equalsIgnoreCase("q")) {
            clientCommandLine = new CommandLine(CommandLine.TYPE_EXEC, msg, null);
            oosCommand.writeObject(clientCommandLine);
            result = "-1";
        } else if (tempMsg[0].equalsIgnoreCase("get")) {
            proceedDownloadFile(msg);
        } else {
            clientCommandLine = new CommandLine(CommandLine.TYPE_UNKNOWN, msg, null);
            oosCommand.writeObject(clientCommandLine);
            CommandLine serverCommandLine = (CommandLine) oisCommand.readObject();
            log.info(serverCommandLine.getValue().toString());
        }
        return result;
    }

    public void proceed() {

        try {
            if (clientSocketCommand == null) {
                clientSocketCommand = new Socket(serverIpAddress, portCommand);
            }
            System.out.println("Connected: " + clientSocketCommand);
            oosCommand = new ObjectOutputStream(clientSocketCommand.getOutputStream());
            oisCommand = new ObjectInputStream(clientSocketCommand.getInputStream());
            while (true) {
                Scanner sc = new Scanner(System.in);
                System.out.print("Enter a string: ");
                String str = sc.nextLine();
                if((str = str.trim()).equalsIgnoreCase("")) {
                    continue;
                }
                String serverMsg = sendMessage(str);
                if (serverMsg.equalsIgnoreCase("-1")) {
                    break;
                }
            }
        } catch (Exception ie) {
            log.error("Can't connect to server");
        } finally {
            if (clientSocketCommand != null) {
                try {
                    clientSocketCommand.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static boolean validateArgsValue(String []args) {
        boolean result = true;
        if(args == null || args.length != 4) {
            log.error("Arguments are invalid");
            result = false;
        }
        log.info("--------------------------------------------------");
        log.info("Syntax: java fileserver.Client 127.0.0.1 3001 3002 /home/son.vunam/Projects/workspace_intellij/FileServer/downloads");
        log.info("First Arg: Server Ip Address.");
        log.info("Second Arg: Server Port Command.");
        log.info("Third Arg: Server Port Data.");
        log.info("Fourth Arg: Folder store downloaded files.");
        log.info("--------------------------------------------------");
        return result;
    }

    public static void main(String[] args) throws Exception {
        if(!validateArgsValue(args)) {
            return;
        }
        String serverIp = "127.0.0.1";
        int portCommand = 3001;
        int portData = 3002;
        if(args.length > 0) {
            serverIp = args[0].trim();
            if (!ValidateIPV4.isValidIPV4(serverIp)) {
                log.error("Server Ip Address is invalid");
                return;
            }
        }
        if (args.length > 1) {
            portCommand = Integer.parseInt(args[1].trim());
        }
        if(args.length > 2) {
            portData = Integer.parseInt(args[2].trim());
        }
        String rootFolderDownload = args[3];
        Socket socket = new Socket(serverIp, portCommand);
        Client client = new Client(socket, serverIp, portCommand, portData, rootFolderDownload);
        client.proceed();
        log.info("Good bye. See you again");
    }
}