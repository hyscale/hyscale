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

package credstore

import (
	"fmt"
	"os"
	"path/filepath"

	"github.com/docker/cli/cli/config"
	"github.com/docker/cli/cli/config/types"
)

const (
	//DockerDir defines the .docker dir
	DockerDir = ".docker"
)

var (
	configDir = os.Getenv("DOCKER_CONFIG")
	//DockerHubAliases define the alias host names to which docker pull, docker push can be resolved with auth credentials of these aliases
	DockerHubAliases = []string{"index.docker.io", "registry-1.docker.io", "docker.io", "index.docker.io/v1/"}
)

//GetCredentialsFromConfig fetches the docker registry credentials from the specified configDir
func GetCredentialsFromConfig(registry string, verbose bool, configDir string) (types.AuthConfig, error) {
	var authConfig types.AuthConfig

	// Loading config.json file to ConfigFile object
	configFile, err := config.Load(configDir)
	if err != nil {
		fmt.Println("Error while loading config.json")
		fmt.Println(err)
		return authConfig, err
	}

	// Iterating over all possible values to
	for _, each := range getSearchPatterns(registry) {
		FetchedStore := configFile.GetCredentialsStore(each)

		if verbose {
			fmt.Println("Fetching credentials", each)
		}
		authConfig, err = FetchedStore.Get(each)

		if err != nil {
			fmt.Println("Error while fetching credentials from config \n", err)
			return authConfig, err
		}

		if authConfig.Username != "" && authConfig.ServerAddress != "" {
			return authConfig, nil
		}
	}
	return authConfig, nil
}

//GetCredentials fetches the docker registry credentials from default config json of the current user
func GetCredentials(registry string, verbose bool) (types.AuthConfig, error) {
	// Constructing docker config json from home directory if DOCKER_CONFIG env is found empty
	var authConfig types.AuthConfig
	if configDir == "" {
		HomeDir, err := os.UserHomeDir()
		if err != nil {
			fmt.Println("Error while fetching home directory", err)
			return authConfig, err
		}
		configDir = filepath.Join(HomeDir, DockerDir)
	}

	return GetCredentialsFromConfig(registry, verbose, configDir)
}

func containsAlias(aliases []string, registry string) bool {
	for _, s := range aliases {
		if s == registry {
			return true
		}
	}
	return false
}

func getSearchPatterns(registry string) []string {
	SearchRegistryAliases := []string{registry}

	if registry == "" {
		SearchRegistryAliases = DockerHubAliases
	}
	searchPatterns := []string{}
	for _, each := range SearchRegistryAliases {
		searchPatterns = append(searchPatterns, getRegistrySearchPatterns(each)...)
	}
	return searchPatterns
}

func getRegistrySearchPatterns(registry string) []string {

	exactMatch := registry
	withHTTPS := "https://" + registry
	withSuffix := registry + "/"
	withHTTPSAndSuffix := "https://" + registry + "/"

	RegistryPatterns := []string{exactMatch, withHTTPS, withSuffix, withHTTPSAndSuffix}
	return RegistryPatterns

}
