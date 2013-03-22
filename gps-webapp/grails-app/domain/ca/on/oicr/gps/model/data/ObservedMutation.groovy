package ca.on.oicr.gps.model.data

import ca.on.oicr.gps.model.laboratory.Target;
import ca.on.oicr.gps.model.laboratory.Panel;
import ca.on.oicr.gps.model.knowledge.KnownMutation;
import ca.on.oicr.gps.model.reporting.Report;
import ca.on.oicr.gps.pipeline.domain.DomainObservedMutation;

/**
 * Corresponds to an observed mutation, and embeds information about the observation, such
 * as quality and confidence, as well as a reference to the mutation observed.
 * 
 * @author swatt
 */

class ObservedMutation implements DomainObservedMutation, Comparable<ObservedMutation> {
	
	static belongsTo = [ RunSample, Report ]
	static hasMany = [reports: Report]
	
	public static final int MUTATION_STATUS_FOUND = 1
	public static final int MUTATION_STATUS_UNKNOWN = 2
	
	public static String MUTATION_CONFIDENCE_HIGH = "HIGH"
	
	/**
	 * The RunSample associated with this observation
	 */
	RunSample runSample
	
	/**
	 * The known mutation associated with this observation
	 */
	KnownMutation knownMutation
		
	Float frequency
	int status
	String confidence
	
	Date reported

    static constraints = {
		frequency(nullable: true, between:[0.0, 1.0])
		status(nullable: false, between:[MUTATION_STATUS_FOUND, MUTATION_STATUS_UNKNOWN])
		confidence(nullable: true, inList:[MUTATION_CONFIDENCE_HIGH])
		knownMutation(nullable: false)
		reported(nullable: true)
    }
	
	static transients = [ "publicId", "panel" ]
	
	/**
	 * The public identifier is currently retrieved from the associated known mutation. This 
	 * might change, if the public identifier should be associated with the observed 
	 * mutation instead
	 * @return a string public identifier
	 */
	String getPublicId() {
		return mutation?.publicId
	}
	
	static def getMutationSummary(params) {
		String query = "\
select new map( \
       (km.gene || ' ' || km.mutation) as mutation, \
       sumry.primaryTumorSite as primaryTumorSite, \
       (select count(p1.id) \
         from Target t1 \
         inner join t1.panel p1 \
         where p1.technology = 'Sequenom' \
         and p1.name = 'OncoCarta' \
         and t1.start = km.start and t1.stop = km.stop and t1.chromosome = km.chromosome and t1.varAllele = km.varAllele \
		 group by ob.id \
         ) as technology, \
       count(distinct sub.id) as subjectCount, \
       min(p.date) as date \
       ) \
from ObservedMutation ob \
inner join ob.knownMutation km \
inner join ob.runSample rs \
inner join rs.process p \
inner join rs.sample sample \
inner join sample.subject sub \
inner join sub.summary sumry \
group by (km.gene || ' ' || km.mutation), sumry.primaryTumorSite \
"
		
		String order = params.order ?: "asc"
		if (params.sort == 'mutation') {
			query += "order by (km.gene || ' ' || km.mutation) ${order}";
		} else if (params.sort == 'primaryTumorSite') {
			query += "order by sumry.primaryTumorSite ${order}";
		} else if (params.sort == 'date') {
			query += "order by p.date ${order}";
		}

		return ObservedMutation.executeQuery(query);
	}
	
	String toString() {
		return "<observedMutation " + knownMutation.gene + " " + knownMutation.mutation + ">";
	}
	
	String toDescription() {
		return knownMutation.toLabel() + " (" + String.format('%.2f', frequency) + ")"
	}
	
	static List<ObservedMutation> getObservedMutations(params) {
		def c = ObservedMutation.createCriteria()
		def results = c.list {
			if (params.max) {
				maxResults(params.max)
			}
			if (params.offset) {
				firstResult(Integer.parseInt(params.offset))
			}
			runSample {
				process {
					order("date", "desc")
				}
			}
			knownMutation {
				order("gene", 'asc')
				order("mutation", 'asc')
			}
		}
		return results
	}
	
	static Integer getObservationCount(KnownMutation mut) {
		def c = ObservedMutation.createCriteria()
		def numberOfObservations = c.count {
			knownMutation {
				eq("id",  mut.id)
			}
		}
		return numberOfObservations
	}
	
	static Integer getPatientCount(KnownMutation mut) {
		def c = ObservedMutation.createCriteria()
		def numberOfPatients = c.get {
			knownMutation {
				eq("id",  mut.id)
			}
			projections {
				runSample {
					sample {
						subject {
							countDistinct("id")
						}
					}
				}
			}
		}
		
		return numberOfPatients
	}

	static Integer getSampleCount(KnownMutation mut) {
		def c = ObservedMutation.createCriteria()
		def numberOfSamples = c.get {
			knownMutation {
				eq("id",  mut.id)
			}
			projections {
				runSample {
					sample {
						countDistinct("id")
					}
				}
			}
		}
		
		return numberOfSamples
	}
	
	static List<ObservedMutation> getObservations(KnownMutation mut) {
		def c = ObservedMutation.createCriteria()
		
		return c.list {
			knownMutation {
				eq("id",  mut.id)
			}
			runSample {
				sample {
					subject {
						order("patientId", "asc")
					}
					order("barcode", "asc")
				}
			}
		}
	}
	
	public Panel getPanel() {
		return runSample.process.panel
	}

	/**
	 * A relatively strict comparison function. 
	 */
	@Override
	public int compareTo(ObservedMutation arg0) {
		if (arg0 instanceof ObservedMutation) {
			return equals(arg0) ? 0 : 1
		} else {
			throw new ClassCastException("Can't compare to ObservedMutation");
		}
	}
}
