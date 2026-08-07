package com.tcgm.dto.request;

import lombok.Data;

@Data
public class OuvrierUpdateRequest {

    private String firstName;
    private String lastName;
    private String cin;
    private String specialite;
    private String phone;
    private String hireDate;
    private Boolean active;
}