FROM gradle:6.0.1-jdk8 as builder
USER root
COPY . .
ARG apiVersion
RUN gradle --no-daemon -PapiVersion=${apiVersion} build

FROM gcr.io/distroless/java:8
ENV JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError
COPY --from=builder /home/gradle/build/deps/external/*.jar /data/
COPY --from=builder /home/gradle/build/deps/fint/*.jar /data/
COPY --from=builder /home/gradle/build/libs/fint-p360-rpc-arkiv-adapter-*.jar /data/fint-p360-rpc-arkiv-adapter.jar
CMD ["/data/fint-p360-rpc-arkiv-adapter.jar"]
