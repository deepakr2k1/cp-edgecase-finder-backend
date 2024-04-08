package com.example.cpdebuggerbackend.controllers;

import com.example.cpdebuggerbackend.models.Code;
import com.example.cpdebuggerbackend.models.CodeRunnerDto;
import com.example.cpdebuggerbackend.models.ResultDto;
import com.example.cpdebuggerbackend.services.EdgeCaseFinderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EdgeCaseFinder {

    @Autowired
    private EdgeCaseFinderService edgeCaseFinderService;

    @PostMapping("/api/code-run")
    public ResponseEntity<ResultDto> codeRun (@RequestBody CodeRunnerDto body) {
        try {
            Code correctCode = body.getCorrectCode();
            Code testingCode = body.getTestingCode();
            Code inputGeneratingCode = body.getInputGeneratingCode();
            Integer testRuns = body.getTestRuns();

            ResultDto resultDto = edgeCaseFinderService.find(inputGeneratingCode, correctCode, testingCode, testRuns);

            return ResponseEntity.status(HttpStatus.OK).body(resultDto);
        } catch (Exception e) {
            System.err.println("==> " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResultDto.builder().errorMessage(e.getMessage()).build());
        }
    }
}
