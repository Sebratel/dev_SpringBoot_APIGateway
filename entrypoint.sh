#!/bin/bash
# Inicia o serviço de cron
service cron start

# Inicia a aplicação Java
exec java $JAVA_OPTS -jar app.jar