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
   limitations under the License.*/
package cmd

import (
    "github.com/spf13/cobra"
    opts "hyscale/cmd/options"
)

var (
	//Files is the flag for defining the hspecs
	Files = "files"
	hspecs []string
)

//GenerateCmd represents the 'hyscale generate' command
var generateCmd = &cobra.Command{
	Use:   "generate",
	Short: "Generates a resource",
	DisableFlagsInUseLine: true,
	Run: func(cmd *cobra.Command, args []string) {
		cmd.Usage()
	},
}

// GenerateServiceCmd represents the 'hyscale generate service' command
var generateServiceCmd = &cobra.Command{
	Use:   "service",
	Short: "Performs 'generate' action on the service",
	DisableFlagsInUseLine: true,
	Run: func(cmd *cobra.Command, args []string) {
		cmd.Usage()
	},
}

//ManifestCmd represents the 'hyscale generate service manifests' command
var manifestCmd = &cobra.Command{
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
    RootCmd.AddCommand(generateCmd)
    generateCmd.AddCommand(generateServiceCmd)
    generateServiceCmd.AddCommand(manifestCmd)

    manifestCmd.Flags().StringP(opts.AppOpts.Option,opts.AppOpts.Shorthand,"",opts.AppOpts.Description)
	manifestCmd.Flags().StringSliceVarP(&hspecs,opts.HspecOpts.Option, opts.HspecOpts.Shorthand, []string{}, opts.HspecOpts.Description)
	manifestCmd.Flags().BoolP("verbose", "v", false, "Verbose output")

	//TODO making profiles exclusive
	manifestCmd.Flags().StringSliceP(opts.HprofOpts.Option,opts.HprofOpts.Shorthand, []string{}, opts.HprofOpts.Description)
	manifestCmd.Flags().StringP(opts.ProfileNameOpts.Option, opts.ProfileNameOpts.Shorthand, "", opts.ProfileNameOpts.Description)

	//Required fields
	manifestCmd.MarkFlagRequired(opts.HspecOpts.Option)
	manifestCmd.MarkFlagRequired(opts.AppOpts.Option)

}
