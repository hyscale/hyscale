package hspec

import (
	"errors"
	
	//"fmt"
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



