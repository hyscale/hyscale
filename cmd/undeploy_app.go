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

//UndeployAppCmd represents the 'hyscale undeploy app' command
var UndeployAppCmd = &cobra.Command{
	Use:   "app",
	Short: "Undeploys app from the kubernetes cluster",
	DisableFlagsInUseLine: true,
	Run: func(cmd *cobra.Command, args []string) {
		cmd.Usage()
	},
}

func init() {
	UndeployCmd.AddCommand(UndeployAppCmd)
	
	UndeployAppCmd.Flags().StringP("app", "a", "", "App Name")
	UndeployAppCmd.MarkFlagRequired("app")

	//TODO accepting 'ns' alias for namespace
	UndeployAppCmd.Flags().StringP("namespace", "n", "", "Namespace")
	UndeployAppCmd.MarkFlagRequired("namespace")
}
