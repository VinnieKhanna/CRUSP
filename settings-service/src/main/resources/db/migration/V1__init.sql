-- noinspection SqlNoDataSourceInspectionForFile

CREATE TABLE crusp_settings (
    settings_id bigint NOT NULL,
    packet_size integer NOT NULL,
    rate real NOT NULL,
    repeats integer NOT NULL,
    sleep integer NOT NULL,
    timeout integer NOT NULL,
    volume integer NOT NULL,
    standard boolean NOT NULL,
    PRIMARY KEY (settings_id),
    CONSTRAINT unique_setting UNIQUE (repeats, volume, packet_size, rate, sleep, timeout)
);

CREATE SEQUENCE hibernate_sequence START 1;
