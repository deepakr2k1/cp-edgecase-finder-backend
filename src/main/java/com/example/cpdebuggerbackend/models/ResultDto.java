package com.example.cpdebuggerbackend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ResultDto {
    Boolean isSameOutput;
    String input;
    String inputFilename;
    String correctCodeOutput;
    String correctCodeOutputFilename;
    String testCodeOutput;
    String testCodeOutputFilename;
    String errorMessage;
}
