package easy_test

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

// https://www.geeksforgeeks.org/given-sorted-array-number-x-find-pair-array-whose-sum-closest-x/
// https://leetcode.com/problems/two-sum/

// Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.
// You may assume that each input would have exactly one solution, and you may not use the same element twice.
// You can return the answer in any order.

func TestTwoSum2(t *testing.T) {
	cases := []struct {
		desc     string
		src      []int
		expected []int
		target   int
	}{
		{"sample", []int{2, 7, 11, 15}, []int{0, 1}, 9},
		{"sample2", []int{3, 2, 4}, []int{1, 2}, 6},
		{"sample3", []int{0, 3, 2, 4}, []int{1, 2}, 0},
		{"Empty", []int{}, []int{}, 0},
		{"Zero", []int{10, 22, 28, 29, 30, 40}, []int{1, 4}, 54},
		{"Positive", []int{1, 2, 3, 4, 5, 6}, []int{0, 2}, 0},
	}

	for _, tc := range cases {
		t.Run(tc.desc, func(_ *testing.T) {
			actual := twoSum(tc.src, tc.target)
			//	fmt.Println(actual)
			assert.ObjectsAreEqual(tc.expected, actual)
		})
	}

	for _, tc := range cases {
		t.Run(tc.desc, func(_ *testing.T) {
			actual := twoSum2(tc.src, tc.target)
			//	fmt.Println(actual)
			assert.ObjectsAreEqual(tc.expected, actual)
		})
	}

	for _, tc := range cases {
		t.Run(tc.desc, func(_ *testing.T) {
			actual := twoSum3(tc.src, tc.target)
			//	fmt.Println(actual)
			assert.ObjectsAreEqual(tc.expected, actual)
		})
	}
}

// 59 ms Runtime  3.6 MB Memory
func twoSum(nums []int, target int) []int {
	for i, v := range nums {
		for k := i + 1; k < len(nums); k++ {
			if target-v == nums[k] {
				return []int{i, k}
			}
		}
	}
	return []int{}
}

// 37ms Runtime  3.6 MB Memory
func twoSum2(nums []int, target int) []int {
	count := len(nums)
	for i := range nums {
		for j := 1; j < count; j++ {
			if i != j {
				if nums[i]+nums[j] == target {
					return []int{i, j}
				}
			}
		}
	}
	return []int{}
}

// 28ms Runtime  3.5 MB Memory
func twoSum3(nums []int, target int) []int {
	for i := 0; i < len(nums)-1; i++ {
		for j := i + 1; j < len(nums); j++ {
			if nums[i]+nums[j] == target {
				return []int{i, j}
			}
		}
	}
	return []int{}
}
