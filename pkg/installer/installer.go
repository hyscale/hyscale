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
