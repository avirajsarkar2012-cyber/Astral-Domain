FROM eclipse-temurin:21-jdk-jammy

WORKDIR /server

# Install necessary utilities
RUN apt-get update && apt-get install -y wget curl jq && rm -rf /var/lib/apt/lists/*

# Download latest stable PaperMC build dynamically
RUN PAPER_BUILD=$(curl -s https://api.papermc.io/v2/projects/paper/versions/1.20.4 | jq '.builds[-1]') && \
    wget https://api.papermc.io/v2/projects/paper/versions/1.20.4/builds/${PAPER_BUILD}/downloads/paper-1.20.4-${PAPER_BUILD}.jar -O server.jar

# Setup plugins folder and download Geyser & Floodgate
RUN mkdir -p plugins
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