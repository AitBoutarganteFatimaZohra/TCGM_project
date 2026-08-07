package com.tcgm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OuvrierRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @NotBlank(message = "Le CIN est obligatoire")
    private String cin;

    private String specialite;
    private String phone;
    private String hireDate; // Format: yyyy-MM-dd
    private Boolean active = true;
}