Neo4j resources library
=======================

The Neo4j resources library allows you to build an application which stores its data in
the [http://neo4j.org/](Neo4j open source graph database) and exposes it through a
domain-specific RESTful JSON API which you define. It is written in Scala and is intended
to be used in other Scala projects, although you may be able to get it to work with
other JVM-based langauges too. Projects using this library can build amazing databases
with a minimum of boilerplate and unnecessary code.

Please see the [Neo4j Scala template](http://github.com/ept/neo4j-scala-template/tree/master)
as a starting point for your application based on the Neo4j resources library.


Building
--------

You need a Java 5 (or newer) environment and Maven 2.0.9 (or newer) installed:

    $ mvn --version
    Maven version: 2.0.10
    Java version: 1.6.0_03-p3
    OS name: "darwin" version: "9.7.0" arch: "i386" Family: "unix"

Install the [JUnit4 Runner for ScalaTest](http://github.com/teigen/scalatest-junit4runner/tree/master)
as follows:

    $ git clone git://github.com/teigen/scalatest-junit4runner.git
    $ cd scalatest-junit4runner
    $ mvn clean install

With that dependency manually resolved, you should now be able to do a full build of
`neo4j-resources`:

    $ git clone git://github.com/ept/neo4j-resources.git
    $ cd neo4j-resources
    $ mvn clean install

To use this library in your projects, add the following to the `dependencies` section of your
`pom.xml`:

    <dependency>
      <groupId>com.eptcomputing</groupId>
      <artifactId>neo4j-resources</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>

If you don't use Maven, take `target/neo4j-resources-1.0-SNAPSHOT.jar` and all of its dependencies,
and add them to your classpath.

To use the project in Eclipse, you must have the Eclipse Scala plugin installed.
You should also do a full Maven build before using Eclipse, to ensure you have
all the dependencies downloaded. Then you should be able to do
"File -> Import -> General -> Existing Projects into Workspace"
and be ready to go. Note that at the time of writing, the Eclipse Scala
plugin appears to have a bug which causes it not to write any class files to the
target directory.


Troubleshooting
---------------

If you're using a Java 6 JDK, you may get an error like "JAXB 2.0 API is being
loaded from the bootstrap classloader, but this RI needs 2.1 API" when building
this project. You can fix this by setting the following environment variable:

    export MAVEN_OPTS="-Djava.endorsed.dirs=$HOME/.m2/repository/javax/xml/bind/jaxb-api/2.1"

Depending on your operating system you may need to to adjust the path above to point
to your Maven repository.


Using this library
------------------

Setting everything up to build correctly can be a bit of a nightmare, which is why I have
prepared a [Neo4j Scala template project](http://github.com/ept/neo4j-scala-template/tree/master)
to make it easy. It contains the full barebones structure for a Scala web project based on this
library, including examples of how to use it and how to test it. (You *do* test everything,
right?)


License
-------

Copyright (c) 2009 Martin Kleppmann.

This program is free software: you can redistribute it and/or modify it under the terms of
the GNU Affero General Public License as published by the Free Software Foundation, version 3.
See `LICENSE.txt` for details.

Please note that the Affero GPL does not allow you to use this software in a closed-source
web application. If distributing the source of your application is not acceptable to you,
please contact [Ept Computing](http://www.eptcomputing.com/) to obtain a commercial license.
