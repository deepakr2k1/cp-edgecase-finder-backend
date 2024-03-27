package com.example.cpdebuggerbackend.exceptions;

import lombok.Data;

@Data
public class ExecTimedOutException extends Exception {
    private String testCaseFilename;
    public ExecTimedOutException(String message, String testCaseFilename) {
        super(message);
        this.testCaseFilename = testCaseFilename;
    }
}
