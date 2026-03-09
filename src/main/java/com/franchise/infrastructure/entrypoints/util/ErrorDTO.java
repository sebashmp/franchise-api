package com.franchise.infrastructure.entrypoints.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {
    private String errorCode;
    private String message;
}
