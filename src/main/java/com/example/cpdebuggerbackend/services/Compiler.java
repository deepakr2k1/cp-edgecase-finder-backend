package com.example.cpdebuggerbackend.services;

import com.example.cpdebuggerbackend.CompilationException;
import com.example.cpdebuggerbackend.utils.Utils;
import org.springframework.stereotype.Service;

import java.io.File;

import static com.example.cpdebuggerbackend.constants.AppConstants.*;

@Service
public class Compiler {

    public String compile(String filename, String lang) throws Exception {
        Lang language = Lang.valueOf(lang);

        return switch (language) {
            case CPP -> compileCppCode(filename);
            case JAVA -> compileJavaCode(filename);
            default -> throw new Exception("Compilation is not supported for " + language);
        };
    }

    private String compileCppCode(String filename) throws Exception {
        String executableFilename = Utils.generateUniqueFilename();

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(WORKING_DIR));
        processBuilder.command("g++", filename, "-o", executableFilename);
        processBuilder.redirectErrorStream(true);

        Process compileProcess = processBuilder.start();
        String output = Utils.readProcessOutput(compileProcess);
        int exitCode = compileProcess.waitFor();

        if (exitCode != 0) {
            throw new CompilationException(output);
        }

        return executableFilename;
    }

    private String compileJavaCode(String filename) {
        return "";
    }
}
