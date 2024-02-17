package easy_test

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"
)

// 1732. Find the Highest Altitude
// https://leetcode.com/problems/find-the-highest-altitude/

func TestFindHighestAltitude2(t *testing.T) {
	cases := []struct {
		desc     string
		src      []int
		expected int
	}{
		{"sample", []int{1, 1, 2}, 2},
	}
	for _, tc := range cases {
		t.Run(tc.desc, func(_ *testing.T) {
			actual := highestAltitude(tc.src)
			fmt.Println(actual)
			assert.ObjectsAreEqual(tc.expected, actual)
		})
	}
}

func highestAltitude(nums []int) int {
	mxA := 0

	alt := make([]int, len(nums)+1)
	for i := 0; i < len(nums); i++ {
		alt[i+1] = alt[i] + nums[i]
		if mxA < alt[i+1] {
			mxA = alt[i+1]
		}
	}
	// fmt.Println(alt)
	return mxA
}

// ------------------------------------------
func TestFindLongestNiceSubstring(t *testing.T) {
	cases := []struct {
		desc     string
		src      string
		expected string
	}{
		{"sample", "YazaAay", "aAa"},
	}
	for _, tc := range cases {
		t.Run(tc.desc, func(_ *testing.T) {
			actual := longestNiceSubstring(tc.src)
			fmt.Println(actual)
			assert.ObjectsAreEqual(tc.expected, actual)
		})
	}
}

func TestFindLongestNiceSubstringLength(t *testing.T) {
	cases := []struct {
		desc     string
		src      string
		expected int64
	}{
		{"sample", "YazaAay", 3},
		{"sample1", "nndNfdfdf", 4},
	}
	for _, tc := range cases {
		t.Run(tc.desc, func(_ *testing.T) {
			actual := longestNiceSubstringLength(tc.src)
			fmt.Println(actual)
			assert.Equal(t, tc.expected, actual)
		})
	}
}

func longestNiceSubstringLength(s string) int64 {
	return int64(len(longestNiceSubstring(s)))
}

func longestNiceSubstring(s string) string {
	switch {
	case len(s) < 2:
		return ""
	case len(s) == 2:
		if s[0]+32 == s[1] || s[0] == s[1]+32 {
			return s
		}

		return ""
	}

	hindering, isEmpty := hinderingLetters(s)
	if isEmpty {
		return s
	}

	result, start := "", 0
	for i := 0; i < len(s); i++ {
		if hindering[s[i]] {
			result = longest(result, longestNiceSubstring(s[start:i]))
			start = i + 1
		}
	}

	return longest(result, longestNiceSubstring(s[start:]))
}

func longest(a, b string) string {
	if len(a) >= len(b) {
		return a
	}

	return b
}

func hinderingLetters(s string) ([]bool, bool) {
	letters := make([]bool, 133)
	for i := 0; i < len(s); i++ {
		letters[s[i]] = true
	}

	isEmpty := true
	for i := byte('A'); i <= 'Z'; i++ {
		switch {
		case letters[i] && letters[i+32]:
			letters[i], letters[i+32] = false, false
		case letters[i] || letters[i+32]:
			isEmpty = false
		}
	}

	return letters, isEmpty
}
