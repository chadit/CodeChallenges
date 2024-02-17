package easy_test

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"
)

// 3. Longest Substring Without Repeating Characters
// https://leetcode.com/problems/longest-substring-without-repeating-characters/

func TestFindLongestSubstringWithoutRepeating(t *testing.T) {
	cases := []struct {
		desc     string
		src      string
		expected int
	}{
		{"sample", "abcabcbb", 3},
		{"sample1", "nndNfdfdf", 4},
	}
	for _, tc := range cases {
		t.Run(tc.desc, func(t *testing.T) {
			actual := lengthOfLongestSubstring(tc.src)
			fmt.Println(actual)
			assert.Equal(t, tc.expected, actual)
		})
	}
}

func Contains(s []rune, str rune) (bool, int) {
	// find element in string
	for ind, v := range s {
		if v == str {
			return true, ind
		}
	}
	return false, -1
}

func lengthOfLongestSubstring(s string) int {
	max_len := 0
	var my_slice []rune
	for _, chr := range s {
		// if element exists, remove old element
		if flag, i := Contains(my_slice, chr); flag {
			my_slice = append(my_slice[i+1:])
		}

		my_slice = append(my_slice, chr)
		slice_len := len(my_slice)
		if slice_len > max_len {
			max_len = slice_len
		}

	}
	return max_len
}
