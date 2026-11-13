# Contrato HTTP do BFF

`manifest.json` lista as **155 rotas** que o BFF expõe (método, path com todos os aliases, serviço de destino na migração,
autenticação, parâmetros e corpo). É o contrato que o gateway precisa preservar (`specs/API_GATEWAY.md`).

- **Gerado do código**, por reflexão nos controllers, por `RouteManifestTest` (sem banco, sem subir o Spring).
- **Guarda**: o mesmo teste falha se uma rota do monólito mudar sem este arquivo ser atualizado.
- **Regenerar**: `./mvnw test -Dtest=RouteManifestTest -Dmanifest.write=true`, revisar o diff e copiar o arquivo para
  o repositório `dev_SpringBoot_Gateway` (`contract-tests/src/test/resources/manifest.json`), que o usa nos testes de paridade.
- **Uso real das rotas**: `docs/USO-ENDPOINTS-ELK.md` e `docs/uso-endpoints-elk.csv`.
