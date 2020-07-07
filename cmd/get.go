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

//GetCmd represents the 'hyscale get' command
var GetCmd = &cobra.Command{
	Use:   "get",
	Short: "Gets the specified resource",
	DisableFlagsInUseLine: true,
	Run: func(cmd *cobra.Command, args []string) {
		cmd.Usage()
	},
}

//GetServiceCmd represents the 'hyscale get service' command
var GetServiceCmd = &cobra.Command{
	Use:   "service",
	Short: "Performs GET action on the service",
	DisableFlagsInUseLine: true,
	Aliases: []string{"services"},
	Run: func(cmd *cobra.Command, args []string) {
        cmd.Usage()
	},
}

//GetAppCmd represents the 'hyscale get app' command
var GetAppCmd = &cobra.Command{
	Use:   "app",
	Short: "Gets the application ",
	Aliases: []string{"apps"},
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
    RootCmd.AddCommand(GetCmd)
    GetCmd.AddCommand(GetServiceCmd)
    GetCmd.AddCommand(GetAppCmd)
}
