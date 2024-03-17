package com.example.cpdebuggerbackend.models;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Code {
    private String language;
    private String content;
}
