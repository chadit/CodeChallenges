JAVAC ?= javac
JAVA ?= java
SRC_DIR := src
TEST_DIR := test
OUT_DIR := out
MAIN_CLASS ?= GradeStatistics
TEST_CLASS ?= GradeStatisticsTest

SOURCES := $(wildcard $(SRC_DIR)/*.java)
CLASSES := $(patsubst $(SRC_DIR)/%.java,$(OUT_DIR)/%.class,$(SOURCES))

# -Xlint:all surfaces the warnings javac hides by default, which is the point
# of compiling coursework with a build file instead of an IDE button.
JAVACFLAGS ?= -Xlint:all -d $(OUT_DIR)

.PHONY: all build run test clean rebuild help

all: build

build: $(CLASSES)

$(OUT_DIR)/%.class: $(SRC_DIR)/%.java
	@mkdir -p $(OUT_DIR)
	$(JAVAC) $(JAVACFLAGS) $<

run: build
	$(JAVA) -cp $(OUT_DIR) $(MAIN_CLASS)

# Test sources live outside src/ so the graded submission folder stays clean.
test: build
	$(JAVAC) $(JAVACFLAGS) -cp $(OUT_DIR) $(TEST_DIR)/$(TEST_CLASS).java
	$(JAVA) -cp $(OUT_DIR) $(TEST_CLASS)

clean:
	rm -rf $(OUT_DIR)
	rm -f $(SRC_DIR)/*.class

rebuild: clean build

help:
	@echo "build    compile $(SRC_DIR)/*.java into $(OUT_DIR)/"
	@echo "run      compile then run $(MAIN_CLASS)"
	@echo "test     compile and run $(TEST_CLASS) against the program"
	@echo "clean    remove $(OUT_DIR)/ and stray .class files in $(SRC_DIR)/"
	@echo "rebuild  clean then build"
	@echo ""
	@echo "Override the entry point: make run MAIN_CLASS=OtherClass"
