#-----------------------------------------------------------------------------
# Variables compartidas entre etapas
ARG KOBWEB_APP_ROOT="site"

#-----------------------------------------------------------------------------
# Etapa de construcción
FROM eclipse-temurin:21-jdk-jammy AS build

ARG KOBWEB_APP_ROOT
ENV KOBWEB_CLI_VERSION=0.9.18 \
    NODE_MAJOR=20

# 1. Copiar código fuente
WORKDIR /src
COPY . .
RUN rm -rf androidapp

# 2. Dependencias mínimas
RUN apt-get update -qq && \
    apt-get install -y --no-install-recommends \
      curl gnupg unzip ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# 3. Instalar Node.js
RUN mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key \
      | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg && \
    echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_${NODE_MAJOR}.x nodistro main" \
      | tee /etc/apt/sources.list.d/nodesource.list && \
    apt-get update -qq && \
    apt-get install -y --no-install-recommends nodejs && \
    rm -rf /var/lib/apt/lists/*

# 4. Instalar Kobweb CLI
RUN curl -sLO https://github.com/varabyte/kobweb-cli/releases/download/v${KOBWEB_CLI_VERSION}/kobweb-${KOBWEB_CLI_VERSION}.zip && \
    unzip -q kobweb-${KOBWEB_CLI_VERSION}.zip -d /kobweb-cli && \
    rm kobweb-${KOBWEB_CLI_VERSION}.zip
ENV PATH="/kobweb-cli/kobweb-${KOBWEB_CLI_VERSION}/bin:${PATH}"

# 5. Ajustes de memoria para Gradle (build ligero)
ENV GRADLE_OPTS="-Xmx64m -XX:MaxMetaspaceSize=32m -XX:+UseSerialGC -XX:+UseCompressedClassPointers \
-Dorg.gradle.daemon=false -Dorg.gradle.parallel=false -Dorg.gradle.workers.max=1"

# 6. Exportar sitio en modo producción (no arranca servidor Dev)
WORKDIR /src/${KOBWEB_APP_ROOT}
RUN kobweb export --layout fullstack --notty

# 7. Limpiar cachés intermedios
RUN rm -rf ~/.gradle ~/.m2 ~/.npm ~/.config ~/.local /root/.cache /tmp/*

#-----------------------------------------------------------------------------
# Etapa de ejecución
FROM eclipse-temurin:21-jre-jammy AS run

ARG KOBWEB_APP_ROOT
WORKDIR /app

# 8. Copiar sólo el resultado exportado
COPY --from=build /src/${KOBWEB_APP_ROOT}/.kobweb ./.kobweb

# 9. Variables para runtime
# IMPORTANTE: Asegurarse que las variables de entorno se pasen correctamente
ENV JAVA_TOOL_OPTIONS="-Xmx160m -XX:MaxRAMPercentage=60 -XX:+UseSerialGC -XX:+UseCompressedClassPointers"

# Crear un wrapper script que asegure que las variables de entorno estén disponibles
RUN echo '#!/bin/bash\n\
# Archivo para variables de entorno fallback\nif [ ! -f /app/.env ]; then\n\
  echo "# Variables de entorno de fallback" > /app/.env\n\
fi\n\
# Cargar variables de archivo .env\nsource /app/.env\n\
# Comprobar si MONGODB_URI está definido, sino usar el valor de fallback\nif [ -z "${MONGODB_URI}" ]; then\n\
  export MONGODB_URI=${MONGODB_URI_FALLBACK:-"mongodb+srv://fallback:password@localhost/gestor_db"}\n\
fi\n\
# Imprimir información de debug (quitar en producción final)\necho "Using MONGODB_URI: ${MONGODB_URI}"\n\
# Iniciar la aplicación con las variables disponibles\nexec /app/.kobweb/server/start.sh "$@"' > /app/start-wrapper.sh && \
chmod +x /app/start-wrapper.sh

EXPOSE 8080
ENTRYPOINT ["/app/start-wrapper.sh"]