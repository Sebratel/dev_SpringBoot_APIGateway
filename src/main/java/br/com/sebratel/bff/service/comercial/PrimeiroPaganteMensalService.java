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
//        List<RelatorioPorVendedorDTO> itensDeVendedoresFiltrados = vendedoresTransformados.stream()
//                .map(vendedor -> { // Usamos map, pois queremos um DTO por vendedor
//                    String nomeVendedorAlvo = vendedor.nome(); // Componente nomeVendedor do record
//
//                    // Filtra a lista base para encontrar itens correspondentes a este vendedor, mês e ano
//                    List<PrimeiroPaganteMensalDTO> listaFiltradaPorVendedor = listaFiltrada.stream()
//                            .filter(itemRelatorio ->
//                                    itemRelatorio.getNome().equals(nomeVendedorAlvo) && // Compara o nome do vendedor
//                                            itemRelatorio.getMesPagamento() == mesPagamentoAlvo && // Compara o mês de pagamento
//                                            itemRelatorio.getAnoPagamento() == anoPagamentoAlvo     // Compara o ano de pagamento
//                            )
//                            .collect(Collectors.toList()); // Coleta para uma List<PrimeiroPaganteMensalDTO>
//
//                    // Cria e preenche o RelatorioPorVendedorDTO para este vendedor
//                    RelatorioPorVendedorDTO relatorioPorVendedorDTO = new RelatorioPorVendedorDTO();
//                    relatorioPorVendedorDTO.setDadosDoVendedor(vendedor);
//                    relatorioPorVendedorDTO.setRelatorioPorVendedor(listaFiltradaPorVendedor);
//
//                    return relatorioPorVendedorDTO; // Retorna o DTO criado
//                })
//                .collect(Collectors.toList()); // Coleta todos os RelatorioPorVendedorDTOs em uma lista final
//
//        return itensDeVendedoresFiltrados;
    }

//    @Transactional(readOnly = true)
//    public List<PrimeiroPaganteMensalDTO> gerarRelatorioMerge(String nomeDoVendedor) {
//
//        logger.info("------- Iniciando geração de relatório primeiro pagante mensal -------");
//
//        // Usamos try-with-resources para garantir que os streams sejam fechados.
//        // A transação @Transactional acima garante que a sessão do Hibernate permaneça aberta.
//        try (
//             ) {
//
//            logger.info("------- Iniciando busca do contrato personalizado -------");
//
//            Map<JoinKey, List<PlanilhaInstalacaoDTO>> mapaPlanilha = listaPlanilhaStream
//                    .filter(p -> p.getClienteNome() != null && p.getDataCriacaoContrato() != null)
//                    .collect(Collectors.groupingBy(p -> new JoinKey(p.getClienteNome(), p.getDataCriacaoContrato())));
//
//            logger.info("------- Iniciando busca de planilha de instalacao e lógica da aplicação -------");
//            logger.info("------- Iniciando merge -------");
//
//            List<PrimeiroPaganteMensalDTO> resultadoMerge = new ArrayList<>();
//
//            listaPersonalizadoStream.forEach(p -> {
//                JoinKey chaveEsquerda = new JoinKey(p.getNome(), p.getDataCriacao());
//
//                if (mapaPlanilha.containsKey(chaveEsquerda)) {
//                    List<PlanilhaInstalacaoDTO> correspondencias = mapaPlanilha.get(chaveEsquerda);
//
//                    for (PlanilhaInstalacaoDTO match : correspondencias) {
//                        resultadoMerge.add(converterParaDTOCombinado(p, match));
//                    }
//                }
//            });
//            logger.info("------- Merge finalizado -------");
//            return resultadoMerge;
//
//        } catch (Exception e) {
//            logger.error("Erro ao gerar relatório merge: {}", e.getMessage(), e);
//            throw new RuntimeException("Erro ao gerar relatório de primeiro pagante mensal", e);
//        }
//    }
//
//    private PrimeiroPaganteMensalDTO converterParaDTOCombinado(ContratoPersonalizadoDTO personalizado, PlanilhaInstalacaoDTO planilha) {
//
//        if (planilha.getDataRetorno() == null) {
//            planilha.setDataRetorno(LocalDate.now());
//        }
//
//        return PrimeiroPaganteMensalDTO.builder()
//                .nome(personalizado.getNome().toUpperCase().trim())
//                .numeroContrato(personalizado.getNumeroContrato())
//                .primeiraEmissao(personalizado.getPrimeiraEmissao())
//                .pagamentoCliente(personalizado.getPagamentoCliente())
//                .dataCriacao(personalizado.getDataCriacao())
//                .description(personalizado.getDescription())
//                .status(personalizado.getStatus())
//                .cadastroCliente(planilha.getCadastroCliente())
//                .cidade(planilha.getCidade())
//                .vendedorNome(planilha.getVendedorNome())
//                .regiaoVendedor(planilha.getRegiaoVendedor())
//                .valor(planilha.getValor())
//                .tecnologia(planilha.getTecnologia())
//                .dataSaida(planilha.getDataSaida())
//                .dataRetorno(planilha.getDataRetorno().atStartOfDay())
//                .statusCancelamento(planilha.getStatusCancelamento())
//                .anoPagamento(personalizado.getPagamentoCliente().getYear())
//                .mesPagamento(personalizado.getPagamentoCliente().getMonth().getValue())
//                .build();
//    }
//
//    private record JoinKey(String nome, LocalDateTime dataCriacao) {
//    }
}