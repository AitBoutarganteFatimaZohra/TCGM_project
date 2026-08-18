package com.tcgm.service;

import com.tcgm.dto.request.ClientRequest;
import com.tcgm.dto.response.ClientResponse;
import com.tcgm.dto.response.SiteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClientService {
    ClientResponse createClient(ClientRequest request);
    ClientResponse updateClient(Long id, ClientRequest request);
    ClientResponse getClientById(Long id);
    Page<ClientResponse> getAllClients(String search, Pageable pageable);
    void deleteClient(Long id);
    List<SiteResponse> getClientSites(Long clientId);
}