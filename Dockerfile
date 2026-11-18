FROM eclipse-temurin:17-jdk AS builder
COPY . .
RUN ./gradlew --no-daemon build

FROM eclipse-temurin:17-jre
ENV JAVA_TOOL_OPTIONS="-XX:+ExitOnOutOfMemoryError -XX:-UseBASE64Intrinsics"
WORKDIR /app
COPY --from=builder build/libs/fint-p360-rpc-arkiv-adapter.jar /app/fint-p360-rpc-arkiv-adapter.jar

USER 1337
ENTRYPOINT ["java", "-jar"]
CMD ["/app/fint-p360-rpc-arkiv-adapter.jar"]
