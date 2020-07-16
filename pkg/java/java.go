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
