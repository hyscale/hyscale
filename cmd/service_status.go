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

// statusCmd represents the 'hyscale service status' command
var statusCmd = &cobra.Command{
	Use:   "status",
	Short: "Get the status of the service",
	DisableFlagsInUseLine: true,	
	Run: func(cmd *cobra.Command, args []string) {

		clinput := CLIInput{
			Cmd:        cmd,
			Interative: false,
		}

		HyscaleRun(&clinput)
	},
}

func init() {
	GetServiceCmd.AddCommand(statusCmd)
	//TODO Allow with ',' delimiter also
	statusCmd.Flags().StringSliceP("service", "s", nil, "Service Name")
	//TODO accepting 'application' alias for App Name
	statusCmd.Flags().StringP("app", "a", "", "App Name")
	//TODO accepting 'ns' alias for namespace
	statusCmd.Flags().StringP("namespace", "n", "", "Namespace")
	//Required fields
	statusCmd.MarkFlagRequired("service")
	statusCmd.MarkFlagRequired("namespace")	
	statusCmd.MarkFlagRequired("app")

}
