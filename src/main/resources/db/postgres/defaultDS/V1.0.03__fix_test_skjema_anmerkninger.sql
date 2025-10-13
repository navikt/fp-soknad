ALTER TABLE dokument RENAME CONSTRAINT "dokument_pkey" TO "pk_dokument_id";
ALTER TABLE forsendelse RENAME CONSTRAINT "forsendelse_pkey" TO "pk_forsendelse_id";
ALTER TABLE forsendelse RENAME CONSTRAINT "uk_forsendelse" TO "pk_uk_forsendelse_id";

ALTER INDEX idx_forsendelse_unique_id RENAME TO "uidx__forsendelse_unique_uuid";
