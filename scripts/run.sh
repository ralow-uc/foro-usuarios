#!/usr/bin/env bash
# ---------------------------------------------------------------------------
#  Arranca foro-usuarios (puerto 8081) contra el esquema FORO de Oracle Cloud.
#
#  Uso:  ./scripts/run.sh
#
#  El otro microservicio del foro (foro-temas) corre desde su propio repositorio.
#  Para la demostracion completa hacen falta los dos arriba, porque este
#  servicio emite los tokens que el otro valida.
# ---------------------------------------------------------------------------
set -euo pipefail
DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/entorno-oracle.sh"

echo "foro-usuarios -> ${ORACLE_USER}@${TNS_ALIAS:-fullstack3_tp}  (puerto 8081)"
cd "$DIR/.."
exec ./mvnw -q spring-boot:run -Dspring-boot.run.profiles=oracle
