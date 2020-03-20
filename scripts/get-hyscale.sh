#!/bin/bash
set -e

BUCKET_NAME=${1:-"prod.downloads.hyscale.io"}

DEFAULT_HYSCALE_VERSION=latest
HYSCALE_TOOL_VERSION=${DEFAULT_HYSCALE_VERSION}

if [ -n "$HYS_VERSION" ]; then
	HYSCALE_TOOL_VERSION=$HYS_VERSION
fi

USER_LOCAL_BIN=/usr/local/bin
HYSCALE=hyscale
HYSCALE_BUCKET_URL="https://s3-us-west-2.amazonaws.com/$BUCKET_NAME/$HYSCALE/release"
HYSCALE_BINARY="hyscale-${HYSCALE_TOOL_VERSION}-linux-amd64"

command_exists() {
        command -v "$@" > /dev/null 2>&1
}

echo "Downloading $HYSCALE ..."
 
if [ $(curl -sL -w "%{http_code}" "$HYSCALE_BUCKET_URL/$HYSCALE_TOOL_VERSION/$HYSCALE" -o $HYSCALE_BINARY) != 200 ];then
     echo -e "\nDownload Failed !!!"
     echo -e "\nSpecified HyScale version $HYSCALE_TOOL_VERSION doesn't exist.\nRetry by providing the valid HyScale version\n\t (or) \nunset the \"HYS_VERSION\" variable to get latest HyScale version.\n"
     exit 1
fi

echo -e "Download successful\n"

chmod +x $HYSCALE_BINARY

user="$(id -un 2>/dev/null || true)"
sh_c='sh -c'
if [ "$user" != 'root' ]; then
		if command_exists sudo; then
			sh_c='sudo -E sh -c'
		elif command_exists su; then
			sh_c='su -c'
		else
			cat >&2 <<-'EOF'
			Warning: HyScale installer needs the ability to run commands as root.
			We are unable to find either "sudo" or "su" available to make this happen.
                        HyScale download is complete, Move $HYSCALE_BINARY as $USER_LOCAL_BIN/$HYSCALE or add to your path to start using $HYSCALE.
			EOF
			exit 1
		fi
fi
$sh_c "mv $HYSCALE_BINARY $USER_LOCAL_BIN/$HYSCALE"

echo -e "Initialising $HYSCALE ...\n"

$USER_LOCAL_BIN/$HYSCALE

echo -e "\nSetup complete. Open a new terminal to use $HYSCALE \n\n" 
