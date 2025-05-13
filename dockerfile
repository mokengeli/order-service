# Étape de build
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app

# Copie et installation des dépendances
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copie du code source et compilation
COPY src ./src
RUN mvn package -DskipTests

# Extraction dynamique de la version
RUN APP_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout) \
    && echo "Version extraite: $APP_VERSION" \
    && cp target/order-service-$APP_VERSION.jar target/app.jar

# Étape finale
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Création d'un utilisateur non-root
RUN addgroup --system appuser && adduser --system --ingroup appuser appuser
USER appuser:appuser

# Copie du JAR compilé
COPY --from=build /app/target/app.jar ./app.jar

# Copie de tous les fichiers de configuration
COPY src/main/resources/application*.yml ./config/

# Exposer le port pour l'API REST et WebSocket
EXPOSE 8080

# Variables d'environnement REQUISES
ENV SERVER_PORT="" \
    JWT_SECRET="" \
    POSTGRES_URL="" \
    POSTGRES_USER="" \
    POSTGRES_PASSWORD="" \
    EUREKA_SERVER_URL="" \
    ALLOWED_ORIGINS="" \
    IS_SSL=""

# Variables d'environnement OPTIONNELLES
ENV TIME_ZONE="GMT+01:00" \
    POSTGRES_SCHEMA="order_schema" \
    USER_SERVICE_ID="user-service" \
    INVENTORY_SERVICE_ID="inventory-service" \
    ACTUATOR_ENDPOINTS="health,info,metrics,websocket,loggers"

# Point d'entrée
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:./config/"]

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD wget -q --spider http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1