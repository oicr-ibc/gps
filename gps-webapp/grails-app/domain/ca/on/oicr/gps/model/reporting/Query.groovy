package ca.on.oicr.gps.model.reporting

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
		
		QueryResult result
		
		def sql = body.trim()
		if (! (sql =~ /^(?i)select\s+/)) {
			throw new RuntimeException("Invalid SELECT query: " + sql)
		}
		
		Work worker = new Work() {
			public void execute(Connection connection) throws SQLException {
				Statement statement = connection.createStatement()
				ResultSet rs = statement.executeQuery(body)
				result = new QueryResult()
				
				ResultSetMetaData rsmd = rs.getMetaData()
				result.columnCount = rsmd.getColumnCount()
				for (Integer i in 1 .. result.columnCount) {
					result.columnLabels.add(rsmd.getColumnLabel(i))
				}
				
				while(rs.next()) {
					List<Object> values = new ArrayList<Object>()
					for (Integer i in 1 .. result.columnCount) {
						values.add(rs.getObject(i))
					}
					result.values.add(values)
				}
				
				result.lastRow = result.values.size() - 1
			}
		}

		withNewSession { org.hibernate.Session session ->
			session.doWork(worker)
		}
		
		return result
	}
}

class QueryResult {
	
	Integer columnCount
	List<String> columnLabels = new ArrayList<String>()
	List<List<Object>> values = new ArrayList<List<Object>>()
	Integer currentRow = -1
	Integer lastRow = -1
	
	Integer getColumnCount() {
		return columnCount
	}
	
	String getColumnLabel(Integer i) {
		return columnLabels.get(i - 1) 
	}
	
	// Cases CR = -1, LR = -1 -> false, new CR = -1
	// Cases CR = -1, LR = 0 -> true, new CR = 0
	// Cases CR = 0, LR = 0 -> false, new CR = 0
	// Cases CR = 0, LR = 1 -> true, new CR = 1
	// Cases CR = 1, LR = 1 -> false, new CR = 1
	Boolean next() {
		if (currentRow < lastRow) {
			currentRow++
			return true
		} else {
			return false
		}
	}
	
	void beforeFirst() {
		currentRow = -1;
	}
	
	def getObject(Integer i) {
		return values.get(currentRow).get(i - 1)
	}
	
	/**
	 * Passed a column, this method allows it to be accumulated. This means
	 * the value in a row is the sum of all previous values in that row. The
	 * result set is modified in this calculation. It is probably possible to
	 * do this in SQL, but that shouldn't be required, and it would depend
	 * a lot on dialect. 
	 */
	void accumulateByColumn(Integer i) {
		Integer previousValue = 0;
		for(List<Object> row : values) {
			Integer value = row.getAt(i - 1) as Integer
			previousValue = value + previousValue
			row.putAt(i - 1, previousValue)
		}
	}
}