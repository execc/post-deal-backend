create table VC_DRAFT
(
    ID varchar(100) not null
        constraint vc_draft_pkey
            primary key,
    "data" text not null,
    issued boolean not null,
    jwt text
);

create table VC_DRAFT_PARTICIPANT
(
    ID varchar(100) not null
        constraint vc_draft_participant_pkey
            primary key,
    PARTICIPANT varchar(100) not null
);

create table VC_DRAFT_SIGNATURE
(
    ID varchar(100) not null
        constraint vc_draft_signature_pkey
            primary key,
    signatures_key varchar(100) not null,
    SIGNATURE text not null
);