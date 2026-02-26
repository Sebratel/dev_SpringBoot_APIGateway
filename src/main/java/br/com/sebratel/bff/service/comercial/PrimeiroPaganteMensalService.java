package br.com.sebratel.bff.service.comercial;

import br.com.sebratel.bff.dto.VendedoresAtivosDTO;
import br.com.sebratel.bff.dto.comercial.ContratoPersonalizadoDTO;
import br.com.sebratel.bff.dto.comercial.PlanilhaInstalacaoDTO;
import br.com.sebratel.bff.dto.comercial.PrimeiroPaganteMensalDTO;
import br.com.sebratel.bff.dto.comercial.RelatorioPorVendedorDTO;
import br.com.sebratel.bff.service.VendedoresAtivosService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PrimeiroPaganteMensalService {

    private static final Logger logger = LoggerFactory.getLogger(PrimeiroPaganteMensalService.class);

    private final ContratoPersonalizadoService personalizadoService;
    private final RelatorioPlanilhaService planilhaService;
    private final VendedoresAtivosService vendedoresAtivosService;

    @Transactional(readOnly = true)
    public List<RelatorioPorVendedorDTO> filtroELoop() {
        List<VendedoresAtivosDTO> vendedoresAtivos = vendedoresAtivosService.listarVendedoresAtivos();

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime primeiroDiaMesAnterior = agora
                .minusMonths(1)
                .with(TemporalAdjusters.firstDayOfMonth())
                .with(LocalTime.MIN);

        LocalDateTime ultimoDiaMesAnterior = agora
                .minusMonths(1)
                .with(TemporalAdjusters.lastDayOfMonth())
                .with(LocalTime.of(23, 59, 59));


        List<VendedoresAtivosDTO> vendedoresTransformados = vendedoresAtivos.stream()
                .map(dto -> {
                    String nomeOriginal = dto.nome();
                    String nomeTransformado = (nomeOriginal != null) ? nomeOriginal.trim().toUpperCase() : null;

                    return new VendedoresAtivosDTO(nomeTransformado, dto.email());
                })
                .toList();

        List<RelatorioPorVendedorDTO> listaDeResposta = new ArrayList<>(List.of());

        record JoinKey(String nome, LocalDateTime data) {}

        vendedoresTransformados.forEach(vendedor -> {
            Stream<PlanilhaInstalacaoDTO> listaPlanilhaStream = planilhaService.listarPlanilhaInstalacao(vendedor.nome());
            List<PlanilhaInstalacaoDTO> planilhaEmLista = listaPlanilhaStream.toList();
            List<String> listaDeClientes = planilhaEmLista.stream().map(PlanilhaInstalacaoDTO::getClienteNome).toList();
            List<ContratoPersonalizadoDTO> listaPersonalizadoStream = personalizadoService.listarContratosPersonalizados(primeiroDiaMesAnterior, ultimoDiaMesAnterior, listaDeClientes).toList();


            Map<JoinKey, List<PlanilhaInstalacaoDTO>> mapPlanilha = planilhaEmLista.stream()
                    .collect(Collectors.groupingBy(p -> new JoinKey(
                            p.getClienteNome(),
                            p.getDataCriacaoContrato()
                    )));


            List<PrimeiroPaganteMensalDTO> dfMerged = listaPersonalizadoStream.stream()
                    .filter(perso -> mapPlanilha.containsKey(new JoinKey(perso.getNome(), perso.getDataCriacao())))
                    .flatMap(perso -> {
                        JoinKey key = new JoinKey(perso.getNome(), perso.getDataCriacao());
                        return mapPlanilha.get(key).stream()
                                .map(plan -> new PrimeiroPaganteMensalDTO(
                                        perso.getNome(),
                                        perso.getNumeroContrato(),
                                        perso.getPrimeiraEmissao(),
                                        perso.getPagamentoCliente(),
                                        perso.getDataCriacao(),
                                        perso.getDescription(),
                                        perso.getStatus(),
                                        plan.getCadastroCliente(),
                                        plan.getCidade(),
                                        plan.getVendedorNome(),
                                        plan.getRegiaoVendedor(),
                                        plan.getValor(),
                                        plan.getTecnologia(),
                                        plan.getDataSaida(),
                                        plan.getDataRetorno() == null ? null : plan.getDataRetorno().atStartOfDay(),
                                        plan.getStatusCancelamento()
                                ));
                    })
                    .toList();



            RelatorioPorVendedorDTO relatorio =  new RelatorioPorVendedorDTO();
            relatorio.setDadosDoVendedor(vendedor);
            relatorio.setRelatorioPorVendedor(dfMerged);

            listaDeResposta.add(relatorio);



        });

        return listaDeResposta;
    }
}