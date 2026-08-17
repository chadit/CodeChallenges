CONTAINER_ENGINE ?= podman
# Pinned to an exact Temurin build so every machine compiles against the same JDK
# and nobody has to install one locally.
JDK_IMAGE ?= docker.io/library/eclipse-temurin:17.0.19_10-jdk-jammy
WORKSPACE := /workspace

SRC_DIR := src
TEST_DIR := test
OUT_DIR := out
MAIN_CLASS ?= MonthlyTemperatures
TEST_CLASS ?= MonthlyTemperaturesTest

SOURCES := $(wildcard $(SRC_DIR)/*.java)

# -Xlint:all surfaces the warnings javac hides by default, which is the point
# of compiling coursework with a build file instead of an IDE button.
JAVACFLAGS ?= -Xlint:all -d $(OUT_DIR)

CONTAINER_RUN = $(CONTAINER_ENGINE) run --rm \
	--volume "$(CURDIR)":$(WORKSPACE) \
	--workdir $(WORKSPACE) \
	$(JDK_IMAGE)

# The program reads a line from stdin, so "make run" needs a terminal when a human
# drives it and must not ask for one when input is piped in, as in:
#   printf 'march\n' | make run
TTY_FLAG = $(shell [ -t 0 ] && echo --tty)

CONTAINER_RUN_INTERACTIVE = $(CONTAINER_ENGINE) run --rm --interactive $(TTY_FLAG) \
	--volume "$(CURDIR)":$(WORKSPACE) \
	--workdir $(WORKSPACE) \
	$(JDK_IMAGE)

.PHONY: all build run test clean rebuild help require-engine

all: build

# Without this guard the recipes fail with a bare "command not found" that says
# nothing about the container engine being the actual requirement.
require-engine:
	@command -v $(CONTAINER_ENGINE) >/dev/null 2>&1 || { \
		echo "E: $(CONTAINER_ENGINE) not found. Install it, or pick another engine:" >&2; \
		echo "E:   make build CONTAINER_ENGINE=docker" >&2; \
		exit 1; \
	}

build: require-engine ## compile src/*.java into out/
	$(CONTAINER_RUN) javac $(JAVACFLAGS) $(SOURCES)

run: build ## compile then run the program
	$(CONTAINER_RUN_INTERACTIVE) java -cp $(OUT_DIR) $(MAIN_CLASS)

# Test sources live outside src/ so the graded submission folder stays clean.
test: build ## compile and run the tests against the program
	$(CONTAINER_RUN) javac $(JAVACFLAGS) -cp $(OUT_DIR) $(TEST_DIR)/$(TEST_CLASS).java
	$(CONTAINER_RUN) java -cp $(OUT_DIR) $(TEST_CLASS)

clean: ## remove out/ (plain host rm, no container needed)
	rm -rf -- $(OUT_DIR)

rebuild: clean build ## clean then build

# Targets document themselves with a trailing "##" so this list never drifts.
# Anything without one, such as require-engine, stays hidden.
help: ## show this help
	@echo "Java runs in $(JDK_IMAGE), so no local JDK is needed."
	@echo "Overrides: CONTAINER_ENGINE=docker  MAIN_CLASS=OtherClass"
	@echo ""
	@awk -F ':.*##' '/^[a-z-]+:.*##/ { printf "  %-8s %s\n", $$1, $$2 }' $(MAKEFILE_LIST)
