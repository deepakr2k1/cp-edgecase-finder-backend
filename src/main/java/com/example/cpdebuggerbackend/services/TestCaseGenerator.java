package com.example.cpdebuggerbackend.services;

import com.example.cpdebuggerbackend.utils.Utils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.example.cpdebuggerbackend.constants.AppConstants.*;

@Service
public class TestCaseGenerator {
    public List<String> generate(String executableFilename, Integer testRuns) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(WORKING_DIR));
        processBuilder.command("./" + executableFilename);

        String outputFilename = Utils.generateUniqueFilename();

        List<String> testCaseFilenames = new ArrayList<>();

        for(int testRun = 1; testRun <= testRuns; testRun++) {
            String testCaseFilename = outputFilename + FILE_SEPARATOR + TEST_CASE_SUFFIX + FILE_SEPARATOR + testRun + TXT_EXTENSION;
            String outputFilepath = WORKING_DIR + testCaseFilename;
            testCaseFilenames.add(testCaseFilename);

            File outputFile = new File(outputFilepath);
            processBuilder.redirectOutput(outputFile);

            Process executionProcess = processBuilder.start();
            executionProcess.waitFor();
        }

        return testCaseFilenames;
    }
}
