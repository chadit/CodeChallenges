package resolver

import (
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
	"strconv"
	"strings"

	"github.com/pkg/errors"
)

const (
	solutionOneInput = "input1"
	solutionTwoInput = "input2"
)

func Resolve() {
	fmt.Println(FirstPart(getDepths(solutionOneInput)))
	fmt.Println(SecondPart(getDepths(solutionTwoInput)))
}

func getDepths(inputName string) []int {
	cwd, err := os.Getwd()
	if err != nil {
		log.Fatal(errors.Wrap(err, "get working directory"))
	}
	fullPath := filepath.Join(cwd, inputName)
	content, err := ioutil.ReadFile(fullPath)
	if err != nil {
		log.Fatal(errors.Wrap(err, "read file"))
	}

	rows, err := toIntegers(strings.Split(strings.TrimSpace(string(content)), "\n"))
	if err != nil {
		log.Fatal(errors.Wrap(err, "converting string to integer"))
	}

	return rows
}

func FirstPart(input []int) int {
	return depthIncreases(input)
}

func SecondPart(input []int) int {
	l := len(input) - 2
	reducedList := make([]int, l)
	for i := 0; i < l; i++ {
		sum := 0
		for _, number := range input[i : i+3] {
			sum += number
		}

		reducedList[i] = sum
	}
	return depthIncreases(reducedList)
}

func depthIncreases(numbers []int) int {
	inc := 0

	for i := 0; i < len(numbers)-1; i++ {
		if numbers[i] < numbers[i+1] {
			inc += 1
		}
	}

	return inc
}

func toIntegers(stringList []string) ([]int, error) {
	ints := make([]int, len(stringList))
	for i, s := range stringList {
		value, err := strconv.Atoi(s)
		if err != nil {
			return nil, err
		}
		ints[i] = value
	}
	return ints, nil
}
