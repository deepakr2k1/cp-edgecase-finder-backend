package com.example.cpdebuggerbackend.services;

import com.example.cpdebuggerbackend.exceptions.ExecTimedOutException;
import com.example.cpdebuggerbackend.exceptions.SegmentationFaultException;
import com.example.cpdebuggerbackend.models.Code;
import com.example.cpdebuggerbackend.models.ResultDto;
import com.example.cpdebuggerbackend.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
        // Compiling inputGeneratingCode, correctCode, testingCode
        List<String> allExecutables = compileAll(inputGeneratingCode, correctCode, testingCode);
        String inputGeneratingExec = allExecutables.get(0);
        String correctCodeExec = allExecutables.get(1);
        String testingCodeExec = allExecutables.get(2);

        // Generating test cases from input generating executable
        List<String> testCaseFilenames = testCaseGenerator.generate(inputGeneratingExec, testRuns);

        try {
            // Run Test cases on Correct & Testing code
            ResultDto resultDto = testCaseRunner.runTestCases(correctCodeExec, testingCodeExec, testCaseFilenames);
            if(!resultDto.getIsSameOutput()) {
                String input = Utils.readFile(resultDto.getInputFilename());
                String output1 = Utils.readFile(resultDto.getCorrectCodeOutputFilename());
                String output2 = Utils.readFile(resultDto.getTestCodeOutputFilename());
                resultDto.setInput(input);
                resultDto.setCorrectCodeOutput(output1);
                resultDto.setTestCodeOutput(output2);
            }
            return resultDto;
        } catch(ExecTimedOutException | SegmentationFaultException  e) {
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
                String codeFileName = saveCode(inputGeneratingCode);
                return compiler.compile(codeFileName, inputGeneratingCode.getLanguage(), Filetype.INPUT_GENERATING_CODE);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }, executor);
        CompletableFuture<String> compileCorrectCode = CompletableFuture.supplyAsync(() -> {
            try {
                String codeFileName = saveCode(correctCode);
                return compiler.compile(codeFileName, correctCode.getLanguage(), Filetype.CORRECT_CODE);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }, executor);
        CompletableFuture<String> compileTestingCode = CompletableFuture.supplyAsync(() -> {
            try {
                String codeFileName = saveCode(testingCode);
                return compiler.compile(codeFileName, testingCode.getLanguage(), Filetype.TESTING_CODE);
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

    public String saveCode(Code code) throws IOException, InterruptedException {
        if(code.getLanguage().equals(Lang.cpp.toString())) {
            String codeFileName = Utils.generateUniqueFilename() + CPP_EXTENSION;
            Utils.saveDataIntoFile(WORKING_DIR + codeFileName, code.getContent());
            return codeFileName;
        } else if (code.getLanguage().equals(Lang.java.toString())) {
            String codeFileName = Utils.createRandomFolder() + "/" + Utils.findMainClassInJavaCode(code.getContent()) + JAVA_EXTENSION;
            Utils.saveDataIntoFile(WORKING_DIR + codeFileName, code.getContent());
            return codeFileName;
        } else if (code.getLanguage().equals(Lang.python.toString())) {
            String codeFileName = Utils.generateUniqueFilename() + PYTHON_EXTENSION;
            Utils.saveDataIntoFile(WORKING_DIR + codeFileName, code.getContent());
            return codeFileName;
        } else {
            throw new RuntimeException("Language not supported!");
        }
    }

}
