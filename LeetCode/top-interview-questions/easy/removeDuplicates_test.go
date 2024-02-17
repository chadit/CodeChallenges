package easy_test

import (
	"fmt"
	"sort"
	"testing"

	"github.com/stretchr/testify/assert"
)

// Remove Duplicates from Sorted Array
// https://leetcode.com/explore/interview/card/top-interview-questions-easy/92/array/727/
// https://leetcode.com/problems/remove-duplicates-from-sorted-array/

func TestRemoveDuplicates(t *testing.T) {
	cases := []struct {
		desc     string
		src      []int
		expected []int
	}{
		{"sample", []int{1, 1, 2}, []int{1, 2}},
		{"sample2", []int{1, 1, 2, 3, 3}, []int{1, 2, 3}},
		{"sample3", []int{2, 3, 3, 3, 6, 9, 9}, []int{2, 3, 6, 9}},
	}
	for _, tc := range cases {
		t.Run(tc.desc, func(t *testing.T) {
			actual := removeDuplicates(tc.src)
			fmt.Println(actual)
			assert.ObjectsAreEqual(tc.expected, actual)
		})
	}
}

func removeDuplicates(nums []int) int {
	next := 1
	for i := 1; i < len(nums); i++ {
		if nums[next-1] != nums[i] {
			nums[next] = nums[i]
			next++
		}
	}
	return next
}

// func TestFindHighestAltitude(t *testing.T) {
// 	cases := []struct {
// 		desc     string
// 		src      []int
// 		expected int
// 	}{
// 		{"sample", []int{1, 1, 2}, 2},
// 	}
// 	for _, tc := range cases {
// 		t.Run(tc.desc, func(_ *testing.T) {
// 			actual := highestAltitude(tc.src)
// 			fmt.Println(actual)
// 			assert.ObjectsAreEqual(tc.expected, actual)
// 		})
// 	}
// }

// func highestAltitude(numbers []int) int {
// 	maxNumer := 0

// 	alt := make([]int, len(numbers)+1)
// 	for i := 0; i < len(numbers); i++ {
// 		alt[i+1] = alt[i] + numbers[i]
// 		if maxNumer < alt[i+1] {
// 			maxNumer = alt[i+1]
// 		}
// 	}

// 	return maxNumer
// }

// write a function that takes in a list of numbers and returns the highest number in the list.
func TestSolution(t *testing.T) {
	cases := []struct {
		desc     string
		src      []int64
		expected int64
	}{
		{"sample", []int64{1, 1, 2}, 2},
		{"TestCase1", []int64{1, 2, 4}, 4},
	}
	for _, tc := range cases {
		t.Run(tc.desc, func(t *testing.T) {
			actual := Solution(tc.src)
			fmt.Println(actual)
			assert.Equal(t, tc.expected, actual)
		})
	}
}

func Solution(numbers []int64) int64 {
	// Type your solution here
	if len(numbers) == 0 {
		return 0
	}
	sort.Slice(numbers, func(i, j int) bool { return numbers[i] > numbers[j] })

	return numbers[0]
}
