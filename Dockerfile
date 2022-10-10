FROM gradle:7.5.1-jdk17 as builder
USER root
COPY . .
ARG USERNAME
ARG TOKEN
RUN gradle --no-daemon -Pgpr.user=${USERNAME} -Pgpr.key=${TOKEN} build

FROM gcr.io/distroless/java17
ENV JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError
WORKDIR /app
COPY --from=builder /home/gradle/build/libs/fint-p360-rpc-arkiv-adapter.jar /app/fint-p360-rpc-arkiv-adapter.jar
CMD ["/app/fint-p360-rpc-arkiv-adapter.jar"]
