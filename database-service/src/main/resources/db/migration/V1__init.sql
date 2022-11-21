CREATE TABLE IF NOT EXISTS measurement_result (
    measurement_id bigint NOT NULL,
    available_bandwidth real NOT NULL,
    error_message varchar (255),
    error_type smallint NOT NULL,
    num_received_packets integer NOT NULL,
    start_time numeric(19,0) NOT NULL,
    settings_id bigint,
    telephony_id bigint,
    downlink boolean,
    PRIMARY KEY (measurement_id)
);

CREATE TABLE IF NOT EXISTS sequence_details (
     sequence_id bigint NOT NULL,
     expected_packets smallint NOT NULL,
     naive_rate real NOT NULL,
     seq_start_time numeric(19,0) NOT NULL,
     measurement_id bigint,
     PRIMARY KEY (sequence_id),
     FOREIGN KEY (measurement_id) REFERENCES measurement_result (measurement_id)
);

CREATE TABLE IF NOT EXISTS received_packet_details (
    packet_id bigint NOT NULL,
    delta_to_start_time bigint NOT NULL,
    packet_nr smallint NOT NULL,
    recv_bytes smallint NOT NULL,
    repeat_nr smallint NOT NULL,
    sequence_id bigint,
    PRIMARY KEY (packet_id),
    FOREIGN KEY (sequence_id) REFERENCES sequence_details (sequence_id)
);

CREATE TABLE IF NOT EXISTS lte (
     telephony_id bigint NOT NULL,
     asu integer,
     ci integer,
     cqi integer,
     dbm integer,
     device_id varchar(255),
     earfcn integer,
     mcc varchar(255),
     mnc varchar(255),
     operator varchar(255),
     operator_alpha_long varchar(255),
     pci integer,
     rsrp integer,
     rsrq integer,
     rssnr integer,
     ta integer,
     tac integer,
     lat numeric,
     lng numeric,
     speed numeric
);

CREATE TABLE IF NOT EXISTS gsm (
    telephony_id bigint NOT NULL,
    arfcn integer,
    asu integer,
    bsic integer,
    cid integer,
    dbm integer,
    device_id varchar(255),
    lac integer,
    mcc varchar(255),
    mnc varchar(255),
    operator varchar(255),
    operator_alpha_long varchar(255),
    ta integer,
    lat numeric,
    lng numeric,
    speed numeric
);

CREATE TABLE IF NOT EXISTS wcdma (
    telephony_id bigint NOT NULL,
    asu integer,
    cid integer,
    dbm integer,
    device_id varchar(255),
    lac integer,
    mcc varchar(255),
    mnc varchar(255),
    operator varchar(255),
    operator_alpha_long varchar(255),
    psc integer,
    uarfcn integer,
    lat numeric,
    lng numeric,
    speed numeric
);

CREATE TABLE IF NOT EXISTS wifi (
    telephony_id bigint NOT NULL,
    asu integer,
    dbm integer,
    device_id varchar(255),
    operator varchar(255),
    lat numeric,
    lng numeric,
    speed numeric
);

CREATE TABLE IF NOT EXISTS cdma (
    telephony_id bigint NOT NULL,
    asu integer,
    basestation_id integer,
    dbm integer,
    device_id varchar(255),
    bs_lat integer,
    bs_lng integer,
    network_id integer,
    operator varchar(255),
    operator_alpha_long varchar(255),
    rssi integer,
    system_id integer,
    lat numeric,
    lng numeric,
    speed numeric
);

CREATE SEQUENCE IF NOT EXISTS hibernate_sequence START 1;
