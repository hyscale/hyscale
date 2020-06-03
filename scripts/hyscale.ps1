$HYS_VERSION="@@HYSCALE_BUILD_VERSION@@"
$HYS_JAR_BIN="hyscale-${HYS_VERSION}.jar"
$dir= "$home\.hyscale"
$fileToCheck = "$dir\$HYS_JAR_BIN"
$HYS_DOWNLOAD_URL = "@@HYSCALE_URL@@"

Function check_java_version
{
  java --version | Out-Null
  If (-NOT  ($? -eq "True")) {
    'Either Java is not installed or found a lesser version of java'
     exit 1

    }
  $JAVA_VERSION=(Get-Command java | Select-Object -ExpandProperty Version).tostring() | %{ $_.Split('.')[0]; }
  if ($JAVA_VERSION -as [int] -lt 11){
     'JDK version 11 and above is required but found lesser version'
      exit 1
  }
}

Function download_hyscale_jar
{
  if(!(Test-Path -Path $dir )){
      New-Item -ItemType directory -Path $dir | Out-Null
  }

  #Removing old Jar and Downloading the latest jar
  if (!(Test-Path $fileToCheck -PathType leaf))
  {
     rm $dir\hyscale-*.jar
     "Downloading hyscale ..."
     wget $HYS_DOWNLOAD_URL -outfile $HYS_JAR_BIN | Out-Null
     "Download successful"
     mv $HYS_JAR_BIN $dir
  }
}

check_java_version
download_hyscale_jar

java -Xms216m -Xmx512m -jar $dir\$HYS_JAR_BIN $args
