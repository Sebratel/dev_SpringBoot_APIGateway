package br.com.sebratel.bff.controller;

import br.com.sebratel.bff.dto.splitters.*;
import br.com.sebratel.bff.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/splitters")
public class SplittersController {

    private final RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService;
    private final ListarSplittersService listarSplittersService;
    private final ListarOltsService listarOltsService;
    private final RecuperarSolicitacoesDeUmUsuarioService recuperarSolicitacoesDeUmUsuarioService;
    private final GetConnectionsService getConnectionsService;

    @Autowired
    public SplittersController(RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService,
                               ListarSplittersService listarSplittersService,
                               ListarOltsService listarOltsService,
                               RecuperarSolicitacoesDeUmUsuarioService recuperarSolicitacoesDeUmUsuarioService,
                               GetConnectionsService getConnectionsService) {
        this.recuperarTokenDoUsuarioIntegradorEllevenService = recuperarTokenDoUsuarioIntegradorEllevenService;
        this.listarSplittersService = listarSplittersService;
        this.listarOltsService = listarOltsService;
        this.recuperarSolicitacoesDeUmUsuarioService = recuperarSolicitacoesDeUmUsuarioService;
        this.getConnectionsService = getConnectionsService;
    }

    @GetMapping("/recuperarToken")
    public RecuperarTokenEllevenOutputDTO recuperarTokenDoUsuarioIntegradorElleven() {
        return recuperarTokenDoUsuarioIntegradorEllevenService.executar();
    }

    @GetMapping("/listarConnections")
    public EllevenSplitterResponseDTO<List<ConnectionDTO>> listarConnections() {
        return getConnectionsService.executar();
    }


    @GetMapping("/listarSplitters")
    public EllevenSplitterResponseDTO<List<NetworkComponentDTO>> listarSplitters() {
        return listarSplittersService.executar();
    }

    @GetMapping("/{splitterId}")
    public EllevenSplitterResponseDTO<List<NetworkComponentDTO>> listarSplitterPeloId(@PathVariable Long splitterId) {
        return listarSplittersService.executar(splitterId);
    }

    @GetMapping("/listarSplitters-paginado")
    public EllevenSplitterResponseDTO<EllevenPaginatedDTO<List<NetworkComponentDTO>>> listarSplittersPaginado(
            @RequestParam(defaultValue = "0") int inicio,
            @RequestParam(defaultValue = "20") int quantidade
    ) {
        return listarSplittersService.executar(inicio, quantidade);
    }

    @GetMapping("/listarOlts")
    public EllevenSplitterResponseDTO<List<NetworkComponentDTO>> listarOlts() {
        return listarOltsService.executar();
    }

    @GetMapping("/solicitacoes/cliente/{clientId}")
    public RecuperarSolicitacaoDeClienteOutputDTO recuperarSolicitacoesDeUmCliente(@PathVariable String clientId) {
            return recuperarSolicitacoesDeUmUsuarioService.executar(clientId);
    }
}
