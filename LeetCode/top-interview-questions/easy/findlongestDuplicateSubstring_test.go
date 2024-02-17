package easy_test

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"
)

// 1044. Longest Duplicate Substring
// https://leetcode.com/problems/longest-duplicate-substring/

func TestFindLongestDuplicateSubstring(t *testing.T) {
	cases := []struct {
		desc     string
		src      string
		expected string
	}{
		{"sample", "banana", "ana"},
	}
	for _, tc := range cases {
		t.Run(tc.desc, func(t *testing.T) {
			actual := longestDupSubstring(tc.src)
			fmt.Println(actual)
			assert.Equal(t, tc.expected, actual)
		})
	}
}

func TestFindLongestDuplicateSubstringLength(t *testing.T) {
	cases := []struct {
		desc     string
		src      string
		expected int64
	}{
		{"sample", "banana", 3},
		{"sample1", "nndNfdfdf", 4},
	}
	for _, tc := range cases {
		t.Run(tc.desc, func(t *testing.T) {
			actual := longestDupSubstringLength(tc.src)
			fmt.Println(actual)
			assert.Equal(t, tc.expected, actual)
		})
	}
}

func longestDupSubstringLength(s string) int64 {
	return int64(len(longestDupSubstring(s)))
}

func longestDupSubstring(s string) string {
	l, r, start := 1, len(s)-1, -1
	for l <= r {
		m := (l + r) >> 1
		if dup, index := duplicate(s, m); dup == true {
			start = index
			l = m + 1
		} else {
			r = m - 1
		}
	}
	if start == -1 {
		return ""
	}
	// substring size is l-1
	return s[start : start+l-1]
}

// string hash
func duplicate(s string, n int) (bool, int) {
	// hash twice to prevent hash collisions :)
	hash1, mod1, msb1, mag1 := 0, 10000000007, 1, 31
	hash2, mod2, msb2, mag2 := 0, 10000000013, 1, 131
	for i := 0; i < n; i++ {
		hash1 = (hash1*mag1 + int(s[i]-'a')) % mod1
		msb1 = (msb1 * mag1) % mod1
		hash2 = (hash2*mag2 + int(s[i]-'a')) % mod2
		msb2 = (msb2 * mag2) % mod2
	}
	hashMap1 := map[int]bool{hash1: true}
	hashMap2 := map[int]bool{hash2: true}
	for i := n; i < len(s); i++ {
		hash1 = ((hash1+mod1)*mag1 - int(s[i-n]-'a')*msb1 + int(s[i]-'a')) % mod1
		hash2 = ((hash2+mod2)*mag2 - int(s[i-n]-'a')*msb2 + int(s[i]-'a')) % mod2
		if hashMap1[hash1] == true && hashMap2[hash2] == true {
			return true, i - n + 1
		}
		hashMap1[hash1] = true
		hashMap2[hash2] = true
	}
	return false, -1
}
