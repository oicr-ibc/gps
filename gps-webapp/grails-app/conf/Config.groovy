// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails.project.groupId = GPS // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
					  zip: 'application/zip',
                      multipartForm: 'multipart/form-data'
                    ]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = false

grails.mail.host = "smtp.oicr.on.ca"

// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

// add support for optional external configuration, i.e., let's get secure stuff
// out of here. 
grails.config.locations = []
def defaultConfigFiles = [
    "/etc/heliotrope/heliotrope-config.properties",
    "/etc/heliotrope/heliotrope-config.groovy",  
    "${userHome}/.grails/heliotrope-config.properties",
    "${userHome}/.grails/heliotrope-config.groovy"
]

defaultConfigFiles.each { filePath ->
    def f = new File(filePath)
    if (f.exists()) {
        grails.config.locations << "file:${filePath}"
    }
}

// set per-environment serverURL stem for creating absolute links
// added authentication so that development system can authenticate
environments {
    production {
        grails.serverURL = "https://gps.oicr.on.ca"
		grails.plugins.springsecurity.ldap.active = true
		grails.plugins.springsecurity.ldap.context.server = 'ldap://ldap.oicr.on.ca/'
		grails.plugins.springsecurity.ldap.context.anonymousReadOnly=true
		grails.plugins.springsecurity.ldap.search.base = 'dc=oicr,dc=on,dc=ca'
		grails.plugins.springsecurity.ldap.authorities.groupSearchBase = 'ou=Groups,dc=oicr,dc=on,dc=ca'
		grails.plugins.springsecurity.ldap.authorities.groupSearchFilter = 'memberUid={1}'
		grails.plugins.springsecurity.ldap.authorities.retrieveDatabaseRoles = true
    }
    staging {
        grails.serverURL = "http://localhost:8080/${appName}"
		grails.plugins.springsecurity.ldap.active = true
		grails.plugins.springsecurity.ldap.context.server = 'ldap://ldap.oicr.on.ca/'
		grails.plugins.springsecurity.ldap.context.anonymousReadOnly=true
		grails.plugins.springsecurity.ldap.search.base = 'dc=oicr,dc=on,dc=ca'
		grails.plugins.springsecurity.ldap.authorities.groupSearchBase = 'ou=Groups,dc=oicr,dc=on,dc=ca'
		grails.plugins.springsecurity.ldap.authorities.groupSearchFilter = 'memberUid={1}'
		grails.plugins.springsecurity.ldap.authorities.retrieveDatabaseRoles = true
		grails.gsp.enable.reload=true
    }
    development {
        grails.serverURL = "http://localhost:8080/${appName}"	
		grails.plugins.springsecurity.ldap.active = false
		grails.gsp.enable.reload=true
    }
    test {
        grails.serverURL = "http://localhost:8080/${appName}"	
		grails.plugins.springsecurity.ldap.active = false
		grails.gsp.enable.reload=true
    }
}

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    
    appenders {
        console name:'stdout', layout:pattern(conversionPattern: '[%d{HH:mm:ss,SSS}] %c %p - %m%n')
    }

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.springframework',
           'net.sf.ehcache',
		   'net.sf.ehcache.hibernate.AbstractEhcacheRegionFactory'
		   
	// During development especially, we want a sensible level of debugging throughout
	// the application. This probably ought to be a little more relaxed when in
	// production. This can be set using environment-specific blocks.
	
	warn   'org.hibernate',
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'net.sf.ehcache.hibernate',
		   'org.springframework.security',
		   'org.springframework.ldap'
	
	trace  'grails.app.controller',
	       'grails.app.service',
		   'grails.app.domain',
		   'ca.on.oicr.gps',
		   'ca.on.oicr.gps.pipeline',
		   'ca.on.oicr.gps.system'
}

hibernate.SQL="trace,stdout"
hibernate.type="trace,stdout"

// ============================================================================
// Used to derive the list of users in UserListService
gps.userListService.groupFilter = "(cn=gps-users)"
gps.userListService.groupMemberAttribute = "memberUid"
gps.userListService.userFilter = "(uid={0})"

// ============================================================================
// S E C U R I T Y

grails.plugins.springsecurity.useBasicAuth = true
grails.plugins.springsecurity.basic.realmName = "GPS"

// Require authentication for the web services. This is important for security. 
grails.plugins.springsecurity.filterChain.chainMap = [
   '/api/**':       'JOINED_FILTERS,-exceptionTranslationFilter',
   '/**':           'JOINED_FILTERS,-basicAuthenticationFilter,-basicExceptionTranslationFilter'
]

//Let's try for pessimistic lockdown
//grails.plugins.springsecurity.rejectIfNoRule = true

// Added by the Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'ca.on.oicr.gps.model.system.SecUser'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'ca.on.oicr.gps.model.system.SecUserSecRole'
grails.plugins.springsecurity.authority.className = 'ca.on.oicr.gps.model.system.SecRole'

// Added by the Spring Security LDAP plugin:
//grails.plugins.springsecurity.ldap.context.managerDn = 'uid=admin,ou=system'
//grails.plugins.springsecurity.ldap.context.managerPassword = 'secret'

// Add some hierarchy to the roles, making our lives easier...
grails.plugins.springsecurity.roleHierarchy = '''
ROLE_GPS-ADMINS > ROLE_GPS-CONTRIBUTORS
ROLE_GPS-OICR > ROLE_GPS-CONTRIBUTORS
ROLE_GPS-CONTRIBUTORS > ROLE_GPS-MANAGERS
ROLE_GPS-MANAGERS > ROLE_GPS-USERS
'''
