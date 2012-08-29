## Installation process

Heliotrope uses sbt 0.11.2+ as its build tool. Guidance on how to install sbt 
is available at: https://github.com/harrah/xsbt/wiki/Getting-Started-Setup.

To run the test suite:

    $ sbt test

To compile everything and package a component as a war file:

    $ sbt package

Currently, Heliotrope introduces a fairly large set of dependencies into each
war file. These should probably be shared between components more effectively,
since they are typically the same for all the service subprojects. The UI 
subprojects are likely to be much smaller, since they are principally serving
static files.

## License and Copyright

Licensed under the GNU General Public License, Version 3.0. See LICENSE.txt for 
more details.
