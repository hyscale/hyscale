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
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"os"
	"strconv"
	"strings"

	hspec "hyscale/hspec"

	"github.com/docker/cli/cli/config/configfile"
	"github.com/docker/cli/cli/config/types"
)

const (
	//debugCredstoreEnv variable to debug the credstore flow
	debugCredstoreEnv = "DEBUG_CREDSTORE"
)

//GetAuthConfig retrieves the respective registy credentials from the docker config
func GetAuthConfig(hspecFiles []string) (*string, error) {

	RegistryMap := make(map[string]bool)

	for _, alias := range DockerHubAliases {
		RegistryMap[alias] = true
	}

	for _, each := range hspecFiles {
		hspec, err := hspec.LoadHspec(each)
		if err != nil {
			log.Fatal("Error while deserializing hspec", err)
		}

		registry := hspec.Image.Registry
		if registry != "" && !RegistryMap[registry] {
			RegistryMap[registry] = true
		}

		stkImg := hspec.Image.BuildSpec.StackImage
		if stkImg != "" && !RegistryMap[stkImg] {
			stackImgReg := getImageRegistry(stkImg)
			RegistryMap[stackImgReg] = true
		}

		if hspec.Agents != nil {
			for _, each := range *hspec.Agents {
				agent := each.Name
				if agent != "" && !RegistryMap[agent] {
					imgReg := getImageRegistry(agent)
					RegistryMap[imgReg] = true
				}
			}
		}
	}
	verbose, ok := strconv.ParseBool(os.Getenv(debugCredstoreEnv))
	if ok != nil {
		verbose = false
	}

	// Constructing all auth configs for the unique registries
	auth := make([]types.AuthConfig, 0)
	for key := range RegistryMap {
		//TODO read verbose from external configuration
		regCreds := GetRegistryCredentials(key, verbose)
		if regCreds != (types.AuthConfig{}) {
			auth = append(auth, regCreds)
		}
	}

	data, err := convert(auth)
	if err != nil {
		log.Fatal("Error while reading config.json")
	}

	authString, err := json.Marshal(data)
	if err != nil {
		log.Fatal("Error while deserializing auth ")
	}
	json := string(authString)
	if verbose {
		fmt.Println(json)
	}
	return &json, nil
}

//GetRegistryCredentials fetches the registry credentials from docker config json
func GetRegistryCredentials(registry string, verbose bool) types.AuthConfig {
	if registry == "" {
		return types.AuthConfig{}
	}

	auth, err := GetCredentials(registry, verbose)
	if err != nil {
		log.Fatal(errors.New("Error while fetching credentials for registry"), registry, err)
	}

	return auth
}

func convert(auths []types.AuthConfig) (*configfile.ConfigFile, error) {
	if auths == nil {
		// Message
	}

	authConfigs := make(map[string]types.AuthConfig)
	for _, each := range auths {

		tmp := each.Username + ":" + each.Password
		each.Auth = base64.StdEncoding.EncodeToString([]byte(tmp))
		each.Username = ""
		each.Password = ""
		authConfigs[each.ServerAddress] = each

	}
	configfile := configfile.ConfigFile{
		AuthConfigs: authConfigs,
	}
	return &configfile, nil
}

func getImageRegistry(img string) string {
	parts := strings.Split(strings.Split(img, ":")[0], "/")
	return parts[0]
}
