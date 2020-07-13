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
	"log"
	"os"

	"github.com/spf13/cobra"
)

var (
	cfgFile string
	version bool
	//RootCmd is the variable defined for Hyscale Command
	RootCmd = &cobra.Command{
		Use:                   "hyscale",
		Short:                 "hyscale",
		DisableFlagsInUseLine: true,
		Run: func(cmd *cobra.Command, args []string) {
			if !version {
				cmd.Usage()
				return
			}
			clinput := CLIInput{
				cmd,
				args,
				false,
			}
			HyscaleRun(&clinput)

		},
	}
)

// RootCmd represents the base command when called without any subcommands.

// Execute adds all child commands to the root command and sets flags appropriately.
// This is called by main.main(). It only needs to happen once to the rootCmd.
func Execute() {
	if err := RootCmd.Execute(); err != nil {
		log.Fatal(err)
		os.Exit(1)
	}
}

func init() {
	RootCmd.Flags().BoolVarP(&version, "version", "v", false, "Hyscale Version")
}
