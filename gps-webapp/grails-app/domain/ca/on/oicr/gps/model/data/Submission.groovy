package ca.on.oicr.gps.model.data

import java.util.Set;

import ca.on.oicr.gps.model.laboratory.Panel;

class Submission {
	
	/**
	 * Submissions can contain a number of sequencing runs. For some types of 
	 * submission, there will be only one. 
	 * 
	 * Samples don't really belong to submissions, as they are tracked 
	 * independently. This is something that needs to be treated with a little
	 * caution.
	 */
	static hasMany = [ processes: Process ]
	
	String dataType
	String userName
	Date dateSubmitted
	String fileName
	byte[] fileContents
	
    static constraints = {
		dataType(nullable: false, maxSize: 16)
		userName(nullable: false, blank:false)
		dateSubmitted(nullable: false, blank:false)
		fileName(nullable: false)
		fileContents(nullable: false, maxSize:Integer.MAX_VALUE)
    }
	
	static mapping = {
		processes cascade: "all"
	}
	
	/*
	 * The transient definition here is required to mark these "properties"
	 * as not to be persisted. 
	 */
	
	static transients = [  "subjects", "patientCount", "patients", "panels", "primaryPanel" ]

	boolean hasCurrentProcesses() {
		return ! processes.isEmpty()
	}
	
	Set<Subject> getSubjects() {
		def subjects = [] as Set<Subject>
		for(Process process in processes) {
			for(RunSample runSample in process.runSamples) {
				subjects.add(runSample.sample.subject)
			}
		}
		return subjects
	}
	
	List<Subject> getPatients() {
		def subjects = Subject.createCriteria().list {
			projections {
				distinct("patientId")
			}
			samples {
				runSamples {
					process {
						submission {
							eq("id", this.id)
						}
					}
				}
			}
		}
		return subjects
	}

	Integer getPatientCount() {
		def subjectCount = Subject.createCriteria().get {
			projections {
				countDistinct("patientId")
			}
			samples {
				runSamples {
					process {
						submission {
							eq("id", this.id)
						}
					}
				}
			}
		}
		return subjectCount
	}
	
	List<Panel> getPanels() {
		def panels = Submission.createCriteria().list {
			projections {
				processes {
					distinct("panel")
				}
			} 
			eq("id", this.id)
		}
		return panels
	}
	
	String getPrimaryPanel() {
		List<Panel> panels = getPanels()
		return panels.get(0)?.technology ?: "unknown"
	}
}
