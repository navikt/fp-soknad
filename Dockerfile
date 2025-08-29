FROM ghcr.io/navikt/fp-baseimages/distroless:21

LABEL org.opencontainers.image.source=https://github.com/navikt/fp-soknad

COPY domene/target/classes/logback*.xml conf/
COPY domene/target/lib/*.jar lib/
COPY domene/target/app.jar .

CMD ["app.jar"]
