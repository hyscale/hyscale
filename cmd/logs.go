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

// 'hyscale get service logs' command
var logsCmd = &cobra.Command{
	Use:   "logs",
	Short: "Displays the service logs",
	DisableFlagsInUseLine: true,
	Run: func(cmd *cobra.Command, args []string) {
		clinput := CLIInput{
			Cmd:        cmd,
			Args:       args,
			Interative: true,
		}
		HyscaleRun(&clinput)
	},
}

func init() {
	GetServiceCmd.AddCommand(logsCmd)
	logsCmd.Flags().StringP(opts.AppOpts.Option,opts.AppOpts.Shorthand,"",opts.AppOpts.Description)
	logsCmd.Flags().StringP(opts.ServiceOpts.Option, opts.ServiceOpts.Shorthand, "", opts.ServiceOpts.Description)
	logsCmd.Flags().StringP(opts.NamespaceOpts.Option, opts.NamespaceOpts.Shorthand, "", opts.NamespaceOpts.Description)
	logsCmd.Flags().StringP("replica", "r", "", "Replica Name")
	logsCmd.Flags().BoolP("tail", "t", false, "Tail output of the service logs")
	logsCmd.Flags().IntP("line", "l", 50, "To specify number of lines")
	
	logsCmd.MarkFlagRequired(opts.ServiceOpts.Option)
	logsCmd.MarkFlagRequired(opts.NamespaceOpts.Option)
	logsCmd.MarkFlagRequired(opts.AppOpts.Option)
}
