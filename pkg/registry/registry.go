package registry

import (
	"fmt"
	"os"
	"path/filepath"

	"hyscale/pkg/constants"

	"github.com/docker/cli/cli/config"
	"github.com/docker/cli/cli/config/types"
)

var (
	configDir = os.Getenv("DOCKER_CONFIG")
	//DockerHubAliases define the alias host names to which docker pull, docker push can be resolved with auth credentials of these aliases
	DockerHubAliases = []string{"index.docker.io", "registry-1.docker.io", "docker.io", "registry.hub.docker.com"}
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

		if authConfig.Username != "" {
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
		configDir = filepath.Join(HomeDir, constants.DockerDir)
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

	if containsAlias(DockerHubAliases, registry) {
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
