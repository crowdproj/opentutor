INSERT INTO users (id, created_at)
    SELECT DISTINCT user_id, now()
    FROM dictionaries
    WHERE user_id NOT IN (SELECT id FROM users);

ALTER TABLE public.dictionaries
    ADD CONSTRAINT fk_dictionaries_users_id
    FOREIGN KEY (user_id)
    REFERENCES public.users(id)
    ON DELETE CASCADE;