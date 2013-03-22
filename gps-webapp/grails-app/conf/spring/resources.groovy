import org.apache.commons.dbcp.BasicDataSource
import org.codehaus.groovy.grails.orm.hibernate.HibernateEventListeners;

import ca.on.oicr.gps.pipeline.PipelineRegistry;
import ca.on.oicr.gps.service.PipelineService;
import ca.on.oicr.gps.system.AuditEventListener;

beans = {
	auditListener(AuditEventListener)

	hibernateEventListeners(HibernateEventListeners) {
		listenerMap = ['post-insert':auditListener,
					   'post-update':auditListener,
					   'post-delete':auditListener]
	}
	
	/**
	 * Parser pipeline for Sanger files
	 */
	sangerPipeline(ca.on.oicr.gps.pipeline.sanger.v1.SangerPipeline)
	
	/**
	 * Parser pipeline for Sequenom files
	 */
	sequenomPipeline(ca.on.oicr.gps.pipeline.sequenom.v1.SequenomPipeline)

	/**
	 * Parser pipeline for HotSpot v1 files
	 */
	hotSpotPipeline(ca.on.oicr.gps.pipeline.hotspot.v1.HotSpotPipeline)

	/**
	 * Parser pipeline for PacBio v2 files
	 */
	pacBioPipeline(ca.on.oicr.gps.pipeline.pacbio.v2.PacBioPipeline)

	/**
	 * Inject the pipeline list into the service
	 */
	// Shouldn't have to specify class according to the user guide, but in practice
	// you have to as Grails breaks otherwise
	pipelineRegistry(PipelineRegistry) { bean ->
		bean.autowire = 'byName'
		bean.scope = 'singleton'
		
		pipelines = [
			ref(sangerPipeline),
			ref(sequenomPipeline),
			ref(hotSpotPipeline),
			ref(pacBioPipeline)
		]
	}
}