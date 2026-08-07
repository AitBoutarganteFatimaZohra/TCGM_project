package com.tcgm.dto.request;

import lombok.Data;

@Data
public class ValidationPointageRequest {

    private String notes;
    private String motifRejet; // Pour le rejet
}