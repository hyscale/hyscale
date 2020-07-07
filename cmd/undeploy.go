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

// UndeployCmd represents the 'hyscale undeploy' command
var undeployCmd = &cobra.Command{
	Use:   "undeploy",
	Short: "Undeploys a resource",
	DisableFlagsInUseLine: true,	
	Run: func(cmd *cobra.Command, args []string) {
		cmd.Usage()
	},
}

//appUndeployCmd represents the 'hyscale undeploy app' command
var appUndeployCmd = &cobra.Command{
	Use:   "app",
	Short: "Undeploys app from the kubernetes cluster",
	DisableFlagsInUseLine: true,
	Run: func(cmd *cobra.Command, args []string) {
		clinput := CLIInput{
			Cmd:        cmd,
			Interative: false,
		}
		HyscaleRun(&clinput)
	},
}

var serviceUndeployCmd = &cobra.Command{
	Use:   "service",
	Short: "Undeploys a service",
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
    RootCmd.AddCommand(undeployCmd)
    undeployCmd.AddCommand(appUndeployCmd)
    undeployCmd.AddCommand(serviceUndeployCmd)

    appUndeployCmd.Flags().StringP(opts.AppOpts.Option,opts.AppOpts.Shorthand,"",opts.AppOpts.Description)
    appUndeployCmd.MarkFlagRequired(opts.AppOpts.Option)
	//TODO accepting 'ns' alias for namespace
	appUndeployCmd.Flags().StringP(opts.NamespaceOpts.Option, opts.NamespaceOpts.Shorthand, "", opts.NamespaceOpts.Description)
    appUndeployCmd.MarkFlagRequired(opts.NamespaceOpts.Option)
    
    
    serviceUndeployCmd.Flags().StringP(opts.AppOpts.Option,opts.AppOpts.Shorthand,"",opts.AppOpts.Description)
	//TODO accepting 'ns' alias for namespace
	serviceUndeployCmd.Flags().StringP(opts.NamespaceOpts.Option, opts.NamespaceOpts.Shorthand, "", opts.NamespaceOpts.Description)
	// Allow with ',' delimiter also
    serviceUndeployCmd.Flags().StringSliceP(opts.ServiceOpts.Option, opts.ServiceOpts.Shorthand, nil, opts.ServiceOpts.Description)
    
    //Required fields
    serviceUndeployCmd.MarkFlagRequired(opts.ServiceOpts.Option)
    serviceUndeployCmd.MarkFlagRequired(opts.AppOpts.Option)
    serviceUndeployCmd.MarkFlagRequired(opts.NamespaceOpts.Option)
	
	
}
