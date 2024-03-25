CREATE TYPE public.enum_credential_status_list_purpose AS ENUM (
    'Revocation',
    'Suspension');


CREATE TABLE public.credential_status_lists
(
    id           UUID PRIMARY KEY                                    default gen_random_uuid(),
    wallet_id    UUID                                       NOT NULL,
    issuer       VARCHAR                                    NOT NULL,
    issued       TIMESTAMP WITH TIME ZONE                   NOT NULL,
    purpose      public.enum_credential_status_list_purpose NOT NULL,
    encoded_list TEXT                                       NOT NULL,
    proof        JSON                                       NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE                   NOT NULL default now(),
    updated_at   TIMESTAMP WITH TIME ZONE                   NOT NULL default now()
);

CREATE INDEX credential_status_lists_wallet_id_index ON public.credential_status_lists (wallet_id);


CREATE TABLE public.credentials_in_status_list
(
    id                         UUID PRIMARY KEY                  default gen_random_uuid(),
    issue_credential_record_id VARCHAR(64)              NOT NULL,
    credential_status_list_id  UUID                     NOT NULL,
    status_list_index          INTEGER                  NOT NULL,
--  is revoked or suspended
    is_canceled                BOOLEAN                  NOT NULL default false,
    created_at                 TIMESTAMP WITH TIME ZONE NOT NULL default now(),
    updated_at                 TIMESTAMP WITH TIME ZONE NOT NULL default now(),

    CONSTRAINT issue_credential_record_id_fkey FOREIGN KEY (issue_credential_record_id) REFERENCES public.issue_credential_records (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT credential_status_list_id_fkey FOREIGN KEY (credential_status_list_id) REFERENCES public.credential_status_lists (id) ON DELETE CASCADE ON UPDATE CASCADE
);

