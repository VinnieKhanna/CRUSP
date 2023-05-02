CREATE TABLE IF NOT EXISTS nr (
    telephony_id bigint NOT NULL,
    asu integer,
    nci bigint,
    dbm integer,
    device_id varchar(255),
    nrarfcn integer,
    mcc varchar(255),
    mnc varchar(255),
    operator varchar(255),
    operator_alpha_long varchar(255),
    pci integer,
    csisinr integer,
    csirsrp integer,
    csirsrq integer,
    sssinr integer,
    ssrsrp integer,
    ssrsrq integer,
    tac integer,
    lat numeric,
    lng numeric,
    speed numeric
);