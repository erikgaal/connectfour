Connect 4
=========

Dependencies
------------
This project depends on the LWJGL 2.9.1 library which implements the C OpenGL bindings to be used in Java.
Using maven to download the dependencies in the pom.xml file you can build or run the project as usual.
http://legacy.lwjgl.org/

Building
--------

CLIENT
- Use maven to fetch the dependencies.
- Optional: download these dependencies yourself and add them in your IDE or classpath when building.

Running
-------

CLIENT
- Add `-Djava.library.path=lib/natives/<youros>/ -Xms4G -Xmx4G` to the VM options
- Run `connectfour.Main` using your IDE

SERVER
- Run `server.Main`

ROGUE
- Run `Rogue.Main`
