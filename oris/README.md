# Oris Model for the Concurrent Task Control

## Running this as Eclipse Project

* Install OpenJDK 11 ([sirio requirement](https://github.com/oris-tool/sirio#installation))

```sh
sudo add-apt-repository ppa:openjdk-r/ppa
sudo apt-get update 
sudo apt-get install openjdk-11-jdk
```

If you have more than one java versions installed, you need to set the default.

```sh
sudo update-alternatives --config java
```

And also javac

```sh
sudo update-alternatives --config javac
```

Also remember to `export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64`


* Manually install sirio as maven dependency (I had credential problems following the installation procedure described [here](https://github.com/oris-tool/sirio))

```sh
  mvn install:install-file \
   -Dfile=lib/sirio-2.0.0-SNAPSHOT.jar \
   -DgroupId=org.oris-tool \
   -DartifactId=sirio \
   -Dversion=2.0.0-SNAPSHOT \
   -Dpackaging=jar \
   -DgeneratePom=true
``` 

Then you are all set and could use this as an eclipse project.