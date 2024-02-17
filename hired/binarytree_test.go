package hired_test

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestSolution(t *testing.T) {
	cases := []struct {
		desc     string
		src      []int64
		expected string
	}{
		{"sample", []int64{3, 6, 2, 9, -1, 10}, "Left"},
		{"TestCase1", []int64{1, 4, 100, 5}, "Right"},
	}
	for _, tc := range cases {
		t.Run(tc.desc, func(t *testing.T) {
			actual := binaryTree(tc.src)
			fmt.Println(actual)
			assert.Equal(t, tc.expected, actual)
		})
	}
}

func binaryTree(arr []int64) string {
	length := int64(len(arr))
	switch length {
	case 0, 1:
		return ""
	case 2:
		return "Left"
	}
	hl := HeightOfTree(arr[1:])
	hr := HeightOfTree(arr[2:])
	if hl > hr {
		return "Left"
	} else if hr > hl {
		return "Right"
	} else {
		return ""
	}
}

func HeightOfTree(arr []int64) int64 {
	length := int64(len(arr))
	switch length {
	case 0:
		return 0
	case 1:
		return 1
	}
	l := LeftChild(0, arr)
	r := RightChild(0, arr)

	if l == -1 && r == -1 {
		return 1
	} else if l == -1 && r < length {
		return 1 + HeightOfTree(arr[r:])
	} else if r == -1 && l < length {
		return 1 + HeightOfTree(arr[l:])
	} else if l < length && r < length {
		// fmt.Println(l, r, arr)
		return 1 + Max(HeightOfTree(arr[r:]), HeightOfTree(arr[l:]))
	}

	return 1
}

func Max(i, j int64) int64 {
	if i >= j {
		return i
	}

	return j
}

func LeftChild(i int64, arr []int64) int64 {
	left := 2*i + 1
	length := int64(len(arr))
	if i < 0 || i > length || left > length || arr[left] == -1 {
		return -1
	}
	return left
}

func RightChild(i int64, arr []int64) int64 {
	var right int64
	length := int64(len(arr))
	right = 2*i + 2
	// if i < 0 || i > length || right > length || arr[right] == -1 {
	// panics
	if i < 0 || i > length || right > length || arr[right] == -1 {
		return -1
	}
	return right
}
