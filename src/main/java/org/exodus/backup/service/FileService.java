package org.exodus.backup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileService {

    private static final Logger LOG = LoggerFactory.getLogger(FileService.class);
    private static final String DEFAULT_BASE_PATH = "/opt/backup-store";

    public boolean folderExists(String folderName) {
        File file = new File(getBasePath()+File.separator+folderName);
        return file.exists() && file.isDirectory();
    }

    public void store(String folderName, String fileName, byte[] content) throws IOException {
        File file = new File(getBasePath()+File.separator+folderName+File.separator+fileName);

        if(!file.exists()){
            boolean created = file.createNewFile();
            LOG.debug(String.format("FILE.CREATED path:%s status %s", file.getAbsolutePath(), created));
        }

        FileOutputStream stream = new FileOutputStream(file);
        stream.write(content);
        stream.close();

    }

    private String getBasePath(){
        String basePath = System.getenv("BACKUP_STORE_PATH");
        if(basePath==null){
            basePath = System.getProperty("BACKUP_STORE_PATH");
        }
        if(basePath==null){
            basePath = DEFAULT_BASE_PATH;
        }
        return basePath;
    }

}
