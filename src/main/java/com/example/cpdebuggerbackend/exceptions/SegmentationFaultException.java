package com.example.cpdebuggerbackend.exceptions;

import com.example.cpdebuggerbackend.utils.Utils;
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

    @Override
    public String getMessage() {
        String message = super.getMessage();
        String[] splits = message.split(executableFileName + "\", ");
        return splits[splits.length - 1];
    }

}
