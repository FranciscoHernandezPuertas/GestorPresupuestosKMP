#-----------------------------------------------------------------------------
ARG KOBWEB_APP_ROOT="site"

#-----------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-jammy as export

ENV KOBWEB_CLI_VERSION=0.9.18
ARG KOBWEB_APP_ROOT
ENV NODE_MAJOR=20

# Configuración crítica de memoria desde el inicio
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.workers.max=1"
ENV JAVA_TOOL_OPTIONS="-Xmx200m -XX:MaxMetaspaceSize=100m -XX:+UseSerialGC -XX:+HeapDumpOnOutOfMemoryError"
ENV NODE_OPTIONS="--max-old-space-size=128"

# Copiar proyecto y eliminar módulo Android
COPY . /project
RUN rm -rf /project/androidapp

# Instalación mínima de dependencias
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates curl unzip wget \
    && rm -rf /var/lib/apt/lists/*

# Instalar Node.js mínimo
RUN mkdir -p /etc/apt/keyrings \
    && curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg \
    && echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_$NODE_MAJOR.x nodistro main" > /etc/apt/sources.list.d/nodesource.list \
    && apt-get update && apt-get install -y nodejs \
    && npm install -g npm@latest \
    && npm install playwright-chromium

# Instalar Kobweb CLI
RUN wget -q https://github.com/varabyte/kobweb-cli/releases/download/v${KOBWEB_CLI_VERSION}/kobweb-${KOBWEB_CLI_VERSION}.zip \
    && unzip kobweb-${KOBWEB_CLI_VERSION}.zip \
    && rm kobweb-${KOBWEB_CLI_VERSION}.zip

ENV PATH="/kobweb-${KOBWEB_CLI_VERSION}/bin:${PATH}"

WORKDIR /project/${KOBWEB_APP_ROOT}

# Configuración extrema de Gradle/Kotlin
RUN mkdir -p ~/.gradle && \
    echo "org.gradle.jvmargs=-Xmx200m -XX:MaxMetaspaceSize=100m" >> ~/.gradle/gradle.properties && \
    echo "kotlin.daemon.jvmargs=-Xmx150m" >> ~/.gradle/gradle.properties && \
    echo "kotlin.incremental=false" >> ~/.gradle/gradle.properties && \
    echo "org.gradle.parallel=false" >> ~/.gradle/gradle.properties && \
    echo "org.gradle.caching=false" >> ~/.gradle/gradle.properties

# Limpieza agresiva de cache
RUN ./gradlew clean --no-daemon

# Construcción segmentada para reducir presión de memoria
RUN ./gradlew compileKotlinJs --no-daemon
RUN ./gradlew jsMainClasses --no-daemon
RUN kobweb export --notty --no-daemon

#-----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy as run

ARG KOBWEB_APP_ROOT

WORKDIR /app

COPY --from=export /project/${KOBWEB_APP_ROOT}/.kobweb ./.kobweb

# Configuración de ejecución para 512MB
ENV JAVA_TOOL_OPTIONS="-Xmx300m -XX:MaxMetaspaceSize=150m -XX:+UseSerialGC"
RUN chmod +x .kobweb/server/start.sh

ENTRYPOINT ["/app/.kobweb/server/start.sh"]