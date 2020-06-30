package util

import (
	"runtime"
)

//IsWindows func is used to check if the host is windows or not
func IsWindows() bool {
	if runtime.GOOS == "windows" {
		return true
	}
	return false
}
