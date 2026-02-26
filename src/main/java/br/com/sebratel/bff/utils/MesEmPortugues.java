package br.com.sebratel.bff.utils;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class MesEmPortugues {
    public static String transformarNumeroDoMesEmString(int numero) {
        if (numero < 1 || numero > 12) {
            return "Mês inválido";
        }

        return Month.of(numero)
                .getDisplayName(TextStyle.FULL, Locale.of("pt", "BR"));
    }
}
