package com.example.cpdebuggerbackend.models;

import lombok.Data;

@Data
public class CodeRunnerDto {
    private Code correctCode;
    private Code testingCode;
    private Code inputGeneratingCode;
    private Integer testRuns;
}
