package installer

// DeploySpec specifies the inputs to the hyscale binary
type DeploySpec struct {
	Interactive bool
	Hspecs      []string
}

// DeployResponse the response status after the command execution
type DeployResponse struct {
	Status string
	ExitCode int
}

// Installer defines the interface for various types of hyscale installers
type Installer interface {
	Run(cliSpec *DeploySpec) (resp *DeployResponse, err error)
}
