package com.example.cpdebuggerbackend.services;

import com.example.cpdebuggerbackend.models.ResultDto;
import com.example.cpdebuggerbackend.utils.Utils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.cpdebuggerbackend.constants.AppConstants.*;

@Service
public class TestCaseRunner {

    public ResultDto runTestCases(String executable1, String executable2, List<String> testCaseFilenames) throws Exception {
        for(String testCaseFile: testCaseFilenames) {
            ExecutorService executor = Executors.newFixedThreadPool(2);
            CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
                try {
                    return execute(executable1, testCaseFile);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, executor);
            CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> {
                try {
                    return execute(executable2, testCaseFile);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, executor);

            CompletableFuture<Void> allTasks = CompletableFuture.allOf(task1, task2);
            allTasks.get();
            executor.shutdown();

            String ouputFile1 = task1.get();
            String ouputFile2 = task2.get();

            Boolean isSameOutput = validateOutput(ouputFile1, ouputFile2);
            if(!isSameOutput) {
                return ResultDto.builder()
                        .isSameOutput(isSameOutput)
                        .inputFilename(testCaseFile)
                        .correctCodeOutputFilename(ouputFile1)
                        .testCodeOutputFilename(ouputFile2)
                        .build();
            }
        }
        return ResultDto.builder().isSameOutput(true).build();
    }

    public String execute(String executableFilename, String inputFilename) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(WORKING_DIR));
        processBuilder.command("./" + executableFilename);

        String outputFilename = Utils.generateUniqueFilename();
        String outputFilenameWithExtension = outputFilename + TXT_EXTENSION;

        String outputFilepath = WORKING_DIR + outputFilenameWithExtension;
        File outputFile = new File(outputFilepath);
        processBuilder.redirectOutput(outputFile);

        String inputFilePath = WORKING_DIR + inputFilename;
        File inputFile = new File(inputFilePath);
        processBuilder.redirectInput(inputFile);

        Process executionProcess = processBuilder.start();
        executionProcess.waitFor();

        return outputFilenameWithExtension;
    }

    public Boolean validateOutput(String filename1, String filename2) throws InterruptedException, IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(WORKING_DIR));
        processBuilder.command("diff", filename1, filename2);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        int exitCode = process.waitFor();

        return exitCode == 0 ? true : false;
    }

}
