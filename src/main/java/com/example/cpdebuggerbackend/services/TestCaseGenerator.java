package com.example.cpdebuggerbackend.services;

import com.example.cpdebuggerbackend.exceptions.ExecTimedOutException;
import com.example.cpdebuggerbackend.utils.Utils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.example.cpdebuggerbackend.constants.AppConstants.*;

@Service
public class TestCaseGenerator {

    public List<String> generate(String executableFilename, Integer testRuns) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<String>> executeInputGeneratingTasks = new ArrayList<>();

        for(int testRun = 1; testRun <= testRuns; testRun++) {
            ProcessBuilder processBuilder = Utils.createExecProcessBuilder(executableFilename);

            CompletableFuture<String> executeInputGenerationTask = CompletableFuture.supplyAsync(() -> {
                String testCaseFilename = Utils.generateUniqueFilename() + TXT_EXTENSION;
                String outputFilepath = WORKING_DIR + testCaseFilename;

                File outputFile = new File(outputFilepath);
                processBuilder.redirectOutput(outputFile);

                try {
                    Process executionProcess = null;
                    executionProcess = processBuilder.start();
                    executionProcess.waitFor();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return testCaseFilename;
            });
            executeInputGeneratingTasks.add(executeInputGenerationTask);
        }

        CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                executeInputGeneratingTasks.toArray(new CompletableFuture[0]));


        List<String> testCaseFilenames;
        try {
            allTasks.get(EXECUTE_THREAD_TIMEOUT, TimeUnit.SECONDS);
            testCaseFilenames = executeInputGeneratingTasks.stream().map(CompletableFuture::join).toList();
        } catch (Exception e) {
            executeInputGeneratingTasks.forEach(task -> task.cancel(true));
            StringBuilder sb = new StringBuilder();
            sb.append("Input Generating code is running for too long [").append(EXECUTE_THREAD_TIMEOUT).append(" sec]\n");
            throw new ExecTimedOutException(sb.toString(), null);
        } finally {
            executor.shutdown();
        }

        return testCaseFilenames;
    }
}
