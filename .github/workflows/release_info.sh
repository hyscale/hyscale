#!/bin/bash
set -euo pipefail

export CHANGELOG=`git log  HEAD...$PREVIOUS_TAG --oneline`
