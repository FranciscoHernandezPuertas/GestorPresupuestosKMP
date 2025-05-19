#-----------------------------------------------------------------------------
# Variables compartidas entre etapas
ARG KOBWEB_APP_ROOT="site"

#-----------------------------------------------------------------------------
# Etapa de construcción
FROM eclipse-temurin:21-jdk-jammy as export

ENV KOBWEB_CLI_VERSION=0.9.18
ARG KOBWEB_APP_ROOT
ENV NODE_MAJOR=20

# Copiar solo lo esencial primero para cachear dependencias
COPY . /project
RUN rm -rf /project/androidapp

# Instalar dependencias del sistema optimizadas
RUN apt-get update -qq && \
    apt-get install -y --no-install-recommends \
    ca-certificates \
    curl \
    gnupg \
    unzip \
    wget && \
    rm -rf /var/lib/apt/lists/*

# Instalar Node.js
RUN mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg && \
    echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_$NODE_MAJOR.x nodistro main" | tee /etc/apt/sources.list.d/nodesource.list && \
    apt-get update -qq && \
    apt-get install -y nodejs && \
    npm init -y --quiet

# Instalar Playwright mínimo
RUN npx playwright install --with-deps chromium

# Instalar Kobweb CLI
RUN wget -q https://github.com/varabyte/kobweb-cli/releases/download/v${KOBWEB_CLI_VERSION}/kobweb-${KOBWEB_CLI_VERSION}.zip && \
    unzip -q kobweb-${KOBWEB_CLI_VERSION}.zip && \
    rm kobweb-${KOBWEB_CLI_VERSION}.zip

ENV PATH="/kobweb-${KOBWEB_CLI_VERSION}/bin:${PATH}"

WORKDIR /project/${KOBWEB_APP_ROOT}

# Configuración de memoria optimizada
RUN mkdir -p ~/.gradle && \
    echo "org.gradle.jvmargs=-Xmx256m -XX:MaxMetaspaceSize=128m" >> ~/.gradle/gradle.properties && \
    echo "kotlin.daemon.jvmargs=-Xmx256m" >> ~/.gradle/gradle.properties && \
    echo "org.gradle.parallel=false" >> ~/.gradle/gradle.properties && \
    echo "org.gradle.daemon=false" >> ~/.gradle/gradle.properties && \
    echo "org.gradle.workers.max=1" >> ~/.gradle/gradle.properties

# Build con optimizaciones de memoria (sin --log-level)
RUN kobweb export --notty

# Verificar y preparar archivo de inicio
RUN if [ -f .kobweb/server/start.sh ]; then chmod +x .kobweb/server/start.sh; fi

#-----------------------------------------------------------------------------
# Etapa final de ejecución
FROM eclipse-temurin:21-jre-jammy as run

ARG KOBWEB_APP_ROOT
WORKDIR /app

COPY --from=export /project/${KOBWEB_APP_ROOT}/.kobweb ./.kobweb

ENV JAVA_TOOL_OPTIONS="-Xmx256m -XX:+UseSerialGC -XX:MaxRAM=512m"

ENTRYPOINT ["/app/.kobweb/server/start.sh"]