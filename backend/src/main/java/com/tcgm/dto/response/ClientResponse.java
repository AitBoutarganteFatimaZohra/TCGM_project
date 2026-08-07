package com.tcgm.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ClientResponse {
    private Long id;
    private String name;
    private String contact;
    private String address;
    private String phone;
    private String email;
    private String ice;
    private String rc;
    private Integer totalSites;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}