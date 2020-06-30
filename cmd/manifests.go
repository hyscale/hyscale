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
package cmd

import (

	"github.com/spf13/cobra"
)

var (
	//Files is the flag for defining the hspecs
	Files = "files"
	hspecs []string
)

//ManifestCmd represents the 'hyscale generate service manifests' command
var ManifestCmd = &cobra.Command{
	Use:   "manifests",
	Short: "Generates manifests from the given service specs",
	DisableFlagsInUseLine: true,
	
	Run: func(cmd *cobra.Command, args []string) {
			clinput := CLIInput{
			Cmd:        cmd,
			Args:       hspecs,
			Interative: false,
		}
		HyscaleRun(&clinput)
	},
}

func init() {
	GenerateServiceCmd.AddCommand(ManifestCmd)

	ManifestCmd.Flags().StringP("app", "a", "", "App Name")
	ManifestCmd.Flags().StringSliceVarP(&hspecs,"files", "f", []string{}, "Service Spec Files")
	ManifestCmd.Flags().BoolP("verbose", "v", false, "Verbose output")

	//TODO making profiles exclusive
	ManifestCmd.Flags().StringSlice("profile", []string{}, "Profile files")
	ManifestCmd.Flags().StringP("", "P", "", "Profile Name")

	//Required fields
	ManifestCmd.MarkFlagRequired("files")
	ManifestCmd.MarkFlagRequired("app")
}
