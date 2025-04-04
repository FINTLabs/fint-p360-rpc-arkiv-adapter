FROM gradle:8.13-jdk21 AS builder
USER root
COPY . .
RUN gradle --no-daemon build

FROM gcr.io/distroless/java21
ENV JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError
WORKDIR /app
COPY --from=builder /home/gradle/build/libs/fint-p360-rpc-arkiv-adapter.jar /app/fint-p360-rpc-arkiv-adapter.jar
CMD ["/app/fint-p360-rpc-arkiv-adapter.jar"]
