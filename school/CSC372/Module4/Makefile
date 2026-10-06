CONTAINER_ENGINE ?= podman
# Pinned to an exact Temurin build so every machine compiles against the same JDK
# and nobody has to install one locally.
JDK_IMAGE := docker.io/library/eclipse-temurin:17.0.19_10-jdk-jammy
WORKSPACE := /workspace

SRC_DIR := src
TEST_DIR := test
OUT_DIR := out
MAIN_CLASS := ShapeArray
TEST_CLASS := ShapeChecks

SOURCES := $(wildcard $(SRC_DIR)/*.java)

# -Xlint:all surfaces the warnings javac hides by default, which is the point
# of compiling coursework with a build file instead of an IDE button.
JAVACFLAGS := -Xlint:all -d $(OUT_DIR)

CONTAINER_RUN = $(CONTAINER_ENGINE) run --rm \
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

# javac would create out/ itself, but only after asking whether it already exists. Right after
# "make clean" the container is still told the deleted directory is there, so javac skips the
# create and the first class-file write fails. Making it host-side settles the answer first.
build: require-engine ## compile src/*.java into out/
	@mkdir -p $(OUT_DIR)
	$(CONTAINER_RUN) javac $(JAVACFLAGS) $(SOURCES)

run: build ## compile then run the program
	$(CONTAINER_RUN) java -cp $(OUT_DIR) $(MAIN_CLASS)

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
	@echo "Override: CONTAINER_ENGINE=docker"
	@echo ""
	@awk -F ':.*##' '/^[a-z-]+:.*##/ { printf "  %-8s %s\n", $$1, $$2 }' $(MAKEFILE_LIST)
