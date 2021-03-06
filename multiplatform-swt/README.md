Info
----
This directory contains  a clone of
[Multiplatform SWT](https://github.com/jendap/multiplatform-swt) maven project
to combine with [SWET](https://github.com/sergueik/selenium_java/tree/master/swd_recorder).

``` bash
rm -rf src/main
cp -R ../swd-recorder/src/main src
mvn package
java -jar example/target/swet-0.0.5-SNAPSHOT.jar
```

Usage
-----

* Note that the project consists of two modules.
* First build and install the swt-multiplatform-loader module (you will use
  the swt-multiplatform-loader-VERSION-multiplatform.jar as a dependency in you project)
``` bash
mvn install
```
* Copy everything from `example/pom.xml` into your pom.xml - from 'properties' down.
  - `dependencies` including scopes and classifiers
  - `maven-dependency-plugin`
  - `spring-boot-maven-plugin`
  - `repositories` section
  - `profiles` section
sections with their configurations

* Change your main.class property