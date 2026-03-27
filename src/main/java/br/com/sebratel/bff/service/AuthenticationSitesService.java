package br.com.sebratel.bff.service;

import br.com.sebratel.bff.dto.ApiResponse;
import br.com.sebratel.bff.dto.AuthenticationSitesInputDTO;
import br.com.sebratel.bff.dto.AuthenticationSitesOutputDTO;
import br.com.sebratel.bff.model.entity.AuthenticationSiteEntity;
import br.com.sebratel.bff.repository.erp.AuthenticationSitesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthenticationSitesService {

    AuthenticationSitesRepository authenticationSitesRepository;

    @Autowired
    public AuthenticationSitesService(AuthenticationSitesRepository authenticationSitesRepository) {
        this.authenticationSitesRepository = authenticationSitesRepository;
    }

    public List<AuthenticationSitesOutputDTO> execute(String title) {
        return authenticationSitesRepository.findByTitle(title).stream().map(AuthenticationSitesOutputDTO::fromEntity).toList();
    }
}
