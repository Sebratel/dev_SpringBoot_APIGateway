package br.com.sebratel.bff.controller.scripts;

import br.com.sebratel.bff.dto.OrderedWeeklyReportDTO;
import br.com.sebratel.bff.dto.RelatorioFinalDTO;
import br.com.sebratel.bff.dto.ActiveSellersInputDTO;
import br.com.sebratel.bff.service.WeeklyReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/weekly-reports", "/api/v1/apoio-semanal"})
@Slf4j
public class WeeklyReportController {


    private final WeeklyReportService weeklyReportService;

    @Autowired
    public WeeklyReportController(WeeklyReportService weeklyReportService) {
        this.weeklyReportService = weeklyReportService;
    }

    @GetMapping(value = "/vendedor")
    public ResponseEntity<OrderedWeeklyReportDTO> getPorVendedor(@RequestBody ActiveSellersInputDTO activeSellersInputDTO) {
        log.info("Iniciando relatorio para {}", activeSellersInputDTO.getNome());
        List<RelatorioFinalDTO> list = weeklyReportService.sellersReportStream(activeSellersInputDTO.getNome()).toList();
        return ResponseEntity.ok(new OrderedWeeklyReportDTO(list));
    }
}