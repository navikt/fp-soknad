FROM ghcr.io/navikt/fp-baseimages/distroless:21

LABEL org.opencontainers.image.source=https://github.com/navikt/fp-soknad

COPY target/classes/logback*.xml conf/
COPY target/lib/*.jar lib/
COPY target/app.jar .
