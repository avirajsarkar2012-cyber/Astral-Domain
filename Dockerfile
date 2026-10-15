FROM maven:3.9-eclipse-temurin-21 AS plugin-build

WORKDIR /build
COPY plugin/pom.xml .
COPY plugin/src ./src
RUN mvn -B package

FROM eclipse-temurin:21-jdk-jammy

WORKDIR /server

# Install necessary utilities
RUN apt-get update && apt-get install -y wget curl jq && rm -rf /var/lib/apt/lists/*

# Download latest stable PaperMC build dynamically
RUN PAPER_BUILD=$(curl -s https://api.papermc.io/v2/projects/paper/versions/1.20.4 | jq '.builds[-1]') && \
    wget https://api.papermc.io/v2/projects/paper/versions/1.20.4/builds/${PAPER_BUILD}/downloads/paper-1.20.4-${PAPER_BUILD}.jar -O server.jar

# Setup plugins folder and download Geyser & Floodgate
RUN mkdir -p plugins
COPY --from=plugin-build /build/target/astral-domain-plugin-1.0.0.jar /server/plugins/AstralDomainPlugin.jar
RUN wget https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/spigot -O plugins/Geyser-Spigot.jar
RUN wget https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/spigot -O plugins/Floodgate-Spigot.jar

# Copy local configurations into the server container
COPY eula.txt /server/eula.txt
COPY server.properties /server/server.properties

# Expose ports for Java (TCP) and Bedrock (UDP)
EXPOSE 25565/tcp
EXPOSE 19132/udp

# Launch server with memory allocation
CMD ["java", "-Xms2G", "-Xmx6G", "-jar", "server.jar", "nogui"]