dataSource {
    pooled = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
	flush.mode = "commit"
}
// environment specific settings
environments {
    development {
        dataSource {
            //dbCreate = "update" // one of 'create', 'create-drop','update'
			//url = "jdbc:mysql://localhost:3306/testgps?characterEncoding=UTF-8&sessionVariables=sql_Mode=ANSI"
			//username='gps'
			//password='GpS'
            dbCreate = 'create-drop'
			url = "jdbc:h2:mem:devDB"
			//loggingSql = true
        }
    }
    test {
        dataSource {
            //dbCreate = "update" // one of 'create', 'create-drop','update'
			//url = "jdbc:mysql://localhost:3306/testgps?characterEncoding=UTF-8&sessionVariables=sql_Mode=ANSI"
			//username='gps'
			//password='GpS'
            dbCreate = 'create-drop'
			url = "jdbc:h2:mem:testDB"
			//loggingSql = true
        }
    }
    staging {
        dataSource {
            dbCreate = "update"
            url = "jdbc:mysql://localhost:3306/heliotrope?characterEncoding=UTF-8&sessionVariables=sql_Mode=ANSI"
			username='gps'
			password='GpS'
			//loggingSql = true
        }
    }
    production {
        dataSource {
            dbCreate = "update"
            url = "jdbc:mysql://localhost:3306/gps?characterEncoding=UTF-8&sessionVariables=sql_Mode=ANSI"
			username='gps'
			password='GpS'
        }
    }
}
