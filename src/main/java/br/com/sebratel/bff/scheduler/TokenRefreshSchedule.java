package br.com.sebratel.bff.scheduler;

import br.com.sebratel.bff.service.massivas.RefreshTokenMassivasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


public class TokenRefreshSchedule {

    private final RefreshTokenMassivasService refreshTokenMassivasService;

    public TokenRefreshSchedule(RefreshTokenMassivasService refreshTokenMassivasService) {
        this.refreshTokenMassivasService = refreshTokenMassivasService;
    }

    public void executar() {
        refreshTokenMassivasService.fetchIncidentsTask();
    }
}