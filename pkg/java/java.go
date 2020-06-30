package java

import (

	installer "hyscale/pkg/installer"
)

//Jar is implementation
type Jar struct {
}

const (
	hyscale     = "hyscale"
	runnableJar = "java -Xms216m -Xmx512m-jar -jar"
	//jarBin      = hyscale + "-" + version.Version
)

// Run method for Java based installer
func (jar *Jar) Run(cliSpec *installer.DeploySpec) (resp *installer.DeployResponse, err error) {
	return resp, nil
}
