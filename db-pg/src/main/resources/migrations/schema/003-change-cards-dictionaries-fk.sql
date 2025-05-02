ALTER TABLE public.cards
    DROP CONSTRAINT fk_cards_dictionaries_id;

ALTER TABLE public.cards
    ADD CONSTRAINT fk_cards_dictionaries_id
    FOREIGN KEY (dictionary_id)
    REFERENCES public.dictionaries(id)
    ON DELETE CASCADE;