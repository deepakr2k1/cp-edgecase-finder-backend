package com.example.cpdebuggerbackend.utils;

import com.example.cpdebuggerbackend.constants.AppConstants.Lang;
import com.example.cpdebuggerbackend.constants.AppConstants.Filetype;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String findMainClassInJavaCode(String javaCode) {
        Pattern pattern = Pattern.compile("class\\s+(\\w+)\\s*\\{");
        Matcher matcher = pattern.matcher(javaCode);

        if (matcher.find()) {
            String className = matcher.group(1);
            return className;
        } else {
            throw new RuntimeException("No Java class found");
        }
    }

    public static Lang getLangFromExecFilename(String filename) {
        String[] splits = filename.split("[./]");
        if(splits.length == 0) {
            throw new RuntimeException("executable filename extension is not present");
        }
        String extension = splits[splits.length - 1];
        System.out.println(filename);
        System.out.println(Arrays.toString(splits));
        System.out.println(extension);
        switch(extension) {
            case "class":
                return Lang.java;
            case "py":
                return Lang.py;
            default:
                return Lang.cpp;
        }
    }

    public static ProcessBuilder createExecProcessBuilder(String executableFilename) {
        Lang lang = getLangFromExecFilename(executableFilename);
        if(lang.equals(Lang.cpp)) {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.directory(new File(WORKING_DIR));
            processBuilder.command("./" + executableFilename);
            return processBuilder;
        } else if(lang.equals(Lang.java)) {
            ProcessBuilder processBuilder = new ProcessBuilder();
            String[] splits = executableFilename.split("/");
            processBuilder.directory(new File(WORKING_DIR + "/" + splits[0]));
            processBuilder.command("java", splits[1].replace(".class", ""));
            return processBuilder;
        } else if(lang.equals(Lang.py)) {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.directory(new File(WORKING_DIR));
            processBuilder.command("python3", executableFilename);
            return processBuilder;
        } else {
            throw new RuntimeException("Language not supported");
        }
    }
}
