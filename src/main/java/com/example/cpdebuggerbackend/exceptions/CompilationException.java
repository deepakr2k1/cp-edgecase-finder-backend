package com.example.cpdebuggerbackend.exceptions;

import com.example.cpdebuggerbackend.constants.AppConstants.Filetype;
import com.example.cpdebuggerbackend.utils.Utils;
import jdk.jshell.execution.Util;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Data
public class CompilationException extends Exception {

    private Filetype filetype;
    private String filename;

    public CompilationException(String message, String filename, Filetype filetype) {
        super(message);
        this.filetype = filetype;
        this.filename = filename;
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        return message.replaceAll(filename, Utils.getFiletypeString(filetype));
    }
}
