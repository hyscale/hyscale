module hyscale

go 1.14

require (
	github.com/docker/cli v0.0.0-20191017083524-a8ff7f821017
	github.com/docker/docker v0.0.0-00010101000000-000000000000
	github.com/docker/docker-credential-helpers v0.6.3 // indirect
	github.com/opencontainers/runc v0.1.1 // indirect
	github.com/pkg/errors v0.9.1 // indirect
	gopkg.in/yaml.v2 v2.2.8
)

replace github.com/docker/docker => github.com/docker/engine v1.13.1
