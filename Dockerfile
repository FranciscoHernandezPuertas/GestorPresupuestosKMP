#-----------------------------------------------------------------------------
# Variables are shared across multiple stages (they need to be explicitly
# opted into each stage by being declaring there too, but their values need
# only be specified once).
ARG KOBWEB_APP_ROOT="site"

#-----------------------------------------------------------------------------
# Create an intermediate stage which builds and exports our site. In the
# final stage, we'll only extract what we need from this stage, saving a lot
# of space.
FROM eclipse-temurin:21 as export

ENV KOBWEB_CLI_VERSION=0.9.18
ARG KOBWEB_APP_ROOT
ENV NODE_MAJOR=20

# Copy project files excluding Android module
COPY . /project

# Remove Android module explicitly
RUN rm -rf /project/androidapp

# Install system dependencies
RUN apt-get update \
    && apt-get install -y ca-certificates curl gnupg unzip wget \
    && mkdir -p /etc/apt/keyrings \
    && curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg \
    && echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_$NODE_MAJOR.x nodistro main" | tee /etc/apt/sources.list.d/nodesource.list \
    && apt-get update \
    && apt-get install -y nodejs \
    && npm init -y \
    && npx playwright install --with-deps chromium

# Install Kobweb CLI
RUN wget https://github.com/varabyte/kobweb-cli/releases/download/v${KOBWEB_CLI_VERSION}/kobweb-${KOBWEB_CLI_VERSION}.zip \
    && unzip kobweb-${KOBWEB_CLI_VERSION}.zip \
    && rm kobweb-${KOBWEB_CLI_VERSION}.zip

ENV PATH="/kobweb-${KOBWEB_CLI_VERSION}/bin:${PATH}"

WORKDIR /project/${KOBWEB_APP_ROOT}

# Configure Gradle memory settings
RUN mkdir -p ~/.gradle && \
    echo "org.gradle.jvmargs=-Xmx325m" >> ~/.gradle/gradle.properties

# Build and export the Kobweb site
RUN kobweb export --notty

# Fix permissions for start script
RUN chmod +x /project/${KOBWEB_APP_ROOT}/.kobweb/server/start.sh

#-----------------------------------------------------------------------------
# Create the final stage, which contains just enough bits to run the Kobweb
# server.
FROM eclipse-temurin:21 as run

ARG KOBWEB_APP_ROOT

WORKDIR /app

# Copy exported artifacts
COPY --from=export /project/${KOBWEB_APP_ROOT}/.kobweb ./.kobweb

# Set memory limits and verify permissions
ENV JAVA_TOOL_OPTIONS="-Xmx512m"
RUN chmod +x .kobweb/server/start.sh

ENTRYPOINT ["/app/.kobweb/server/start.sh"]