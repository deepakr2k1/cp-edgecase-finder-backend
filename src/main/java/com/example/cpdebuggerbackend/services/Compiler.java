package com.example.cpdebuggerbackend.services;

import com.example.cpdebuggerbackend.exceptions.CompilationException;
import com.example.cpdebuggerbackend.utils.Utils;
import org.springframework.stereotype.Service;

import java.io.File;

import static com.example.cpdebuggerbackend.constants.AppConstants.*;

@Service
public class Compiler {

    public String compile(String filename, String lang, Filetype filetype) throws Exception {
        Lang language = Lang.valueOf(lang);

        return switch (language) {
            case cpp -> compileCppCode(filename, filetype);
            case java -> compileJavaCode(filename, filetype);
            case python -> filename;
        };
    }

    private String compileCppCode(String filename, Filetype filetype) throws Exception {
        String executableFilename = Utils.generateUniqueFilename();

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(WORKING_DIR));
        processBuilder.command("g++", filename, "-o", executableFilename);
        processBuilder.redirectErrorStream(true);

        Process compileProcess = processBuilder.start();
        String output = Utils.readProcessOutput(compileProcess);
        int exitCode = compileProcess.waitFor();

        if (exitCode != 0) {
            throw new CompilationException(output, filename, filetype);
        }

        return executableFilename;
    }

    private String compileJavaCode(String filename, Filetype filetype) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(WORKING_DIR));
        processBuilder.command("javac", filename);
        processBuilder.redirectErrorStream(true);

        Process compileProcess = processBuilder.start();
        String output = Utils.readProcessOutput(compileProcess);
        int exitCode = compileProcess.waitFor();

        if (exitCode != 0) {
            throw new CompilationException(output, filename, filetype);
        }

        String executableFilename = filename.replace(".java", ".class");

        return executableFilename;
    }
}
