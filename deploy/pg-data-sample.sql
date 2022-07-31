--
-- PostgreSQL database dump
--

-- Dumped from database version 13.2 (Debian 13.2-1.pgdg100+1)
-- Dumped by pg_dump version 13.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: dev
--

COPY public.users (id, login, pwd, role) FROM stdin;
42	demo	$2a$10$QdTL6pRxYsHLXNuwMmQl6eRjXUQ8EJHkji2E0J4zQADtGFNFRVkcG	2
\.


--
-- Data for Name: languages; Type: TABLE DATA; Schema: public; Owner: dev
--

COPY public.languages (id, parts_of_speech) FROM stdin;
EN	noun,verb,adjective,adverb,pronoun,preposition,conjunction,interjection,article
RU	существительное,прилагательное,числительное,местоимение,глагол,наречие,причастие,предлог,союз,частица,междометие
\.


--
-- Data for Name: dictionaries; Type: TABLE DATA; Schema: public; Owner: dev
--

COPY public.dictionaries (id, target_lang, name, source_lang, user_id) FROM stdin;
1	RU	Weather	EN	42
\.


--
-- Data for Name: cards; Type: TABLE DATA; Schema: public; Owner: dev
--

COPY public.cards (id, text, answered, details, part_of_speech, transcription, dictionary_id) FROM stdin;
1	atmospheric	0	parsed from lingvo xml	ADJECTIVE	ˌætməs'ferik	1
2	weather	0	parsed from lingvo xml	NOUN	'weðə	1
3	snow	0	parsed from lingvo xml	NOUN	snəu	1
4	rain	0	parsed from lingvo xml	NOUN	rein	1
5	wind	0	parsed from lingvo xml	NOUN	wind	1
6	hail	0	parsed from lingvo xml	NOUN	heil	1
7	thunderstorm	0	parsed from lingvo xml	NOUN	'θʌndəstɔːm	1
8	precipitation	0	parsed from lingvo xml	NOUN	priˌsipi'teiʃ(ə)n	1
9	thunder	0	parsed from lingvo xml	NOUN	'θʌndə	1
10	lightning	0	parsed from lingvo xml	NOUN	'laitniŋ	1
11	anticyclone	0	parsed from lingvo xml	NOUN	'ænti'saikləun	1
12	atmosphere	0	parsed from lingvo xml	NOUN	'ætməsfiə	1
13	barometer	0	parsed from lingvo xml	NOUN	bə'rɔmitə	1
14	windy	0	parsed from lingvo xml	ADJECTIVE	'windi	1
15	humidity	0	parsed from lingvo xml	NOUN	hjuː'midəti	1
16	humid	0	parsed from lingvo xml	ADJECTIVE	'hjuːmid	1
17	moist	0	parsed from lingvo xml	ADJECTIVE	mɔist	1
18	damp	0	parsed from lingvo xml	ADJECTIVE	dæmp	1
19	sunrise	0	parsed from lingvo xml	NOUN	'sʌnraiz	1
20	degree	0	parsed from lingvo xml	NOUN	di'griː	1
21	pressure	0	parsed from lingvo xml	NOUN	'preʃə	1
22	stuffy	0	parsed from lingvo xml	ADJECTIVE	'stʌfi	1
23	heat	0	parsed from lingvo xml	NOUN	hiːt	1
24	hot	0	parsed from lingvo xml	ADJECTIVE	hɔt	1
25	sunset	0	parsed from lingvo xml	NOUN	'sʌnset	1
26	frost	0	parsed from lingvo xml	NOUN	frɔst	1
27	drizzle	0	parsed from lingvo xml	NOUN	'drizl	1
28	haze	0	parsed from lingvo xml	NOUN	heiz	1
29	ice	0	parsed from lingvo xml	NOUN	ais	1
30	shower	0	parsed from lingvo xml	NOUN	'ʃəuə	1
31	puddle	0	parsed from lingvo xml	NOUN	'pʌdl	1
32	meteorology	0	parsed from lingvo xml	NOUN	ˌmiːti(ə)'rɔləʤi	1
33	cloudy	0	parsed from lingvo xml	ADJECTIVE	'klaudi	1
34	thaw	0	parsed from lingvo xml	NOUN	θɔː	1
35	cool	0	parsed from lingvo xml	ADJECTIVE	kuːl	1
36	dusty	0	parsed from lingvo xml	ADJECTIVE	'dʌsti	1
37	rainbow	0	parsed from lingvo xml	NOUN	'reinbəu	1
38	fresh	0	parsed from lingvo xml	ADJECTIVE	freʃ	1
39	slippery	0	parsed from lingvo xml	ADJECTIVE	'slipəri	1
40	slush	0	parsed from lingvo xml	NOUN	slʌʃ	1
41	smog	0	parsed from lingvo xml	NOUN	smɔg	1
42	snowfall	0	parsed from lingvo xml	NOUN		1
43	sunny	0	parsed from lingvo xml	ADJECTIVE	'sʌni	1
44	dry	0	parsed from lingvo xml	ADJECTIVE	drai	1
45	wet	0	parsed from lingvo xml	ADJECTIVE	wet	1
46	temperature	0	parsed from lingvo xml	NOUN	'tempriʧə	1
47	warm	0	parsed from lingvo xml	ADJECTIVE	wɔːm	1
48	thermometer	0	parsed from lingvo xml	NOUN	θə'mɔmitə	1
49	fog	0	parsed from lingvo xml	NOUN	fɔg	1
50	mist	0	parsed from lingvo xml	NOUN	mist	1
51	foggy	0	parsed from lingvo xml	ADJECTIVE	'fɔgi	1
52	water	0	parsed from lingvo xml	NOUN	'wɔːtə	1
53	cold	0	parsed from lingvo xml	ADJECTIVE	kəuld	1
54	cyclone	0	parsed from lingvo xml	NOUN	'saikləun	1
55	storm	0	parsed from lingvo xml	NOUN	stɔːm	1
56	clear	0	parsed from lingvo xml	ADJECTIVE	kliə	1
57	sleet	0	parsed from lingvo xml	NOUN	sliːt	1
58	cloud	0	parsed from lingvo xml	NOUN	klaud	1
59	chilly	0	parsed from lingvo xml	ADJECTIVE	'ʧili	1
60	downpour	0	parsed from lingvo xml	NOUN	'daunpɔː	1
61	gale	0	parsed from lingvo xml	NOUN	geil	1
62	scorching	0	parsed from lingvo xml	ADJECTIVE	'skɔːʧiŋ	1
63	dust	0	parsed from lingvo xml	NOUN	dʌst	1
64	blustery	0	parsed from lingvo xml	ADJECTIVE	'blʌstəri	1
65	overcast	0	parsed from lingvo xml	ADJECTIVE	'əuvəkɑːst	1
\.


--
-- Data for Name: examples; Type: TABLE DATA; Schema: public; Owner: dev
--

COPY public.examples (id, text, card_id) FROM stdin;
1	atmospheric front -- атмосферный фронт	1
2	atmospheric layer -- слой атмосферы	1
3	atmospheric instability -- атмосферная нестабильность	1
4	weather bureau -- бюро погоды	2
5	nasty weather -- ненастная погода	2
6	weather forecast -- прогноз погоды	2
7	spell of cold weather -- похолодание	2
8	It snows. -- Идет снег.	3
9	snow depth -- высота снежного покрова	3
10	a flake of snow -- снежинка	3
11	torrential rain -- проливной дождь	4
12	heavy rain -- проливной дождь, ливень	4
13	It rains. -- Идет дождь.	4
14	drizzling rain -- изморось	4
15	a sudden gust of wind — внезапный порыв ветра	5
16	It hails. -- Идет град.	6
17	It was thundering all night long. -- Всю ночь гремел гром.	9
18	a flash of lightning -- вспышка молнии	10
19	The barometer is falling. -- Барометр падает.	13
20	It is windy. -- Ветрено.	14
21	windy weather -- ветреная погода	14
22	relative humidity -- относительная влажность	15
23	damp air -- влажный воздух	18
24	5 degrees above (below) zero -- 5 градусов выше (ниже) нуля	20
25	low pressure area -- область пониженного давления	21
26	atmospheric pressure -- атмосферное давление	21
27	hot weather -- жаркая погода	24
28	It is hot. -- Жарко.	24
29	hoar-frost -- иней, изморозь	26
30	It drizzles. -- Идёт мелкий дождь.	27
31	heat haze -- марево	28
32	thin ice -- тонкий лед	29
33	scattered showers — местами проливные дожди	30
34	It is cloudy. -- Облачно.	33
35	The snow started to thaw. -- Снег начал таять.	34
36	cool breeze -- прохладный ветерок	35
37	dusty road -- пыльная дорога	36
38	fresh air -- свежий воздух	38
39	slippery road -- скользкая дорога	39
40	sunny day -- солнечный день	43
41	dry air -- сухой воздух	44
42	What’s the temperature today? -- Какая сегодня температура?	46
43	Fahrenheit thermometer -- термометр Фаренгейта / со шкалой Фаренгейта	48
44	Celsius / Centigrade thermometer -- термометр Цельсия / со шкалой Цельсия	48
45	thick fog -- густой туман	49
46	morning mist -- утренний туман, утренняя дымка	50
47	cold weather -- холодная погода	53
48	dust storm -- пыльная буря	55
49	The sky was clear. -- Небо было безоблачным.	56
50	gale warning -- штормовое предупреждение	61
51	scorching sun -- палящее солнце	62
52	scorching day -- знойный день	62
53	blustery wind -- порывистый ветер	64
54	blustery weather -- ветреная погода	64
55	The sky is overcast. -- Небо затянуто облаками.	65
\.


--
-- Data for Name: translations; Type: TABLE DATA; Schema: public; Owner: dev
--

COPY public.translations (id, text, card_id) FROM stdin;
1	атмосферный	1
2	погода	2
3	снег	3
4	дождь	4
5	ветер	5
6	град	6
7	гроза	7
8	осадки	8
9	гром	9
10	молния	10
11	антициклон	11
12	атмосфера	12
13	барометр	13
14	ветреный	14
15	влажность	15
16	влажный	16
17	мокрый	16
18	сырой	16
19	влажный	17
20	сырой	17
21	мокрый	17
22	влажный	18
23	сырой	18
24	восход солнца	19
25	градус	20
26	давление	21
27	душный	22
28	жара	23
29	жаркий	24
30	закат	25
31	мороз	26
32	заморозки	26
33	изморось	27
34	мелкий дождь	27
35	мгла	28
36	дымка	28
37	лёгкий туман	28
38	лед	29
39	ливень	30
40	лужа	31
41	метеорология	32
42	облачный	33
43	оттепель	34
44	прохладный	35
45	пыльный	36
46	радуга	37
47	чистый	38
48	свежий	38
49	скользкий	39
50	слякоть	40
51	смог	41
52	густой туман с копотью	41
53	снегопад	42
54	солнечный	43
55	сухой	44
56	влажный	45
57	сырой	45
58	температура	46
59	теплый	47
60	термометр	48
61	градусник	48
62	туман	49
63	дымка	50
64	пасмурность	50
65	туман	50
66	туманный	51
67	вода	52
68	холодный	53
69	циклон	54
70	шторм	55
71	ураган	55
72	буря	55
73	гроза	55
74	безоблачный	56
75	ясный	56
76	дождь со снегом	57
77	туча	58
78	облако	58
79	промозглый	59
80	ливень	60
81	шторм	61
82	сильный ветер	61
83	палящий	62
84	знойный	62
85	пыль	63
86	порывистый	64
87	ветреный	64
88	покрытый облаками	65
\.


--
-- Name: cards_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dev
--

SELECT pg_catalog.setval('public.cards_id_seq', 65, true);


--
-- Name: dictionaries_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dev
--

SELECT pg_catalog.setval('public.dictionaries_id_seq', 1, true);


--
-- Name: examples_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dev
--

SELECT pg_catalog.setval('public.examples_id_seq', 55, true);


--
-- Name: translations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dev
--

SELECT pg_catalog.setval('public.translations_id_seq', 88, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dev
--

SELECT pg_catalog.setval('public.users_id_seq', 1001, true);

--
-- PostgreSQL database dump complete
--

