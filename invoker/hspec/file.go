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

import (
	"errors"
	"io/ioutil"
	"gopkg.in/yaml.v2"
)

//LoadHspec loads the given hspecfile and return the hspec struct
func LoadHspec(hspecFile string) (Hspec, error) {
	hspec := Hspec{}

	if hspecFile == "" {
			err := errors.New("Invalid hspec file")
			//fmt.Println(err)
			return hspec, err
	}

	data, err := ioutil.ReadFile(hspecFile)
	if err != nil {
			return hspec,err
	}

	err = yaml.Unmarshal(data, &hspec)
	if err != nil {
			//fmt.Println("Error while unmarshling yaml")
			return hspec, err
	}

	return hspec, nil
}



