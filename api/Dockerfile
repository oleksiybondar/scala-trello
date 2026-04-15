ARG SBT_IMAGE=sbtscala/scala-sbt:latest

FROM ${SBT_IMAGE} AS build

WORKDIR /workspace

COPY project ./project
COPY build.sbt ./
COPY README.md ./
COPY src ./src

RUN sbt clean stage

FROM eclipse-temurin:17-jre

WORKDIR /opt/api

COPY --from=build /workspace/target/universal/stage/ /opt/api/

EXPOSE 8080

ENTRYPOINT ["./bin/api"]
