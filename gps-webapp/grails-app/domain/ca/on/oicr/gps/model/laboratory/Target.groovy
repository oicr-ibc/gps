package ca.on.oicr.gps.model.laboratory

import ca.on.oicr.gps.pipeline.domain.DomainTarget

class Target implements DomainTarget {
	
	static belongsTo = [ panel: Panel ]

	/**
	 *  Defines the chromosome of the target - must be int (not Integer) for OpenCSV to work
	 */
	String chromosome
	
	/**
	 * Defines the gene tested by the target
	 */
	String gene
	
	/**
	 * Defines the start bound of the target - must be int (not Integer) for OpenCSV to work
	 */
	int start

	/**
	 * Defines the stop bound of the target - must be int (not Integer) for OpenCSV to work
	 */
	int stop

	/**
	 * Defines the reference allele tested by the target
	 */
	String refAllele
	
	/**
	 * Defines the variant allele tested by the target
	 */
	String varAllele
	
	/**
	* Defines the mutation tested by the target
	*/
    String mutation
	
    static constraints = {
		chromosome(nullable: false, blank: false)
		gene(nullable: true)
		start(nullable: true)
		stop(nullable: true)
		refAllele(nullable: true)
		varAllele(nullable: true)
		mutation(nullable: true)
    }
}
