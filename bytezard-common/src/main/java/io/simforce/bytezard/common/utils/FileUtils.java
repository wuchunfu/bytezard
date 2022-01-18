package io.simforce.bytezard.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FileUtils
 */
public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
        throw new UnsupportedOperationException("Construct JdbcUtils");
    }

    public static String readFile(String fileName) {
        String str = "";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);

            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8);
            int ch = 0;
            StringBuilder sb = new StringBuilder();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            str = sb.toString();
            return str;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * create directory and user
     * @param execLocalPath execute local path
     * @param userName user name
     * @throws IOException errors
     */
    public static void createWorkDirAndUserIfAbsent(String execLocalPath, String userName) throws IOException{
        //if work dir exists, first delete
        File execLocalPathFile = new File(execLocalPath);

        if (execLocalPathFile.exists()){
            org.apache.commons.io.FileUtils.forceDelete(execLocalPathFile);
        }

        //create work dir
        org.apache.commons.io.FileUtils.forceMkdir(execLocalPathFile);
        logger.info("create dir success {}" , execLocalPath);


        //if not exists this user,then create
        if (!OSUtils.getUserList().contains(userName)){
            OSUtils.createUser(userName);
        }
        logger.info("create user name success {}", userName);
    }

    public static String getJobExecDir(String jobType, long taskId) {
        String fileName = String.format(
                "%s/exec/job/%s/%s",
                "/bytezard",
                jobType,
                taskId);
        File file = new File(fileName);
        if (!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }

        return fileName;
    }
}
