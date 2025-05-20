#-----------------------------------------------------------------------------
# Variables compartidas entre etapas
ARG KOBWEB_APP_ROOT="site"
#-----------------------------------------------------------------------------
# Etapa de construcción
FROM eclipse-temurin:21-jdk-jammy AS build
ARG KOBWEB_APP_ROOT
ENV KOBWEB_CLI_VERSION=0.9.18 \
    NODE_MAJOR=20

# 1. Copiar código fuente y eliminar elementos innecesarios
WORKDIR /src
COPY . .
RUN rm -rf androidapp .gradle .idea

# 2. Instalar dependencias del sistema (incluyendo las requeridas por Playwright)
RUN apt-get update -qq && \
    apt-get install -y --no-install-recommends \
      curl gnupg unzip ca-certificates \
      # Dependencias de Playwright
      libglib2.0-0 \
      libnss3 \
      libnspr4 \
      libdbus-1-3 \
      libatk1.0-0 \
      libatk-bridge2.0-0 \
      libatspi2.0-0 \
      libx11-6 \
      libxcomposite1 \
      libxdamage1 \
      libxext6 \
      libxfixes3 \
      libxrandr2 \
      libgbm1 \
      libxcb1 \
      libxkbcommon0 \
      libasound2 \
      # Dependencias básicas
      && \
    rm -rf /var/lib/apt/lists/*

# 3. Instalar Node.js optimizado
RUN mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg && \
    echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_${NODE_MAJOR}.x nodistro main" > /etc/apt/sources.list.d/nodesource.list && \
    apt-get update -qq && \
    apt-get install -y --no-install-recommends nodejs && \
    npm install -g npm@latest && \
    rm -rf /var/lib/apt/lists/*

# 4. Instalar Kobweb CLI minimizando espacio
RUN curl -sLO https://github.com/varabyte/kobweb-cli/releases/download/v${KOBWEB_CLI_VERSION}/kobweb-${KOBWEB_CLI_VERSION}.zip && \
    unzip -q kobweb-${KOBWEB_CLI_VERSION}.zip -d /kobweb-cli && \
    rm kobweb-${KOBWEB_CLI_VERSION}.zip
ENV PATH="/kobweb-cli/kobweb-${KOBWEB_CLI_VERSION}/bin:${PATH}"

# 5. Optimizar memoria para Gradle (ajustado para 512MB)
ENV GRADLE_OPTS="-Xmx128m -XX:MaxMetaspaceSize=64m -XX:+UseSerialGC -XX:+UseCompressedClassPointers -Dorg.gradle.daemon=false -Dorg.gradle.parallel=false -Dorg.gradle.workers.max=1"

# 6. Construir proyecto con limpieza progresiva
WORKDIR /src/${KOBWEB_APP_ROOT}
RUN kobweb export --layout fullstack --notty && \
    # Limpiar durante el build para ahorrar espacio
    rm -rf ~/.gradle/caches ~/.m2 ~/.npm /root/.cache /tmp/*

#-----------------------------------------------------------------------------
# Etapa de ejecución ultra ligera
FROM eclipse-temurin:21-jre-jammy AS run
ARG KOBWEB_APP_ROOT
WORKDIR /app

# 7. Copiar solo lo esencial del build
COPY --from=build --chown=1001:0 /src/${KOBWEB_APP_ROOT}/.kobweb ./.kobweb

# 8. Configurar runtime seguro para Render
USER 1001
ENV JAVA_TOOL_OPTIONS="-Xmx160m -XX:MaxRAMPercentage=70 -XX:+UseSerialGC -XX:+UseCompressedClassPointers -Dfile.encoding=UTF-8"
EXPOSE 8080

# 9. Entrypoint optimizado sin overhead
ENTRYPOINT ["/app/.kobweb/server/start.sh"]