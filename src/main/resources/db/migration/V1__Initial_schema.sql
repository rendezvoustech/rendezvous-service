CREATE TABLE rendezvous (
                        id                  BIGSERIAL PRIMARY KEY NOT NULL,
                        owner_id            integer NOT NULL,
                        name                varchar(255),
                        participant_ids     integer[],
                        status              varchar(255),
                        created_date       timestamp NOT NULL,
                        last_modified_date  timestamp NOT NULL,
                        version             integer NOT NULL
);