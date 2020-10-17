create table did
(
    public_key varchar(100) not null
        constraint did_pkey
            primary key,
    created timestamp not null,
    modified timestamp not null,
    login varchar(50) not null,
    status varchar(30) not null,
    password_hash varchar(255) not null,
    phone_number varchar(255) not null,
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    patronymic varchar(255) not null,
    activation_code varchar(255) not null,
    biometric_public_key varchar(255) not null,
    version integer default 1 not null
);