FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

COPY pom.xml ./
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests \
    && mv "$(find target -maxdepth 1 -type f -name '*.jar' ! -name 'original-*.jar' | head -n 1)" /build/app.jar


FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN useradd --system --create-home acervo \
    && mkdir -p /app/uploads \
    && chown -R acervo:acervo /app

COPY --from=build --chown=acervo:acervo /build/app.jar /app/app.jar

USER acervo

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]