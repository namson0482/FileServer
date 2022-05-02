package fileserver.hyperion360;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fileserver.CommandDispatcherServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtils {

    public final static List<String> getListFiles(String path) {
        log.info(path);
        List<String> listFiles = new ArrayList();
        File dic = new File(path);
        File[] dicList = dic.listFiles();
        assert dicList != null : "Assert exception";
        for (File file : dicList) {
            String fileName = file.getName();
            listFiles.add(fileName);
        }
        return listFiles;
    }

}
