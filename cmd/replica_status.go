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
    opts "hyscale/cmd/options"
)

//getReplicaCmd represents the 'hyscale get service' command
var getReplicaCmd = &cobra.Command{
	Use:   "replica",
	Short: "get the service service replicas",
	Aliases: []string{"replicas"},
	DisableFlagsInUseLine: true,
	Run: func(cmd *cobra.Command, args []string) {
        cmd.Usage()
	},
}

//replicaStatusCmd represents the 'hyscale get replica status' command
var replicaStatusCmd = &cobra.Command{
	Use:                   "status",
	Short:                 "Get the status of the service replicas",
	DisableFlagsInUseLine: true,
	Run: func(cmd *cobra.Command, args []string) {

		clinput := CLIInput{
			Cmd:        cmd,
			Interative: true,
		}

		HyscaleRun(&clinput)
	},
}


func init() {
    GetCmd.AddCommand(getReplicaCmd)
    getReplicaCmd.AddCommand(replicaStatusCmd)
	
	replicaStatusCmd.Flags().StringP(opts.ServiceOpts.Option, opts.ServiceOpts.Shorthand, "", opts.ServiceOpts.Description)
	replicaStatusCmd.Flags().StringP(opts.AppOpts.Option,opts.AppOpts.Shorthand,"",opts.AppOpts.Description)
	replicaStatusCmd.Flags().StringP(opts.NamespaceOpts.Option, opts.NamespaceOpts.Shorthand, "", opts.NamespaceOpts.Description)
	//Required fields
	replicaStatusCmd.MarkFlagRequired(opts.ServiceOpts.Option)
	replicaStatusCmd.MarkFlagRequired(opts.AppOpts.Option)
	replicaStatusCmd.MarkFlagRequired(opts.NamespaceOpts.Option)
}
