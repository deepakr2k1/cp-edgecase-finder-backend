package com.example.cpdebuggerbackend.exceptions;

import lombok.Data;

@Data
public class SegmentationFaultException extends RuntimeException implements CodeRunException {
    private String testCaseFilename;
    private String executableFileName;
    public SegmentationFaultException(String message, String testCaseFilename, String executableFileName) {
        super(message);
        this.testCaseFilename = testCaseFilename;
        this.executableFileName = executableFileName;
    }

}
