package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.splitters.EllevenSplitterResponseDTO;
import br.com.sebratel.bff.dto.splitters.RecuperarTokenEllevenOutputDTO;
import br.com.sebratel.bff.service.ListarOltsService;
import br.com.sebratel.bff.service.ListarSplittersService;
import br.com.sebratel.bff.service.RecuperarTokenDoUsuarioIntegradorEllevenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/splitters")
public class SplittersController {

    private final RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService;
    private final ListarSplittersService listarSplittersService;
    private final ListarOltsService listarOltsService;

    @Autowired
    public SplittersController(RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService, ListarSplittersService listarSplittersService, CacheManager cacheManager, ListarOltsService listarOltsService) {
        this.recuperarTokenDoUsuarioIntegradorEllevenService = recuperarTokenDoUsuarioIntegradorEllevenService;
        this.listarSplittersService = listarSplittersService;
        this.listarOltsService = listarOltsService;
    }

    @GetMapping("/recuperarToken")
    public RecuperarTokenEllevenOutputDTO recuperarTokenDoUsuarioIntegradorElleven() {
        return recuperarTokenDoUsuarioIntegradorEllevenService.executar();
    }

    @GetMapping("/listarSplitters")
    public EllevenSplitterResponseDTO listarSplitters() {
        RecuperarTokenEllevenOutputDTO auth = recuperarTokenDoUsuarioIntegradorEllevenService.executar();
        return listarSplittersService.executar(auth.accessToken());
    }

    @GetMapping("/listarOlts")
    public EllevenSplitterResponseDTO listarOlts() {
        RecuperarTokenEllevenOutputDTO auth = recuperarTokenDoUsuarioIntegradorEllevenService.executar();
        return listarOltsService.executar(auth.accessToken());
    }

}
