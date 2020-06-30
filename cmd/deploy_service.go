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
var(

	hspecList []string
)
//ServiceCmd represents the 'hyscale deploy service' command
var ServiceCmd = &cobra.Command{
	Use:   "service",
	Short: "Deploys the services to k8s cluster",
	DisableFlagsInUseLine: true,
	Run: func(cmd *cobra.Command, args []string) {
		clinput := CLIInput{
			Cmd:        cmd,
			Args:       hspecList,
			Interative: false,
		}

		HyscaleRun(&clinput)
	},
}

func init() {
	DeployCmd.AddCommand(ServiceCmd)

	ServiceCmd.Flags().StringP("app", "a", "", "App Name")
	//TODO accepting 'ns' alias for namespace
	ServiceCmd.Flags().StringP("namespace", "n", "", "Namespace")
	ServiceCmd.Flags().StringSliceVarP(&hspecList,"files", "f", []string{}, "Service Spec Files")
	ServiceCmd.Flags().BoolP("verbose", "v", false, "Verbose output")

	//TODO making profiles exclusive
	ServiceCmd.Flags().StringSlice("profile", []string{}, "Profile files")
	ServiceCmd.Flags().StringP("", "P", "", "Profile Name")

	//Required fields
	ServiceCmd.MarkFlagRequired("files")
	ServiceCmd.MarkFlagRequired("namespace")
	ServiceCmd.MarkFlagRequired("app")
}
