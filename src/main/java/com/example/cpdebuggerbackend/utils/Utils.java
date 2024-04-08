package com.example.cpdebuggerbackend.utils;

import com.example.cpdebuggerbackend.constants.AppConstants.Filetype;

import java.io.*;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.example.cpdebuggerbackend.constants.AppConstants.TXT_EXTENSION;
import static com.example.cpdebuggerbackend.constants.AppConstants.WORKING_DIR;

public class Utils {

    public static String readProcessOutput(Process process) throws IOException {
        BufferedReader reader = new BufferedReader (new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append (System.getProperty("line.separator"));
        }
        return builder.toString();
    }

    public static String generateUniqueFilename() {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.toString().replace(":", "-");
        String randomUUID = UUID.randomUUID().toString().replace("-", "");
        return timestamp + "-" + randomUUID;
    }

    public static void saveDataIntoFile(String filepath, String data) throws IOException {
        FileWriter fileWriter = new FileWriter(filepath);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(data);
        bufferedWriter.close();
    }

    public static String readFile(String fileName) throws IOException {
        StringBuilder content = new StringBuilder();

        BufferedReader br = new BufferedReader(new FileReader(WORKING_DIR + fileName));
        String line;
        while ((line = br.readLine()) != null) {
            content.append(line).append("\n");
        }

        return content.toString();
    }

    public static String getFiletypeString(Filetype filetype) {
        switch (filetype) {
            case CORRECT_CODE -> {
                return "CORRECT CODE";
            }
            case TESTING_CODE -> {
                return "TESTING CODE";
            }
            case INPUT_GENERATING_CODE -> {
                return "INPUT GENERATING CODE";
            }
            default -> {
                return "";
            }
        }
    }

    public static String createRandomFolder() throws IOException, InterruptedException {
        String folderName = generateUniqueFilename();

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(WORKING_DIR));
        processBuilder.command("mkdir", folderName);

        Process createFolderProcess = processBuilder.start();
        createFolderProcess.waitFor();

        return folderName;
    }
}
