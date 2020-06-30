package cmd

import (
	
	"github.com/spf13/cobra"
)
// versionCommand represents 'hyscale version' command
var versionCommand = &cobra.Command{
	Use:   "version",
	Short: "Print the version of Hyscale",
	Hidden: true,
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
	RootCmd.AddCommand(versionCommand)
}
