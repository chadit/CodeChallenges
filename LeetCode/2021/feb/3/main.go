package main

import "fmt"

func main() {
	fmt.Println("vim-go")
}

/**
 * Definition for singly-linked list.
  * type ListNode struct {
	   *     Val int
	    *     Next *ListNode
		 * }
*/
func hasCycle(head *ListNode) bool {
	if head == nil {
		return false
	}
	l1, l2 := head, head

	for l2 != nil && l2.Next != nil {
		l2 = l2.Next.Next
		l1 = l1.Next
		if l1 == l2 {
			return true
		}
	}
	return false
}
