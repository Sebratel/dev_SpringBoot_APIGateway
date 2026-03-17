package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.splitters.EllevenSplitterResponseDTO;
import br.com.sebratel.bff.dto.splitters.RecuperarSolicitacaoDeClienteOutputDTO;
import br.com.sebratel.bff.dto.splitters.RecuperarTokenEllevenOutputDTO;
import br.com.sebratel.bff.service.ListarOltsService;
import br.com.sebratel.bff.service.ListarSplittersService;
import br.com.sebratel.bff.service.RecuperarSolicitacoesDeUmUsuarioService;
import br.com.sebratel.bff.service.RecuperarTokenDoUsuarioIntegradorEllevenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/splitters")
public class SplittersController {

    private final RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService;
    private final ListarSplittersService listarSplittersService;
    private final ListarOltsService listarOltsService;
    private final RecuperarSolicitacoesDeUmUsuarioService recuperarSolicitacoesDeUmUsuarioService;

    @Autowired
    public SplittersController(RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService,
                               ListarSplittersService listarSplittersService,
                               ListarOltsService listarOltsService,
                               RecuperarSolicitacoesDeUmUsuarioService recuperarSolicitacoesDeUmUsuarioService) {
        this.recuperarTokenDoUsuarioIntegradorEllevenService = recuperarTokenDoUsuarioIntegradorEllevenService;
        this.listarSplittersService = listarSplittersService;
        this.listarOltsService = listarOltsService;
        this.recuperarSolicitacoesDeUmUsuarioService = recuperarSolicitacoesDeUmUsuarioService;
    }

    @GetMapping("/recuperarToken")
    public RecuperarTokenEllevenOutputDTO recuperarTokenDoUsuarioIntegradorElleven() {
        return recuperarTokenDoUsuarioIntegradorEllevenService.executar();
    }

    @GetMapping("/listarSplitters")
    public EllevenSplitterResponseDTO listarSplitters() {
        return listarSplittersService.executar();
    }

    @GetMapping("/listarSplitters-paginado")
    public EllevenSplitterResponseDTO listarSplittersPaginado(
            @RequestParam(defaultValue = "0") int inicio,
            @RequestParam(defaultValue = "20") int quantidade
    ) {
        return listarSplittersService.executar(inicio, quantidade);
    }

    @GetMapping("/listarOlts")
    public EllevenSplitterResponseDTO listarOlts() {
        return listarOltsService.executar();
    }

    @GetMapping("/solicitacoes/cliente/{clientId}")
    public RecuperarSolicitacaoDeClienteOutputDTO recuperarSolicitacoesDeUmCliente(@PathVariable String clientId) {
            return recuperarSolicitacoesDeUmUsuarioService.executar(clientId);
    }
}
