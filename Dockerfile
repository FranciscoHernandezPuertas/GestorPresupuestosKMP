#-----------------------------------------------------------------------------
# Variables compartidas entre etapas
ARG KOBWEB_APP_ROOT="site"

#-----------------------------------------------------------------------------
# Etapa de construcción
FROM eclipse-temurin:21-jdk-jammy as export

ENV KOBWEB_CLI_VERSION=0.9.18
ARG KOBWEB_APP_ROOT
ENV NODE_MAJOR=20

# 1. Copiar solo lo esencial
COPY . /project
RUN rm -rf /project/androidapp

# 2. Instalación mínima de dependencias
RUN apt-get update -qq && \
    apt-get install -y --no-install-recommends \
    curl \
    gnupg \
    unzip && \
    rm -rf /var/lib/apt/lists/*

# 3. Instalar Node.js optimizado
RUN mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg && \
    echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_$NODE_MAJOR.x nodistro main" | tee /etc/apt/sources.list.d/nodesource.list && \
    apt-get update -qq && \
    apt-get install -y --no-install-recommends nodejs

# 4. Instalar Playwright
RUN npx playwright install --with-deps chromium

# 5. Instalar Kobweb CLI
RUN curl -sLO https://github.com/varabyte/kobweb-cli/releases/download/v${KOBWEB_CLI_VERSION}/kobweb-${KOBWEB_CLI_VERSION}.zip && \
    unzip -q kobweb-${KOBWEB_CLI_VERSION}.zip && \
    rm kobweb-${KOBWEB_CLI_VERSION}.zip

ENV PATH="/kobweb-${KOBWEB_CLI_VERSION}/bin:${PATH}"

WORKDIR /project/${KOBWEB_APP_ROOT}

# 6. Configuración de memoria extrema
RUN mkdir -p ~/.gradle && \
    echo "org.gradle.jvmargs=-Xmx96m -XX:MaxMetaspaceSize=64m -XX:+UseSerialGC -XX:+UseCompressedClassPointers" >> ~/.gradle/gradle.properties && \
    echo "kotlin.daemon.jvmargs=-Xmx64m" >> ~/.gradle/gradle.properties && \
    echo "org.gradle.parallel=false" >> ~/.gradle/gradle.properties && \
    echo "org.gradle.daemon=false" >> ~/.gradle/gradle.properties && \
    echo "org.gradle.workers.max=1" >> ~/.gradle/gradle.properties

# 7. Build con verificación de archivos
RUN kobweb export --notty && \
    if [ -f .kobweb/server/start.sh ]; then chmod +x .kobweb/server/start.sh; fi

# 8. Limpieza final
RUN rm -rf \
    ~/.gradle/caches \
    /root/.cache \
    /tmp/*

#-----------------------------------------------------------------------------
# Etapa final de ejecución
FROM eclipse-temurin:21-jre-jammy as run

ARG KOBWEB_APP_ROOT
WORKDIR /app

COPY --from=export /project/${KOBWEB_APP_ROOT}/.kobweb ./.kobweb

ENV JAVA_TOOL_OPTIONS="-Xmx192m -XX:+UseSerialGC -XX:+UseCompressedClassPointers"

ENTRYPOINT ["/app/.kobweb/server/start.sh"]