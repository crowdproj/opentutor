CREATE TABLE public.languages(
    id character varying(255) NOT NULL,
    parts_of_speech character varying(255) NOT NULL
);

CREATE TABLE public.cards (
    id bigint NOT NULL,
    text text NOT NULL,
    answered integer,
    details text,
    part_of_speech character varying(255),
    transcription text,
    dictionary_id bigint NOT NULL
);

ALTER TABLE public.cards ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.cards_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.dictionaries (
    id bigint NOT NULL,
    target_lang character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    source_lang character varying(255) NOT NULL,
    user_id bigint NOT NULL
);

ALTER TABLE public.dictionaries ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.dictionaries_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.examples (
    id bigint NOT NULL,
    text text NOT NULL,
    card_id bigint NOT NULL
);

ALTER TABLE public.examples ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.examples_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.translations (
    id bigint NOT NULL,
    text text NOT NULL,
    card_id bigint NOT NULL
);

ALTER TABLE public.translations ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.translations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.users (
    id bigint NOT NULL,
    uuid UUID NOT NULL UNIQUE ,
    role INT
);

ALTER TABLE public.users ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

ALTER TABLE ONLY public.languages
    ADD CONSTRAINT languages_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.cards
    ADD CONSTRAINT cards_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.dictionaries
    ADD CONSTRAINT dictionaries_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.examples
    ADD CONSTRAINT examples_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.translations
    ADD CONSTRAINT translations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.dictionaries
    ADD CONSTRAINT fk_dictionaries_users_id FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.dictionaries
    ADD CONSTRAINT fk_dictionaries_source_language_id FOREIGN KEY (source_lang) REFERENCES public.languages(id);

ALTER TABLE ONLY public.dictionaries
    ADD CONSTRAINT fk_dictionaries_target_language_id FOREIGN KEY (target_lang) REFERENCES public.languages(id);

ALTER TABLE ONLY public.cards
    ADD CONSTRAINT fk_cards_dictionaries_id FOREIGN KEY (dictionary_id) REFERENCES public.dictionaries(id);

ALTER TABLE ONLY public.examples
    ADD CONSTRAINT fk_examples_cards_id FOREIGN KEY (card_id) REFERENCES public.cards(id);

ALTER TABLE ONLY public.translations
    ADD CONSTRAINT fk_translations_cards_id FOREIGN KEY (card_id) REFERENCES public.cards(id);