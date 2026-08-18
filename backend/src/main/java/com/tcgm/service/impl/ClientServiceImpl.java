package com.tcgm.service.impl;

import com.tcgm.dto.request.ClientRequest;
import com.tcgm.dto.response.ClientResponse;
import com.tcgm.dto.response.SiteResponse;
import com.tcgm.exception.BadRequestException;
import com.tcgm.exception.ResourceNotFoundException;
import com.tcgm.mapper.ClientMapper;
import com.tcgm.mapper.SiteMapper;
import com.tcgm.model.Client;
import com.tcgm.repository.ClientRepository;
import com.tcgm.service.ClientService;
import com.tcgm.service.JournalService;
import com.tcgm.model.enums.TypeAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final SiteMapper siteMapper;
    private final JournalService journalService;

    @Override
    @Transactional
    public ClientResponse createClient(ClientRequest request) {
        log.info("Création d'un nouveau client: {}", request.getName());

        // Vérifier si le client existe déjà
        if (clientRepository.existsByName(request.getName())) {
            throw new BadRequestException("Un client avec ce nom existe déjà");
        }

        Client client = clientMapper.toEntity(request);
        client = clientRepository.save(client);

        journalService.logAction(
            TypeAction.CREATION,
            "CLIENT",
            client.getId(),
            "Création du client: " + client.getName(),
            null
        );

        log.info("Client créé avec succès: {}", client.getName());
        return clientMapper.toResponse(client);
    }

    @Override
    @Transactional
    public ClientResponse updateClient(Long id, ClientRequest request) {
        log.info("Mise à jour du client ID: {}", id);

        Client client = clientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Client", id));

        // Vérifier si le nom n'est pas déjà pris par un autre client
        if (!client.getName().equals(request.getName()) && 
            clientRepository.existsByName(request.getName())) {
            throw new BadRequestException("Un client avec ce nom existe déjà");
        }

        clientMapper.updateEntity(client, request);
        client = clientRepository.save(client);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "CLIENT",
            client.getId(),
            "Mise à jour du client: " + client.getName(),
            null
        );

        log.info("Client mis à jour avec succès: {}", client.getName());
        return clientMapper.toResponse(client);
    }

    @Override
    public ClientResponse getClientById(Long id) {
        log.debug("Récupération du client ID: {}", id);
        Client client = clientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Client", id));
        return clientMapper.toResponse(client);
    }

    @Override
    public Page<ClientResponse> getAllClients(String search, Pageable pageable) {
        log.debug("Récupération de tous les clients");
        if (search != null && !search.isEmpty()) {
            return clientRepository.searchClients(search, pageable)
                .map(clientMapper::toResponse);
        }
        return clientRepository.findAll(pageable)
            .map(clientMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteClient(Long id) {
        log.info("Suppression du client ID: {}", id);

        Client client = clientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Client", id));

        // Vérifier si le client a des sites
        if (!client.getSites().isEmpty()) {
            throw new BadRequestException("Impossible de supprimer ce client car il a des sites associés");
        }

        journalService.logAction(
            TypeAction.SUPPRESSION,
            "CLIENT",
            client.getId(),
            "Suppression du client: " + client.getName(),
            null
        );

        clientRepository.delete(client);
        log.info("Client supprimé avec succès: {}", client.getName());
    }

    @Override
    public List<SiteResponse> getClientSites(Long clientId) {
        log.debug("Récupération des sites du client ID: {}", clientId);

        Client client = clientRepository.findByIdWithSites(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        return siteMapper.toResponseList(client.getSites());
    }
}