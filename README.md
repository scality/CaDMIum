CaDMIum
=======

CaDMIum is a set of opensource filesystem tools:
- cdmi is a CDMI client java library
- sofs.utils is a library providing a modification notifier for distributed filesystems


Versions
------------

The current version of the project is 2.0.0.

Usage
------------

For each project, checkout the project and run the maven build with the following command :

```shell
mvn clean package
```

cdmi
------------
Please note that you need to configure your cdmi server, user and password for integration tests in: 
cdmi/src/test/resources/integrationtest.properties

sofs.utils
------------
This project is using new features from Java 1.7: It provides a java.nio.WatchService implementation.
If you still want to use earlier Java version, you will have to rely on the legacy package, which is Java 1.6 compliant and provides a limited implementation.
