# =============================================================================
# Sangho SDK Java — Makefile
# =============================================================================
# Usage : make <target>
# Prérequis : JDK 21+, Maven, git
# =============================================================================

.DEFAULT_GOAL := help
.PHONY: help install compile test test-class test-coverage test-integration \
        lint package package-sources build clean \
        version-patch version-minor version-major changelog \
        publish-local publish release-patch release-minor release-major \
        _bump _git-tag-and-push info

# Détection automatique Windows vs Unix
ifeq ($(OS),Windows_NT)
    MVN := mvn.cmd
    RM  := rmdir /s /q
else
    MVN := mvn
    RM  := rm -rf
endif

RESET  := \033[0m
BOLD   := \033[1m
GREEN  := \033[32m
YELLOW := \033[33m
CYAN   := \033[36m

VERSION := $(shell $(MVN) -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec 2>/dev/null || echo "0.0.0")

# -----------------------------------------------------------------------------
# AIDE
# -----------------------------------------------------------------------------
help: ## Affiche cette aide
	@echo ""
	@echo "$(BOLD)$(CYAN)Sangho SDK Java$(RESET)"
	@echo "$(CYAN)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━$(RESET)"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-20s$(RESET) %s\n", $$1, $$2}'
	@echo ""

# -----------------------------------------------------------------------------
# INSTALLATION / BUILD
# -----------------------------------------------------------------------------
install: ## Résout les dépendances (sans build)
	$(MVN) dependency:resolve dependency:resolve-plugins

compile: ## Compile les sources
	$(MVN) compile

# -----------------------------------------------------------------------------
# TESTS
# -----------------------------------------------------------------------------
test: ## Lance les tests unitaires (exclut @Tag("integration"), cf. pom.xml)
	$(MVN) test

test-class: ## Lance une classe de test précise (make test-class CLASS=CustomersResourceTest)
	$(MVN) test -Dtest=$(CLASS)

test-coverage: ## Lance les tests avec rapport de couverture JaCoCo
	@echo "$(CYAN)→ Tests + couverture...$(RESET)"
	$(MVN) verify
	@echo "$(GREEN)✓ Rapport généré dans target/site/jacoco/index.html$(RESET)"

test-integration: ## Tests intégration sandbox (nécessite SANGHO_API_KEY)
	@echo "$(CYAN)→ Tests intégration sandbox...$(RESET)"
	$(MVN) test -Pintegration
	@echo "$(GREEN)✓ Tests intégration OK$(RESET)"

# -----------------------------------------------------------------------------
# QUALITÉ DU CODE
# -----------------------------------------------------------------------------
lint: ## Vérifie la compilation stricte (warnings inclus)
	$(MVN) compile -Dmaven.compiler.showWarnings=true

# -----------------------------------------------------------------------------
# PACKAGE
# -----------------------------------------------------------------------------
package: ## Package le jar (sans tests)
	$(MVN) package -DskipTests

package-sources: ## Package jar + sources
	$(MVN) package source:jar -DskipTests

build: clean test package ## Build complet (tests + package)
	@echo "$(GREEN)✓ Build terminé$(RESET)"
	@ls -lh target/*.jar 2>/dev/null || true

# -----------------------------------------------------------------------------
# NETTOYAGE
# -----------------------------------------------------------------------------
clean: ## Nettoie target/
	$(MVN) clean

# -----------------------------------------------------------------------------
# VERSIONING (Semantic Versioning)
# -----------------------------------------------------------------------------
version-patch: test ## Bump patch version (1.1.0 → 1.1.1)
	@$(MAKE) _bump PART=patch

version-minor: test ## Bump minor version (1.1.0 → 1.2.0)
	@$(MAKE) _bump PART=minor

version-major: test ## Bump major version (1.1.0 → 2.0.0)
	@$(MAKE) _bump PART=major

_bump: ## (Interne) Bump la version dans pom.xml + HttpClient.SDK_VERSION
	@echo "$(CYAN)→ Bump $(PART)...$(RESET)"
	@CURRENT=$$($(MVN) -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec); \
	MAJOR=$$(echo $$CURRENT | cut -d. -f1); \
	MINOR=$$(echo $$CURRENT | cut -d. -f2); \
	PATCH=$$(echo $$CURRENT | cut -d. -f3); \
	case "$(PART)" in \
		major) NEW="$$((MAJOR+1)).0.0" ;; \
		minor) NEW="$$MAJOR.$$((MINOR+1)).0" ;; \
		*)     NEW="$$MAJOR.$$MINOR.$$((PATCH+1))" ;; \
	esac; \
	$(MVN) -q versions:set -DnewVersion=$$NEW -DgenerateBackupPoms=false; \
	sed -i.bak "s/SDK_VERSION = \"[^\"]*\"/SDK_VERSION = \"$$NEW\"/" src/main/java/com/sangho/http/HttpClient.java; \
	rm -f src/main/java/com/sangho/http/HttpClient.java.bak; \
	echo "$(GREEN)✓ Nouvelle version : $$NEW$(RESET)"

changelog: ## Rappelle de documenter la release dans CHANGELOG.md
	@echo "$(YELLOW)⚠ Ajoutez une entrée dans CHANGELOG.md avant de release.$(RESET)"

# -----------------------------------------------------------------------------
# PUBLICATION Maven Central
# -----------------------------------------------------------------------------
publish-local: ## Installe le jar dans le repo Maven local (~/.m2)
	$(MVN) install -DskipTests

publish: ## Publie sur Maven Central (nécessite le profile "release" + credentials GPG/Sonatype)
	$(MVN) deploy -Prelease -DskipTests

# -----------------------------------------------------------------------------
# RELEASE COMPLÈTE (versioning + git tag + publish)
# -----------------------------------------------------------------------------
release-patch: ## Release patch complète (bump + tag + publish)
	$(MAKE) version-patch
	$(MAKE) _git-tag-and-push
	$(MAKE) publish

release-minor: ## Release minor complète (bump + tag + publish)
	$(MAKE) version-minor
	$(MAKE) _git-tag-and-push
	$(MAKE) publish

release-major: ## Release major complète (bump + tag + publish)
	$(MAKE) version-major
	$(MAKE) _git-tag-and-push
	$(MAKE) publish

_git-tag-and-push:
	$(eval NEW_VERSION := $(shell $(MVN) -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec))
	@echo "$(CYAN)→ Git commit + tag v$(NEW_VERSION)...$(RESET)"
	git add pom.xml src/main/java/com/sangho/http/HttpClient.java CHANGELOG.md
	git commit -m "chore: release v$(NEW_VERSION)"
	git tag -a "v$(NEW_VERSION)" -m "Release v$(NEW_VERSION)"
	git push origin main --tags
	@echo "$(GREEN)✓ Tag v$(NEW_VERSION) poussé$(RESET)"

# -----------------------------------------------------------------------------
# INFOS
# -----------------------------------------------------------------------------
info: ## Affiche les infos du SDK
	@echo "$(BOLD)Artifact:$(RESET) com.sangho:sangho-java"
	@echo "$(BOLD)Version :$(RESET) $(VERSION)"
	@echo "$(BOLD)Java    :$(RESET) $(shell java -version 2>&1 | head -n1)"
	@echo "$(BOLD)Maven   :$(RESET) $(shell $(MVN) -v 2>/dev/null | head -n1)"
