#!/bin/bash

echo "--- 📥 Iniciando Git Pull ---"
git pull # mude para sua branch (master/main/dev)

# Verifica se o git pull falhou
if [ $? -ne 0 ]; then
    echo "❌ Erro ao baixar código do Git. Abortando."
    exit 1
fi

echo "--- 🏗️  Limpando e Buildando Containers ---"
# BuildKit é necessário para o cache de dependencias (.m2) do Dockerfile
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1
# Evita timeout do compose em builds mais longos
export COMPOSE_HTTP_TIMEOUT=600
docker-compose up -d --build

echo "--- 🧹 Removendo imagens órfãs (opcional) ---"
docker image prune -f

echo "--- 📋 Status dos Containers ---"
docker-compose ps

echo "🚀 Aplicação atualizada com sucesso!"
