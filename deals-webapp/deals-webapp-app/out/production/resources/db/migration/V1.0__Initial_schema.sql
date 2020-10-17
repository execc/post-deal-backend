-- Transaction tracker required tables
-- This table keep information about tracked (sent) transaction
create table tx
(
    id varchar(100) not null
        constraint tx_pkey
            primary key,
    contract_id varchar(100) default ''::character varying not null,
    type integer not null,
    created timestamp not null,
    status varchar(30) not null,
    tx_body text not null,
    meta jsonb,
    version integer default 1 not null,
    business_object_id varchar(255)
);

create index tx_contract_id__idx
    on tx (contract_id);

create index tx_contract_id__prt0__idx
    on tx (contract_id)
    where ((status)::text <> 'SUCCESS'::text);

create index tx_business_object_id_idx
    on tx (business_object_id);

-- This table keeps information about create smart contracts
create table smart_contract_info
(
    id varchar(255) not null
        constraint smart_contract_info_pkey
            primary key,
    image_hash varchar(255) not null,
    image varchar(255) not null,
    version integer not null,
    contract_name varchar(255) not null,
    sender varchar(255) not null,
    created timestamp,
    modified timestamp
);

create index smart_contract_info_image_idx
    on smart_contract_info (image);

create index smart_contract_info_image_hash_idx
    on smart_contract_info (image_hash);

create index smart_contract_info_sender_idx
    on smart_contract_info (sender);

create index smart_contract_contract_name_idx
    on smart_contract_info (contract_name);

-- Transaction Observer required table
-- This table implements queue partitioning mechanics
create table tx_queue_partition
(
    id varchar(255) not null
        constraint tx_queue_partition_pkey
            primary key,
    priority integer not null,
    last_enqueued_tx_timestamp timestamp,
    last_enqueued_tx_id varchar(255) not null,
    last_read_tx_id varchar(255),
    created timestamp,
    modified timestamp
);

create index ix__tx_queue_partition__priority_timestamp_partial
    on tx_queue_partition (priority desc, last_enqueued_tx_timestamp asc)
    where (((last_read_tx_id)::text <> (last_enqueued_tx_id)::text) OR (last_read_tx_id IS NULL));

-- This table tracks transaction statuses
create table tx_status
(
    tx_id varchar(100) not null
        constraint tx_status_tx_id_fkey
            references tx,
    status varchar(30) not null,
    created timestamp not null,
    constraint tx_status_pk
        primary key (tx_id, status)
);

-- This table keeps info about transaction tracking status
create table tx_track_info
(
    id varchar(255) not null,
    contract_id varchar(255) not null
        constraint fk_contract_id
            references smart_contract_info,
    status varchar(255) not null,
    type integer not null,
    body jsonb not null,
    errors jsonb,
    created timestamp,
    modified timestamp
);

create index tx_track_info_contract_id_idx
    on tx_track_info (contract_id);

-- This table implements transaction queue
create table enqueued_tx
(
    id varchar(255) not null
        constraint enqueued_tx_pkey
            primary key,
    status varchar(255) not null,
    body jsonb not null,
    block_height bigint not null,
    position_in_block integer not null,
    tx_timestamp timestamp,
    created timestamp,
    modified timestamp,
    partition_id varchar(255) not null
        constraint fk_partition_id
            references tx_queue_partition
);

create index enqueued_tx_created_idx
    on enqueued_tx (created);

create index ix__enqueued_tx__block_height_position_partition_status
    on enqueued_tx (block_height, position_in_block, partition_id, status);

-- This table keeps information about current block being tracked
create table block_height_info
(
    id uuid not null
        constraint block_height_info_pkey
            primary key,
    created_timestamp timestamp,
    current_height bigint not null,
    node_alias varchar(255)
        constraint block_height_node_alias_unique
            unique,
    update_timestamp timestamp,
    version bigint not null
);

-- This table enables parallel execution of 2 or more back end apps
create table shedlock
(
    name varchar(64) not null
        constraint shedlock_pkey
            primary key,
    lock_until timestamp(3),
    locked_at timestamp(3),
    locked_by varchar(255)
);
