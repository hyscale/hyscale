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

// scaleServiceCmd represents the 'hyscale scale service' command
var scaleServiceCmd = &cobra.Command{
	Use:   "service",
	Short: "Scales the service of an application",
	DisableFlagsInUseLine: true,	
	Run: func(cmd *cobra.Command, args []string) {
		clinput := CLIInput{
			cmd,
			args,
			false,
		}
		HyscaleRun(&clinput)
	},
}

func init() {
	ScaleCmd.AddCommand(scaleServiceCmd)
	
	scaleServiceCmd.Flags().StringP("app", "a", "", "App Name")
	scaleServiceCmd.Flags().StringP("service", "s", "", "Service Name")
	scaleServiceCmd.Flags().StringP("namespace", "n", "", "Namespace")

	// TODO make these mutually exclusive
	scaleServiceCmd.Flags().String("up", "", "Scale up service by specified value")
	scaleServiceCmd.Flags().String("down", "", "Scale down service by specified value")
	scaleServiceCmd.Flags().String("to", "", "Scale service to a specified value")

	scaleServiceCmd.MarkFlagRequired("service")
	scaleServiceCmd.MarkFlagRequired("namespace")
	scaleServiceCmd.MarkFlagRequired("app")

}
