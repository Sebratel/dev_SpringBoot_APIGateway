package br.com.sebratel.bff.enums;

/**
 * De qual tipo de evento veio a linha de assignment_clients (a presenca da linha e o gatilho do aviso
 * ao cliente). O historico permanente de cada tipo e separado; esta coluna so identifica a origem.
 */
public enum ClientOrigin {
    MASSIVA,
    GMUD
}
