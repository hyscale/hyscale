@echo off
SET USER=%USERNAME%
SET HYS_DIR=%userprofile%\.hyscale
SET HYSCALE=hyscale
SET USER_DIR=%cd%
SET DOCKER_CONF=%userprofile%\.docker\config.json
SET KUBE_CONF=%userprofile%\.kube\config
SET DOCKER_SOCK=/var/run/docker.sock
SET DOCKER_REPO_PATH=@@HYSCALE_DOCKER_REPO_PATH@@
SET HYSCALE_DATA_VOL=hyscale
SET HYSCALE_BUILD_VERSION=@@HYSCALE_BUILD_VERSION@@
SET HYSCALE_COMMAND=%*
SET HYSCALE_COMMAND=%HYSCALE_COMMAND:\=/%

mkdir %HYS_DIR%\%HYSCALE_DATA_VOL% > NUL
docker pull %DOCKER_REPO_PATH%/%HYSCALE%:%HYSCALE_BUILD_VERSION% > NUL

docker run ^
       --rm ^
       --label name=%HYSCALE% ^
       -v %USER_DIR%:/hyscale/app:ro ^
       -v %KUBE_CONF%:/hyscale/.kube/config:ro ^
       -v %DOCKER_CONF%:/hyscale/.docker/config.json:ro ^
       -v %HYS_DIR%\%HYSCALE_DATA_VOL%:/hyscale/hyscale ^
       -e HYSCALECTL_HOME=%HYS_DIR% ^
       -e HYSCALECTL_KUBECONF=%KUBE_CONF% ^
       -e HYSCALECTL_DOCKERCONF=%DOCKER_CONF% ^
       -e DOCKER_CONFIG=/hyscale/.docker ^
       -e HYSCALE_HOST_FS=\ ^
       -v %DOCKER_SOCK%:%DOCKER_SOCK%:ro ^
       %DOCKER_REPO_PATH%/%HYSCALE%:%HYSCALE_BUILD_VERSION% %HYSCALE_COMMAND%
