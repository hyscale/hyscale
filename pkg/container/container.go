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

package container

import (
	"io"
	"os"
	"os/exec"
	"os/user"
	"strings"

	"github.com/docker/docker/api/types/mount"

	"hyscale/pkg/constants"
	cnst "hyscale/pkg/constants"
	installer "hyscale/pkg/installer"
	"hyscale/pkg/registry"
	util "hyscale/pkg/util"
)

const (
	//ImageCleanUpPolicy is used to clean the build images
	ImageCleanUpPolicy = "IMAGE_CLEANUP_POLICY"

	homeEnv            = "HYSCALECTL_HOME"
	kubeConfEnv        = "HYSCALECTL_KUBECONF"
	dockerConfEnv      = "HYSCALECTL_DOCKERCONF"
	dockerConfigDirEnv = "DOCKER_CONFIG"
	lbReadyTimeoutEnv  = "HYS_LB_READY_TIMEOUT"
	workflowLoggerDisabled = "WORKFLOW_LOGGER_DISABLED"
	//EqualsTo is used for constructing environment varibles
	EqualsTo               = "="
	containerFileSeparator = "/"
	configDir              = "/.docker"
	configJSONPath         = configDir + "/config.json"
	kubeConfDir            = "/.kube"
	kubeConfPath           = kubeConfDir + "/config"
	unixSocket             = "/var/run/docker.sock"
	dockerHost             = "DOCKER_HOST"
	externalRegistryEnv    = "HYS_REGISTRY_CONFIG"
)


var (
	imageName      = cnst.ImageName+":"+cnst.ImageTag
	currentUser, _ = user.Current()
	dockerConf     = currentUser.HomeDir + configJSONPath
	dockerConfDir  = currentUser.HomeDir + configDir
	kubeConf       = currentUser.HomeDir + kubeConfPath
	pwd, _         = os.Getwd()
)

//HysContainer is container based installer for hyscale
type HysContainer struct {
}

//Run method for Container based hyscale installer
func (hyscontainer *HysContainer) Run(cliSpec *installer.DeploySpec) (resp *installer.DeployResponse, err error) {

	labels := make(map[string]string)
	labels["name"] = constants.Hyscale

	args := []string{"run", "--rm","--net=host"}

	// Attach the cmd stdin to os.Stdin
	args = append(args, "-i")

	for k, v := range labels {
		args = append(args, "--label")
		args = append(args, BuildOption(k, v))
	}
	for k, v := range *getEnvs(constants.User) {
		args = append(args, "-e")
		args = append(args, BuildOption(k, v))
	}

	for _, mount := range *getVolumes() {
		args = append(args, "-v")
		args = append(args, BuildVolumes(&mount))
	}
	args = append(args, imageName)
	args = append(args, os.Args[1:]...)

	cmd := exec.Command("docker", args...)

	// Setting the StdIn to standardinput in case of interative stream
	if cliSpec.Interactive {
		cmd.Stdin = os.Stdin
	}

	// Writing the docker config json to stdin of docker container
	if len(cliSpec.Hspecs) > 0 {
		authConfig, err := registry.GetAuthConfig(cliSpec.Hspecs)
		if err != nil {
			panic(err)
		}
		
		stdin, e := cmd.StdinPipe()
		if e != nil {
			panic(e)
		}

		go func() {
			defer stdin.Close()
			io.WriteString(stdin, *authConfig)
		}()
	}

	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	err = cmd.Run()
	if err != nil {
		if exit, ok := err.(*exec.ExitError); ok {
			os.Exit(exit.ExitCode())
		}
	}
	return resp, nil
}

//BuildOption builds the options in the map form to return the option string
func BuildOption(key string, value string) string {
	var builder strings.Builder
	builder.WriteString(key)
	builder.WriteString("=")
	builder.WriteString(value)

	return builder.String()
}

func getEnvs(user *user.User) *map[string]string {
	envs := make(map[string]string)

	envs[homeEnv] = user.HomeDir + "/." + constants.Hyscale
	envs[kubeConfEnv] = kubeConf
	envs[dockerConfEnv] = dockerConf
	envs[dockerConfigDirEnv] = "/" + constants.Hyscale + "/.docker"
	envs[externalRegistryEnv] = "true"

	// If the DockerHost environment variable is empty , the value is set based on the OS
	dockerHostEnv := os.Getenv(dockerHost)
	if dockerHostEnv == ""{
		if util.IsWindows() {
			envs[dockerHost] = constants.WindowsInternalDockerHost
		} else {
			envs[dockerHost] = "unix://" + unixSocket
		}
	}else{
		envs[dockerHost] = dockerHostEnv
	}

	// IMAGE_CLEANUP_POLICY Env
	cleanUp := os.Getenv(ImageCleanUpPolicy)
	if cleanUp != "" {
		envs[ImageCleanUpPolicy] = cleanUp
	}

	//HYS_LB_READY_TIMEOUT Env
	lbTimeout := os.Getenv(lbReadyTimeoutEnv)
	if lbTimeout != "" {
		envs[lbReadyTimeoutEnv] = lbTimeout
	}
	return &envs
}

func getVolumes() *[]mount.Mount {
	volumes := []mount.Mount{
		{
			Type:     mount.TypeBind,
			Source:   dockerConf,
			Target:   containerFileSeparator + constants.Hyscale + configJSONPath,
			ReadOnly: true,
		},
		{
			Type:     mount.TypeBind,
			Source:   kubeConf,
			Target:   containerFileSeparator + constants.Hyscale + kubeConfPath,
			ReadOnly: true,
		},
		{
			Type:     mount.TypeBind,
			Source:   pwd,
			Target:   containerFileSeparator + constants.Hyscale + "/app",
			ReadOnly: true,
		},
		{
			Type:     mount.TypeBind,
			Source:   constants.HysDir + containerFileSeparator + constants.Hyscale,
			Target:   containerFileSeparator + constants.Hyscale + containerFileSeparator + constants.Hyscale,
			ReadOnly: false,
		},
	}

	// Mount unix docket for all unix OS
	if !util.IsWindows() {
		dockersock := mount.Mount{
			Type:     mount.TypeBind,
			Source:   unixSocket,
			Target:   unixSocket,
			ReadOnly: true,
		}
		volumes = append(volumes, dockersock)
	}

	return &volumes
}

//BuildVolumes builds the volume mounts to return the -v string
func BuildVolumes(mount *mount.Mount) string {
	var builder strings.Builder
	builder.WriteString(mount.Source)
	builder.WriteString(":")
	builder.WriteString(mount.Target)
	if mount.ReadOnly {
		builder.WriteString(":ro")
	}

	return builder.String()
}
