# Imagem base para uso eventual do MySQL local.
# As credenciais devem ser fornecidas exclusivamente pelo Docker Compose ou pelo ambiente.
FROM mysql:8.4

COPY init.sql /docker-entrypoint-initdb.d/
