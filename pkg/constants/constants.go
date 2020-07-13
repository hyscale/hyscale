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

package constants

import (
	"os/user"
)

const (
	// Hyscale defines the constant hyscale
	Hyscale = "hyscale"
	//DockerDir defines the .docker dir
	DockerDir = ".docker"
	//ImageName Defines the latest image name of the HyScale Image
	ImageName = "@@IMAGE_NAME@@"
	// ImageTag Defines the latest image tag of the HyScale Image
	ImageTag = "@@IMAGE_TAG@@"
)

var (
	//User defines the current user executing the hyscale binary
	User, _ = user.Current()

	// HysDir defines the hyscale directory of the 'User'
	HysDir = User.HomeDir + "/." + Hyscale

	//WindowsInternalDockerHost specifies the host address of where docker daemon is running
	WindowsInternalDockerHost = "tcp://host.docker.internal:2375"
)
