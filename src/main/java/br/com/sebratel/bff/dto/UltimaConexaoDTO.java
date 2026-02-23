package br.com.sebratel.bff.dto;

import java.time.LocalDateTime;

public record UltimaConexaoDTO(
        String usuario,
        LocalDateTime inicio,
        LocalDateTime atualizado,
        LocalDateTime pausado,
        Long recebendo,
        Long enviando,
        String ipConexao
) {}