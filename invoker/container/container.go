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
	"runtime"
	"strings"

	"hyscale/credstore"

	"github.com/docker/docker/api/types/mount"
)

const (
	// Hyscale defines the constant hyscale
	Hyscale = "hyscale"
	//ImageName Defines the latest image name of the HyScale Image
	ImageName = "@@IMAGE_NAME@@"
	// ImageTag Defines the latest image tag of the HyScale Image
	ImageTag = "@@IMAGE_TAG@@"
	//ImageCleanUpPolicy is used to clean the build images
	ImageCleanUpPolicy     = "HYS_IMAGE_CLEANUP_POLICY"
	hysHostFsEnv           = "HYSCALE_HOST_FS"
	homeEnv                = "HYSCALECTL_HOME"
	kubeConfEnv            = "HYSCALECTL_KUBECONF"
	dockerConfEnv          = "HYSCALECTL_DOCKERCONF"
	dockerConfigDirEnv     = "DOCKER_CONFIG"
	lbReadyTimeoutEnv      = "HYS_LB_READY_TIMEOUT"
	workflowLoggerDisabled = "WORKFLOW_LOGGER_DISABLED"
	//LogLevel for hyscale tool logs default is info
	LogLevel = "HYS_LOG_LEVEL"
	//LogSize for hyscale tool logs default is 100MB
	LogSize = "HYS_LOG_SIZE"
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
	//User defines the current user executing the hyscale binary
	User, _ = user.Current()

	// HysDir defines the hyscale directory of the 'User'
	HysDir = User.HomeDir + "/." + Hyscale

	//WindowsInternalDockerHost specifies the host address of where docker daemon is running
	WindowsInternalDockerHost = "tcp://host.docker.internal:2375"

	imageName      = ImageName + ":" + ImageTag
	currentUser, _ = user.Current()
	dockerConf     = currentUser.HomeDir + configJSONPath
	dockerConfDir  = currentUser.HomeDir + configDir
	kubeConf       = currentUser.HomeDir + kubeConfPath
	pwd, _         = os.Getwd()
)

//CliSpec refers to the Spec of the command line that controls the HyscaleRun
type CliSpec struct {
	Interactive   bool
	Hspecs        []string
	DisableBanner bool
}

//Run method for Container based hyscale installer
func Run(cliSpec *CliSpec) (err error) {

	labels := make(map[string]string)
	labels["name"] = Hyscale

	args := []string{"run", "--rm", "--net=host"}

	// Attach the cmd stdin to os.Stdin
	args = append(args, "-i")

	for k, v := range labels {
		args = append(args, "--label")
		args = append(args, BuildOption(k, v))
	}
	for k, v := range *getEnvs(User) {
		args = append(args, "-e")
		args = append(args, BuildOption(k, v))
	}

	// Disabling hyscale banner
	if cliSpec.DisableBanner {
		args = append(args, "-e")
		args = append(args, BuildOption(workflowLoggerDisabled, "true"))
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
		authConfig, err := credstore.GetAuthConfig(cliSpec.Hspecs)
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
	return nil
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

	envs[homeEnv] = user.HomeDir + "/." + Hyscale
	envs[kubeConfEnv] = kubeConf
	envs[dockerConfEnv] = dockerConf
	envs[dockerConfigDirEnv] = "/" + Hyscale + "/.docker"
	envs[externalRegistryEnv] = "true"

	// The DOCKER_HOST environment variable value is set based on the OS
	if IsWindows() {
		envs[dockerHost] = WindowsInternalDockerHost
		envs[hysHostFsEnv] = string(os.PathSeparator)
	} else {
		envs[dockerHost] = "unix://" + unixSocket
	}
	// HYS_IMAGE_CLEANUP_POLICY Env
	cleanUp := os.Getenv(ImageCleanUpPolicy)
	if cleanUp != "" {
		envs[ImageCleanUpPolicy] = cleanUp
	}

	//HYS_LB_READY_TIMEOUT Env
	lbTimeout := os.Getenv(lbReadyTimeoutEnv)
	if lbTimeout != "" {
		envs[lbReadyTimeoutEnv] = lbTimeout
	}

	logLevel := os.Getenv(LogLevel)
	if logLevel != "" {
		envs[LogLevel] = logLevel
	}

	logSize := os.Getenv(LogSize)
	if logSize != "" {
	    envs[LogSize] = logSize
	}
	return &envs
}

func getVolumes() *[]mount.Mount {
	volumes := []mount.Mount{
		{
			Type:     mount.TypeBind,
			Source:   dockerConf,
			Target:   containerFileSeparator + Hyscale + configJSONPath,
			ReadOnly: true,
		},
		{
			Type:     mount.TypeBind,
			Source:   kubeConf,
			Target:   containerFileSeparator + Hyscale + kubeConfPath,
			ReadOnly: true,
		},
		{
			Type:     mount.TypeBind,
			Source:   pwd,
			Target:   containerFileSeparator + Hyscale + "/app",
			ReadOnly: true,
		},
		{
			Type:     mount.TypeBind,
			Source:   HysDir + containerFileSeparator + Hyscale,
			Target:   containerFileSeparator + Hyscale + containerFileSeparator + Hyscale,
			ReadOnly: false,
		},
	}

	// Mount unix docket for all unix OS
	if !IsWindows() {
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

//IsWindows verifies whether the host machine is windows or not
func IsWindows() bool {
	if runtime.GOOS == "windows" {
		return true
	}
	return false
}
