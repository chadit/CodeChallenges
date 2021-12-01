package resolver_test

import (
	"testing"

	resolver "github.com/chadit/CodeChallenges/AdventOfCode/2021/Day1/Resolver"
	"github.com/stretchr/testify/assert"
)

func TestDay1Part1(t *testing.T) {
	t.Run("Should solve the example", func(t *testing.T) {
		input := []int{
			199,
			200,
			208,
			210,
			200,
			207,
			240,
			269,
			260,
			263,
		}

		expected := 7
		actual := resolver.FirstPart(input)

		assert.Equal(t, expected, actual)
	})
}

func TestDay1Part2(t *testing.T) {
	t.Run("Should solve the example", func(t *testing.T) {
		input := []int{
			199,
			200,
			208,
			210,
			200,
			207,
			240,
			269,
			260,
			263,
		}

		expected := 5
		actual := resolver.SecondPart(input)

		assert.Equal(t, expected, actual)
	})
}
