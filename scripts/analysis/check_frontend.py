#!/usr/bin/env python3
"""Validador do frontend — TASKs 6.1 (design system) + 6.2 (landing) + 6.3 (auth) + 6.4 (dashboard) + 6.5 (estudos) + 6.6 (questão) + 6.7 (simulado) + 6.8 (perfil) + 12.1 (admin).

Verifica `frontend/` sem rodar navegador:
  - arquivos obrigatórios existem;
  - tokens cobrem cor/tipo/espaço/raio/foco/toque;
  - componentes cobrem os 13 itens da TASK 6.1;
  - HTML com lang/viewport/skip-link/tema;
  - JS sem innerHTML com dados, sem URL de API fora de config.js;
  - sem segredos no frontend;
  - design-system.html cobre todas as seções;
  - index.html (landing 6.2 full-screen) cobre propósito/confiança/acesso
    em mensagem genérica, sem inventar oferta (evidência do acervo vive
    no app/admin/docs);
  - páginas de auth 6.3 (login/cadastro/recuperação/redefinição) com
    formulários, links entre si e a partir da landing.

Somente leitura. Sai com código != 0 em erro.

Uso:
    python3 scripts/analysis/check_frontend.py
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
FRONT = REPO_ROOT / "frontend"

REQUIRED_FILES = [
    "index.html",
    "design-system.html",
    "login.html",
    "cadastro.html",
    "recuperar-senha.html",
    "redefinir-senha.html",
    "dashboard.html",
    "estudos.html",
    "questao.html",
    "simulado.html",
    "perfil.html",
    "admin.html",
    "css/tokens.css",
    "css/base.css",
    "css/components.css",
    "css/utilities.css",
    "css/landing.css",
    "css/auth.css",
    "css/dashboard.css",
    "css/estudos.css",
    "css/questao.css",
    "css/simulado.css",
    "css/perfil.css",
    "css/admin.css",
    "js/config.js",
    "js/api/client.js",
    "js/api/auth.js",
    "js/api/admin.js",
    "js/api/dashboard.js",
    "js/api/estudos.js",
    "js/api/questao.js",
    "js/api/simulado.js",
    "js/api/perfil.js",
    "js/state/store.js",
    "js/state/session.js",
    "js/components/ui.js",
    "js/components/figure.js",
    "js/components/passage.js",
    "js/main.js",
    "js/views/auth-shared.js",
    "js/views/login.js",
    "js/views/cadastro.js",
    "js/views/recuperar-senha.js",
    "js/views/redefinir-senha.js",
    "js/views/dashboard.js",
    "js/views/estudos.js",
    "js/views/questao.js",
    "js/views/simulado.js",
    "js/views/perfil.js",
    "js/views/admin.js",
    "assets/favicon.svg",
    "assets/figures/manifest.json",
    "assets/landing/desk.webp",
    "assets/landing/hero.webp",
    "assets/landing/numbers.webp",
    "assets/landing/simulation.webp",
    "assets/landing/students.webp",
    "assets/landing/vector.webp",
]

REQUIRED_TOKENS = [
    "--color-primary-900", "--color-primary-700",
    "--color-ink-900", "--color-ink-500",
    "--color-success-700", "--color-danger-700",
    "--color-warning-700", "--color-info-700",
    "--font-sans", "--text-md", "--leading-body",
    "--space-1", "--space-4", "--space-8",
    "--radius-sm", "--radius-md", "--focus-ring",
    "--touch-min", "--container-max",
]

REQUIRED_COMPONENTS = [
    ".btn", ".btn--primary", ".btn--secondary", ".btn--ghost", ".btn--danger",
    ".field", ".input", ".select", ".textarea", ".field__error",
    ".card", ".card__header", ".card__body", ".card__footer",
    ".table", ".badge", ".badge--success", ".badge--danger",
    ".alert", ".alert--success", ".alert--danger",
    ".modal",     ".empty", ".spinner", ".skeleton", ".progress",
    ".qfigure", ".qfigure__img", ".qfigure__caption",
    ".passage", ".passage__body", ".passage__para",
    ".error-summary",
]

REQUIRED_SECTIONS = [
    "tokens", "tipografia", "botoes", "formularios", "cards", "tabelas",
    "badges", "feedback", "modais", "vazios", "loading", "questao", "js",
]

# TASK 6.2 — landing: hero navy preservado + seções claras abaixo que
# explicam o produto (o-que-e / como-funciona / como-praticar /
# de-onde-vem / cta-final). Mensagem genérica por decisão de produto;
# o validador exige o hero completo + as seções + honestidade.
REQUIRED_LANDING_SECTIONS = [
    "hero-t", "hero-brand", "hero-visual",
    "o-que-e", "como-funciona", "como-praticar",
    "de-onde-vem", "cta-final",
]

# TASK 6.3 — páginas de autenticação: arquivo → id do form exigido
REQUIRED_AUTH_FORMS = {
    "login.html": "login-form",
    "cadastro.html": "register-form",
    "recuperar-senha.html": "forgot-form",
    "redefinir-senha.html": "reset-form",
}

# Termos que inventariam oferta/garantia (AGENTS.md §4 — nunca inventar)
FORBIDDEN_LANDING_CLAIMS = [
    "grátis", "gratis", "gratuito", "garantia de aprovação",
    "garantimos", "milhares de questões", "todas as edições",
]

errors: list[str] = []


def fail(msg: str) -> None:
    errors.append(msg)


def main() -> int:
    for f in REQUIRED_FILES:
        if not (FRONT / f).is_file():
            fail(f"ausente: frontend/{f}")

    tokens = (FRONT / "css" / "tokens.css").read_text(encoding="utf-8") if (FRONT / "css" / "tokens.css").exists() else ""
    for t in REQUIRED_TOKENS:
        if t not in tokens:
            fail(f"token ausente em tokens.css: {t}")

    comp = (FRONT / "css" / "components.css").read_text(encoding="utf-8") if (FRONT / "css" / "components.css").exists() else ""
    for c in REQUIRED_COMPONENTS:
        if c not in comp:
            fail(f"componente ausente em components.css: {c}")

    base = (FRONT / "css" / "base.css").read_text(encoding="utf-8") if (FRONT / "css" / "base.css").exists() else ""
    if "prefers-reduced-motion" not in base:
        fail("base.css sem prefers-reduced-motion")
    if ":focus-visible" not in base:
        fail("base.css sem :focus-visible")

    for page in ["index.html", "design-system.html", "dashboard.html", "questao.html", "simulado.html", "perfil.html", "admin.html", *REQUIRED_AUTH_FORMS]:
        p = FRONT / page
        if not p.is_file():
            continue
        html = p.read_text(encoding="utf-8")
        if 'lang="pt-BR"' not in html:
            fail(f"{page} sem lang=\"pt-BR\"")
        if 'name="viewport"' not in html:
            fail(f"{page} sem meta viewport")
        if "skip-link" not in html:
            fail(f"{page} sem skip-link")
        if 'type="module"' not in html:
            fail(f"{page} sem script type=\"module\"")

    ds = (FRONT / "design-system.html").read_text(encoding="utf-8") if (FRONT / "design-system.html").exists() else ""
    for s in REQUIRED_SECTIONS:
        if f'id="{s}"' not in ds:
            fail(f"design-system.html sem seção #{s}")

    # TASK 6.2 — landing: mensagem genérica por decisão de produto (a
    # plataforma pode crescer além de um exame específico). A honestidade
    # do acervo (2021 ausente, 240 questões, anuladas) continua exigida
    # nas telas do app (estudos/simulado), na área admin e em docs/.
    landing = (FRONT / "index.html").read_text(encoding="utf-8") if (FRONT / "index.html").exists() else ""
    if landing:
        for s in REQUIRED_LANDING_SECTIONS:
            if f'id="{s}"' not in landing:
                fail(f"index.html (landing) sem bloco #{s}")
        if 'href="./cadastro.html"' not in landing:
            fail("index.html sem CTA para cadastro (direcionar cadastro/login)")
        if 'href="./login.html"' not in landing:
            fail("index.html sem CTA para login (direcionar cadastro/login)")
        lowered = landing.lower()
        for claim in FORBIDDEN_LANDING_CLAIMS:
            if claim in lowered:
                fail(f"index.html com afirmação não comprovada: {claim!r}")

    # TASK 6.3 — auth: forms, interligação e sem placeholder vencido
    for page, form_id in REQUIRED_AUTH_FORMS.items():
        p = FRONT / page
        if not p.is_file():
            continue
        html = p.read_text(encoding="utf-8")
        if f'id="{form_id}"' not in html:
            fail(f"{page} sem formulário #{form_id}")
        if 'type="password"' not in html and page != "recuperar-senha.html":
            fail(f"{page} sem campo de senha")
        if "voupassar-api" not in html:
            fail(f"{page} sem meta voupassar-api (config.js)")
        if "css/auth.css" not in html:
            fail(f"{page} sem css/auth.css")
    login_html = (FRONT / "login.html").read_text(encoding="utf-8") if (FRONT / "login.html").exists() else ""
    if login_html:
        if "cadastro.html" not in login_html or "recuperar-senha.html" not in login_html:
            fail("login.html sem links para cadastro/recuperação")
    reset_html = (FRONT / "redefinir-senha.html").read_text(encoding="utf-8") if (FRONT / "redefinir-senha.html").exists() else ""
    if reset_html and "token" not in reset_html.lower():
        fail("redefinir-senha.html sem campo de token")
    if landing:
        if "cadastro.html" not in landing or "login.html" not in landing:
            fail("index.html sem links reais para cadastro/login (TASK 6.3)")
        if "Telas chegam na TASK 6.3" in landing:
            fail("index.html com placeholder vencido da TASK 6.3")

    # TASK 6.4 — dashboard: blocos + guarda de auth + sem placeholder vencido
    # O bloco "dash-notes" (notas técnicas de auditoria) foi removido das
    # telas de aluno por decisão de produto: é documentação interna, não
    # conteúdo de estudo. A honestidade do projeto continua na landing,
    # na área administrativa e em docs/. Não reintroduzir o bloco aqui.
    dash = (FRONT / "dashboard.html").read_text(encoding="utf-8") if (FRONT / "dashboard.html").exists() else ""
    if dash:
        for sid in ["dash-guard", "dash-error", "dash-loading", "dash-content",
                    "dash-stats", "dash-disciplines", "dash-priorities",
                    "dash-next", "dash-plan", "dash-evolution",
                    "dash-simulations", "evo-granularity",
                    "plan-generate", "plan-regenerate"]:
            if f'id="{sid}"' not in dash:
                fail(f"dashboard.html sem bloco #{sid} (TASK 6.4)")
        if "css/dashboard.css" not in dash:
            fail("dashboard.html sem css/dashboard.css")
        if "js/views/dashboard.js" not in dash:
            fail("dashboard.html sem js/views/dashboard.js")
        if "chega na TASK 6.4" in dash:
            fail("dashboard.html com placeholder vencido da TASK 6.4")
        for vid in ["login.js", "cadastro.js"]:
            v = (FRONT / "js" / "views" / vid).read_text(encoding="utf-8") if (FRONT / "js" / "views" / vid).exists() else ""
            if v and "dashboard.html" not in v:
                fail(f"js/views/{vid} sem link para o dashboard (TASK 6.4)")
        shared = (FRONT / "js" / "views" / "auth-shared.js").read_text(encoding="utf-8") if (FRONT / "js" / "views" / "auth-shared.js").exists() else ""
        if shared and "dashboard.html" not in shared:
            fail("js/views/auth-shared.js sem link para o dashboard (TASK 6.4)")

    # TASK 6.5 — estudos: conteúdo + filtros + questões + progresso + navegação
    study = (FRONT / "estudos.html").read_text(encoding="utf-8") if (FRONT / "estudos.html").exists() else ""
    if study:
        for sid in ["study-guard", "study-error", "study-loading", "study-content",
                    "study-filters", "f-disciplina", "f-topico", "f-subtopico",
                    "f-ano", "f-dificuldade", "study-browser", "study-list",
                    "study-pagination", "study-progress", "study-plan",
                    "study-count", "btn-clear", "btn-filter"]:
            if f'id="{sid}"' not in study:
                fail(f"estudos.html sem bloco #{sid} (TASK 6.5)")
        if "css/estudos.css" not in study:
            fail("estudos.html sem css/estudos.css")
        if "js/views/estudos.js" not in study:
            fail("estudos.html sem js/views/estudos.js")
        study_js = (FRONT / "js" / "views" / "estudos.js").read_text(encoding="utf-8") if (FRONT / "js" / "views" / "estudos.js").exists() else ""
        if "2021" not in study and "2021" not in study_js:
            fail("estudos (html/js) omite a ausência de 2021 (AGENTS.md §3)")
        if "estudos.html" not in dash:
            fail("dashboard.html sem link para estudos.html (TASK 6.5)")

    # TASK 6.6 — questão dedicada: leitura + seleção + confirmação + feedback (acerto/erro + gabarito + assunto; sem passo a passo)
    questao = (FRONT / "questao.html").read_text(encoding="utf-8") if (FRONT / "questao.html").exists() else ""
    if questao:
        for sid in ["questao-guard", "questao-error", "questao-loading", "questao-content",
                    "questao-back", "questao-title", "questao-meta", "questao-badges",
                    "questao-statement", "questao-passages", "questao-figure", "questao-topic",
                    "questao-form", "questao-options", "questao-hint",
                    "questao-submit", "questao-blank", "questao-reset",
                    "questao-feedback", "questao-feedback-empty",
                    "questao-source"]:
            if f'id="{sid}"' not in questao:
                fail(f"questao.html sem bloco #{sid} (TASK 6.6)")
        if "css/questao.css" not in questao:
            fail("questao.html sem css/questao.css")
        if "js/views/questao.js" not in questao:
            fail("questao.html sem js/views/questao.js")
        if "voupassar-api" not in questao:
            fail("questao.html sem meta voupassar-api (config.js)")
        questao_js = (FRONT / "js" / "views" / "questao.js").read_text(encoding="utf-8") if (FRONT / "js" / "views" / "questao.js").exists() else ""
        for token in ["fetchQuestion", "submitAttempt", "openStudySession", "BLANK", "NECESSITA REVISÃO", "voltar", "renderPassages"]:
            if token not in questao_js:
                fail(f"js/views/questao.js sem {token!r} (TASK 6.6)")
        questao_api = (FRONT / "js" / "api" / "questao.js").read_text(encoding="utf-8") if (FRONT / "js" / "api" / "questao.js").exists() else ""
        if "/api/v1/questions" not in questao_api or "/api/v1/attempts" not in questao_api:
            fail("js/api/questao.js sem endpoints de questão/tentativa (TASK 6.6)")
        study_js = (FRONT / "js" / "views" / "estudos.js").read_text(encoding="utf-8") if (FRONT / "js" / "views" / "estudos.js").exists() else ""
        if "questao.html" not in study_js:
            fail("js/views/estudos.js sem link para questao.html (TASK 6.6)")

    # TASK 6.7 — simulado: hub (criar por disciplina/edição + histórico) + execução (caderno + encerrar + resultado)
    sim = (FRONT / "simulado.html").read_text(encoding="utf-8") if (FRONT / "simulado.html").exists() else ""
    if sim:
        for sid in ["sim-guard", "sim-error", "sim-loading", "sim-content",
                    "sim-hub", "sim-create-discipline", "s-disc-disciplina",
                    "s-disc-qtd", "s-disc-dificuldade", "s-disc-modo",
                    "sim-disc-submit", "sim-create-edition", "s-ed-edicao",
                    "s-ed-modo", "sim-ed-submit", "sim-history",
                    "sim-history-count", "sim-more", "sim-exec", "sim-back",
                    "sim-mode-badge", "sim-exec-title", "sim-exec-meta",
                    "sim-exec-badges", "sim-progress-text", "sim-progress-bar",
                    "sim-progress-fill", "sim-hidden-note", "sim-questions",
                    "sim-submit", "sim-abandon", "sim-result-section",
                    "sim-result", "sim-confirm", "sim-confirm-text",
                    "sim-confirm-yes", "sim-confirm-no"]:
            if f'id="{sid}"' not in sim:
                fail(f"simulado.html sem bloco #{sid} (TASK 6.7)")
        if "css/simulado.css" not in sim:
            fail("simulado.html sem css/simulado.css")
        if "js/views/simulado.js" not in sim:
            fail("simulado.html sem js/views/simulado.js")
        if "voupassar-api" not in sim:
            fail("simulado.html sem meta voupassar-api (config.js)")
        if "2021" not in sim:
            fail("simulado.html omite a ausência de 2021 (AGENTS.md §3)")
        sim_js = (FRONT / "js" / "views" / "simulado.js").read_text(encoding="utf-8") if (FRONT / "js" / "views" / "simulado.js").exists() else ""
        for token in ["createByDiscipline", "createByEdition", "fetchAttempt",
                      "submitSimulation", "abandonSimulation", "fetchResult",
                      "fetchFeedback", "simulationAttemptId", "2021"]:
            if token not in sim_js:
                fail(f"js/views/simulado.js sem {token!r} (TASK 6.7)")
        sim_api = (FRONT / "js" / "api" / "simulado.js").read_text(encoding="utf-8") if (FRONT / "js" / "api" / "simulado.js").exists() else ""
        for token in ["/api/v1/simulations/by-discipline", "/api/v1/simulations/by-edition",
                      "/api/v1/simulations/attempts", "/feedback/", "/api/v1/attempts",
                      "/api/v1/questions", "/api/v1/disciplines", "/api/v1/editions"]:
            if token not in sim_api:
                fail(f"js/api/simulado.js sem endpoint {token!r} (TASK 6.7)")
        for page, label in [("dashboard.html", "dashboard"), ("estudos.html", "estudos"), ("questao.html", "questao")]:
            nav = (FRONT / page).read_text(encoding="utf-8") if (FRONT / page).exists() else ""
            if nav and "simulado.html" not in nav:
                fail(f"{page} sem link para simulado.html (TASK 6.7)")
        dash_js = (FRONT / "js" / "views" / "dashboard.js").read_text(encoding="utf-8") if (FRONT / "js" / "views" / "dashboard.js").exists() else ""
        if "As telas de prova chegam" in dash_js:
            fail("js/views/dashboard.js com placeholder vencido da TASK 6.7")

    # TASK 6.8 — perfil: dados + estatísticas + evolução + histórico +
    # dominados + atenção + metas + conquistas (Fase 7 pendente honesto)
    perfil = (FRONT / "perfil.html").read_text(encoding="utf-8") if (FRONT / "perfil.html").exists() else ""
    if perfil:
        for sid in ["perfil-guard", "perfil-error", "perfil-loading", "perfil-content",
                    "perfil-dados", "perfil-form", "pf-nome", "pf-ano",
                    "pf-alvo", "pf-objetivo", "perfil-save",
                    "perfil-stats", "perfil-modes", "perfil-disciplines",
                    "perfil-metas", "perfil-strengths", "perfil-weaknesses",
                    "evo-granularity", "perfil-evolution",
                    "perfil-history-count", "perfil-history", "perfil-more",
                    "perfil-achievements"]:
            if f'id="{sid}"' not in perfil:
                fail(f"perfil.html sem bloco #{sid} (TASK 6.8)")
        if "css/perfil.css" not in perfil:
            fail("perfil.html sem css/perfil.css")
        if "js/views/perfil.js" not in perfil:
            fail("perfil.html sem js/views/perfil.js")
        if "voupassar-api" not in perfil:
            fail("perfil.html sem meta voupassar-api (config.js)")
        perfil_js = (FRONT / "js" / "views" / "perfil.js").read_text(encoding="utf-8") if (FRONT / "js" / "views" / "perfil.js").exists() else ""
        for token in ["fetchProfile", "updateProfile", "fetchProfileStats",
                      "fetchHistory", "fetchDiagnosis", "fetchEvolution",
                      "perfil-more", "Conquistas em construção"]:
            if token not in perfil_js:
                fail(f"js/views/perfil.js sem {token!r} (TASK 6.8)")
        # A tela de perfil não pode expor identificadores de tarefa nem nomes de
        # tabela/coluna da API no texto que o aluno lê. O check exigia a
        # string "Fase 7", mas ela sobrevivia apenas em comentário de arquivo,
        # o que tornava a verificação enganosa; agora checamos o texto real.
        for leak in ["TASKs 7.1", "question_attempts", "discipline_id"]:
            if leak in perfil_js:
                fail(f"js/views/perfil.js expõe detalhe interno {leak!r} ao aluno")
        perfil_api = (FRONT / "js" / "api" / "perfil.js").read_text(encoding="utf-8") if (FRONT / "js" / "api" / "perfil.js").exists() else ""
        for token in ["/api/v1/profile", "/api/v1/profile/stats",
                      "/api/v1/profile/history", "/api/v1/diagnosis",
                      "/api/v1/performance/evolution", "/api/v1/performance/overview"]:
            if token not in perfil_api:
                fail(f"js/api/perfil.js sem endpoint {token!r} (TASK 6.8)")
        for page in ["dashboard.html", "estudos.html", "questao.html", "simulado.html"]:
            nav = (FRONT / page).read_text(encoding="utf-8") if (FRONT / page).exists() else ""
            if nav and "perfil.html" not in nav:
                fail(f"{page} sem link para perfil.html (TASK 6.8)")

    # TASK 12.1 — admin: métricas + inconsistências + fila + curadoria (CURATOR/ADMIN)
    admin = (FRONT / "admin.html").read_text(encoding="utf-8") if (FRONT / "admin.html").exists() else ""
    if admin:
        for sid in ["admin-guard", "admin-forbidden", "admin-error", "admin-loading", "admin-content",
                    "admin-metrics", "admin-metrics-tables", "admin-inconsistencies",
                    "admin-filters", "f-validation", "btn-filter", "btn-clear",
                    "admin-queue-count", "admin-queue", "admin-prev", "admin-next",
                    "admin-page-info", "admin-notes"]:
            if f'id="{sid}"' not in admin:
                fail(f"admin.html sem bloco #{sid} (TASK 12.1)")
        if "css/admin.css" not in admin:
            fail("admin.html sem css/admin.css")
        if "js/views/admin.js" not in admin:
            fail("admin.html sem js/views/admin.js")
        if "voupassar-api" not in admin:
            fail("admin.html sem meta voupassar-api (config.js)")
        if "CURATOR" not in admin:
            fail("admin.html sem menção a CURATOR/ADMIN (guarda de papel, TASK 12.1)")
        admin_js = (FRONT / "js" / "views" / "admin.js").read_text(encoding="utf-8") if (FRONT / "js" / "views" / "admin.js").exists() else ""
        for token in ["fetchReviewQueue", "fetchInconsistencies", "fetchAdminMetrics",
                      "updateQuestionStatus", "reviewClassification", "next=admin.html",
                      "CURATOR", "ADMIN", "questao.html?id="]:
            if token not in admin_js:
                fail(f"js/views/admin.js sem {token!r} (TASK 12.1)")
        admin_api = (FRONT / "js" / "api" / "admin.js").read_text(encoding="utf-8") if (FRONT / "js" / "api" / "admin.js").exists() else ""
        for token in ["/api/v1/admin/review-queue", "/api/v1/admin/inconsistencies",
                      "/api/v1/admin/metrics", "/api/v1/admin/questions",
                      "/api/v1/admin/classifications"]:
            if token not in admin_api:
                fail(f"js/api/admin.js sem endpoint {token!r} (TASK 12.1)")

    # JS: sem innerHTML; URL de API só em config.js/meta
    for js in (FRONT / "js").rglob("*.js"):
        src = js.read_text(encoding="utf-8")
        code = "\n".join(
            line for line in src.splitlines()
            if not line.strip().startswith(("*", "//"))
        )
        if re.search(r"\.innerHTML\s*=", code):
            fail(f"{js.relative_to(REPO_ROOT)} usa innerHTML (usar textContent)")
        if js.name != "config.js" and re.search(r"https?://[^\s\"']+", src):
            fail(f"{js.relative_to(REPO_ROOT)} com URL absoluta (centralizar em config.js)")
        if re.search(r"localhost:\d+", src) and js.name != "config.js":
            fail(f"{js.relative_to(REPO_ROOT)} com localhost hardcoded")

    # Telas do aluno não podem mostrar detalhe interno do projeto. O bloco
    # "Leitura honesta" foi removido por decisão de produto (é documentação do
    # motor, não conteúdo de estudo); a honestidade do AGENTS.md §4 continua
    # expressa em português natural e na área administrativa.
    # A área admin fica de fora de propósito: lá os códigos são o vocabulário
    # de trabalho de quem opera a curadoria.
    ALUNO_HTML = ["index.html", "login.html", "cadastro.html",
                  "dashboard.html", "estudos.html", "questao.html",
                  "simulado.html", "perfil.html",
                  "recuperar-senha.html", "redefinir-senha.html"]
    VAZAMENTOS_HTML = [
        "question_attempts", "discipline_id", "v1-deterministico",
        "taxonomia v1.1", "TASKs ", "TASK ", "sessionStorage",
        "localStorage", "bcrypt",
    ]
    for name in ALUNO_HTML:
        f = FRONT / name
        if not f.exists():
            continue
        src = f.read_text(encoding="utf-8")
        # O script inline do <head> usa localStorage para guardar a preferência
        # de tema; esse uso é legítimo e fica excluído da verificação.
        src = re.sub(r"<script>.*?</script>", "", src, flags=re.S)
        for token in VAZAMENTOS_HTML:
            if token in src:
                fail(f"{name} expõe detalhe interno {token!r} ao aluno")

    ALUNO_JS = ["dashboard.js", "estudos.js", "questao.js",
                "simulado.js", "perfil.js"]
    # Bug real que já ocorreu: interpolar direto no texto um campo enum da
    # API, fazendo "FACIL", "PROVA" ou "LP" aparecerem na tela. A tradução
    # mora em js/vocab.js — aqui na view o valor tem que passar por ela.
    # (Nomes de tabela/coluna e identificadores de task continuam banidos.)
    INTERPOLACAO_ENUM = re.compile(
        r"\$\{[^}]*\.(sourceType|difficultyEstimate|classificationStatus|"
        r"publicationStatus|classificationConfidence|disciplineCode|mode|"
        r"sourceDifficulty)[^}]*\}"
    )
    TRADUTORES = (
        "disciplineLabel", "difficultyLabel", "modeLabel", "sourceTypeLabel",
        "statusLabel", "masteryLabel", "classificationLabel",
        "confidenceLabel", "publicationLabel", "topicLabel",
    )
    VAZAMENTOS_JS = [
        "question_attempts", "discipline_id", "TASKs ",
    ]
    # As `notes` que a API devolve são trilha de auditoria do servidor
    # ("curadoria TASK 12.2", "NECESSITA REVISÃO", "regra DESCONHECIDA").
    # Já vazaram para a tela do aluno renderizando q.notes / attempt.notes;
    # a verificação estática não pega isso, então a regra é explícita.
    NOTAS_AUDITORIA = re.compile(
        r"(q|attempt|fb|res|overview|diagnosis|stats|item|question)\??\.notes"
    )
    for name in ALUNO_JS:
        f = FRONT / "js" / "views" / name
        if not f.exists():
            continue
        code = "\n".join(
            line for line in f.read_text(encoding="utf-8").splitlines()
            if not line.strip().startswith(("*", "//"))
        )
        for token in VAZAMENTOS_JS:
            if token in code:
                fail(f"js/views/{name} expõe detalhe interno {token!r} ao aluno")
        hit = NOTAS_AUDITORIA.search(code)
        if hit:
            fail(
                f"js/views/{name} renderiza notas de auditoria da API "
                f"({hit.group(0)}) na tela do aluno"
            )
        for expr in (m.group(0) for m in INTERPOLACAO_ENUM.finditer(code)):
            if any(t in expr for t in TRADUTORES):
                continue  # já passa pelo vocabulário
            fail(
                f"js/views/{name} interpola enum cru da API no texto "
                f"({expr}) — traduzir via js/vocab.js"
            )

    for html_file in [FRONT / "index.html", FRONT / "design-system.html",
                      FRONT / "dashboard.html", FRONT / "estudos.html",
                      FRONT / "questao.html", FRONT / "simulado.html",
                      FRONT / "perfil.html", FRONT / "admin.html",
                      *(FRONT / p for p in REQUIRED_AUTH_FORMS)]:
        if not html_file.is_file():
            continue
        html = html_file.read_text(encoding="utf-8")
        for secret in ["JWT_SECRET", "POSTGRES_PASSWORD", "CHANGE_ME"]:
            if secret in html:
                fail(f"{html_file.name} contém possível segredo: {secret}")

    if errors:
        print(f"check_frontend: {len(errors)} falha(s):")
        for e in errors:
            print(f"  - {e}")
        return 1
    print(f"check_frontend: OK ({len(REQUIRED_FILES)} arquivos, "
          f"{len(REQUIRED_TOKENS)} tokens, {len(REQUIRED_COMPONENTS)} componentes, "
          f"{len(REQUIRED_SECTIONS)} seções do DS, "
          f"{len(REQUIRED_LANDING_SECTIONS)} seções da landing, "
          f"{len(REQUIRED_AUTH_FORMS)} páginas de auth)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
