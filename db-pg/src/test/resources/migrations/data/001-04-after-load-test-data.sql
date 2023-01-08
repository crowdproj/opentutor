ALTER TABLE public.users ALTER COLUMN details TYPE JSON USING details::json;
ALTER TABLE public.dictionaries ALTER COLUMN details TYPE JSON USING details::json;
ALTER TABLE public.cards ALTER COLUMN details TYPE JSON USING details::jsonb;
ALTER TABLE public.cards ALTER COLUMN words TYPE JSON USING words::json;