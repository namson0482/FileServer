package fileserver.hyperion360;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DownloadClientHandlerThread extends Thread {

    private final Socket socket;

    private final int length_byte = 4096;

    String rootFolder;

    public DownloadClientHandlerThread(Socket socket, String rootFolder) {
        this.socket = socket;
        this.rootFolder = rootFolder;
    }

    @SneakyThrows
    public void run() {
        File currentDirectory = new File(new File(".").getAbsolutePath());

        System.out.println("Processing: " + socket);
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        DataInputStream dis = new DataInputStream(socket.getInputStream());

        String fileName = dis.readUTF();
        String filePath = rootFolder + "/" + fileName;
        FileInputStream fis = null;
        boolean fileExists = true;
        try {
            fis = new FileInputStream(filePath);
        } catch (Exception e) {
            fileExists = false;
        }
        if(fileExists) {
            byte[] buffer = new byte[length_byte];
            int bytesRead = 0;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
            dos.flush();
            fis.close();
            dos.close();
            dis.close();
            socket.close();
        } else {
            log.info(fileName + "> File does not exist in server directory.\n");
        }

    }
}
