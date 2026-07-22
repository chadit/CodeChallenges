JAVAC ?= javac
JAVA ?= java
SRC_DIR := src
OUT_DIR := out
MAIN_CLASS ?= RestaurantInfo

SOURCES := $(wildcard $(SRC_DIR)/*.java)
CLASSES := $(patsubst $(SRC_DIR)/%.java,$(OUT_DIR)/%.class,$(SOURCES))

# -Xlint:all surfaces the warnings javac hides by default, which is the point
# of compiling coursework with a build file instead of an IDE button.
JAVACFLAGS ?= -Xlint:all -d $(OUT_DIR)

.PHONY: all build run clean rebuild help

all: build

build: $(CLASSES)

$(OUT_DIR)/%.class: $(SRC_DIR)/%.java
	@mkdir -p $(OUT_DIR)
	$(JAVAC) $(JAVACFLAGS) $<

run: build
	$(JAVA) -cp $(OUT_DIR) $(MAIN_CLASS)

clean:
	rm -rf $(OUT_DIR)
	rm -f $(SRC_DIR)/*.class

rebuild: clean build

help:
	@echo "build    compile $(SRC_DIR)/*.java into $(OUT_DIR)/"
	@echo "run      compile then run $(MAIN_CLASS)"
	@echo "clean    remove $(OUT_DIR)/ and stray .class files in $(SRC_DIR)/"
	@echo "rebuild  clean then build"
	@echo ""
	@echo "Override the entry point: make run MAIN_CLASS=OtherClass"
