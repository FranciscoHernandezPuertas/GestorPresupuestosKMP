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

# Instalar dependencias mínimas
RUN apt-get update -qq && \
    apt-get install -y --no-install-recommends \
    curl \
    gnupg \
    unzip && \
    rm -rf /var/lib/apt/lists/*

# Instalar Node.js mínimo
RUN mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg && \
    echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_$NODE_MAJOR.x nodistro main" | tee /etc/apt/sources.list.d/nodesource.list && \
    apt-get update -qq && \
    apt-get install -y --no-install-recommends nodejs && \
    npm config set update-notifier false

# Instalar Playwright mínimo
RUN npx playwright install --with-deps chromium

# Instalar Kobweb CLI
RUN curl -sLO https://github.com/varabyte/kobweb-cli/releases/download/v${KOBWEB_CLI_VERSION}/kobweb-${KOBWEB_CLI_VERSION}.zip && \
    unzip -q kobweb-${KOBWEB_CLI_VERSION}.zip && \
    rm kobweb-${KOBWEB_CLI_VERSION}.zip

ENV PATH="/kobweb-${KOBWEB_CLI_VERSION}/bin:${PATH}"

WORKDIR /project/${KOBWEB_APP_ROOT}

# Configuración extrema de memoria para 512MB
RUN mkdir -p ~/.gradle && \
    echo "org.gradle.jvmargs=-Xmx128m -XX:MaxMetaspaceSize=96m -XX:+UseSerialGC -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UnlockExperimentalVMOptions -XX:-UseContainerSupport" >> ~/.gradle/gradle.properties && \
    echo "kotlin.daemon.jvmargs=-Xmx96m" >> ~/.gradle/gradle.properties && \
    echo "org.gradle.parallel=false" >> ~/.gradle/gradle.properties && \
    echo "org.gradle.daemon=false" >> ~/.gradle/gradle.properties && \
    echo "org.gradle.workers.max=1" >> ~/.gradle/gradle.properties && \
    echo "org.gradle.caching=true" >> ~/.gradle/gradle.properties

# Build con máxima optimización
RUN kobweb export --notty --stacktrace --no-daemon --offline

# Limpieza extrema post-build
RUN rm -rf \
    ~/.gradle/caches \
    ~/.npm \
    /root/.cache \
    /tmp/*

# Preparar archivo de inicio
RUN chmod +x .kobweb/server/start.sh

#-----------------------------------------------------------------------------
# Etapa final de ejecución
FROM eclipse-temurin:21-jre-jammy as run

ARG KOBWEB_APP_ROOT
WORKDIR /app

COPY --from=export /project/${KOBWEB_APP_ROOT}/.kobweb ./.kobweb

ENV JAVA_TOOL_OPTIONS="-Xmx192m -XX:+UseSerialGC -XX:MaxRAM=512m -XX:+UseCompressedClassPointers -XX:+UseCompressedOops"

ENTRYPOINT ["/app/.kobweb/server/start.sh"]