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
package options

//CmdOption represents the option of the command 
type CmdOption struct{
     Option string
     Shorthand string
     Description string
}

var (
    //AppOpts gives options for the application resource
    //TODO accepting 'application' alias for App Name
    AppOpts = &CmdOption{Option:"app",Shorthand:"a",Description : "App name"}
    //NamespaceOpts gives options for the Namespace resource
    //TODO accepting 'ns' alias for namespace
    NamespaceOpts = &CmdOption{Option:"namespace",Shorthand:"n",Description : "namespace"}
    //ServiceOpts represents option for the service resource
    //TODO Allow with ',' delimiter also
    ServiceOpts = &CmdOption{Option:"service",Shorthand:"s",Description : "service name"}
    //HspecOpts represents option for the hspec 
    HspecOpts = &CmdOption{Option:"files",Shorthand:"f",Description : "Service Spec Files"}
    //HprofOpts represents option for the hprof 
    HprofOpts = &CmdOption{Option:"profile",Shorthand:"p",Description : "Profile files"}
    //ProfileNameOpts represents -P option 
    ProfileNameOpts = &CmdOption{Option:"",Shorthand:"P",Description : "Profile Name"}
)
