#!/usr/bin/env python3
"""
Le a saida JSON do Semgrep, publica um resumo legivel no GitHub Step Summary e
decide se o pipeline deve bloquear.

Por que existe, em vez de usar "semgrep --error" direto: aquele modo falha sem
dizer o que encontrou, e o log do job nao chega ao resumo do PR. Aqui o
resultado fica visivel mesmo quando bloqueia -- e principalmente quando NAO
bloqueia, para que nada seja ignorado em silencio.

Regra de bloqueio:
  bloqueia   -> achado de severidade ERROR fora da lista de passivos conhecidos
  reporta    -> WARNING, INFO e passivos conhecidos
"""

import json
import os
import sys
from collections import Counter

# ---------------------------------------------------------------------------
# Passivos conhecidos: achados reais, ja rastreados, cuja correcao depende de
# uma decisao que nao esta neste repositorio. Aparecem no resumo com destaque,
# mas nao bloqueiam todo PR ate serem resolvidos.
#
# REGRA: nenhuma entrada aqui sem finding rastreado, justificativa e data de
# revisao. Isto nao e um mecanismo para silenciar achado inconveniente.
# ---------------------------------------------------------------------------
PASSIVOS_CONHECIDOS = [
    {
        "path": "src/main/resources/id_rsa_private.pem",
        "finding": "F-01",
        "motivo": (
            "Chave privada RSA versionada. Corrigir exige rotacao do par de "
            "chaves e definicao de janela de transicao para os QR Codes ja "
            "emitidos -- decisao de negocio, nao de codigo. Ate la o achado e "
            "reportado a cada execucao."
        ),
        "revisar_em": "2026-09-30",
    },
]


def eh_passivo_conhecido(caminho: str):
    """Retorna a entrada de passivo correspondente ao caminho, ou None."""
    normalizado = caminho.replace("\\", "/").lstrip("./")
    for passivo in PASSIVOS_CONHECIDOS:
        if normalizado.endswith(passivo["path"]):
            return passivo
    return None


def severidade(resultado: dict) -> str:
    return resultado.get("extra", {}).get("severity", "INFO").upper()


def escrever(linhas):
    destino = os.environ.get("GITHUB_STEP_SUMMARY")
    texto = "\n".join(linhas)
    if destino:
        with open(destino, "a", encoding="utf-8") as arquivo:
            arquivo.write(texto + "\n")
    print(texto)


def main() -> int:
    caminho_json = sys.argv[1] if len(sys.argv) > 1 else "semgrep.json"

    if not os.path.exists(caminho_json):
        escrever([
            "### SAST (Semgrep)",
            "",
            f":x: Arquivo `{caminho_json}` nao foi gerado -- a varredura nao chegou a rodar.",
        ])
        return 1

    with open(caminho_json, encoding="utf-8") as arquivo:
        dados = json.load(arquivo)

    resultados = dados.get("results", [])
    erros_de_execucao = dados.get("errors", [])

    bloqueantes = []
    passivos = []
    for resultado in resultados:
        if severidade(resultado) != "ERROR":
            continue
        passivo = eh_passivo_conhecido(resultado.get("path", ""))
        if passivo:
            passivos.append((resultado, passivo))
        else:
            bloqueantes.append(resultado)

    contagem = Counter(severidade(r) for r in resultados)

    # O SHA no cabecalho existe para tornar o relatorio auto-identificavel.
    # Sem ele e facil analisar o log de um run antigo e "corrigir" algo que ja
    # foi corrigido -- aconteceu duas vezes durante a montagem deste pipeline.
    sha = os.environ.get("GITHUB_SHA", "")
    ref = os.environ.get("GITHUB_REF_NAME", "")
    proveniencia = []
    if sha:
        proveniencia.append(f"commit `{sha[:7]}`")
    if ref:
        proveniencia.append(f"ref `{ref}`")

    linhas = [
        "### SAST (Semgrep)",
        "",
    ]
    if proveniencia:
        linhas += [
            "Analisado em " + ", ".join(proveniencia) + ".",
            "",
        ]
    linhas += [
        "| Severidade | Achados |",
        "| --- | --- |",
        f"| ERROR | {contagem.get('ERROR', 0)} |",
        f"| WARNING | {contagem.get('WARNING', 0)} |",
        f"| INFO | {contagem.get('INFO', 0)} |",
        "",
    ]

    if erros_de_execucao:
        linhas += [
            ":warning: **A varredura reportou erros de execucao** "
            "(regra invalida ou arquivo nao parseavel):",
            "",
        ]
        for erro in erros_de_execucao[:10]:
            mensagem = erro.get("long_msg") or erro.get("message") or str(erro)
            linhas.append(f"- `{mensagem[:300]}`")
        linhas.append("")

    if bloqueantes:
        linhas += [
            f":x: **{len(bloqueantes)} achado(s) ERROR bloqueando o pipeline**",
            "",
            "| Regra | Arquivo | Linha |",
            "| --- | --- | --- |",
        ]
        for resultado in bloqueantes[:40]:
            regra = resultado.get("check_id", "?").split(".")[-1]
            caminho = resultado.get("path", "?")
            linha = resultado.get("start", {}).get("line", "?")
            linhas.append(f"| `{regra}` | `{caminho}` | {linha} |")
        if len(bloqueantes) > 40:
            linhas.append(f"| ... | mais {len(bloqueantes) - 40} achado(s) | |")
        linhas.append("")

    if passivos:
        linhas += [
            f":warning: **{len(passivos)} achado(s) ERROR em passivo conhecido "
            "-- reportado, nao bloqueia**",
            "",
        ]
        vistos = set()
        for resultado, passivo in passivos:
            chave = (passivo["finding"], resultado.get("path"))
            if chave in vistos:
                continue
            vistos.add(chave)
            linhas += [
                f"- **{passivo['finding']}** em `{resultado.get('path')}` "
                f"(revisar ate {passivo['revisar_em']})",
                f"  - {passivo['motivo']}",
            ]
        linhas.append("")

    if not bloqueantes and not passivos:
        linhas.append(":white_check_mark: Nenhum achado de severidade ERROR.")

    linhas += [
        "",
        "Achados WARNING e INFO nao bloqueiam, mas ficam no JSON publicado "
        "nos artefatos do run. Nada e descartado.",
    ]

    escrever(linhas)

    if bloqueantes:
        print(f"::error::Semgrep encontrou {len(bloqueantes)} achado(s) ERROR bloqueante(s).")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
