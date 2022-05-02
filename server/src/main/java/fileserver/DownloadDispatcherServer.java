package fileserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fileserver.hyperion360.DownloadClientHandlerThread;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DownloadDispatcherServer implements Runnable {

    int portDownload;

    String rootFolder;

    DownloadDispatcherServer(int port, String rootFolder) {

        this.portDownload = port;
        this.rootFolder = rootFolder;
    }

    @SneakyThrows
    public void run() {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        try (
            ServerSocket sisterSocket = new ServerSocket(portDownload);
        ) {
            log.info("Binding to port " + portDownload + ", please wait  ...");
            log.info("Download Server started: " + sisterSocket);
            log.info("Waiting for a client to download files...");
            while (true) {
                Socket sisterClientSocket = sisterSocket.accept();
                System.out.println("Client accepted: " + sisterClientSocket);

                DownloadClientHandlerThread downloadClientHandlerThread = new DownloadClientHandlerThread(sisterClientSocket, rootFolder);
                executor.execute(downloadClientHandlerThread);
            }

        } finally {
            executor.shutdown();
        }
    }
}
