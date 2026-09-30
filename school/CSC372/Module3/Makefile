CONTAINER_ENGINE ?= podman
# Pinned to an exact Temurin build so every machine compiles against the same JDK
# and nobody has to install one locally.
JDK_IMAGE := docker.io/library/eclipse-temurin:17.0.19_10-jdk-jammy
WORKSPACE := /workspace

SRC_DIR := src
TEST_DIR := test
OUT_DIR := out
MAIN_CLASS := DateLogApp
TEST_CLASS := DateLogChecks

SOURCES := $(wildcard $(SRC_DIR)/*.java)

# -Xlint:all surfaces the warnings javac hides by default, which is the point
# of compiling coursework with a build file instead of an IDE button.
JAVACFLAGS := -Xlint:all -d $(OUT_DIR)

CONTAINER_RUN = $(CONTAINER_ENGINE) run --rm \
	--volume "$(CURDIR)":$(WORKSPACE) \
	--workdir $(WORKSPACE) \
	$(JDK_IMAGE)

.PHONY: all build run test clean rebuild help require-engine require-java

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
	@# javac only emits classes, and the app loads its icon from the classpath.
	@cp -- $(SRC_DIR)/icon.png $(OUT_DIR)/

# The window needs a display, which the container does not have, so only this target uses the
# host's Java. Any Java 17 or newer runs the classes the container compiled; an older one would
# stop with a bare UnsupportedClassVersionError instead of saying what it needs.
require-java:
	@command -v java >/dev/null 2>&1 || { \
		echo "E: java not found. The GUI opens on this machine, so 'make run' needs a local Java 17+." >&2; \
		exit 1; \
	}
	@# The version line is not always first: JAVA_TOOL_OPTIONS adds a "Picked up" line above it.
	@# Java 8 reports itself as "1.8.0_x", so its major is the number after "1.".
	@major=$$(java -version 2>&1 | sed -n '/version "/{s/.*version "1\.\([0-9]*\).*/\1/;s/.*version "\([0-9]*\).*/\1/;p;q;}'); \
	[ "$${major:-0}" -ge 17 ] || { \
		echo "E: 'make run' needs Java 17 or newer, found: $$(java -version 2>&1 | grep -m1 'version "')" >&2; \
		exit 1; \
	}

run: require-java build ## compile then open the GUI with the host's Java
	java -cp $(OUT_DIR) $(MAIN_CLASS)

# Test sources live outside src/ so the graded submission folder stays clean. The checks choose the
# panel's menu items without showing a window, so they run headless in the container.
test: build ## compile and run the tests against the program
	$(CONTAINER_RUN) javac $(JAVACFLAGS) -cp $(OUT_DIR) $(TEST_DIR)/$(TEST_CLASS).java
	$(CONTAINER_RUN) java -Djava.awt.headless=true -cp $(OUT_DIR) $(TEST_CLASS)

clean: ## remove out/ (plain host rm, no container needed)
	rm -rf -- $(OUT_DIR)

rebuild: clean build ## clean then build

# Targets document themselves with a trailing "##" so this list never drifts.
# Anything without one, such as require-engine, stays hidden.
help: ## show this help
	@echo "Compiling and testing run in $(JDK_IMAGE); only 'make run' needs a local Java."
	@echo "Override: CONTAINER_ENGINE=docker"
	@echo ""
	@awk -F ':.*##' '/^[a-z-]+:.*##/ { printf "  %-8s %s\n", $$1, $$2 }' $(MAKEFILE_LIST)
