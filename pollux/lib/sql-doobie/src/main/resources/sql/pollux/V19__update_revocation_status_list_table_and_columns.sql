ALTER TABLE public.credential_status_lists
    DROP COLUMN encoded_list,
    DROP COLUMN proof;

ALTER TABLE public.credential_status_lists
    ADD COLUMN status_list_credential JSON NOT NULL,
    ADD COLUMN size INTEGER NOT NULL DEFAULT 131072,
    ADD COLUMN last_used_index INTEGER NOT NULL DEFAULT 0;

ALTER TABLE public.credentials_in_status_list
    ADD COLUMN is_processed BOOLEAN NOT NULL DEFAULT false;
