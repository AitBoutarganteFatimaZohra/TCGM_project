package com.tcgm.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse {
    private Boolean success;
    private String message;
    private Object data;
}