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

package hspec

//Hspec defines the struct for hyscale service spec
//TODO complete the hspec struct with other fields
type Hspec struct {
	Name  string `yaml:"name"`
	Image struct {
		Registry  string    `yaml:"registry"`
		Name      string    `yaml:"name"`
		Tag       string    `yaml:"tag"`
		BuildSpec BuildSpec `yaml:"buildSpec"`
	} `yaml:"image"`
	Agents *[]Agents `yaml:"agents"`
}

//BuildSpec defines how to build the service
type BuildSpec struct {
	StackImage           string      `yaml:"stackImage"`
	Artifacts            *[]Artifact `yaml:"artifacts"`
	ConfigCommands       string      `yaml:"configCommands"`
	ConfigCommandsScript string      `yaml:"configCommandsScript"`
	RunCommands          string      `yaml:"runCommands"`
	RunCommandsScript    string      `yaml:"runCommandsScript"`
}

//Artifact specifies the service binary to be built inside the Image
type Artifact struct {
	Name        string `yaml:"name"`
	Source      string `yaml:"source"`
	Destination string `yaml:"destination"`
}

// Agents define the sidecars of any service
type Agents struct {
	Name              string            `yaml:"name"`
	Image             string            `yaml:"image"`
	Props             map[string]string `yaml:"props,omitempty"`
	PropsVolumePath   PropsVolumePath   `yaml:"propsVolumePath,omitempty"`
	Secrets           interface{}       `yaml:"secrets,omitempty"`
	SecretsVolumePath SecretsVolumePath `yaml:"secretsVolumePath,omitempty"`
	VolumesFrom       []*VolumesFrom    `yaml:"volumes,omitempty"`
}

// PropsVolumePath defines the volume path of the Props defined in hspec
type PropsVolumePath string

// SecretsVolumePath defines the volume path of the Secrets defined in hspec
type SecretsVolumePath string

// VolumesFrom defines the volume to be mounted from the volumes defined for the service
type VolumesFrom struct {
	MountPath string `yaml:"mountPath"`
	Attach    string `yaml:"attach"`
}
