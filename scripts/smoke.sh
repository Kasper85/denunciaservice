#!/usr/bin/env bash
set -euo pipefail

if [ -z "${1:-}" ]; then
  echo "Uso: $0 <BASE_URL>"
  echo "Ejemplo: $0 http://localhost:8080"
  exit 1
fi

BASE_URL="${1%/}"
echo "=========================================="
echo "Iniciando smoke test contra: ${BASE_URL}"
echo "=========================================="

CURL_TIMEOUT=90
DNI="98765432"
RANDOM_NUM=$((1000 + RANDOM % 9000))
DIRECCION="Av. Smoke Test ${RANDOM_NUM}"
DESCRIPCION="Denuncia automatizada de prueba ${RANDOM_NUM}"

# Paso 1: E1 - Registrar denuncia (POST /denuncias/)
echo -n "[1/5] POST /denuncias/ ... "
POST_PAYLOAD=$(cat <<EOF
{
  "dni": "${DNI}",
  "tipo": "ROBO",
  "direccion": "${DIRECCION}",
  "descripcion": "${DESCRIPCION}"
}
EOF
)

RESPONSE_FILE=$(mktemp)
HTTP_CODE=$(curl.exe -s -S --max-time "${CURL_TIMEOUT}" -w "%{http_code}" -o "${RESPONSE_FILE}" \
  -X POST "${BASE_URL}/denuncias/" \
  -H "Content-Type: application/json" \
  -d "${POST_PAYLOAD}")

if [ "${HTTP_CODE}" != "201" ]; then
  echo "FALLO (HTTP ${HTTP_CODE})"
  cat "${RESPONSE_FILE}"
  rm -f "${RESPONSE_FILE}"
  exit 1
fi

ID=$(grep -o '"id":[0-9]*' "${RESPONSE_FILE}" | head -n1 | cut -d: -f2)
if [ -z "${ID}" ]; then
  echo "FALLO: No se pudo obtener el ID de la respuesta"
  cat "${RESPONSE_FILE}"
  rm -f "${RESPONSE_FILE}"
  exit 1
fi
echo "OK (ID creado: ${ID})"

# Paso 2: E3 - Obtener denuncia por ID (GET /denuncias/{id})
echo -n "[2/5] GET /denuncias/${ID} ... "
HTTP_CODE=$(curl.exe -s -S --max-time "${CURL_TIMEOUT}" -w "%{http_code}" -o "${RESPONSE_FILE}" \
  -X GET "${BASE_URL}/denuncias/${ID}")

if [ "${HTTP_CODE}" != "200" ]; then
  echo "FALLO (HTTP ${HTTP_CODE})"
  cat "${RESPONSE_FILE}"
  rm -f "${RESPONSE_FILE}"
  exit 1
fi
echo "OK"

# Paso 3: E2 - Listar denuncias paginadas (GET /denuncias/?offset=0&limit=5)
echo -n "[3/5] GET /denuncias/?offset=0&limit=5 ... "
HTTP_CODE=$(curl.exe -s -S --max-time "${CURL_TIMEOUT}" -w "%{http_code}" -o "${RESPONSE_FILE}" \
  -X GET "${BASE_URL}/denuncias/?offset=0&limit=5")

if [ "${HTTP_CODE}" != "200" ]; then
  echo "FALLO (HTTP ${HTTP_CODE})"
  cat "${RESPONSE_FILE}"
  rm -f "${RESPONSE_FILE}"
  exit 1
fi
echo "OK"

# Paso 4: E4 - Listar denuncias de un ciudadano (GET /denuncias/ciudadano/{dni})
echo -n "[4/5] GET /denuncias/ciudadano/${DNI} ... "
HTTP_CODE=$(curl.exe -s -S --max-time "${CURL_TIMEOUT}" -w "%{http_code}" -o "${RESPONSE_FILE}" \
  -X GET "${BASE_URL}/denuncias/ciudadano/${DNI}")

if [ "${HTTP_CODE}" != "200" ]; then
  echo "FALLO (HTTP ${HTTP_CODE})"
  cat "${RESPONSE_FILE}"
  rm -f "${RESPONSE_FILE}"
  exit 1
fi
echo "OK"

# Paso 5: E5 - Atender denuncia (PUT /denuncias/{id})
echo -n "[5/5] PUT /denuncias/${ID} ... "
HTTP_CODE=$(curl.exe -s -S --max-time "${CURL_TIMEOUT}" -w "%{http_code}" -o "${RESPONSE_FILE}" \
  -X PUT "${BASE_URL}/denuncias/${ID}")

if [ "${HTTP_CODE}" != "200" ]; then
  echo "FALLO (HTTP ${HTTP_CODE})"
  cat "${RESPONSE_FILE}"
  rm -f "${RESPONSE_FILE}"
  exit 1
fi

if ! grep -q '"estado":0' "${RESPONSE_FILE}" && ! grep -q '"estado": 0' "${RESPONSE_FILE}"; then
  echo "FALLO: El estado no cambio a 0"
  cat "${RESPONSE_FILE}"
  rm -f "${RESPONSE_FILE}"
  exit 1
fi
echo "OK (estado=0 verificado)"

rm -f "${RESPONSE_FILE}"
echo "=========================================="
echo "Smoke test COMPLETADO EXITOSAMENTE (5/5 OK)"
echo "=========================================="
exit 0
