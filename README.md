GPS web application
===================

GPS is an integrated system designed specifically to support the use of clinical genomics in personalized medicine. 
This application is designed to assist a study team, manage the processes involved in gathering sequencing data and
help experts correlate results with the clinical literature for reporting to physicians. The system tracks patients, 
samples, genomic sequencing information, clinical decisions, and reports across the cohort, monitors progress and 
sends reminders, and works alongside an electronic data capture system for the trial's clinical and genomic data. 
It incorporates systems to read, store, analyze and consolidate sequencing results from multiple technologies, 
and provides a curated knowledge base of mutations' tumor frequency (from the COSMIC database) annotated with clinical
significance and drug sensitivity to generate reports for clinicians.


Building GPS
------------

We use Maven 3 as a build tool, and this should download and generate any dependencies required. So if you 
need to build the application, all you need to do is:

```bash
git clone git@github.com:oicr-ibc/gps.git
cd gps
mvn
```


Running a local GPS
-------------------

Also using Maven, once you have built the code as above, it is easy to start running a local copy of the
application, using a command like this:

```bash
mvn -pl gps-webapp grails:run-app
```

This will start the application, by default at `http://localhost:8080/gps-webapp`. You can configure
the application by setting any local values you need, for example to change the authentication system,
in a local file at `~/.grails/gps-config.groovy`. 


Deploying GPS
-------------

If you want to deploy GPS on a Debian-based server, and happen to have installed the Debian packaging tools, you
can make a new Debian package using the following command from Maven:

```bash
mvn -P ci-build
```

This will assemble a new package, at `gps-server/target/gps-server-x.x.x.dpkg`, which can be moved to your
server and installed directly, using a command like:

```bash
sudo dpkg -i gps-server-x.x.x.dpkg
```

This is the recommended approach to server deployment, as the Debian package specifies all the dependencies, and
the installer process configures the database. You'll then find configuration files under `/etc/gps` and can't
start and stop the application using, for example, `/etc/init.d/gps start`. 
