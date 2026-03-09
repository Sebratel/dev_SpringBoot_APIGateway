package br.com.sebratel.bff.scheduler;

import br.com.sebratel.bff.service.massivas.RefreshTokenMassivasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenRefreshSchedule {

    private final RefreshTokenMassivasService refreshTokenMassivasService;

    @Autowired
    public TokenRefreshSchedule(RefreshTokenMassivasService refreshTokenMassivasService) {
        this.refreshTokenMassivasService = refreshTokenMassivasService;
    }


    @Scheduled(fixedRateString = "${app.schedule.intervalo}")
    public void executar() {
        refreshTokenMassivasService.fetchIncidentsTask();
    }
}