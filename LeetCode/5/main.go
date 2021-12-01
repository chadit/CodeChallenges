package simplifypath

import "strings"

func simplifyPath(path string) string {
	allPaths := strings.Split(path, "/")
	paths := make([]string, 0, len(allPaths))
	for _, p := range allPaths {
		if p == "" || p == "." {
			continue
		}
		if p == ".." {
			if len(paths) != 0 {
				paths = paths[:len(paths)-1]
			}
		} else {
			paths = append(paths, p)
		}
	}
	if len(paths) == 0 {
		return "/"
	}
	return "/" + strings.Join(paths, "/")
}
