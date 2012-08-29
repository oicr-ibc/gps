package ca.on.oicr.gps.model.reporting

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.hibernate.jdbc.Work;

class Query {
	
	String name
	String body

    static constraints = {
		name(nullable: false, blank: false)
		body(nullable: false, blank: false, maxSize: 32767)
    }
	
	def executeQuery() {
		
		def result
		
		def sql = body.trim()
		if (! (sql =~ /^(?i)select\s+/)) {
			throw new RuntimeException("Invalid SELECT query: " + sql)
		}
		
		def worker = new Work() {
			public void execute(Connection connection) throws SQLException {
				Statement statement = connection.createStatement()
				result = statement.executeQuery(body)
			}
		}

		withNewSession { org.hibernate.Session session ->
			session.doWork(worker)
		}
		
		return result
	}
}
