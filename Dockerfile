#-----------------------------------------------------------------------------
# Variables compartidas entre etapas
ARG KOBWEB_APP_ROOT="site"

#-----------------------------------------------------------------------------
# Etapa de construcción
FROM eclipse-temurin:21-jdk-slim as build

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

# 3. Instalar Node.js ligero
RUN mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg && \
    echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_$NODE_MAJOR.x nodistro main" \
      | tee /etc/apt/sources.list.d/nodesource.list && \
    apt-get update -qq && apt-get install -y nodejs && rm -rf /var/lib/apt/lists/*

# 4. Instalar Kobweb CLI
RUN curl -sLO https://github.com/varabyte/kobweb-cli/releases/download/v${KOBWEB_CLI_VERSION}/kobweb-${KOBWEB_CLI_VERSION}.zip && \
    unzip -q kobweb-${KOBWEB_CLI_VERSION}.zip && rm kobweb-${KOBWEB_CLI_VERSION}.zip
ENV PATH="/kobweb-${KOBWEB_CLI_VERSION}/bin:${PATH}"

# 5. Configuración de memoria para gradle
WORKDIR /src/${KOBWEB_APP_ROOT}
RUN mkdir -p ~/.gradle && \
    printf "org.gradle.jvmargs=-Xmx96m -XX:MaxMetaspaceSize=64m -XX:+UseSerialGC -XX:+UseCompressedClassPointers\n" >> ~/.gradle/gradle.properties && \
    printf "kotlin.daemon.jvmargs=-Xmx64m\norg.gradle.parallel=false\norg.gradle.daemon=false\norg.gradle.workers.max=1\n" \
      >> ~/.gradle/gradle.properties

# 6. Build y export minimal
RUN kobweb export --notty && \
    [ -f .kobweb/server/start.sh ] && chmod +x .kobweb/server/start.sh || true

# 7. Limpieza intermedia
RUN rm -rf ~/.gradle/caches /root/.cache /tmp/*

#-----------------------------------------------------------------------------
# Etapa final de ejecución
FROM eclipse-temurin:21-jre-slim as run

ARG KOBWEB_APP_ROOT
WORKDIR /app

# Copiar artefactos mínimos para arrancar
COPY --from=build /src/${KOBWEB_APP_ROOT}/.kobweb ./.kobweb

# Ajustes de JVM para 512MB
ENV JAVA_TOOL_OPTIONS="-Xmx192m -XX:+UseSerialGC -XX:+UseCompressedClassPointers"

EXPOSE 8080
ENTRYPOINT ["/app/.kobweb/server/start.sh"]
