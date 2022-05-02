package fileserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DownloadFileHandlerThread extends Thread {

    private CountDownLatch countDownLatch;

    public final static String SERVER_IP = "127.0.0.1";

    String fileName;

    long fileLength;

    Socket clientSocketDownload = null;

    Map<String, String> progressBar;

    private final int LENGTH_BYTE = 4096;

    int portData = 3002;

    String rootFolderDownload;

    public DownloadFileHandlerThread(String fileName, long fileLength, Map<String, String> progressBar,
        CountDownLatch countDownLatch, int portData, String rootFolderDownload) {
        this.fileName = fileName;
        this.fileLength = fileLength;
        this.progressBar = progressBar;
        this.countDownLatch = countDownLatch;
        this.portData = portData;
        this.rootFolderDownload = rootFolderDownload;
    }

    public void run()  {
        FileOutputStream fos = null;
        try {
            String filePath = rootFolderDownload + "/" + fileName;
            File file = new File(filePath);
            if(file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);

            clientSocketDownload = new Socket(SERVER_IP, portData);
            DataOutputStream dos = new DataOutputStream(clientSocketDownload.getOutputStream());
            DataInputStream dis = new DataInputStream(clientSocketDownload.getInputStream());
            dos.writeUTF(fileName);
            dos.flush();
            char[] animationChars = new char[]{'|', '/', '-', '\\'};
            int i =0;
            int bytesRead;
            byte[] buffer = new byte[LENGTH_BYTE];
            int total = 0;
            while ((bytesRead = dis.read(buffer, 0, buffer.length)) > 0) {
                fos.write(buffer, 0, bytesRead);
                total += LENGTH_BYTE;
                float percentage = (float) (total * 1.0 / fileLength) * 100;
                if(percentage > 100) {
                    percentage = 100f;
                }
                DecimalFormat df = new DecimalFormat("#.##");
                String value = fileName + " Processing: " + df.format(percentage) + "% " + animationChars[i++ % 4];
                this.progressBar.put(fileName, value);
                StringBuilder output = new StringBuilder();
                for(Map.Entry<String, String> entry: this.progressBar.entrySet()) {
                    output.append(this.progressBar.get(entry.getKey())).append(" && ");
                }
                output = new StringBuilder(output.substring(0, output.length() - 3));
                System.out.print(output + "\r");

            }
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            this.countDownLatch.countDown();
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
            if(clientSocketDownload != null) {
                try {
                    clientSocketDownload.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
