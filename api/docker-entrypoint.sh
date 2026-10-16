#!/bin/sh
# Arranque de la API en el contenedor. Acepta la base como una sola URL
# (DATABASE_URL=postgresql://usuario:clave@host[:puerto]/db[?sslmode=require],
# formato de Neon y Render) y la pasa al formato que entiende Java (JDBC).
set -e
if [ -n "$DATABASE_URL" ]; then
  sin_esquema="${DATABASE_URL#*://}"            # usuario:clave@host/db?params
  credenciales="${sin_esquema%%@*}"             # usuario:clave
  destino="${sin_esquema#*@}"                   # host/db?params
  destino_sin_params="${destino%%\?*}"          # host/db
  : "${DB_USER:=${credenciales%%:*}}"
  : "${DB_PASSWORD:=${credenciales#*:}}"
  export DB_USER DB_PASSWORD
  case "$destino" in
    *sslmode=require*) export DB_URL="jdbc:postgresql://${destino_sin_params}?sslmode=require" ;;
    *)                 export DB_URL="jdbc:postgresql://${destino_sin_params}" ;;
  esac
fi
exec java -jar app.jar --server.port="${PORT:-8080}"
