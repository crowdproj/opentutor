ALTER TABLE public.users
    ADD details JSON NOT NULL DEFAULT '{}'::JSON;