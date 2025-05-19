###############################################################################
# Stage 1: Build & Export Kobweb Site
###############################################################################

# Base image for building
ARG KOBWEB_APP_ROOT="site"
FROM eclipse-temurin:21 AS export

# Kobweb CLI version and Node setup
ENV KOBWEB_CLI_VERSION=0.9.18
ARG KOBWEB_APP_ROOT
ENV NODE_MAJOR=20

# Copy project and remove Android module
WORKDIR /project
COPY . .
RUN rm -rf androidapp

# Install OS deps, Node.js & Playwright
RUN apt-get update \
    && apt-get install -y ca-certificates curl gnupg unzip wget procps \
    && mkdir -p /etc/apt/keyrings \
    && curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key \
       | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg \
    && echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_${NODE_MAJOR}.x nodistro main" \
       | tee /etc/apt/sources.list.d/nodesource.list \
    && apt-get update \
    && apt-get install -y nodejs \
    && npm init -y \
    && npx playwright install --with-deps chromium

# Install Kobweb CLI
RUN wget https://github.com/varabyte/kobweb-cli/releases/download/v${KOBWEB_CLI_VERSION}/kobweb-${KOBWEB_CLI_VERSION}.zip \
    && unzip kobweb-${KOBWEB_CLI_VERSION}.zip \
    && rm kobweb-${KOBWEB_CLI_VERSION}.zip
ENV PATH="/kobweb-${KOBWEB_CLI_VERSION}/bin:${PATH}"

# Configure Gradle/Kotlin to use minimal memory & single worker
WORKDIR /project/${KOBWEB_APP_ROOT}
RUN mkdir -p ~/.gradle \
    && echo "org.gradle.jvmargs=-Xmx384m -XX:MaxPermSize=128m" >> ~/.gradle/gradle.properties \
    && echo "kotlin.daemon.jvmargs=-Xmx256m -Xss512k" >> ~/.gradle/gradle.properties \
    && echo "org.gradle.workers.max=1" >> ~/.gradle/gradle.properties \
    && echo "kotlin.js.ir.incremental=false" >> ~/.gradle/gradle.properties

# Build & export the Kobweb site (no --release flag)
RUN kobweb export --notty

# Ensure start script is executable (relative to WORKDIR)
RUN chmod +x .kobweb/server/start.sh


###############################################################################
# Stage 2: Runtime Image
###############################################################################
FROM eclipse-temurin:21 AS run
ARG KOBWEB_APP_ROOT
WORKDIR /app

# Copy only the exported site artifacts
COPY --from=export /project/${KOBWEB_APP_ROOT}/.kobweb ./.kobweb

# Limit Java memory for runtime
ENV JAVA_TOOL_OPTIONS="-Xmx512m"

# Ensure start script is executable
RUN chmod +x .kobweb/server/start.sh

# Launch the Kobweb server
ENTRYPOINT ["/app/.kobweb/server/start.sh"]
