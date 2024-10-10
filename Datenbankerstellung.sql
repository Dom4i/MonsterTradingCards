--
-- PostgreSQL database dump
--

-- Dumped from database version 16.4
-- Dumped by pg_dump version 16.4

-- Started on 2024-10-10 13:35:14

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
-- TOC entry 2 (class 3079 OID 24593)
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- TOC entry 4867 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 218 (class 1259 OID 24614)
-- Name: cards; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cards (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    name text NOT NULL,
    damage double precision NOT NULL,
    element_type text NOT NULL,
    card_type text NOT NULL
);


ALTER TABLE public.cards OWNER TO postgres;

--
-- TOC entry 216 (class 1259 OID 16465)
-- Name: packages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.packages (
    id character varying NOT NULL,
    name character varying NOT NULL
);


ALTER TABLE public.packages OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 24604)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    username character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    coins integer,
    name character varying(255),
    bio text,
    image text
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 4861 (class 0 OID 24614)
-- Dependencies: 218
-- Data for Name: cards; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cards (id, name, damage, element_type, card_type) FROM stdin;
\.


--
-- TOC entry 4859 (class 0 OID 16465)
-- Dependencies: 216
-- Data for Name: packages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.packages (id, name) FROM stdin;
845f0dc7-37d0-426e-994e-43fc3ac83c08	WaterGoblin
\.


--
-- TOC entry 4860 (class 0 OID 24604)
-- Dependencies: 217
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, username, password, coins, name, bio, image) FROM stdin;
8083831d-3533-4bdf-b427-5d05ef24ff57	kienboec	daniel	20	\N	\N	\N
5f8b77f2-f592-4b4e-8e29-415d4c34bc28	admin	istrator	20	\N	\N	\N
d25f125b-2bb2-43e4-aa9e-0517864ddd14	altenhof	markus	20	Altenhofer	me codin...	:-D
\.


--
-- TOC entry 4715 (class 2606 OID 24621)
-- Name: cards cards_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cards
    ADD CONSTRAINT cards_pkey PRIMARY KEY (id);


--
-- TOC entry 4709 (class 2606 OID 16471)
-- Name: packages packages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.packages
    ADD CONSTRAINT packages_pkey PRIMARY KEY (id);


--
-- TOC entry 4711 (class 2606 OID 24611)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 4713 (class 2606 OID 24613)
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


-- Completed on 2024-10-10 13:35:14

--
-- PostgreSQL database dump complete
--

