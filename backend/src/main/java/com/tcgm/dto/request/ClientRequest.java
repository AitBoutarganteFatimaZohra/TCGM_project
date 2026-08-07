package com.tcgm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientRequest {

    @NotBlank(message = "Le nom du client est obligatoire")
    private String name;

    private String contact;
    private String address;
    private String phone;
    private String email;
    private String ice; // Identifiant Commun de l'Entreprise
    private String rc;  // Registre de Commerce
}