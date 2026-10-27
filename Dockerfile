FROM gradle:7.5.1-jdk17 AS builder
USER root
COPY . .
RUN gradle --no-daemon build

FROM gcr.io/distroless/java17 AS runtime-base

FROM gradle:7.5.1-jdk17 AS certificates
USER root
COPY --from=runtime-base /etc/ssl/certs/java/cacerts /tmp/truststore.jks
COPY certs/globalsign-root-r46.crt /tmp/globalsign-root-r46.crt
RUN keytool -importcert -noprompt \
    -alias globalsign-root-r46 \
    -file /tmp/globalsign-root-r46.crt \
    -keystore /tmp/truststore.jks \
    -storetype JKS \
    -storepass changeit

FROM runtime-base
ENV JAVA_TOOL_OPTIONS=-XX:+ExitOnOutOfMemoryError
COPY --from=certificates /tmp/truststore.jks /etc/ssl/certs/java/cacerts
WORKDIR /app
COPY --from=builder /home/gradle/build/libs/fint-p360-rpc-arkiv-adapter.jar /app/fint-p360-rpc-arkiv-adapter.jar
CMD ["/app/fint-p360-rpc-arkiv-adapter.jar"]
