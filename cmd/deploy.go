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
var(
	hspecList []string
)

// DeployCmd represents the 'hyscale deploy' command
var DeployCmd = &cobra.Command{
	Use:   "deploy",
	Short: "Deploys the specified resource",
	DisableFlagsInUseLine: true,
	Run: func(cmd *cobra.Command, args []string) {
		cmd.Usage()
	},
}

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
    RootCmd.AddCommand(DeployCmd)
    DeployCmd.AddCommand(ServiceCmd)

    ServiceCmd.Flags().StringP(opts.AppOpts.Option,opts.AppOpts.Shorthand,"",opts.AppOpts.Description)
    //TODO accepting 'ns' alias for namespace
	ServiceCmd.Flags().StringP(opts.NamespaceOpts.Option, opts.NamespaceOpts.Shorthand, "", opts.NamespaceOpts.Description)
	ServiceCmd.Flags().StringSliceVarP(&hspecList,opts.HspecOpts.Option, opts.HspecOpts.Shorthand, []string{}, opts.HspecOpts.Description)
	ServiceCmd.Flags().BoolP("verbose", "v", false, "Verbose output")

	//TODO making profiles exclusive
	ServiceCmd.Flags().StringSliceP(opts.HprofOpts.Option,opts.HprofOpts.Shorthand, []string{}, opts.HprofOpts.Description)
	ServiceCmd.Flags().StringP(opts.ProfileNameOpts.Option, opts.ProfileNameOpts.Shorthand, "", opts.ProfileNameOpts.Description)

	//Required fields
	ServiceCmd.MarkFlagRequired(opts.HspecOpts.Option)
	ServiceCmd.MarkFlagRequired(opts.NamespaceOpts.Option)
	ServiceCmd.MarkFlagRequired(opts.AppOpts.Option)
}
