#-----------------------------------------------------------------------------
# Variables compartidas entre etapas
ARG KOBWEB_APP_ROOT="site"

#-----------------------------------------------------------------------------
# Etapa de construcción
FROM eclipse-temurin:21-jdk-jammy AS build

ENV KOBWEB_CLI_VERSION=0.9.18
ARG KOBWEB_APP_ROOT
ENV NODE_MAJOR=20

# 1. Copiar código fuente y eliminar carpetas innecesarias
WORKDIR /src
COPY . .
RUN rm -rf androidapp

# 2. Instalar dependencias mínimas
RUN apt-get update -qq && \
    apt-get install -y --no-install-recommends \
      curl gnupg unzip ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# 3. Instalar Node.js desde Nodesource
RUN mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key \
      | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg && \
    echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_$NODE_MAJOR.x nodistro main" \
      | tee /etc/apt/sources.list.d/nodesource.list && \
    apt-get update -qq && \
    apt-get install -y --no-install-recommends nodejs && \
    rm -rf /var/lib/apt/lists/*

# 4. Instalar Kobweb CLI globalmente
RUN curl -sLO https://github.com/varabyte/kobweb-cli/releases/download/v${KOBWEB_CLI_VERSION}/kobweb-${KOBWEB_CLI_VERSION}.zip && \
    unzip -q kobweb-${KOBWEB_CLI_VERSION}.zip -d /kobweb-cli && \
    rm kobweb-${KOBWEB_CLI_VERSION}.zip
ENV PATH="/kobweb-cli/kobweb-${KOBWEB_CLI_VERSION}/bin:${PATH}"

# 5. Ajustar opciones de Gradle para bajo consumo de memoria
ENV GRADLE_OPTS="-Xmx64m -XX:MaxMetaspaceSize=32m -XX:+UseSerialGC -XX:+UseCompressedClassPointers -Dorg.gradle.parallel=false -Dorg.gradle.daemon=false -Dorg.gradle.workers.max=1 -Dorg.gradle.configuration-cache=false"

# 6. Exportar el sitio en modo Fullstack (incluye carpeta site)
WORKDIR /src/${KOBWEB_APP_ROOT}
RUN kobweb export --layout fullstack --notty && \
    chmod +x .kobweb/server/start.sh

# 7. Limpieza de cachés intermedios para reducir el tamaño
RUN rm -rf ~/.gradle ~/.m2 ~/.npm ~/.config ~/.local /root/.cache /tmp/*

#-----------------------------------------------------------------------------
# Etapa final de ejecución
FROM eclipse-temurin:21-jre-jammy AS run

ARG KOBWEB_APP_ROOT
WORKDIR /app

# 8. Copiar únicamente el resultado de export
COPY --from=build /src/${KOBWEB_APP_ROOT}/.kobweb ./.kobweb

# 9. Variables de entorno para producción en 512 MB
ENV KOBWEB_SERVER_ENV=PROD
ENV JAVA_TOOL_OPTIONS="-Xmx160m -XX:MaxRAMPercentage=60 -XX:+UseSerialGC -XX:+UseCompressedClassPointers"
ENV JAVA_TMPDIR="/tmp"

EXPOSE 8080
ENTRYPOINT ["/app/.kobweb/server/start.sh"]
