package br.com.sebratel.bff.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MesEmPortuguesTest {

    @Test
    @DisplayName("Deve retornar 'Mês inválido' para números menores que 1")
    void deveRetornarMensagemDeErroParaNumeroMenorQueUm() {
        String resultado = MesEmPortugues.transformarNumeroDoMesEmString(0);
        assertEquals("Mês inválido", resultado);

        String resultadoNegativo = MesEmPortugues.transformarNumeroDoMesEmString(-5);
        assertEquals("Mês inválido", resultadoNegativo);
    }

    @Test
    @DisplayName("Deve retornar 'Mês inválido' para números maiores que 12")
    void deveRetornarMensagemDeErroParaNumeroMaiorQueDoze() {
        String resultado = MesEmPortugues.transformarNumeroDoMesEmString(13);
        assertEquals("Mês inválido", resultado);
    }

    @ParameterizedTest
    @CsvSource({
            "1, janeiro",
            "2, fevereiro",
            "3, março",
            "4, abril",
            "5, maio",
            "6, junho",
            "7, julho",
            "8, agosto",
            "9, setembro",
            "10, outubro",
            "11, novembro",
            "12, dezembro"
    })
    @DisplayName("Deve retornar o nome do mês correto em português")
    void deveRetornarNomeDoMesCorreto(int numero, String nomeEsperado) {
        String resultado = MesEmPortugues.transformarNumeroDoMesEmString(numero);

        // Convertendo para lowercase para evitar falhas caso o SO mude a capitalização padrão
        assertEquals(nomeEsperado, resultado.toLowerCase());
    }

    @Test
    @DisplayName("Deve garantir que o mês retornado é o esperado para um mês específico")
    void deveValidarMesEspecifico() {
        // Teste simples sem parametrização para clareza
        String resultado = MesEmPortugues.transformarNumeroDoMesEmString(12);
        assertTrue(resultado.equalsIgnoreCase("dezembro"));
    }
}