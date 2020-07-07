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

var scaleCmd = &cobra.Command{
	Use:   "scale",
	Short: "scale the resource",
    DisableFlagsInUseLine: true,
	Run: func(cmd *cobra.Command, args []string) {
		cmd.Usage()
	},
}

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
    RootCmd.AddCommand(scaleCmd)
    scaleCmd.AddCommand(scaleServiceCmd)
	
	scaleServiceCmd.Flags().StringP(opts.AppOpts.Option,opts.AppOpts.Shorthand,"",opts.AppOpts.Description)
	scaleServiceCmd.Flags().StringP(opts.NamespaceOpts.Option, opts.NamespaceOpts.Shorthand, "", opts.NamespaceOpts.Description)
	scaleServiceCmd.Flags().StringP(opts.ServiceOpts.Option, opts.ServiceOpts.Shorthand, "", opts.ServiceOpts.Description)

    scaleServiceCmd.MarkFlagRequired(opts.AppOpts.Option)
	scaleServiceCmd.MarkFlagRequired(opts.NamespaceOpts.Option)
	scaleServiceCmd.MarkFlagRequired(opts.ServiceOpts.Option)

	// TODO make these mutually exclusive
	scaleServiceCmd.Flags().String("up", "", "Scale up service by specified value")
	scaleServiceCmd.Flags().String("down", "", "Scale down service by specified value")
	scaleServiceCmd.Flags().String("to", "", "Scale service to a specified value")	
}
