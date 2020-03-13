#sed -i "s|@@BUILD_NUMBER@@|$Hyscale_Docker_Image_Tag.$BUILD_NUMBER|g" /var/jenkins_home/hyscale/$BUILD_NUMBER/hyscale-tool-ops/scripts/*
#sed -i "s|@@HYSCALE_BUILD_VERSION@@|$Hyscale_Docker_Image_Tag.$BUILD_NUMBER|g" /var/jenkins_home/hyscale/$BUILD_NUMBER/hyscale-tool-ops/scripts/*
#cp /var/jenkins_home/hyscale/$BUILD_NUMBER/hyscale-tool-ops/Dockerfile /var/jenkins_home/hyscale/$BUILD_NUMBER/hyscale-ctl/
#cd /var/jenkins_home/hyscale/$BUILD_NUMBER/hyscale-ctl/
docker build --pull -t gcr.io/hyscale-dev-env/hyscale/hyscale:test.$BUILD_NUMBER .
docker rmi -f gcr.io/hyscale-dev-env/hyscale/hyscale:test.$BUILD_NUMBER
echo $BUILD_NUMBER
