package matrix_test

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"
)

type Board struct {
	Contents [][]int
}

func TestFindShortestPath(t *testing.T) {
	b := Board{
		Contents: [][]int{
			{4, 2, 10, 5},
			{2, 0, 15, 9},
			{1, 4, 4, 5},
		},
	}

	res := shortestPathBinaryMatrix1(b.Contents)
	assert.Equal(t, 19, res)
}

var (
	pathLength int
	globalGrid [][]int
)

func shortestPathBinaryMatrix(grid [][]int) int {
	pathLength = 200
	edges := make([][]int, len(grid))
	gridLength := len(grid)
	globalGrid = grid
	for i := 0; i < gridLength; i++ {
		edges[i] = make([]int, gridLength)
	}
	searchPath(0, 0, edges, 0, gridLength)
	if pathLength == 200 {
		return -1
	}
	return pathLength
}

func searchPath(x int, y int, edges [][]int, edgeCount int, gridLength int) [][]int {
	edgeCount++
	if edges[y][x] != 0 && edges[y][x] <= edgeCount {
		return edges
	}
	if edgeCount >= pathLength || globalGrid[y][x] == 1 {
		return edges
	}
	edges[y][x] = edgeCount
	if x == len(globalGrid[0])-1 && y == gridLength-1 {
		pathLength = edgeCount
		return edges
	}
	for i := -1; i <= 1; i++ {
		for j := -1; j <= 1; j++ {
			if y+i < 0 || x+j < 0 || y+i >= gridLength || x+j >= gridLength {
				continue
			}
			if globalGrid[y+i][x+j] == 1 {
				continue
			}
			searchPath(x+j, y+i, edges, edgeCount, gridLength)
		}
	}
	return edges
}

type Trip struct {
	Row, Col int
}

func shortestPathBinaryMatrix1(grid [][]int) int {
	fmt.Println(grid[0][0])
	if grid[0][0] != 0 || grid[len(grid)-1][len(grid[0])-1] != 0 {
		return -1
	}

	queue := []Trip{{0, 0}}
	grid[0][0] = 1

	for len(queue) > 0 {
		now := queue[0]
		queue = queue[1:]

		if now.Row == len(grid)-1 && now.Col == len(grid[0])-1 {
			return grid[now.Row][now.Col]
		}

		moves := [][]int{{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}}

		for _, move := range moves {
			t := Trip{now.Row + move[0], now.Col + move[1]}

			if t.Row < 0 || t.Row >= len(grid) || t.Col < 0 || t.Col >= len(grid[0]) || grid[t.Row][t.Col] != 0 {
				continue
			}

			grid[t.Row][t.Col] = grid[now.Row][now.Col] + 1
			queue = append(queue, t)
		}
	}

	return -1
}
