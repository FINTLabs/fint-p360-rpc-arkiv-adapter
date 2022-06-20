FROM gradle:6.8.1-jdk8 as builder
USER root
COPY . .
RUN gradle --no-daemon build

FROM gcr.io/distroless/java:8
ENV JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError
COPY --from=builder /home/gradle/build/deps/external/*.jar /data/
COPY --from=builder /home/gradle/build/deps/fint/*.jar /data/
COPY --from=builder /home/gradle/build/libs/fint-p360-rpc-arkiv-adapter-*.jar /data/fint-p360-rpc-arkiv-adapter.jar
CMD ["/data/fint-p360-rpc-arkiv-adapter.jar"]
