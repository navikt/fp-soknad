-- Create a global sequence for primary keys
CREATE SEQUENCE SEQ_GLOBAL_PK
    INCREMENT BY 50
    MINVALUE 2000000
    START WITH 2000000
    NO CYCLE
    CACHE 50;

-- =========================================
-- TABLE: FORSENDELSE
-- =========================================
CREATE TABLE FORSENDELSE (
                             ID BIGINT PRIMARY KEY DEFAULT nextval('SEQ_GLOBAL_PK'),
                             FORSENDELSE_ID UUID NOT NULL,
                             FODSELSNUMMER VARCHAR(255) NOT NULL,
                             SAKSNUMMER VARCHAR(255),
                             JOURNALPOST_ID VARCHAR(255),
                             FORSENDELSE_STATUS VARCHAR(50) NOT NULL,
                             FORSENDELSE_MOTTATT TIMESTAMP NOT NULL
);

-- Unique constraint and index
CREATE UNIQUE INDEX IDX_FORSENDELSE_UNIQUE_ID ON FORSENDELSE (FORSENDELSE_ID);

ALTER TABLE FORSENDELSE
    ADD CONSTRAINT UK_FORSENDELSE UNIQUE (FORSENDELSE_ID);

-- Comments on FORSENDELSE
COMMENT ON TABLE FORSENDELSE IS 'Metadataene til dokumentene som fordeles';
COMMENT ON COLUMN FORSENDELSE.ID IS 'Primærnøkkel';
COMMENT ON COLUMN FORSENDELSE.FORSENDELSE_ID IS 'Unik ID for forsendelsen';
COMMENT ON COLUMN FORSENDELSE.FODSELSNUMMER IS 'ID til avsenderen av et dokument';
COMMENT ON COLUMN FORSENDELSE.SAKSNUMMER IS 'ID til fagsak et dokument knyttes mot';
COMMENT ON COLUMN FORSENDELSE.JOURNALPOST_ID IS 'ID til dokumentet i JOARK';
COMMENT ON COLUMN FORSENDELSE.FORSENDELSE_STATUS IS 'Status på dokumentforsendelse';
COMMENT ON COLUMN FORSENDELSE.FORSENDELSE_MOTTATT IS 'Tidspunktet forsendelsen ble mottatt hos NAV';

-- =========================================
-- TABLE: DOKUMENT
-- =========================================
CREATE TABLE DOKUMENT (
                          ID BIGINT PRIMARY KEY DEFAULT nextval('SEQ_GLOBAL_PK'),
                          FORSENDELSE_ID UUID NOT NULL,
                          DOKUMENT_TYPE_ID VARCHAR(50) NOT NULL,
                          CONTENT BYTEA NOT NULL,
                          ARKIV_FILTYPE VARCHAR(50),
                          BESKRIVELSE VARCHAR(150)
);

-- Comments on DOKUMENT
COMMENT ON TABLE DOKUMENT IS 'Tabell for lagring av dokumenter';
COMMENT ON COLUMN DOKUMENT.ID IS 'Primærnøkkel';
COMMENT ON COLUMN DOKUMENT.FORSENDELSE_ID IS 'Unik ID for forsendelsen dokumentet tilhører';
COMMENT ON COLUMN DOKUMENT.DOKUMENT_TYPE_ID IS 'Type dokument';
COMMENT ON COLUMN DOKUMENT.CONTENT IS 'Dokumentets innhold';
COMMENT ON COLUMN DOKUMENT.ARKIV_FILTYPE IS 'Filtype for arkivering';
COMMENT ON COLUMN DOKUMENT.BESKRIVELSE IS 'Beskrivelse av dokumentet';

-- =========================================
-- CONSTRAINT & INDEX RENAMES
-- =========================================
ALTER TABLE DOKUMENT RENAME CONSTRAINT dokument_pkey TO pk_dokument_id;
ALTER TABLE FORSENDELSE RENAME CONSTRAINT forsendelse_pkey TO pk_forsendelse_id;
ALTER TABLE FORSENDELSE RENAME CONSTRAINT uk_forsendelse TO pk_uk_forsendelse_id;

ALTER INDEX idx_forsendelse_unique_id RENAME TO uidx__forsendelse_unique_uuid;
