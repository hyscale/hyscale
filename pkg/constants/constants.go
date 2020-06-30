package constants

import (
	"os/user"
)

const (
	// Hyscale defines the constant hyscale
	Hyscale = "hyscale"
	//DockerDir defines the .docker dir
	DockerDir = ".docker"
)

var (
	//User defines the current user executing the hyscale binary
	User, _ = user.Current()

	// HysDir defines the hyscale directory of the 'User'
	HysDir = User.HomeDir + "/." + Hyscale

	//WindowsInternalDockerHost specifies the host address of where docker daemon is running
	WindowsInternalDockerHost = "tcp://host.docker.internal:2375"
)
