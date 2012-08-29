grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        mavenLocal()
        mavenCentral()
        mavenRepo "http://maven.springframework.org/milestone"
		mavenRepo "http://snapshots.repository.codehaus.org"
        mavenRepo "http://repository.codehaus.org"
        mavenRepo "http://download.java.net/maven/2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        runtime 'mysql:mysql-connector-java:5.1.20'
		compile 'joda-time:joda-time:1.6.2'
		compile 'net.sf.opencsv:opencsv:2.0'
		compile 'ca.on.oicr:gps-variant-parser:0.0.2-RELEASE'
		
		// On Java 6 at least, this is needed to avoid a conflict
		compile('org.apache.xmlgraphics:fop:0.93') {
			excludes([group: 'xml-apis', name: 'xmlParserAPIs'])
		}
		
		test    'org.htmlparser:htmlparser:1.6'
		test    'org.easymock:easymock:3.0'
		test    'commons-io:commons-io:2.0.1'
		test    'org.gmock:gmock:0.8.2'
    }
}
