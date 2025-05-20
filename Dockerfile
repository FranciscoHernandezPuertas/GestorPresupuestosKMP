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

# 2. Dependencias mínimas + librerías para Playwright
RUN apt-get update -qq && \
    apt-get install -y --no-install-recommends \
      curl \
      gnupg \
      unzip \
      ca-certificates \
      # Librerías requeridas por Playwright para navegadores \
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
      libasound2 && \
    rm -rf /var/lib/apt/lists/*

# 3. Instalar Node.js (corrigiendo la URL del repositorio)
RUN mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key  \
      | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg && \
    echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_ ${NODE_MAJOR}.x nodistro main" \
      | tee /etc/apt/sources.list.d/nodesource.list && \
    apt-get update -qq && \
    apt-get install -y --no-install-recommends nodejs && \
    rm -rf /var/lib/apt/lists/*

# 4. Instalar Kobweb CLI
RUN curl -sLO https://github.com/varabyte/kobweb-cli/releases/download/v ${KOBWEB_CLI_VERSION}/kobweb-${KOBWEB_CLI_VERSION}.zip && \
    unzip -q kobweb-${KOBWEB_CLI_VERSION}.zip -d /kobweb-cli && \
    rm kobweb-${KOBWEB_CLI_VERSION}.zip
ENV PATH="/kobweb-cli/kobweb-${KOBWEB_CLI_VERSION}/bin:${PATH}"

# 5. Ajustes de memoria para Gradle (compatible con 512MB)
ENV GRADLE_OPTS="-Xmx128m -XX:MaxMetaspaceSize=48m -XX:+UseSerialGC -XX:+UseCompressedClassPointers \
  -Dorg.gradle.daemon=false -Dorg.gradle.parallel=false -Dorg.gradle.workers.max=1"

# 6. Exportar sitio en modo producción
WORKDIR /src/${KOBWEB_APP_ROOT}
RUN kobweb export --layout fullstack --notty

# 7. Limpiar cachés intermedios
RUN rm -rf ~/.gradle ~/.m2 ~/.npm ~/.config ~/.local /root/.cache /tmp/*

#-----------------------------------------------------------------------------
# Etapa de ejecución
FROM eclipse-temurin:21-jre-jammy AS run

ARG KOBWEB_APP_ROOT
WORKDIR /app

# 8. Copiar solo el resultado exportado
COPY --from=build /src/${KOBWEB_APP_ROOT}/.kobweb ./.kobweb

# 9. Variables para runtime (ajustadas a 512MB)
ENV JAVA_TOOL_OPTIONS="-Xmx192m -XX:MaxRAMPercentage=50 -XX:+UseSerialGC -XX:+UseCompressedClassPointers"

# Script de inicio con variables de entorno
RUN echo '#!/bin/bash\n\
# Cargar variables de entorno\nsource /app/.env 2>/dev/null || true\n\
# Fallback para MONGODB_URI\nexport MONGODB_URI=${MONGODB_URI:-"mongodb+srv://fallback:password@localhost/gestor_db"}\n\
echo "Using MONGODB_URI: $MONGODB_URI"\n\
exec /app/.kobweb/server/start.sh "$@"' > /app/start-wrapper.sh && \
  chmod +x /app/start-wrapper.sh

EXPOSE 8080
ENTRYPOINT ["/app/start-wrapper.sh"]