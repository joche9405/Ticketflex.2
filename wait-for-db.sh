#!/bin/bash
# Script para esperar a que MySQL esté disponible

# ----------------------------------------------------
# 1. Variables de Entorno
# ----------------------------------------------------
HOST=$MYSQL_HOST
PORT=$MYSQL_PORT
TIMEOUT=60 # Esperar máximo 60 segundos

echo "Esperando por MySQL en $HOST:$PORT..."

# ----------------------------------------------------
# 2. Bucle de Espera
# ----------------------------------------------------
for i in $(seq 1 $TIMEOUT); do
    if nc -z $HOST $PORT &> /dev/null; then
        echo "MySQL está activo. Continuando con el comando de inicio de la aplicación..."
        # --------------------------------------------------------------
        # ¡CAMBIO CRÍTICO!
        # Reemplaza el proceso del script con el comando de arranque (java -jar app.jar).
        # Esto permite que la aplicación se ejecute en el PID 1 del contenedor.
        # --------------------------------------------------------------
        exec "$@" 
    fi
    sleep 1
done

# ----------------------------------------------------
# 3. Fallo
# ----------------------------------------------------
echo "Tiempo de espera agotado después de $TIMEOUT segundos. MySQL no está disponible."
exit 1 # Salir con error