package ca.on.oicr.gps.system

import groovy.lang.MetaProperty;

import org.apache.log4j.Logger;
import org.hibernate.event.AbstractEvent;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.springframework.security.core.context.SecurityContextHolder;

import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.model.data.Summary;
import ca.on.oicr.gps.model.system.AuditRecord;

class AuditEventListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {
	
	static final Logger log = Logger.getLogger(this)
	
	def currentUserService
	
	void onPostUpdate(PostUpdateEvent event) {
		if (! event.entity.hasProperty('audit')) {
			return
		} else {
			auditChange(event)
		}
	}

	void onPostInsert(PostInsertEvent event) {
		if (! event.entity.hasProperty('audit')) {
			return
		} else {
			auditChange(event)
		}
	}

	void onPostDelete(PostDeleteEvent event) {
		if (! event.entity.hasProperty('audit')) {
			return
		} else {
			auditChange(event)
		}
	}
	
	private void auditChange(event) {
		def entity = event.entity
		
		def userName = currentUserService?.currentUserName() ?: "unknown"
		
		List persistedNames = event.persister.propertyNames
		for(String propertyName in entity.audit) {
			Integer index = persistedNames.findIndexOf { it.equals(propertyName) }
			auditProperty(event, entity, userName, propertyName, index)
		}
	}
	
	private void auditProperty(PostDeleteEvent event, Object entity, String userName, String name, Integer index) {
		Object value = event.deletedState[index]
		AuditRecord rec = new AuditRecord(type: AuditRecord.TYPE_DELETE, 
										  patientId: getPatientId(entity), 
										  propertyName: name,
									      oldValue: getValue(value), 
			                              userName: userName, timestamp: new Date())
		rec.save(validate:true, failOnError: true)
	}

	private void auditProperty(PostInsertEvent event, Object entity, String userName, String name, Integer index) {
		Object value = event.state[index]
		AuditRecord rec = new AuditRecord(type: AuditRecord.TYPE_INSERT, 
										  patientId: getPatientId(entity), 
									      propertyName: name,
									      newValue: getValue(value), 
			                              userName: userName, timestamp: new Date())
		rec.save(validate:true, failOnError: true)
	}

	private void auditProperty(PostUpdateEvent event, Object entity, String userName, String name, Integer index) {
		Object oldValue = event.oldState[index]
		Object newValue = event.state[index]
		if (oldValue == null && newValue == null) {
			return;
		} else if (oldValue == null || newValue == null || ! newValue.equals(oldValue)) {
			AuditRecord rec = new AuditRecord(type: AuditRecord.TYPE_UPDATE, 
										  	  patientId: getPatientId(entity), 
											  propertyName: name,
											  oldValue: getValue(oldValue), newValue: getValue(newValue),
											  userName: userName, timestamp: new Date())
			rec.save(validate:true, failOnError: true)
		}
	}
	
	private String getValue(value) {
		if (value == null || value instanceof String) {
			return value
		} else {
			return value.toString()
		}
	}
	
	private String getPatientId(Subject entity) {
		return entity.patientId
	}
	
	private String getPatientId(Summary entity) {
		return getPatientId(entity.subject)
	}
	
	private String getPatientId(Object entity) {
		throw new Exception("Can't get record identifier for " + entity)
	}
}
