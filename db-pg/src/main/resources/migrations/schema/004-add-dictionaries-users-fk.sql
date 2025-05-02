ALTER TABLE public.dictionaries
    ADD CONSTRAINT fk_dictionaries_users_id
    FOREIGN KEY (user_id)
    REFERENCES public.users(id)
    ON DELETE CASCADE;