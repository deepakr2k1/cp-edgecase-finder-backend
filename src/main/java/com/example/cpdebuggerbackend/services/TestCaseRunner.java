package com.example.cpdebuggerbackend.services;

import com.example.cpdebuggerbackend.exceptions.ExecTimedOutException;
import com.example.cpdebuggerbackend.models.ResultDto;
import com.example.cpdebuggerbackend.utils.Utils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

import static com.example.cpdebuggerbackend.constants.AppConstants.*;

@Service
public class TestCaseRunner {

    public ResultDto runTestCases(String correctCodeExec, String testingCodeExec, List<String> testCaseFilenames) throws Exception {
        for(String testCaseFile: testCaseFilenames) {
            ExecutorService executor = Executors.newFixedThreadPool(2);
            CompletableFuture<String> executeCorrectCodeTask = CompletableFuture.supplyAsync(() -> {
                try {
                    return execute(correctCodeExec, testCaseFile);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, executor);
            CompletableFuture<String> executeTestingCodeTask = CompletableFuture.supplyAsync(() -> {
                try {
                    return execute(testingCodeExec, testCaseFile);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, executor);

            try {
                CompletableFuture<Void> allTasks = CompletableFuture.allOf(executeCorrectCodeTask, executeTestingCodeTask);
                allTasks.get(EXECUTE_THREAD_TIMEOUT, TimeUnit.SECONDS);
            } catch(Exception e) {
                StringBuilder sb = new StringBuilder();
                if (!executeCorrectCodeTask.isDone()) {
                    sb.append("Correct code is running for too long [").append(EXECUTE_THREAD_TIMEOUT).append(" sec]\n");
                }
                if (!executeTestingCodeTask.isDone()) {
                    sb.append("Testing code is running for too long [").append(EXECUTE_THREAD_TIMEOUT).append(" sec]\n");
                }
                throw new ExecTimedOutException(sb.toString(), testCaseFile);
            } finally {
                executor.shutdown();
            }

            String ouputFile1 = executeCorrectCodeTask.get();
            String ouputFile2 = executeTestingCodeTask.get();

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
