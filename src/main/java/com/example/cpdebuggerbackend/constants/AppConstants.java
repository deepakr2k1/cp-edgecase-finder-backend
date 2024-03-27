package com.example.cpdebuggerbackend.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

public class AppConstants {

    public static final String WORKING_DIR = "/Users/deepakr2k1/IdeaProjects/cp-debugger/cp-edge-case-finder-backend/code-files/";
    public static final String FILE_SEPARATOR = "-";
    public static final String TEST_CASE_SUFFIX = "test-case";
    public static final String OUTPUT_SUFFIX = "output";
    public static final String TXT_EXTENSION = ".txt";
    public static final String CPP_EXTENSION = ".cpp";
    public static final String JAVA_EXTENSION = ".java";
    public static final String PYTHON_EXTENSION = ".py";
    public static final Integer COMPILE_THREAD_TIMEOUT = 3;
    public static final Integer EXECUTE_THREAD_TIMEOUT = 10;

    public enum Lang {
        cpp,
        java,
        py
    }

    public enum Filetype {
        CORRECT_CODE,
        TESTING_CODE,
        INPUT_GENERATING_CODE
    }

}