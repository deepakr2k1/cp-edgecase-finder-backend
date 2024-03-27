package com.example.cpdebuggerbackend.services;

import com.example.cpdebuggerbackend.exceptions.CompilationException;
import com.example.cpdebuggerbackend.exceptions.ExecTimedOutException;
import com.example.cpdebuggerbackend.models.Code;
import com.example.cpdebuggerbackend.models.ResultDto;
import com.example.cpdebuggerbackend.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.example.cpdebuggerbackend.constants.AppConstants.*;

@Service
public class EdgeCaseFinderService {
    @Autowired
    Compiler compiler;
    @Autowired
    TestCaseGenerator testCaseGenerator;
    @Autowired
    TestCaseRunner testCaseRunner;

    public ResultDto find(Code inputGeneratingCode, Code correctCode, Code testingCode, Integer testRuns) throws Exception {
        List<String> allExecutables = compileAll(inputGeneratingCode, correctCode, testingCode);
        String inputGeneratingExec = allExecutables.get(0);
        String correctCodeExec = allExecutables.get(1);
        String testingCodeExec = allExecutables.get(2);

        List<String> testCaseFilenames = testCaseGenerator.generate(inputGeneratingExec, testRuns);

        try {
            ResultDto resultDto = testCaseRunner.runTestCases(correctCodeExec, testingCodeExec, testCaseFilenames);
            if(!resultDto.getIsSameOutput()) {
                System.out.println(resultDto.toString());
                String input = Utils.readFile(resultDto.getInputFilename());
                String output1 = Utils.readFile(resultDto.getCorrectCodeOutputFilename());
                String output2 = Utils.readFile(resultDto.getTestCodeOutputFilename());
                resultDto.setInput(input);
                resultDto.setCorrectCodeOutput(output1);
                resultDto.setTestCodeOutput(output2);
            }
            return resultDto;
        } catch(ExecTimedOutException e) {
            StringBuilder sb = new StringBuilder(e.getMessage());
            String input = Utils.readFile(e.getTestCaseFilename());
            sb.append("\nInput\n").append(input);
            throw new RuntimeException(sb.toString());
        }
    }

    private List<String> compileAll(Code inputGeneratingCode, Code correctCode, Code testingCode) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CompletableFuture<String> compileInputGeneratingCode = CompletableFuture.supplyAsync(() -> {
            try {
                String filename = Utils.generateUniqueFilename();
                Utils.saveDataIntoFile(WORKING_DIR + filename + CPP_EXTENSION, inputGeneratingCode.getContent());
                return compiler.compile(filename + CPP_EXTENSION, inputGeneratingCode.getLanguage(), Filetype.INPUT_GENERATING_CODE);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }, executor);
        CompletableFuture<String> compileCorrectCode = CompletableFuture.supplyAsync(() -> {
            try {
                String filename = Utils.generateUniqueFilename();
                Utils.saveDataIntoFile(WORKING_DIR + filename + CPP_EXTENSION, correctCode.getContent());
                return compiler.compile(filename + CPP_EXTENSION, correctCode.getLanguage(), Filetype.CORRECT_CODE);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }, executor);
        CompletableFuture<String> compileTestingCode = CompletableFuture.supplyAsync(() -> {
            try {
                String filename = Utils.generateUniqueFilename();
                Utils.saveDataIntoFile(WORKING_DIR + filename + CPP_EXTENSION, testingCode.getContent());
                return compiler.compile(filename + CPP_EXTENSION, testingCode.getLanguage(), Filetype.TESTING_CODE);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }, executor);

        try {
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(compileInputGeneratingCode, compileCorrectCode, compileTestingCode);
            allTasks.get(COMPILE_THREAD_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            String message = e.getMessage();
            if(message != null && !message.isEmpty()) {
                throw new RuntimeException(e.getMessage());
            }

            StringBuilder sb = new StringBuilder();
            if (!compileInputGeneratingCode.isDone()) {
                sb.append("Compilation of Input Generating code is Timed out [").append(COMPILE_THREAD_TIMEOUT).append(" sec]\n");
            }
            if (!compileCorrectCode.isDone()) {
                sb.append("Compilation of Correct code is Timed out [").append(COMPILE_THREAD_TIMEOUT).append(" sec]\n");
            }
            if (!compileTestingCode.isDone()) {
                sb.append("Compilation of Testing code is Timed out [").append(COMPILE_THREAD_TIMEOUT).append(" sec]\n");
            }
            throw new RuntimeException(sb.toString());
        } finally {
            executor.shutdown();
        }

        List<String> allExecutables = new ArrayList<>();
        allExecutables.add(compileInputGeneratingCode.get());
        allExecutables.add(compileCorrectCode.get());
        allExecutables.add(compileTestingCode.get());

        return allExecutables;
    }

}
