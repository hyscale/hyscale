/*
Copyright 2019 Pramati Prism, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package main

import (
	"errors"
	"log"
	"os"
	"os/exec"
	"strconv"
	"strings"

	container "hyscale/container"
)

const (
	dockerVersionWarnMsg = "Docker Version 18.09 and above is required but found a lesser version"
	dockerRequireMsg     = "Docker 18.09 and above is required to run Hyscale"
)

func main() {
	dockerPresent := checkForDocker()
	if !dockerPresent {
		log.Fatal(errors.New(dockerRequireMsg))
	}
	args := os.Args[1:]
	clispec := parse(args)
	container.Run(clispec)
}

func parse(args []string) *container.CliSpec {
	interactive := false

	if len(args) > 2 && ("logs" == args[2] || "log" == args[2]) {
		interactive = true
	}

	var outputEnabled bool
	var hspecs []string
	for i, each := range args {
		if strings.HasPrefix(each, "-o") || strings.HasPrefix(each, "--output") {
			outputEnabled = true
		}

		if strings.HasPrefix(each, "-f=") {
			hspecs = append(hspecs, strings.Split(each[3:], ",")...)
		} else if strings.HasPrefix(each, "-f") {
			hspecs = append(hspecs, strings.Split(args[i+1], ",")...)
		}
	}
	return &container.CliSpec{
		Interactive:   interactive,
		Hspecs:        hspecs,
		DisableBanner: outputEnabled,
	}
}

func checkForDocker() bool {
	dockerVerified := false
	cmd := exec.Command("docker", "--version")
	out, err := cmd.Output()
	if err != nil {
		log.Fatal(errors.New(dockerRequireMsg))
	}
	dockerVersionStr := string(out)

	if dockerVersionStr != "" {
		tokens := strings.Split(dockerVersionStr, " ")
		dockerVersion, err := strconv.Atoi(strings.Split(tokens[2], ".")[0])
		if err != nil {
			log.Fatal(err)
		}
		if dockerVersion < 18 {
			log.Fatal(errors.New(dockerVersionWarnMsg))
		}
		dockerVerified = true
	}
	return dockerVerified
}
