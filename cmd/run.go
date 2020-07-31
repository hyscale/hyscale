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
package cmd

import (
	"errors"
	"log"
	"os/exec"
	"strconv"
	"strings"

	"hyscale/pkg/container"
	installer "hyscale/pkg/installer"

	//java "hyscale/pkg/java"

	"github.com/spf13/cobra"
)

const (
	javaVersionWarnMsg   = "JDK version 11 and above is required but found a lesser version"
	dockerVersionWarnMsg = "Docker Version 18.09 and above is required but found a lesser version"
	dockerRequireMsg     = "Docker 18.09 and above is required to run Hyscale"
)

//CLIInput is the struct for user input
type CLIInput struct {
	Cmd           *cobra.Command
	Args          []string
	Interative    bool
	DisableBanner bool
}

//HyscaleRun is an entrypoint for Hyscale CommandLine Run
func HyscaleRun(cliInput *CLIInput) {

	dockerPresent := checkForDocker()
	if !dockerPresent {
		log.Fatal(errors.New(dockerRequireMsg))
	}

	//javaPresent := checkForJava()
	spec := installer.DeploySpec{Interactive: cliInput.Interative, Hspecs: cliInput.Args, DisableBanner: cliInput.DisableBanner}
	/*if javaPresent {
		// Check for Hyscale jar if not present download it
		var javaInstaller java.Jar
		javaInstaller.Run(&spec)

	}*/

	if dockerPresent {
		var containerInstaller container.HysContainer
		containerInstaller.Run(&spec)
	}
}

func checkForJava() bool {

	javaInstalled := false
	cmd := exec.Command("java", "-version")
	out, err := cmd.CombinedOutput()
	if err != nil {
		log.Fatal(err)
	}

	javaVersion := string(out)
	if javaVersion != "" {
		tokens := strings.Split(javaVersion, " ")
		majorVersion, err := strconv.Atoi(strings.Split(strings.Trim(tokens[2], "\""), ".")[0])
		//fmt.Println(majorVersion)
		if err != nil {
			log.Fatal(err)
		}
		if majorVersion < 11 {
			log.Fatal(errors.New(javaVersionWarnMsg))
		}
		javaInstalled = true
	}
	return javaInstalled
}

func checkForDocker() bool {
	dockerVerified := false
	cmd := exec.Command("docker", "--version")
	out, err := cmd.CombinedOutput()
	if err != nil {
		log.Fatal(err)
	}
	dockerVersionStr := string(out)
	//fmt.Println(dockerVersionStr)
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
