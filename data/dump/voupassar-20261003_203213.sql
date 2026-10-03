--
-- PostgreSQL database dump
--

\restrict rNbBmzDbAeMQbOj7jpfbZfgRLXC1gmpXFaAOI4zfMKce59SKhAt3ymSgRua7EzA

-- Dumped from database version 16.15
-- Dumped by pg_dump version 16.15

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

ALTER TABLE IF EXISTS ONLY public.user_roles DROP CONSTRAINT IF EXISTS user_roles_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.user_roles DROP CONSTRAINT IF EXISTS user_roles_role_id_fkey;
ALTER TABLE IF EXISTS ONLY public.topics DROP CONSTRAINT IF EXISTS topics_discipline_id_fkey;
ALTER TABLE IF EXISTS ONLY public.subtopics DROP CONSTRAINT IF EXISTS subtopics_topic_id_fkey;
ALTER TABLE IF EXISTS ONLY public.study_sessions DROP CONSTRAINT IF EXISTS study_sessions_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.study_plans DROP CONSTRAINT IF EXISTS study_plans_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.study_plan_items DROP CONSTRAINT IF EXISTS study_plan_items_topic_id_fkey;
ALTER TABLE IF EXISTS ONLY public.study_plan_items DROP CONSTRAINT IF EXISTS study_plan_items_subtopic_id_fkey;
ALTER TABLE IF EXISTS ONLY public.study_plan_items DROP CONSTRAINT IF EXISTS study_plan_items_study_plan_id_fkey;
ALTER TABLE IF EXISTS ONLY public.student_topic_performance DROP CONSTRAINT IF EXISTS student_topic_performance_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.student_topic_performance DROP CONSTRAINT IF EXISTS student_topic_performance_topic_id_fkey;
ALTER TABLE IF EXISTS ONLY public.student_topic_performance DROP CONSTRAINT IF EXISTS student_topic_performance_subtopic_id_fkey;
ALTER TABLE IF EXISTS ONLY public.student_profiles DROP CONSTRAINT IF EXISTS student_profiles_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.student_achievements DROP CONSTRAINT IF EXISTS student_achievements_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.student_achievements DROP CONSTRAINT IF EXISTS student_achievements_achievement_id_fkey;
ALTER TABLE IF EXISTS ONLY public.simulations DROP CONSTRAINT IF EXISTS simulations_owner_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.simulations DROP CONSTRAINT IF EXISTS simulations_exam_id_fkey;
ALTER TABLE IF EXISTS ONLY public.simulation_questions DROP CONSTRAINT IF EXISTS simulation_questions_simulation_attempt_id_fkey;
ALTER TABLE IF EXISTS ONLY public.simulation_questions DROP CONSTRAINT IF EXISTS simulation_questions_question_id_fkey;
ALTER TABLE IF EXISTS ONLY public.simulation_attempts DROP CONSTRAINT IF EXISTS simulation_attempts_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.simulation_attempts DROP CONSTRAINT IF EXISTS simulation_attempts_simulation_id_fkey;
ALTER TABLE IF EXISTS ONLY public.review_session_questions DROP CONSTRAINT IF EXISTS review_session_questions_study_session_id_fkey;
ALTER TABLE IF EXISTS ONLY public.review_session_questions DROP CONSTRAINT IF EXISTS review_session_questions_question_id_fkey;
ALTER TABLE IF EXISTS ONLY public.refresh_tokens DROP CONSTRAINT IF EXISTS refresh_tokens_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.refresh_tokens DROP CONSTRAINT IF EXISTS refresh_tokens_replaced_by_id_fkey;
ALTER TABLE IF EXISTS ONLY public.questions DROP CONSTRAINT IF EXISTS questions_exam_id_fkey;
ALTER TABLE IF EXISTS ONLY public.questions DROP CONSTRAINT IF EXISTS questions_exam_document_id_fkey;
ALTER TABLE IF EXISTS ONLY public.questions DROP CONSTRAINT IF EXISTS questions_discipline_id_fkey;
ALTER TABLE IF EXISTS ONLY public.question_tag_map DROP CONSTRAINT IF EXISTS question_tag_map_tag_id_fkey;
ALTER TABLE IF EXISTS ONLY public.question_tag_map DROP CONSTRAINT IF EXISTS question_tag_map_question_id_fkey;
ALTER TABLE IF EXISTS ONLY public.question_sources DROP CONSTRAINT IF EXISTS question_sources_question_id_fkey;
ALTER TABLE IF EXISTS ONLY public.question_sources DROP CONSTRAINT IF EXISTS question_sources_exam_document_id_fkey;
ALTER TABLE IF EXISTS ONLY public.question_passages DROP CONSTRAINT IF EXISTS question_passages_question_id_fkey;
ALTER TABLE IF EXISTS ONLY public.question_passages DROP CONSTRAINT IF EXISTS question_passages_passage_id_fkey;
ALTER TABLE IF EXISTS ONLY public.question_options DROP CONSTRAINT IF EXISTS question_options_question_id_fkey;
ALTER TABLE IF EXISTS ONLY public.question_figures DROP CONSTRAINT IF EXISTS question_figures_question_id_fkey;
ALTER TABLE IF EXISTS ONLY public.question_classifications DROP CONSTRAINT IF EXISTS question_classifications_topic_id_fkey;
ALTER TABLE IF EXISTS ONLY public.question_classifications DROP CONSTRAINT IF EXISTS question_classifications_subtopic_id_fkey;
ALTER TABLE IF EXISTS ONLY public.question_classifications DROP CONSTRAINT IF EXISTS question_classifications_reviewed_by_fkey;
ALTER TABLE IF EXISTS ONLY public.question_classifications DROP CONSTRAINT IF EXISTS question_classifications_question_id_fkey;
ALTER TABLE IF EXISTS ONLY public.question_attempts DROP CONSTRAINT IF EXISTS question_attempts_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.question_attempts DROP CONSTRAINT IF EXISTS question_attempts_study_session_id_fkey;
ALTER TABLE IF EXISTS ONLY public.question_attempts DROP CONSTRAINT IF EXISTS question_attempts_simulation_attempt_id_fkey;
ALTER TABLE IF EXISTS ONLY public.question_attempts DROP CONSTRAINT IF EXISTS question_attempts_question_id_fkey;
ALTER TABLE IF EXISTS ONLY public.progress_snapshots DROP CONSTRAINT IF EXISTS progress_snapshots_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.password_reset_tokens DROP CONSTRAINT IF EXISTS password_reset_tokens_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.passages DROP CONSTRAINT IF EXISTS passages_exam_id_fkey;
ALTER TABLE IF EXISTS ONLY public.exam_versions DROP CONSTRAINT IF EXISTS exam_versions_exam_id_fkey;
ALTER TABLE IF EXISTS ONLY public.exam_essay_prompts DROP CONSTRAINT IF EXISTS exam_essay_prompts_exam_id_fkey;
ALTER TABLE IF EXISTS ONLY public.exam_documents DROP CONSTRAINT IF EXISTS exam_documents_exam_version_id_fkey;
DROP TRIGGER IF EXISTS trg_users_updated ON public.users;
DROP TRIGGER IF EXISTS trg_user_roles_updated ON public.user_roles;
DROP TRIGGER IF EXISTS trg_topics_updated ON public.topics;
DROP TRIGGER IF EXISTS trg_subtopics_updated ON public.subtopics;
DROP TRIGGER IF EXISTS trg_study_sessions_updated ON public.study_sessions;
DROP TRIGGER IF EXISTS trg_study_plans_updated ON public.study_plans;
DROP TRIGGER IF EXISTS trg_study_plan_items_updated ON public.study_plan_items;
DROP TRIGGER IF EXISTS trg_student_topic_performance_updated ON public.student_topic_performance;
DROP TRIGGER IF EXISTS trg_student_profiles_updated ON public.student_profiles;
DROP TRIGGER IF EXISTS trg_student_achievements_updated ON public.student_achievements;
DROP TRIGGER IF EXISTS trg_simulations_updated ON public.simulations;
DROP TRIGGER IF EXISTS trg_simulation_questions_updated ON public.simulation_questions;
DROP TRIGGER IF EXISTS trg_simulation_attempts_updated ON public.simulation_attempts;
DROP TRIGGER IF EXISTS trg_roles_updated ON public.roles;
DROP TRIGGER IF EXISTS trg_review_session_questions_updated ON public.review_session_questions;
DROP TRIGGER IF EXISTS trg_refresh_tokens_updated ON public.refresh_tokens;
DROP TRIGGER IF EXISTS trg_questions_updated ON public.questions;
DROP TRIGGER IF EXISTS trg_question_tags_updated ON public.question_tags;
DROP TRIGGER IF EXISTS trg_question_tag_map_updated ON public.question_tag_map;
DROP TRIGGER IF EXISTS trg_question_sources_updated ON public.question_sources;
DROP TRIGGER IF EXISTS trg_question_options_updated ON public.question_options;
DROP TRIGGER IF EXISTS trg_question_classifications_updated ON public.question_classifications;
DROP TRIGGER IF EXISTS trg_question_attempts_updated ON public.question_attempts;
DROP TRIGGER IF EXISTS trg_qclass_coherence ON public.question_classifications;
DROP TRIGGER IF EXISTS trg_progress_snapshots_updated ON public.progress_snapshots;
DROP TRIGGER IF EXISTS trg_plan_item_coherence ON public.study_plan_items;
DROP TRIGGER IF EXISTS trg_perf_coherence ON public.student_topic_performance;
DROP TRIGGER IF EXISTS trg_password_reset_updated ON public.password_reset_tokens;
DROP TRIGGER IF EXISTS trg_four_options ON public.question_options;
DROP TRIGGER IF EXISTS trg_exams_updated ON public.exams;
DROP TRIGGER IF EXISTS trg_exam_versions_updated ON public.exam_versions;
DROP TRIGGER IF EXISTS trg_exam_essay_prompts_updated ON public.exam_essay_prompts;
DROP TRIGGER IF EXISTS trg_exam_documents_updated ON public.exam_documents;
DROP TRIGGER IF EXISTS trg_disciplines_updated ON public.disciplines;
DROP TRIGGER IF EXISTS trg_attempts_immutable ON public.question_attempts;
DROP TRIGGER IF EXISTS trg_achievements_updated ON public.achievements;
DROP INDEX IF EXISTS public.uq_study_plans_active;
DROP INDEX IF EXISTS public.uq_qclass_approved;
DROP INDEX IF EXISTS public.idx_sim_attempts_user;
DROP INDEX IF EXISTS public.idx_sessions_user;
DROP INDEX IF EXISTS public.idx_review_sq_session;
DROP INDEX IF EXISTS public.idx_review_sq_question;
DROP INDEX IF EXISTS public.idx_refresh_tokens_user;
DROP INDEX IF EXISTS public.idx_questions_trgm;
DROP INDEX IF EXISTS public.idx_questions_public;
DROP INDEX IF EXISTS public.idx_questions_exam;
DROP INDEX IF EXISTS public.idx_questions_disc;
DROP INDEX IF EXISTS public.idx_question_sources_q;
DROP INDEX IF EXISTS public.idx_question_passages_q;
DROP INDEX IF EXISTS public.idx_question_options_q;
DROP INDEX IF EXISTS public.idx_question_figures_q;
DROP INDEX IF EXISTS public.idx_qclass_topic;
DROP INDEX IF EXISTS public.idx_qclass_q;
DROP INDEX IF EXISTS public.idx_plan_items;
DROP INDEX IF EXISTS public.idx_perf_user_acc;
DROP INDEX IF EXISTS public.idx_password_reset_user;
DROP INDEX IF EXISTS public.idx_passages_exam;
DROP INDEX IF EXISTS public.idx_attempts_user_q;
DROP INDEX IF EXISTS public.idx_attempts_user_mode;
DROP INDEX IF EXISTS public.idx_attempts_question;
DROP INDEX IF EXISTS public.flyway_schema_history_s_idx;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_pkey;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_email_key;
ALTER TABLE IF EXISTS ONLY public.user_roles DROP CONSTRAINT IF EXISTS user_roles_pkey;
ALTER TABLE IF EXISTS ONLY public.topics DROP CONSTRAINT IF EXISTS topics_pkey;
ALTER TABLE IF EXISTS ONLY public.topics DROP CONSTRAINT IF EXISTS topics_discipline_id_code_key;
ALTER TABLE IF EXISTS ONLY public.subtopics DROP CONSTRAINT IF EXISTS subtopics_topic_id_code_key;
ALTER TABLE IF EXISTS ONLY public.subtopics DROP CONSTRAINT IF EXISTS subtopics_pkey;
ALTER TABLE IF EXISTS ONLY public.study_sessions DROP CONSTRAINT IF EXISTS study_sessions_pkey;
ALTER TABLE IF EXISTS ONLY public.study_plans DROP CONSTRAINT IF EXISTS study_plans_pkey;
ALTER TABLE IF EXISTS ONLY public.study_plan_items DROP CONSTRAINT IF EXISTS study_plan_items_pkey;
ALTER TABLE IF EXISTS ONLY public.student_topic_performance DROP CONSTRAINT IF EXISTS student_topic_performance_user_id_topic_id_subtopic_id_key;
ALTER TABLE IF EXISTS ONLY public.student_topic_performance DROP CONSTRAINT IF EXISTS student_topic_performance_pkey;
ALTER TABLE IF EXISTS ONLY public.student_profiles DROP CONSTRAINT IF EXISTS student_profiles_pkey;
ALTER TABLE IF EXISTS ONLY public.student_achievements DROP CONSTRAINT IF EXISTS student_achievements_pkey;
ALTER TABLE IF EXISTS ONLY public.simulations DROP CONSTRAINT IF EXISTS simulations_pkey;
ALTER TABLE IF EXISTS ONLY public.simulation_questions DROP CONSTRAINT IF EXISTS simulation_questions_pkey;
ALTER TABLE IF EXISTS ONLY public.simulation_attempts DROP CONSTRAINT IF EXISTS simulation_attempts_pkey;
ALTER TABLE IF EXISTS ONLY public.roles DROP CONSTRAINT IF EXISTS roles_pkey;
ALTER TABLE IF EXISTS ONLY public.roles DROP CONSTRAINT IF EXISTS roles_code_key;
ALTER TABLE IF EXISTS ONLY public.review_session_questions DROP CONSTRAINT IF EXISTS review_session_questions_pkey;
ALTER TABLE IF EXISTS ONLY public.refresh_tokens DROP CONSTRAINT IF EXISTS refresh_tokens_token_hash_key;
ALTER TABLE IF EXISTS ONLY public.refresh_tokens DROP CONSTRAINT IF EXISTS refresh_tokens_pkey;
ALTER TABLE IF EXISTS ONLY public.questions DROP CONSTRAINT IF EXISTS questions_source_type_source_year_source_question_number_ex_key;
ALTER TABLE IF EXISTS ONLY public.questions DROP CONSTRAINT IF EXISTS questions_pkey;
ALTER TABLE IF EXISTS ONLY public.questions DROP CONSTRAINT IF EXISTS questions_checksum_key;
ALTER TABLE IF EXISTS ONLY public.question_tags DROP CONSTRAINT IF EXISTS question_tags_pkey;
ALTER TABLE IF EXISTS ONLY public.question_tags DROP CONSTRAINT IF EXISTS question_tags_code_key;
ALTER TABLE IF EXISTS ONLY public.question_tag_map DROP CONSTRAINT IF EXISTS question_tag_map_pkey;
ALTER TABLE IF EXISTS ONLY public.question_sources DROP CONSTRAINT IF EXISTS question_sources_pkey;
ALTER TABLE IF EXISTS ONLY public.question_passages DROP CONSTRAINT IF EXISTS question_passages_question_id_passage_id_key;
ALTER TABLE IF EXISTS ONLY public.question_passages DROP CONSTRAINT IF EXISTS question_passages_pkey;
ALTER TABLE IF EXISTS ONLY public.question_options DROP CONSTRAINT IF EXISTS question_options_question_id_label_key;
ALTER TABLE IF EXISTS ONLY public.question_options DROP CONSTRAINT IF EXISTS question_options_pkey;
ALTER TABLE IF EXISTS ONLY public.question_figures DROP CONSTRAINT IF EXISTS question_figures_question_id_position_key;
ALTER TABLE IF EXISTS ONLY public.question_figures DROP CONSTRAINT IF EXISTS question_figures_pkey;
ALTER TABLE IF EXISTS ONLY public.question_classifications DROP CONSTRAINT IF EXISTS question_classifications_pkey;
ALTER TABLE IF EXISTS ONLY public.question_attempts DROP CONSTRAINT IF EXISTS question_attempts_pkey;
ALTER TABLE IF EXISTS ONLY public.progress_snapshots DROP CONSTRAINT IF EXISTS progress_snapshots_user_id_taken_at_key;
ALTER TABLE IF EXISTS ONLY public.progress_snapshots DROP CONSTRAINT IF EXISTS progress_snapshots_pkey;
ALTER TABLE IF EXISTS ONLY public.password_reset_tokens DROP CONSTRAINT IF EXISTS password_reset_tokens_token_hash_key;
ALTER TABLE IF EXISTS ONLY public.password_reset_tokens DROP CONSTRAINT IF EXISTS password_reset_tokens_pkey;
ALTER TABLE IF EXISTS ONLY public.passages DROP CONSTRAINT IF EXISTS passages_pkey;
ALTER TABLE IF EXISTS ONLY public.passages DROP CONSTRAINT IF EXISTS passages_exam_id_passage_key_key;
ALTER TABLE IF EXISTS ONLY public.flyway_schema_history DROP CONSTRAINT IF EXISTS flyway_schema_history_pk;
ALTER TABLE IF EXISTS ONLY public.exams DROP CONSTRAINT IF EXISTS exams_year_key;
ALTER TABLE IF EXISTS ONLY public.exams DROP CONSTRAINT IF EXISTS exams_pkey;
ALTER TABLE IF EXISTS ONLY public.exam_versions DROP CONSTRAINT IF EXISTS exam_versions_pkey;
ALTER TABLE IF EXISTS ONLY public.exam_versions DROP CONSTRAINT IF EXISTS exam_versions_exam_id_version_code_key;
ALTER TABLE IF EXISTS ONLY public.exam_essay_prompts DROP CONSTRAINT IF EXISTS exam_essay_prompts_pkey;
ALTER TABLE IF EXISTS ONLY public.exam_documents DROP CONSTRAINT IF EXISTS exam_documents_sha256_key;
ALTER TABLE IF EXISTS ONLY public.exam_documents DROP CONSTRAINT IF EXISTS exam_documents_pkey;
ALTER TABLE IF EXISTS ONLY public.disciplines DROP CONSTRAINT IF EXISTS disciplines_pkey;
ALTER TABLE IF EXISTS ONLY public.disciplines DROP CONSTRAINT IF EXISTS disciplines_code_key;
ALTER TABLE IF EXISTS ONLY public.achievements DROP CONSTRAINT IF EXISTS achievements_pkey;
ALTER TABLE IF EXISTS ONLY public.achievements DROP CONSTRAINT IF EXISTS achievements_code_key;
DROP TABLE IF EXISTS public.users;
DROP TABLE IF EXISTS public.user_roles;
DROP TABLE IF EXISTS public.topics;
DROP TABLE IF EXISTS public.subtopics;
DROP TABLE IF EXISTS public.study_sessions;
DROP TABLE IF EXISTS public.study_plans;
DROP TABLE IF EXISTS public.study_plan_items;
DROP TABLE IF EXISTS public.student_topic_performance;
DROP TABLE IF EXISTS public.student_profiles;
DROP TABLE IF EXISTS public.student_achievements;
DROP TABLE IF EXISTS public.simulations;
DROP TABLE IF EXISTS public.simulation_questions;
DROP TABLE IF EXISTS public.simulation_attempts;
DROP TABLE IF EXISTS public.roles;
DROP TABLE IF EXISTS public.review_session_questions;
DROP TABLE IF EXISTS public.refresh_tokens;
DROP TABLE IF EXISTS public.questions;
DROP TABLE IF EXISTS public.question_tags;
DROP TABLE IF EXISTS public.question_tag_map;
DROP TABLE IF EXISTS public.question_sources;
DROP TABLE IF EXISTS public.question_passages;
DROP TABLE IF EXISTS public.question_options;
DROP TABLE IF EXISTS public.question_figures;
DROP TABLE IF EXISTS public.question_classifications;
DROP TABLE IF EXISTS public.question_attempts;
DROP TABLE IF EXISTS public.progress_snapshots;
DROP TABLE IF EXISTS public.password_reset_tokens;
DROP TABLE IF EXISTS public.passages;
DROP TABLE IF EXISTS public.flyway_schema_history;
DROP TABLE IF EXISTS public.exams;
DROP TABLE IF EXISTS public.exam_versions;
DROP TABLE IF EXISTS public.exam_essay_prompts;
DROP TABLE IF EXISTS public.exam_documents;
DROP TABLE IF EXISTS public.disciplines;
DROP TABLE IF EXISTS public.achievements;
DROP FUNCTION IF EXISTS public.set_updated_at();
DROP FUNCTION IF EXISTS public.prevent_attempt_mutation();
DROP FUNCTION IF EXISTS public.enforce_four_options();
DROP FUNCTION IF EXISTS public.check_subtopic_topic();
DROP EXTENSION IF EXISTS pg_trgm;
DROP EXTENSION IF EXISTS citext;
--
-- Name: citext; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;


--
-- Name: EXTENSION citext; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION citext IS 'data type for case-insensitive character strings';


--
-- Name: pg_trgm; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;


--
-- Name: EXTENSION pg_trgm; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION pg_trgm IS 'text similarity measurement and index searching based on trigrams';


--
-- Name: check_subtopic_topic(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.check_subtopic_topic() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  t BIGINT;
BEGIN
  IF NEW.subtopic_id IS NOT NULL THEN
    SELECT topic_id INTO t FROM subtopics WHERE id = NEW.subtopic_id;
    IF NOT FOUND THEN
      RAISE EXCEPTION 'subtopic_id % inexistente', NEW.subtopic_id;
    END IF;
    IF NEW.topic_id IS NULL THEN
      NEW.topic_id := t;
    ELSIF NEW.topic_id <> t THEN
      RAISE EXCEPTION 'subtópico % não pertence ao tópico %', NEW.subtopic_id, NEW.topic_id;
    END IF;
  END IF;
  RETURN NEW;
END;
$$;


--
-- Name: enforce_four_options(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.enforce_four_options() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  r RECORD;
BEGIN
  FOR r IN
    SELECT q.id, q.kind, count(o.id) AS n
      FROM questions q LEFT JOIN question_options o ON o.question_id = q.id
     WHERE q.kind = 'OBJECTIVE'
       AND q.validation_status <> 'PENDING'
     GROUP BY q.id, q.kind
  LOOP
    IF r.n <> 4 THEN
      RAISE EXCEPTION 'questão objetiva % tem % alternativas (esperado 4)', r.id, r.n;
    END IF;
  END LOOP;
  RETURN NULL;
END;
$$;


--
-- Name: prevent_attempt_mutation(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.prevent_attempt_mutation() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  RAISE EXCEPTION 'question_attempts é imutável (correção só via nova tentativa)';
END;
$$;


--
-- Name: set_updated_at(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.set_updated_at() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: achievements; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.achievements (
    id bigint NOT NULL,
    code text NOT NULL,
    title text NOT NULL,
    description text,
    rule_json jsonb DEFAULT '{}'::jsonb NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: achievements_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.achievements ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.achievements_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: disciplines; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.disciplines (
    id bigint NOT NULL,
    code text NOT NULL,
    name text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: disciplines_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.disciplines ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.disciplines_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: exam_documents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.exam_documents (
    id bigint NOT NULL,
    exam_version_id bigint NOT NULL,
    kind text NOT NULL,
    file_name text NOT NULL,
    sha256 character(64) NOT NULL,
    pages smallint NOT NULL,
    generator text,
    note text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT exam_documents_kind_check CHECK ((kind = ANY (ARRAY['CADERNO'::text, 'GABARITO_PRELIMINAR'::text, 'GABARITO_DEFINITIVO'::text, 'GABARITO_FINAL'::text, 'OFERTAS'::text, 'OUTRO'::text]))),
    CONSTRAINT exam_documents_pages_check CHECK ((pages > 0))
);


--
-- Name: exam_documents_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.exam_documents ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.exam_documents_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: exam_essay_prompts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.exam_essay_prompts (
    exam_id bigint NOT NULL,
    genre text NOT NULL,
    theme text NOT NULL,
    pseudonym text NOT NULL,
    proposal_excerpt text NOT NULL,
    criteria_text text,
    page smallint NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT exam_essay_prompts_page_check CHECK ((page > 0))
);


--
-- Name: exam_versions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.exam_versions (
    id bigint NOT NULL,
    exam_id bigint NOT NULL,
    version_code text NOT NULL,
    published_at date,
    note text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: exam_versions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.exam_versions ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.exam_versions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: exams; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.exams (
    id bigint NOT NULL,
    year smallint NOT NULL,
    edital text NOT NULL,
    duration_minutes smallint DEFAULT 240 NOT NULL,
    objective_count smallint DEFAULT 40 NOT NULL,
    lp_count smallint DEFAULT 20 NOT NULL,
    mat_count smallint DEFAULT 20 NOT NULL,
    has_essay boolean DEFAULT true NOT NULL,
    scoring_rule text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT exams_duration_minutes_check CHECK ((duration_minutes > 0)),
    CONSTRAINT exams_lp_count_check CHECK ((lp_count >= 0)),
    CONSTRAINT exams_mat_count_check CHECK ((mat_count >= 0)),
    CONSTRAINT exams_objective_count_check CHECK ((objective_count > 0)),
    CONSTRAINT exams_year_check CHECK ((year = ANY (ARRAY[2020, 2022, 2023, 2024, 2025, 2026])))
);


--
-- Name: exams_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.exams ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.exams_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: passages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.passages (
    id bigint NOT NULL,
    exam_id bigint NOT NULL,
    passage_key text NOT NULL,
    label text NOT NULL,
    kind text NOT NULL,
    title text,
    byline text,
    subtitle text,
    intro text,
    content text,
    visual_description text,
    format_note text,
    source_note text,
    page_start smallint NOT NULL,
    page_end smallint NOT NULL,
    checksum character(64) NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT passages_check CHECK ((page_end >= page_start)),
    CONSTRAINT passages_check1 CHECK (((content IS NOT NULL) OR (visual_description IS NOT NULL))),
    CONSTRAINT passages_kind_check CHECK ((kind = ANY (ARRAY['TEXTO'::text, 'TRECHO'::text, 'TABELA'::text, 'GRAFICO'::text, 'IMAGEM'::text, 'CHARGE'::text, 'TIRINHA'::text]))),
    CONSTRAINT passages_label_check CHECK ((label <> ''::text)),
    CONSTRAINT passages_page_start_check CHECK ((page_start > 0)),
    CONSTRAINT passages_passage_key_check CHECK ((passage_key <> ''::text))
);


--
-- Name: passages_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.passages ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.passages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: password_reset_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.password_reset_tokens (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    token_hash text NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    used_at timestamp with time zone,
    created_ip text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT password_reset_tokens_check CHECK ((expires_at > created_at)),
    CONSTRAINT password_reset_tokens_check1 CHECK (((used_at IS NULL) OR (used_at >= created_at)))
);


--
-- Name: password_reset_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.password_reset_tokens ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.password_reset_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: progress_snapshots; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.progress_snapshots (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    taken_at timestamp with time zone DEFAULT now() NOT NULL,
    metrics_json jsonb DEFAULT '{}'::jsonb NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: progress_snapshots_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.progress_snapshots ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.progress_snapshots_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: question_attempts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.question_attempts (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    question_id bigint NOT NULL,
    study_session_id bigint,
    simulation_attempt_id bigint,
    selected_option text NOT NULL,
    is_correct boolean,
    was_annulled boolean DEFAULT false NOT NULL,
    time_spent_seconds integer,
    mode text NOT NULL,
    answered_at timestamp with time zone DEFAULT now() NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT question_attempts_check CHECK (((study_session_id IS NOT NULL) OR (simulation_attempt_id IS NOT NULL))),
    CONSTRAINT question_attempts_check1 CHECK ((((NOT was_annulled) AND (is_correct IS NOT NULL)) OR (was_annulled AND (is_correct IS NULL)))),
    CONSTRAINT question_attempts_mode_check CHECK ((mode = ANY (ARRAY['ESTUDO'::text, 'PROVA'::text, 'REVISAO'::text]))),
    CONSTRAINT question_attempts_selected_option_check CHECK ((selected_option = ANY ((ARRAY['A'::bpchar, 'B'::bpchar, 'C'::bpchar, 'D'::bpchar, 'BLANK'::bpchar])::text[]))),
    CONSTRAINT question_attempts_time_spent_seconds_check CHECK (((time_spent_seconds IS NULL) OR (time_spent_seconds >= 0)))
);


--
-- Name: question_attempts_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.question_attempts ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.question_attempts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: question_classifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.question_classifications (
    id bigint NOT NULL,
    question_id bigint NOT NULL,
    taxonomy_version text NOT NULL,
    topic_id bigint,
    subtopic_id bigint,
    skill text,
    reasoning_type text,
    confidence text NOT NULL,
    evidence text NOT NULL,
    origin text DEFAULT 'CLASSIFICACAO_DERIVADA_FONTE'::text NOT NULL,
    status text DEFAULT 'PENDING'::text NOT NULL,
    reviewed_by bigint,
    reviewed_at timestamp with time zone,
    observation text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT question_classifications_confidence_check CHECK ((confidence = ANY (ARRAY['ALTA'::text, 'MEDIA'::text, 'BAIXA'::text]))),
    CONSTRAINT question_classifications_evidence_check CHECK ((evidence <> ''::text)),
    CONSTRAINT question_classifications_status_check CHECK ((status = ANY (ARRAY['PENDING'::text, 'REVIEWED'::text, 'APPROVED'::text, 'REJECTED'::text])))
);


--
-- Name: question_classifications_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.question_classifications ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.question_classifications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: question_figures; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.question_figures (
    id bigint NOT NULL,
    question_id bigint NOT NULL,
    "position" smallint DEFAULT 1 NOT NULL,
    file_path text NOT NULL,
    alt_text text NOT NULL,
    page smallint,
    publication_status text DEFAULT 'PENDENTE_REVISAO'::text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT question_figures_alt_text_check CHECK ((alt_text <> ''::text)),
    CONSTRAINT question_figures_file_path_check CHECK ((file_path <> ''::text)),
    CONSTRAINT question_figures_page_check CHECK (((page IS NULL) OR (page > 0))),
    CONSTRAINT question_figures_position_check CHECK (("position" > 0)),
    CONSTRAINT question_figures_publication_status_check CHECK ((publication_status = ANY (ARRAY['PENDENTE_REVISAO'::text, 'PUBLICAVEL'::text])))
);


--
-- Name: question_figures_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.question_figures ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.question_figures_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: question_options; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.question_options (
    id bigint NOT NULL,
    question_id bigint NOT NULL,
    label character(1) NOT NULL,
    option_text text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT question_options_label_check CHECK ((label = ANY (ARRAY['A'::bpchar, 'B'::bpchar, 'C'::bpchar, 'D'::bpchar])))
);


--
-- Name: question_options_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.question_options ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.question_options_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: question_passages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.question_passages (
    id bigint NOT NULL,
    question_id bigint NOT NULL,
    passage_id bigint NOT NULL,
    "position" smallint DEFAULT 1 NOT NULL,
    CONSTRAINT question_passages_position_check CHECK (("position" > 0))
);


--
-- Name: question_passages_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.question_passages ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.question_passages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: question_sources; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.question_sources (
    id bigint NOT NULL,
    question_id bigint NOT NULL,
    exam_document_id bigint NOT NULL,
    role text NOT NULL,
    page_start smallint,
    page_end smallint,
    doc_sha256 character(64) NOT NULL,
    note text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT question_sources_check CHECK (((page_end IS NULL) OR (page_start IS NULL) OR (page_end >= page_start))),
    CONSTRAINT question_sources_page_start_check CHECK (((page_start IS NULL) OR (page_start > 0))),
    CONSTRAINT question_sources_role_check CHECK ((role = ANY (ARRAY['PRIMARY'::text, 'GABARITO'::text, 'OFERTAS'::text, 'COMPLEMENTAR'::text])))
);


--
-- Name: question_sources_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.question_sources ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.question_sources_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: question_tag_map; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.question_tag_map (
    question_id bigint NOT NULL,
    tag_id bigint NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: question_tags; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.question_tags (
    id bigint NOT NULL,
    code text NOT NULL,
    description text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: question_tags_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.question_tags ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.question_tags_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: questions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.questions (
    id bigint NOT NULL,
    source_type text NOT NULL,
    exam_id bigint,
    exam_document_id bigint,
    source_year smallint,
    source_question_number smallint,
    statement text NOT NULL,
    kind text DEFAULT 'OBJECTIVE'::text NOT NULL,
    discipline_id bigint NOT NULL,
    page_start smallint NOT NULL,
    page_end smallint NOT NULL,
    answer_key character(1) NOT NULL,
    annulled boolean DEFAULT false NOT NULL,
    checksum character(64) NOT NULL,
    has_figure boolean DEFAULT false NOT NULL,
    difficulty_estimate text,
    explanation text,
    validation_status text DEFAULT 'PENDING'::text NOT NULL,
    publication_status text DEFAULT 'PENDENTE_REVISAO'::text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT questions_answer_key_check CHECK ((answer_key = ANY (ARRAY['A'::bpchar, 'B'::bpchar, 'C'::bpchar, 'D'::bpchar, 'X'::bpchar]))),
    CONSTRAINT questions_check CHECK ((page_end >= page_start)),
    CONSTRAINT questions_check1 CHECK (((annulled AND (answer_key = 'X'::bpchar)) OR ((NOT annulled) AND (answer_key <> 'X'::bpchar)))),
    CONSTRAINT questions_check2 CHECK ((((source_type = 'OFFICIAL'::text) AND (exam_id IS NOT NULL) AND (source_year IS NOT NULL) AND ((source_question_number >= 1) AND (source_question_number <= 40))) OR (source_type <> 'OFFICIAL'::text))),
    CONSTRAINT questions_difficulty_estimate_check CHECK (((difficulty_estimate IS NULL) OR (difficulty_estimate = ANY (ARRAY['FACIL'::text, 'MEDIA'::text, 'DIFICIL'::text])))),
    CONSTRAINT questions_kind_check CHECK ((kind = ANY (ARRAY['OBJECTIVE'::text, 'DISCURSIVE'::text]))),
    CONSTRAINT questions_page_start_check CHECK ((page_start > 0)),
    CONSTRAINT questions_publication_status_check CHECK ((publication_status = ANY (ARRAY['PUBLICAVEL'::text, 'NAO_PUBLICAVEL'::text, 'PENDENTE_REVISAO'::text, 'SOMENTE_REFERENCIA'::text]))),
    CONSTRAINT questions_source_type_check CHECK ((source_type = ANY (ARRAY['OFFICIAL'::text, 'AUTHORAL'::text, 'ADAPTED'::text, 'INTERNAL_REVIEW'::text, 'EXPERIMENTAL'::text]))),
    CONSTRAINT questions_statement_check CHECK ((statement <> ''::text)),
    CONSTRAINT questions_validation_status_check CHECK ((validation_status = ANY (ARRAY['PENDING'::text, 'REVIEWED'::text, 'APPROVED'::text, 'REJECTED'::text])))
);


--
-- Name: questions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.questions ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.questions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: refresh_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.refresh_tokens (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    token_hash text NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    revoked_at timestamp with time zone,
    replaced_by_id bigint,
    created_ip text,
    user_agent text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT refresh_tokens_check CHECK (((revoked_at IS NULL) OR (revoked_at >= created_at)))
);


--
-- Name: refresh_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.refresh_tokens ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.refresh_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: review_session_questions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.review_session_questions (
    study_session_id bigint NOT NULL,
    "position" smallint NOT NULL,
    question_id bigint NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT review_session_questions_position_check CHECK (("position" > 0))
);


--
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    id bigint NOT NULL,
    code text NOT NULL,
    description text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT roles_code_check CHECK ((code = ANY (ARRAY['STUDENT'::text, 'CURATOR'::text, 'ADMIN'::text])))
);


--
-- Name: roles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.roles ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: simulation_attempts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.simulation_attempts (
    id bigint NOT NULL,
    simulation_id bigint NOT NULL,
    user_id bigint NOT NULL,
    mode text NOT NULL,
    status text DEFAULT 'IN_PROGRESS'::text NOT NULL,
    started_at timestamp with time zone DEFAULT now() NOT NULL,
    submitted_at timestamp with time zone,
    score_json jsonb,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT simulation_attempts_check CHECK (((submitted_at IS NULL) OR (submitted_at >= started_at))),
    CONSTRAINT simulation_attempts_mode_check CHECK ((mode = ANY (ARRAY['ESTUDO'::text, 'PROVA'::text]))),
    CONSTRAINT simulation_attempts_status_check CHECK ((status = ANY (ARRAY['IN_PROGRESS'::text, 'SUBMITTED'::text, 'ABANDONED'::text])))
);


--
-- Name: simulation_attempts_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.simulation_attempts ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.simulation_attempts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: simulation_questions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.simulation_questions (
    simulation_attempt_id bigint NOT NULL,
    "position" smallint NOT NULL,
    question_id bigint NOT NULL,
    frozen_answer_key character(1) NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT simulation_questions_frozen_answer_key_check CHECK ((frozen_answer_key = ANY (ARRAY['A'::bpchar, 'B'::bpchar, 'C'::bpchar, 'D'::bpchar, 'X'::bpchar]))),
    CONSTRAINT simulation_questions_position_check CHECK (("position" > 0))
);


--
-- Name: simulations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.simulations (
    id bigint NOT NULL,
    owner_user_id bigint,
    type text NOT NULL,
    exam_id bigint,
    filter_json jsonb DEFAULT '{}'::jsonb NOT NULL,
    title text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT simulations_check CHECK ((((type = 'REAL_EDITION'::text) AND (exam_id IS NOT NULL)) OR (type = 'BY_DISCIPLINE'::text))),
    CONSTRAINT simulations_type_check CHECK ((type = ANY (ARRAY['BY_DISCIPLINE'::text, 'REAL_EDITION'::text])))
);


--
-- Name: simulations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.simulations ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.simulations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: student_achievements; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.student_achievements (
    user_id bigint NOT NULL,
    achievement_id bigint NOT NULL,
    awarded_at timestamp with time zone DEFAULT now() NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: student_profiles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.student_profiles (
    user_id bigint NOT NULL,
    display_name text NOT NULL,
    school_year text,
    target_year smallint,
    study_goal text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT student_profiles_display_name_check CHECK ((display_name <> ''::text)),
    CONSTRAINT student_profiles_target_year_check CHECK (((target_year IS NULL) OR ((target_year >= 2000) AND (target_year <= 2100))))
);


--
-- Name: student_topic_performance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.student_topic_performance (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    topic_id bigint NOT NULL,
    subtopic_id bigint,
    attempts integer DEFAULT 0 NOT NULL,
    hits integer DEFAULT 0 NOT NULL,
    accuracy numeric(5,4) GENERATED ALWAYS AS (
CASE
    WHEN (attempts = 0) THEN NULL::numeric
    ELSE ((hits)::numeric / (attempts)::numeric)
END) STORED,
    last_attempt_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT student_topic_performance_attempts_check CHECK ((attempts >= 0)),
    CONSTRAINT student_topic_performance_check CHECK ((hits <= attempts)),
    CONSTRAINT student_topic_performance_hits_check CHECK ((hits >= 0))
);


--
-- Name: student_topic_performance_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.student_topic_performance ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.student_topic_performance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: study_plan_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.study_plan_items (
    id bigint NOT NULL,
    study_plan_id bigint NOT NULL,
    topic_id bigint NOT NULL,
    subtopic_id bigint,
    priority smallint NOT NULL,
    reason text NOT NULL,
    evidence_json jsonb NOT NULL,
    status text DEFAULT 'TODO'::text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT study_plan_items_evidence_json_check CHECK ((evidence_json <> '{}'::jsonb)),
    CONSTRAINT study_plan_items_priority_check CHECK (((priority >= 1) AND (priority <= 5))),
    CONSTRAINT study_plan_items_status_check CHECK ((status = ANY (ARRAY['TODO'::text, 'DOING'::text, 'DONE'::text, 'SKIPPED'::text])))
);


--
-- Name: study_plan_items_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.study_plan_items ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.study_plan_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: study_plans; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.study_plans (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    algorithm_version text NOT NULL,
    generated_at timestamp with time zone DEFAULT now() NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: study_plans_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.study_plans ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.study_plans_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: study_sessions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.study_sessions (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    mode text NOT NULL,
    status text DEFAULT 'IN_PROGRESS'::text NOT NULL,
    started_at timestamp with time zone DEFAULT now() NOT NULL,
    finished_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT study_sessions_check CHECK (((finished_at IS NULL) OR (finished_at >= started_at))),
    CONSTRAINT study_sessions_mode_check CHECK ((mode = ANY (ARRAY['ESTUDO'::text, 'PROVA'::text, 'REVISAO'::text]))),
    CONSTRAINT study_sessions_status_check CHECK ((status = ANY (ARRAY['IN_PROGRESS'::text, 'FINISHED'::text, 'ABANDONED'::text])))
);


--
-- Name: study_sessions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.study_sessions ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.study_sessions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: subtopics; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.subtopics (
    id bigint NOT NULL,
    topic_id bigint NOT NULL,
    code text NOT NULL,
    name text NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: subtopics_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.subtopics ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.subtopics_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: topics; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.topics (
    id bigint NOT NULL,
    discipline_id bigint NOT NULL,
    code text NOT NULL,
    name text NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: topics_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.topics ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.topics_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_roles (
    user_id bigint NOT NULL,
    role_id bigint NOT NULL,
    granted_at timestamp with time zone DEFAULT now() NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    email public.citext NOT NULL,
    password_hash text NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    credential_version integer DEFAULT 1 NOT NULL,
    last_login_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT users_credential_version_check CHECK ((credential_version >= 1)),
    CONSTRAINT users_email_check CHECK ((email OPERATOR(public.~) '^[^@]+@[^@]+$'::public.citext)),
    CONSTRAINT users_password_hash_check CHECK ((password_hash <> ''::text))
);


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.users ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Data for Name: achievements; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.achievements (id, code, title, description, rule_json, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: disciplines; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.disciplines (id, code, name, created_at, updated_at) FROM stdin;
1	LINGUA_PORTUGUESA	Língua Portuguesa	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
2	MATEMATICA	Matemática	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
\.


--
-- Data for Name: exam_documents; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.exam_documents (id, exam_version_id, kind, file_name, sha256, pages, generator, note, created_at, updated_at) FROM stdin;
1	1	GABARITO_FINAL	data/provas/2020/Gabarito_Final.pdf	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	1	\N	Anuladas Q7 e Q26	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
2	1	CADERNO	data/provas/2020/Prova.pdf	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	15	Microsoft Word	\N	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
3	2	GABARITO_DEFINITIVO	data/provas/2022/GABARITO_Prova_Exame_Tecnico_Integrado_2022.pdf	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	4	FUNCERN	Contém preliminar (págs. 1–2) + definitivo (págs. 2–4), idênticos	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
4	2	CADERNO	data/provas/2022/Prova_Exame_Tecnico_Integrado_2022_XNn8mg4.pdf	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	15	Microsoft Word	\N	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
5	3	GABARITO_DEFINITIVO	data/provas/2023/Gabarito_Final_-_Exame_de_Selecao_2023.pdf	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	3	\N	Grade na pág. 3; págs. 1–2 = ofertas; anulada Q21	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
6	3	CADERNO	data/provas/2023/Caderno_de_provas.pdf	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	14	Microsoft Word	\N	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
7	4	GABARITO_DEFINITIVO	data/provas/2024/Gabarito_Oficial_Definitivo_IFRN_78.pdf	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	1	\N	Anulada Q17	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
8	4	CADERNO	data/provas/2024/Cadernos_de_provas_-_Cursos_Tecnicos_Integrados_2024_.pdf	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	15	PDFium	\N	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
9	5	GABARITO_DEFINITIVO	data/provas/2025/Exame_de_Seleção_2024_-_Gabarito_Final.pdf	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	1	\N	Nome 2024 × conteúdo 2025; sem anuladas	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
10	5	CADERNO	data/provas/2025/PROVA_IFRN_-_EXAME_DE_SELEÇÃO_2025.pdf	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	14	Microsoft Word	Vedado levar o caderno (novidade 2025–2026)	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
11	6	GABARITO_DEFINITIVO	data/provas/2026/GABARITO_EXAME_DE_SELEÇÃO_2026_-_DEFINITIVO.pdf	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	1	\N	Anulada Q37	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
12	6	CADERNO	data/provas/2026/Caderno_de_Provas_-Técnico_Integrado_2026.pdf	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	14	PDF24	Numeração com U+200B; normalizar na extração	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
\.


--
-- Data for Name: exam_essay_prompts; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.exam_essay_prompts (exam_id, genre, theme, pseudonym, proposal_excerpt, criteria_text, page, created_at, updated_at) FROM stdin;
1	Artigo de opinião	Cyberbullying: a responsabilidade de resolver o problema é da escola?	Ariel da Net	Escreva um artigo de opinião: a responsabilidade de resolver o problema do cyberbullying é da escola?	\N	14	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
2	Artigo de opinião	As terras indígenas devem ser protegidas pelo governo brasileiro?	Potiguar da Silva	Escreva um artigo de opinião sobre a posse das terras indígenas.	Orientações e critérios impressos no caderno (ver PDF-fonte)	14	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
3	Artigo de opinião	A Lei de Cotas e a disputa por vagas nas universidades	Negritude Iorubá	Escreva um artigo de opinião sobre a Lei das Cotas.	\N	13	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
4	Artigo de opinião	A responsabilidade pela preservação da Amazônia é de todos os países?	Amazonense da Silva	Escreva um artigo de opinião sobre a preservação da Amazônia.	\N	14	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
5	Artigo de opinião	O uso de celulares deve ser proibido nas escolas brasileiras?	O REI DA NET	Escreva um artigo de opinião sobre o uso de celulares nas escolas.	\N	13	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
6	Artigo de opinião	O Brasil deve assumir um papel central no combate às mudanças climáticas?	Amazonino Belém	Escreva um artigo de opinião sobre o papel do Brasil no combate às mudanças climáticas.	\N	13	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
\.


--
-- Data for Name: exam_versions; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.exam_versions (id, exam_id, version_code, published_at, note, created_at, updated_at) FROM stdin;
1	1	FINAL	\N	Só o final no repo; preliminar NÃO CONFIRMADA	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
2	2	DEFINITIVO	\N	Preliminar + definitivo idênticos 40/40 no mesmo PDF via FUNCERN; retificação posterior NÃO CONFIRMADA	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
3	3	DEFINITIVO	\N	Só o definitivo no repo; págs. 1–2 listam ofertas, não respostas	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
4	4	DEFINITIVO	\N	Só o definitivo no repo	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
5	5	DEFINITIVO	\N	Arquivo nomeado 2024 com conteúdo de 2025; mapeamento canônico pendente	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
6	6	DEFINITIVO	2025-11-04	Data interna do gabarito; X = questão anulada	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
\.


--
-- Data for Name: exams; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.exams (id, year, edital, duration_minutes, objective_count, lp_count, mat_count, has_essay, scoring_rule, created_at, updated_at) FROM stdin;
1	2020	29/2019	240	40	20	20	t	\N	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
2	2022	041/2021	240	40	20	20	t	\N	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
3	2023	040/2022	240	40	20	20	t	\N	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
4	2024	078/2023	240	40	20	20	t	\N	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
5	2025	23/2024	240	40	20	20	t	\N	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
6	2026	48/2025	240	40	20	20	t	\N	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	schema	SQL	V1__schema.sql	1889696932	voupassar	2026-10-01 00:52:17.686144	274	t
2	2	seed	SQL	V2__seed.sql	-436809676	voupassar	2026-10-01 00:52:18.015889	28	t
3	3	password reset	SQL	V3__password_reset.sql	-388559043	voupassar	2026-10-01 00:52:18.068655	17	t
4	4	attempts selected option	SQL	V4__attempts_selected_option.sql	1646983758	voupassar	2026-10-01 00:52:18.096096	15	t
5	5	review session questions	SQL	V5__review_session_questions.sql	642009638	voupassar	2026-10-01 10:03:36.320021	43	t
6	6	footer cleanup	SQL	V6__footer_cleanup.sql	609357553	voupassar	2026-10-03 00:52:42.880232	77	t
7	7	footer pagenumbers	SQL	V7__footer_pagenumbers.sql	1793433782	voupassar	2026-10-03 00:52:43.030534	18	t
8	8	question figures	SQL	V8__question_figures.sql	-1711068376	voupassar	2026-10-03 00:52:43.075606	38	t
9	9	passages	SQL	V9__passages.sql	1750775050	voupassar	2026-10-03 18:30:00.849931	54	t
\.


--
-- Data for Name: passages; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.passages (id, exam_id, passage_key, label, kind, title, byline, subtitle, intro, content, visual_description, format_note, source_note, page_start, page_end, checksum, created_at, updated_at) FROM stdin;
1	1	TEXTO-1	Texto 1	TEXTO	1 em cada 4 crianças já sofreu ofensas na Internet: cyberbullying desafia pais	Júlia Marques, O Estado de S.Paulo. — 17 de dezembro de 2017 | 03h00	Percentual de vítimas cresce ano a ano: passou de 15% em 2014 para 23% no ano passado. Falta de intimidade de adultos com tecnologia – enquanto crianças são nativas digitais – é uma das explicações para a dificuldade dos pais de identificar riscos	\N	Quando entrou em um colégio novo, na Zona Oeste do Rio, os problemas começaram para Laura, de 13 anos. “Ela é popular. Faz amizade fácil e é bonita. Aquilo provocou a ira de um grupo de colegas”, lembra Rita, de 46 anos, mãe da jovem. Para conter as brigas na escola particular, a menina foi trocada de turno, mas a família jamais imaginaria que, mesmo distante dos antigos colegas, as agressões continuariam em outro espaço: o virtual. “Achei que haveria um basta. Mas foi pior. Pegaram a foto dela e botaram nas redes sociais. Fizeram o horror”, conta a mãe. “Se ela abria o live (vídeo ao vivo na Internet), sempre, entrava um e xingava.” Laura foi ofendida com palavras como “rata” e “demônio” nas redes sociais.\n\nAgressões levaram a família a mudar a jovem de escola; hoje, ela faz acompanhamento psicológico. A situação ficou insustentável até que a mãe trocou a menina de escola no meio do ano. “A foto da minha filha deve andar na Internet. Agora, ela está com trauma, no psicólogo. Amava publicar nas redes e não posta mais.” Os nomes de vítimas e familiares foram trocados para preservá-los. Casos como o de Laura não são isolados. Pesquisa do Comitê Gestor da Internet no Brasil (CGI.br), de outubro, mediu o comportamento online de jovens.\n\nOs dados revelam que, de cada quatro crianças e adolescentes, um foi tratado de forma ofensiva na Internet, o que corresponde a 5,6 milhões de meninos e meninas entre 9 e 17 anos. O percentual cresce ano a ano: passou de 15% em 2014 para 20% em 2015 até chegar a 23% no ano passado. “Nesse dado (sobre ofensas online), a criança ou adolescente foi exposto a um risco, mas não necessariamente teve alguma sequela”, pondera Maria Eugenia Sozio, coordenadora da pesquisa TIC Kids Online Brasil. A taxa, portanto, nem sempre corresponde a cyberbullying – quando a agressão virtual é repetida –, mas faz soar o alerta para perigos que crianças e adolescentes correm na web e a importância da atenção dos pais. Segundo especialistas, as ofensas na Internet podem ter impacto ainda maior na vida das crianças. “Uma postagem atinge número incontável de pessoas e isso aumenta o sofrimento da vítima. Ela não sabe quem viu ou não”, afirma a psicóloga e pesquisadora da Universidade Estadual Paulista (Unesp) Luciana Lapa.\n\nEm casos de agressão na escola, o jovem encontra refúgio em casa. “No cyberbullying, não. Onde quer que ele vá, a agressão vai junto”, diz Luciana. Outro problema é a gravidade das ofensas, encorajadas pela distância física da vítima. Também é comum que as agressões partam de pessoas da mesma faixa etária e que fazem parte do convívio. Para a pedagoga e psicopedagoga clínica e institucional Denise Aragão, as ofensas podem afetar até o desempenho na escola. “As crianças ficam preocupadas em se defender e perdem o desejo de aprender.” O uso crescente dos smartphones pelos jovens, com acesso cada vez mais particular, desafia a mediação dos pais.\n\nA gerente de operações Ana, de 53 anos, conhecia os riscos da Internet, mas se assustou quando passou por uma situação constrangedora na família. Quando a filha tinha 14 anos (hoje ela tem 18), uma foto íntima da garota vazou entre alunos de uma escola particular na Zona Sul paulistana após uma brincadeira entre amigas. Os celulares facilitaram a propagação. “Ela ficou envergonhada. Foi uma semana de constrangimentos”, conta. “Em casa, fizemos questão de explicar o quão sério aquilo era. Mostramos que isso pode ficar no ‘currículo’ dela para o resto da vida”.\n\nA mãe de Helena, de 10 anos, só percebeu o problema depois que notou que a filha estava cabisbaixa e chorava pelos cantos. “Fizeram um grupo no Whatsapp (entre os colegas da escola) para xingá-la por causa da cor. Chamavam de macaca e ‘nega’ do cabelo duro”, conta a assistente administrativa Adriana, de 39 anos. Ela procurou os pais dos agressores. “Fazia uma semana que um deles tinha dado um celular para uma das meninas. Foi aí que ele descobriu. Acho que os pais deveriam prestar mais atenção ao que o filho faz na Internet”, desabafa.\n\nApesar de 23% das crianças e adolescentes terem relatado à pesquisa que foram vítimas de ofensas na Internet, só 11% dos pais disseram que os filhos passaram por incômodos. A falta de intimidade de adultos com a tecnologia – enquanto as crianças são nativas digitais – ajuda a explicar a dificuldade das famílias em identificar riscos. “O gap existe, mas é preciso revertê-lo. Uma sugestão é estar disponível, querer saber o que a criança faz na Internet”, diz Heloisa Ribeiro, da Childhood Brasil, entidade de proteção a crianças e adolescentes.	\N	\N	Texto adaptado para fins pedagógicos. Disponível em: https://brasil.estadao.com.br/geral,1-em-cada-4-criancas-ja-sofreu-ofensas-na-internet-cyberbullying-desafia-pais,70002122721 / Acesso em: 13 ago de 2019.	2	2	6c93e4875e23dbd2243063a2b6bd43ff8b813524e74e1713a5a2c023f41a2913	2026-10-03 18:30:05.114235+00	2026-10-03 18:30:05.114235+00
2	1	TEXTO-2	Texto 2	IMAGEM	\N	\N	\N	\N	\N	Charge colorida com faixa superior preta e o título “Cyberbullying”. Uma mão gigante segura um smartphone cuja tela exibe postagens; abaixo da mão, um menino de boné e mochila aparece encolhido, com expressão assustada. Descrição da curadoria a partir do caderno oficial (p. 3); recorte de imagem pendente (fase de figuras).	\N	Disponível em: https://www.google.com/search?q= +CYBERBULLYING&client=firefox-b-d&tbm / Acesso em: 13 ago de 2019.	3	3	887ae6b6758dbf9df3e4d6dff52c6edb4158ed6907b5540f59f668346b848422	2026-10-03 18:30:05.175768+00	2026-10-03 18:30:05.175768+00
3	1	TRECHO-Q04-07	Trecho — questões 4 a 7	TRECHO	\N	\N	\N	Considere o trecho a seguir para responder às questões de 4 a 7.	Quando entrou em um colégio novo, na Zona Oeste do Rio, os problemas começaram para Laura, de 13 anos. “Ela é popular. Faz amizade fácil e é bonita. Aquilo provocou a ira de um grupo de colegas”, lembra Rita, de 46 anos, mãe da jovem.	\N	No caderno, o trecho aparece emoldurado; os termos “Quando”, “de 13 anos” e “de 46 anos” estão sublinhados.	\N	4	4	760dc0e997d98f26be84d36a3c6daf38ce7a396bb76699deec19aad56ed40207	2026-10-03 18:30:05.202215+00	2026-10-03 18:30:05.202215+00
4	1	TRECHO-Q11-15	Trecho — questões 11 a 15	TRECHO	\N	\N	\N	Considere o trecho para responder às questões de 11 a 15.	Segundo [1] especialistas, as ofensas na Internet podem ter impacto [2] ainda maior na vida das crianças. “Uma postagem atinge número incontável de pessoas e isso aumenta o sofrimento da vítima. Ela [3] não sabe quem viu ou não”, afirma a psicóloga e pesquisadora [4] da Universidade Estadual [5] Paulista (Unesp) Luciana Lapa.	\N	No caderno, o trecho aparece emoldurado; os termos marcados com [1]–[5] estão em negrito sublinhado.	\N	5	5	e84b84bfa65a3aa31e099f8b1b581eb7729a740c0d435d3e3a22c59c5ea8113d	2026-10-03 18:30:05.238538+00	2026-10-03 18:30:05.238538+00
5	2	TEXTO-1	Texto 1	TEXTO	Estudo mostra que o número de invasões em terras indígenas aumentou	05/10/2019 20h59	Uma das questões que vão ser abordadas no Sínodo, no Vaticano, será a situação dos índios que vivem na região amazônica	\N	Estudo mostra que o número de invasões em terras indígenas aumentou. Uma das questões que vão ser abordadas no Sínodo, no Vaticano, será a situação dos índios que vivem na região amazônica. Um estudo do Conselho Indigenista Missionário (CIMI) mostra que o número de invasões em terras indígenas aumentou em 2019.\n\nNa terra indígena Alto Rio Guamá, nordeste do Pará, vivem cerca de dois mil índios, a maioria do povo Tembé. Lá, segundo os caciques, os invasores derrubam a mata nativa para retirar madeira, para a criação de gado e até para plantar maconha. Em setembro, os índios já tinham encontrado acampamentos, espingardas e várias pilhas de madeira em uma área a 30 quilômetros da aldeia. O Ibama, em parceria com a Polícia Federal e a Funai, foi até a região e aplicou cerca de R$ 10 milhões em multas.\n\n“A nossa área preservada tem muito fundamento para a questão indígena, porque sem a área preservada nós não temos como mostrar a nossa cultura”, declara Edinaldo Tembé, cacique Tembé. A grilagem de terra, o desmatamento, o garimpo ilegal e a contaminação dos rios estão entre os principais problemas apontados no relatório sobre violência e invasões nas terras indígenas do CIMI, fundado há 47 anos e ligado à Igreja Católica.\n\nSegundo o levantamento, em 2017, foram 96 invasões em terras indígenas. Em 2018, o número subiu para 111 e apenas de janeiro a setembro de 2019, já foram registradas 160 invasões. Pará, Rondônia e Amazonas são os estados onde houve mais invasões no ano passado. Uma das áreas citadas no relatório é a terra indígena Munduruku, no sudoeste do Pará. De acordo com o CIMI, existem mais de 500 garimpos ilegais na região, desmatando e poluindo os rios da bacia do Tapajós.\n\nO bispo emérito do Xingu, que vai participar do Sínodo no Vaticano, defendeu que os índios precisam ter suas terras protegidas. “Terra é ambiente de se morar, de se viver, é terra dos ritos e dos mitos e de toda a convivência dos povos que estão aqui e isso não pode ser menosprezado, desprezado”, afirma Dom Erwim Krautler.\n\nA Funai declarou que se tem empenhado no monitoramento da região com a ajuda da Polícia Federal, do Ibama e do Instituto Chico Mendes. Disse que a pesquisa do CIMI carece de validação científica e que, em breve, vai divulgar dados oficiais que comprovam o aumento da fiscalização. Para tentar acabar com o garimpo ilegal, o governo já anunciou que vai propor a regulamentação da Constituição nos artigos sobre exploração em terras indígenas. A ideia seria permitir ganhos econômicos e organizar a fiscalização dos garimpos. Antes de qualquer mudança, o governo tem que ouvir as comunidades envolvidas.	\N	\N	Disponível em: https://g1.globo.com/jornal-nacional/noticia/2019/10/05/estudo-mostra-que-o-numero-de-invasoes-em-terras-indigena. Acesso em: 24 set. 2021. Adaptado para uso nesta avaliação.	2	2	40219b186f392ea3b34bb360f793ffa5ec11dd86a8b3c8d668a1c9ff21e5f71b	2026-10-03 18:50:10.121586+00	2026-10-03 18:50:10.121586+00
6	2	TEXTO-2	Texto 2	GRAFICO	Distribuição das Terras Indígenas Regularizadas por região administrativa	\N	\N	\N	\N	Gráfico de barras verticais em tom avermelhado com os percentuais de terras indígenas regularizadas por região administrativa: Norte 54%, Centro-Oeste 19%, Nordeste 11%, Sul 10% e Sudeste 6%. No canto inferior esquerdo do gráfico, a marca “OBSERVATÓRIO SOCIOAMBIENTAL”. Recorte da imagem pendente da fase de figuras; valores transcritos literalmente do caderno.	\N	Disponível em: http://www.observatoriosocioambiental.org/2017/10/descumprindo-ordens-judiciais-e-termo.html. Acesso em: 24 set. 2021.	3	3	2c28164a360de61d7167ab37e71a48853eb198bc2d8e986b89a9cee1a522de17	2026-10-03 18:50:10.159711+00	2026-10-03 18:50:10.159711+00
7	2	TEXTO-3	Texto 3	CHARGE	\N	\N	\N	\N	\N	Charge colorida assinada “LATUFF 2016 CIMI”: um juiz de toga e barrete pretos, com o braço estendido sobre uma área demarcada com ocas e placas “ÁREA INDÍGENA”, traz no braço a faixa “MARCO TEMPORAL”. Em dois balões de fala o juiz diz “DE 1988 PRA FRENTE...” e “...O ESBULHO TÁ LIBERADO!”. Ao lado, um homem de chapéu de vaqueiro opera um trator de esteira amarelo sobre as ocas. Recorte da imagem pendente da fase de figuras; falas e letreiros transcritos literalmente do caderno.	\N	Disponível em: https://twitter.com/latuffcartoons/status/760446245869645824. Acesso em: 24 set. 2021.	3	3	91f91b8b6f24dfeb55ee0282da52eb6520c1649e0aa2ea77a146a0d3462ed842	2026-10-03 18:50:10.197366+00	2026-10-03 18:50:10.197366+00
8	2	TRECHO-Q04-06	Trecho — questões 4 a 6	TRECHO	\N	\N	\N	Leia o trecho a seguir para responder às questões 04, 05 e 06.	Lá, segundo os caciques, os invasores derrubam a mata nativa para retirar madeira, para a criação de gado e até para plantar maconha.	\N	No caderno, o trecho aparece emoldurado; os vocábulos “Lá”, “para” (o primeiro) e “até” estão em negrito.	\N	4	4	30bd9a14b8d50ef508d2e9326332073be9f3a807a51e8e8d31b3dbde1383b0fd	2026-10-03 18:50:10.237864+00	2026-10-03 18:50:10.237864+00
9	2	TRECHO-Q08	Trecho — questão 8	TRECHO	\N	\N	\N	Considere o trecho para responder à questão 8	Segundo o levantamento, em 2017, foram 96 invasões em terras indígenas.	\N	No caderno, o trecho aparece emoldurado; o vocábulo “Segundo” está em negrito sublinhado.	\N	5	5	8fd794635dacc94bc61e4203208c20714196f2c66b0ea44ddc735e80bbc4c691	2026-10-03 18:50:10.282397+00	2026-10-03 18:50:10.282397+00
10	2	TRECHO-Q09-12	Trecho — questões 9 a 12	TRECHO	\N	\N	\N	Considere o trecho abaixo retirado do Texto 1 para responder às questões 09, 10, 11 e 12.	Pará, Rondônia e Amazonas são os estados onde houve mais invasões no ano passado. Uma das áreas citadas no relatório é a terra indígena Munduruku, no sudoeste do Pará. De acordo com o CIMI, existem mais de 500 garimpos ilegais na região, desmatando e poluindo os rios da bacia do Tapajós.	\N	No caderno, o trecho aparece emoldurado; os termos “houve”, “relatório” e “do Tapajós” estão em negrito sublinhado.	\N	5	5	89dd6db1b61bc9012bb525f874f7178c300553bbe7ebec2a79e2cf175a82dc6a	2026-10-03 18:50:10.313627+00	2026-10-03 18:50:10.313627+00
11	3	TEXTO-1	Texto 1	TEXTO	Dez anos da Lei das Cotas	Rosane Garcia — postado em 08/07/2022 | 06:00	\N	\N	A Lei das Cotas (nº 12.711/2012), que reserva 50% das vagas em universidade e institutos federais para negros, completará uma década em 28 de agosto próximo. Ao longo desses 10 anos, ela se tornou mais inclusiva, ao abrir espaço para indígenas e estudantes da rede pública, segmentos da sociedade que, como os negros, foram, historicamente, segregados pelo Estado, pautado pelo racismo estrutural e ambiental, base orientadora da formulação de políticas públicas excludentes.\n\nVista como uma reparação por danos provocados aos negros, após mais de 300 anos de escravidão, o novo marco legal esbarrou na reação dos abomináveis racistas e escravagistas contemporâneos, indignados com a conquista do movimento negro, que completou 40 anos neste 2022. Congressistas foram ao Supremo Tribunal Federal para tentar derrubar a nova lei.\n\nEntre os argumentos, havia a alegação de que a regra privilegia pretos e pardos em detrimento dos direitos dos brancos, tornando desigual a disputa por uma vaga nas universidades. É puro deboche falar em desigualdade, quando todas as ações do poder público e setor privado sempre mortificaram o artigo da Constituição anterior a de 1988, em que todos "são iguais perante as leis". Escárnio.\n\nUm discurso ridículo, ante os mais de 300 anos de escravidão, tortura e assassinatos de homens, mulheres e crianças sequestrados em terras africanas. Um crime de lesa-humanidade nunca reparado pelo Estado brasileiro. Pelo contrário. Ter muitos escravos e ser dono de grandes extensões de terra — latifúndios — representavam o poder político do explorador. Hoje, a eliminação de negros ainda é fato corriqueiro, protagonizado por integrantes de forças de segurança pública em investidas nas periferias dos centros urbanos.\n\nEm uma década, a Lei das Cotas mudou o perfil das universidades brasileiras. Nesse período, o número de alunos negros cresceu em torno de 400%. Hoje, eles somam mais de 38% dos estudantes, embora o povo preto seja 56% da população brasileira.\n\nO movimento pela diversidade tem mexido com as organizações privadas. Hoje, grandes corporações do setor privado com compromisso social, nos mais diferentes ramos de atividade, criam programas para que profissionais negros ocupem cargos de chefia ou de coordenação dentro das empresas. Uma mudança importante no comportamento do empresariado, cujo olhar passa a enxergar a pluralidade étnica e, também, de gêneros no país.\n\nMesmo com todos os efeitos positivos, a Lei das Cotas tem sido rechaçada por grupos conservadores e neonazistas, inconformados com a presença dos negros nas universidades ou em postos de mando em algumas empresas. Esses segmentos têm representantes no Congresso e incitam os parlamentares a não revalidarem a Lei das Cotas. As eleições de novembro podem ser um escudo, para evitar o estilhaçamento da legislação. Mas, considerando-se a atual composição do parlamento, é preciso ficar atento, pois o compromisso da maioria não tem sido, em hipótese alguma, com a sociedade. E, menos ainda, com os afro-brasileiros.	\N	No caderno, o título está centralizado em negrito e o nome da autora/data ('Rosane Garcia — postado em 08/07/2022 | 06:00') alinhados à direita; corpo em 7 parágrafos sem marcas de questão.	Texto adaptado para fins pedagógicos. Disponível em: https://www.correiobraziliense.com.br/opiniao/2022/07/5020827-artigo-dez-anos-da-lei-das-cotas.html. Acesso em: 28 ago. 2022	2	2	391fd5969d6a6bc7e802adfb9c6f25929ace61f540f7312fe75f915089188c55	2026-10-03 19:38:25.098028+00	2026-10-03 19:38:25.098028+00
12	3	TEXTO-2	Texto 2	CHARGE	\N	\N	\N	\N	\N	Charge colorida: um homem branco de terno azul segura uma bola de futebol com as duas mãos diante de um menino negro de camiseta verde-clara, calça azul e tênis vermelhos; no balão de fala verde-claro lê-se: 'UNIVERSIDADE PRA QUÊ? VOCÊ JOGA FUTEBOL TÃO BEM, TOMA SUA COTA!'. Há assinatura vertical à esquerda do desenho.	\N	Disponível em: https://nanabritomorais.jusbrasil.com.br/114688343/as-cotas-para-negros-por-que-mudei-de-opiniao. Acesso em: 28 ago. 2022.	3	3	b92608b22caf5d5110f56ede03476013d6dc7a964fbfb4ef99165b54aefb66c8	2026-10-03 19:38:25.165012+00	2026-10-03 19:38:25.165012+00
13	3	TRECHO-Q06-12	Trecho — questões 6 a 12	TRECHO	\N	\N	\N	Considere o trecho para responder às questões de 06 a 12.	Entre os argumentos, havia a alegação de que (1) a regra privilegia pretos e pardos em detrimento dos direitos dos brancos, tornando desigual a disputa por uma vaga nas universidades. É puro deboche falar em desigualdade, quando todas as ações do poder público e setor privado sempre mortificaram o artigo da Constituição anterior a de 1988, em que (2) todos (3) "são iguais perante as leis" (3). Escárnio (4).	\N	No caderno, o trecho aparece emoldurado; os elementos 'que (1)', 'que (2)', 'todos (3)', '(3)' após as aspas e 'Escárnio (4)' estão em negrito. Corresponde ao 3º parágrafo do Texto 1, acrescido dos marcadores (1)–(4).	\N	4	4	648b81aa297f62301d9f997086d485d6a9fc75508c0f45a261c989f411bf756d	2026-10-03 19:38:25.204271+00	2026-10-03 19:38:25.204271+00
14	3	TRECHO-Q13-15	Trecho — questões 13 a 15	TRECHO	\N	\N	\N	Considere o trecho para responder às questões 13 a 15.	Mesmo com todos os efeitos positivos, (1) a Lei das Cotas tem sido rechaçada por grupos conservadores e neonazistas (2), inconformados com a presença dos negros nas universidades (3) ou em postos de mando em algumas empresas (4).	\N	No caderno, o trecho aparece emoldurado; os elementos ', (1)', 'por grupos conservadores e neonazistas (2)', 'nas universidades (3)' e 'em algumas empresas (4)' estão em negrito. Corresponde ao 1º período do último parágrafo do Texto 1, acrescido dos marcadores (1)–(4).	\N	6	6	d355c87e145912afd537da48bc43e42c6f8a3eb93716261627b3605d000c560c	2026-10-03 19:38:25.251834+00	2026-10-03 19:38:25.251834+00
15	4	TEXTO-1	Texto 1	TEXTO	O QUE É A CÚPULA DA AMAZÔNIA E QUAIS AS EXPECTATIVAS PARA O EVENTO NO PARÁ	Amazônia paraense (Leandro Fonseca/Exame) — Marina Filippe — Publicado em 3 de agosto de 2023 às, 07h00	Cúpula da Amazônia, em Belém, receberá chefes de estado dos oito países que possuem a floresta no seu território, além da sociedade civil.	\N	Belém do Pará sedia, nos próximos dias 8 e 9, a Cúpula da Amazônia. O evento receberá chefes de estado dos oito países que possuem a floresta no seu território, além da sociedade civil. O objetivo desse evento é definir políticas e estratégias para o desenvolvimento sustentável da região. A Cúpula pode ser ainda uma prévia das discussões globais durante a COP30, prevista para 2025, visto que, em maio deste ano, o presidente Luiz Inácio Lula da Silva confirmou a intenção de realizar o evento em uma cidade da Amazônia Legal brasileira.\n\nO que é a Cúpula da Amazônia\n\nA Cúpula da Amazônia é um evento de dois dias na cidade de Belém, no Pará. Na ocasião, serão abordadas as políticas públicas da região amazônica e o fortalecimento da Organização do Tratado de Cooperação da Amazônia (OTCA). “Os temas amazônicos não podem ser resolvidos apenas por um país. Desde os anos 1970, quando se negociou o tratado, se reconheceu que é necessário haver articulação entre os oito países que detêm o bioma, sendo eles Brasil, Bolívia, Colômbia, Equador, Guiana, Peru, Suriname e Venezuela”, diz Gisela Padovan, secretária de América Latina e Caribe no Ministério das Relações Exteriores.\n\nQuem participa da Cúpula da Amazônia\n\nA Cúpula deste mês será a quarta reunião dos presidentes dos países participantes do tratado. Isto é, a primeira desde 2009. Além dos oito países, foram convidados o Congo, a República Democrática do Congo e a Indonésia (países com florestas tropicais), São Vicente e Granadinas (país que ocupa a presidência da CELAC), a França (pela Guiana Francesa), a Alemanha e a Noruega (principais doadores do Fundo Amazônia), além do presidente da COP28 e de representantes de bancos de fomento, como BID, NBD (Banco dos Brics), entre outros. Espera-se também a participação da sociedade civil. Pensando nisso, organizações se uniram para a produção de um documento que aponta os principais temas a serem tratados para a preservação da Amazônia. “Mais de 60 organizações são signatárias da carta que aponta o que pode ser a contribuição da sociedade civil para a Cúpula da Amazônia a partir de temas prioritários como foco nas populações mais vulneráveis, a preservação do bioma e questão climática em si”, disse JP Amaral, gerente de meio ambiente e clima do Instituto Alana, em coletiva de imprensa da rede Observatório do Clima.\n\nQual a importância da Cúpula da Amazônia 2023\n\nDe acordo com o documento do Observatório do Clima, estudos recentes têm levantado a hipótese de o bioma amazônico entrar em colapso por conta do efeito combinado de desmatamento e mudança do clima. Isso poderia ocasionar um desmatamento em cerca de 25% da área da floresta. “A Amazônia tem mudado muito. Na região nordeste da floresta, por exemplo, o desmatamento chega a 40%. Isso significa um colapso climático que já chegou e pode piorar”, diz Luciana Gatti, pesquisadora do Instituto Nacional de Pesquisas Espaciais.\n\nEm 2021, um grupo do INPE mostrou, após coletar nove anos de dados, que algumas regiões da Amazônia brasileira já emitem mais gás carbônico para a atmosfera do que capturam, revertendo o papel do ecossistema de sorvedouro para ao menos uma parte do carbono extra lançado pelos seres humanos no ar.\n\nApesar dos dados alarmantes, o presidente Lula considera a Cúpula como momento para a organização das políticas e fim do desmatamento. “O Brasil vai cumprir com aquilo que foi prometido, nós vamos chegar ao desmatamento zero em 2030, escrevam e guardem para me cobrar”, disse em café da manhã com correspondentes da imprensa internacional. Além disso, um documento elaborado no evento deve ser apresentado na COP28, em dezembro, em Dubai.\n\nDiálogos Amazônicos: programação\n\nAlém de palestras e eventos paralelos à Cúpula, nos dias 8 e 9, a cidade de Belém receberá outros eventos a partir da sexta-feira, 4 de agosto. A programação faz parte dos Diálogos Amazônicos, com cinco plenárias principais, quatro plenárias transversais e mais de 400 atividades organizadas por movimentos sociais, entidades e órgãos governamentais.\n\nCada uma das plenárias-síntese terá 7 expositores: 4 da sociedade civil e 3 governamentais, sendo pelo menos 1 brasileiro em cada categoria. As plenárias-síntese debaterão temas como participação social, erradicação do trabalho escravo, saúde, soberania, segurança alimentar e nutricional, ciência e tecnologia, transição energética, mudança do clima e a proteção aos povos indígenas e tradicionais da região.\n\nÉ a partir dos debates desses encontros que serão produzidos os cinco relatórios que serão entregues aos presidentes que participarão da Cúpula da Amazônia, também em Belém, nos dias 8 e 9 de agosto. Os documentos serão entregues por cinco representantes da sociedade civil escolhidos entre os participantes dos Diálogos Amazônicos.\n\nJá as plenárias transversais vão debater as situações de públicos específicos como as mulheres, os jovens e os negros na região amazônica. As três reuniões previstas na programação acontecerão no mesmo espaço das plenárias-síntese, no intervalo entre as duas reuniões principais que acontecerão nos dias 5 e 6 de agosto. As discussões dessas plenárias poderão ter trechos incluídos nos relatórios das plenárias-síntese. A programação completa dos Diálogos Amazônicos já está disponível no site do Governo Federal. A expectativa é de que, presencialmente, 10.000 pessoas participem das atividades.	\N	No caderno, o título está em caixa alta e negrito; o subtítulo em itálico; os intertítulos ('O que é a Cúpula da Amazônia', 'Quem participa da Cúpula da Amazônia', 'Qual a importância da Cúpula da Amazônia 2023', 'Diálogos Amazônicos: programação') em negrito; a expressão 'Diálogos Amazônicos' em negrito na primeira ocorrência do corpo. Crédito da foto ('Amazônia paraense (Leandro Fonseca/Exame)') impresso acima do nome da autora.	\N	2	3	81e323b1c70f19dee8ea137d120ccb885f63d92a81d90c2ff2fb625786753041	2026-10-03 19:57:05.062838+00	2026-10-03 19:57:05.062838+00
16	4	TEXTO-2	Texto 2	CHARGE	\N	\N	\N	\N	\N	Charge colorida com o título 'MOMENTO IMPORTANTE...' em vermelho no topo: à esquerda, uma televisão sobre um móvel exibe três apresentadoras de programa de auditório de braços erguidos; um homem careca de camiseta branca, segurando um controle remoto, diz no balão de fala: 'VOCÊ TINHA RAZÃO, FILHO, ESTA É UMA DAS REUNIÕES MAIS IMPORTANTES DA NOSSA ÉPOCA.'; à direita, um menino de camiseta laranja com a mão no rosto responde no balão de fala: 'EU TAVA FALANDO DA CÚPULA DA AMAZÔNIA! E NÃO DA REUNIÃO ENTRE A XUXA, ANGÉLICA E A ELIANA!'. Assinatura 'PAIXÃO' no canto inferior direito. Recorte da imagem pendente da fase de figuras; falas e letreiros transcritos literalmente do caderno.	\N	Disponível em: https://www.google.com/search?q=cupula+da+amazonia+&tbm=isch&ved=2ahUKEwjSrIGdhtGAAxVanpUCHQrW. Acesso em: 09 ago. 2023.	3	3	1aee65a0f977a21e76e7fb832bca25016f740afaeb690e260a9a3c312f8a0ccb	2026-10-03 19:57:05.119984+00	2026-10-03 19:57:05.119984+00
17	4	TEXTO-3	Texto 3	GRAFICO	Desmatamento anual na Amazônia de janeiro a dezembro (km²)	\N	\N	\N	\N	Gráfico de barras verticais em tons de marrom com o desmatamento anual na Amazônia, de janeiro a dezembro, em km²: 2008: 2259; 2009: 1887; 2010: 1488; 2011: 1415; 2012: 1769; 2013: 1144; 2014: 2995; 2015: 3098; 2016: 3641; 2017: 2607; 2018: 5078; 2019: 6200; 2020: 8058; 2021: 10362; 2022: 10573 (valores impressos em branco dentro das barras). Eixo vertical de 0 a 11000. Abaixo do gráfico, a legenda 'Fonte: Sistema de Alerta de Desmatamento do Imazon (SAD)'. Recorte da imagem pendente da fase de figuras; título, valores e fonte transcritos literalmente do caderno.	\N	Fonte: Sistema de Alerta de Desmatamento do Imazon (SAD). Disponível em: https://imazon.org.br/imprensa/amazonia-perdeu-quase-3-mil-campos-de-futebol-por-dia-de-floresta-em-2022-maior-desmatamento-em-15-anos. Acesso em: 09 ago. 2023.	4	4	d85d099017fba95c0bea667c91a16e1ad95c7b8426b44151135d0a02f968ba07	2026-10-03 19:57:05.167376+00	2026-10-03 19:57:05.167376+00
18	4	TRECHO-Q06-09	Trecho — questões 6 a 9	TRECHO	\N	\N	\N	Considere o trecho para responder às questões de 06 e 09.	Em 2021, um grupo do INPE mostrou, após coletar nove anos de dados, que (1) algumas regiões da Amazônia brasileira já emitem mais gás carbônico para a atmosfera do que capturam, revertendo o papel do ecossistema de sorvedouro para ao menos uma parte do carbono extra (2) lançado pelos seres humanos no ar.	\N	No caderno, o trecho aparece emoldurado; os elementos 'que (1)' e 'extra (2)' estão em negrito. Corresponde ao 5º parágrafo do Texto 1, acrescido dos marcadores (1)–(2).	\N	5	5	f6ef830d8bd4892e0d7c799cfb9b18986eae814e03c5dfe7dc2ce3cf2b22f029	2026-10-03 19:57:05.215283+00	2026-10-03 19:57:05.215283+00
19	4	TRECHO-Q10-13	Trecho — questões 10 a 13	TRECHO	\N	\N	\N	Considere o trecho para resolver às questões de 10 a 13.	Apesar dos dados alarmantes, o presidente Lula considera a Cúpula como momento para a organização das políticas e fim do desmatamento. “(1) O Brasil vai cumprir com aquilo que (2) foi prometido, nós vamos chegar ao desmatamento zero em 2030, escrevam e guardem para me cobrar”(3), disse em café da manhã com correspondentes da imprensa internacional. Além disso (4), um documento elaborado no evento (5) deve ser apresentado na COP28, em dezembro, em Dubai.	\N	No caderno, o trecho aparece emoldurado; os elementos '(1)', 'que (2)', '"(3)', 'Além disso (4)' e 'evento (5)' estão em negrito. Corresponde ao 6º parágrafo do Texto 1, acrescido dos marcadores (1)–(5).	\N	6	6	0afc917fdafa959024b9e6f6999a6fd65e4654d2abc3f56a1be74bc1aa1ef8fe	2026-10-03 19:57:05.265061+00	2026-10-03 19:57:05.265061+00
20	4	TRECHO-Q15-17	Trecho — questões 15 a 17	TRECHO	\N	\N	\N	Considere o trecho para responder às questões 15 a 17.	Eu tava falando da Cúpula da Amazônia (1)! E não da reunião entre a Xuxa, Angélica e a Eliana.	\N	No caderno, o trecho aparece emoldurado; o elemento 'da Amazônia (1)' está em negrito. Corresponde à fala do menino na charge do Texto 2.	\N	7	7	75b1ad21865d62cb53fa664a1cc957ab233eb2f0c4d30b48ebe48dee5d7e21bc	2026-10-03 19:57:05.311456+00	2026-10-03 19:57:05.311456+00
21	4	TRECHO-Q25-27	Trecho — questões 25 a 27	TRECHO	\N	\N	\N	Considere o trecho para resolver as questões 25, 26 e 27.	O tamanho dos aparelhos de televisão, em geral, é dado em polegadas e corresponde à medida da diagonal da tela do aparelho. Uma polegada é equivalente a 2,5 cm. No Texto 2, é possível ver uma televisão de formato retangular, sobre uma mesa. Suponha que as dimensões dessa tela plana são 95 cm de largura e 53 cm de altura.	\N	No caderno, o trecho aparece emoldurado; a expressão 'Texto 2' está em negrito.	\N	10	10	c47fee862fe1c9b066fc8c7cf4a73d2f37e44d3531222d9eae8435bb694dfc68	2026-10-03 19:57:05.362261+00	2026-10-03 19:57:05.362261+00
22	5	TEXTO-1	Texto 1	TEXTO	RIO PROÍBE CELULARES NAS ESCOLAS ATÉ NO RECREIO	Por G1 Rio, 02/02/2024 05h25	A medida veio depois de uma consulta pública, aberta em dezembro, em que 83% dos respondentes concordaram com a restrição.	\N	Um decreto do prefeito Eduardo Paes (PSD) publicado no Diário Oficial desta sexta-feira (2) proíbe o uso de celulares nas escolas da rede municipal – inclusive no recreio –. A medida entra em vigor em 30 dias. Desde agosto, o estudante da prefeitura já não podia pegar no telefone dentro da classe, somente nos intervalos. A partir de março, nem isso. Diz o decreto: "Fica proibida a utilização de celulares e outros dispositivos eletrônicos pelos alunos nas unidades escolares da rede pública municipal de ensino nas seguintes situações: dentro da sala de aula; fora da sala de aula quando houver explanação do professor e/ou realização de trabalhos individuais ou em grupo na unidade escolar; durante os intervalos, incluindo o recreio".\n\n"Os celulares e demais dispositivos eletrônicos deverão ser guardados na mochila ou bolsa do próprio aluno, desligado ou ligado em modo silencioso e sem vibração", especifica o decreto. "A gente acredita que a escola é um local de aprendizagem e interação social. As crianças não podem continuar ficando isoladas nas suas próprias telas, sem interagir umas com as outras, sem brincar. A escola precisa dessa interação humana", disse o secretário municipal de Educação, Renan Ferreirinha. 8 em cada 10 pessoas são a favor de proibir o uso de celulares em escolas do Rio.\n\nExceções\n\nO aluno poderá mexer no celular nas seguintes exceções:\n\n• antes da primeira aula do dia, desde que fora da sala;\n\n• após a última aula do dia, desde que fora da sala;\n\n• quando houver autorização expressa do professor regente para fins pedagógicos, como pesquisas, leituras ou acesso ao material Rioeduca;\n\n• para os alunos com deficiência ou com condições de saúde que necessitam destes dispositivos para monitoramento ou auxílio de sua necessidade;\n\n• durante os intervalos, incluindo o recreio, quando a cidade estiver classificada a partir do Estágio Operacional 3;\n\n• quando houver autorização expressa da equipe gestora da unidade escolar em casos que ensejem o fechamento ou interrupção temporária das atividades da unidade escolar, de acordo com o protocolo do programa Acesso Mais Seguro;\n\n• durante os intervalos para os alunos da Educação de Jovens e Adultos;\n\n• quando houver autorização expressa da equipe gestora da unidade escolar por motivos de força maior.\n\nEm caso de descumprimento pelo aluno, "o professor poderá advertir o aluno e/ou cercear o uso dos dispositivos eletrônicos em sala de aula, bem como acionar a equipe gestora da unidade escolar".\n\nJustificativas\n\nA medida veio depois de uma consulta pública, aberta em dezembro, em que 83% dos respondentes concordaram com a restrição. O resultado saiu no último dia 23. Ao justificar o decreto, Paes citou a própria pesquisa da Secretaria Municipal de Educação e recomendações da Organização Mundial de Saúde (OMS) e da Organização das Nações Unidas para a Educação, a Ciência e a Cultura (Unesco) sobre limites no tempo de tela para crianças. "A análise [da Unesco] de uma grande amostra de jovens com idades entre 2 e 17 anos nos Estados Unidos mostrou que um maior tempo de tela estava associado a uma piora do bem-estar; menos curiosidade, autodisciplina e estabilidade emocional; maior ansiedade e diagnósticos de depressão", escreveu Paes.\n\nA tecnologia pode ter um impacto negativo se for inadequada ou excessiva. Dados de avaliações internacionais em larga escala, tais como os fornecidos pelo Programa de Avaliação Internacional de Estudantes (Pisa) – maior avaliação mundial de estudantes –, sugerem uma correlação negativa entre o uso excessivo das tecnologias [...] e o desempenho acadêmico. Descobriu-se que a simples proximidade de um aparelho celular era capaz de distrair os estudantes e provocar um impacto negativo na aprendizagem em 14 países", prosseguiu o prefeito. Paes afirmou ainda que "estudos da Bélgica, Espanha e Reino Unido mostram que proibir telefones celulares nas escolas melhora o desempenho acadêmico, especialmente para estudantes com baixo desempenho". "Um relatório da Organização para Cooperação e Desenvolvimento Econômico (OCDE), responsável pelo Pisa, revela que 45% dos alunos relataram sentir-se nervosos ou ansiosos se seus telefones não estivessem perto deles, em média, nos países da OCDE, e 65% relataram serem distraídos pelo uso de dispositivos digitais em pelo menos algumas aulas de matemática. A proporção ultrapassou 80% na Argentina, Brasil, Chile, Finlândia e Uruguai", detalhou.	\N	No caderno, o título está em caixa alta centralizada, a linha de autoria alinhada à direita e o primeiro parágrafo (lead) em itálico; 'Exceções' e 'Justificativas' são intertítulos em negrito; as exceções aparecem como lista com marcadores; o '(2)' após 'sexta-feira' consta no original.	Disponível em: <https://g1.globo.com/rj/rio-de-janeiro/2024/02/02/decreto-celular-escolas.ghtml>. Acesso em: 14 Jul 2024. (Texto adaptado para uso nesta avaliação).	2	3	4a1651de0175c5cf9c8cfe31c1eb0594f85f59a0f64a3e1e31bb2f9d4c3b1f1f	2026-10-03 20:15:07.360911+00	2026-10-03 20:15:07.360911+00
23	5	TEXTO-2	Texto 2	CHARGE	\N	\N	\N	\N	\N	Charge colorida em quadro único: à esquerda, um menino sentado em carteira escolar tira uma selfie com um celular; no balão de fala: 'Projeto de lei autoriza o uso do celular nas escolas para fim pedagógico. LEGAL!'. À direita, três livros antropomorfizados (com pernas, braços e expressões faciais) afastam-se caminhando; nos balões: 'CHEGOU NOSSA VEZ, GALERA...' e '...VAMOS LÁ PEGAR O SEGURO-DESEMPREGO!'. Ao fundo, janela e quadro verde de sala de aula. Assinatura estilizada no canto inferior direito.	\N	Disponível em: <https://omunicipio.com.br/uso-de-celulares-nas-escolas/>. Acesso em: 15 jul. 2024.	3	3	8b51fe8e533174ef3cbeb35baed2693fd5042804a65cd67d1c33cb10b4c71c12	2026-10-03 20:15:07.437013+00	2026-10-03 20:15:07.437013+00
24	5	TRECHO-Q06-09	Trecho — questões 6 a 9	TRECHO	\N	\N	\N	Utilize o trecho para responder às questões 06 a 09.	Diz o decreto: (1) "Fica proibida a utilização de celulares e outros dispositivos eletrônicos (2) pelos alunos nas unidades escolares da rede pública municipal de ensino nas seguintes situações: (3) dentro da sala de aula; fora da sala de aula quando houver explanação (4) do professor e/ou realização de trabalhos individuais ou em grupo na unidade escolar; durante os intervalos, incluindo o recreio".	\N	No caderno, o trecho aparece emoldurado; os marcadores (1)–(4) estão em negrito; os termos 'a utilização de celulares e outros dispositivos eletrônicos' e 'explanação' estão sublinhados. Corresponde ao trecho final do 1º parágrafo do Texto 1, acrescido dos marcadores (1)–(4).	\N	4	4	6fc6e589de85baa972b1660caaf3befadb3afb96218a29c6be1c08dee1f22b90	2026-10-03 20:15:07.493716+00	2026-10-03 20:15:07.493716+00
25	5	TRECHO-Q10-14	Trecho — questões 10 a 14	TRECHO	\N	\N	\N	Considere o trecho do Texto 1 para responder às questões de 10 a 14.	"Os celulares e demais dispositivos eletrônicos deverão (1) ser guardados na mochila ou bolsa do próprio aluno, desligado ou ligado em modo silencioso e sem vibração", especifica o decreto. "A gente (2) acredita que a escola é um local de aprendizagem e interação social. As crianças não podem continuar ficando isoladas nas suas próprias telas, sem interagir umas com as outras, sem brincar (3). A escola precisa dessa interação humana", disse o secretário municipal de Educação, Renan Ferreirinha. 8 em cada 10 pessoas são a favor de proibir o uso de celulares em escolas do Rio.	\N	No caderno, o trecho aparece emoldurado; os marcadores (1)–(3) estão em negrito; os termos 'deverão' e 'A gente' e o período 'As crianças não podem continuar ficando isoladas nas suas próprias telas, sem interagir umas com as outras, sem brincar' estão sublinhados. Corresponde ao 2º parágrafo do Texto 1, acrescido dos marcadores (1)–(3).	\N	5	5	0fd789f8e18b8ae11a8b3dfb480844de4b62667b9a1ed49c83c94fdd106e8887	2026-10-03 20:15:07.55157+00	2026-10-03 20:15:07.55157+00
26	6	TEXTO-1	Texto 1	TEXTO	COP30 no Brasil: o que é, quando acontece e por que é tão importante para o futuro do clima	Por: Redação em 24/07/2025 ‖ Atualizado: 24/07/2025, às 11:02	A escolha de Belém como sede da COP30 transcende uma celebração simbólica dos 10 anos do Acordo de Paris, prometendo marcar um momento de ação concreta e compromissos efetivos.	\N	A 30ª Conferência das Nações Unidas sobre Mudança do Clima (Conferência das Partes – COP30 –), que acontecerá entre os dias 10 e 21 de novembro na cidade de Belém, capital do Pará, já está movimentando o Brasil de diversas maneiras. O evento, que ocorre anualmente, reúne líderes mundiais, cientistas, organizações não governamentais e representantes da sociedade civil para discutir o futuro do planeta, por meio de ações para combater as mudanças climáticas. Na edição anterior, realizada em Baku, Azerbaijão, a conferência enfrentou críticas por não estabelecer metas suficientemente ambiciosas para mitigar os efeitos das mudanças climáticas.\n\nDurante o evento, o país terá a responsabilidade de apresentar seus esforços em áreas como energias renováveis, biocombustíveis e agricultura de baixo carbono. Para o governo brasileiro, a COP30 é uma oportunidade singular de reforçar o papel do Brasil como líder nas discussões globais sobre mudanças climáticas e sustentabilidade. Os temas centrais da COP30 abrangerão a redução de emissões de gases de efeito estufa, a adaptação às mudanças climáticas, o financiamento climático para países em desenvolvimento, tecnologias de energia renovável e soluções de baixo carbono, além da preservação de florestas e biodiversidade. A justiça climática e os impactos sociais das mudanças climáticas também fazem parte dos temas centrais do encontro.\n\nSegundo estimativas da Fundação Getúlio Vargas (FGV), espera-se que a COP30 atraia cerca de 40 mil visitantes. Dentre eles, pelo menos, 7 mil serão integrantes da ONU e de delegações de países-membros. A escolha de Belém como sede da COP30 transcende uma celebração simbólica dos 10 anos do Acordo de Paris, prometendo marcar um momento de ação concreta e compromissos efetivos. Conforme afirmou o embaixador André Corrêa do Lago, presidente da conferência, este é um momento que exige ação concreta e a implementação de compromissos já firmados. Em entrevista à Jovem Pan, Lago deixou claro que o evento será um marco de transição entre a diplomacia climática e a execução de políticas sustentáveis. Mais de 30 anos após sediar a Rio-92, evento tido como a pedra fundamental para o estabelecimento das COP, o Brasil recebe novamente uma conferência de tal magnitude.\n\nMas afinal, o que é a COP?\n\nA Conferência das Partes (COP) é o órgão decisório da Convenção-Quadro das Nações Unidas sobre Mudança do Clima (CQNUMC ou UNFCCC). Sua função é implementar os compromissos globais de combate às mudanças climáticas assumidos pelos países signatários e ratificadores da Convenção. Atualmente, 198 nações participam da UNFCCC, tornando-a um dos maiores organismos multilaterais da Organização das Nações Unidas (ONU). A COP representa a cúpula global do clima, que é realizada anualmente em um país diferente. Ela também funciona como Reunião das Partes para o Protocolo de Quioto (CMP) e o Acordo de Paris, cujo objetivo principal é mitigar o aquecimento global e manter o aumento da temperatura global abaixo de 2º C, com esforços para limitá-lo a 1,5º C.\n\nNa dinâmica da COP, que ocorre ao longo de duas semanas, a primeira semana é dedicada a discussões técnicas, enquanto a segunda é voltada para encontros políticos e assinatura dos acordos. Os resultados devem ser alcançados por consenso, garantindo que todos os países tenham direito a voto. Durante a COP, eventos ocorrem simultaneamente todos os dias. A conferência é dividida em Zona Azul e Zona Verde. A Zona Azul, que é gerenciada diretamente pela ONU, é onde acontecem as negociações políticas e os encontros diplomáticos. Já a Zona Verde sedia painéis para o público geral, apresentação de ONG e outras atividades, inclusive as culturais.\n\nComo o Brasil está se preparando para receber a COP30?\n\nBelém é a cidade escolhida para sediar o evento que acontecerá em novembro. A capital paraense tem a missão de recuperar a posição de “capital da Amazônia”. Uma força-tarefa foi montada para preparar a cidade e a região para receber a COP30. Neste sentido, a prefeitura da cidade e o governo do estado estão trabalhando junto ao governo federal e à iniciativa privada para impulsionar o desenvolvimento e deixar a cidade pronta para receber os milhares de visitantes previstos. O Além da Energia vem acompanhando os avanços nos preparativos da cidade. Estima-se que o conjunto de obras do Parque da Cidade, espaço que sediará as exposições e reuniões da COP30, receba pelo menos R$ 980 milhões em investimentos. Ao todo, são 38 obras em andamento na cidade, o que representa um investimento de R$ 7,3 bilhões, de acordo com o G1. Entre as obras, estão a modernização do Aeroporto Internacional de Belém, a revitalização de rodovias e o BRT Metropolitano, além do citado Parque da Cidade. As obras estão organizadas em quatro categorias: hospedagem, infraestrutura, mobilidade e saneamento. Na área de infraestrutura, que tem mais projetos e recursos, são 14 obras orçadas em R$ 2,7 bilhões. Hospedagem, mobilidade e saneamento recebem oito obras públicas cada uma. Apesar de impulsionar o desenvolvimento, a escolha da capital paraense como sede da COP carrega implicações ambientais e geopolíticas. No entanto, o desafio logístico é considerável. Acelerando os projetos de infraestrutura, o governo também se preocupa com a rede hoteleira: “A questão dos leitos é crítica. Os preços subiram muito e isso pode afastar não só delegações oficiais, mas também a sociedade civil, empresários e cientistas. A COP precisa ser acessível”, alertou Corrêa do Lago.\n\nA Amazônia como sede da COP no Brasil\n\nA cúpula reunirá líderes globais na Amazônia brasileira para focar na questão climática. Essa escolha é estratégica para o Brasil, pois destaca o bioma e oferece ao país uma plataforma para mostrar suas iniciativas de preservação e transição energética. O Brasil é um líder nesse setor e possui recursos que lhe permitem dar o exemplo. Entre os desafios do Brasil na COP30, está a mediação das discussões, o que pode demonstrar que o país pode assumir um papel central no combate às mudanças climáticas. A COP30 será presidida pelo embaixador André Corrêa do Lago, com Ana Toni atuando como CEO da Conferência. As ministras Marina Silva, do Meio Ambiente e Mudança do Clima, e Sonia Guajajara, dos Povos Indígenas, devem participar ativamente das discussões.\n\nUm dos pontos centrais da conferência será a tentativa de reposicionar o Brasil no cenário internacional como uma potência ambiental. Com uma das matrizes energéticas mais limpas do mundo, um histórico no uso de biocombustíveis e investimentos crescentes em tecnologias sustentáveis, o país pretende apresentar um novo modelo de crescimento verde. “Essa agenda favorece o Brasil. Podemos crescer mais, gerar empregos e atender à nova demanda global por produtos sustentáveis”, reforçou o embaixador. A Amazônia corresponde a um terço das florestas tropicais do mundo e desempenha um papel determinante na absorção global de carbono, ajudando a reduzir (naturalmente) os níveis de gases de efeito estufa na atmosfera. Segundo a CNN Brasil, o país tem chances de ser o grande protagonista da edição, mas ainda precisa mostrar ao mundo que isso não se deve apenas ao fato de abrigar parte da maior floresta do planeta, o que, por si só, já justificaria o título. A expectativa da COP30 é que o Brasil demonstre aos líderes globais como está combatendo as mudanças climáticas, uma vez que é referência mundial na utilização de energia limpa, com mais de 90% da eletricidade proveniente de fontes renováveis. Pedro Côrtes, professor do Instituto de Energia e Ambiente da Universidade de São Paulo (USP), declarou em entrevista à CNN Brasil que receber a COP pode representar uma “excelente oportunidade” para o Brasil mostrar suas iniciativas na área de geração de energia.	\N	No caderno, o título está em caixa alta e centralizado; a linha de autoria (Por: Redação em 24/07/2025 ‖ Atualizado: 24/07/2025, às 11:02) alinhada à direita; os intertítulos 'Mas afinal, o que é a COP?', 'Como o Brasil está se preparando para receber a COP30?' e 'A Amazônia como sede da COP no Brasil' estão em negrito e alinhados à esquerda; os parágrafos têm indentação; a citação direta do embaixador Corrêa do Lago está entre aspas; o crédito com URL está no rodapé da página 3 (não faz parte do texto-base, mas é preservado em source_note).	Disponível em: https://www.alemdaenergia.engie.com.br/cop30-no-brasil-por-que-e-tao-importante-para-o-futuro-do-clima/. Acesso em: 05 set 2025 (texto adaptado para uso nesta avaliação).	2	3	e01eed1fc02c68e65b09adce5fbabbde8c351ddcf566b81d6ac54882c8aff86b	2026-10-03 21:36:12.603758+00	2026-10-03 21:36:12.603758+00
27	6	TEXTO-2	Texto 2	CHARGE	Belém e os preparativos para a COP30	Charge de Dantagonico	\N	\N	\N	Charge colorida em quadro único: assinatura 'DANTAGONICO' no canto superior esquerdo; título 'Belém e os preparativos para a COP30' centralizado; ao centro, uma área de mata devastada com troncos cortados e uma estrada de acesso sendo aberta; no lado direito, um painel com a inscrição 'AQUI VAI SER A ESTRADA DE ACESSO À CONFERÊNCIA QUE VAI DISCUTIR O FUTURO DO MEIO AMBIENTE'; no canto inferior, um balão de fala com o texto 'POR QUE TEMOS QUE DESMATAR TODA ESSA ÁREA?'. A composição visual contrasta a devastação ambiental com a proposta de discussão sobre o futuro do meio ambiente na COP30. Nenhuma legenda adicional no caderno além do link de fonte e do título.	Charge colorida em quadro único: assinatura estilizada 'DANTAGONICO' no canto superior esquerdo; título 'Belém e os preparativos para a COP30' centralizado; a imagem mostra uma área de mata devastada com troncos cortados; no centro, uma estrada de acesso sendo aberta; à direita, um painel indicando 'AQUI VAI SER A ESTRADA DE ACESSO À CONFERÊNCIA QUE VAI DISCUTIR O FUTURO DO MEIO AMBIENTE'; no canto inferior, um balão de fala: 'POR QUE TEMOS QUE DESMATAR TODA ESSA ÁREA?'. Nenhum texto extraível além dos elementos descritos.	Disponível em: https://www.google.com/search?client=firefox-b-d&sca_esv=60db1cbf7d4ebbd5&sxsrf=AE3TifOjsRqb2G2Uc3zX7PtuzbC8PWQ2JQ:1757. Acesso em: 05 set. 2025.	4	4	d4bfb6cc0f66c0c5891fcee8da0a577537c005665cdec9615fcedbf2fc5a5618	2026-10-03 21:36:12.672332+00	2026-10-03 21:36:12.672332+00
28	6	TRECHO-Q08-10	Trecho — questões 8 a 10	TRECHO	\N	\N	\N	O trecho a seguir deve ser utilizado para responder às questões de 8 a 10.	A conferência é dividida em Zona Azul e Zona Verde. A Zona Azul, que(1) é gerenciada diretamente pela ONU, é onde(2) acontecem as negociações políticas e os encontros diplomáticos(3). Já a Zona Verde sedia painéis para o público geral, apresentação de ONG e outras atividades, inclusive as culturais.	\N	No caderno, o trecho aparece emoldurado com borda simples; os marcadores (1)–(3) estão em negrito; os termos 'que' (1), 'onde' (2) e 'negociações políticas e os encontros diplomáticos' (3) estão sublinhados. Corresponde ao trecho do Texto 1 sobre a divisão da conferência.	Trecho extraído do Texto 1 (artigo COP30), caderno oficial 2026, p. 6.	6	6	d826cdfca701e3b84581f9b9851db221997cfa30aa473930c1f4d0e65b84ea0f	2026-10-03 21:36:12.711955+00	2026-10-03 21:36:12.711955+00
29	6	TRECHO-Q11-14	Trecho — questões 11 a 14	TRECHO	\N	\N	\N	Considere o trecho a seguir para resolver às questões de 11 a 14.	Apesar de(1) impulsionar(2) o desenvolvimento, a escolha da capital paraense como sede da COP carrega implicações ambientais e geopolíticas. No entanto(3), o desafio logístico é considerável. Acelerando os projetos de infraestrutura, o governo também se preocupa com a rede hoteleira: “(4)A questão dos leitos é crítica. Os preços subiram muito e isso pode afastar não só delegações oficiais, mas também(5) a sociedade civil, empresários e cientistas. A COP precisa ser acessível”(4), alertou Corrêa do Lago.	\N	No caderno, o trecho aparece emoldurado; os marcadores (1)–(5) estão em negrito; os termos 'Apesar de' (1), 'impulsionar' (2), 'No entanto' (3) e 'mas também' (5) estão sublinhados; a citação direta entre aspas (4) está destacada pelo uso das aspas; o ponto final após 'acessível' está dentro das aspas e seguido de '(4)'. Corresponde ao trecho do Texto 1 sobre os desafios logísticos.	Trecho extraído do Texto 1 (artigo COP30), caderno oficial 2026, p. 6.	6	6	d0ae349dc509efe351372dd5b5305725ba8a579d7da2e4cf3188cb31296ebcdc	2026-10-03 21:36:12.747984+00	2026-10-03 21:36:12.747984+00
30	6	TRECHO-Q15-17	Trecho — questões 15 a 17	TRECHO	\N	\N	\N	O trecho a seguir deve ser utilizado para responder às questões 15 a 17.	Com uma das matrizes energéticas(1) mais limpas do mundo,(2) um histórico(3) no uso de biocombustíveis(4) e investimentos crescentes em tecnologias sustentáveis(5), o país(6) pretende apresentar um novo modelo de crescimento verde(7).	\N	No caderno, o trecho aparece emoldurado; os marcadores (1)–(7) estão em negrito; os termos 'matrizes energéticas' (1), 'mundo' (2), 'histórico' (3), 'biocombustíveis' (4), 'tecnologias sustentáveis' (5), 'o país' (6) e 'crescimento verde' (7) estão sublinhados; a vírgula após 'mundo' (2) está destacada pelo marcador. Corresponde ao trecho do Texto 1 sobre a matriz energética brasileira.	Trecho extraído do Texto 1 (artigo COP30), caderno oficial 2026, p. 7.	7	7	38f96b5aba49802f97d355f32a421c0a8b6b62392d0f989c4119051c245910c0	2026-10-03 21:36:12.786414+00	2026-10-03 21:36:12.786414+00
31	6	TRECHO-Q23-24	Trecho — questões 23 a 24	TRECHO	\N	\N	\N	Utilize as informações do Texto 1 a seguir para responder às questões 23 e 24.	O evento da COP30, em Belém, estima receber cerca de 40 mil visitantes, dos quais, pelo menos, 7 mil serão integrantes da ONU e de delegações de países-membros.	\N	No caderno, o bloco aparece como texto destacado entre as questões 22 e 23 (matemática); não está emoldurado como os trechos de língua portuguesa, mas é apresentado como bloco de referência para cálculo. Nenhum marcador numérico no texto; o conteúdo é uma citação direta do Texto 1 (parágrafo sobre visitantes).	Trecho extraído do Texto 1 (artigo COP30), caderno oficial 2026, p. 8.	8	8	e1e72d39f07491ceda211f3da69955ef6e2e411a33ba74d0b61d5af283e8c8a5	2026-10-03 21:36:12.82633+00	2026-10-03 21:36:12.82633+00
\.


--
-- Data for Name: password_reset_tokens; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.password_reset_tokens (id, user_id, token_hash, expires_at, used_at, created_ip, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: progress_snapshots; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.progress_snapshots (id, user_id, taken_at, metrics_json, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: question_attempts; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.question_attempts (id, user_id, question_id, study_session_id, simulation_attempt_id, selected_option, is_correct, was_annulled, time_spent_seconds, mode, answered_at, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: question_classifications; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.question_classifications (id, question_id, taxonomy_version, topic_id, subtopic_id, skill, reasoning_type, confidence, evidence, origin, status, reviewed_by, reviewed_at, observation, created_at, updated_at) FROM stdin;
1	1	v1.1	1	6	IDENTIFICAR_INTENCAO_COMUNICATIVA	INTERPRETATIVO_INFERENCIAL	ALTA	A intenção comunicativa dominante no Texto 1 é + opções informar/narrar/criticar/descrever	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
2	2	v1.1	1	5	LOCALIZAR_INFORMACAO_EXPLICITA	INTERPRETATIVO_LOCALIZACAO	ALTA	as famílias têm dificuldade em identificar o cyberbullying devido + opções com causas	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
3	3	v1.1	1	5	LOCALIZAR_INFORMACAO_EXPLICITA	INTERPRETATIVO_LOCALIZACAO	ALTA	De acordo com o Texto 1 + os ataques sofridos na Internet podem se tornar muito ofensivos	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
4	4	v1.1	2	14	ANALISAR_CLASSE_MORFOLOGICA	ANALITICO_GRAMATICAL	ALTA	O vocábulo Quando apresenta ideia de + opções tempo/espaço/movimento/atualidade	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
5	5	v1.1	2	10	ANALISAR_ESTRUTURA_PERIODO	ANALITICO_GRAMATICAL	ALTA	O trecho constitui-se de + opções três períodos e seis orações / quatro períodos e sete orações	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
6	6	v1.1	2	9	ANALISAR_FUNCAO_SINTATICA	ANALITICO_GRAMATICAL	ALTA	os termos apresentam a mesma função sintática + opções Ela e Aquilo / popular e fácil	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
7	7	v1.1	2	13	APLICAR_PONTUACAO	ANALITICO_GRAMATICAL	ALTA	As vírgulas que separam de 13 anos e de 46 anos + opção apostos	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
8	8	v1.1	1	5	LOCALIZAR_INFORMACAO_EXPLICITA	INTERPRETATIVO_LOCALIZACAO	ALTA	De acordo com o Texto 1 + as agressões feitas na Internet podem causar maior impacto	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
9	9	v1.1	1	5	LOCALIZAR_INFORMACAO_EXPLICITA	INTERPRETATIVO_LOCALIZACAO	ALTA	De acordo com o Texto 1 + a família não sabe ainda como proteger os filhos	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
10	10	v1.1	1	4	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	MEDIA	A leitura do Texto 1 permite inferir que + o domínio precoce das tecnologias expõe as crianças	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
11	11	v1.1	2	12	INFERIR_SENTIDO_VOCABULARIO	ANALITICO_GRAMATICAL	ALTA	O elemento linguístico Segundo [1] indica sentido de + conformidade/consequência/condição/causa	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
12	12	v1.1	2	11	IDENTIFICAR_REFERENTE_COESIVO	INTERPRETATIVO_LOCALIZACAO	ALTA	O elemento linguístico Ela [3] se refere à + vítima/psicóloga/pesquisadora/postagem	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
13	13	v1.1	2	14	ANALISAR_CLASSE_MORFOLOGICA	ANALITICO_GRAMATICAL	ALTA	pesquisadora [4] e Estadual [5] assumem valor de + substantivo e adjetivo	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
14	14	v1.1	2	13	APLICAR_PONTUACAO	ANALITICO_GRAMATICAL	ALTA	o uso das aspas justifica-se por + isolar citação textual direta de autoria alheia	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
15	15	v1.1	2	12	INFERIR_SENTIDO_VOCABULARIO	INTERPRETATIVO_LOCALIZACAO	ALTA	O vocábulo impacto [2] tem sentido de + efeito/causa/duração/possibilidade	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
16	16	v1.1	1	4	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	BAIXA	A leitura do Texto 2 permite inferir que o cyberbullying é + um perigo para as crianças	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
17	17	v1.1	1	4	INFERIR_INFORMACAO	VISUAL_ESPACIAL	BAIXA	a imagem da mão oferece ideia de + opressão/proteção/empatia/desrespeito	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
18	18	v1.1	1	4	INFERIR_INFORMACAO	VISUAL_ESPACIAL	BAIXA	a ideia de que o jovem está indefeso é dada pelo + tamanho da mão em relação ao seu tamanho	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
19	19	v1.1	1	2	IDENTIFICAR_TIPO_GENERO	INTERPRETATIVO_INFERENCIAL	BAIXA	No Texto 2, há o predomínio da + argumentação caracterizada pela defesa de um ponto de vista	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
20	20	v1.1	1	1	IDENTIFICAR_TIPO_GENERO	INTERPRETATIVO_INFERENCIAL	MEDIA	Os Textos 1 e 2, respectivamente, são + notícia/tirinha, artigo/cartaz, editorial/cartum, reportagem/charge	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
21	21	v1.1	9	38	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	texto escrito em 2017, a soma dos anos de nascimento de Laura (13) e sua mãe (46)	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
22	22	v1.1	8	32	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MONOETAPA	ALTA	a idade de Laura correspondeu a um quarto da idade da sua mãe quando Laura tinha	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
23	23	v1.1	9	37	CALCULAR_DIRETO	MODELAGEM_MONOETAPA	ALTA	450 alunos, 5/9 acessavam rede social sem idade mínima e, desses, 3/5 eram meninos	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
24	24	v1.1	9	35	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MONOETAPA	ALTA	papel 88 cm por 104 cm, cartões quadrados de maior dimensão sem sobrar pedaços	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
25	25	v1.1	8	32	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	Internet dois quintos do tempo de estudo; estuda 4h no colégio; ficar 2h na Internet	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
26	26	v1.1	10	42	MODELAR_SITUACAO_PROBLEMA	PROPORCIONAL	BAIXA	a razão entre o percentual de pais que desconhecem e as crianças que relataram ofensas	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
27	27	v1.1	9	36	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	5,6 milhões de meninos e meninas; Em notação científica a quantidade é	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
28	28	v1.1	7	29	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MONOETAPA	ALTA	pátio retangular 50 m por 80 m lotado; em média 4 pessoas por metro quadrado	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
29	29	v1.1	8	32	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	4.800 panfletos; 6 alunos doentes; cada presente distribuiu 40 panfletos a mais	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
30	30	v1.1	7	29	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	original 18 cm por 30 cm ampliada 10 vezes; R$ 120,00 por metro quadrado	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
31	31	v1.1	5	18	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	uma em cada quatro crianças sofreu ofensas; percentualmente corresponde a + 25%	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
32	32	v1.1	10	42	MODELAR_SITUACAO_PROBLEMA	PROPORCIONAL	ALTA	razão entre a contribuição do pai e a da mãe é 4/11; total de 300,00 reais mensais	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
33	33	v1.1	10	39	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MONOETAPA	ALTA	de cada quatro um foi tratado de forma ofensiva, o que corresponde a 5,6 milhões	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
34	34	v1.1	10	40	CONVERTER_UNIDADES	PROPORCIONAL	ALTA	distância em linha reta 6 cm; escala 1:14.200.000; distância real em km	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
35	35	v1.1	6	22	INTERPRETAR_GRAFICO_TABELA	MODELAGEM_MULTIETAPAS	BAIXA	Gráfico 1 mostra o número de alunos por tipo de agressão; diferença percentual maior/menor	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
36	36	v1.1	6	23	INTERPRETAR_GRAFICO_TABELA	MODELAGEM_MONOETAPA	BAIXA	média aritmética do número de alunos que sofreu agressão: estatura, religião e gênero	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
37	37	v1.1	5	20	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	15% em 2014, 20% em 2015, 23% em 2016; mesma quantidade x; que não sofreram agressões	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
38	38	v1.1	6	21	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	dado honesto de 12 faces; face maior que 5 versa sobre Bullying na Internet	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
39	39	v1.1	8	33	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MONOETAPA	ALTA	valor fixo R$150,00 de combustível mais R$390,00 a diária; equação em função de x dias	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
40	40	v1.1	9	35	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	um de 8 em 8 horas e outro de 10 em 10 horas; ingeriu os dois às 14h do domingo	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
41	41	v1.1	1	4	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	ALTA	A leitura do Texto 1 permite inferir que / invasões interferem na preservação da cultura	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
42	42	v1.1	1	1	IDENTIFICAR_TIPO_GENERO	INTERPRETATIVO_INFERENCIAL	MEDIA	O Texto 1 / noticia, incluindo certa crítica / descreve / narra / defende	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
43	43	v1.1	1	2	IDENTIFICAR_TIPO_GENERO	INTERPRETATIVO_INFERENCIAL	ALTA	No último parágrafo do Texto 1, predomina a narração / argumentação	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
44	44	v1.1	2	11	IDENTIFICAR_REFERENTE_COESIVO	ANALITICO_GRAMATICAL	ALTA	o vocábulo LÁ refere-se a / terra indígena Alto Rio Guamá / povo Tembé	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
45	45	v1.1	2	14	ANALISAR_CLASSE_MORFOLOGICA	ANALITICO_GRAMATICAL	MEDIA	O vocábulo PARA estabelece com a oração seguinte uma relação de / finalidade	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
46	46	v1.1	2	12	INFERIR_SENTIDO_VOCABULARIO	INTERPRETATIVO_INFERENCIAL	ALTA	o uso do vocábulo ATÉ / expressa a indignação do autor / até para plantar maconha	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
47	47	v1.1	2	13	APLICAR_PONTUACAO	ANALITICO_GRAMATICAL	ALTA	uso da vírgula se justifica pelo mesmo motivo de Segundo o levantamento, em 2017	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
48	48	v1.1	2	12	INFERIR_SENTIDO_VOCABULARIO	ANALITICO_GRAMATICAL	ALTA	O termo destacado SEGUNDO pode ser substituído por / de acordo com	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
49	49	v1.1	2	8	APLICAR_NORMA_PADRAO	ANALITICO_GRAMATICAL	ALTA	substitui, respeitando a norma-padrão, a forma verbal HOUVE / onde houve mais invasões	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
50	50	v1.1	2	10	ANALISAR_ESTRUTURA_PERIODO	ANALITICO_GRAMATICAL	ALTA	O trecho possui / três períodos e seis orações / dois períodos e cinco orações	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
51	51	v1.1	2	9	ANALISAR_FUNCAO_SINTATICA	ANALITICO_GRAMATICAL	ALTA	O termo destacado DO TAPAJÓS é um / adjunto adnominal / complemento nominal / bacia	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
52	52	v1.1	2	7	APLICAR_NORMA_PADRAO	ANALITICO_GRAMATICAL	MEDIA	RELATÓRIO recebeu acento agudo pelo mesmo motivo de / polícia / indígenas / sínodo	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
53	53	v1.1	1	6	IDENTIFICAR_INTENCAO_COMUNICATIVA	INTERPRETATIVO_INFERENCIAL	ALTA	A intenção comunicativa prioritária do Texto 2 é / informar o percentual por região	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
54	54	v1.1	1	5	LOCALIZAR_INFORMACAO_EXPLICITA	INTERPRETATIVO_LOCALIZACAO	ALTA	As informações presentes no Texto 2 permitem afirmar que / maior percentual de terras	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
55	55	v1.1	2	12	INFERIR_SENTIDO_VOCABULARIO	INTERPRETATIVO_INFERENCIAL	BAIXA	leitura da linguagem verbal e da não verbal do Texto 3, o termo jurídico esbulho é o ato de	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
56	56	v1.1	2	8	APLICAR_NORMA_PADRAO	INTERPRETATIVO_INFERENCIAL	BAIXA	A linguagem utilizada pelo Juiz no Texto 3 / expressa a coloquialidade da linguagem oral	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
57	57	v1.1	1	6	IDENTIFICAR_INTENCAO_COMUNICATIVA	INTERPRETATIVO_INFERENCIAL	MEDIA	O Texto 3 expressa / um posicionamento contrário ao marco temporal	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
58	58	v1.1	1	4	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	BAIXA	A expressão na face do Juiz indica / hostilidade / ousadia / avareza / pânico	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
59	59	v1.1	1	4	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	BAIXA	A leitura do Texto 3 permite inferir que, entre seus personagens, existe / cumplicidade	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
60	60	v1.1	1	5	LOCALIZAR_INFORMACAO_EXPLICITA	INTERPRETATIVO_LOCALIZACAO	MEDIA	É comum aos três textos desta avaliação / o tipo textual / a temática / o gênero textual	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
61	61	v1.1	10	42	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MONOETAPA	ALTA	área a 30 quilômetros da aldeia / foi e retornou / 12 horas / velocidade média em km/h	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
62	62	v1.1	6	23	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MONOETAPA	ALTA	média aritmética das invasões em 2017, 2018 e 2019 não ultrapassassem / 96 e 111 / I ≤ 3M − 207	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
63	63	v1.1	10	42	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	500 garimpos ilegais, com 100 garimpeiros cada / 2.400.000 hectares / densidade demográfica	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
64	64	v1.1	4	17	CALCULAR_DIRETO	MODELAGEM_MONOETAPA	ALTA	R$ 10 milhões em multas / juros simples de 2% ao mês / ao final de 2 anos / montante	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
65	65	v1.1	8	33	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	96 invasões em 2017 e 111 em 2018 / função afim / número de invasões (N) em função do ano (x)	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
66	66	v1.1	9	35	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	passava a cada 3 dias, a cada 4 dias e a cada 6 dias / todos no mesmo dia, novamente, daqui a	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
67	67	v1.1	9	37	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	um quarto ao Ibama, dois quintos do que sobrou à Polícia Federal e o restante à Funai	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
68	68	v1.1	10	42	MODELAR_SITUACAO_PROBLEMA	PROPORCIONAL	ALTA	50 manifestantes / razão entre indígenas e garimpeiros de um para quatro / total de indígenas	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
69	69	v1.1	10	41	MODELAR_SITUACAO_PROBLEMA	PROPORCIONAL	ALTA	300 campos em 30 dias / grupo reduzido a dois terços / mesmo ritmo / tempo em dias	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
70	70	v1.1	10	40	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	MEDIA	comprimento quatro vezes a largura / 256 hectares / escala 1:10.000 / largura e comprimento em cm	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
71	71	v1.1	6	22	INTERPRETAR_GRAFICO_TABELA	INTERPRETATIVO_LOCALIZACAO	BAIXA	Analisando o gráfico do Texto 2, a única afirmação correta é / percentuais por região	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
72	72	v1.1	9	35	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	doar R$ 540,00 / menor mensalidade / maior número de meses até 12 / R$ 30 / 45 / 60 / 100	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
73	73	v1.1	9	36	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	cerca de dois mil índios / representação em notação científica / 2.10³	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
74	74	v1.1	6	21	INTERPRETAR_GRAFICO_TABELA	MODELAGEM_MONOETAPA	ALTA	35 estudantes / escolhendo-se ao acaso / probabilidade de ter opinado por desmatamento	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
75	75	v1.1	7	25	CALCULAR_DIRETO	MODELAGEM_MULTIETAPAS	ALTA	dez troncos em cilindros retos / diâmetro 20 cm e altura 1,0 m / volume em m³ / π=3,14	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
76	76	v1.1	5	19	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	96 em 2017, 111 em 2018 / mesmo percentual de aumento / número aproximado para 2019	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
77	77	v1.1	7	27	RACIOCINIO_ESPACIAL	VISUAL_ESPACIAL	BAIXA	lados do triângulo retângulo ABC / ponto mais alto da árvore / guarda com 1,68 m / Figura 3	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
78	78	v1.1	7	29	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	círculo de diâmetro igual a 600 metros / medida da área em m² / 90000π	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
79	79	v1.1	7	27	RACIOCINIO_ESPACIAL	VISUAL_ESPACIAL	BAIXA	largura DE de um lago / esquema da Figura 4 / ∆ABC ~ ∆EDC / expressão de DE	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
80	80	v1.1	9	38	CALCULAR_DIRETO	CALCULO_DIRETO	MEDIA	total de invasões nos três anos / representação no sistema binário / (101101111)2	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
81	81	v1.1	1	2	IDENTIFICAR_TIPO_GENERO	INTERPRETATIVO_INFERENCIAL	ALTA	Quanto à organização tipológica, o Texto 1 é predominantemente	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
82	82	v1.1	1	1	IDENTIFICAR_TIPO_GENERO	INTERPRETATIVO_INFERENCIAL	ALTA	Quanto ao gênero textual, o Texto 1 configura-se como	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
83	83	v1.1	1	5	LOCALIZAR_INFORMACAO_EXPLICITA	INTERPRETATIVO_LOCALIZACAO	ALTA	De acordo com o Texto 1, a Lei das Cotas	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
84	84	v1.1	1	4	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	ALTA	O Texto 1 permite inferir que	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
85	85	v1.1	1	5	LOCALIZAR_INFORMACAO_EXPLICITA	INTERPRETATIVO_LOCALIZACAO	ALTA	De acordo com o Texto 1, [alternativas sobre segregação e extermínio]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
86	86	v1.1	2	10	ANALISAR_ESTRUTURA_PERIODO	ANALITICO_GRAMATICAL	ALTA	a segunda oração do primeiro período cumpre a função sintática de completar o sentido de um nome	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
87	87	v1.1	2	8	APLICAR_NORMA_PADRAO	ANALITICO_GRAMATICAL	ALTA	o primeiro período está corretamente reescrito e mantém seu sentido original [havia/existia, tem/têm]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
88	88	v1.1	2	14	ANALISAR_CLASSE_MORFOLOGICA	ANALITICO_GRAMATICAL	ALTA	O vocábulo QUE (1) é [pronome/advérbio/conjunção/partícula expletiva]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
89	89	v1.1	2	11	IDENTIFICAR_REFERENTE_COESIVO	ANALITICO_GRAMATICAL	ALTA	O vocábulo QUE (2) refere-se a [ações/poder público/setor privado/artigo da Constituição]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
90	90	v1.1	2	13	APLICAR_PONTUACAO	ANALITICO_GRAMATICAL	ALTA	o uso das aspas (3) tem por fim [citação de 'são iguais perante as leis']	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
91	91	v1.1	2	12	INFERIR_SENTIDO_VOCABULARIO	INTERPRETATIVO_INFERENCIAL	ALTA	a palavra ESCÁRNIO (4) pode ser substituída por [desdém/imperícia/desleixo/arrogância]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
92	92	v1.1	2	7	APLICAR_NORMA_PADRAO	ANALITICO_GRAMATICAL	MEDIA	O vocábulo ESCÁRNIO (4) é acentuado pelo mesmo motivo de [público/étnica/abomináveis/completará]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
93	93	v1.1	2	13	APLICAR_PONTUACAO	ANALITICO_GRAMATICAL	ALTA	Em relação ao uso da vírgula assinalada com (1), é correto afirmar que ela [separa adjunto adverbial...]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
94	94	v1.1	2	9	ANALISAR_FUNCAO_SINTATICA	ANALITICO_GRAMATICAL	ALTA	O termo em destaque POR GRUPOS CONSERVADORES E NEONAZISTAS (2) exerce função de [agente da passiva...]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
95	95	v1.1	2	14	ANALISAR_CLASSE_MORFOLOGICA	ANALITICO_GRAMATICAL	ALTA	NAS UNIVERSIDADES (3) e EM ALGUMAS EMPRESAS (4) têm, respectivamente, valor de [locução adverbial...]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
96	96	v1.1	1	3	IDENTIFICAR_INTENCAO_COMUNICATIVA	INTERPRETATIVO_INFERENCIAL	MEDIA	O primeiro período do último parágrafo do Texto 1 introduz uma ideia de [conclusão/oposição...]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
97	97	v1.1	1	6	IDENTIFICAR_INTENCAO_COMUNICATIVA	INTERPRETATIVO_INFERENCIAL	ALTA	Sobre o Texto 2, é correto afirmar que [crítica à ideia racista / narrar episódio / informar / descrever]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
98	98	v1.1	2	9	ANALISAR_FUNCAO_SINTATICA	ANALITICO_GRAMATICAL	ALTA	são expressões que têm o mesmo valor e mesma função sintática [você/sua, futebol/cota...]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
99	99	v1.1	2	12	INFERIR_SENTIDO_VOCABULARIO	INTERPRETATIVO_INFERENCIAL	ALTA	o vocábulo COTA foi utilizado de forma [denotativa/conotativa/metonímica/pleonástica]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
100	100	v1.1	1	6	IDENTIFICAR_INTENCAO_COMUNICATIVA	INTERPRETATIVO_INFERENCIAL	MEDIA	Os Textos 1 e 2 apresentam em comum [intenção comunicativa/linguagem verbal/registro informal]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
101	101	v1.1	5	20	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	3 mil alunos e mais de 38% deles são negros, logo [menos de 1140 / menos de 1860...]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
102	102	v1.1	6	21	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	a probabilidade de que ele não seja negro é aproximadamente de [31/50, 62/50, 38/50, 19/50]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
103	103	v1.1	7	28	RACIOCINIO_ESPACIAL	VISUAL_ESPACIAL	BAIXA	O perímetro, em cm, de cada um desses pentágonos corresponde a [20/25/15/30]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
104	104	v1.1	7	29	RACIOCINIO_ESPACIAL	VISUAL_ESPACIAL	BAIXA	A área, em cm2, correspondente a cada um desses hexágonos (Figura 1) é de [75√3/2...]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
105	105	v1.1	5	18	INTERPRETAR_GRAFICO_TABELA	PROPORCIONAL	BAIXA	A fração da quantidade de vagas reservadas para [PPI] em relação ao total [...] equivale a	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
106	106	v1.1	8	30	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	MEDIA	eram 15 mil estudantes; em 2020, passaram a ser 36 mil. Se o aumento se mantiver nessa proporção	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
107	107	v1.1	8	33	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MONOETAPA	ALTA	valor inicial de R$ 2500,00, acrescido de R$ 830,00 mensais [...] é dada por [y = 830x + 2500...]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
108	108	v1.1	6	23	INTERPRETAR_GRAFICO_TABELA	CALCULO_DIRETO	BAIXA	a média aritmética do percentual de respondentes que são a favor [...] corresponde a	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
109	109	v1.1	6	22	INTERPRETAR_GRAFICO_TABELA	INTERPRETATIVO_LOCALIZACAO	BAIXA	Os percentuais referentes aos respondentes que não souberam opinar, são, respectivamente	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
110	110	v1.1	10	41	MODELAR_SITUACAO_PROBLEMA	PROPORCIONAL	ALTA	dentre cada 1500 habitantes [...] 645 sejam pretos ou pardos. Considerando 23 mil habitantes	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
111	111	v1.1	9	37	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	dois quintos das 1000 vagas [...] dois quintos do restante [...] vagas que faltaram para completar	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
112	112	v1.1	9	36	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	reserva de 50% das vagas [...] 1,1 milhão [...] em notação científica, era [5,5⋅10⁵...]	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
113	113	v1.1	9	38	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	reavaliada a cada quinquênio por um período de 300 anos, o total de reavaliações [...] será	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
114	114	v1.1	8	32	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MONOETAPA	ALTA	500 pessoas [...] o número de pessoas que apoiavam era o triplo do de pessoas que não apoiavam	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
115	115	v1.1	3	16	CONVERTER_UNIDADES	MODELAGEM_MONOETAPA	ALTA	durou 48 horas ininterruptas e, a cada 20 minutos, uma apresentação cultural diferente subia ao palco	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
116	116	v1.1	8	32	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MONOETAPA	MEDIA	reservariam 30% das suas X vagas [...] 40% das suas Y [...] A expressão que representa o total T	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
117	117	v1.1	10	39	MODELAR_SITUACAO_PROBLEMA	PROPORCIONAL	MEDIA	semelhante a um triângulo retângulo pitagórico de lados 3, 4 e 5 [...] hipotenusa medindo 300 m	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
118	118	v1.1	4	17	CALCULAR_DIRETO	MODELAGEM_MONOETAPA	ALTA	R$ 100 000,00 a uma taxa de juros simples de 1% ao mês [...] ao final de 60 meses	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
119	119	v1.1	7	27	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	qual a diferença entre a soma dos ângulos internos do hexágono e do pentágono [...] regulares	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
120	120	v1.1	9	35	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	MEDIA	a cada dois anos [...] a cada três anos [...] a cada quatro anos [...] em 2020, ele estudou as três	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
121	121	v1.1	1	6	IDENTIFICAR_INTENCAO_COMUNICATIVA	INTERPRETATIVO_INFERENCIAL	ALTA	O propósito comunicativo dominante no Texto 1 é; informar o leitor sobre um evento de temática ambiental.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
122	122	v1.1	1	3	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	ALTA	o primeiro parágrafo cumpre o papel de; situar o leitor no contexto, apresentando o objeto do texto.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
123	123	v1.1	2	13	APLICAR_PONTUACAO	ANALITICO_GRAMATICAL	ALTA	o uso dos parênteses se justifica porque assume a função de; introduzir explicações acessórias.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
124	124	v1.1	1	5	LOCALIZAR_INFORMACAO_EXPLICITA	INTERPRETATIVO_LOCALIZACAO	ALTA	De acordo com o Texto 1, é correto afirmar que; desmatamento de um quarto da floresta ocasionaria colapso.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
125	125	v1.1	1	4	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	ALTA	A leitura do Texto 1 permite inferir que; o governo demonstra interesse em políticas públicas ambientais.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
126	126	v1.1	1	4	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	ALTA	há uma informação implícita: antes, a Amazônia emitia menos gás carbônico que atualmente.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
127	127	v1.1	2	10	ANALISAR_ESTRUTURA_PERIODO	ANALITICO_GRAMATICAL	ALTA	Na sua organização sintática, o trecho apresenta; período composto por subordinação e seis orações.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
128	128	v1.1	2	14	ANALISAR_CLASSE_MORFOLOGICA	ANALITICO_GRAMATICAL	ALTA	O elemento linguístico que (1) é um(a); pronome relativo.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
129	129	v1.1	2	14	ANALISAR_CLASSE_MORFOLOGICA	ANALITICO_GRAMATICAL	MEDIA	Sobre o elemento linguístico extra (2); morfologicamente, é um adjetivo.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
130	130	v1.1	2	13	APLICAR_PONTUACAO	ANALITICO_GRAMATICAL	ALTA	o uso das aspas (1 e 3) tem por fim; destacar uma citação direta.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
131	131	v1.1	2	11	IDENTIFICAR_REFERENTE_COESIVO	ANALITICO_GRAMATICAL	ALTA	O elemento linguístico QUE (2) refere-se à palavra; aquilo.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
132	132	v1.1	2	11	IDENTIFICAR_REFERENTE_COESIVO	ANALITICO_GRAMATICAL	MEDIA	ALÉM DISSO (4); acrescentar uma informação ao que vem sendo enunciado.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
133	133	v1.1	2	11	IDENTIFICAR_REFERENTE_COESIVO	ANALITICO_GRAMATICAL	ALTA	O elemento linguístico EVENTO (5) refere-se; à Cúpula.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
134	134	v1.1	1	6	IDENTIFICAR_INTENCAO_COMUNICATIVA	INTERPRETATIVO_INFERENCIAL	MEDIA	O Texto 2; critica a desinformação do público em relação à Cúpula da Amazônia.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
135	135	v1.1	2	14	ANALISAR_CLASSE_MORFOLOGICA	ANALITICO_GRAMATICAL	ALTA	A expressão DA AMAZÔNIA (1) classifica-se como; locução adjetiva.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
136	136	v1.1	2	8	APLICAR_NORMA_PADRAO	INTERPRETATIVO_INFERENCIAL	MEDIA	Eu tava falando da Cúpula da Amazônia!; o registro informal.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
137	137	v1.1	2	7	APLICAR_NORMA_PADRAO	ANALITICO_GRAMATICAL	BAIXA	No trecho; proparoxítono, monossílabos átonos, monossílabos tônicos, acentuados.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
138	138	v1.1	1	5	INTERPRETAR_GRAFICO_TABELA	INTERPRETATIVO_LOCALIZACAO	MEDIA	Segundo o Texto 3, o desmatamento foi; sempre crescente de 2017 a 2022.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
139	139	v1.1	1	1	IDENTIFICAR_TIPO_GENERO	INTERPRETATIVO_INFERENCIAL	MEDIA	O elemento que relaciona os textos 1, 2 e 3 entre si é o; recorte temático.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
140	140	v1.1	1	1	IDENTIFICAR_TIPO_GENERO	INTERPRETATIVO_INFERENCIAL	ALTA	Texto 3 configura-se, quanto ao gênero textual, como um gráfico.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
141	141	v1.1	6	21	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	probabilidade de país com inicial B; 2 em 8 nomes, igual a 1/4.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
142	142	v1.1	9	35	CALCULAR_DIRETO	MODELAGEM_MONOETAPA	ALTA	mais de 60; múltiplo de 6 e divisível por primo maior que 10; pode ser 78.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
143	143	v1.1	9	38	CALCULAR_DIRETO	CALCULO_DIRETO	MEDIA	10.000 pessoas; representação em forma de potência; 10 elevado a 4.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
144	144	v1.1	10	42	CALCULAR_DIRETO	PROPORCIONAL	ALTA	a razão entre brasileiros e estrangeiros; 2 em 7, igual a 2/5.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
145	145	v1.1	7	29	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	área da tela; 95 cm de largura e 53 cm de altura; 95 x 53 = 5035.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
146	146	v1.1	7	26	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	medida da diagonal da tela, aproximadamente; lados 95 cm e 53 cm.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
147	147	v1.1	7	24	CONVERTER_UNIDADES	ESTIMATIVA	ALTA	tamanho em polegadas, mais próximo de; 1 polegada = 2,5 cm; 43.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
148	148	v1.1	10	41	MODELAR_SITUACAO_PROBLEMA	PROPORCIONAL	ALTA	36 homens em 25 dias; mesmo trabalho em 20 dias; 45 homens.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
149	149	v1.1	8	31	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	comissão de 19 pessoas; dobro dos homens excede em 5 o de mulheres.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
150	150	v1.1	9	37	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	120 pessoas; um quarto brasileiros, um quinto do Equador; resto igual entre 6 países.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
151	151	v1.1	6	22	INTERPRETAR_GRAFICO_TABELA	INTERPRETATIVO_LOCALIZACAO	MEDIA	Com base no Texto 3; acumulada 2019-2022 chegou a 35.193 km².	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
152	152	v1.1	6	23	CALCULAR_DIRETO	CALCULO_DIRETO	MEDIA	desmatamento médio 2013-2022, aproximadamente; opções próximas (5376 a 5787).	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
153	153	v1.1	5	19	CALCULAR_DIRETO	PROPORCIONAL	MEDIA	aumento percentual em 2022 em relação a 2018; opções próximas (96% a 108%).	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
154	154	v1.1	8	30	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MONOETAPA	ALTA	aumente em 20 por ano, de 2025 até 2029; chegou a 300; antes era 200.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
155	155	v1.1	10	41	MODELAR_SITUACAO_PROBLEMA	PROPORCIONAL	ALTA	6 trabalhadores, 8h/dia, 5 dias; em 2 dias a 10h/dia; contratar mais 6.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
156	156	v1.1	10	41	MODELAR_SITUACAO_PROBLEMA	PROPORCIONAL	MEDIA	300 pessoas; a cada 2 min, 4 saem e 2 entram; esvaziar em 5 horas.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
157	157	v1.1	8	33	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MONOETAPA	ALTA	fixo de R$ 10.000 + R$ 2.000 por transmissão; C(x) = 10.000 + 2.000x.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
158	158	v1.1	4	17	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	juros simples de 1% ao mês; R$ 100.000.000; após 8 meses: 108 milhões.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
159	159	v1.1	3	16	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	MEDIA	11h36min de voo a 625 km/h; distância de 7250 km.	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
160	160	v1.1	10	40	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	miniatura 2cm por 8cm; escala 1:5000; área real de 40.000 m².	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
161	161	v1.1	1	5	LOCALIZAR_INFORMACAO_EXPLICITA	INTERPRETATIVO_LOCALIZACAO	MEDIA	De acordo com o Texto 1 + 'a maioria da população consultada é favorável à proibição total'	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
162	162	v1.1	1	4	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	MEDIA	a proibição ... deveu-se 'ao fato de que seu uso inadequado pode ter um impacto negativo na saúde emocional'	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
163	163	v1.1	1	6	IDENTIFICAR_INTENCAO_COMUNICATIVA	INTERPRETATIVO_INFERENCIAL	ALTA	A intenção comunicativa predominante no Texto 1 é 'informar sobre a proibição do uso de celulares'	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
164	164	v1.1	1	2	IDENTIFICAR_TIPO_GENERO	INTERPRETATIVO_INFERENCIAL	ALTA	Quanto ao tipo textual, no último parágrafo do Texto 1, predomina a 'argumentação, pois defende o ponto de vista'	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
165	165	v1.1	2	12	INFERIR_SENTIDO_VOCABULARIO	INTERPRETATIVO_INFERENCIAL	MEDIA	No título do Texto 1, uso do elemento linguístico ATÉ 'deixa pressuposta a ideia de que o uso era liberado'	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
166	166	v1.1	2	10	ANALISAR_ESTRUTURA_PERIODO	ANALITICO_GRAMATICAL	ALTA	Quanto à sua organização, o trecho é formado por 'um período e três orações'	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
167	167	v1.1	2	13	APLICAR_PONTUACAO	ANALITICO_GRAMATICAL	ALTA	Sobre o uso dos dois-pontos no trecho: 'em (1), ele antecede uma citação'	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
168	168	v1.1	2	9	ANALISAR_FUNCAO_SINTATICA	ANALITICO_GRAMATICAL	ALTA	A UTILIZAÇÃO DE CELULARES E OUTROS DISPOSITIVOS ELETRÔNICOS assume a função de 'sujeito'	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
169	169	v1.1	2	12	INFERIR_SENTIDO_VOCABULARIO	INTERPRETATIVO_LOCALIZACAO	ALTA	EXPLANAÇÃO pode ser substituído, sem prejuízo do sentido, por 'explicação'	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
170	170	v1.1	2	14	ANALISAR_CLASSE_MORFOLOGICA	ANALITICO_GRAMATICAL	MEDIA	o elemento linguístico DEVERÃO exprime sentido de 'obrigatoriedade'	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
171	171	v1.1	2	11	IDENTIFICAR_REFERENTE_COESIVO	INTERPRETATIVO_LOCALIZACAO	ALTA	A expressão A GENTE tem como referente o 'secretário' (autor da fala citada)	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
172	172	v1.1	1	5	LOCALIZAR_INFORMACAO_EXPLICITA	INTERPRETATIVO_LOCALIZACAO	ALTA	'AS CRIANÇAS NÃO PODEM CONTINUAR FICANDO ISOLADAS NAS SUAS PRÓPRIAS TELAS' = o uso isola as crianças	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
173	173	v1.1	1	4	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	ALTA	informação implícita: 'na contemporaneidade, as crianças estão se isolando por causa do celular'	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
174	174	v1.1	1	3	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	MEDIA	Sobre a organização do trecho: 'a síntese das ideias do parágrafo está no último período'	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
175	175	v1.1	1	4	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	BAIXA	No Texto 2, os sentidos se estabelecem a partir de 'pontos de vista conflitantes/convergentes'	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
176	176	v1.1	1	4	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	BAIXA	A leitura dos elementos verbais e não verbais: o Texto 2 'critica o uso de equipamentos eletrônicos'	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
177	177	v1.1	2	14	ANALISAR_CLASSE_MORFOLOGICA	ANALITICO_GRAMATICAL	ALTA	SEGURO-DESEMPREGO é formado pelo processo de 'composição por justaposição'	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
178	178	v1.1	1	4	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	MEDIA	Sobre o Texto 2: 'sua temática é a mesma do Texto 1' (comparação temática/tipo/registro)	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
179	179	v1.1	1	1	IDENTIFICAR_TIPO_GENERO	INTERPRETATIVO_INFERENCIAL	ALTA	Textos 1 e 2 configuram-se como 'reportagem e charge'	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
180	180	v1.1	1	4	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	MEDIA	Os Textos 1 e 2 apresentam em comum o 'recorte temático'	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
181	181	v1.1	10	39	MODELAR_SITUACAO_PROBLEMA	PROPORCIONAL	ALTA	'de cada 10 pessoas 8 são a favor' em 2450 pessoas: contra = 2/10 de 2450 = 490	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
182	182	v1.1	10	42	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	'A razão entre o número de acertos (6) e o total de chutes (15)' = 6/15 = 2/5	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
183	183	v1.1	6	21	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	150 alunos (60 apoiam, 45 indiferentes): 'probabilidade de que ele seja um aluno que se opõe' = 45/150	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
184	184	v1.1	5	19	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	multa inicial de R$ 50,00 'acrescida de 10%' a cada reincidência; total em três ocasiões = 165,50	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
185	185	v1.1	10	41	MODELAR_SITUACAO_PROBLEMA	PROPORCIONAL	ALTA	'30 deles em 150' usando celulares: estimativa para 2000 alunos 'considerando a mesma proporção' = 400	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
186	186	v1.1	6	23	INTERPRETAR_GRAFICO_TABELA	CALCULO_DIRETO	ALTA	'gráfico de barras' de horas semanais de sete grupos: 'a média aritmética ... é'	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
187	187	v1.1	7	29	CALCULAR_DIRETO	MODELAGEM_MONOETAPA	ALTA	sala retangular, 'uma câmera para até 10 metros quadrados': nº mínimo = área/10 arredondado p/ cima	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
188	188	v1.1	10	41	MODELAR_SITUACAO_PROBLEMA	PROPORCIONAL	MEDIA	'14 horas e 30 minutos' em 7 dias: 'utilizando a mesma proporção' projetar para 90 dias	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
189	189	v1.1	9	37	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	'1/4 ... 1/5 ... 1/8' do tempo usados; restam '4 horas e 15 minutos': tempo total = 10h	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
190	190	v1.1	8	32	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	MEDIA	tela retangular de área 75 cm² com 'lado maior o triplo do menor': soma dos lados = 40 cm	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
191	191	v1.1	5	20	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	300 alunos do ensino superior; '17% favoráveis': não favoráveis = 83% de 300 = 249	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
192	192	v1.1	5	20	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	30 tablets de R$ 200,00 com 'desconto de 55%': total = 30 x 200 x 0,45 = 2700	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
193	193	v1.1	6	21	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	'13 deles trouxeram seus celulares' em turma de 28: probabilidade = 13/28	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
194	194	v1.1	10	39	MODELAR_SITUACAO_PROBLEMA	PROPORCIONAL	ALTA	proporção de 9 para 4 em 234 alunos: os que não utilizam = 4/13 de 234 = 72	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
195	195	v1.1	10	41	MODELAR_SITUACAO_PROBLEMA	PROPORCIONAL	MEDIA	67 (2016) para 83 (2024): 'se o aumento mantiver essa proporção' projetar para 2034 = 103	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
196	196	v1.1	5	18	ESTIMAR_APROXIMAR	ESTIMATIVA	ALTA	'A fração que mais se aproxima' de 83%: 25/30 = 83,3%	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
197	197	v1.1	4	17	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	R$ 1500,00 em 12 meses com 'juros simples de 2% ao mês': total = 1500 + 360 = 1860	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
198	198	v1.1	8	33	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MONOETAPA	ALTA	R$ 300 por postagem + taxa fixa 250 + R$ 360 por transmissão: I(x,y) = 250 + 300x + 360y	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
199	199	v1.1	7	26	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	MEDIA	diagonal de tela 150 mm x 75 mm com '1 polegada = 2,5 cm': Pitágoras + conversão ≈ 6,7 pol	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
200	200	v1.1	7	29	RACIOCINIO_ESPACIAL	VISUAL_ESPACIAL	MEDIA	'todos alinhados na mesma orientação': comparar 6x10=60 vs 13x5=65 na mesa 1,0 x 0,8 m	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
201	201	v1.1	1	5	LOCALIZAR_INFORMACAO_EXPLICITA	INTERPRETATIVO_LOCALIZACAO	ALTA	A leitura do Texto 1 permite afirmar que a Conferência das Partes (COP)	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
202	202	v1.1	1	5	LOCALIZAR_INFORMACAO_EXPLICITA	INTERPRETATIVO_LOCALIZACAO	ALTA	Segundo o Texto 1, a Conferência das Partes (COP) tem como função	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
203	203	v1.1	1	6	IDENTIFICAR_INTENCAO_COMUNICATIVA	INTERPRETATIVO_INFERENCIAL	ALTA	No Texto 1, a intenção comunicativa predominante é	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
204	204	v1.1	1	5	LOCALIZAR_INFORMACAO_EXPLICITA	INTERPRETATIVO_LOCALIZACAO	ALTA	De acordo com o Texto 1, a realização da COP30 no Brasil	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
205	205	v1.1	1	2	IDENTIFICAR_TIPO_GENERO	INTERPRETATIVO_INFERENCIAL	ALTA	O Texto 1 organiza-se, predominantemente, a partir do tipo textual	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
206	206	v1.1	1	4	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	ALTA	A partir da leitura do Texto 1, é correto inferir que	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
207	207	v1.1	1	3	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	ALTA	O primeiro parágrafo do Texto 1 cumpre o papel de apresentar, em linhas gerais, o recorte temático	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
208	208	v1.1	2	10	ANALISAR_ESTRUTURA_PERIODO	ANALITICO_GRAMATICAL	ALTA	Do ponto de vista de sua organização sintática, é correto afirmar que o trecho apresenta 3 períodos	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
209	209	v1.1	2	11	IDENTIFICAR_REFERENTE_COESIVO	ANALITICO_GRAMATICAL	ALTA	As expressões QUE (1) e ONDE (2) referem-se à Zona Azul	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
210	210	v1.1	2	9	ANALISAR_FUNCAO_SINTATICA	ANALITICO_GRAMATICAL	ALTA	AS NEGOCIAÇÕES POLÍTICAS E OS ENCONTROS DIPLOMÁTICOS (3), sintaticamente, assume a função de sujeito	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
211	211	v1.1	2	10	ANALISAR_ESTRUTURA_PERIODO	ANALITICO_GRAMATICAL	MEDIA	Sobre o uso do elemento linguístico APESAR DE (1): introduz uma ideia de concessão	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
212	212	v1.1	2	12	INFERIR_SENTIDO_VOCABULARIO	INTERPRETATIVO_INFERENCIAL	ALTA	A expressão IMPULSIONAR (2) tem sentido de fomentar	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
213	213	v1.1	2	14	ANALISAR_CLASSE_MORFOLOGICA	ANALITICO_GRAMATICAL	ALTA	NO ENTANTO (3) e MAS TAMBÉM (5) assumem, respectivamente, o valor de conjunção e locução conjuntiva	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
214	214	v1.1	2	13	APLICAR_PONTUACAO	ANALITICO_GRAMATICAL	ALTA	O uso das aspas, indicadas por (4), é justificado porque marca uma citação direta	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
215	215	v1.1	2	8	APLICAR_NORMA_PADRAO	ANALITICO_GRAMATICAL	ALTA	Sobre a acentuação de palavras no trecho: 1 e 6, hiato; 3, paroxítona; 6, oxítona	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
216	216	v1.1	2	13	APLICAR_PONTUACAO	ANALITICO_GRAMATICAL	ALTA	O uso da vírgula assinalada com 2 justifica-se por separar termos de mesma função sintática	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
217	217	v1.1	1	4	INFERIR_SENTIDO_VOCABULARIO	INTERPRETATIVO_INFERENCIAL	ALTA	CRESCIMENTO VERDE (7): modelo que concilia o crescimento econômico com a sustentabilidade ambiental	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
218	218	v1.1	1	4	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	MEDIA	O sentido global do Texto 2 é construído pela articulação entre linguagem verbal e não verbal	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
219	219	v1.1	1	1	IDENTIFICAR_TIPO_GENERO	INTERPRETATIVO_INFERENCIAL	ALTA	Quanto ao gênero textual dos Textos 1 e 2: o Texto 2 é uma charge	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
220	220	v1.1	1	4	INFERIR_INFORMACAO	INTERPRETATIVO_INFERENCIAL	MEDIA	Os Textos 1 e 2 apresentam em comum o recorte temático	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
221	221	v1.1	9	34	CALCULAR_DIRETO	CALCULO_DIRETO	BAIXA	Em algarismos romanos, os números 30 e 2025 são representados, respectivamente, por XXX e MMXXV	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
222	222	v1.1	9	38	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	4.900 lugares, adicionou 350, interditou 150; cada auditório comporta 50 lugares	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
223	223	v1.1	6	21	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	probabilidade de que, ao escolher um visitante ao acaso entre 40 mil, ele seja dos 7 mil da ONU	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
224	224	v1.1	10	39	CALCULAR_DIRETO	PROPORCIONAL	ALTA	30 mil visitantes, mantendo-se a mesma proporção de integrantes da ONU e delegações	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
225	225	v1.1	3	15	CONVERTER_UNIDADES	MODELAGEM_MULTIETAPAS	ALTA	2 litros por dia, garrafas de 500 mL, grades com 12 garrafas, 12 dias, 40 mil visitantes	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
226	226	v1.1	10	41	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	MEDIA	BRT a cada 20 minutos com 200 passageiros; tempo para transportar 40 mil visitantes	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
227	227	v1.1	5	20	CALCULAR_DIRETO	MODELAGEM_MULTIETAPAS	ALTA	motorista recebeu 20% e 25% do arrecadado; restante entregue ao coordenador	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
228	228	v1.1	5	20	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	14 de infraestrutura em 38 obras; percentual aproximado da categoria	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
229	229	v1.1	4	17	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	empréstimo de R$ 80.000,00 a juros simples, taxa de 1,5% ao mês, prazo de 10 meses	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
230	230	v1.1	7	26	RACIOCINIO_ESPACIAL	VISUAL_ESPACIAL	BAIXA	triângulo retângulo ABC, ângulo B de 30º, BC de 5 m, painel ADEF, CD de 1,6 m	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
231	231	v1.1	7	29	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	praça retangular 60 por 40 com espaço circular central de 15 metros de raio	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
232	232	v1.1	6	23	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	metas 42%, 37%, 48%, 35% e 38%; com o 6º país a média passou a ser 41,5%	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
233	233	v1.1	8	32	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	1/3 de X no aeroporto, 1 bilhão em saneamento, 4/5 do restante em mobilidade, sobraram 0,6	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
234	234	v1.1	9	38	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	40 mil pessoas, 47 leitos por hotel; número mínimo de hotéis necessários	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
235	235	v1.1	8	33	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MONOETAPA	ALTA	oferta inicial de 20 mil leitos mais 500 novos leitos por mês x de obras	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
236	236	v1.1	10	41	MODELAR_SITUACAO_PROBLEMA	PROPORCIONAL	ALTA	120 operários em 15 meses; reduzida para 80 operários, mesmo ritmo de trabalho	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
237	237	v1.1	9	35	CALCULAR_DIRETO	CALCULO_DIRETO	ALTA	lotes de 36 e 48 unidades; mesma quantidade por pacote, sem sobra; maior quantidade de pacotes	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
238	238	v1.1	8	32	CALCULAR_DIRETO	CALCULO_DIRETO	MEDIA	largura reduzida em 3 metros e comprimento ampliado em 5; nova área em função de x e y	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
239	239	v1.1	7	27	RACIOCINIO_ESPACIAL	VISUAL_ESPACIAL	MEDIA	triângulo isósceles de lados l, ângulo de 120°, altura relativa ao vértice desse ângulo	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
240	240	v1.1	8	31	MODELAR_SITUACAO_PROBLEMA	MODELAGEM_MULTIETAPAS	ALTA	garrafas R$ 40 e copos R$ 15; 1000 itens e arrecadação de R$ 25.000,00	CLASSIFICACAO_DERIVADA_FONTE	PENDING	\N	\N	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
\.


--
-- Data for Name: question_figures; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.question_figures (id, question_id, "position", file_path, alt_text, page, publication_status, created_at, updated_at) FROM stdin;
1	16	1	2020/Q16.webp	Texto-base compartilhado (Texto 2 — charge) da prova 2020.	6	PUBLICAVEL	2026-10-03 22:48:47.601333+00	2026-10-03 22:48:47.601333+00
2	17	1	2020/Q17.webp	Texto-base compartilhado (Texto 2 — charge) da prova 2020.	6	PUBLICAVEL	2026-10-03 22:48:47.632597+00	2026-10-03 22:48:47.632597+00
3	18	1	2020/Q18.webp	Texto-base compartilhado (Texto 2 — charge) da prova 2020.	6	PUBLICAVEL	2026-10-03 22:48:47.666758+00	2026-10-03 22:48:47.666758+00
4	19	1	2020/Q19.webp	Texto-base compartilhado (Texto 2 — charge) da prova 2020.	6	PUBLICAVEL	2026-10-03 22:48:47.700169+00	2026-10-03 22:48:47.700169+00
5	20	1	2020/Q20.webp	Texto-base compartilhado (Texto 2 — charge) da prova 2020.	7	PUBLICAVEL	2026-10-03 22:48:47.739767+00	2026-10-03 22:48:47.739767+00
6	26	1	2020/Q26.webp	Figura (gráfico/fórmula) da questão 26 da prova 2020 (página 9 do caderno).	9	PENDENTE_REVISAO	2026-10-03 22:48:47.770839+00	2026-10-03 22:48:47.770839+00
7	35	1	2020/Q35.webp	Figura compartilhada das questões 35 e 36 da prova 2020 (página 12 do caderno).	12	PUBLICAVEL	2026-10-03 22:48:47.798563+00	2026-10-03 22:48:47.798563+00
8	36	1	2020/Q36.webp	Figura compartilhada das questões 35 e 36 da prova 2020 (página 12 do caderno).	12	PUBLICAVEL	2026-10-03 22:48:47.825164+00	2026-10-03 22:48:47.825164+00
9	55	1	2022/Q15.webp	Texto-base compartilhado (Texto 2 — charge) da prova 2022.	6	PUBLICAVEL	2026-10-03 22:48:47.848994+00	2026-10-03 22:48:47.848994+00
10	56	1	2022/Q16.webp	Texto-base compartilhado (Texto 2 — charge) da prova 2022.	6	PUBLICAVEL	2026-10-03 22:48:47.86976+00	2026-10-03 22:48:47.86976+00
11	57	1	2022/Q17.webp	Texto-base compartilhado (Texto 3 — gráfico/charge) da prova 2022.	7	PUBLICAVEL	2026-10-03 22:48:47.8949+00	2026-10-03 22:48:47.8949+00
12	58	1	2022/Q18.webp	Texto-base compartilhado (Texto 3 — gráfico/charge) da prova 2022.	7	PUBLICAVEL	2026-10-03 22:48:47.929081+00	2026-10-03 22:48:47.929081+00
13	59	1	2022/Q19.webp	Texto-base compartilhado (Texto 2 — charge) da prova 2022.	7	PUBLICAVEL	2026-10-03 22:48:47.958828+00	2026-10-03 22:48:47.958828+00
14	71	1	2022/Q31.webp	Texto-base compartilhado (Texto 3 — gráfico/charge) da prova 2022.	10	PUBLICAVEL	2026-10-03 22:48:47.992518+00	2026-10-03 22:48:47.992518+00
15	77	1	2022/Q37.webp	Figura 3 (terceira figura) da questão 37 da prova 2022 (página 12 do caderno).	12	PUBLICAVEL	2026-10-03 22:48:48.023364+00	2026-10-03 22:48:48.023364+00
16	79	1	2022/Q39.webp	Figura 4 (quarta figura) da questão 39 da prova 2022 (página 12 do caderno).	12	PUBLICAVEL	2026-10-03 22:48:48.05756+00	2026-10-03 22:48:48.05756+00
17	103	1	2023/Q23.webp	Figura 1 compartilhada pelas questões 23 e 24 da prova 2023 (página 8 do caderno).	8	PUBLICAVEL	2026-10-03 22:48:48.088635+00	2026-10-03 22:48:48.088635+00
18	104	1	2023/Q24.webp	Figura 1 compartilhada pelas questões 23 e 24 da prova 2023 (página 8 do caderno).	8	PUBLICAVEL	2026-10-03 22:48:48.125501+00	2026-10-03 22:48:48.125501+00
19	105	1	2023/Q25.webp	Figura (pentágono/hexágono) da questão 25 da prova 2023 (página 9 do caderno).	9	PENDENTE_REVISAO	2026-10-03 22:48:48.162978+00	2026-10-03 22:48:48.162978+00
20	108	1	2023/Q28.webp	Texto-base compartilhado (Texto 2 — gráfico) da prova 2023.	10	PUBLICAVEL	2026-10-03 22:48:48.191946+00	2026-10-03 22:48:48.191946+00
21	109	1	2023/Q29.webp	Texto-base compartilhado (Texto 2 — gráfico) da prova 2023.	10	PUBLICAVEL	2026-10-03 22:48:48.227945+00	2026-10-03 22:48:48.227945+00
22	138	1	2024/Q18.webp	Texto-base compartilhado (Texto 3 — gráfico) da prova 2024.	7	PUBLICAVEL	2026-10-03 22:48:48.26018+00	2026-10-03 22:48:48.26018+00
23	151	1	2024/Q31.webp	Texto-base compartilhado (Texto 3 — gráfico) da prova 2024.	11	PUBLICAVEL	2026-10-03 22:48:48.29682+00	2026-10-03 22:48:48.29682+00
24	152	1	2024/Q32.webp	Texto-base compartilhado (Texto 3 — gráfico) da prova 2024.	11	PUBLICAVEL	2026-10-03 22:48:48.328087+00	2026-10-03 22:48:48.328087+00
25	153	1	2024/Q33.webp	Texto-base compartilhado (Texto 3 — gráfico) da prova 2024.	11	PUBLICAVEL	2026-10-03 22:48:48.364843+00	2026-10-03 22:48:48.364843+00
26	175	1	2025/Q15.webp	Texto-base compartilhado (Texto 2 — charge) da prova 2025.	6	PUBLICAVEL	2026-10-03 22:48:48.398274+00	2026-10-03 22:48:48.398274+00
27	176	1	2025/Q16.webp	Texto-base compartilhado (Texto 2 — charge) da prova 2025.	6	PUBLICAVEL	2026-10-03 22:48:48.425569+00	2026-10-03 22:48:48.425569+00
28	186	1	2025/Q26.webp	Texto-base compartilhado (Texto 2 — charge) da prova 2025.	9	PUBLICAVEL	2026-10-03 22:48:48.459252+00	2026-10-03 22:48:48.459252+00
29	187	1	2025/Q27.webp	Texto-base compartilhado (Texto 2 — charge) da prova 2025.	9	PUBLICAVEL	2026-10-03 22:48:48.487073+00	2026-10-03 22:48:48.487073+00
30	193	1	2025/Q33.webp	Figura (gráfico/tabela) da questão 33 da prova 2025 (página 11 do caderno).	11	PENDENTE_REVISAO	2026-10-03 22:48:48.512895+00	2026-10-03 22:48:48.512895+00
31	196	1	2025/Q36.webp	Figura (trecho/ilustração) da questão 36 da prova 2025 (página 11 do caderno).	11	PENDENTE_REVISAO	2026-10-03 22:48:48.548864+00	2026-10-03 22:48:48.548864+00
32	218	1	2026/Q18.webp	Texto-base compartilhado (Texto 2 — charge) da prova 2026.	7	PUBLICAVEL	2026-10-03 22:48:48.585525+00	2026-10-03 22:48:48.585525+00
33	219	1	2026/Q19.webp	Texto-base compartilhado (Texto 2 — charge) da prova 2026.	8	PUBLICAVEL	2026-10-03 22:48:48.618433+00	2026-10-03 22:48:48.618433+00
34	220	1	2026/Q20.webp	Texto-base compartilhado (Texto 2 — charge) da prova 2026.	8	PUBLICAVEL	2026-10-03 22:48:48.649344+00	2026-10-03 22:48:48.649344+00
35	230	1	2026/Q30.webp	Texto-base compartilhado (Texto 2 — charge) da prova 2026.	10	PUBLICAVEL	2026-10-03 22:48:48.705541+00	2026-10-03 22:48:48.705541+00
36	239	1	2026/Q39.webp	Texto-base compartilhado (Texto 2 — charge) da prova 2026.	12	PUBLICAVEL	2026-10-03 22:48:48.733342+00	2026-10-03 22:48:48.733342+00
37	34	1	2020/Q34.webp	Figura da questão 34 da prova 2020 (página 8 do caderno).	8	PUBLICAVEL	2026-10-03 22:48:48.768858+00	2026-10-03 22:48:48.768858+00
38	72	1	2022/Q32.webp	Figura 1 da questão 32 da prova 2022 (página 11 do caderno).	11	PUBLICAVEL	2026-10-03 22:48:48.796264+00	2026-10-03 22:48:48.796264+00
39	74	1	2022/Q34.webp	Figura da questão 34 da prova 2022.	10	PUBLICAVEL	2026-10-03 22:48:48.830512+00	2026-10-03 22:48:48.830512+00
40	75	1	2022/Q35.webp	Figura da questão 35 da prova 2022 (página 11 do caderno).	11	PUBLICAVEL	2026-10-03 22:48:48.866689+00	2026-10-03 22:48:48.866689+00
41	200	1	2025/Q40.webp	Figura da questão 40 da prova 2025 (página 13 do caderno).	13	PUBLICAVEL	2026-10-03 22:48:48.901097+00	2026-10-03 22:48:48.901097+00
\.


--
-- Data for Name: question_options; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.question_options (id, question_id, label, option_text, created_at, updated_at) FROM stdin;
1	1	A	informar sobre o aumento do cyberbullying no Brasil.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
2	1	B	narrar situações de cyberbullying com crianças e jovens.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
3	1	C	criticar a dificuldade das famílias em perceber o cyberbullying.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
4	1	D	descrever casos de cyberbullying analisados por psicológos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
5	2	A	à falta de atenção dada aos filhos pela vida corrida nas grandes cidades.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
6	2	B	ao uso cada vez mais frequente dos smartphones pelos jovens.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
7	2	C	ao conhecimento reduzido dos pais sobre as novas tecnologias.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
8	2	D	à ausência de atenção dos pais ao uso indiscriminado da Internet pelos filhos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
9	3	A	as agressões só não são graves por serem oriundas de pessoas que estão fisicamente distantes.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
10	3	B	os ataques sofridos na Internet podem se tornar muito ofensivos e prejudiciais à vida das vítimas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
11	3	C	o surgimento do cyberbullying deve-se ao acesso irrestrito de crianças e jovens aos smartphones.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
13	4	A	tempo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
14	4	B	espaço.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
15	4	C	movimento.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
16	4	D	atualidade.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
17	5	A	três períodos e seis orações.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
18	5	B	quatro períodos e sete orações.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
19	5	C	dois períodos e cinco orações.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
20	5	D	cinco períodos e quatro orações.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
21	6	A	ira e mãe.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
22	6	B	Laura e Rita.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
23	6	C	Ela e Aquilo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
24	6	D	popular e fácil.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
25	7	A	apostos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
26	7	B	orações adjetivas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
27	7	C	vocativo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
28	7	D	adjuntos adverbiais.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
29	8	A	os casos apresentados no texto são isolados, pois não há pesquisas que comprovem a existência de cyberbullying no Brasil.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
30	8	B	o uso indiscriminado dos smartphones pelos jovens influencia muito pouco a disseminação das agressões a jovens.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
31	8	C	as agressões feitas na Internet podem causar maior impacto na vida das crianças do que aquelas realizadas presencialmente.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
33	9	A	a família não sabe ainda como proteger os filhos dos riscos do cyberbullying.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
34	9	B	a agressão não acontece no contexto escolar, mas em outros espaços sociais.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
35	9	C	o cyberbullying é inevitável, pois os pais não se dão conta dos riscos dele.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
36	9	D	o uso de smartphones tornou-se uma ameaça à boa convivência familiar.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
37	10	A	o percentual de vítimas de cyberbullying começou a decrescer no Brasil, desde 2013.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
38	10	B	a escola é o refúgio de crianças e adolescentes vítimas de agressão e de cyberbullying.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
39	10	C	o domínio precoce das tecnologias expõe as crianças aos riscos de cyberbullying.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
40	10	D	a parceria entre a escola e a família contra o cyberbullying mostrou-se eficaz desde 2014. Considere o trecho para responder às questões de 11 a 15. Segundo [1] especialistas, as ofensas na Internet podem ter impacto [2] ainda maior na vida das crianças. “Uma postagem atinge número incontável de pessoas e isso aumenta o sofrimento da vítima. Ela [3] não sabe quem viu ou não”, afirma a psicóloga e pesquisadora [4] da Universidade Estadual [5] Paulista (Unesp) Luciana Lapa.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
41	11	A	conformidade.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
42	11	B	consequência.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
43	11	C	condição.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
44	11	D	causa.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
45	12	A	vítima.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
46	12	B	psicóloga.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
47	12	C	pesquisadora.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
48	12	D	postagem.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
49	13	A	substantivo e substantivo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
50	13	B	adjetivo e adjetivo	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
51	13	C	adjetivo e substantivo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
53	14	A	destacar algumas expressões de conteúdo irônico.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
54	14	B	isolar citação textual que rompe com o registro formal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
55	14	C	destaca ideia expressa em registro informal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
56	14	D	isolar citação textual direta de autoria alheia.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
57	15	A	efeito.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
58	15	B	causa.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
59	15	C	duracão.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
60	15	D	possibilidade.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
61	16	A	um problema que atinge todos que têm acesso a tecnologias.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
62	16	B	uma brincadeira infantil inofensiva utilizada pelas crianças.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
63	16	C	um perigo para as crianças que usam livremente tecnologias.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
64	16	D	uma brincadeira infantil tão perigosa como outra qualquer.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
65	17	A	opressão.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
66	17	B	proteção.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
67	17	C	empatia.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
68	17	D	desrespeito.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
69	18	A	movimento da mão em proteção ao jovem estudante.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
70	18	B	acesso fácil dos jovens aos aparelhos conectados à Internet.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
71	18	C	tamanho da mão em relação ao seu próprio tamanho.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
72	18	D	fato de ser um jovem com farda e mochila a caminho da escola.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
73	19	A	argumentação caracterizada pela defesa de um ponto de vista.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
74	19	B	narração caracterizada pela veracidade dos fatos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
75	19	C	argumentação caracterizada pela linguagem verbal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
77	20	A	notícia e tirinha.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
78	20	B	artigo de opinião e cartaz.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
79	20	C	editorial e cartum.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
81	21	A	3974.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
82	21	B	3975.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
83	21	C	3976.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
84	21	D	3977.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
85	22	A	10 anos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
86	22	B	11 anos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
87	22	C	12 anos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
88	22	D	13 anos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
89	23	A	100 meninos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
90	23	B	150 meninos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
91	23	C	200 meninos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
92	23	D	250 meninos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
93	24	A	8 cm.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
94	24	B	11 cm.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
95	24	C	12 cm.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
97	25	A	60 minutos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
98	25	B	90 minutos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
99	25	C	120 minutos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
100	25	D	150 minutos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
101	26	A	. 11 23	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
102	26	B	. 12 11	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
103	26	C	. 23 12	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
104	26	D	. 23	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
105	27	A	5,6 ⋅ 106 .	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
106	27	B	5,6 ⋅ 107 .	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
107	27	C	56 ⋅ 106 .	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
108	27	D	56 ⋅ 107 .	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
109	28	A	12.000 pessoas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
110	28	B	14.000 pessoas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
111	28	C	16.000 pessoas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
113	29	A	24.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
114	29	B	30.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
115	29	C	34.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
116	29	D	40.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
117	30	A	R$ 620,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
118	30	B	R$ 648,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
119	30	C	R$ 660,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
120	30	D	R$ 688,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
121	31	A	50%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
122	31	B	4%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
123	31	C	25%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
124	31	D	10%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
125	32	A	120,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
126	32	B	210,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
127	32	C	280,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
129	33	A	22,4 milhões.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
130	33	B	34,6 milhões.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
131	33	C	14,2 milhões.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
132	33	D	23,5 milhões.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
133	34	A	852.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
134	34	B	965.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
135	34	C	754.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
137	35	A	29%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
138	35	B	34%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
139	35	C	21%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
140	35	D	49%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
141	36	A	456.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
142	36	B	324.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
143	36	C	281.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
144	36	D	309.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
145	37	A	. 50 127𝑥	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
146	37	B	. 20 117𝑥	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
147	37	C	. 10 157𝑥	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
149	38	A	12 5	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
150	38	B	12 2	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
151	38	C	3 1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
152	38	D	3	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
153	39	A	𝑦 = 390. 𝑥 + 300.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
154	39	B	𝑦 = 150. 𝑥 + 390.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
155	39	C	𝑦 = 150. 𝑥 + 300.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
156	39	D	𝑦 = 390. 𝑥 + 150.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
157	40	A	segunda-feira, às 06 horas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
158	40	B	segunda-feira, às 14 horas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
159	40	C	terça-feira, às 06 horas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
161	41	A	o governo tem ouvido as comunidades indígenas envolvidas em conflitos de terra.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
162	41	B	as invasões constantes de terras indígenas interferem na preservação da cultura desses povos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
163	41	C	as organizações não governamentais desconhecem o problema da invasão de terras indígenas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
164	41	D	o Ibama, a Polícia Federal e a Funai comprovam as fiscalizações realizadas em terras indígenas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
165	42	A	noticia, incluindo certa crítica, que o número de invasões em terras indígenas aumentou.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
166	42	B	descreve, de forma imparcial, como as terras indígenas estão sendo desmatadas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
167	42	C	narra, incluindo opiniões, as ações de invasão que estão ocorrendo nas terras indígenas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
168	42	D	defende, de forma velada, a posição do governo em relação ao desmatamento das terras indígenas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
169	43	A	narração, devido à presença de verbos no presente.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
170	43	B	argumentação, devido à presença de progressão textual.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
171	43	C	narração, pois há uma relação temporal entre os enunciados.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
172	43	D	argumentação, pois há um posicionamento sobre a temática. Leia o trecho a seguir para responder às questões 04, 05 e 06. Lá, segundo os caciques, os invasores derrubam a mata nativa para retirar madeira, para a criação de gado e até para plantar maconha.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
173	44	A	“povo Tembé”.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
174	44	B	“terra indígena Alto Rio Guamá”.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
175	44	C	“mata nativa”.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
176	44	D	“caciques”.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
177	45	A	condição.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
178	45	B	temporalidade.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
179	45	C	consequência.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
180	45	D	finalidade.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
181	46	A	manifesta a concordância do autor com o fato de se desmatar para plantar maconha.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
182	46	B	indica um limite espacial para a plantação de maconha na mata devastada.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
183	46	C	expressa a indignação do autor em relação ao motivo pelo qual a terra foi devastada.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
185	47	A	“Pará, Rondônia e Amazonas são os estados onde houve mais invasões no ano passado.”.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
186	47	B	“Lá, segundo os caciques, os invasores derrubam a mata nativa para retirar madeira, [...]	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
187	47	C	“O bispo emérito do Xingu, que vai participar do Sínodo no Vaticano, defendeu que os índios precisam ter suas terras protegidas.”	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
188	47	D	‘"A nossa área preservada tem muito fundamento para a questão indígena, porque sem a área preservada nós não temos como mostrar a nossa cultura"’ [...]. Considere o trecho para responder à questão 8 Segundo o levantamento, em 2017, foram 96 invasões em terras indígenas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
189	48	A	de acordo com.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
190	48	B	mesmo com.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
191	48	C	apesar de.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
192	48	D	tal qual. Considere o trecho abaixo retirado do Texto 1 para responder às questões 09, 10, 11 e 12. Pará, Rondônia e Amazonas são os estados onde houve mais invasões no ano passado. Uma das áreas citadas no relatório é a terra indígena Munduruku, no sudoeste do Pará. De acordo com o CIMI, existem mais de 500 garimpos ilegais na região, desmatando e poluindo os rios da bacia do Tapajós.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
193	49	A	aconteceram.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
194	49	B	aconteciam.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
195	49	C	aconteceu.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
196	49	D	acontecia.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
197	50	A	três períodos e quatro orações.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
198	50	B	três períodos e seis orações.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
199	50	C	dois períodos e cinco orações.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
200	50	D	dois períodos e três orações.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
201	51	A	complemento nominal e complementa o sentido do termo “bacia”	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
202	51	B	aposto explicativo e explica o sentido do termo “rios”.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
203	51	C	adjunto adnominal e especifica o termo “bacia”.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
465	117	A	60 e 80.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
205	52	A	polícia.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
206	52	B	indígenas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
207	52	C	emérito.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
208	52	D	sínodo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
209	53	A	convencer as autoridades sobre a necessidade de regulamentar terras indígenas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
210	53	B	sensibilizar o leitor de cada região sobre a situação de suas terras indígenas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
211	53	C	destacar a Região Norte pela quantidade de terras indígenas regularizadas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
212	53	D	informar o percentual de terras indígenas regularizadas por região brasileira.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
213	54	A	na região Sudeste é onde vive a menor quantidade de índios do país.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
214	54	B	o número de índios que vivem na região Sul é menor do que o número de índios que vivem no Nordeste.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
215	54	C	a região Norte é onde se localiza o maior percentual de terras indígenas regularizadas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
216	54	D	o percentual de terras indígenas regularizadas no Centro-Oeste é 4 vezes menor do que o percentual de terras regularizadas no Norte.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
217	55	A	afastamento de um ou de vários indivíduos pertencentes a uma coletividade, por questões políticas, doutrinárias etc.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
218	55	B	remoção das etnias minoritárias de uma área promovendo a superioridade de um determinado grupo étnico dominante.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
219	55	C	usurpação pelo qual uma pessoa é privada, ou espoliada, de coisa de que tenha propriedade ou posse.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
220	55	D	separação mediante a qual indivíduos e grupos perdem o contato físico e social com outros indivíduos e grupos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
221	56	A	expressa a coloquialidade da linguagem oral.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
222	56	B	demonstra o tom autoritário da fala do Juiz.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
223	56	C	caracteriza o uso linguístico de uma região.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
225	57	A	um posicionamento a favor do marco temporal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
226	57	B	um posicionamento contrário ao marco temporal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
227	57	C	uma crítica à posse de terras pelos indígenas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
228	57	D	uma crítica ao uso de tratores em área indígena.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
229	58	A	hostilidade.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
230	58	B	ousadia.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
231	58	C	avareza.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
232	58	D	pânico.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
233	59	A	amizade.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
234	59	B	respeito.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
235	59	C	confiança.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
236	59	D	cumplicidade.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
237	60	A	o tipo textual.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
238	60	B	a temática.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
239	60	C	o gênero textual.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
241	61	A	6.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
242	61	B	7.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
243	61	C	5.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
244	61	D	8.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
245	62	A	𝐼 ≥ 3𝑀 − 207.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
246	62	B	𝐼 ≤ 3𝑀 − 207.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
247	62	C	𝐼 ≤ 𝑀 − 207.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
248	62	D	𝐼 ≥ 𝑀 − 207.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
249	63	A	. 24 5	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
250	63	B	. 2400 5	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
251	63	C	. 24000 5	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
252	63	D	. 240	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
253	64	A	14.800.000.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
254	64	B	14.400.000.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
255	64	C	15.400.000.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
257	65	A	𝑁(𝑥) = 15𝑥 − 30.300.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
258	65	B	𝑁(𝑥) = 20𝑥 − 30.159.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
259	65	C	𝑁(𝑥) = 20𝑥 − 30.300.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
260	65	D	𝑁(𝑥) = 15𝑥 − 30.159.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
261	66	A	12 dias.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
262	66	B	14 dias.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
263	66	C	16 dias.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
264	66	D	18 dias.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
265	67	A	4,0 milhões.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
266	67	B	4,5 milhões.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
267	67	C	3,5 milhões.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
268	67	D	5,0 milhões.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
269	68	A	14.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
270	68	B	18.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
271	68	C	22.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
272	68	D	10.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
273	69	A	30.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
274	69	B	60.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
275	69	C	45.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
331	83	C	foi pensada por corporações do setor privado comprometidas com a pluralidade de etnias e de gêneros no país.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
466	117	B	90 e 120.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
277	70	A	8m e 32m.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
278	70	B	6cm e 24cm.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
279	70	C	6m e 24m.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
280	70	D	8cm e 32cm.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
281	71	A	em todas as regiões os percentuais são menores que 40%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
282	71	B	a diferença entre os percentuais referentes às regiões sul e sudeste é maior que 5%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
283	71	C	o percentual referente à região norte é menor do que a soma dos percentuais das demais regiões.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
284	71	D	o percentual médio de terras indígenas regularizadas nas cinco regiões é de 20%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
285	72	A	R$ 30,00.                                             https://doe.fundobrasil.org.br/sos-amazonia-site- institucional/single_step	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
286	72	B	R$ 100,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
287	72	C	R$ 45,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
288	72	D	R$ 60,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
289	73	A	20.10². ᶟ	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
290	73	B	2.10 .	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
291	73	C	0,2.10⁴.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
293	74	A	. 35 3                                         Problemas abordados       Nº de alunas           Nº de alunos	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
294	74	B	.                                                                 (meninas)              (meninos) 35 Grilagem de Terra             10                      2 1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
295	74	C	. 35                                              Desmatamento                   3                          8 20	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
296	74	D	. 35                                              Garimpo Ilegal                 1                          4 Contaminação dos Rios                1                          6 Total               15                      20	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
297	75	A	628.                                                                     Figura criada pelo elaborador.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
298	75	B	0,314.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
299	75	C	1,26.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
300	75	D	35.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
301	76	A	146.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
302	76	B	126.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
303	76	C	128.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
305	77	A	6,0.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
306	77	B	6,86. Figura 3	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
307	77	C	7,68.                                                                      Figura adaptada para fins pedagógicos. Disponível em:	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
308	77	D	7,0.                                                           https://www.florestal.gov.br/documentos/inform acoes-florestais/inventario-florestal-nacional- ifn/documentos/manual-de-campo-ifn/3202- anexo-6-procedimentos-para-medicao-de- altura/file	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
309	78	A	360000𝜋.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
310	78	B	30000𝜋.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
311	78	C	120000𝜋.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
312	78	D	90000𝜋.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
313	79	A	. ̅̅̅̅ 𝐴𝐵 ̅̅̅̅.𝐴𝐶 𝐴𝐵    ̅̅̅̅	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
314	79	B	. ̅̅̅̅ 𝐶𝐸 ̅̅̅̅ 𝐴𝐵 .𝐶𝐷̅̅̅̅	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
315	79	C	.                                                                   Figura 4 ̅̅̅̅ 𝐵𝐶                                                                      Figura adaptada para fins pedagógicos. ̅̅̅̅  ̅̅̅̅                                                                                         Disponível em: 𝐵𝐶 .𝐶𝐸	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
317	80	A	(110101101)2 .	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
318	80	B	(101101111)2 .	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
319	80	C	(101111010)2 .	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
321	81	A	argumentativo, por explicitar o quadro dos resultados de uma década da existência da Lei das Cotas no Brasil.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
322	81	B	narrativo, por contar como se deu o processo de instituição da Lei das Cotas no Brasil, há uma década.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
323	81	C	argumentativo, por defender um ponto de vista favorável à manutenção da Lei das Cotas no Brasil.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
324	81	D	narrativo, por informar o leitor sobre os impactos causados pela instituição da Lei das Cotas no Brasil.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
325	82	A	artigo de opinião.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
326	82	B	reportagem.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
327	82	C	editorial.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
328	82	D	notícia.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
329	83	A	foi criada para reparar o mal causado, historicamente, aos índios marginalizados pelo Estado brasileiro.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
330	83	B	cumpriu o papel de rever o crime de lesa-humanidade cometido pelo Estado brasileiro durante a escravidão.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
464	116	D	𝑇 = 0,03𝑋 + 0,04𝑌 + 0,05𝑍.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
333	84	A	o protagonismo de negros que vivem nas periferias dos grandes centros urbanos é garantido pelas forças de segurança pública.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
334	84	B	a igualdade de direitos previstos constitucionalmente, há muito tempo, vem sendo violada no Brasil.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
335	84	C	o contingente de alunos negros inseridos nas universidades brasileiras, nos últimos dez anos, é superior ao de brancos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
336	84	D	a presença dos negros nas universidades prova que o parlamento brasileiro tem compromisso com políticas públicas afirmativas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
337	85	A	o branco, atualmente, ocupa o papel de inferioridade do negro de outrora.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
338	85	B	a sociedade brasileira superou a segregação racial com a Lei das Cotas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
339	85	C	a legislação brasileira é determinada por grupos ultraconservadores.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
340	85	D	o extermínio de negros foi apenas reconfigurado na contemporaneidade. Considere o trecho para responder às questões de 06 a 12. Entre os argumentos, havia a alegação de que (1) a regra privilegia pretos e pardos em detrimento dos direitos dos brancos, tornando desigual a disputa por uma vaga nas universidades. É puro deboche falar em desigualdade, quando todas as ações do poder público e setor privado sempre mortificaram o artigo da Constituição anterior a de 1988, em que (2) todos (3) "são iguais perante as leis" (3). Escárnio (4).	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
341	86	A	a segunda oração do primeiro período cumpre a função sintática de completar o sentido de um nome.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
342	86	B	o primeiro período, sintaticamente, organiza-se a partir de quatro orações subordinadas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
343	86	C	o segundo período é simples e organiza-se, sintaticamente, em torno de uma oração absoluta.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
344	86	D	a primeira oração do segundo período tem a função sintática de um adjunto adverbial, indicando circunstância de tempo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
345	87	A	Entre os argumentos tem a alegação de que a regra privilegia pretos e pardos e prejudica os direitos dos brancos, o que torna desigual, a disputa por uma vaga nas universidades.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
346	87	B	Entre os argumentos, têm a alegação de que a regra favorece pretos e pardos, prejudicando os direitos dos brancos, o qual torna desigual a disputa por uma vaga nas universidades.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
347	87	C	Existia entre os argumentos a alegação de que a regra privilegia pretos e pardos, prejudicando os direitos dos brancos, o que torna desigual a disputa por uma vaga nas universidades.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
349	88	A	pronome.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
350	88	B	advérbio.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
351	88	C	conjunção.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
352	88	D	partícula expletiva.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
353	89	A	ações.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
354	89	B	poder público.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
355	89	C	setor privado.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
356	89	D	artigo da Constituição.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
357	90	A	enfatizar uma expressão.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
358	90	B	destacar uma citação direta.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
359	90	C	destacar uma citação indireta.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
360	90	D	indicar início e fim de um discurso de autoridade.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
361	91	A	desdém.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
362	91	B	imperícia.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
363	91	C	desleixo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
364	91	D	arrogância.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
365	92	A	público.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
366	92	B	étnica.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
367	92	C	abomináveis.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
369	93	A	separa adjunto adverbial.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
370	93	B	isola uma oração coordenada.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
371	93	C	separa oração subordinada adjetiva.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
372	93	D	isola termos de mesma função sintática.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
373	94	A	partícula apassivadora	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
374	94	B	agente da passiva.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
375	94	C	complemento nominal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
376	94	D	adjunto adnominal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
377	95	A	locução adjetiva e locução prepositiva.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
378	95	B	locução conjuntiva e locução verbal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
379	95	C	locução adverbial e locução adverbial.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
380	95	D	locução adverbial e locução prepositiva.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
381	96	A	conclusão de ideias previamente discutidas ao longo do texto.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
382	96	B	oposição em relação a algo que ainda será apresentado no texto.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
383	96	C	explicação de dados utilizados no desenvolvimento do texto.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
384	96	D	concessão em relação a algo que foi discutido anteriormente no texto.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
385	97	A	apresenta uma crítica à ideia racista de que negro não precisa estudar em uma universidade.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
386	97	B	tem o propósito de narrar um episódio de preconceito racial cometido contra uma criança negra.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
387	97	C	tem a intenção de informar o leitor sobre a criação da Lei das Cotas para negros nas universidades.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
389	98	A	você e sua.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
390	98	B	bem e toma.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
391	98	C	futebol e cota.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
392	98	D	universidade e tão.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
393	99	A	denotativa.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
394	99	B	conotativa.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
395	99	C	metonímica.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
396	99	D	pleonástica.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
397	100	A	a intenção comunicativa.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
398	100	B	o uso da linguagem não verbal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
399	100	C	o uso da linguagem verbal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
401	101	A	menos de 1140 são alunos negros.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
402	101	B	menos de 1860 são alunos não negros.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
403	101	C	mais de 1860 são alunos negros.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
404	101	D	mais de 1860 são alunos não negros.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
405	102	A	31/50.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
406	102	B	62/50.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
407	102	C	38/50.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
408	102	D	19/50. Considere a Figura 1 para responder às questões 23 e 24. Figura 1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
409	103	A	20.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
410	103	B	25.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
411	103	C	15.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
412	103	D	30.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
413	104	A	75√3⁄2.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
414	104	B	75√3⁄5.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
415	104	C	25√3⁄4.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
417	105	A	26/50.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
418	105	B	13/100.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
419	105	C	13/50.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
420	105	D	1/4.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
421	106	A	15 mil alunos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
422	106	B	46 mil alunos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
423	106	C	51 mil alunos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
424	106	D	56 mil alunos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
425	107	A	𝑦 = 830𝑥 + 2500.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
426	107	B	𝑦 = 2500𝑥 + 830.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
427	107	C	𝑦 = 830𝑥 − 2500.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
429	108	A	45,8%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
430	108	B	63,4%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
431	108	C	55,3%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
432	108	D	59,5%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
433	109	A	7%, 8%, 15% e 7%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
434	109	B	8%, 20%, 10% e 7%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
435	109	C	8%, 20%, 12% e 7%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
436	109	D	7%, 9%, 12% e 8%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
437	110	A	9890.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
438	110	B	12432.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
439	110	C	11798.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
441	111	A	300.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
442	111	B	200.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
443	111	C	260.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
444	111	D	360.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
445	112	A	5,5 ⋅ 106 .	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
446	112	B	5,5 ⋅ 104 .	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
447	112	C	5,5 ⋅ 105 .	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
448	112	D	5,5 ⋅ 107 .	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
449	113	A	50.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
450	113	B	70.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
451	113	C	60.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
452	113	D	80.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
453	114	A	375.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
454	114	B	125.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
455	114	C	250.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
456	114	D	350.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
457	115	A	72.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
458	115	B	100.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
459	115	C	144.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
461	116	A	𝑇 = 𝑋 + 𝑌 + 𝑍.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
462	116	B	𝑇 = 30𝑋 + 40𝑌 + 50𝑍.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
463	116	C	𝑇 = 0,3𝑋 + 0,4𝑌 + 0,5𝑍.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
467	117	C	120 e 160.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
468	117	D	180 e 240.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
469	118	A	R$ 160 000,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
470	118	B	R$ 120 000,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
471	118	C	R$ 140 000,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
472	118	D	R$ 180 000,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
473	119	A	150°.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
474	119	B	180°.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
475	119	C	120°.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
476	119	D	200°.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
477	120	A	2044.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
478	120	B	2034.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
479	120	C	2024.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
481	121	A	apresentar um quadro do desmatamento da Amazônia brasileira.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
482	121	B	discutir o papel que exercem os países na Cúpula da Amazônia.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
483	121	C	apresentar a programação das atividades da Cúpula da Amazônia.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
484	121	D	informar o leitor sobre um evento cuja temática trata de questões ambientais.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
485	122	A	situar o leitor no contexto da discussão, apresentando, sucintamente, o objeto de que tratará o texto.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
486	122	B	defender uma solução pacífica para a problemática do aquecimento global e da mudança climática.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
487	122	C	problematizar a função de cada país signatário de um acordo assumido para preservar o meio ambiente.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
488	122	D	discutir políticas públicas de controle do desmatamento para o desenvolvimento sustentável da Amazônia.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
489	123	A	apresentar o significado de expressões.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
490	123	B	explicar o significado de siglas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
491	123	C	introduzir explicações acessórias.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
493	124	A	a mudança climática, nos últimos tempos, provocou o desmatamento de mais da metade da floresta amazônica.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
494	124	B	a pauta principal da Cúpula da Amazônia serão os problemas sociais enfrentados por grupos sociais como os povos originários.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
495	124	C	as pesquisas apontam que o desmatamento de um quarto da floresta amazônica ocasionaria um colapso ambiental.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
496	124	D	a meta estabelecida pelo Brasil para a superação do desmatamento já foi atingida, antes da Cúpula da Amazônia.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
497	125	A	o governo brasileiro demonstra interesse em desenvolver políticas públicas para resolver os problemas ambientais.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
498	125	B	o governo brasileiro se mostra omisso em relação à implementação de políticas para a resolução dos problemas sociais na região.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
499	125	C	a Amazônia brasileira é a responsável pela emissão de cerca de 25% do gás carbônico na região nordeste da floresta.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
500	125	D	a sociedade civil é a responsável direta pela superação do desmatamento por disseminar gás carbônico no ar. Considere o trecho para responder às questões de 06 e 09. Em 2021, um grupo do INPE mostrou, após coletar nove anos de dados, que (1) algumas regiões da Amazônia brasileira já emitem mais gás carbônico para a atmosfera do que capturam, revertendo o papel do ecossistema de sorvedouro para ao menos uma parte do carbono extra (2) lançado pelos seres humanos no ar.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
501	126	A	dados de pesquisa apontam que os seres humanos já cumprem seu papel e poluem menos o meio ambiente.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
502	126	B	há uma informação implícita: antes, a Amazônia brasileira emitia menos gás carbônico que atualmente.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
503	126	C	A Amazônia brasileira é a maior responsável pela emissão de gás carbônico nos últimos 9 anos no mundo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
504	126	D	há um papel a ser desenvolvido pelo ecossistema amazônico: emitir mais gás carbono para o meio ambiente.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
505	127	A	um período simples e uma oração absoluta.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
506	127	B	dois períodos compostos por subordinação e duas orações.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
507	127	C	um período composto por subordinação e seis orações.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
509	128	A	pronome relativo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
510	128	B	conjunção coordenativa.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
511	128	C	pronome demonstrativo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
512	128	D	conjunção subordinativa.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
513	129	A	morfologicamente, é um adjetivo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
514	129	B	morfologicamente, é um substantivo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
515	129	C	mantém relação de antonímia com “raro”.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
516	129	D	mantém relação de sinonímia com “previsto”. Considere o trecho para resolver às questões de 10 a 13. Apesar dos dados alarmantes, o presidente Lula considera a Cúpula como momento para a organização das políticas e fim do desmatamento. “(1) O Brasil vai cumprir com aquilo que (2) foi prometido, nós vamos chegar ao desmatamento zero em 2030, escrevam e guardem para me cobrar"(3), disse em café da manhã com correspondentes da imprensa internacional. Além disso (4), um documento elaborado no evento (5) deve ser apresentado na COP28, em dezembro, em Dubai.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
517	130	A	enfatizar uma expressão.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
518	130	B	destacar uma citação direta.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
519	130	C	destacar uma citação indireta.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
520	130	D	marcar uma opinião de autoridade.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
521	131	A	Brasil.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
522	131	B	aquilo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
523	131	C	prometido.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
524	131	D	desmatamento.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
525	132	A	antecipar uma informação que deveria vir depois.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
526	132	B	explicitar uma informação suprimida no texto.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
527	132	C	retificar uma informação que foi apresentada antes.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
528	132	D	acrescentar uma informação ao que vem sendo enunciado.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
529	133	A	à Cúpula.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
530	133	B	ao desmatamento zero.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
531	133	C	ao café da manhã.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
533	134	A	denuncia a infantilidade do público que prefere programas de auditório.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
534	134	B	compara programas de auditório à reunião da Cúpula da Amazônia.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
535	134	C	critica a desinformação do público em relação à Cúpula da Amazônia.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
536	134	D	mostra que o público prefere programas de auditório a reuniões políticas. Considere o trecho para responder às questões 15 a 17. Eu tava falando da Cúpula da Amazônia (1)! E não da reunião entre a Xuxa, Angélica e a Eliana.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
537	135	A	locução adjetiva.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
538	135	B	complemento verbal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
539	135	C	locução adverbial.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
540	135	D	complemento nominal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
541	136	A	o registro informal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
542	136	B	o registro formal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
543	136	C	a variante social.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
544	136	D	a variante histórica.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
545	137	A	um vocábulo é proparoxítono.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
546	137	B	três vocábulos são monossílabos átonos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
547	137	C	quatro vocábulos são monossílabos tônicos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
548	137	D	dois vocábulos são acentuados pelo mesmo motivo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
549	138	A	sempre crescente de 2014 a 2017.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
550	138	B	sempre crescente de 2017 a 2022.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
551	138	C	decrescente em 2012 em relação a 2011.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
553	139	A	tipo textual.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
554	139	B	gênero textual.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
555	139	C	recorte temático.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
556	139	D	registro informal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
557	140	A	Texto 1 configura-se, quanto ao gênero textual, como um editorial.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
558	140	B	Texto 2 configura-se, predominantemente, como um texto descritivo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
559	140	C	Texto 3 configura-se, quanto ao gênero textual, como um gráfico.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
561	141	A	. 4 1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
562	141	B	. 8 2	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
563	141	C	. 3 3	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
564	141	D	. 5	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
565	142	A	72.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
566	142	B	84.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
567	142	C	78.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
568	142	D	90.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
569	143	A	1003.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
570	143	B	105.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
571	143	C	104.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
572	143	D	1004.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
573	144	A	. 5 4	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
574	144	B	. 5 2	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
575	144	C	. 7 4	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
577	145	A	2518.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
578	145	B	4515.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
579	145	C	5035.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
580	145	D	4953.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
581	146	A	110 cm.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
582	146	B	109 cm.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
583	146	C	106 cm.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
584	146	D	105 cm.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
585	147	A	43.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
586	147	B	40.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
587	147	C	50.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
588	147	D	55.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
589	148	A	40 homens.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
590	148	B	42 homens.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
591	148	C	43 homens.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
592	148	D	45 homens.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
593	149	A	08 mulheres e 11 homens.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
594	149	B	12 mulheres e 07 homens.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
595	149	C	07 mulheres e 12 homens.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
931	233	C	10.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
597	150	A	12.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
598	150	B	13.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
599	150	C	11.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
600	150	D	14. As questões 31, 32 e 33 deverão ser respondidas com base no Texto 3	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
601	151	A	A área desmatada é crescente de 2015 até 2022.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
602	151	B	A área desmatada é decrescente de 2008 até 2013.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
603	151	C	A área desmata em 2014 é o dobro da área desmatada em 2013.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
604	151	D	A área desmatada acumulada nos últimos quatro anos, entre 2019 e 2022, chegou aos 35.193 km 2.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
605	152	A	5376.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
606	152	B	5787.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
607	152	C	5490.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
608	152	D	5520.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
609	153	A	98,78 %.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
610	153	B	108,21 %.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
611	153	C	102,42 %.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
612	153	D	96,89 %.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
613	154	A	220.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
614	154	B	200.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
615	154	C	180.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
617	155	A	Contratar mais 4 trabalhadores.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
618	155	B	Contratar mais 8 trabalhadores.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
619	155	C	Contratar mais 2 trabalhadores.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
620	155	D	Contratar mais 6 trabalhadores.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
621	156	A	4 horas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
622	156	B	6 horas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
623	156	C	5 horas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
624	156	D	7 horas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
625	157	A	C(x) = 10.000x.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
626	157	B	C(x) = 12.000x.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
627	157	C	C(x) = 10.000 + 2.000x.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
628	157	D	C(x) = 2.000 + 10.000x.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
629	158	A	R$ 108.000.000,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
630	158	B	R$ 100.800.000,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
631	158	C	R$ 180.000.000,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
632	158	D	R$ 100.080.000,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
633	159	A	7250 km.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
634	159	B	8000 km.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
635	159	C	7750 km.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
637	160	A	40.000.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
638	160	B	30.000.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
639	160	C	25.000.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
641	161	A	a minoria da população respondente da pesquisa concorda com a restrição total do acesso ao celular nas escolas do Rio.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
642	161	B	a escola, na era da tecnologia, tornou-se para os estudantes do Rio um espaço de desaprendizagem.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
643	161	C	a maioria da população consultada é favorável à proibição total do uso do celular nas escolas do Rio.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
644	161	D	a avaliação em larga escala aponta que a responsabilidade de controlar o uso do celular na sala de aula é da família.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
645	162	A	ao fato de que os adolescentes se sentem nervosos se seus telefones não estão perto deles.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
646	162	B	ao fato de que seu uso inadequado pode ter um impacto negativo na saúde emocional.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
647	162	C	a uma recomendação do Programa de Avaliação Internacional de Estudantes (PISA).	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
649	163	A	informar sobre a proibição do uso de celulares e outros dispositivos eletrônicos nas escolas do Rio de Janeiro.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
650	163	B	explicar os motivos que levaram as escolas do Rio de Janeiro a proibir o uso de celulares e outros dispositivos eletrônicos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
651	163	C	identificar em que momentos é permitido utilizar celulares e outros dispositivos eletrônicos nas escolas do Rio de Janeiro.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
652	163	D	justificar o uso de celulares e outros dispositivos eletrônico em alguns momentos nas escolas do Rio de Janeiro.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
653	164	A	narração, porque apresenta justificativas que apontam como se deu o percurso do desenvolvimento tecnológico nas atividades escolares em diferentes países do mundo e do Brasil.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
654	164	B	narração, visto que apresenta, de forma detalhada, as percepções do autor sobre um acontecimento da contemporaneidade, o avanço descontrolado do desenvolvimento tecnológico.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
655	164	C	argumentação, pois tem por objetivo instruir o leitor, a partir da explicação de conceitos e informações sobre os impactos da tecnologia na melhoria do desempenho acadêmico dos estudantes.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
656	164	D	argumentação, pois defende o ponto de vista de que, se for usada em excesso ou de forma inadequada, a tecnologia pode ser prejudicial à formação acadêmica dos estudantes.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
932	233	D	12.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
657	165	A	permite inferir que, nas escolas do Rio, não havia ainda a restrição do uso do celular pelos estudantes.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
658	165	B	deixa pressuposta a ideia de que, nas escolas do Rio, o uso do celular era liberado em algum momento.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
659	165	C	traduz um comentário favorável à liberação do uso do celular pelos estudantes nas escolas do Rio.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
660	165	D	manifesta uma apreciação sobre possibilidades do uso do celular pelos estudantes nas escolas do Rio. Utilize o trecho para responder às questões 06 a 09. Diz o decreto: (1) “Fica proibida a utilização de celulares e outros dispositivos eletrônicos (2) pelos alunos nas unidades escolares da rede pública municipal de ensino nas seguintes situações: (3) dentro da sala de aula; fora da sala de aula quando houver explanação (4) do professor e/ou realização de trabalhos individuais ou em grupo na unidade escolar; durante os intervalos, incluindo o recreio”.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
661	166	A	um período e três orações.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
662	166	B	dois períodos e três orações.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
663	166	C	dois períodos e duas orações.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
664	166	D	um período e quatro orações.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
665	167	A	em (1), ele antecede uma citação.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
666	167	B	em (3), ele antecede uma citação.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
667	167	C	em ambas as ocorrências, eles antecedem uma explicação.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
669	168	A	aposto.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
670	168	B	sujeito.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
671	168	C	objeto direto.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
672	168	D	complemento nominal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
673	169	A	organização.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
674	169	B	explicação.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
675	169	C	sistematização.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
676	169	D	fundamentação Considere o trecho do Texto 1 para responder às questões de 10 a 14. “Os celulares e demais dispositivos eletrônicos deverão (1) ser guardados na mochila ou bolsa do próprio aluno, desligado ou ligado em modo silencioso e sem vibração”, especifica o decreto. “A gente (2) acredita que a escola é um local de aprendizagem e interação social. As crianças não podem continuar ficando isoladas nas suas próprias telas, sem interagir umas com as outras, sem brincar (3). A escola precisa dessa interação humana”, disse o secretário municipal de Educação, Renan Ferreirinha. 8 em cada 10 pessoas são a favor de proibir o uso de celulares em escolas do Rio.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
677	170	A	necessidade.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
678	170	B	possibilidade.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
679	170	C	obrigatoriedade.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
680	170	D	probabilidade.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
681	171	A	escola.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
682	171	B	crianças.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
683	171	C	pessoas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
684	171	D	secretário.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
685	172	A	as crianças brincam entre si com os celulares.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
686	172	B	os celulares são usados pelas crianças para brincar.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
687	172	C	o uso dos celulares isola as crianças umas das outras.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
689	173	A	na escola, as crianças já não aprendem mais com os professores.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
690	173	B	na sociedade tecnológica, não há mais interação com as crianças.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
691	173	C	na escola, a partir de agora, as crianças estarão totalmente isoladas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
692	173	D	na contemporaneidade, as crianças estão se isolando por causa do celular.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
693	174	A	a ideia central do parágrafo está localizada no segundo período.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
694	174	B	a síntese das ideias do parágrafo está no último período.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
695	174	C	o parágrafo anuncia ao leitor a temática do texto.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
696	174	D	o parágrafo é predominantemente narrativo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
697	175	A	pontos de vista conflitantes.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
698	175	B	pontos de vista convergentes.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
699	175	C	inexistência de um ponto de vista.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
700	175	D	defesa explícita de um ponto de vista.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
701	176	A	recomenda o uso de livros impressos nas escolas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
702	176	B	censura o uso de livros impressos nas escolas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
703	176	C	apoia o uso de equipamentos eletrônicos nas escolas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
704	176	D	critica o uso de equipamentos eletrônicos nas escolas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
705	177	A	derivação parassintética.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
706	177	B	composição por aglutinação.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
707	177	C	composição por justaposição.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
708	177	D	derivação prefixal e sufixal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
709	178	A	sua temática é a mesma do Texto 1.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
710	178	B	tanto ele como o Texto 1 são do tipo descritivo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
711	178	C	ele tem a mesma intenção comunicativa do Texto 1.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
713	179	A	notícia e anedota.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
714	179	B	editorial e tirinha.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
933	234	A	854.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
715	179	C	reportagem e charge.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
716	179	D	artigo de opinião e cartum.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
717	180	A	registro informal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
718	180	B	recorte temático.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
719	180	C	gênero textual.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
721	181	A	1960.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
722	181	B	1600.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
723	181	C	800.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
724	181	D	490.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
725	182	A	3/5.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
726	182	B	5/2.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
727	182	C	2/5.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
728	182	D	6/25.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
729	183	A	1/3.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
730	183	B	0,30.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
731	183	C	0,45.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
732	183	D	1/5.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
733	184	A	R$ 150,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
734	184	B	R$ 160,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
735	184	C	R$ 165,50.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
736	184	D	R$ 155,50.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
737	185	A	300.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
738	185	B	500.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
739	185	C	600.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
741	186	A	9,0.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
742	186	B	9,5.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
743	186	C	8,0.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
744	186	D	8,5.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
745	187	A	4,8.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
746	187	B	5,2.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
747	187	C	5,0.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
749	188	A	186 dias, 10 horas e 17 minutos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
750	188	B	7 dias, 18 horas e 26 minutos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
751	188	C	18 dias, 7 horas e 21 minutos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
752	188	D	6 dias, 15 horas e 28 minutos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
753	189	A	10 h.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
754	189	B	8 h.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
755	189	C	12 h.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
756	189	D	7 h.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
757	190	A	45 cm.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
758	190	B	35 cm.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
759	190	C	40 cm.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
760	190	D	50 cm.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
761	191	A	51.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
762	191	B	96.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
763	191	C	249.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
764	191	D	300.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
765	192	A	R$ 2700,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
766	192	B	R$ 3300,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
767	192	C	R$ 3000,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
769	193	A	. 28 13	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
770	193	B	. 56 13	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
771	193	C	. 14 13	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
772	193	D	. 28	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
773	194	A	72.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
774	194	B	90.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
775	194	C	104.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
776	194	D	162.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
777	195	A	99.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
778	195	B	87.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
779	195	C	103.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
780	195	D	109. Considere o trecho baseado no Texto 1 para resolver a questão. “Rio proíbe celulares nas escolas até no recreio”, medida veio depois de uma consulta pública, aberta em dezembro, na qual 83% dos participantes manifestaram concordância com a restrição.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
781	196	A	. 30 732	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
782	196	B	. 900 741	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
783	196	C	. 900 24	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
785	197	A	R$ 1530,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
786	197	B	R$ 1860,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
787	197	C	R$ 1902,36.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
788	197	D	R$ 1865,06.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
789	198	A	𝐼(𝑥, 𝑦) = 250 + 300𝑥 + 360𝑦.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
790	198	B	𝐼(𝑥, 𝑦) = 300 + 360𝑥 + 250𝑦.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
791	198	C	𝐼(𝑥, 𝑦) = 360 + 250𝑥 + 300𝑦.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
792	198	D	𝐼(𝑥, 𝑦) = 300 + 360𝑥 + 300𝑦.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
793	199	A	9,0.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
794	199	B	6,7.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
795	199	C	1,9.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
796	199	D	6,1.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
797	200	A	60.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
798	200	B	72.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
799	200	C	71.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
801	201	A	é um órgão das Nações Unidas cuja preocupação central são as questões ambientais.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
802	201	B	é uma instituição brasileira cujo objetivo é o monitoramento das mudanças climáticas no mundo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
803	201	C	é um evento simbólico em que cada país sede, anualmente, mostra suas ações em defesa do meio ambiente.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
804	201	D	é um evento financiado pelas Nações Unidas em que os líderes mundiais discutem o desenvolvimento econômico.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
805	202	A	estabelecer metas ambiciosas para mitigar os efeitos das mudanças climáticas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
806	202	B	acompanhar os avanços nos preparativos da cidade-sede das conferências.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
807	202	C	mitigar o aquecimento global e manter o aumento da temperatura abaixo de 2º.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
809	203	A	informar o leitor sobre um evento que acontecerá no Brasil para discutir questões relativas às mudanças climáticas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
810	203	B	comunicar à população que o Brasil, no contexto internacional, é uma grande potência nas questões ambientais.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
811	203	C	apresentar ao leitor os altos investimentos do governo federal para que o Brasil possa sediar a COP30.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
812	203	D	refletir sobre o papel dos líderes mundiais em relação à necessidade de superação dos problemas ambientais.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
813	204	A	tem provocado um sentimento de insatisfação da população brasileira pelos gastos para sua realização.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
814	204	B	está servindo de exemplo para inspirar outros países a realizarem eventos turísticos como a COP30.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
815	204	C	tem gerado preocupações no que diz respeito à infraestrutura existente na cidade que sediará esse evento.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
816	204	D	está levando a população brasileira a uma tomada de consciência sobre a necessidade de cuidar da Amazônia.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
817	205	A	argumentativo, porque visa formar a opinião do leitor sobre uma temática de grande relevância social.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
818	205	B	descritivo, já que tem por fim apresentar as características da Amazônia, a maior floresta do planeta.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
819	205	C	narrativo, visto que conta sobre a realização de alguns eventos realizados para discutir questões ambientais.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
820	205	D	explicativo, uma vez que apresenta dados e informações sobre a COP30 para esclarecer o leitor sobre a temática.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
821	206	A	o Brasil pode se destacar na COP30 não só por possuir a Amazônia, mas também por usar energias limpas e renováveis.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
822	206	B	a COP30 pode tornar o Brasil um dos líderes globais e uma referência mundial na utilização de energia limpa.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
823	206	C	a Amazônia tem metade das florestas do mundo com papel na absorção de carbono e redução do efeito estufa na atmosfera.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
824	206	D	o Brasil sempre exerceu papel de liderança global de maior destaque no combate às mudanças climáticas.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
825	207	A	antecipar para o leitor possíveis conclusões sobre o evento COP30.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
826	207	B	apresentar, em linhas gerais, o recorte temático do texto ao leitor.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
827	207	C	discutir os problemas enfrentados pela organização da COP30.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
829	208	A	o trecho apresenta 3 períodos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
830	208	B	o trecho apresenta 2 períodos.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
831	208	C	o primeiro período apresenta 2 orações.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
832	208	D	o segundo período apresenta 5 orações.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
833	209	A	conferência.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
834	209	B	Zona Azul.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
835	209	C	Zona Verde.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
836	209	D	ONU.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
837	210	A	objeto direto.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
838	210	B	complemento nominal.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
839	210	C	complemento adverbial.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
840	210	D	sujeito. Considere o trecho a seguir para resolver às questões de 11 a 14. Apesar de(1) impulsionar(2) o desenvolvimento, a escolha da capital paraense como sede da COP carrega implicações ambientais e geopolíticas. No entanto(3), o desafio logístico é considerável. Acelerando os projetos de infraestrutura, o governo também se preocupa com a rede hoteleira: “(4)A questão dos leitos é crítica. Os preços subiram muito e isso pode afastar não só delegações oficiais, mas também(5) a sociedade civil, empresários e cientistas. A COP precisa ser acessível”(4), alertou Corrêa do Lago.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
841	211	A	retoma uma ideia de conformidade em relação àquilo que já foi dito.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
842	211	B	introduz uma ideia de concessão em relação a algo que será dito.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
843	211	C	reforça uma ideia de consequência em relação àquilo que será dito.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
844	211	D	imprime uma ideia de condição em relação a algo que já foi dito.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
845	212	A	fomentar.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
846	212	B	arrefecer	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
847	212	C	implementar.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
849	213	A	locução prepositiva e conjunção.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
850	213	B	conjunção e locução conjuntiva.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
851	213	C	preposição e preposição.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
852	213	D	conjunção e preposição.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
853	214	A	marca uma citação direta.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
854	214	B	enfatiza a voz de uma autoridade.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
855	214	C	ironiza as atitudes do governo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
856	214	D	marca uma citação indireta. O trecho a seguir deve ser utilizado para responder às questões 15 a 17. Com uma das matrizes energéticas(1) mais limpas do mundo,(2) um histórico(3) no uso de biocombustíveis(4) e investimentos crescentes em tecnologias sustentáveis(5), o país(6) pretende apresentar um novo modelo de crescimento verde(7).	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
857	215	A	1 e 6 são acentuadas por conterem hiato.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
858	215	B	3 é acentuada por ser paroxítona.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
859	215	C	6 é acentuada por ser oxítona.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
860	215	D	4 e 5 são acentuadas pela mesma regra.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
861	216	A	marcar omissão de um verbo.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
862	216	B	separar termos de mesma função sintática.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
863	216	C	separar oração intercalada.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
864	216	D	separar um aposto.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
865	217	A	busca financiar a abertura de indústrias que utilizam energias renováveis.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
866	217	B	privilegia a sustentabilidade ambiental em detrimento do desenvolvimento econômico.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
867	217	C	se ampara na participação cidadã e no seu engajamento em questões sociais e ambientais.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
868	217	D	concilia o crescimento econômico com a promoção da sustentabilidade ambiental.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
869	218	A	articulação entre a linguagem verbal, materializada no título; em sintonia com a linguagem não verbal, materializada no sorriso do trabalhador ao cortar a última árvore.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
870	218	B	presença da linguagem não verbal, que mostra o machado e a motosserra; e a área de mata devastada.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
871	218	C	articulação entre a linguagem não verbal, materializada nas árvores cortadas, no machado e na motosserra; e a linguagem verbal, materializada no diálogo entre os dois trabalhadores e o título.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
873	219	A	o Texto 1 é um comentário.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
874	219	B	o Texto 2 é uma charge.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
875	219	C	o Texto 1 é um editorial.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
876	219	D	o Texto 2 é uma tirinha.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
877	220	A	o recorte temático.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
878	220	B	o registro informal da linguagem.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
879	220	C	a predominância da linguagem conotativa.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
880	220	D	a tipologia textual.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
881	221	A	VVV e LLVX.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
882	221	B	XXX e MMXV.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
883	221	C	CCC e MCXV.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
884	221	D	XXX e MMXXV.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
885	222	A	100.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
886	222	B	102.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
887	222	C	101.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
888	222	D	103. Utilize as informações do Texto 1 a seguir para responder às questões 23 e 24. O evento da COP30, em Belém, estima receber cerca de 40 mil visitantes, dos quais, pelo menos, 7 mil serão integrantes da ONU e de delegações de países-membros.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
889	223	A	17,5%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
890	223	B	10%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
891	223	C	12,5%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
893	224	A	5250.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
894	224	B	3750.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
895	224	C	5050.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
896	224	D	4850.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
897	225	A	140.000.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
898	225	B	160.000.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
899	225	C	120.000.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
900	225	D	200.000.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
901	226	A	80h.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
902	226	B	50h.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
903	226	C	46h30min.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
904	226	D	66h40min.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
905	227	A	R$ 6.469,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
906	227	B	R$ 1.260,00	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
907	227	C	R$ 4.320,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
908	227	D	R$ 3.069,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
909	228	A	41%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
910	228	B	34%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
911	228	C	37%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
913	229	A	R$ 88.000,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
914	229	B	R$ 12.000,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
915	229	C	R$ 68.000,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
916	229	D	R$ 92.000,00.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
917	230	A	100 e 200.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
918	230	B	90 e 180.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
919	230	C	80 e 160.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
920	230	D	100 e 220.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
921	231	A	225 · (10 − 3π).	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
922	231	B	25 · (90 − 2π).	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
923	231	C	75 · (32 − 3π).	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
925	232	A	47%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
926	232	B	51%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
927	232	C	49%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
928	232	D	45%.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
929	233	A	6.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
930	233	B	8.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
934	234	B	850.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
935	234	C	852.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
936	234	D	856.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
937	235	A	𝑓(𝑥) = 500𝑥.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
938	235	B	𝑓(𝑥) = 20500𝑥.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
939	235	C	𝑓(𝑥) = 500 + 20000𝑥.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
940	235	D	𝑓(𝑥) = 20000 + 500𝑥.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
941	236	A	22,5.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
942	236	B	20.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
943	236	C	18,5.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
945	237	A	6.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
946	237	B	8.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
947	237	C	12.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
948	237	D	24.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
949	238	A	xy + 5x − 3y − 15.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
950	238	B	xy − 5x + 3y − 15.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
951	238	C	xy − 3x + 5y + 15.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
952	238	D	xy + 3x − 5y − 15.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
953	239	A	3 .	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
954	239	B	2𝑙. 𝑙	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
955	239	C	2 .	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
956	239	D	𝑙.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
957	240	A	600 e 400.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
958	240	B	400 e 600.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
959	240	C	500 e 300.	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
12	3	D	o sofrimento das vítimas do cyberbullying no Brasil pode ser amenizado por entidades protetivas. Considere o trecho a seguir para responder às questões de 4 a 7. Quando entrou em um colégio novo, na Zona Oeste do Rio, os problemas começaram para Laura, de 13 anos. “Ela é popular. Faz amizade fácil e é bonita. Aquilo provocou a ira de um grupo de colegas”, lembra Rita, de 46 anos, mãe da jovem.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
32	8	D	a maioria dos pais percebe, com antecedência, que os filhos estão vivendo alguma agressão por meio das redes sociais.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
52	13	D	substantivo e adjetivo.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
76	19	D	narração caracterizada pela presença de personagem.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
80	20	D	reportagem e charge.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
96	24	D	13 cm.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
112	28	D	18.000 pessoas.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
128	32	D	140,00.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
136	34	D	698. Considere o Gráfico 1 para responder às questões 35 e 36. Gráfico 1	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
160	40	D	terça-feira, às 14 horas.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
184	46	D	limita a três as atividades que provocaram o desmatamento da mata nativa.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
204	51	D	adjunto adverbial e determina o local dos rios.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
224	56	D	indica a classe social e o gênero do Juiz.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
240	60	D	a variação linguística.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
256	64	D	15.800.000.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
276	69	D	75.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
292	73	D	0,02.10⁵.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
304	76	D	138.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
316	79	D	. ̅̅̅̅ 𝐴𝐶                                                  https://congressoemfoco.uol.com.br/tema/meio- ambiente/governo-estuda-medidas-para-regularizar- garimpo-clandestino/	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
320	80	D	(111100110)2 .	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
332	83	D	reconfigurou as feições das universidades brasileiras, tornando-as menos elitizadas em dez anos.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
348	87	D	Haviam entre os argumentos a alegação de que a regra, privilegia pretos e pardos, e prejudica os direitos dos brancos, tornando desigual a disputa por uma vaga nas universidades.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
368	92	D	completará. Considere o trecho para responder às questões 13 a 15. Mesmo com todos os efeitos positivos, (1) a Lei das Cotas tem sido rechaçada por grupos conservadores e neonazistas (2), inconformados com a presença dos negros nas universidades (3) ou em postos de mando em algumas empresas (4).	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
388	97	D	apresenta uma descrição de como se configura o racismo estrutural no cenário brasileiro.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
400	100	D	a variante linguística do registro informal.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
416	104	D	5√3⁄2. Observe, na Figura 2, o organograma de distribuição de vagas de uma universidade. Figura 2 PPI: Pretos, pardos ou indígenas PcD: Pessoas com deficiência	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
428	107	D	𝑦 = 2500𝑥 − 830. Considere o Gráfico 1 para responder às questões 28 e 29. Gráfico 1: Posicionamento em relação à reserva de vagas para negros, por faixa etária, em percentual.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
440	110	D	9898.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
460	115	D	200.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
480	120	D	2054.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
492	123	D	dar destaque às informações.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
508	127	D	dois períodos: um simples e um composto por coordenação e três orações.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
532	133	D	à COP28.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
552	138	D	decrescente em 2022 em relação a 2021.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
560	140	D	Texto 3 configura-se, predominantemente, como um texto argumentativo.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
596	149	D	11 mulheres e 08 homens.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
616	154	D	160.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
636	159	D	8250 km.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
640	160	D	35.000.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
648	162	D	a um acordo para cumprir o protocolo do Programa Acesso Mais Seguro.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
148	37	D	.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:43.046349+00
944	236	D	25.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:43.046349+00
668	167	D	em ambas as ocorrências, eles antecedem uma enumeração.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
688	172	D	a interação entre as crianças é estimulada pelo uso dos celulares.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
712	178	D	seu registro de linguagem é o mesmo que o do Texto 1.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
720	180	D	tipo textual.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
740	185	D	400.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
748	187	D	4,0.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
768	192	D	R$ 6000,00.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
800	200	D	65.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:42.935255+00
576	144	D	. Considere o trecho para resolver as questões 25, 26 e 27. O tamanho dos aparelhos de televisão, em geral, é dado em polegadas e corresponde à medida da diagonal da tela do aparelho. Uma polegada é equivalente a 2,5 cm. No Texto 2, é possível ver uma televisão de formato retangular, sobre uma mesa. Suponha que as dimensões dessa tela plana são 95 cm de largura e 53 cm de altura.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:43.046349+00
784	196	D	.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:43.046349+00
808	202	D	implementar os compromissos globais de combate às mudanças climáticas.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:43.046349+00
828	207	D	sintetizar, em linhas gerais, as metas traçadas para a edição anterior. O trecho a seguir deve ser utilizado para responder às questões de 8 a 10. A conferência é dividida em Zona Azul e Zona Verde. A Zona Azul, que(1) é gerenciada diretamente pela ONU, é onde(2) acontecem as negociações políticas e os encontros diplomáticos(3). Já a Zona Verde sedia painéis para o público geral, apresentação de ONG e outras atividades, inclusive as culturais.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:43.046349+00
848	212	D	postergar.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:43.046349+00
872	218	D	presença da linguagem verbal, materializada no diálogo entre os trabalhadores, em contraste com o título do texto.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:43.046349+00
892	223	D	20%.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:43.046349+00
912	228	D	31%.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:43.046349+00
924	231	D	75 · (32 − 2π).	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:43.046349+00
960	240	D	300 e 500.	2026-10-01 00:57:59.264367+00	2026-10-03 00:52:43.046349+00
\.


--
-- Data for Name: question_passages; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.question_passages (id, question_id, passage_id, "position") FROM stdin;
1	1	1	1
2	2	1	2
3	3	1	3
4	8	1	4
5	9	1	5
6	10	1	6
7	20	1	7
8	21	1	8
9	22	1	9
10	26	1	10
11	27	1	11
12	31	1	12
13	32	1	13
14	33	1	14
15	37	1	15
16	16	2	1
17	17	2	2
18	18	2	3
19	19	2	4
20	20	2	5
21	30	2	6
22	4	3	1
23	5	3	2
24	6	3	3
25	7	3	4
26	11	4	1
27	12	4	2
28	13	4	3
29	14	4	4
30	15	4	5
31	41	5	1
32	42	5	2
33	43	5	3
34	44	5	4
35	61	5	5
36	62	5	6
37	63	5	7
38	64	5	8
39	65	5	9
40	73	5	10
41	74	5	11
42	76	5	12
43	78	5	13
44	80	5	14
45	53	6	1
46	54	6	2
47	71	6	3
48	55	7	1
49	56	7	2
50	57	7	3
51	59	7	4
52	44	8	1
53	45	8	2
54	46	8	3
55	48	9	1
56	49	10	1
57	50	10	2
58	51	10	3
59	52	10	4
60	81	11	1
61	82	11	2
62	83	11	3
63	84	11	4
64	85	11	5
65	96	11	6
66	100	11	7
67	113	11	8
68	97	12	1
69	98	12	2
70	99	12	3
71	100	12	4
72	103	12	5
73	119	12	6
74	86	13	1
75	87	13	2
76	88	13	3
77	89	13	4
78	90	13	5
79	91	13	6
80	92	13	7
81	93	14	1
82	94	14	2
83	95	14	3
84	121	15	1
85	122	15	2
86	123	15	3
87	124	15	4
88	125	15	5
89	139	15	6
90	140	15	7
91	141	15	8
92	142	15	9
93	143	15	10
94	144	15	11
95	134	16	1
96	139	16	2
97	140	16	3
98	138	17	1
99	139	17	2
100	140	17	3
101	151	17	4
102	126	18	1
103	127	18	2
104	128	18	3
105	129	18	4
106	130	19	1
107	131	19	2
108	132	19	3
109	133	19	4
110	135	20	1
111	136	20	2
112	137	20	3
113	145	21	1
114	146	21	2
115	147	21	3
116	161	22	1
117	162	22	2
118	163	22	3
119	164	22	4
120	165	22	5
121	179	22	6
122	180	22	7
123	181	22	8
124	187	22	9
125	175	23	1
126	176	23	2
127	177	23	3
128	178	23	4
129	179	23	5
130	180	23	6
131	166	24	1
132	167	24	2
133	168	24	3
134	169	24	4
135	170	25	1
136	171	25	2
137	172	25	3
138	173	25	4
139	174	25	5
140	201	26	1
141	202	26	2
142	203	26	3
143	204	26	4
144	205	26	5
145	206	26	6
146	207	26	7
147	217	26	8
148	219	26	9
149	220	26	10
150	228	26	11
151	218	27	1
152	219	27	2
153	220	27	3
154	208	28	1
155	209	28	2
156	210	28	3
157	211	29	1
158	212	29	2
159	213	29	3
160	214	29	4
161	215	30	1
162	216	30	2
163	217	30	3
164	223	31	1
165	224	31	2
\.


--
-- Data for Name: question_sources; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.question_sources (id, question_id, exam_document_id, role, page_start, page_end, doc_sha256, note, created_at, updated_at) FROM stdin;
1	1	2	PRIMARY	3	3	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
2	1	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
3	2	2	PRIMARY	3	3	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
4	2	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
5	3	2	PRIMARY	3	4	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
6	3	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
7	4	2	PRIMARY	4	4	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
8	4	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
9	5	2	PRIMARY	4	4	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
10	5	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
11	6	2	PRIMARY	4	4	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
12	6	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
13	7	2	PRIMARY	4	4	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
14	7	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
15	8	2	PRIMARY	4	5	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
16	8	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
17	9	2	PRIMARY	5	5	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
18	9	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
19	10	2	PRIMARY	5	5	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
20	10	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
21	11	2	PRIMARY	5	5	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
22	11	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
23	12	2	PRIMARY	5	5	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
24	12	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
25	13	2	PRIMARY	5	6	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
26	13	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
27	14	2	PRIMARY	6	6	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
28	14	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
29	15	2	PRIMARY	6	6	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
30	15	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
31	16	2	PRIMARY	6	6	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
32	16	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
33	17	2	PRIMARY	6	6	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
34	17	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
35	18	2	PRIMARY	6	6	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
36	18	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
37	19	2	PRIMARY	6	7	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
38	19	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
39	20	2	PRIMARY	7	8	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
40	20	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
41	21	2	PRIMARY	8	8	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
42	21	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
43	22	2	PRIMARY	8	8	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
44	22	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
45	23	2	PRIMARY	8	8	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
46	23	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
47	24	2	PRIMARY	8	9	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
48	24	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
49	25	2	PRIMARY	9	9	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
50	25	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
51	26	2	PRIMARY	9	9	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
52	26	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
53	27	2	PRIMARY	9	9	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
54	27	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
55	28	2	PRIMARY	9	10	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
56	28	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
57	29	2	PRIMARY	10	10	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
58	29	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
59	30	2	PRIMARY	10	10	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
60	30	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
61	31	2	PRIMARY	10	10	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
62	31	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
63	32	2	PRIMARY	10	11	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
64	32	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
65	33	2	PRIMARY	11	11	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
66	33	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
67	34	2	PRIMARY	11	12	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
68	34	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
69	35	2	PRIMARY	12	12	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
70	35	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
71	36	2	PRIMARY	12	12	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
72	36	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
73	37	2	PRIMARY	12	13	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
74	37	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
75	38	2	PRIMARY	13	13	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
76	38	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
77	39	2	PRIMARY	13	13	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
78	39	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
79	40	2	PRIMARY	13	14	f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
80	40	1	GABARITO	\N	\N	d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
81	41	4	PRIMARY	4	4	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
82	41	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
83	42	4	PRIMARY	4	4	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
84	42	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
85	43	4	PRIMARY	4	4	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
86	43	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
87	44	4	PRIMARY	4	4	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
88	44	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
89	45	4	PRIMARY	4	4	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
90	45	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
91	46	4	PRIMARY	4	5	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
92	46	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
93	47	4	PRIMARY	5	5	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
94	47	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
95	48	4	PRIMARY	5	5	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
96	48	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
97	49	4	PRIMARY	5	5	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
98	49	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
99	50	4	PRIMARY	5	5	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
100	50	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
101	51	4	PRIMARY	5	6	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
102	51	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
103	52	4	PRIMARY	6	6	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
104	52	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
105	53	4	PRIMARY	6	6	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
106	53	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
107	54	4	PRIMARY	6	6	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
108	54	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
109	55	4	PRIMARY	6	6	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
110	55	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
111	56	4	PRIMARY	6	7	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
112	56	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
113	57	4	PRIMARY	7	7	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
114	57	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
115	58	4	PRIMARY	7	7	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
116	58	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
117	59	4	PRIMARY	7	7	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
118	59	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
119	60	4	PRIMARY	7	8	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
120	60	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
121	61	4	PRIMARY	8	8	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
122	61	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
123	62	4	PRIMARY	8	8	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
124	62	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
125	63	4	PRIMARY	8	8	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
126	63	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
127	64	4	PRIMARY	8	9	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
128	64	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
129	65	4	PRIMARY	9	9	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
130	65	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
131	66	4	PRIMARY	9	9	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
132	66	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
133	67	4	PRIMARY	9	9	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
134	67	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
135	68	4	PRIMARY	9	9	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
136	68	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
137	69	4	PRIMARY	9	10	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
138	69	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
139	70	4	PRIMARY	10	10	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
140	70	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
141	71	4	PRIMARY	10	10	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
142	71	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
143	72	4	PRIMARY	10	10	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
144	72	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
145	73	4	PRIMARY	10	11	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
146	73	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
147	74	4	PRIMARY	11	11	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
148	74	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
149	75	4	PRIMARY	11	11	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
150	75	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
151	76	4	PRIMARY	11	12	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
152	76	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
153	77	4	PRIMARY	12	12	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
154	77	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
155	78	4	PRIMARY	12	12	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
156	78	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
157	79	4	PRIMARY	12	13	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
158	79	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
159	80	4	PRIMARY	13	14	ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
160	80	3	GABARITO	\N	\N	35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
161	81	6	PRIMARY	3	3	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
162	81	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
163	82	6	PRIMARY	3	3	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
164	82	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
165	83	6	PRIMARY	3	4	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
166	83	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
167	84	6	PRIMARY	4	4	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
168	84	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
169	85	6	PRIMARY	4	4	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
170	85	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
171	86	6	PRIMARY	4	4	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
172	86	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
173	87	6	PRIMARY	4	5	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
174	87	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
175	88	6	PRIMARY	5	5	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
176	88	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
177	89	6	PRIMARY	5	5	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
178	89	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
179	90	6	PRIMARY	5	5	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
180	90	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
181	91	6	PRIMARY	5	5	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
182	91	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
183	92	6	PRIMARY	5	6	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
184	92	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
185	93	6	PRIMARY	6	6	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
186	93	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
187	94	6	PRIMARY	6	6	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
188	94	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
189	95	6	PRIMARY	6	6	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
190	95	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
191	96	6	PRIMARY	6	6	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
192	96	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
193	97	6	PRIMARY	6	7	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
194	97	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
195	98	6	PRIMARY	7	7	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
196	98	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
197	99	6	PRIMARY	7	7	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
198	99	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
199	100	6	PRIMARY	7	8	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
200	100	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
201	101	6	PRIMARY	8	8	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
202	101	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
203	102	6	PRIMARY	8	8	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
204	102	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
205	103	6	PRIMARY	8	8	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
206	103	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
207	104	6	PRIMARY	8	9	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
208	104	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
209	105	6	PRIMARY	9	9	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
210	105	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
211	106	6	PRIMARY	9	9	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
212	106	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
213	107	6	PRIMARY	9	10	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
214	107	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
215	108	6	PRIMARY	10	10	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
216	108	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
217	109	6	PRIMARY	10	10	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
218	109	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
219	110	6	PRIMARY	10	11	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
220	110	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
221	111	6	PRIMARY	11	11	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
222	111	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
223	112	6	PRIMARY	11	11	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
224	112	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
225	113	6	PRIMARY	11	11	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
226	113	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
227	114	6	PRIMARY	11	11	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
228	114	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
229	115	6	PRIMARY	11	12	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
230	115	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
231	116	6	PRIMARY	12	12	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
232	116	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
233	117	6	PRIMARY	12	12	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
234	117	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
235	118	6	PRIMARY	12	12	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
236	118	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
237	119	6	PRIMARY	12	12	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
238	119	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
239	120	6	PRIMARY	12	13	99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
240	120	5	GABARITO	\N	\N	57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
241	121	8	PRIMARY	4	4	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
242	121	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
243	122	8	PRIMARY	4	4	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
244	122	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
245	123	8	PRIMARY	4	5	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
246	123	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
247	124	8	PRIMARY	5	5	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
248	124	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
249	125	8	PRIMARY	5	5	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
250	125	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
251	126	8	PRIMARY	5	5	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
252	126	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
253	127	8	PRIMARY	5	6	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
254	127	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
255	128	8	PRIMARY	6	6	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
256	128	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
257	129	8	PRIMARY	6	6	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
258	129	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
259	130	8	PRIMARY	6	6	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
260	130	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
261	131	8	PRIMARY	6	6	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
262	131	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
263	132	8	PRIMARY	6	6	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
264	132	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
265	133	8	PRIMARY	6	7	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
266	133	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
267	134	8	PRIMARY	7	7	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
268	134	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
269	135	8	PRIMARY	7	7	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
270	135	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
271	136	8	PRIMARY	7	7	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
272	136	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
273	137	8	PRIMARY	7	7	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
274	137	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
275	138	8	PRIMARY	7	8	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
276	138	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
277	139	8	PRIMARY	8	8	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
278	139	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
279	140	8	PRIMARY	8	9	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
280	140	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
281	141	8	PRIMARY	9	9	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
282	141	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
283	142	8	PRIMARY	9	9	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
284	142	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
285	143	8	PRIMARY	9	9	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
286	143	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
287	144	8	PRIMARY	9	10	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
288	144	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
289	145	8	PRIMARY	10	10	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
290	145	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
291	146	8	PRIMARY	10	10	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
292	146	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
293	147	8	PRIMARY	10	10	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
294	147	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
295	148	8	PRIMARY	10	10	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
296	148	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
297	149	8	PRIMARY	10	11	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
298	149	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
299	150	8	PRIMARY	11	11	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
300	150	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
301	151	8	PRIMARY	11	11	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
302	151	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
303	152	8	PRIMARY	11	11	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
304	152	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
305	153	8	PRIMARY	11	11	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
306	153	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
307	154	8	PRIMARY	11	12	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
308	154	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
309	155	8	PRIMARY	12	12	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
310	155	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
311	156	8	PRIMARY	12	12	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
312	156	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
313	157	8	PRIMARY	12	12	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
314	157	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
315	158	8	PRIMARY	12	12	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
316	158	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
317	159	8	PRIMARY	12	13	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
318	159	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
319	160	8	PRIMARY	13	14	8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
320	160	7	GABARITO	\N	\N	6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
321	161	10	PRIMARY	3	3	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
322	161	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
323	162	10	PRIMARY	3	4	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
324	162	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
325	163	10	PRIMARY	4	4	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
326	163	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
327	164	10	PRIMARY	4	4	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
328	164	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
329	165	10	PRIMARY	4	4	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
330	165	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
331	166	10	PRIMARY	4	4	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
332	166	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
333	167	10	PRIMARY	4	5	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
334	167	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
335	168	10	PRIMARY	5	5	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
336	168	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
337	169	10	PRIMARY	5	5	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
338	169	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
339	170	10	PRIMARY	5	5	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
340	170	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
341	171	10	PRIMARY	5	5	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
342	171	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
343	172	10	PRIMARY	5	6	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
344	172	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
345	173	10	PRIMARY	6	6	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
346	173	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
347	174	10	PRIMARY	6	6	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
348	174	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
349	175	10	PRIMARY	6	6	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
350	175	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
351	176	10	PRIMARY	6	6	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
352	176	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
353	177	10	PRIMARY	6	6	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
354	177	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
355	178	10	PRIMARY	6	7	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
356	178	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
357	179	10	PRIMARY	7	7	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
358	179	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
359	180	10	PRIMARY	7	8	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
360	180	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
361	181	10	PRIMARY	8	8	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
362	181	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
363	182	10	PRIMARY	8	8	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
364	182	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
365	183	10	PRIMARY	8	8	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
366	183	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
367	184	10	PRIMARY	8	8	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
368	184	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
369	185	10	PRIMARY	8	9	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
370	185	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
371	186	10	PRIMARY	9	9	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
372	186	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
373	187	10	PRIMARY	9	10	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
374	187	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
375	188	10	PRIMARY	10	10	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
376	188	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
377	189	10	PRIMARY	10	10	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
378	189	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
379	190	10	PRIMARY	10	10	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
380	190	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
381	191	10	PRIMARY	10	10	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
382	191	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
383	192	10	PRIMARY	10	11	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
384	192	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
385	193	10	PRIMARY	11	11	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
386	193	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
387	194	10	PRIMARY	11	11	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
388	194	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
389	195	10	PRIMARY	11	11	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
390	195	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
391	196	10	PRIMARY	11	12	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
392	196	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
393	197	10	PRIMARY	12	12	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
394	197	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
395	198	10	PRIMARY	12	12	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
396	198	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
397	199	10	PRIMARY	12	12	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
398	199	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
399	200	10	PRIMARY	12	13	3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
400	200	9	GABARITO	\N	\N	878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
401	201	12	PRIMARY	4	4	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
402	201	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
403	202	12	PRIMARY	4	5	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
404	202	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
405	203	12	PRIMARY	5	5	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
406	203	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
407	204	12	PRIMARY	5	5	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
408	204	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
409	205	12	PRIMARY	5	5	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
410	205	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
411	206	12	PRIMARY	5	5	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
412	206	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
413	207	12	PRIMARY	5	6	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
414	207	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
415	208	12	PRIMARY	6	6	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
416	208	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
417	209	12	PRIMARY	6	6	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
418	209	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
419	210	12	PRIMARY	6	6	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
420	210	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
421	211	12	PRIMARY	6	6	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
422	211	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
423	212	12	PRIMARY	6	7	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
424	212	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
425	213	12	PRIMARY	7	7	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
426	213	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
427	214	12	PRIMARY	7	7	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
428	214	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
429	215	12	PRIMARY	7	7	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
430	215	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
431	216	12	PRIMARY	7	7	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
432	216	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
433	217	12	PRIMARY	7	7	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
434	217	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
435	218	12	PRIMARY	7	8	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
436	218	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
437	219	12	PRIMARY	8	8	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
438	219	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
439	220	12	PRIMARY	8	8	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
440	220	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
441	221	12	PRIMARY	8	8	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
442	221	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
443	222	12	PRIMARY	8	8	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
444	222	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
445	223	12	PRIMARY	8	9	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
446	223	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
447	224	12	PRIMARY	9	9	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
448	224	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
449	225	12	PRIMARY	9	9	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
450	225	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
451	226	12	PRIMARY	9	9	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
452	226	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
453	227	12	PRIMARY	9	9	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
454	227	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
455	228	12	PRIMARY	9	10	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
456	228	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
457	229	12	PRIMARY	10	10	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
458	229	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
459	230	12	PRIMARY	10	10	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
460	230	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
461	231	12	PRIMARY	10	11	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
462	231	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
463	232	12	PRIMARY	11	11	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
464	232	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
465	233	12	PRIMARY	11	11	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
466	233	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
467	234	12	PRIMARY	11	11	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
468	234	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
469	235	12	PRIMARY	11	11	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
470	235	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
471	236	12	PRIMARY	11	12	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
472	236	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
473	237	12	PRIMARY	12	12	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
474	237	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
475	238	12	PRIMARY	12	12	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
476	238	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
477	239	12	PRIMARY	12	12	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
478	239	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
479	240	12	PRIMARY	12	13	6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
480	240	11	GABARITO	\N	\N	5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece	\N	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
\.


--
-- Data for Name: question_tag_map; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.question_tag_map (question_id, tag_id, created_at, updated_at) FROM stdin;
16	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
17	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
18	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
19	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
20	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
26	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
35	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
36	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
55	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
56	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
57	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
58	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
59	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
71	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
77	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
79	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
103	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
104	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
105	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
108	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
109	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
138	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
151	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
152	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
153	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
175	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
176	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
186	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
187	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
193	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
196	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
218	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
219	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
220	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
230	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
239	1	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
\.


--
-- Data for Name: question_tags; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.question_tags (id, code, description, created_at, updated_at) FROM stdin;
1	FIGURA	Questao depende de figura/grafico/charge ausente do texto extraido; requer revisao visual antes de publicar	2026-10-01 00:57:59.264367+00	2026-10-01 00:57:59.264367+00
\.


--
-- Data for Name: questions; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.questions (id, source_type, exam_id, exam_document_id, source_year, source_question_number, statement, kind, discipline_id, page_start, page_end, answer_key, annulled, checksum, has_figure, difficulty_estimate, explanation, validation_status, publication_status, created_at, updated_at) FROM stdin;
1	OFFICIAL	1	2	2020	1	A intenção comunicativa dominante no Texto 1 é	OBJECTIVE	1	3	3	A	f	82aa02ee12370dcdb25e6a93bb73996cb3b15b3c0b4baf8f5379d88246856251	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
2	OFFICIAL	1	2	2020	2	De acordo com o Texto 1, as famílias têm dificuldade em identificar o cyberbullying devido	OBJECTIVE	1	3	3	C	f	bb459eb70068df4e98dddb47240a7448a4de94a086c8e09d0d3b84881a6907a0	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
4	OFFICIAL	1	2	2020	4	O vocábulo Quando apresenta ideia de	OBJECTIVE	1	4	4	A	f	ddfd1c59fc666ac58fae38b9ab3014bfa732014610ac7a7f61f944085ff74db1	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
5	OFFICIAL	1	2	2020	5	O trecho constitui-se de	OBJECTIVE	1	4	4	B	f	a425dbead3fbd906f7900ec89605863af49d602b129866a99c9066d2cc778851	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
6	OFFICIAL	1	2	2020	6	Assinale a opção em que os termos apresentam a mesma função sintática.	OBJECTIVE	1	4	4	C	f	6111e3af2a7758d7fb8e7719be7fae700a86e2b6b53f20eb513f5bb9072abc09	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
7	OFFICIAL	1	2	2020	7	As vírgulas que separam as expressões sublinhadas “de 13 anos” e “de 46 anos” justificam-se por separar	OBJECTIVE	1	4	4	X	t	28f1d09ad6364ab0abc9c48ed3cbca8adcb758f0028cab73c4b48a60c5f9500d	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
9	OFFICIAL	1	2	2020	9	De acordo com o Texto 1,	OBJECTIVE	1	5	5	A	f	f3261b6070063019f9b30d9acf39091ce4ffc1648fdf29c0e84870cfe1fd5e07	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
10	OFFICIAL	1	2	2020	10	A leitura do Texto 1 permite inferir que	OBJECTIVE	1	5	5	C	f	139cf6e31c5fd2190c9fb9df75efc97fe38caa7882a564f5a66f15d3afe19426	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
11	OFFICIAL	1	2	2020	11	O elemento linguístico Segundo [1] indica sentido de	OBJECTIVE	1	5	5	A	f	a212120def4a1197d76654681fcf82daf99bdbe37ee68036520961ac0308ddff	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
12	OFFICIAL	1	2	2020	12	O elemento linguístico Ela [3] se refere à	OBJECTIVE	1	5	5	A	f	70cc66f51b40017e0f2fc1d96aa829a8e4f565b28285479e163deefe05558931	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
14	OFFICIAL	1	2	2020	14	No trecho, o uso das aspas justifica-se por	OBJECTIVE	1	6	6	D	f	ec52925fa9c422c26592fce43bd593c94e6bbefc8c9d748fdd80e3e733c06b55	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
15	OFFICIAL	1	2	2020	15	O vocábulo impacto [2] tem sentido de	OBJECTIVE	1	6	6	A	f	57709ee54f620f5a276e2b89ee54cdd9ccd0bb35174f234ee0e3f446c4042772	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
16	OFFICIAL	1	2	2020	16	A leitura do Texto 2 permite inferir que o cyberbullying é	OBJECTIVE	1	6	6	C	f	bc6960e3ce4779702acf962bdca7115104474b5669fb59a66fe9b7bf6fb9c8fc	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
17	OFFICIAL	1	2	2020	17	No Texto 2, a imagem da mão oferece ideia de	OBJECTIVE	1	6	6	A	f	9ae68e13e34b5c35e718e9c7a663c85f481ca0dadec254866919b36d40a9badb	t	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
18	OFFICIAL	1	2	2020	18	No Texto 2, a ideia de que o jovem está indefeso é dada pelo	OBJECTIVE	1	6	6	C	f	e055878d44b4489e6bead307198e200f74f2211d214cc8822ddefeca4762dac2	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
21	OFFICIAL	1	2	2020	21	De acordo com o Texto 1, quando entrou em um colégio novo, na Zona Oeste do Rio, os problemas começaram para Laura, de 13 anos. “Ela é popular. Faz amizade fácil e é bonita. Aquilo provocou a ira de um grupo de colegas”, lembra Rita, de 46 anos, mãe da jovem. Sabendo que o texto foi escrito em 2017, a soma dos anos de nascimento de Laura e sua mãe resulta em	OBJECTIVE	2	8	8	B	f	a4a76cdf840b26f5464d5fb86526c0a3ced51568708b5a4f16b6e9b1fcf54bd1	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
22	OFFICIAL	1	2	2020	22	Ao ler o Texto 1, onde Rita, de 46 anos, relata que sua filha Laura, de 13 anos, foi vítima de ofensas, um estudante de Matemática percebeu que a idade de Laura correspondeu a um quarto da idade da sua mãe quando Laura tinha	OBJECTIVE	2	8	8	B	f	be79930eece3f6786ca484602446c31b598939dbd03689c48cadddcfd9353489	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
23	OFFICIAL	1	2	2020	23	Uma das orientações dadas por especialistas para a prevenção do cyberbullying é a observação dos limites de idade definidos para o uso de redes socias. Em uma pesquisa feita com 450 alunos de uma 5 escola, observou-se que dos alunos da escola acessavam uma rede social com idade mínima maior 9 3 que a sua idade e que, desses, eram meninos. O número de meninos a acessar alguma rede social sem 5 ter a idade mínima necessária é	OBJECTIVE	2	8	8	B	f	2d80793325f9f34f291f6c6b5af4f76c269435953ce23b083f5947b046316df0	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
96	OFFICIAL	3	6	2023	16	O primeiro período do último parágrafo do Texto 1 introduz uma ideia de	OBJECTIVE	1	6	6	D	f	d51df1a4e99a935559c42a3b6d240a5f6c8dcb75dd6046c87c5985d618f9db3b	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
25	OFFICIAL	1	2	2020	25	Um pai preocupado com o cyberbullying em escolas e com o tempo de seu filho na internet resolve negociar a frequência com que ele acessará o mundo virtual. O pai estabeleceu que seu filho poderia utilizar a Internet dois quintos do tempo em que permanece estudando. Se o filho estuda 4 horas por dia no colégio, o tempo, em minutos, que deverá estudar em casa para que possa ficar na Internet por um período de duas horas é	OBJECTIVE	2	9	9	A	f	f50a7ff419cb80fdbc63e29a6db8dc4753550c929917b7e630267a3b6f6a5ceb	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
26	OFFICIAL	1	2	2020	26	De acordo com o Texto 1, apenas 23% das crianças e adolescentes relataram à pesquisa que foram vítimas de ofensas na Internet. Suponha que só 11% dos pais desse grupo disseram ter conhecimento que os filhos passaram por incômodos. Nessas condições, a razão entre o percentual de pais que desconhecem que seus filhos sofreram ofensas e a quantidade de crianças que relataram ofensas é de 23	OBJECTIVE	2	9	9	X	t	da3f43ec0a8f01a79226ce7259fe23134b5a5aae03dd04446d3682c6ed4e8767	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
27	OFFICIAL	1	2	2020	27	Segundo o Texto 1, 5,6 milhões de meninos e meninas entre 9 e 17 anos foram tratados de forma ofensiva na Internet. Em notação científica, a quantidade de meninos e meninas ofendidos nas redes sociais é	OBJECTIVE	2	9	9	A	f	264d24a5c45fc97738aa1ba3900d33e30dd3c37bc6bb066dccb5f2e146eee88f	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
29	OFFICIAL	1	2	2020	29	Os alunos de uma turma resolveram distribuir panfletos contra o cyberbullying. Inicialmente, haviam 4.800 panfletos para ser distribuídos e cada aluno iria distribuir a mesma quantidade de panfletos. No dia da distribuição, 6 alunos ficaram doentes e cada um dos alunos presentes teve que distribuir 40 panfletos a mais do que o planejado. O número de alunos presentes para a distribuição foi	OBJECTIVE	2	10	10	A	f	56e466bfffe5693f3dc90b53eb7b71796f55218ccecf2691439640f9df9a65ca	f	DIFICIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
30	OFFICIAL	1	2	2020	30	A imagem do Texto 2 será reproduzida em um outdoor para fomentar um debate sobre o cyberbullying em certa comunidade. A gráfica recebeu uma imagem original retangular com medidas de 18 cm de largura por 30 cm de comprimento. Sabendo que a gráfica irá ampliar a figura em 10 vezes do seu tamanho original e que o preço para a confecção desse outdoor é de R$ 120,00 por metro quadrado, o valor cobrado por esse outdoor será de	OBJECTIVE	2	10	10	B	f	1710d1b0ab11fbc5f79e400cbe6a372595e957c3ced00d5b3423379caf88a5c0	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
31	OFFICIAL	1	2	2020	31	De acordo com o título do Texto 1, uma em cada quatro crianças sofreu ofensas na Internet, no ano de 2017. Essa mesma proporção de crianças, percentualmente, corresponde a	OBJECTIVE	2	10	10	C	f	fefbe95f31ce6925b164a1e593504b3c75757ad859bf0e4bb79c2bdc67566177	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
33	OFFICIAL	1	2	2020	33	De acordo com o Texto 1, a pesquisa do Comitê Gestor da Internet no Brasil (CGI.br), em determinado mês, mediu o comportamento online de crianças e adolescentes. “Os dados revelaram que, de cada quatro crianças e adolescentes, um foi tratado de forma ofensiva na Internet, o que corresponde a 5,6 milhões de meninos e meninas entre 9 e 17 anos”. Com base nessas informações, aproximadamente, o total de jovens pesquisados é de	OBJECTIVE	2	11	11	A	f	dff8610417ea3b5dd7da634b989fa389bf9caa60677d0140433ffc465563368d	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
35	OFFICIAL	1	2	2020	35	Em uma escola da grande Natal, uma pesquisa de opinião realizada com 2524 estudantes mostrou que as agressões de cyberbullying sofridas por eles estão associadas a: estatura, obesidade, religião, higiene pessoal, etnia e gênero. Supondo que cada aluno só podia relatar um tipo de agressão, o Gráfico 1 mostra o número de alunos em cada tipo de agressão. Com base nessas informações, a diferença percentual entre as agressões com maior e menor registro, em relação ao total de entrevistados, aproximadamente, é de	OBJECTIVE	2	12	12	C	f	7eec6d3edd905b1a133cbae87d2327fc40e540d41f41be74418d1df059519bec	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
36	OFFICIAL	1	2	2020	36	Com base nos dados do Gráfico 1, a média aritmética do número de alunos que sofreu agressão na Internet relacionada a estatura, religião e gênero foi de	OBJECTIVE	2	12	12	C	f	90a1451ae9fc96e7a914fef6fef35caf815bcb97f7cf005a196f22284978dba4	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
38	OFFICIAL	1	2	2020	38	A equipe pedagógica de uma escola estadual sorteou o tema da palestra de abertura do ano letivo, utilizando o lançamento aleatório de um dado honesto, com 12 faces, numeradas de 1 até 12. Para isso, estabeleceram-se as seguintes condições: se após o lançamento do dado a face virada para cima for um número maior que 5, o tema versará sobre “Bullying na Internet” e, se a face voltada para cima for um número menor ou igual a 5, o tema versará sobre “Meio Ambiente”. Após o lançamento do dado, a probabilidade do tema sorteado ser sobre “Bullying na Internet” é de 7	OBJECTIVE	2	13	13	A	f	706fa898bbc88b9aca3d82d49a3b5f94c305e7d08642f3d8aa7c0aa825996d59	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
39	OFFICIAL	1	2	2020	39	Um casal resolveu levar a filha, vítima de cyberbullying, para passar 𝒙 dias em um hotel fazenda sem acesso à Internet. O custo total da viagem de 𝒚 reais corresponde a um valor fixo de R$150,00 para os gastos com combustível, mais R$ 390,00 a diária do quarto triplo com direito a todas as refeições. Em função de 𝒙 dias de hospedagem, a equação que representa o custo total de 𝒚 reais da viagem é	OBJECTIVE	2	13	13	D	f	6d97ae6fe5176acf0704aeee6910d433d59d124941744782d8042e6e00e40c2b	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
41	OFFICIAL	2	4	2022	1	A leitura do Texto 1 permite inferir que	OBJECTIVE	1	4	4	B	f	b3aaab3582bfe76a818a5a9969c1b3ae89955574694c9e813abfa2f417d7362a	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
42	OFFICIAL	2	4	2022	2	O Texto 1	OBJECTIVE	1	4	4	A	f	d4e34d7bcf35daa804c18932ca535c66d7ad0ed90c54ab1c6a44de1c31407e22	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
43	OFFICIAL	2	4	2022	3	No último parágrafo do Texto 1, predomina a	OBJECTIVE	1	4	4	C	f	2865f9180f4d6faf30519f3018f5a84937e634b323e2d85277d7dd10715e3bab	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
44	OFFICIAL	2	4	2022	4	Considerando sua inserção no Texto 1, o vocábulo LÁ refere-se a	OBJECTIVE	1	4	4	B	f	835755d03f5f75dc002d612b3c1cdd6577bfdde1e792996663893b5c628a3eb1	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
45	OFFICIAL	2	4	2022	5	O vocábulo PARA, assinalado no trecho acima, estabelece com a oração seguinte uma relação de	OBJECTIVE	1	4	4	D	f	75a3494ab6ed46ad62f6ecdd5ea2624c9b3a79193fd6024b1ebecc39be7c6c50	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
47	OFFICIAL	2	4	2022	7	Assinale a opção que apresenta um trecho em que o uso da vírgula se justifica pelo mesmo motivo de “Segundo o levantamento, em 2017, foram 96 invasões em terras indígenas.”.	OBJECTIVE	1	5	5	B	f	b67aa25fb5839fedf688782b70b1fd708c2d9811b04fc594cb60bc4c22f58f0f	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
48	OFFICIAL	2	4	2022	8	O termo destacado SEGUNDO, sem prejuízo ao sentido original no trecho, pode ser substituído por	OBJECTIVE	1	5	5	A	f	a676b472be854b441519da3b6808806691433879b1e88a7aee583dd8bf1da786	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
49	OFFICIAL	2	4	2022	9	Assinale a opção que substitui, respeitando a norma-padrão, a forma verbal HOUVE destacada no trecho.	OBJECTIVE	1	5	5	A	f	64a73f65860f268a394cafb2c71c631c83f266d03df7a063fad6c71ee332bed5	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
50	OFFICIAL	2	4	2022	10	O trecho possui	OBJECTIVE	1	5	5	B	f	81a6bedafbd17dd9f7da601dc73d8cfb43aac09f1ce63950f8517f2ddae861c3	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
52	OFFICIAL	2	4	2022	12	O vocábulo destacado RELATÓRIO recebeu acento agudo pelo mesmo motivo de	OBJECTIVE	1	6	6	A	f	be8e6702c5a57dc15de9dbbe085d75ad154317a1bdf11d5fe7b187b5d02d7412	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
53	OFFICIAL	2	4	2022	13	A intenção comunicativa prioritária do Texto 2 é	OBJECTIVE	1	6	6	D	f	f415b77f68ead52f2cb9a77412f2c2efbad60bca2cdb586db84acdfd504214f8	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
54	OFFICIAL	2	4	2022	14	As informações presentes no Texto 2 permitem afirmar que	OBJECTIVE	1	6	6	C	f	8c65a7f66cc16189e94e93e2d3f7369ed1f08ed5cbe5def0f3701453903038ad	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
55	OFFICIAL	2	4	2022	15	O marco temporal estabelece que só poderão solicitar demarcações os povos que comprovarem que habitavam a área requerida na data de promulgação da Constituição Federal, ou seja, em 5 de outubro de 1988. Considerando essa informação e a leitura da linguagem verbal e da não verbal do Texto 3, o termo jurídico esbulho é o ato de	OBJECTIVE	1	6	6	C	f	cf4167a6bfbb869f0aa86dd255fdc347ac5d94edf2b9eea8ff8fe9b88a1a397a	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
57	OFFICIAL	2	4	2022	17	O Texto 3 expressa	OBJECTIVE	1	7	7	B	f	f12da7191e703a0f7f84d4ce1270799c059fc880ea065285cea701a8da5f4732	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
58	OFFICIAL	2	4	2022	18	A expressão na face do Juiz indica	OBJECTIVE	1	7	7	A	f	696230bb68daa6e65c61397060670b4d74d06763f8e1fee75e0e32a922beb3dd	t	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
59	OFFICIAL	2	4	2022	19	A leitura do Texto 3 permite inferir que, entre seus personagens, existe	OBJECTIVE	1	7	7	D	f	0297736329b0d3a4b6dcdbdce05ceeff03242ee1b96479f429dab2f526917ced	t	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
61	OFFICIAL	2	4	2022	21	Segundo o texto 1, “Em setembro, os índios já tinham encontrado acampamentos, espingardas e várias pilhas de madeira em uma área a 30 quilômetros da aldeia.” Considerando que um índio o qual foi da sua aldeia a esses acampamentos e, em seguida, retornou à aldeia, levando 12 horas para finalizar o percurso, a velocidade média, em km/h, que esse índio imprimiu foi de	OBJECTIVE	2	8	8	C	f	c2427730af44ad986a00f9d6d511a7e7fb8feb524607721e7efc2dfbef11312e	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
62	OFFICIAL	2	4	2022	22	Um órgão fiscalizador estabeleceu como meta que a média aritmética das invasões em terras indígenas nos anos de 2017, 2018 e 2019 não ultrapassassem um valor predeterminado. Sabendo que, de acordo com o Texto 01, em 2017, foram registradas 96 invasões e, em 2018, foram registradas 111 invasões e se 𝑀 é a média aritmética predeterminada, a expressão que indica o número de invasões 𝐼 ,em 2019, para que a meta não seja ultrapassada, deverá ser	OBJECTIVE	2	8	8	B	f	f18d208f61d1a8e657a9654ab60f9a909faa9dd9e6c4b78421fffa9961a78c58	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
63	OFFICIAL	2	4	2022	23	O texto 1 comenta que, de acordo com o Conselho Indigenista Missionário (Cimi), somente na terra indígena (TI) de Munduruku existem mais de 500 garimpos ilegais. Sabendo que a TI de Munduruku tem, aproximadamente, 2.400.000 hectares, se considerarmos o número exato de 500 garimpos ilegais, com 100 garimpeiros em cada garimpo, a densidade demográfica, em habitantes por hectare, dos garimpeiros na terra indígena de Muduruku é de 5	OBJECTIVE	2	8	8	D	f	cd03a702d7b097930b9ee3b9427df9f0658c09e39a1d78009a0bab529db4644c	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
65	OFFICIAL	2	4	2022	25	Ao ler o texto 1, que indica 96 invasões em terras indígenas em 2017 e 111 em 2018, um estudante desejou exprimir uma função afim que determinasse o número de invasões (N) em função do ano (x). Para estimar as invasões esperadas nos anos seguintes, caso o crescimento das invasões fosse linear, a função encontrada foi	OBJECTIVE	2	9	9	D	f	0dd36fed664ca827cbbc763587bbba25b8a4dc96f7146e53a6738b035618076d	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
66	OFFICIAL	2	4	2022	26	Objetivando coibir as invasões em terras indígenas, os agentes de fiscalização organizaram três grupos de patrulhamento dessas terras. Considerando que o primeiro grupo passava por determinada aldeia a cada 3 dias, o segundo grupo, a cada 4 dias e o terceiro grupo, a cada 6 dias, se os três grupos passaram nessa aldeia hoje, eles passarão, todos no mesmo dia, novamente, daqui a	OBJECTIVE	2	9	9	A	f	bddba21f1e2a2193c56a3872d7ba11aa942dea6a9402b2b87f30f6cb80ba53f0	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
67	OFFICIAL	2	4	2022	27	Suponha que, por determinação de um juiz, os R$ 10 milhões em multas aplicadas aos acampamentos ilegais devam ser gastos na seguinte proporção: um quarto deverá ser destinado ao Ibama, dois quintos do que sobrou à Polícia Federal e o restante à Funai, o valor repassado à Funai foi de	OBJECTIVE	2	9	9	B	f	75c271dcc392539b8f268764ac5eefaaa7c1ab20a1cb201efba17bec43aca011	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
68	OFFICIAL	2	4	2022	28	Em uma audiência para se discutir a situação dos garimpos ilegais em terras indígenas estavam presentes 50 manifestantes, dentre indígenas e garimpeiros. Como a população indígena era significativamente menor na região, um repórter observou que a razão entre o número de indígenas e o de garimpeiros presentes na audiência era de um para quatro. O total de indígenas que estavam presentes na audiência era de	OBJECTIVE	2	9	9	D	f	3b809e725e456aa36e0a1186376ea0d8dc5d7fb90bb1833eaa6c15a61c1a824b	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
70	OFFICIAL	2	4	2022	30	Um cartógrafo deseja representar a área desmatada por garimpos ilegais em uma terra indígena. Observando que a área tem forma retangular, ele conclui que o comprimento da região desmatada é quatro vezes a largura da mesma e que o total desmatado foi de 256 hectares (ha). Sabendo que 1ha = 10.000 𝑚2 e que a representação será feita na escala 1:10.000, as dimensões representadas no desenho do cartógrafo terão largura e comprimento, em cm, respectivamente, iguais a	OBJECTIVE	2	10	10	D	f	0b369c6ee58c34d3f0f4b6ac5d2a026d3f685970e90438d73f9b086c004e3a39	f	DIFICIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
71	OFFICIAL	2	4	2022	31	Analisando o gráfico do Texto 2, a única afirmação correta é:	OBJECTIVE	2	10	10	D	f	89c2342b3a56b417d54be4b67b74b4fbfc1df551578fcda9a6195569ca9ce508	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
72	OFFICIAL	2	4	2022	32	Um internauta encontrou no site Fundo Brasil o convite para fazer doações mensais e ajudar os povos indígenas. A Figura 1, mostra o convite com os possíveis valores de doação. O internauta deseja doar um total de R$ 540,00, com a menor mensalidade possível, ao longo do maior número de meses e de modo que não ultrapasse mais de 12 meses. Para atender todas as condições, o internauta deve escolher fazer a doação no valor de Figura 1 Figura adaptada para fins pedagógicos. Disponível em:	OBJECTIVE	2	10	10	C	f	c9bb586803ea76a3df6b33d8cf2dd412c640cb37480a01ef4fb8af0e8eb5c933	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
74	OFFICIAL	2	4	2022	34	A Tabela 1 mostra o resultado de uma pesquisa de opinião com 35 estudantes sobre qual é o problema mais grave, dentre os destacados no relatório de violência e invasões nas terras indígenas, abordado no Texto 1. Escolhendo-se, ao acaso, uma pessoa desse grupo, a probabilidade que essa pessoa tenha opinado por desmatamento é de 11 Tabela 1- Problemas destacados nas invasões de terras indígenas	OBJECTIVE	2	11	11	A	f	d15d98efec79adae68452b2f33b2b04f33e6d1ca3abea3681d552e0ee0a68103	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
75	OFFICIAL	2	4	2022	35	A Figura 2 mostra dez troncos de madeira, hipoteticamente idênticos, em formato de cilindros retos, empilhados, cujas medidas de diâmetro e altura são, respectivamente, 20 centímetros e 1,0 metro. Considerando 𝜋 = 3.14, o volume de madeira, em m3, correspondente a todos os troncos empilhados é de Figura 2	OBJECTIVE	2	11	11	B	f	2b750d515afe15d0cd26ecdbf0b355c93e76e3cccc733be2d9fcf1ef4db83fac	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
77	OFFICIAL	2	4	2022	37	Um guarda florestal responsável por fiscalizar uma determinada área de preservação ambiental identificou as medidas dos lados 𝐴𝐶 ̅̅̅̅ e 𝐵𝐶 ̅̅̅̅ do triângulo retângulo ABC, como também o ponto A, considerando-o o ponto mais alto 10 m de uma determinada árvore, de acordo com a Figura 3. 10 m Sabendo que o guarda florestal, tem, aproximadamente, 1,68 m de altura, a medida, em metros, da altura da árvore é . de 8m	OBJECTIVE	2	12	12	C	f	53ebb2c04afb3f93f4cb1be861ed72c807926db8961bde199cbafa340feb619e	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
78	OFFICIAL	2	4	2022	38	Supondo que a aldeia dos Munduruku, mencionados no Texto1, tem o formato de um círculo de diâmetro igual a 600 metros, a medida da área desse círculo, em m2, é dada por	OBJECTIVE	2	12	12	D	f	bab8970d43801587ed2112a4b4ab5398fe61579231fabab1ece521a165028f50	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
81	OFFICIAL	3	6	2023	1	Quanto à organização tipológica, o Texto 1 é predominantemente	OBJECTIVE	1	3	3	C	f	a1482035e0f19e9d1403d3d5a2a16bf16fde0985969b02c8227903415369e872	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
82	OFFICIAL	3	6	2023	2	Quanto ao gênero textual, o Texto 1 configura-se como	OBJECTIVE	1	3	3	A	f	24a5b8e8945a957d67dc202eab7464e3b5331d3130e68228e1d90dcbf1dc41c0	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
84	OFFICIAL	3	6	2023	4	O Texto 1 permite inferir que	OBJECTIVE	1	4	4	B	f	dd9bab475e9a3dca750089935fec950369218ce4d9f9bfe3857bf560cb591988	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
85	OFFICIAL	3	6	2023	5	De acordo com o Texto 1,	OBJECTIVE	1	4	4	D	f	8c3266e9a08f84c866e26cabc5c1978f3654810d09568295a6f2ab229b30ea6e	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
86	OFFICIAL	3	6	2023	6	Sobre o trecho, é correto afirmar que	OBJECTIVE	1	4	4	A	f	d6511e0e1374b23d2fa6f5202c2583fc6034c649cc1eabc3a14404a86da30aa9	f	DIFICIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
88	OFFICIAL	3	6	2023	8	O vocábulo QUE (1) é	OBJECTIVE	1	5	5	C	f	3e6d1d428a5fac573a65c4a61f31b65f15d7f7e744c1ff4c099bba431e5db273	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
89	OFFICIAL	3	6	2023	9	O vocábulo QUE (2) refere-se a	OBJECTIVE	1	5	5	D	f	a0bfec2124e39c0b3210202143adf0c6eb002178f6a1a03c7d0ff65ba15555da	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
90	OFFICIAL	3	6	2023	10	No trecho, o uso das aspas (3) tem por fim	OBJECTIVE	1	5	5	B	f	4fc26ce274b2c458d6f148f2e82c87c6ad880683da38b84a3f917fef556b70b4	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
91	OFFICIAL	3	6	2023	11	No trecho, mantendo seu sentido original, a palavra ESCÁRNIO (4) pode ser substituída por	OBJECTIVE	1	5	5	A	f	79e3e09311a302ea2fa6c7be69156d248374b600b804047a6abddfc50a2460c2	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
93	OFFICIAL	3	6	2023	13	Em relação ao uso da vírgula assinalada com (1), é correto afirmar que ela	OBJECTIVE	1	6	6	A	f	2b5dd45327f4b4e8cea779968a9b76a3f71762794cd641b47cf35481d3c01920	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
94	OFFICIAL	3	6	2023	14	O termo em destaque POR GRUPOS CONSERVADORES E NEONAZISTAS (2) exerce função de	OBJECTIVE	1	6	6	B	f	b1f15f844d8d7db886f5ddc76c342658d4f68b54b37935e35fcb5184970a31a2	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
95	OFFICIAL	3	6	2023	15	Os elementos linguísticos NAS UNIVERSIDADES (3) e EM ALGUMAS EMPRESAS (4) têm, respectivamente, valor de	OBJECTIVE	1	6	6	C	f	0a23a0673f4cdfaf72211038a112bb1fc921b7c8401d05581523799cc85411f1	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
98	OFFICIAL	3	6	2023	18	No Texto 2, são expressões que têm o mesmo valor e mesma função sintática	OBJECTIVE	1	7	7	C	f	03e806f87de40dd6e6b29bab57b5bf7c2f13590fa0f4d95c69389ddbeeef3250	f	DIFICIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
99	OFFICIAL	3	6	2023	19	No Texto 2, o vocábulo COTA foi utilizado de forma	OBJECTIVE	1	7	7	B	f	bfcf697486f2e42e494c1c2aa0260ee7dfbbc03f396cbdb7f92fd2d0885e1513	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
101	OFFICIAL	3	6	2023	21	Uma universidade brasileira tem 3 mil alunos e mais de 38% deles são negros, logo,	OBJECTIVE	2	8	8	X	t	1357375b0cd02c329ecc4ebb51feddd839939905d0c32b340c512f5436ea65a4	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
102	OFFICIAL	3	6	2023	22	Escolhendo aleatoriamente um aluno de uma universidade brasileira com 3 mil alunos e considerando exatamente 38% o percentual de alunos negros matriculados nessa universidade, a probabilidade de que ele não seja negro é aproximadamente de	OBJECTIVE	2	8	8	A	f	de3005f2fd21aa6e7e6adc2e088e220d816ae2688c40e32a8f081b8f672dc07f	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
103	OFFICIAL	3	6	2023	23	A bola que aparece no Texto 2 é formada por 12 pentágonos e 20 hexágonos que são polígonos regulares, como representado na Figura 1. O perímetro, em cm, de cada um desses pentágonos corresponde a	OBJECTIVE	2	8	8	B	f	1638fb85b0c85043257b0670815b922b5e8173d89bb216c09f28e6619211b07b	t	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
105	OFFICIAL	3	6	2023	25	A fração da quantidade de vagas reservadas para alunos pretos, pardos ou indígenas em relação ao total de vagas disponibilizadas nessa instituição equivale a	OBJECTIVE	2	9	9	C	f	7d07cf1f6575f96e7968de6e45eb69912db7647419aa7cdb73281b11d41469f8	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
106	OFFICIAL	3	6	2023	26	O número de alunos pretos ou pardos matriculados em instituições de ensino superior demonstra como o acesso ao ensino universitário se modificou ao longo dos anos. Em 2006, em uma universidade, eles eram 15 mil estudantes; em 2020, passaram a ser 36 mil. Se o aumento se mantiver nessa proporção, em 2030, o número de alunos pretos ou pardos matriculados nessa instituição será de	OBJECTIVE	2	9	9	C	f	66eac413e0a476728163602c5edcf66d8c00a79ea85a8d037489fd83980072df	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
108	OFFICIAL	3	6	2023	28	Considerando as informações do Gráfico 1, a média aritmética do percentual de respondentes que são a favor de reserva de vagas para negros, em todas as faixas etárias, corresponde, aproximadamente, a	OBJECTIVE	2	10	10	D	f	ba36c341c10f6689a36f27f479d5fab73057985f234daf2dfbb0c5c25c0f52d6	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
109	OFFICIAL	3	6	2023	29	Os percentuais referentes aos respondentes que não souberam opinar, são, respectivamente	OBJECTIVE	2	10	10	B	f	9980b0ec03764906416b3fa14d0f8a8d12c4045e41b4b4557e59374dfe1d79f9	t	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
111	OFFICIAL	3	6	2023	31	Uma instituição de ensino ampliará a reserva de vagas para negros (pretos ou pardos) em 1000 vagas nos próximos 3 anos. No primeiro ano, a instituição pretende disponibilizar dois quintos das 1000 vagas planejadas; no segundo ano, pretende aumentar essa disponibilidade em dois quintos do restante de vagas planejadas e ainda não disponibilizadas; e no terceiro ano, aumentará a disponibilidade com as vagas que faltaram para completar as 1000. O número de vagas a mais que serão ofertadas no terceiro ano será	OBJECTIVE	2	11	11	D	f	26915247647a31da22126ce8052dedf59ea6568979485e17c20aefa026b1cbe1	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
112	OFFICIAL	3	6	2023	32	Em 2020, o número de estudantes em universidades federais era de 1,1 milhão. Considerando que a reserva de 50% das vagas para negros (pretos ou pardos) foi integralmente aplicada, o total de estudantes negros em universidades federais no ano de 2020, em notação científica, era	OBJECTIVE	2	11	11	C	f	ca88448da3c964faa37e09bbfaf30c6d5977a747fbf3e88e633613304317b0c4	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
113	OFFICIAL	3	6	2023	33	O Texto 1 informa que foram mais de 300 anos de escravidão. Se um legislador propuser que a Lei das Cotas, e seus efeitos, seja reavaliada a cada quinquênio por um período de 300 anos, o total de reavaliações sobre a Lei das Cotas a serem realizadas será	OBJECTIVE	2	11	11	C	f	7e5c1f2709acb3a4ca98bef5ad01bdbf138a09285e374241774ded809d912f2a	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
114	OFFICIAL	3	6	2023	34	Em uma pesquisa de opinião sobre o apoio à Lei das Cotas, 500 pessoas opinaram se apoiavam ou não a sua manutenção. Se o número de pessoas que apoiavam as cotas era o triplo do de pessoas que não apoiavam, o número de pessoas que apoiavam a Lei das Cotas nessa pesquisa foi	OBJECTIVE	2	11	11	A	f	34fe57d93873085c104c62696d442ade5010954ca95f6b391a1e6dc9442ee016	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
116	OFFICIAL	3	6	2023	36	Em um estado com instituições de ensino superior municipais, estaduais e federais, ficou estabelecido que as instituições municipais reservariam 30% das suas X vagas para pretos ou pardos; as instituições estaduais reservariam 40% das suas Y vagas; e as federais, 50% das suas Z vagas. A expressão que representa o total T de vagas ofertadas para pretos ou pardos nesse estado é	OBJECTIVE	2	12	12	C	f	4f8e6da377fdd8f333359e0fc446cd43b769a377ded2f356f30aed85336c2afd	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
117	OFFICIAL	3	6	2023	37	Para simbolizar os 300 anos de escravidão, uma instituição de ensino deseja construir uma praça em formato de triângulo retângulo com hipotenusa medindo 300 m, que seja semelhante a um triângulo retângulo pitagórico de lados medindo 3, 4 e 5. As medidas dos catetos da praça triangular, em metros, serão	OBJECTIVE	2	12	12	D	f	2ffc00f02202dc619c0eca93abb34d833ea5d659a975d9104aa6b3df4301a651	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
118	OFFICIAL	3	6	2023	38	O financiamento estudantil tem sido uma alternativa para os alunos que, infelizmente, não conseguem uma vaga em uma universidade pública. Se uma financeira oferece um financiamento para um curso de graduação que custa R$ 100 000,00 a uma taxa de juros simples de 1% ao mês e com pagamento ao final de 60 meses, o montante a ser pago pelo estudante que tomar esse financiamento será	OBJECTIVE	2	12	12	A	f	5f1af9f38a54adc09fefa2e048462fc8bcb3a942079eb94babb72c2d04998cd6	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
119	OFFICIAL	3	6	2023	39	Observando que a bola de futebol representada no Texto 2 é formada por pentágonos e hexágonos, um professor perguntou aos seus alunos qual a diferença entre a soma dos ângulos internos do hexágono e do pentágono se eles fossem considerados regulares. A resposta esperada é	OBJECTIVE	2	12	12	B	f	ce53a6ae3a3394cc43ccd8a96a6d74abb3462252607c8de46b4346bffee179f0	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
121	OFFICIAL	4	8	2024	1	O propósito comunicativo dominante no Texto 1 é	OBJECTIVE	1	4	4	D	f	c361207b3c2e4021f45a325e9a6992a11e012640f3b8f951e82fe302f5f05224	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
122	OFFICIAL	4	8	2024	2	No Texto 1, o primeiro parágrafo cumpre o papel de	OBJECTIVE	1	4	4	A	f	863e49f9ea8764f59d0d15433e36a447f0ec8c56af9e28e8728a9393b139e67c	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
124	OFFICIAL	4	8	2024	4	De acordo com o Texto 1, é correto afirmar que	OBJECTIVE	1	5	5	C	f	e6a9e4c8d15dc505ac9257a8c236c61f8228b7418e659b36d32887f990ad76b8	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
125	OFFICIAL	4	8	2024	5	A leitura do Texto 1 permite inferir que	OBJECTIVE	1	5	5	A	f	7e05a59031f50ca1b6dd42a677e027abff32bfd805cab6ecda0f8a8c19482962	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
126	OFFICIAL	4	8	2024	6	A leitura do trecho permite afirmar que	OBJECTIVE	1	5	5	B	f	80df900885f6277a229c529cac21c8159e49bf5213b4d74e23f05e4c6f006919	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
128	OFFICIAL	4	8	2024	8	O elemento linguístico que (1) é um(a)	OBJECTIVE	1	6	6	D	f	119fa6e440e9611192eea2e065c5cafd1f8722ec1c80906dea2e3d1cf03c29f5	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
129	OFFICIAL	4	8	2024	9	Sobre o elemento linguístico extra (2), é correto afirmar que	OBJECTIVE	1	6	6	A	f	c8236bbc79894eaee0a5191e233422d8f21772137fd2403245238d863a5a162f	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
130	OFFICIAL	4	8	2024	10	No trecho, o uso das aspas (1 e 3) tem por fim	OBJECTIVE	1	6	6	B	f	dfe28e04aad6316fe4666aa283f0d1e33ae36bd4fd160afc5e141968fc169431	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
131	OFFICIAL	4	8	2024	11	O elemento linguístico QUE (2) refere-se à palavra	OBJECTIVE	1	6	6	B	f	6108ca5eed4deb6d066fab231f656ec090a0e251af91e0cdc788a620a39cb360	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
132	OFFICIAL	4	8	2024	12	O elemento linguístico ALÉM DISSO (4) cumpre o papel de	OBJECTIVE	1	6	6	D	f	f56191bc797882ac72245fc39c6d95773b7c2ef547b0890aa2c48de20cf729b4	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
134	OFFICIAL	4	8	2024	14	O Texto 2	OBJECTIVE	1	7	7	C	f	ae1061abb9d127821215af9a791acc1cf64d7e92e59ffe66d3743c6b6e8d8d25	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
135	OFFICIAL	4	8	2024	15	A expressão DA AMAZÔNIA (1) classifica-se como	OBJECTIVE	1	7	7	A	f	9f94b2e38aa63c3983ecfa6795b118059b8f22ec242c005247393b13298883ad	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
136	OFFICIAL	4	8	2024	16	O trecho utiliza	OBJECTIVE	1	7	7	A	f	1d5fc072bfe2d1411fa1c4e21a5f005bf8c528024fc6ec1f1a8bd1a30f378ce1	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
137	OFFICIAL	4	8	2024	17	No trecho,	OBJECTIVE	1	7	7	X	t	5669c506597bda2a9b324e6e79f6b8caf8282d547bfcc90f509aa76a94623270	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
139	OFFICIAL	4	8	2024	19	O elemento que relaciona os textos 1, 2 e 3 entre si é o	OBJECTIVE	1	8	8	C	f	70b5943a2f5cc52f8563b450f9d2dae09831e9bf7950645ccaea55deb3d32758	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
141	OFFICIAL	4	8	2024	21	De acordo com o Texto 1, a Cúpula da Amazônia receberá os presidentes dos seguintes países: Brasil, Bolívia, Colômbia, Equador, Guiana, Peru, Suriname e Venezuela. Após a abertura do evento, foi realizado um sorteio, retirando um nome dentre esses países. A probabilidade do nome retirado, aleatoriamente, ser de um país cujo nome se inicia com a letra B foi de 1	OBJECTIVE	2	9	9	A	f	bbcf2a545edf598ce7d2134b5863f8645fdb463d55bff0ac6f201c73ad8bb331	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
142	OFFICIAL	4	8	2024	22	Segundo o Texto 1, mais de 60 organizações são signatárias da carta que aponta o que pode ser a contribuição da sociedade civil para a Cúpula da Amazônia. Se o número exato de organizações é múltiplo de 6 e é divisível por um número primo maior que 10, esse número pode ser	OBJECTIVE	2	9	9	C	f	489127c4fd2f75a4e8bc73e1015d8a415d2a78a053c19fc4dfc71b70152b6409	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
143	OFFICIAL	4	8	2024	23	De acordo com o Texto 1, a expectativa dos organizadores é que 10.000 pessoas participem, de forma presencial, da Cúpula da Amazônia. Uma representação desse número em forma de potência é de	OBJECTIVE	2	9	9	C	f	2e497492fd5ca6d8fd219416462bfb55f0e038973c6f23776187cace782fdb69	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
145	OFFICIAL	4	8	2024	25	O valor da área, em cm2, da tela dessa televisão é	OBJECTIVE	2	10	10	C	f	83ac268bda530d4190434c1ac859c47eeb7f7efd39efcc754e9e18875ef72d6e	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
146	OFFICIAL	4	8	2024	26	A medida da diagonal da tela, dessa televisão, aproximadamente, é de	OBJECTIVE	2	10	10	B	f	570f6d076b4b102a2d04b57a3334732e6b408414dce467088241d4f0bdde2c2d	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
147	OFFICIAL	4	8	2024	27	O tamanho da televisão, em polegadas, é mais próximo de	OBJECTIVE	2	10	10	A	f	6b58a1cc3f67979aea1a7956784cb2cd0ec4628241626b72545593ded8996b33	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
148	OFFICIAL	4	8	2024	28	Um estudo realizado por um engenheiro florestal identificou que para reflorestar uma certa área desmatada da Amazônia, seriam necessários 36 homens, trabalhando no mesmo ritmo durante 25 dias. Para realizar o mesmo trabalho, em apenas 20 dias, aplicando o mesmo ritmo de trabalho, seriam necessários um total de	OBJECTIVE	2	10	10	D	f	2e253e185a6f522e538e681a15cf95e808524474db98b1eda95955bd7b06aeeb	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
150	OFFICIAL	4	8	2024	30	Em uma reunião com membros dos oitos países que detêm o bioma amazônico estavam presentes 120 pessoas. Sabendo que um quarto delas eram brasileiros, um quinto eram do Equador e que cada um dos outros 6 países estavam com o mesmo número de representantes, o número de representantes de cada um desses 6 países era	OBJECTIVE	2	11	11	C	f	3971d5e18c7df1fd6c79cac3d7dcdf50bdc8d1907e6799eddfd0544b78407b97	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
151	OFFICIAL	4	8	2024	31	Com base no Texto 3, é correto afirmar que	OBJECTIVE	2	11	11	D	f	b475aca6188d68fd4ec7b5393e96a75add4fd7c530cfa093c1cf95a3680a0fd3	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
152	OFFICIAL	4	8	2024	32	O desmatamento médio nos últimos dez anos, entre 2013 e 2022, em km 2, aproximadamente, é de	OBJECTIVE	2	11	11	A	f	7f8e472ef798a29d5471c9b7673d4327835f78ec78ed300f2d77c361dd378579	t	DIFICIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
153	OFFICIAL	4	8	2024	33	No ano de 2022, o aumento percentual do desmatamento na Amazônia, em relação ao ano 2018, aproximadamente, foi de	OBJECTIVE	2	11	11	B	f	ca1f6327d758f98254751cf8b5473ccff3c641cbcb92b1068711628261555271	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
155	OFFICIAL	4	8	2024	35	Para montar uma das estruturas da cúpula da Amazônia, uma empresa sabe que se 6 de seus trabalhadores trabalharem 8 horas por dia, durante 5 dias, finalizam a estrutura. Precisando finalizar essa estrutura em 2 dias, se seus funcionários trabalharem 10 horas por dia, mantendo o mesmo ritmo de trabalho, será necessário	OBJECTIVE	2	12	12	D	f	203d964ccf6549195eff8e57922a108712f8d56f038acd35f9c277482101d358	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
179	OFFICIAL	5	10	2025	19	Em relação ao gênero textual, respectivamente, os Textos 1 e 2 configuram-se como	OBJECTIVE	1	7	7	C	f	4c2bf64034ab148bdda825d53b451521c3ca300b69f0bf23afb5df7cd534be23	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
156	OFFICIAL	4	8	2024	36	Em uma exposição de fotografias sobre o bioma amazônico, 300 pessoas estavam no salão da exposição quando a portaria percebeu que, a cada 2 min, 4 pessoas saíam e 2 entravam. Considerando esse fluxo de pessoas, o tempo para a exposição ficar vazia será de	OBJECTIVE	2	12	12	C	f	e63287244fab5ec60f4fb60eacdf42ec51caa51dd2a39419c7df61e6bade3906	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
157	OFFICIAL	4	8	2024	37	Um governo deseja veicular uma campanha de conscientização sobre a preservação do meio ambiente em uma emissora de televisão. Se a emissora cobra um valor fixo de R$ 10.000,00 e mais uma taxa de R$ 2.000,00 por cada transmissão, a função que melhor representa o custo total que o governo terá para veicular x propagandas é	OBJECTIVE	2	12	12	C	f	f773670d53b9285e7f01a9d838de7bde4a457e057cdf98b4e35e70bd8e7c477f	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
158	OFFICIAL	4	8	2024	38	Supondo que uma das sugestões tomadas na reunião da cúpula fosse a abertura de uma conta para financiamento das ações de proteção da Amazônia; sabendo que essa conta tem rendimento a juros simples de 1% ao mês e que foram depositados R$ 100.000.000,00 de reais, o montante disponível para uso após 8 meses é de	OBJECTIVE	2	12	12	A	f	ac48d1b3f6626f06f3917a2cb2092f32b793ab13cb3e5dd54bc6ccaaa107ef5e	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
161	OFFICIAL	5	10	2025	1	De acordo com o Texto 1,	OBJECTIVE	1	3	3	C	f	ebc0e94ce62ac3f515c4de38851d89008387dde90491930e2cd3832dcbebb83e	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
163	OFFICIAL	5	10	2025	3	A intenção comunicativa predominante no Texto 1 é	OBJECTIVE	1	4	4	A	f	6148dac36d28147834c40405d7b09a27aeb2296fabf2aa45723a69742f9bc0c6	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
164	OFFICIAL	5	10	2025	4	Quanto ao tipo textual, no último parágrafo do Texto 1, predomina a	OBJECTIVE	1	4	4	D	f	89f297ab66fa7820f4751c6d0cebc2d68f92699340679101124d86d9de9511e2	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
165	OFFICIAL	5	10	2025	5	No título de Texto 1, uso do elemento linguístico ATÉ	OBJECTIVE	1	4	4	B	f	8706047255f274323163fb85007ca127da3db6b8f95951920cf40b430599a8c2	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
166	OFFICIAL	5	10	2025	6	Quanto à sua organização, o trecho é formado por	OBJECTIVE	1	4	4	D	f	5d3ce8da63b23dec2880467c267b5390f140a2b0bf75df2c755294e16ca23b4c	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
168	OFFICIAL	5	10	2025	8	A expressão linguística A UTILIZAÇÃO DE CELULARES E OUTROS DISPOSITIVOS ELETRÔNICOS (2) assume, no trecho, a função de	OBJECTIVE	1	5	5	B	f	5f610b4674a4953b3ad2359eaae63925ac1aeda45539d9d25275636fdccba637	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
169	OFFICIAL	5	10	2025	9	O elemento linguístico EXPLANAÇÃO (4) pode, sem prejuízo para o sentido do trecho, ser substituído por	OBJECTIVE	1	5	5	B	f	67fc23f7ab38840974d4942f662cb49feb6e654492487c7903c619c73593108e	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
170	OFFICIAL	5	10	2025	10	No primeiro período do trecho, o elemento linguístico DEVERÁ (1) exprime sentido de	OBJECTIVE	1	5	5	C	f	8108a6eb0cccb7933f3aaab80ceb889efd612892f0baf366d409f90fd38bec32	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
171	OFFICIAL	5	10	2025	11	A expressão A GENTE (2) tem como referente	OBJECTIVE	1	5	5	D	f	0271cc73c2cda2d439d2b64a405d278770bbd8f680bc1e40ed2495f1d657eb4e	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
173	OFFICIAL	5	10	2025	13	Assinale a opção que apresenta a informação implícita presente no trecho.	OBJECTIVE	1	6	6	D	f	6997eedd0b4c99ce1175a62c1ebcf74b1d9721f0844ad8d9f8297afab09a6091	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
174	OFFICIAL	5	10	2025	14	Sobre a organização do trecho, é correto afirmar que	OBJECTIVE	1	6	6	A	f	c8a238fc14034124532929dd508ee00cffa2270b4e9f669c27e80b082db9bc75	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
175	OFFICIAL	5	10	2025	15	No Texto 2, os sentidos se estabelecem, primordialmente, a partir de	OBJECTIVE	1	6	6	A	f	03dabb835a7fd6f92fa08bd6d4b1a7ad38974dc58d938a505d17931b79d1bfc0	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
176	OFFICIAL	5	10	2025	16	A leitura dos elementos verbais e não verbais permite afirmar que o Texto 2	OBJECTIVE	1	6	6	D	f	847f7be8444cc33b96a3e72cb89208f01d55c5fe5383d9b2e267df6b729ebec3	t	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
177	OFFICIAL	5	10	2025	17	O elemento linguístico SEGURO-DESEMPREGO, presente no Texto 2, é formado pelo processo de	OBJECTIVE	1	6	6	C	f	d9537ab64f49508bac0840624dde8f39b6aad5ea80c648d4ce8a284e7f7d3fba	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
181	OFFICIAL	5	10	2025	21	Segundo o Texto 1, de cada 10 pessoas 8 são a favor de proibir o uso de celulares em escolas do Rio. Suponha que essa proporção foi obtida a partir de uma pesquisa realizada com 2450 pessoas. Nessa pesquisa, o número de pessoas que se manifestaram contra a proibição foi de	OBJECTIVE	2	8	8	D	f	5304c6d9abf802ba3d99aa7b16b6cdd97f06021c3a3f67a8e0edbeb58cfb3a39	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
182	OFFICIAL	5	10	2025	22	Considere que, durante uma revisão de conteúdo para as provas, um estudante participou de um simulado online com 25 questões. Ele chutou 15 das questões, acertando 6 delas. A razão entre o número de acertos das questões que ele chutou e o total de chutes dados pelo estudante é	OBJECTIVE	2	8	8	C	f	64c1a21c7d77546f3e1427bc311c8a6803b3f608e998c7fb9629b0dcf42e1b54	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
183	OFFICIAL	5	10	2025	23	Uma escola está avaliando o impacto de uma nova política que proíbe o uso de celulares durante as aulas. A administração entrevistou 150 alunos para saber a opinião deles sobre essa proibição. 60 dos alunos entrevistados disseram que apoiam a proibição, 45 disseram que são indiferentes e o restante se opõe à proibição. Se um aluno é escolhido aleatoriamente entre os entrevistados, a probabilidade de que ele seja um aluno que declarou que se opõe à proibição é	OBJECTIVE	2	8	8	B	f	113285f919f5ad4714675a480d9eb391a35915cee2c82f670550bbefdc161ddb	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
184	OFFICIAL	5	10	2025	24	Suponha que em uma determinada escola foi implementada uma nova política que proíbe o uso de telas (celulares, tablets etc.) durante as aulas. De acordo com o regulamento, se um aluno for pego usando qualquer dispositivo de tela durante o horário de aula, ele será punido com uma multa inicial de R$ 50,00. Além disso, a cada dia em que o dispositivo for usado após o primeiro incidente, o aluno receberá o valor da multa imediatamente anterior acrescida de 10%. Se um aluno for pego usando uma tela em três ocasiões diferentes durante o mês, o valor total da multa que ele deverá pagar ao final do mês é	OBJECTIVE	2	8	8	C	f	fae31946a34c1e99a3182420ab5adfcc4803d636a369d3ca76c96a8cee306cb6	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
186	OFFICIAL	5	10	2025	26	Considere o gráfico de barras abaixo. Ele mostra o número de horas semanais que sete grupos de alunos passam usando telas para atividades acadêmicas. O gráfico representa a seguinte distribuição de horas de uso de telas: A média aritmética de horas semanais de uso de telas para esses sete grupos de alunos é	OBJECTIVE	2	9	9	A	f	fa5068d18ffc2f9f07d69f6d696649b09f18897a156d92d61b8b9f8e1ab6ffb2	t	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
188	OFFICIAL	5	10	2025	28	Durante uma semana (7 dias), um monitoramento revelou que um estudante passou 14 horas e 30 minutos em frente ao celular e ao computador. Preocupado com o tempo excessivo, o estudante decidiu estimar quanto tempo ele passaria em frente a essas telas durante um período de três meses (90 dias). Utilizando a mesma proporção, o tempo aproximado que o estudante passaria em frente ao celular e ao computador em três meses é	OBJECTIVE	2	10	10	B	f	acf14c9d21c78ead6950df34904f9816e09de609c9eb619af9f4f7ec2dbf34ad	f	DIFICIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
189	OFFICIAL	5	10	2025	29	Um estudante está organizando seu tempo diário disponível para atividades diversas e decide dedicar 1/4 do seu tempo total para usar o celular para fins acadêmicos, 1/5 para assistir a aulas online e 1/8 para fazer tarefas escolares. Após essas atividades, ele tem 4 horas e 15 minutos restantes para descanso e outras atividades pessoais. Por dia, o tempo total do estudante disponível para as atividades descritas é	OBJECTIVE	2	10	10	A	f	4f0d2f828bc58e96eed81100e3e0ae3db5189391eed558800e00dc96d8d8e883	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
190	OFFICIAL	5	10	2025	30	Um grupo de pesquisadores do Rio de Janeiro está analisando o impacto do tamanho das telas usadas pelos estudantes em seu desempenho acadêmico. Eles estudam uma tela retangular com uma área de 75 cm². Sabe-se que o lado maior da tela é o triplo do comprimento do lado menor. A soma dos comprimentos dos lados dessa tela é	OBJECTIVE	2	10	10	C	f	3c44d7d967396f58191d89f21abf6636b9ec9d14e46b2a5756d28d7783c77b32	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
191	OFFICIAL	5	10	2025	31	Em uma instituição de ensino do Rio de Janeiro, há um total de 780 alunos. 135 desses alunos são do ensino fundamental, 345 são do ensino médio e os demais são do ensino superior. Sabendo que 17% dos alunos do ensino superior são favoráveis à proibição do uso de celulares, a quantidade de alunos do ensino superior não favoráveis à proibição é de	OBJECTIVE	2	10	10	C	f	165ac7363d96b613b1bb817f279cda16df3118e9c27fdc1b0fbb9ad41b746288	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
215	OFFICIAL	6	12	2026	15	Sobre a acentuação de palavras no trecho, é correto afirmar que	OBJECTIVE	1	7	7	D	f	73cb41b9d375a51cf28711513ff883263eb80a64af8dae2b3f52e6d29f251349	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
193	OFFICIAL	5	10	2025	33	Uma escola decidiu proibir o porte de celulares durante as aulas. Em uma inspeção, verificou-se que, em uma turma com 28 alunos, 13 deles trouxeram seus celulares para a sala de aula. Ao escolher aleatoriamente um aluno dessa turma, a probabilidade de que ele tenha trazido seu celular é de 15	OBJECTIVE	2	11	11	D	f	65fa0527ded5872610dfec3271dcbf8c105913e25feef08a59d47c979d772cb2	t	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
194	OFFICIAL	5	10	2025	34	Em uma escola da capital fluminense, uma pesquisa revelou que a proporção de alunos que utilizam celulares em sala de aula para os que não utilizam é de 9 para 4. Sabendo que há 234 alunos na escola, a quantidade de alunos que não utilizam celulares em sala de aula é de	OBJECTIVE	2	11	11	A	f	7edad2854d72801fc9a6b9f24c4dbdb35c2d79df341e2bbae12f17066cb28fee	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
195	OFFICIAL	5	10	2025	35	Em 2016, 67 alunos foram flagrados usando o celular indevidamente. Em 2024, esse número aumentou para 83. Se o aumento mantiver essa proporção, a quantidade de alunos que serão flagrados em 2034 será de	OBJECTIVE	2	11	11	C	f	2adcab9a14fb4c2d0d5d49cdace5065982f611ec0396a667aceb2441bcb10393	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
197	OFFICIAL	5	10	2025	37	Uma aluna está interessada em adquirir um celular recém-lançado, que custa R$ 1500,00 à vista. Para facilitar o pagamento, ela optou por parcelar a compra em 12 meses com uma taxa de juros simples, aplicada sobre todo valor do bem, de 2% ao mês. O valor total que ela pagará ao final de um ano será de	OBJECTIVE	2	12	12	B	f	2e314d3ee40ae550bc925402fe6ec016a1a8bb01aa8f7f73a64f0b4439e6a1b2	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
198	OFFICIAL	5	10	2025	38	Uma prefeitura deseja lançar uma campanha, a ser veiculada tanto em redes sociais quanto em emissoras de TV, para promover os benefícios da proibição do uso de celulares nas salas de aula. O custo para veicular a campanha nas redes sociais inclui uma taxa de R$ 300,00 por cada postagem, enquanto nas emissoras de TV o custo é composto por uma taxa fixa de R$ 250,00, além de R$ 360,00 por cada transmissão. A função que representa o investimento total da prefeitura, considerando a veiculação de 𝑥 postagens e 𝑦 transmissões, pode ser expressa na forma:	OBJECTIVE	2	12	12	A	f	90a93810414cbb17c76049883b6a485dd94aa49d0ced769f6f4a1cc193d5c7af	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
199	OFFICIAL	5	10	2025	39	O tamanho de um celular, geralmente, é medido em polegadas, correspondendo à medida da diagonal da tela do aparelho. Considerando que uma polegada equivale a 2,5 cm, o tamanho aproximado, em polegadas, de um celular com dimensões de 150 mm por 75 mm é de	OBJECTIVE	2	12	12	B	f	739fc26fb19be8e4a6d57134b1b4ee5428ae0956bc5ffb4e79308f14fbe473f6	f	DIFICIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
201	OFFICIAL	6	12	2026	1	A leitura do Texto 1 permite afirmar que a Conferência das Partes (COP)	OBJECTIVE	1	4	4	A	f	9222852dee35ca3a8ef50e9ab1ba364bb14c118c7f1a3c4209661c69b09be9f2	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
203	OFFICIAL	6	12	2026	3	No Texto 1, a intenção comunicativa predominante é	OBJECTIVE	1	5	5	A	f	002298798030802ae5538bfc7a3236894303f37b1ebfac275fcd98747e08dcc9	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
204	OFFICIAL	6	12	2026	4	De acordo com o Texto 1, a realização da COP30 no Brasil	OBJECTIVE	1	5	5	C	f	badadf675a737cb779ace46a6d71a99b3aed240a033b89f6b6ce6e1f97ff96a1	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
205	OFFICIAL	6	12	2026	5	O Texto 1 organiza-se, predominantemente, a partir do tipo textual	OBJECTIVE	1	5	5	D	f	3ae4813b1dbb2fa436efd7c7523ea608bebc2f505b7fa9c598efe1c60723a072	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
206	OFFICIAL	6	12	2026	6	A partir da leitura do Texto 1, é correto inferir que	OBJECTIVE	1	5	5	A	f	dd99e70977196b78b8745995f89490a34d4ac54ba82b632bef91a834f1e7530e	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
208	OFFICIAL	6	12	2026	8	Do ponto de vista de sua organização sintática, é correto afirmar que	OBJECTIVE	1	6	6	A	f	8e28c03345271e68dc5769220d597650d9ef6428178f8d00168d07eb7bfd7795	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
209	OFFICIAL	6	12	2026	9	As expressões QUE (1) e ONDE (2) referem-se à	OBJECTIVE	1	6	6	B	f	5daa34fd90e2ec336adcba2552ec84307ed1e782906f1eec64c073bcb2eea823	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
210	OFFICIAL	6	12	2026	10	A expressão AS NEGOCIAÇÕES POLÍTICAS E OS ENCONTROS DIPLOMÁTICOS (3), sintaticamente, assume a função de	OBJECTIVE	1	6	6	D	f	c503ace74364af291d658ef816de4d3300e7d1c5eacab267735abdc237b7ebaf	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
211	OFFICIAL	6	12	2026	11	Sobre o uso do elemento linguístico APESAR DE (1) no primeiro período do trecho, é correto afirmar que	OBJECTIVE	1	6	6	B	f	bc41f7896c785ca51fe379b37758e116517152ab4f019b7b95f6ec223809b4e1	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
213	OFFICIAL	6	12	2026	13	Os elementos linguísticos NO ENTANTO (3) e MAS TAMBÉM (5) assumem, respectivamente, o valor de	OBJECTIVE	1	7	7	B	f	b742c9f1f47a83dbcb7c2201a423a47cd7b212b63dc9854a7687a659daa7814c	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
214	OFFICIAL	6	12	2026	14	O uso das aspas, indicadas por (4), é justificado porque	OBJECTIVE	1	7	7	A	f	bf30880e0a82e41dcc1d49bc5d7d289765ba6e62fa725f411ae4bfb66d15ab56	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
216	OFFICIAL	6	12	2026	16	O uso da vírgula assinalada com 2 justifica-se por	OBJECTIVE	1	7	7	B	f	a3f551f55565a2a8402864c6d440b8c67e92fca3094896bd5830afb271f90376	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
217	OFFICIAL	6	12	2026	17	A expressão CRESCIMENTO VERDE (7), considerando sua inserção no Texto 1, significa um modelo de desenvolvimento que	OBJECTIVE	1	7	7	D	f	c7c5d7a869e7903b724e590daf994db068d43ab2cf2680c73c11afd670e4d80e	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
219	OFFICIAL	6	12	2026	19	Quanto ao gênero textual dos Textos 1 e 2, é correto afirmar:	OBJECTIVE	1	8	8	B	f	e79689321ccc7681e4c7e60953af10e2048582f991ece325fda3f56caa290407	t	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
220	OFFICIAL	6	12	2026	20	Os Textos 1 e 2 apresentam em comum	OBJECTIVE	1	8	8	A	f	80d416cdadf94ff8667ee525fc50e16226487cea03514a74b813dee159a6b027	t	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
221	OFFICIAL	6	12	2026	21	A Conferência das Nações Unidas sobre Mudança do Clima (Conferência das Partes – COP30 –) terá sua 30ª edição em 2025, no Brasil. Em algarismos romanos, os números 30 e 2025 são representados, respectivamente, por	OBJECTIVE	2	8	8	D	f	f5e21b01d3ee6c7d38a9bdb61fc6206dfe41e36d0676cf7257587ce3a3b736bf	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
222	OFFICIAL	6	12	2026	22	Suponha que, para a realização da COP30, em Belém, foram reservados, inicialmente, 4.900 lugares em auditórios. Durante a preparação, a organização adicionou mais 350 novos lugares e, em seguida, precisou interditar 150 lugares por questões de segurança. Se cada auditório após essas alterações descritas comporta 50 lugares, o número de auditórios que serão necessários para acomodar todos os lugares disponíveis é	OBJECTIVE	2	8	8	B	f	799e454a1a53ca1e64d73c15219dd3c0b9130598fce2a2f139713c26958a6f4a	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
224	OFFICIAL	6	12	2026	24	Se, em uma simulação menor do evento, fossem esperados cerca 30 mil visitantes no total, mantendo-se a mesma proporção de integrantes da ONU e de delegações de países-membros, o número de integrantes da ONU e de delegações de países-membros seria cerca de	OBJECTIVE	2	9	9	A	f	32d791a85eef5fc821a95266fde2ac2b297f3d269cc7b04b8a00680c3d52a1d3	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
225	OFFICIAL	6	12	2026	25	Considere que cada um dos 40 mil visitantes da COP30 consumirá, em média, 2 litros de água por dia, que será distribuída em garrafas de 500 mL, organizadas em grades contendo 12 garrafas cada. Durante os 12 dias de evento, o número total de grades necessárias para garantir a hidratação de todos os participantes será	OBJECTIVE	2	9	9	B	f	0f20ab35ee7cc38b75a51e73a8250de9f83f02a2e06a6b1fb206d85c6c4ab326	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
226	OFFICIAL	6	12	2026	26	Considere que um BRT (Bus Rapid Transit) Metropolitano sai do terminal a cada 20 minutos, sempre com lotação máxima de 200 passageiros. Supondo que todos os 40 mil visitantes precisassem usar apenas esse BRT para se deslocar e que o serviço operasse de forma ininterrupta, o tempo necessário para que todos eles fossem transportados seria	OBJECTIVE	2	9	9	D	f	98e5c1fa543d4a1cba6b4093d0d2e761f52dfb71cd4535c48cd50db7a7618bcd	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
227	OFFICIAL	6	12	2026	27	Uma empresa de turismo organizou uma excursão para os participantes da COP30 e alugou 2 ônibus, conforme indicado a seguir. ● O primeiro ônibus levou 36 passageiros, cada um pagando R$ 75,00. ● O segundo ônibus levou 48 passageiros, cada um pagando R$ 60,00. O motorista do primeiro ônibus recebeu 20% do valor arrecadado dos seus passageiros. Já o motorista do segundo ônibus recebeu 25% do valor arrecadado dos seus passageiros. Sabendo que o restante do dinheiro foi entregue ao coordenador da excursão, o valor total recebido pelo coordenador foi	OBJECTIVE	2	9	9	C	f	51c1cd0b791c861894364a72f1ac684a1c28d601ce31547d64cbe30e5599cb08	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
229	OFFICIAL	6	12	2026	29	Durante a preparação para a COP30, em Belém (2025), uma empresa contraiu um empréstimo de R$ 80.000,00 a juros simples, com taxa de 1,5% ao mês, pelo prazo de 10 meses. Sabendo que o empréstimo será quitado integralmente ao final do prazo firmado, o montante a ser pago, ao término dos 10 meses, será	OBJECTIVE	2	10	10	D	f	09fba31481370cf7a9076a45401439b582384edaa87483e5be6ad834f9a2ddb1	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
230	OFFICIAL	6	12	2026	30	Suponha que, em um projeto de um pavilhão sustentável para a COP30, a estrutura é representada pelo triângulo retângulo ABC, conforme ilustrado na figura 1. FIGURA 1: projeto do pavilhão sustentável da COP30 Fonte: FUNCERN, 2025. Na figura 1, o ângulo em A é reto, o ângulo em B mede 30º e a cobertura inclinada BC mede 5 m. Um painel ecológico retangular (ADEF) foi instalado, com a parte superior da parede, CD, medindo 1,6 m. Nesses termos, as medidas, em centímetros, dos segmentos AD (altura do painel) e BE (base inclinada da cobertura remanescente), respectivamente, são	OBJECTIVE	2	10	10	B	f	ae63b6e1918f5d6b0abf32c832b7efaf996b840c0fa1dd048b2a31bd37ef5913	t	DIFICIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
232	OFFICIAL	6	12	2026	32	Na COP30, que será realizada na cidade de Belém do Pará, em 2025, cinco países apresentaram as seguintes metas de redução de emissões de CO₂ (dióxido de carbono) até 2030: 42%, 37%, 48%, 35% e 38%. Posteriormente, outro país apresentou sua meta e, com isso, a média das metas dos seis países passou a ser 41,5%. Com base nessas informações, a meta apresentada pelo 6º país foi	OBJECTIVE	2	11	11	C	f	452d56be9a9bc8be102b7e057a46fe08715f9a9f86bc4215a76b964bda55af35	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
233	OFFICIAL	6	12	2026	33	Imagine que, para preparar Belém para a COP30, o governo destinou X bilhões de reais em recursos extras além dos investimentos já anunciados. Suponha que foram apresentados os seguintes gastos: ● 1/3 desse valor X foi gasto na modernização do aeroporto internacional; ● 1 bilhão foi utilizado em saneamento; ● 4/5 do que ainda restava foi gasto em mobilidade urbana; ● sobraram 0,6 bilhão de reais, ao final, para pequenos ajustes na cidade. Com base nessas informações, o valor de X, em bilhões de reais, foi	OBJECTIVE	2	11	11	A	f	02b44e7251a2485392b26c988c71af482b7a3a53b5817fb75bac4c8ffdff6063	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
234	OFFICIAL	6	12	2026	34	Um dos problemas da cidade de Belém para sediar a COP30 é a hospedagem dos visitantes. Estima-se que 40 mil pessoas precisem de acomodações, mas a rede hoteleira conta, em média, com 47 leitos por hotel. Em virtude disso, o número mínimo de hotéis que seriam necessários para atender a todos os visitantes do evento é	OBJECTIVE	2	11	11	C	f	497b45e4f29cbf3ed921fd45350dccb0aa47294ad0db558df5db22858180f1aa	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
235	OFFICIAL	6	12	2026	35	Na preparação para a COP30, o governo precisou aumentar a quantidade de leitos disponíveis. Se a oferta inicial são de 20 mil leitos e, a cada mês de obras de expansão, forem acrescentados 500 novos leitos, uma relação que representa a quantidade de leitos em função do tempo de obras, em meses, x, é	OBJECTIVE	2	11	11	D	f	bda3a6a1a2858c92d21a78c591c3aa7bc1a219b935f2448c97310a100bb66f21	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
237	OFFICIAL	6	12	2026	37	Durante as obras para a COP30, uma das entregas de materiais precisa ser organizada em pacotes. Um tipo de material vem em lotes de 36 unidades, e outro, em lotes de 48 unidades. Os pacotes devem ser montados de modo que cada um tenha a mesma quantidade de cada material, sem que sobre nada. Sendo assim, a maior quantidade de pacotes que podem ser formados será	OBJECTIVE	2	12	12	X	t	dea6f3eff948aa9376bbc43c40d2933b121912d5c8ce2b872650a0c491000ffc	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
238	OFFICIAL	6	12	2026	38	Considere que, para a instalação de painéis solares durante a COP30, será utilizado um espaço retangular de dimensões x (largura) e y (comprimento). No projeto de adaptação, a largura será reduzida em 3 metros e o comprimento será ampliado em 5 metros. A expressão que representa a nova área desse espaço, em função de x e y, é	OBJECTIVE	2	12	12	A	f	0c6eb026f56fb76d7116f8a191b74cde731d38d8c404bc002b22370cd3dca1fe	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
239	OFFICIAL	6	12	2026	39	Sabendo que um dos auditórios da Zona Verde da COP30 terá o formato de um triângulo isósceles, com os lados de mesma medida, 𝑙, em que um dos ângulos mede 120° e que se deseja estender um tapete de comprimento igual à altura relativa ao vértice que contém o ângulo de 120°, a medida da altura, em função do lado 𝑙, é 𝑙	OBJECTIVE	2	12	12	C	f	c2eb4289144927a8b8547330ed0667755eeeb2e6ff5508b3fea3acc7c3ebae9e	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-01 00:57:58.949678+00
3	OFFICIAL	1	2	2020	3	De acordo com o Texto 1,	OBJECTIVE	1	3	4	B	f	9f0bb9cf205bdeff4eaa03caa50da2c792c7589d65a9da1d6d75ab17d8d07ef7	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
8	OFFICIAL	1	2	2020	8	De acordo com o Texto 1,	OBJECTIVE	1	4	5	C	f	dbb89036c861bf6c9130d7f24dc70520027adc1e616cdba910441a579c2ae4f2	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
13	OFFICIAL	1	2	2020	13	Os elementos linguísticos pesquisadora [4] e Estadual [5], respectivamente, assumem valor de	OBJECTIVE	1	5	6	D	f	1f280679412c01677a990da6a402af4cb8d19283e4bf95997fc842c593e14a4f	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
19	OFFICIAL	1	2	2020	19	No Texto 2, há o predomínio da	OBJECTIVE	1	6	7	A	f	882ec1a42e24e77474ce508835720676c16987e65d831c7c15a2427b66aaf12d	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
20	OFFICIAL	1	2	2020	20	Os Textos 1 e 2, respectivamente, são	OBJECTIVE	1	7	8	D	f	fe5ef61b4b3a763093aa82073de8e391d53df16d5b5adc2b89ae251b81235188	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
24	OFFICIAL	1	2	2020	24	Ao organizar uma campanha de conscientização contra o cyberbullying, um grupo de alunos resolve divulgar os males dessa prática. Dispondo de um papel para cartão com 88 cm de comprimento por 104 cm de largura, eles pretendem produzir cartões quadrados com a maior dimensão possível e de tal forma que não sobrem pedaços do papel para cartão. Assim, o lado dos cartões recortados deverá medir	OBJECTIVE	2	8	9	A	f	254bb481c5f98d2f745defee38f07635a1075db1da2cfc0bdc548ddcb35ba9b4	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
28	OFFICIAL	1	2	2020	28	Em uma campanha contra o cyberbullying, uma escola promoveu uma palestra para pais e alunos. Querendo estimar o público presente, o professor de Matemática observou que o pátio, que tinha forma retangular com 50 m de comprimento por 80 m de largura, estava lotado. Sabendo que, em média, o pátio comporta 4 pessoas por metro quadrado, ele conclui que estavam presentes	OBJECTIVE	2	9	10	C	f	1a3ffe8645017de96f6d7016c97034a5f0a68b66b7d8159a28eebd7fa5a9207c	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
100	OFFICIAL	3	6	2023	20	Os Textos 1 e 2 apresentam em comum	OBJECTIVE	1	7	8	C	f	53d7facca4b3efb01c3446a2fbebc6c81cfc33752eeef10f94b1de03ca4401bf	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
32	OFFICIAL	1	2	2020	32	A jovem Laura, mencionada no Texto 1, passou a ter acompanhamento psicológico após as agressões sofridas na Internet. Considere que, para pagar os custos da terapia, a mãe e o pai da adolescente pagaram juntos um total de 300,00 reais mensais. Supondo que a razão entre a contribuição do pai e a 4 contribuição da mãe é , o valor a mais, em reais, que a mãe de Laura paga, mensalmente, em relação 11 ao pai, é de	OBJECTIVE	2	10	11	D	f	b6206f49913abe6b53207077cde6a99c7f4e8423c98e0bb7483025b72e3da249	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
34	OFFICIAL	1	2	2020	34	Na tentativa de superar os traumas das agressões sofridas em ataques de cyberbullying, uma adolescente residente em Natal mudou-se para a casa dos avós no Piauí. Curiosa para saber a distância real entre as duas cidades, ela utilizou o mapa da Figura 1, cuja distância, em linha reta, entre as duas capitais mede 6 cm e, a escala adotada é 1:14.200.000. Com base nesses valores, em km, a distância real entre Natal e Piauí é de	OBJECTIVE	2	11	12	A	f	9240ec639578e840f503caafade836ad02be79d3ca7367993c563aa3a12ce50e	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
40	OFFICIAL	1	2	2020	40	Laura começou um tratamento médico após ter sofrido sérias agressões nas redes sociais. Na prescrição médica, foram receitados dois medicamentos: um deles deve ser ingerido de 8 em 8 horas e o outro, de 10 em 10 horas. Supondo que Laura ingeriu os dois medicamentos às 14 horas do domingo, ela irá ingerir novamente os dois medicamentos juntos	OBJECTIVE	2	13	14	C	f	005d748d1c46b4cd6369b17a62a7ee675715a2835d60b514213057f5f0962179	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
46	OFFICIAL	2	4	2022	6	No trecho, o uso do vocábulo ATÉ	OBJECTIVE	1	4	5	C	f	acccfe6bfa86c6ea966b1b42258aaef184936b35b3f20b9d656c823e435ec635	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
51	OFFICIAL	2	4	2022	11	O termo destacado DO TAPAJÓS é um	OBJECTIVE	1	5	6	C	f	a391c885fde0db5a73a6900a4e00754fe4238e71f82fdbc1880a80597199dca1	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
56	OFFICIAL	2	4	2022	16	A linguagem utilizada pelo Juiz no Texto 3	OBJECTIVE	1	6	7	A	f	3dc5d0a703c00f439f992eef7a5677123707f9be9cd8cc170f0a42cd8f2a6cb3	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
60	OFFICIAL	2	4	2022	20	É comum aos três textos desta avaliação	OBJECTIVE	1	7	8	B	f	dd1ce39d7071ff5dd23281c56a89bd6269ff9bfa37c427269e05fdd01583efea	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
64	OFFICIAL	2	4	2022	24	Segundo o texto 1, o Ibama aplicou R$ 10 milhões em multas contra acampamentos ilegais localizados na terra indígena Alto Rio Guamá. Supondo que os invasores não pagaram as multas e que, sobre elas, sejam cobrados juros mensais, no regime de juros simples, de 2% ao mês, o montante da dívida a ser paga, ao final de 2 anos, será de	OBJECTIVE	2	8	9	A	f	d3e07f15cb7cfae02b23278b961a5b4415a6bde2b6dc3bafd7fbb5d5f8f7c036	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
69	OFFICIAL	2	4	2022	29	Um orgão ambientalista observou que um grupo de garimpeiros ilegais estava desmatando o equivalente a 300 campos de futebol em 30 dias e que, após uma campanha de fiscalização, o grupo de garimpeiros se reduziu a dois terços do número original. Admitindo que os garimpeiros restantes, após a campanha de fiscalização, mantiveram o mesmo ritmo de desmatamento de antes, o tempo, em dias, que eles levarão para desmatar o equivalente a 300 campos de futebol será	OBJECTIVE	2	9	10	C	f	8ce9cdeb8d8330594a889c8f67e486263e83c8ac7f75bd0e94a44e2a6b15b8e5	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
73	OFFICIAL	2	4	2022	33	No Texto 1, cerca de dois mil índios vivem na terra indígena Alto Rio Guamá. A representação desse mesmo quantitativo em notação científica é dada por	OBJECTIVE	2	10	11	B	f	9fc6c2c908195e3c4521bf412804d9d92abbab59c2322b2e2f8d577aa4867a75	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
76	OFFICIAL	2	4	2022	36	De acordo com o Texto 1, no ano de 2017, houve 96 invasões em terras indígenas, em 2018, o número subiu para 111 e, em 2019, foi para 160, somente no mês de janeiro. Supondo que tivesse sido preservado o mesmo percentual de aumento do ano de 2018, o número aproximado de invasões, para todo o ano de 2019, teria sido igual a	OBJECTIVE	2	11	12	C	f	7fa3342fb1ade5f779e0c7eb3d4ade9e16aafae5f607e6a851c888d1b8ff0f03	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
79	OFFICIAL	2	4	2022	39	Para medir a largura ̅̅̅̅ 𝐷𝐸 de um lago, dentro de um garimpo desativado, foi utilizado o esquema da Figura 4. Observando-se que o ∆𝐴𝐵𝐶~ ∆𝐸𝐷𝐶 , a expressão que determina o valor de ̅̅̅̅ 𝐷𝐸 é dada por ̅̅̅̅ .𝐸𝐶 𝐴𝐶 ̅̅̅̅	OBJECTIVE	2	12	13	C	f	d965636881786618538e9f57a5c92c995263b4d0333b12037b0c30845745b6be	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
80	OFFICIAL	2	4	2022	40	O Texto 1 registra os números de invasões em terras indígenas nos anos de 2017, 2018 e no período de janeiro a setembro de 2019. O valor total de invasões registradas nos três anos tem a sua representação no sistema binário dada por	OBJECTIVE	2	13	14	B	f	6e934646d4d683ce75de7bdcd61116e0ba36ba51a9157e59ef00e32bcd7383b9	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
83	OFFICIAL	3	6	2023	3	De acordo com o Texto 1, a Lei das Cotas	OBJECTIVE	1	3	4	D	f	835c8155fd2f98ba28de20704f20292a81fd281de0789d6202f20bad999cf4ad	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
87	OFFICIAL	3	6	2023	7	Observando a norma-padrão da língua portuguesa, o primeiro período está corretamente reescrito e mantém seu sentido original na opção	OBJECTIVE	1	4	5	C	f	a24947e86c60c167e51c28929299b647a768854a06f946a6258b7fdd7e92ff6b	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
92	OFFICIAL	3	6	2023	12	O vocábulo ESCÁRNIO (4) é acentuado pelo mesmo motivo de	OBJECTIVE	1	5	6	C	f	2dc2b324117c62ace5ff31522b8ee589aa24ccb769fd132890989eb804d0e589	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
97	OFFICIAL	3	6	2023	17	Sobre o Texto 2, é correto afirmar que	OBJECTIVE	1	6	7	A	f	24a8ecb20fc7a562d2ec0b14d043e47e6bb92f5e3e46923c33a5565e8521fe8d	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
104	OFFICIAL	3	6	2023	24	A área, em cm2, correspondente a cada um desses hexágonos (Figura 1) é de	OBJECTIVE	2	8	9	A	f	68ec41ddd743fb494145bc333bd4b5c915ab1b9c67be6dfa9b54bbd69fc37914	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
107	OFFICIAL	3	6	2023	27	Após a aprovação de alunos que obtiveram acesso ao ensino superior por meio de reserva de vagas para pessoas com deficiência, uma instituição de ensino realiza uma previsão orçamentária para a contratação dos profissionais que atuarão no apoio a esses estudantes. O custo total é descrito em 𝒚 reais, e equivale a um valor inicial de contratação de R$ 2500,00, acrescido de R$ 830,00 mensais, referentes à remuneração dos profissionais durante 𝒙 meses, estabelecidos de acordo com a necessidade e nível dos estudantes. A lei de formação, que representa o custo total de 𝒚 reais para pagamento dos profissionais, é dada por	OBJECTIVE	2	9	10	A	f	dcc5d3b087578bca2d3a15e53fa2fb08593eeab122654f1cc36cfd4ee29d34a6	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
110	OFFICIAL	3	6	2023	30	Conforme informações do Índice Folha de Equilíbrio Racial (IFER), para que seja atingido um equilíbrio entre brancos e negros (pretos ou pardos) com ensino superior completo, é preciso que, dentre cada 1500 habitantes de 30 anos ou mais que têm o curso superior completo, 645 sejam pretos ou pardos. Considerando uma população de 23 mil habitantes com ensino superior completo e faixa etária de 30 anos ou mais, o número necessário de indivíduos pretos ou pardos nessa população para que essa proporção seja atingida é	OBJECTIVE	2	10	11	A	f	e455b83ce187878eacf4a3cc9955de1da5076182ec2cc3090a9e336c48821403	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
115	OFFICIAL	3	6	2023	35	Um grupo resolveu fazer um evento com apresentações culturais em apoio à continuidade da Lei das Cotas. Esse evento durou 48 horas ininterruptas e, a cada 20 minutos, uma apresentação cultural diferente subia ao palco. O número de apresentações distintas ocorridas no evento foi	OBJECTIVE	2	11	12	C	f	8211c80f502a84ffe4cecbc3e8f781898a9e6974a9f1ce4a4a4561771f4ac28b	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
120	OFFICIAL	3	6	2023	40	Uma universidade subdividiu os 50% de vagas destinadas às cotas raciais em vagas para candidatos que se enquadrem no critério racial e sejam de baixa renda, vagas para candidatos que se enquadrem no critério racial e fizeram todo o ensino médio em escola pública e vagas em que apenas o critério racial é exigido. Para avaliar se essa subdivisão estava tendo efeitos positivos na inserção social, um pesquisador estudou, a cada dois anos, as vagas de baixa renda; a cada três anos, as vagas de escola pública; e, a cada quatro anos, as vagas que exigiam apenas o critério racial. Se, em 2020, ele estudou as três categorias, após 2020, um ano em que ele estudará as três categorias juntas novamente será	OBJECTIVE	2	12	13	A	f	5cf6c089b94a1a8c199cf772447c1d4690c134ae50352697088ec5c78cabc2ef	f	DIFICIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
123	OFFICIAL	4	8	2024	3	No parágrafo 3 do Texto 1, o uso dos parênteses se justifica porque assume a função de	OBJECTIVE	1	4	5	C	f	3deea9f940ccafcdc7017daaf5d48ff062327a092eaaa726d6167b8a526a6b9f	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
127	OFFICIAL	4	8	2024	7	Na sua organização sintática, o trecho apresenta	OBJECTIVE	1	5	6	C	f	ed43f947746aa7dfddd9fdd290837efc64d878c74b45ba02d919bdd3a42079df	f	DIFICIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
133	OFFICIAL	4	8	2024	13	O elemento linguístico EVENTO (5) refere-se	OBJECTIVE	1	6	7	A	f	f36d6a49750e183d2209e3c90eceb9900fb41ec8da29cbed85ff21cac7e30fdf	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
138	OFFICIAL	4	8	2024	18	Segundo o Texto 3, o desmatamento da Amazônia foi	OBJECTIVE	1	7	8	B	f	aa2980a43ccd25da812c2bbd1b4187290f83a64810c3bf3a0bdca1675eb5aef1	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
140	OFFICIAL	4	8	2024	20	Sobre os textos 1, 2 e 3, é correto afirmar que o	OBJECTIVE	1	8	9	C	f	f8fde32528919d22a24632b7faaf13e17d7c5c3a9324db5876c8b152732244ec	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
149	OFFICIAL	4	8	2024	29	O Ministério do Meio Ambiente e Mudança do Clima enviou uma comissão de 19 pessoas, entre homens e mulheres, para o evento a Cúpula da Amazônia. O dobro do número de homens excede em 5 o número de mulheres. Dessa forma, a comissão foi composta por	OBJECTIVE	2	10	11	D	f	e9f2ce48abf64332adcbbce7e279d904ca6e5e208d1b09dc46586f08d3515ff4	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
154	OFFICIAL	4	8	2024	34	Suponha que, para cumprir com a meta de desmatamento zero em 2030, o governo brasileiro aumente o quantitativo de servidores que trabalham na fiscalização em 20 pessoas por ano, a partir de 2025 até 2029. Se, após esses aumentos, a quantidade de servidores no órgão de fiscalização chegou a 300, o número de servidores antes do primeiro aumento foi de	OBJECTIVE	2	11	12	B	f	7c69d7de2a2704019d0c9de82593229dc9d8b0c87c1af41bdff3af2555c15ffb	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
159	OFFICIAL	4	8	2024	39	Um membro da comitiva francesa notou que o avião que o traria para a Cúpula da Amazônia , em Belém, levaria 11h36min de voo a uma velocidade média de 625km/h. Dessa forma, ele deduziu que a distância da França a Belém era de	OBJECTIVE	2	12	13	A	f	95dddb65ef42ad8340e5548f3626c7e8f9bd905ced5b4ecb511bd69ba097c557	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
160	OFFICIAL	4	8	2024	40	Como lembrança da Cúpula da Amazônia, uma empresa entregou uma miniatura retangular do parque estadual Mangal das Garças. Sabendo que essa miniatura tinha dimensões de 2cm de comprimento por 8cm de largura e que foi feita numa escala de 1:5000, a área real do mangal das garças, que foi representada, em m², na miniatura é	OBJECTIVE	2	13	14	A	f	7f362951655b4b2be4f6c6c08e502b5e839027e6c4583657c5cf9437aa472c37	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
162	OFFICIAL	5	10	2025	2	A leitura do Texto 1 permite afirmar que a proibição de celulares e outros dispositivos eletrônicos nas escolas do Rio de Janeiro deveu-se	OBJECTIVE	1	3	4	B	f	df7c126444b328d56a8799f96db14cbd4c47752fa3654458286268bd576f6f9e	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
167	OFFICIAL	5	10	2025	7	Sobre o uso dos dois-pontos no trecho, é correto afirmar que	OBJECTIVE	1	4	5	A	f	d02eecb9fbb6b553fc31984f30934d9063b9aa2ee214821c5e5a72fc7e9014e8	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
172	OFFICIAL	5	10	2025	12	O trecho sublinhado AS CRIANÇAS NÃO PODEM CONTINUAR FICANDO ISOLADAS NAS SUAS PRÓPRIAS TELAS, SEM INTERAGIR UMAS COM AS OUTRAS, SEM BRINCAR (3) permite afirmar que	OBJECTIVE	1	5	6	C	f	c5b9d9b5e25a96d37382439beaca9e5d21df9bb79701a0e8dfab33a87506705f	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
178	OFFICIAL	5	10	2025	18	Sobre o Texto 2, é correto afirmar que	OBJECTIVE	1	6	7	A	f	bb5b5131bf02d0c72dd2008bc6b4bbde72d206eea986a699d05edf76ab746f57	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
180	OFFICIAL	5	10	2025	20	Os Textos 1 e 2 apresentam em comum	OBJECTIVE	1	7	8	B	f	74d7d26df5d5552cdf672f65b1a06a5b22b15837224829652487f6e04871f635	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
185	OFFICIAL	5	10	2025	25	Considere que uma escola está monitorando o cumprimento da nova política que proíbe o uso de celulares durante as aulas. Em uma pesquisa com 150 alunos, foi constatado que 30 deles estavam usando celulares durante as aulas, apesar da proibição. A escola deseja estimar quantos alunos estariam usando celulares em um total de 2000 alunos. Considerando a mesma proporção da pesquisa, a estimativa de alunos que usariam celulares durante as aulas é	OBJECTIVE	2	8	9	D	f	ecc134e418e814d16804634bc0f43f158f1f464de67768805a3ca9f81d46dbd5	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
187	OFFICIAL	5	10	2025	27	Considere que uma escola está planejando a instalação de novas câmeras de segurança em suas salas de aula para garantir o cumprimento do decreto mencionado no Texto 1. Para determinar a quantidade de câmeras necessárias, a escola precisa calcular a área de cada um desses espaços. Cada sala de aula tem a forma de um retângulo e a escola pretende instalar uma câmera para até 10 metros quadrados. Considerando a sala de aula apresentada na figura acima, o número mínimo de câmeras necessárias para cobrir toda a sala é	OBJECTIVE	2	9	10	C	f	743fdf35c3d7f6ba0cbcea2fb6dc8450381139459be70124a2e6438593ce9d37	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
192	OFFICIAL	5	10	2025	32	Para promover o uso consciente de tecnologia na sala de aula e reduzir a dependência de celulares, uma escola comprou 30 tablets educativos. O preço unitário de cada tablet era de R$ 200,00, mas a escola obteve um desconto de 55%. O valor total investido na compra dos tablets foi de	OBJECTIVE	2	10	11	A	f	9dc0fd53aa54246e439b59e28885d847c562367ce02b4ca61a72d584032663fe	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
200	OFFICIAL	5	10	2025	40	Considerando que todos os celulares devem estar alinhados na mesma orientação (horizontal ou vertical), a quantidade máxima de celulares com dimensões 150 mm x 75 mm que podem ser dispostos sobre uma mesa de 1,0 m por 0,8 m, sem sobreposição e sem ultrapassar os limites da mesa é de	OBJECTIVE	2	12	13	D	f	d57077bd6dbc77fabb83c64e1dbfff64a8dd319b4a3b9061214ea4fe9a9adb0f	f	DIFICIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:42.935255+00
196	OFFICIAL	5	10	2025	36	A fração que mais se aproxima desse percentual é 25	OBJECTIVE	2	11	12	A	f	b74631ff92d029b282db06cba3594677e8ceb937e840189c673ad1d050e04e26	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:43.046349+00
37	OFFICIAL	1	2	2020	37	De acordo com o Texto 1, o percentual de crianças e adolescentes ofendidos na Internet cresce ano a ano, passando de 15% em 2014 para 20% em 2015 até chegar em 23% no ano de 2016. Considere que uma pesquisa foi feita em 2014 numa cidade A, em 2015 numa cidade B e, em 2016, numa cidade C, sempre com a mesma quantidade x de jovens. Considere também que os percentuais obtidos corresponderam aos resultados indicados no Texto 1, para cada ano. A expressão que representa o número total de crianças e adolescentes pesquisados nas três cidades que não sofreram, nesses três anos, agressões na Internet é 121𝑥	OBJECTIVE	2	12	13	A	f	25ce570b259a6f2338e6223f70765a2c96b9349c3092faf296a039562569b166	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:43.046349+00
144	OFFICIAL	4	8	2024	24	Segundo o Texto 1, “[...]Cada uma das plenárias-síntese terá 7 expositores: 4 da sociedade civil e 3 governamentais, sendo pelo menos 1 brasileiro em cada categoria [...]”. Considerando uma plenária que tinha exatamente um brasileiro nos membros da sociedade civil e um nos membros governamentais, a razão entre o número de brasileiros e o número de estrangeiros nessa plenária foi de 2	OBJECTIVE	2	9	10	A	f	723436062887c3d5955867a978f74eef1504bb6f7f14ed3b9f4e42db15a7d4a8	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:43.046349+00
202	OFFICIAL	6	12	2026	2	Segundo o Texto 1, a Conferência das Partes (COP) tem como função	OBJECTIVE	1	4	5	D	f	a9b48bbd7398609d429c511c825379d5592ce69f9453973abf126a415fe13047	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:43.046349+00
207	OFFICIAL	6	12	2026	7	O primeiro parágrafo do Texto 1 cumpre o papel de	OBJECTIVE	1	5	6	B	f	49cbf761d38d036353d40d1367d5a3843d8b94bb29721a7827985c3a14dd5d5b	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:43.046349+00
212	OFFICIAL	6	12	2026	12	A expressão IMPULSIONAR (2) tem sentido de	OBJECTIVE	1	6	7	A	f	d6a148eba616df6d5bba2d2a73dbab90c62247c1f2c2ff050e59fbc11e58f8f9	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:43.046349+00
218	OFFICIAL	6	12	2026	18	O sentido global do Texto 2 é construído pela	OBJECTIVE	1	7	8	C	f	05d604a1432bd4c5f76d1f00ce0e18dac74392988c79099d66f576228cc4f4e1	t	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:43.046349+00
223	OFFICIAL	6	12	2026	23	Considerando, para efeito de cálculo, 40 mil visitantes, dos quais 7 mil são integrantes da ONU e de delegações de países-membros, a probabilidade de que, ao escolher um visitante ao acaso, ele pertença ao grupo integrantes da ONU e de delegações de países-membros, é	OBJECTIVE	2	8	9	A	f	e41f8b765796069eb2dc10975686dc14d08c21a5676655acdc6517beb320cb97	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:43.046349+00
228	OFFICIAL	6	12	2026	28	De acordo com o Texto 1, para preparar Belém para a COP30, estão em andamento 38 obras, distribuídas em quatro categorias: 14 de infraestrutura, 8 de hospedagem, 8 de mobilidade e 8 de saneamento. O percentual aproximado das obras que pertencem à categoria infraestrutura é	OBJECTIVE	2	9	10	C	f	1abf0329384bd6d0723219511e116b70c8150793efde53a7a4c4815a7e7bb1ce	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:43.046349+00
231	OFFICIAL	6	12	2026	31	Suponha que, para a COP30 em Belém, tenha sido projetada uma praça de convivência em formato retangular, medindo 60 metros de comprimento por 40 metros de largura. No centro da praça, será construído um espaço circular para apresentações culturais, com 15 metros de raio, que não poderá ser utilizado para circulação. Nesses termos, a área, em metros quadrados, disponível para circulação nessa praça é	OBJECTIVE	2	10	11	C	f	99264dd1be21fe06bbb9473ccd8cad6f5855e4c6aefe8195a9a788c201f67f8f	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:43.046349+00
236	OFFICIAL	6	12	2026	36	Uma equipe de 120 operários leva 15 meses para concluir determinada obra de mobilidade para a COP30. Se essa equipe for reduzida para 80 operários, mantendo o mesmo ritmo de trabalho, o tempo necessário, em meses, para a conclusão da mesma obra será	OBJECTIVE	2	11	12	A	f	5bd36e619bed8d5a8bbae1e380b29b9915fc277ca542a1e78ad9c180e6d1a0e6	f	FACIL	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:43.046349+00
240	OFFICIAL	6	12	2026	40	Suponha que, para reduzir o consumo de copos descartáveis durante a COP30, sejam vendidos itens oficiais reutilizáveis do evento, como: garrafas, no valor de R$ 40,00; e copos no valor de R$ 15,00. Sabendo que, ao todo, serão comercializados 1000 itens e se espera uma arrecadação de R$ 25.000,00, o número de garrafas e copos reutilizáveis que serão vendidos, respectivamente, será	OBJECTIVE	2	12	13	B	f	82317895031034a1d2eaff3563312a6da004d0c1357c9f0e9d089fdd86694984	f	MEDIA	\N	PENDING	PENDENTE_REVISAO	2026-10-01 00:57:58.949678+00	2026-10-03 00:52:43.046349+00
\.


--
-- Data for Name: refresh_tokens; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.refresh_tokens (id, user_id, token_hash, expires_at, revoked_at, replaced_by_id, created_ip, user_agent, created_at, updated_at) FROM stdin;
58	5	37ac8256260dee506ffc7e35e94a1ac2b9571b25b91431dfbc9844f7d01d7ef1	2026-10-10 18:08:09.258918+00	\N	\N	172.19.0.1	curl/8.22.0	2026-10-03 18:08:09.110701+00	2026-10-03 18:08:09.110701+00
59	5	0ad8bc2315d70bc36c5100e70521c7477625917e78e4ad6a452bcf77331a0ba5	2026-10-10 18:08:09.461258+00	\N	\N	172.19.0.1	curl/8.22.0	2026-10-03 18:08:09.386183+00	2026-10-03 18:08:09.386183+00
9	4	ba0ab487ce45ef12f78b4737f9d39c61fa6903dfa81395c48e1afac282ca8f11	2026-10-10 01:04:13.34469+00	2026-10-03 01:04:20.554223+00	10	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) HeadlessChrome/152.0.0.0 Safari/537.36	2026-10-03 01:04:13.238516+00	2026-10-03 01:04:20.550318+00
10	4	37cf69427f93bc8313a31aabd512fcbb4b19ae360fd1b5bacf2197478d99139c	2026-10-10 01:04:20.554223+00	2026-10-03 01:04:29.051809+00	11	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) HeadlessChrome/152.0.0.0 Safari/537.36	2026-10-03 01:04:20.550318+00	2026-10-03 01:04:29.049604+00
7	4	f51379247a99d70c5d754d2587138b19278da6f0e747034b658ba5e526234790	2026-10-10 00:53:56.226778+00	2026-10-03 01:04:29.103011+00	\N	172.19.0.1	curl/8.22.0	2026-10-03 00:53:56.091842+00	2026-10-03 01:04:29.101548+00
8	4	87f9044825ad206bbad033f3f109fb76b02987f1b4e8feb665b4d5b94e8b4610	2026-10-10 00:53:56.432523+00	2026-10-03 01:04:29.103011+00	\N	172.19.0.1	curl/8.22.0	2026-10-03 00:53:56.360889+00	2026-10-03 01:04:29.101548+00
11	4	54789ae264138fafedc5861512cd48a977f288b62659938ff2bc190a554652b7	2026-10-10 01:04:29.051809+00	2026-10-03 01:04:29.103011+00	\N	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) HeadlessChrome/152.0.0.0 Safari/537.36	2026-10-03 01:04:29.049604+00	2026-10-03 01:04:29.101548+00
12	4	01ae2e3ac8d753365b4af9481e9e3448bde2a3918448bb91b53f54c69013d8c0	2026-10-10 01:16:02.481413+00	2026-10-03 01:17:20.114299+00	13	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:16:02.415979+00	2026-10-03 01:17:20.11006+00
13	4	c48a7a38d5d564afb0ead6764c0d9d9f882fc9e4ce641c55373f5e4e8ecdc897	2026-10-10 01:17:20.114299+00	2026-10-03 01:17:28.406919+00	\N	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:17:20.11006+00	2026-10-03 01:17:28.405573+00
14	4	a2b4667e72dc10daff1327676a17be40f6ac379ac4d1c775d8aa941d7f8fd3a6	2026-10-10 01:17:38.409577+00	2026-10-03 01:17:38.481362+00	15	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) HeadlessChrome/152.0.0.0 Safari/537.36	2026-10-03 01:17:38.293585+00	2026-10-03 01:17:38.480044+00
16	4	79004a8eb8dcf3018f6318eafa9b55a9373b067f9aab7929f18baa92ea6186d0	2026-10-10 01:17:38.538008+00	\N	\N	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) HeadlessChrome/152.0.0.0 Safari/537.36	2026-10-03 01:17:38.536693+00	2026-10-03 01:17:38.536693+00
15	4	f1f8cf01b46cdd2c1cefb11fec3a667c75473d2b8fb00c1be74067260fe60584	2026-10-10 01:17:38.481362+00	2026-10-03 01:17:38.538008+00	16	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) HeadlessChrome/152.0.0.0 Safari/537.36	2026-10-03 01:17:38.480044+00	2026-10-03 01:17:38.536693+00
17	4	d10e3dfe1d66d32076efd269909aff87027e991196c5aa8665afe60ec056a7b7	2026-10-10 01:17:40.0389+00	2026-10-03 01:17:40.124622+00	18	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:17:39.964473+00	2026-10-03 01:17:40.121989+00
18	4	2e6d4a3811db1c0f7c28430f40bb6d47d4263073e0cb523f958d29b2ad2488a8	2026-10-10 01:17:40.124622+00	2026-10-03 01:26:47.941321+00	19	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:17:40.121989+00	2026-10-03 01:26:47.93774+00
19	4	8f7e3a65ab3a628c124ce9cb08263faf9b4fffcf0124006a005ef4934c8f09c9	2026-10-10 01:26:47.941321+00	2026-10-03 01:34:50.213321+00	20	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:26:47.93774+00	2026-10-03 01:34:50.208868+00
20	4	36095a5ca6a1dae71ba45a292716c43235dfbdab634eae0afd8e9133f2c240b0	2026-10-10 01:34:50.213321+00	2026-10-03 01:35:52.74006+00	21	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:34:50.208868+00	2026-10-03 01:35:52.735017+00
21	4	5e71ec2052850b72ea0ad2f360f23dbe26080c2f6f68049fa5c3b3e0e55fd3a2	2026-10-10 01:35:52.74006+00	2026-10-03 01:35:54.380887+00	22	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:35:52.735017+00	2026-10-03 01:35:54.377564+00
22	4	81eb14505d9834c3530ea0616e597b3ff261076b9d44c5fa0dbbf7a22e1abba3	2026-10-10 01:35:54.380887+00	2026-10-03 01:36:06.886358+00	23	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:35:54.377564+00	2026-10-03 01:36:06.884022+00
23	4	8166f32b466141daa9b69b56ebe05d655864133490f4c28f11bf7500dde0464e	2026-10-10 01:36:06.886358+00	2026-10-03 01:36:16.792174+00	24	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:36:06.884022+00	2026-10-03 01:36:16.789171+00
24	4	2c3cf837723caf39d17c055baa6983279da0473c72cbee8747c36e92dd875e3b	2026-10-10 01:36:16.792174+00	2026-10-03 01:37:02.896933+00	25	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:36:16.789171+00	2026-10-03 01:37:02.893704+00
25	4	a867095b24be776e8e822f225e0252b11ed27476f23e972e9c1efbe5dee08ed1	2026-10-10 01:37:02.896933+00	2026-10-03 01:40:35.111535+00	26	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:37:02.893704+00	2026-10-03 01:40:35.109776+00
26	4	613a1deb7c0a996a414555f901f7e2fc0bdacd646da002d3669f8e130a735347	2026-10-10 01:40:35.111535+00	2026-10-03 01:41:01.1813+00	27	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:40:35.109776+00	2026-10-03 01:41:01.178783+00
27	4	538c11dfaa89d7119434d163e79025bf5650ccac0224ee308de5d4e114b7b73e	2026-10-10 01:41:01.1813+00	2026-10-03 01:41:02.53282+00	28	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:41:01.178783+00	2026-10-03 01:41:02.528312+00
28	4	f5945534104df2948d9732e165048b9110bdd132e7d61175f843d4ee9d8398b6	2026-10-10 01:41:02.53282+00	2026-10-03 01:41:32.103825+00	29	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:41:02.528312+00	2026-10-03 01:41:32.102611+00
29	4	64839f199afc6196ba4f36b33cb34537dd3eb9339c3e5e9bdc78d4519b85b0bd	2026-10-10 01:41:32.103825+00	2026-10-03 01:47:09.176964+00	30	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:41:32.102611+00	2026-10-03 01:47:09.173685+00
30	4	94ade573b78fcba43d05093e099fd59132fe787d125bda501df7e38f689d4c01	2026-10-10 01:47:09.176964+00	2026-10-03 01:48:11.256127+00	31	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:47:09.173685+00	2026-10-03 01:48:11.250516+00
31	4	cd58968c240750dfe490c5d7edee5758e61f1ab404dd7b58017bd607330b8c3c	2026-10-10 01:48:11.256127+00	2026-10-03 01:48:17.197793+00	32	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:48:11.250516+00	2026-10-03 01:48:17.196216+00
32	4	9c7d91e90738dcc250d724b33dcd3323c153f0563c10ae3d033cecae4b6e41f6	2026-10-10 01:48:17.197793+00	2026-10-03 01:49:05.025516+00	33	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:48:17.196216+00	2026-10-03 01:49:05.023969+00
33	4	fcbd5f06b97a74ec431d61cce139e91a8cb303a9af935f8e2ebc618987305c69	2026-10-10 01:49:05.025516+00	2026-10-03 01:50:33.381829+00	34	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:49:05.023969+00	2026-10-03 01:50:33.380629+00
35	4	65838f4f500dd192525d964a0715bb5251f778222c9ba2879f2b8cdd04ee3bc7	2026-10-10 01:51:37.538833+00	2026-10-03 01:52:46.744714+00	36	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:51:37.536364+00	2026-10-03 01:52:46.742613+00
34	4	0a4b5f1d21d5306eafaab78207834a4c79f55732f5eb8e8102625595078ee7e4	2026-10-10 01:50:33.381829+00	2026-10-03 01:51:37.538833+00	35	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:50:33.380629+00	2026-10-03 01:51:37.536364+00
57	4	14b78ddebc695fae371cfc0e293b86a152068068ec2ab44fa46103077affca2f	2026-10-10 02:41:32.555341+00	2026-10-03 02:45:17.765453+00	\N	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 02:41:32.55371+00	2026-10-03 02:45:17.763424+00
60	5	b6191e092a2e9891a41622dca9edb42d8b5b272390cabe3f719b9a19f977ca65	2026-10-10 18:10:58.132924+00	\N	\N	172.19.0.1	curl/8.22.0	2026-10-03 18:10:58.028955+00	2026-10-03 18:10:58.028955+00
36	4	ecde263ddde0e25f1ccaa6e6bc9a62d1492bb6a27cb5385b86993bf09f4f3442	2026-10-10 01:52:46.744714+00	2026-10-03 01:53:00.827268+00	37	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:52:46.742613+00	2026-10-03 01:53:00.826126+00
37	4	606613068be71074218fb0027c2ef64863a0714244facd443d3395f68440c18d	2026-10-10 01:53:00.827268+00	2026-10-03 01:53:14.506825+00	38	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:53:00.826126+00	2026-10-03 01:53:14.502352+00
38	4	cb27539ab6e46a87fa74bb0101dd1896be6584dd5e6ff320cab79700b5fbef81	2026-10-10 01:53:14.506825+00	2026-10-03 01:53:25.222016+00	39	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:53:14.502352+00	2026-10-03 01:53:25.220978+00
61	5	568bc6925b48eeab4db519084592c07ff95e2af8a0fc9ad64f2ace2f5f86eae8	2026-10-10 18:11:32.724747+00	2026-10-03 18:11:32.813389+00	62	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 18:11:32.66046+00	2026-10-03 18:11:32.807839+00
39	4	b8264e3ec08d14c93210f9e8a97a8d830a33c2847c8cf62163c24a0cfcd83e46	2026-10-10 01:53:25.222016+00	2026-10-03 01:53:28.443322+00	40	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:53:25.220978+00	2026-10-03 01:53:28.440466+00
40	4	9c5577fc03153015ea9034c62205ed33f0d3705b4ab98888a2062c52727d31ee	2026-10-10 01:53:28.443322+00	2026-10-03 01:53:35.172363+00	41	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:53:28.440466+00	2026-10-03 01:53:35.170777+00
62	5	5646d459f19298be368c96b1c8ea7f61b5f159e23e29e5205603a28a3e6497fb	2026-10-10 18:11:32.813389+00	2026-10-03 18:11:46.587542+00	63	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 18:11:32.807839+00	2026-10-03 18:11:46.584963+00
41	4	dfdd9ad2198fb853f8943b6e7b23c1d85edfef8d6c82fa6398895a51d6e0432c	2026-10-10 01:53:35.172363+00	2026-10-03 01:53:38.109525+00	42	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:53:35.170777+00	2026-10-03 01:53:38.107584+00
42	4	b1278705c28803eb597457c57e224703251858a6772d8d5a7e1e218186b7e835	2026-10-10 01:53:38.109525+00	2026-10-03 01:53:39.464026+00	43	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:53:38.107584+00	2026-10-03 01:53:39.462107+00
63	5	1c78917137ff28eef3a073cbd6df09354df9f946c3376c7514439607aaeb8683	2026-10-10 18:11:46.587542+00	2026-10-03 18:11:48.326428+00	64	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 18:11:46.584963+00	2026-10-03 18:11:48.323973+00
43	4	92f659ab811faaaac85f5ee8d0efa69e9c8742bb4a6f7c984eae33114f43ea68	2026-10-10 01:53:39.464026+00	2026-10-03 01:53:40.663687+00	44	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:53:39.462107+00	2026-10-03 01:53:40.66321+00
44	4	035c8794680e01f6761f4849a3481bafa5a73185390c013a0c32830b086bb50a	2026-10-10 01:53:40.663687+00	2026-10-03 01:53:41.86001+00	45	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:53:40.66321+00	2026-10-03 01:53:41.858192+00
65	5	f5c60d1e6575d37cd1c44298d78abb59aa6d24047ce4abb7d3b770b9d46f5001	2026-10-10 18:13:35.516537+00	2026-10-03 18:28:07.454093+00	66	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 18:13:35.515767+00	2026-10-03 18:28:07.450333+00
45	4	6da77ae4d024c29a7e684c3a4b001935403f1a10401bea6a68d73311df0b8925	2026-10-10 01:53:41.86001+00	2026-10-03 01:53:47.88688+00	46	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:53:41.858192+00	2026-10-03 01:53:47.885792+00
68	5	8053c1ba0e7fa1daa777f89580d377d918534526cfce169420c30c07d2f9dac0	2026-10-10 18:33:31.457906+00	\N	\N	172.19.0.1	curl/8.22.0	2026-10-03 18:33:31.34304+00	2026-10-03 18:33:31.34304+00
46	4	a019508fe0043ffced11648e5a55bd6d674085a04358a527d9e6492279e1d8d5	2026-10-10 01:53:47.88688+00	2026-10-03 01:54:54.379213+00	47	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:53:47.885792+00	2026-10-03 01:54:54.378352+00
69	5	2a1ef643fcef2f8a32669b39cc3d290e9c8c44d0d9595c39d34a03ff56286d3c	2026-10-10 18:33:35.476745+00	\N	\N	172.19.0.1	curl/8.22.0	2026-10-03 18:33:35.393909+00	2026-10-03 18:33:35.393909+00
47	4	4a5d7878bfd27fcb0facfc58e29147c90d93d79f2c2e151ab7f8ba0f7a652fd4	2026-10-10 01:54:54.379213+00	2026-10-03 01:54:57.607237+00	48	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:54:54.378352+00	2026-10-03 01:54:57.60561+00
48	4	389d920e47db395e88828901f2983cccbf4214736a6e7bab84ca6576b7dd2d2e	2026-10-10 01:54:57.607237+00	2026-10-03 01:57:46.145273+00	49	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:54:57.60561+00	2026-10-03 01:57:46.144594+00
49	4	9b5e3c784547b607e27e3565bf05d899e0194fd0f4d26dc3d2ec24b6d37c691e	2026-10-10 01:57:46.145273+00	2026-10-03 02:41:05.917508+00	50	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 01:57:46.144594+00	2026-10-03 02:41:05.913685+00
50	4	74785ad787bdab804efde2ce4e147c502d0d223d267e1995964422d8408222fd	2026-10-10 02:41:05.917508+00	2026-10-03 02:41:11.382812+00	51	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 02:41:05.913685+00	2026-10-03 02:41:11.381791+00
51	4	29eaf2ccde8c2494ade6211577e92433fc40332752dbfc1a5d9f55b93026997c	2026-10-10 02:41:11.382812+00	2026-10-03 02:41:16.973045+00	52	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 02:41:11.381791+00	2026-10-03 02:41:16.97166+00
52	4	ba7d9cc5fbc2403257d8a822fe737661a58c9a361488cf8b8b197e39848b96e1	2026-10-10 02:41:16.973045+00	2026-10-03 02:41:18.779477+00	53	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 02:41:16.97166+00	2026-10-03 02:41:18.778648+00
53	4	2dd9d955bc8b1805d6dd2bb67cff0f36324e4608f24b32753796dee5435d2a06	2026-10-10 02:41:18.779477+00	2026-10-03 02:41:22.936899+00	54	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 02:41:18.778648+00	2026-10-03 02:41:22.935964+00
54	4	593ff88adfde59ab9070f0f01f90a38fb5387ea59ee968273d163118273ddf86	2026-10-10 02:41:22.936899+00	2026-10-03 02:41:25.201399+00	55	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 02:41:22.935964+00	2026-10-03 02:41:25.20037+00
55	4	a438c16851333a50de9e77b9568cdcdc507a4447b6e91c9292f8742035e8db9f	2026-10-10 02:41:25.201399+00	2026-10-03 02:41:29.110361+00	56	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 02:41:25.20037+00	2026-10-03 02:41:29.106105+00
56	4	65e2a6461babaedfa3fbdf591510b3305a3933b3c30388c7650b2d68504ac2f0	2026-10-10 02:41:29.110361+00	2026-10-03 02:41:32.555341+00	57	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 02:41:29.106105+00	2026-10-03 02:41:32.55371+00
64	5	7e5421e5f91d77b7074a10843c5d51251160046da2e3811a37fcff325f17d560	2026-10-10 18:11:48.326428+00	2026-10-03 18:13:35.516537+00	65	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 18:11:48.323973+00	2026-10-03 18:13:35.515767+00
66	5	a978dd359dfa40e62c8acf5da0170235b938ed6537639dde73442eb4de37470b	2026-10-10 18:28:07.454093+00	2026-10-03 18:28:27.197042+00	67	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 18:28:07.450333+00	2026-10-03 18:28:27.191792+00
67	5	baecb1f16e2b0fb91ce6a902ae78ada0ce416614f97e014085ec6e9bceab5f29	2026-10-10 18:28:27.197042+00	2026-10-03 19:12:22.284134+00	\N	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 18:28:27.191792+00	2026-10-03 19:12:22.278714+00
229	5	2bb9f0267594f2aff0e67bc4672eae33000739d2c2a09d6f00e3a3c9a521ffed	2026-10-10 23:22:35.786466+00	2026-10-03 23:22:35.996894+00	230	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 23:22:35.671631+00	2026-10-03 23:22:35.991104+00
230	5	fd66f3a80e6418f6acf7aa3c576e368b96633e420636a45b1560b9df3fb8353c	2026-10-10 23:22:35.996894+00	2026-10-03 23:23:41.617444+00	231	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 23:22:35.991104+00	2026-10-03 23:23:41.614993+00
232	5	2c1b93740a01547f725a5d128c6ebb7bd3d8d63e7eb5fc6b08d19b7693fe239e	2026-10-10 23:24:53.34176+00	\N	\N	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 23:24:53.340053+00	2026-10-03 23:24:53.340053+00
231	5	680fccac2096eba8184d8a70af2758dbb642c40b40fd648fcedec99c3f924502	2026-10-10 23:23:41.617444+00	2026-10-03 23:24:53.34176+00	232	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 23:23:41.614993+00	2026-10-03 23:24:53.340053+00
70	5	5ebce06734bf047f5753d2a2f004262815994e8f085b61d5e561dd648be9eae6	2026-10-10 18:34:50.263293+00	2026-10-03 18:34:50.348438+00	71	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) HeadlessChrome/152.0.0.0 Safari/537.36	2026-10-03 18:34:50.197865+00	2026-10-03 18:34:50.344093+00
71	5	b547eb7369a8eae17e7be53253400eb7d9ad2ee30720a6a41d19cd06cdec7c0b	2026-10-10 18:34:50.348438+00	2026-10-03 18:34:59.621345+00	72	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) HeadlessChrome/152.0.0.0 Safari/537.36	2026-10-03 18:34:50.344093+00	2026-10-03 18:34:59.619911+00
72	5	9943165cc4424087301c05b9834aaa94655afcb8ad693b72d3667bd201b96689	2026-10-10 18:34:59.621345+00	2026-10-03 18:35:21.262922+00	73	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) HeadlessChrome/152.0.0.0 Safari/537.36	2026-10-03 18:34:59.619911+00	2026-10-03 18:35:21.260303+00
73	5	8103be2da280905d5e184692f05c523cbeddc97bf05ce08c13d1a9b5a6b96401	2026-10-10 18:35:21.262922+00	2026-10-03 18:35:49.02046+00	74	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) HeadlessChrome/152.0.0.0 Safari/537.36	2026-10-03 18:35:21.260303+00	2026-10-03 18:35:49.017965+00
75	5	e80e0e016918efec07f40ebc438367779b125a1cf92ae6d9484cb19077bbcffe	2026-10-10 18:36:08.400479+00	\N	\N	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) HeadlessChrome/152.0.0.0 Safari/537.36	2026-10-03 18:36:08.398074+00	2026-10-03 18:36:08.398074+00
74	5	349e40b83c56041ea053bd6d513b8ea42984a4e7e5f0be3b78bd89ab9fdfb7dd	2026-10-10 18:35:49.02046+00	2026-10-03 18:36:08.400479+00	75	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) HeadlessChrome/152.0.0.0 Safari/537.36	2026-10-03 18:35:49.017965+00	2026-10-03 18:36:08.398074+00
77	5	50b55baebee31d5b413bf78ab45e66f5df50d0069326bdfbc07f5834109bc322	2026-10-10 19:12:36.324698+00	2026-10-03 19:12:36.389712+00	78	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 19:12:36.236771+00	2026-10-03 19:12:36.387423+00
78	5	4c3cb9abdaf5256077b5d8710ff6030394bab486a940207108af2ecbd032521c	2026-10-10 19:12:36.389712+00	2026-10-03 19:15:42.971768+00	\N	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 19:12:36.387423+00	2026-10-03 19:15:42.970064+00
79	5	9c2ffae09467fab6f1cce0ed6e6110b0d058f28e16bca2f8e7a0044bd3a66d80	2026-10-10 19:15:58.741127+00	2026-10-03 19:15:58.790094+00	80	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 19:15:58.672674+00	2026-10-03 19:15:58.788567+00
81	5	80e9c007a089fecb1fb18c3e93f7390ff177062e52045651cfa3ca73b93ea5f5	2026-10-10 19:16:40.472963+00	\N	\N	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 19:16:40.469695+00	2026-10-03 19:16:40.469695+00
80	5	543bd14e8a7bad04f1e64e6f44b35ad3af723d712d028cdb573fefdae6c60d68	2026-10-10 19:15:58.790094+00	2026-10-03 19:16:40.472963+00	81	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 19:15:58.788567+00	2026-10-03 19:16:40.469695+00
83	5	a0b762ef0e52d1d40fee4b2840cbf4ffc83e386907f965ac0e97a43f1d5b0d35	2026-10-10 19:21:30.652789+00	2026-10-03 19:21:30.690899+00	84	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 19:21:30.568967+00	2026-10-03 19:21:30.688255+00
85	5	09f1f4d3181df7667fb2acb838caac5b603bc83f4995e70bc2035ed85c4a937e	2026-10-10 19:22:16.863378+00	\N	\N	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 19:22:16.854913+00	2026-10-03 19:22:16.854913+00
84	5	ee9c39f85e9b0ee58921af27f7cc7a9553657415fe3284d4bb76afaf0fa2a2bc	2026-10-10 19:21:30.690899+00	2026-10-03 19:22:16.863378+00	85	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 19:21:30.688255+00	2026-10-03 19:22:16.854913+00
86	5	24e6f822690bff0b858052fd02db77c331ed696de3e6ca179b3c2c0190123dc8	2026-10-10 19:22:57.850197+00	2026-10-03 19:22:58.001613+00	87	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 19:22:57.769938+00	2026-10-03 19:22:58.000132+00
87	5	c5a7c2521ea22d93cc9489402019167db8f5227f2b36dbd942131fa342bbb446	2026-10-10 19:22:58.001613+00	2026-10-03 19:29:15.944641+00	92	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 19:22:58.000132+00	2026-10-03 19:29:15.943404+00
92	5	029b7d4fd1aa04f6e113e30672f7778b6140fcf8a54c51badea6d9067204175d	2026-10-10 19:29:15.944641+00	2026-10-03 19:29:39.066416+00	95	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 19:29:15.943404+00	2026-10-03 19:29:39.064776+00
95	5	7064724fbb5103e374f79ae36b8d62ce0c6fcd65a473541d85de976708a3eb1e	2026-10-10 19:29:39.066416+00	2026-10-03 19:29:53.290423+00	97	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 19:29:39.064776+00	2026-10-03 19:29:53.288826+00
105	5	5aa571049ff5aeb63eeb82213e45c8bf0d393fbf54c66f0f5ff93dd9fa618400	2026-10-10 19:32:30.44965+00	\N	\N	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 19:32:30.448894+00	2026-10-03 19:32:30.448894+00
97	5	27aa84b0ddc4e34541d424467d8cac0ec613cdce0e1cc62bac071f8bafdd17ba	2026-10-10 19:29:53.290423+00	2026-10-03 19:32:30.44965+00	105	172.19.0.1	Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36	2026-10-03 19:29:53.288826+00	2026-10-03 19:32:30.448894+00
\.


--
-- Data for Name: review_session_questions; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.review_session_questions (study_session_id, "position", question_id, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.roles (id, code, description, created_at, updated_at) FROM stdin;
1	STUDENT	Estudante: seus próprios attempts/planos	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
2	CURATOR	Curadoria: revisar classificações e publicação	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
3	ADMIN	Administração: métricas e gestão	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
\.


--
-- Data for Name: simulation_attempts; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.simulation_attempts (id, simulation_id, user_id, mode, status, started_at, submitted_at, score_json, created_at, updated_at) FROM stdin;
5	5	4	PROVA	IN_PROGRESS	2026-10-03 01:50:33.309705+00	\N	\N	2026-10-03 01:50:33.278772+00	2026-10-03 01:50:33.278772+00
\.


--
-- Data for Name: simulation_questions; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.simulation_questions (simulation_attempt_id, "position", question_id, frozen_answer_key, created_at, updated_at) FROM stdin;
5	1	64	A	2026-10-03 01:50:33.278772+00	2026-10-03 01:50:33.278772+00
5	2	78	D	2026-10-03 01:50:33.278772+00	2026-10-03 01:50:33.278772+00
5	3	77	C	2026-10-03 01:50:33.278772+00	2026-10-03 01:50:33.278772+00
5	4	155	D	2026-10-03 01:50:33.278772+00	2026-10-03 01:50:33.278772+00
5	5	79	C	2026-10-03 01:50:33.278772+00	2026-10-03 01:50:33.278772+00
5	6	223	A	2026-10-03 01:50:33.278772+00	2026-10-03 01:50:33.278772+00
5	7	222	B	2026-10-03 01:50:33.278772+00	2026-10-03 01:50:33.278772+00
5	8	231	C	2026-10-03 01:50:33.278772+00	2026-10-03 01:50:33.278772+00
5	9	34	A	2026-10-03 01:50:33.278772+00	2026-10-03 01:50:33.278772+00
5	10	107	A	2026-10-03 01:50:33.278772+00	2026-10-03 01:50:33.278772+00
\.


--
-- Data for Name: simulations; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.simulations (id, owner_user_id, type, exam_id, filter_json, title, created_at, updated_at) FROM stdin;
5	4	BY_DISCIPLINE	\N	{"mode": "PROVA", "type": "BY_DISCIPLINE", "difficulty": null, "discipline": "MATEMATICA", "questionCount": 10}	Simulado Matemática — 10 questões [PROVA]	2026-10-03 01:50:33.278772+00	2026-10-03 01:50:33.278772+00
\.


--
-- Data for Name: student_achievements; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.student_achievements (user_id, achievement_id, awarded_at, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: student_profiles; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.student_profiles (user_id, display_name, school_year, target_year, study_goal, created_at, updated_at) FROM stdin;
4	Aluno Teste	\N	\N	\N	2026-10-03 00:53:56.091842+00	2026-10-03 00:53:56.091842+00
5	Aluno Teste	\N	\N	\N	2026-10-03 18:08:09.110701+00	2026-10-03 18:08:09.110701+00
\.


--
-- Data for Name: student_topic_performance; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.student_topic_performance (id, user_id, topic_id, subtopic_id, attempts, hits, last_attempt_at, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: study_plan_items; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.study_plan_items (id, study_plan_id, topic_id, subtopic_id, priority, reason, evidence_json, status, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: study_plans; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.study_plans (id, user_id, is_active, algorithm_version, generated_at, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: study_sessions; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.study_sessions (id, user_id, mode, status, started_at, finished_at, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: subtopics; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.subtopics (id, topic_id, code, name, is_active, created_at, updated_at) FROM stdin;
1	1	GENERO_TEXTUAL	Gênero textual	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
2	1	TIPO_TEXTUAL	Tipo textual	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
3	1	FUNCAO_ELEMENTO_PARAGRAFO	Função de elemento/parágrafo	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
4	1	INFERENCIA	Inferência	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
5	1	INFORMACAO_EXPLICITA	Informação explícita	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
6	1	INTENCAO_COMUNICATIVA	Intenção comunicativa	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
7	2	ACENTUACAO_GRAFICA	Acentuação gráfica	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
8	2	NORMA_PADRAO	Norma-padrão	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
9	2	SINTAXE_FUNCAO	Funções sintáticas	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
10	2	SINTAXE_PERIODO	Sintaxe do período	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
11	2	COESAO_REFERENCIA	Coesão e referência	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
12	2	SEMANTICA_VOCABULARIO	Semântica e vocabulário	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
13	2	PONTUACAO	Pontuação	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
14	2	MORFOLOGIA	Morfologia	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
15	3	VOLUME_CAPACIDADE	Volume e capacidade	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
16	3	TEMPO	Tempo	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
17	4	JUROS_SIMPLES	Juros simples	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
18	5	REPRESENTACAO_FRACAO	Representação fracionária	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
19	5	VARIACAO	Variação percentual	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
20	5	CALCULO_DIRETO	Cálculo direto	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
21	6	PROBABILIDADE	Probabilidade	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
22	6	LEITURA_GRAFICO_TABELA	Leitura de gráfico/tabela	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
23	6	MEDIA	Média	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
24	7	UNIDADES_MEDIDA	Unidades de medida	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
25	7	VOLUME	Volume	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
26	7	PITAGORAS_DISTANCIA	Pitágoras e distância	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
27	7	POLIGONOS	Polígonos	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
28	7	PERIMETRO_COMPRIMENTO	Perímetro e comprimento	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
29	7	AREA_PLANA	Área plana	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
30	8	SEQUENCIAS	Sequências	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
31	8	SISTEMAS	Sistemas	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
32	8	EQUACOES	Equações	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
33	8	FUNCAO_AFIM	Função afim	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
34	9	SISTEMAS_NUMERACAO	Sistemas de numeração	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
35	9	DIVISIBILIDADE_MMC_MDC	Divisibilidade, MMC e MDC	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
36	9	NOTACAO_CIENTIFICA	Notação científica	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
37	9	FRACOES	Frações	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
38	9	OPERACOES	Operações	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
39	10	PROPORCIONALIDADE	Proporcionalidade	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
40	10	ESCALA	Escala	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
41	10	REGRA_DE_TRES	Regra de três	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
42	10	RAZAO	Razão	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
\.


--
-- Data for Name: topics; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.topics (id, discipline_id, code, name, is_active, created_at, updated_at) FROM stdin;
1	1	INTERPRETACAO_TEXTUAL	Interpretação textual	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
2	1	GRAMATICA_NORMA	Gramática e norma	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
3	2	GRANDEZAS_MEDIDAS	Grandezas e medidas	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
4	2	MATEMATICA_FINANCEIRA	Matemática financeira	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
5	2	PORCENTAGEM	Porcentagem	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
6	2	ESTATISTICA_DADOS	Estatística e dados	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
7	2	GEOMETRIA	Geometria	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
8	2	ALGEBRA	Álgebra	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
9	2	ARITMETICA	Aritmética	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
10	2	RAZAO_PROPORCAO	Razão e proporção	t	2026-10-01 00:52:18.034689+00	2026-10-01 00:52:18.034689+00
\.


--
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.user_roles (user_id, role_id, granted_at, created_at, updated_at) FROM stdin;
4	1	2026-10-03 00:53:56.211131+00	2026-10-03 00:53:56.091842+00	2026-10-03 00:53:56.091842+00
5	1	2026-10-03 18:08:09.244274+00	2026-10-03 18:08:09.110701+00	2026-10-03 18:08:09.110701+00
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.users (id, email, password_hash, is_active, credential_version, last_login_at, created_at, updated_at) FROM stdin;
5	aluno@teste.local	$2a$10$Shwgfvo9ZukojMMxuC1XE.spplU.FvScfSkn22yMJm1/Fjyt8gxrK	t	1	2026-10-03 23:24:53.34176+00	2026-10-03 18:08:09.110701+00	2026-10-03 23:24:53.340053+00
4	teste@voupassar.local	$2a$10$jGMdqwRSS5W20xOPH4Up9eKZWzX1ad6V8bhuDIZrkGtYgOeDuFA16	t	1	2026-10-03 02:41:32.555341+00	2026-10-03 00:53:56.091842+00	2026-10-03 02:41:32.55371+00
\.


--
-- Name: achievements_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.achievements_id_seq', 1, false);


--
-- Name: disciplines_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.disciplines_id_seq', 2, true);


--
-- Name: exam_documents_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.exam_documents_id_seq', 12, true);


--
-- Name: exam_versions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.exam_versions_id_seq', 6, true);


--
-- Name: exams_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.exams_id_seq', 6, true);


--
-- Name: passages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.passages_id_seq', 31, true);


--
-- Name: password_reset_tokens_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.password_reset_tokens_id_seq', 1, false);


--
-- Name: progress_snapshots_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.progress_snapshots_id_seq', 1, false);


--
-- Name: question_attempts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.question_attempts_id_seq', 8, true);


--
-- Name: question_classifications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.question_classifications_id_seq', 240, true);


--
-- Name: question_figures_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.question_figures_id_seq', 41, true);


--
-- Name: question_options_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.question_options_id_seq', 960, true);


--
-- Name: question_passages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.question_passages_id_seq', 165, true);


--
-- Name: question_sources_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.question_sources_id_seq', 480, true);


--
-- Name: question_tags_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.question_tags_id_seq', 1, true);


--
-- Name: questions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.questions_id_seq', 240, true);


--
-- Name: refresh_tokens_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.refresh_tokens_id_seq', 232, true);


--
-- Name: roles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.roles_id_seq', 3, true);


--
-- Name: simulation_attempts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.simulation_attempts_id_seq', 5, true);


--
-- Name: simulations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.simulations_id_seq', 5, true);


--
-- Name: student_topic_performance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.student_topic_performance_id_seq', 24, true);


--
-- Name: study_plan_items_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.study_plan_items_id_seq', 1, false);


--
-- Name: study_plans_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.study_plans_id_seq', 1, false);


--
-- Name: study_sessions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.study_sessions_id_seq', 5, true);


--
-- Name: subtopics_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.subtopics_id_seq', 42, true);


--
-- Name: topics_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.topics_id_seq', 10, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.users_id_seq', 12, true);


--
-- Name: achievements achievements_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.achievements
    ADD CONSTRAINT achievements_code_key UNIQUE (code);


--
-- Name: achievements achievements_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.achievements
    ADD CONSTRAINT achievements_pkey PRIMARY KEY (id);


--
-- Name: disciplines disciplines_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.disciplines
    ADD CONSTRAINT disciplines_code_key UNIQUE (code);


--
-- Name: disciplines disciplines_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.disciplines
    ADD CONSTRAINT disciplines_pkey PRIMARY KEY (id);


--
-- Name: exam_documents exam_documents_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_documents
    ADD CONSTRAINT exam_documents_pkey PRIMARY KEY (id);


--
-- Name: exam_documents exam_documents_sha256_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_documents
    ADD CONSTRAINT exam_documents_sha256_key UNIQUE (sha256);


--
-- Name: exam_essay_prompts exam_essay_prompts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_essay_prompts
    ADD CONSTRAINT exam_essay_prompts_pkey PRIMARY KEY (exam_id);


--
-- Name: exam_versions exam_versions_exam_id_version_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_versions
    ADD CONSTRAINT exam_versions_exam_id_version_code_key UNIQUE (exam_id, version_code);


--
-- Name: exam_versions exam_versions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_versions
    ADD CONSTRAINT exam_versions_pkey PRIMARY KEY (id);


--
-- Name: exams exams_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exams
    ADD CONSTRAINT exams_pkey PRIMARY KEY (id);


--
-- Name: exams exams_year_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exams
    ADD CONSTRAINT exams_year_key UNIQUE (year);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: passages passages_exam_id_passage_key_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.passages
    ADD CONSTRAINT passages_exam_id_passage_key_key UNIQUE (exam_id, passage_key);


--
-- Name: passages passages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.passages
    ADD CONSTRAINT passages_pkey PRIMARY KEY (id);


--
-- Name: password_reset_tokens password_reset_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (id);


--
-- Name: password_reset_tokens password_reset_tokens_token_hash_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_token_hash_key UNIQUE (token_hash);


--
-- Name: progress_snapshots progress_snapshots_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.progress_snapshots
    ADD CONSTRAINT progress_snapshots_pkey PRIMARY KEY (id);


--
-- Name: progress_snapshots progress_snapshots_user_id_taken_at_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.progress_snapshots
    ADD CONSTRAINT progress_snapshots_user_id_taken_at_key UNIQUE (user_id, taken_at);


--
-- Name: question_attempts question_attempts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_attempts
    ADD CONSTRAINT question_attempts_pkey PRIMARY KEY (id);


--
-- Name: question_classifications question_classifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_classifications
    ADD CONSTRAINT question_classifications_pkey PRIMARY KEY (id);


--
-- Name: question_figures question_figures_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_figures
    ADD CONSTRAINT question_figures_pkey PRIMARY KEY (id);


--
-- Name: question_figures question_figures_question_id_position_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_figures
    ADD CONSTRAINT question_figures_question_id_position_key UNIQUE (question_id, "position");


--
-- Name: question_options question_options_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_options
    ADD CONSTRAINT question_options_pkey PRIMARY KEY (id);


--
-- Name: question_options question_options_question_id_label_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_options
    ADD CONSTRAINT question_options_question_id_label_key UNIQUE (question_id, label);


--
-- Name: question_passages question_passages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_passages
    ADD CONSTRAINT question_passages_pkey PRIMARY KEY (id);


--
-- Name: question_passages question_passages_question_id_passage_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_passages
    ADD CONSTRAINT question_passages_question_id_passage_id_key UNIQUE (question_id, passage_id);


--
-- Name: question_sources question_sources_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_sources
    ADD CONSTRAINT question_sources_pkey PRIMARY KEY (id);


--
-- Name: question_tag_map question_tag_map_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_tag_map
    ADD CONSTRAINT question_tag_map_pkey PRIMARY KEY (question_id, tag_id);


--
-- Name: question_tags question_tags_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_tags
    ADD CONSTRAINT question_tags_code_key UNIQUE (code);


--
-- Name: question_tags question_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_tags
    ADD CONSTRAINT question_tags_pkey PRIMARY KEY (id);


--
-- Name: questions questions_checksum_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT questions_checksum_key UNIQUE (checksum);


--
-- Name: questions questions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT questions_pkey PRIMARY KEY (id);


--
-- Name: questions questions_source_type_source_year_source_question_number_ex_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT questions_source_type_source_year_source_question_number_ex_key UNIQUE (source_type, source_year, source_question_number, exam_document_id);


--
-- Name: refresh_tokens refresh_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id);


--
-- Name: refresh_tokens refresh_tokens_token_hash_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_token_hash_key UNIQUE (token_hash);


--
-- Name: review_session_questions review_session_questions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.review_session_questions
    ADD CONSTRAINT review_session_questions_pkey PRIMARY KEY (study_session_id, "position");


--
-- Name: roles roles_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_code_key UNIQUE (code);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: simulation_attempts simulation_attempts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simulation_attempts
    ADD CONSTRAINT simulation_attempts_pkey PRIMARY KEY (id);


--
-- Name: simulation_questions simulation_questions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simulation_questions
    ADD CONSTRAINT simulation_questions_pkey PRIMARY KEY (simulation_attempt_id, "position");


--
-- Name: simulations simulations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simulations
    ADD CONSTRAINT simulations_pkey PRIMARY KEY (id);


--
-- Name: student_achievements student_achievements_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_achievements
    ADD CONSTRAINT student_achievements_pkey PRIMARY KEY (user_id, achievement_id);


--
-- Name: student_profiles student_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_profiles
    ADD CONSTRAINT student_profiles_pkey PRIMARY KEY (user_id);


--
-- Name: student_topic_performance student_topic_performance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_topic_performance
    ADD CONSTRAINT student_topic_performance_pkey PRIMARY KEY (id);


--
-- Name: student_topic_performance student_topic_performance_user_id_topic_id_subtopic_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_topic_performance
    ADD CONSTRAINT student_topic_performance_user_id_topic_id_subtopic_id_key UNIQUE NULLS NOT DISTINCT (user_id, topic_id, subtopic_id);


--
-- Name: study_plan_items study_plan_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.study_plan_items
    ADD CONSTRAINT study_plan_items_pkey PRIMARY KEY (id);


--
-- Name: study_plans study_plans_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.study_plans
    ADD CONSTRAINT study_plans_pkey PRIMARY KEY (id);


--
-- Name: study_sessions study_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.study_sessions
    ADD CONSTRAINT study_sessions_pkey PRIMARY KEY (id);


--
-- Name: subtopics subtopics_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subtopics
    ADD CONSTRAINT subtopics_pkey PRIMARY KEY (id);


--
-- Name: subtopics subtopics_topic_id_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subtopics
    ADD CONSTRAINT subtopics_topic_id_code_key UNIQUE (topic_id, code);


--
-- Name: topics topics_discipline_id_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.topics
    ADD CONSTRAINT topics_discipline_id_code_key UNIQUE (discipline_id, code);


--
-- Name: topics topics_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.topics
    ADD CONSTRAINT topics_pkey PRIMARY KEY (id);


--
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_attempts_question; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_attempts_question ON public.question_attempts USING btree (question_id);


--
-- Name: idx_attempts_user_mode; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_attempts_user_mode ON public.question_attempts USING btree (user_id, mode, answered_at DESC);


--
-- Name: idx_attempts_user_q; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_attempts_user_q ON public.question_attempts USING btree (user_id, question_id, answered_at DESC);


--
-- Name: idx_passages_exam; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_passages_exam ON public.passages USING btree (exam_id, passage_key);


--
-- Name: idx_password_reset_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_password_reset_user ON public.password_reset_tokens USING btree (user_id, expires_at) WHERE (used_at IS NULL);


--
-- Name: idx_perf_user_acc; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_perf_user_acc ON public.student_topic_performance USING btree (user_id, accuracy);


--
-- Name: idx_plan_items; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_plan_items ON public.study_plan_items USING btree (study_plan_id, priority);


--
-- Name: idx_qclass_q; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qclass_q ON public.question_classifications USING btree (question_id, status, taxonomy_version);


--
-- Name: idx_qclass_topic; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qclass_topic ON public.question_classifications USING btree (topic_id, subtopic_id);


--
-- Name: idx_question_figures_q; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_question_figures_q ON public.question_figures USING btree (question_id, "position");


--
-- Name: idx_question_options_q; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_question_options_q ON public.question_options USING btree (question_id);


--
-- Name: idx_question_passages_q; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_question_passages_q ON public.question_passages USING btree (question_id, "position");


--
-- Name: idx_question_sources_q; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_question_sources_q ON public.question_sources USING btree (question_id);


--
-- Name: idx_questions_disc; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_questions_disc ON public.questions USING btree (discipline_id, id);


--
-- Name: idx_questions_exam; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_questions_exam ON public.questions USING btree (exam_id, source_question_number);


--
-- Name: idx_questions_public; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_questions_public ON public.questions USING btree (source_type, publication_status) WHERE (publication_status = 'PUBLICAVEL'::text);


--
-- Name: idx_questions_trgm; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_questions_trgm ON public.questions USING gin (statement public.gin_trgm_ops);


--
-- Name: idx_refresh_tokens_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_refresh_tokens_user ON public.refresh_tokens USING btree (user_id, expires_at) WHERE (revoked_at IS NULL);


--
-- Name: idx_review_sq_question; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_review_sq_question ON public.review_session_questions USING btree (question_id);


--
-- Name: idx_review_sq_session; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_review_sq_session ON public.review_session_questions USING btree (study_session_id, "position");


--
-- Name: idx_sessions_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sessions_user ON public.study_sessions USING btree (user_id, started_at DESC);


--
-- Name: idx_sim_attempts_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sim_attempts_user ON public.simulation_attempts USING btree (user_id, started_at DESC);


--
-- Name: uq_qclass_approved; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_qclass_approved ON public.question_classifications USING btree (question_id) WHERE (status = 'APPROVED'::text);


--
-- Name: uq_study_plans_active; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_study_plans_active ON public.study_plans USING btree (user_id) WHERE is_active;


--
-- Name: achievements trg_achievements_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_achievements_updated BEFORE UPDATE ON public.achievements FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: question_attempts trg_attempts_immutable; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_attempts_immutable BEFORE DELETE OR UPDATE ON public.question_attempts FOR EACH ROW EXECUTE FUNCTION public.prevent_attempt_mutation();


--
-- Name: disciplines trg_disciplines_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_disciplines_updated BEFORE UPDATE ON public.disciplines FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: exam_documents trg_exam_documents_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_exam_documents_updated BEFORE UPDATE ON public.exam_documents FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: exam_essay_prompts trg_exam_essay_prompts_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_exam_essay_prompts_updated BEFORE UPDATE ON public.exam_essay_prompts FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: exam_versions trg_exam_versions_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_exam_versions_updated BEFORE UPDATE ON public.exam_versions FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: exams trg_exams_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_exams_updated BEFORE UPDATE ON public.exams FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: question_options trg_four_options; Type: TRIGGER; Schema: public; Owner: -
--

CREATE CONSTRAINT TRIGGER trg_four_options AFTER INSERT OR DELETE ON public.question_options DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION public.enforce_four_options();


--
-- Name: password_reset_tokens trg_password_reset_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_password_reset_updated BEFORE UPDATE ON public.password_reset_tokens FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: student_topic_performance trg_perf_coherence; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_perf_coherence BEFORE INSERT OR UPDATE ON public.student_topic_performance FOR EACH ROW EXECUTE FUNCTION public.check_subtopic_topic();


--
-- Name: study_plan_items trg_plan_item_coherence; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_plan_item_coherence BEFORE INSERT OR UPDATE ON public.study_plan_items FOR EACH ROW EXECUTE FUNCTION public.check_subtopic_topic();


--
-- Name: progress_snapshots trg_progress_snapshots_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_progress_snapshots_updated BEFORE UPDATE ON public.progress_snapshots FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: question_classifications trg_qclass_coherence; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_qclass_coherence BEFORE INSERT OR UPDATE ON public.question_classifications FOR EACH ROW EXECUTE FUNCTION public.check_subtopic_topic();


--
-- Name: question_attempts trg_question_attempts_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_question_attempts_updated BEFORE UPDATE ON public.question_attempts FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: question_classifications trg_question_classifications_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_question_classifications_updated BEFORE UPDATE ON public.question_classifications FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: question_options trg_question_options_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_question_options_updated BEFORE UPDATE ON public.question_options FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: question_sources trg_question_sources_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_question_sources_updated BEFORE UPDATE ON public.question_sources FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: question_tag_map trg_question_tag_map_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_question_tag_map_updated BEFORE UPDATE ON public.question_tag_map FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: question_tags trg_question_tags_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_question_tags_updated BEFORE UPDATE ON public.question_tags FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: questions trg_questions_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_questions_updated BEFORE UPDATE ON public.questions FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: refresh_tokens trg_refresh_tokens_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_refresh_tokens_updated BEFORE UPDATE ON public.refresh_tokens FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: review_session_questions trg_review_session_questions_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_review_session_questions_updated BEFORE UPDATE ON public.review_session_questions FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: roles trg_roles_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_roles_updated BEFORE UPDATE ON public.roles FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: simulation_attempts trg_simulation_attempts_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_simulation_attempts_updated BEFORE UPDATE ON public.simulation_attempts FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: simulation_questions trg_simulation_questions_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_simulation_questions_updated BEFORE UPDATE ON public.simulation_questions FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: simulations trg_simulations_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_simulations_updated BEFORE UPDATE ON public.simulations FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: student_achievements trg_student_achievements_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_student_achievements_updated BEFORE UPDATE ON public.student_achievements FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: student_profiles trg_student_profiles_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_student_profiles_updated BEFORE UPDATE ON public.student_profiles FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: student_topic_performance trg_student_topic_performance_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_student_topic_performance_updated BEFORE UPDATE ON public.student_topic_performance FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: study_plan_items trg_study_plan_items_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_study_plan_items_updated BEFORE UPDATE ON public.study_plan_items FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: study_plans trg_study_plans_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_study_plans_updated BEFORE UPDATE ON public.study_plans FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: study_sessions trg_study_sessions_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_study_sessions_updated BEFORE UPDATE ON public.study_sessions FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: subtopics trg_subtopics_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_subtopics_updated BEFORE UPDATE ON public.subtopics FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: topics trg_topics_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_topics_updated BEFORE UPDATE ON public.topics FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: user_roles trg_user_roles_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_user_roles_updated BEFORE UPDATE ON public.user_roles FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: users trg_users_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_users_updated BEFORE UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: exam_documents exam_documents_exam_version_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_documents
    ADD CONSTRAINT exam_documents_exam_version_id_fkey FOREIGN KEY (exam_version_id) REFERENCES public.exam_versions(id) ON DELETE CASCADE;


--
-- Name: exam_essay_prompts exam_essay_prompts_exam_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_essay_prompts
    ADD CONSTRAINT exam_essay_prompts_exam_id_fkey FOREIGN KEY (exam_id) REFERENCES public.exams(id) ON DELETE CASCADE;


--
-- Name: exam_versions exam_versions_exam_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_versions
    ADD CONSTRAINT exam_versions_exam_id_fkey FOREIGN KEY (exam_id) REFERENCES public.exams(id) ON DELETE CASCADE;


--
-- Name: passages passages_exam_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.passages
    ADD CONSTRAINT passages_exam_id_fkey FOREIGN KEY (exam_id) REFERENCES public.exams(id) ON DELETE CASCADE;


--
-- Name: password_reset_tokens password_reset_tokens_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: progress_snapshots progress_snapshots_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.progress_snapshots
    ADD CONSTRAINT progress_snapshots_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: question_attempts question_attempts_question_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_attempts
    ADD CONSTRAINT question_attempts_question_id_fkey FOREIGN KEY (question_id) REFERENCES public.questions(id) ON DELETE RESTRICT;


--
-- Name: question_attempts question_attempts_simulation_attempt_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_attempts
    ADD CONSTRAINT question_attempts_simulation_attempt_id_fkey FOREIGN KEY (simulation_attempt_id) REFERENCES public.simulation_attempts(id) ON DELETE SET NULL;


--
-- Name: question_attempts question_attempts_study_session_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_attempts
    ADD CONSTRAINT question_attempts_study_session_id_fkey FOREIGN KEY (study_session_id) REFERENCES public.study_sessions(id) ON DELETE SET NULL;


--
-- Name: question_attempts question_attempts_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_attempts
    ADD CONSTRAINT question_attempts_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: question_classifications question_classifications_question_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_classifications
    ADD CONSTRAINT question_classifications_question_id_fkey FOREIGN KEY (question_id) REFERENCES public.questions(id) ON DELETE CASCADE;


--
-- Name: question_classifications question_classifications_reviewed_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_classifications
    ADD CONSTRAINT question_classifications_reviewed_by_fkey FOREIGN KEY (reviewed_by) REFERENCES public.users(id) ON DELETE SET NULL;


--
-- Name: question_classifications question_classifications_subtopic_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_classifications
    ADD CONSTRAINT question_classifications_subtopic_id_fkey FOREIGN KEY (subtopic_id) REFERENCES public.subtopics(id) ON DELETE RESTRICT;


--
-- Name: question_classifications question_classifications_topic_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_classifications
    ADD CONSTRAINT question_classifications_topic_id_fkey FOREIGN KEY (topic_id) REFERENCES public.topics(id) ON DELETE RESTRICT;


--
-- Name: question_figures question_figures_question_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_figures
    ADD CONSTRAINT question_figures_question_id_fkey FOREIGN KEY (question_id) REFERENCES public.questions(id) ON DELETE CASCADE;


--
-- Name: question_options question_options_question_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_options
    ADD CONSTRAINT question_options_question_id_fkey FOREIGN KEY (question_id) REFERENCES public.questions(id) ON DELETE CASCADE;


--
-- Name: question_passages question_passages_passage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_passages
    ADD CONSTRAINT question_passages_passage_id_fkey FOREIGN KEY (passage_id) REFERENCES public.passages(id) ON DELETE CASCADE;


--
-- Name: question_passages question_passages_question_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_passages
    ADD CONSTRAINT question_passages_question_id_fkey FOREIGN KEY (question_id) REFERENCES public.questions(id) ON DELETE CASCADE;


--
-- Name: question_sources question_sources_exam_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_sources
    ADD CONSTRAINT question_sources_exam_document_id_fkey FOREIGN KEY (exam_document_id) REFERENCES public.exam_documents(id) ON DELETE RESTRICT;


--
-- Name: question_sources question_sources_question_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_sources
    ADD CONSTRAINT question_sources_question_id_fkey FOREIGN KEY (question_id) REFERENCES public.questions(id) ON DELETE CASCADE;


--
-- Name: question_tag_map question_tag_map_question_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_tag_map
    ADD CONSTRAINT question_tag_map_question_id_fkey FOREIGN KEY (question_id) REFERENCES public.questions(id) ON DELETE CASCADE;


--
-- Name: question_tag_map question_tag_map_tag_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_tag_map
    ADD CONSTRAINT question_tag_map_tag_id_fkey FOREIGN KEY (tag_id) REFERENCES public.question_tags(id) ON DELETE CASCADE;


--
-- Name: questions questions_discipline_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT questions_discipline_id_fkey FOREIGN KEY (discipline_id) REFERENCES public.disciplines(id) ON DELETE RESTRICT;


--
-- Name: questions questions_exam_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT questions_exam_document_id_fkey FOREIGN KEY (exam_document_id) REFERENCES public.exam_documents(id) ON DELETE RESTRICT;


--
-- Name: questions questions_exam_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT questions_exam_id_fkey FOREIGN KEY (exam_id) REFERENCES public.exams(id) ON DELETE RESTRICT;


--
-- Name: refresh_tokens refresh_tokens_replaced_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_replaced_by_id_fkey FOREIGN KEY (replaced_by_id) REFERENCES public.refresh_tokens(id) ON DELETE SET NULL;


--
-- Name: refresh_tokens refresh_tokens_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: review_session_questions review_session_questions_question_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.review_session_questions
    ADD CONSTRAINT review_session_questions_question_id_fkey FOREIGN KEY (question_id) REFERENCES public.questions(id) ON DELETE RESTRICT;


--
-- Name: review_session_questions review_session_questions_study_session_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.review_session_questions
    ADD CONSTRAINT review_session_questions_study_session_id_fkey FOREIGN KEY (study_session_id) REFERENCES public.study_sessions(id) ON DELETE CASCADE;


--
-- Name: simulation_attempts simulation_attempts_simulation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simulation_attempts
    ADD CONSTRAINT simulation_attempts_simulation_id_fkey FOREIGN KEY (simulation_id) REFERENCES public.simulations(id) ON DELETE RESTRICT;


--
-- Name: simulation_attempts simulation_attempts_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simulation_attempts
    ADD CONSTRAINT simulation_attempts_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: simulation_questions simulation_questions_question_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simulation_questions
    ADD CONSTRAINT simulation_questions_question_id_fkey FOREIGN KEY (question_id) REFERENCES public.questions(id) ON DELETE RESTRICT;


--
-- Name: simulation_questions simulation_questions_simulation_attempt_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simulation_questions
    ADD CONSTRAINT simulation_questions_simulation_attempt_id_fkey FOREIGN KEY (simulation_attempt_id) REFERENCES public.simulation_attempts(id) ON DELETE CASCADE;


--
-- Name: simulations simulations_exam_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simulations
    ADD CONSTRAINT simulations_exam_id_fkey FOREIGN KEY (exam_id) REFERENCES public.exams(id) ON DELETE RESTRICT;


--
-- Name: simulations simulations_owner_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simulations
    ADD CONSTRAINT simulations_owner_user_id_fkey FOREIGN KEY (owner_user_id) REFERENCES public.users(id) ON DELETE SET NULL;


--
-- Name: student_achievements student_achievements_achievement_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_achievements
    ADD CONSTRAINT student_achievements_achievement_id_fkey FOREIGN KEY (achievement_id) REFERENCES public.achievements(id) ON DELETE CASCADE;


--
-- Name: student_achievements student_achievements_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_achievements
    ADD CONSTRAINT student_achievements_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: student_profiles student_profiles_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_profiles
    ADD CONSTRAINT student_profiles_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: student_topic_performance student_topic_performance_subtopic_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_topic_performance
    ADD CONSTRAINT student_topic_performance_subtopic_id_fkey FOREIGN KEY (subtopic_id) REFERENCES public.subtopics(id) ON DELETE RESTRICT;


--
-- Name: student_topic_performance student_topic_performance_topic_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_topic_performance
    ADD CONSTRAINT student_topic_performance_topic_id_fkey FOREIGN KEY (topic_id) REFERENCES public.topics(id) ON DELETE RESTRICT;


--
-- Name: student_topic_performance student_topic_performance_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_topic_performance
    ADD CONSTRAINT student_topic_performance_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: study_plan_items study_plan_items_study_plan_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.study_plan_items
    ADD CONSTRAINT study_plan_items_study_plan_id_fkey FOREIGN KEY (study_plan_id) REFERENCES public.study_plans(id) ON DELETE CASCADE;


--
-- Name: study_plan_items study_plan_items_subtopic_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.study_plan_items
    ADD CONSTRAINT study_plan_items_subtopic_id_fkey FOREIGN KEY (subtopic_id) REFERENCES public.subtopics(id) ON DELETE RESTRICT;


--
-- Name: study_plan_items study_plan_items_topic_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.study_plan_items
    ADD CONSTRAINT study_plan_items_topic_id_fkey FOREIGN KEY (topic_id) REFERENCES public.topics(id) ON DELETE RESTRICT;


--
-- Name: study_plans study_plans_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.study_plans
    ADD CONSTRAINT study_plans_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: study_sessions study_sessions_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.study_sessions
    ADD CONSTRAINT study_sessions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: subtopics subtopics_topic_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subtopics
    ADD CONSTRAINT subtopics_topic_id_fkey FOREIGN KEY (topic_id) REFERENCES public.topics(id) ON DELETE RESTRICT;


--
-- Name: topics topics_discipline_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.topics
    ADD CONSTRAINT topics_discipline_id_fkey FOREIGN KEY (discipline_id) REFERENCES public.disciplines(id) ON DELETE RESTRICT;


--
-- Name: user_roles user_roles_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id) ON DELETE CASCADE;


--
-- Name: user_roles user_roles_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict rNbBmzDbAeMQbOj7jpfbZfgRLXC1gmpXFaAOI4zfMKce59SKhAt3ymSgRua7EzA

