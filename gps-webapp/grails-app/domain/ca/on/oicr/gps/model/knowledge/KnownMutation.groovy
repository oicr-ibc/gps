package ca.on.oicr.gps.model.knowledge

import java.util.List
import java.util.UUID

import ca.on.oicr.gps.model.reporting.Report
import ca.on.oicr.gps.pipeline.domain.DomainKnownMutation

class KnownMutation implements DomainKnownMutation, Comparable<KnownMutation> {
	
	static belongsTo = [ 
		knownGene: KnownGene
	]
	
	static hasMany = [ 
		confirmations: MutationConfirmation,
		sensitivity: AgentSensitivity,
		effectiveness: AgentEffectiveness,
		significance: ClinicalSignificance, 
		frequencies: KnownMutationFrequency
	]
	
	static hasOne =  [ 
		characteristics: MutationCharacteristics
	]
	
	SortedSet<MutationConfirmation> confirmations
	
	/**
	 * When a mutation has been predefined through an external database, the origin
	 * field will contain this value. 
	 */
	static final int ORIGIN_PREDEFINED = 0
	
	/**
	 * When a mutation has been observed in a sample, and has not been predefined
	 * through an external database, the origin field will contain this value. 
	 */
	static final int ORIGIN_OBSERVED = 1

	String build = "hg19"
	
	/**
	 * The publicly visible key
	 */
	String publicId
	String guid
	
	String gene
	String refAllele
	String varAllele
	String chromosome
	int start
	int stop
	String mutation
	String ncbiReference
	int origin = ORIGIN_PREDEFINED
	Boolean visible = false
	
	String lastEditedBy
	Date lastUpdated

	static mapping = {
		effectiveness sort:'agents', cascade:"all-delete-orphan"
		sensitivity sort:'agentName', cascade:"all-delete-orphan"
		confirmations sort:'date', order:'desc'
		frequencies sort:'frequency', order:'desc'
		knownGene index: 'mutation_idx'
		mutation index: 'mutation_idx'
		visible index: 'mutation_idx'
		autoTimestamp false
	}
	
    static constraints = {
		build(nullable: false, blank:false, inList: ['hg19'])
		publicId(nullable: true, blank:false, maxSize: 10)
		guid(nullable: true, maxSize: 40)
		gene(nullable: false, blank:false, maxSize: 32)
		refAllele(nullable: false, maxSize: 64, blank:false, matches: "[ACGTN*]+|[-]")
		varAllele(nullable: false, maxSize: 64, blank:false, matches: "[ACGTN*]+|[-]", unique: ['build', 'chromosome', 'start', 'stop'])
		chromosome(nullable: false, blank:false)
		start(nullable: false)
		stop(nullable: false)
		mutation(nullable: true, blank:false, maxSize: 45)
		ncbiReference(nullable: true, blank:false, maxSize: 20)
		origin(nullable: false)
		lastEditedBy(nullable:true)
		lastUpdated(nullable:true)
    }
	
	static transients = [
		'tumourTypes', 'statusCode', 'orderedSignificances', 'significanceFrequency'
	]
	
	public int compareTo(KnownMutation o) {
		Integer compareGene = gene.compareTo(o.gene)
		return (compareGene != 0) ? compareGene : mutation.compareTo(o.mutation)
	}
	
	public getStatusCode() {
		return "complete";
	}
	
	public Boolean isModified() {
		return this.isDirty() ||
			sensitivity.find { it.isDirty() } ||
			effectiveness.find { it.isDirty() } ||
			significance.find { it.isDirty() } ||
			confirmations.find { it.isDirty() } ||
			frequencies.find { it.isDirty() }
	}
	
	public Boolean isConfirmed() {
		return (confirmations.size() > 0 && lastUpdated != null && confirmations.first().date >= lastUpdated)
	}
	
	public String toString() {
		return "<mutation " + gene + " " + mutation + ">";
	}

	public String toLabel() { 
		return gene + " " + mutation;
	}

	public static String generatePublicId (KnownMutation mut) {
		return sprintf('M%08d', mut.id)
	}

	public static String generateGuid (KnownMutation mut) {
		return UUID.randomUUID().toString()
	}
	
	public List<ClinicalSignificance> findSignificances(String thisTumourType) {
		def criteria = ClinicalSignificance.createCriteria()
		def foundSignificance = criteria.list {
			mutation {
				eq("id", this.id)
			}
			tumourType {
				eq("name", thisTumourType)
			}
		}
		return foundSignificance
	}
	
	public Boolean isComplete() {
		// Assume, initially, that this mutation is completely described. 
		Boolean complete = true
		
		if (characteristics?.action == null) {
			complete = false
		} else if (characteristics.action != MutationCharacteristics.ACTION_UNKNOWN
				   && characteristics.actionReference == null) {
			complete = false
		}
		if (! complete) return false
				   
		if (characteristics?.agentsAvailable == MutationCharacteristics.AVAILABLE_YES
			&& effectiveness.size() < 1) {
			complete = false
		}
		if (! complete) return false
		
		significance.each {  
			if (it.significance != ClinicalSignificance.SIGNIFICANCE_UNKNOWN &&
				(! it.significanceReference ||
				 ! it.significanceEvidence || 
				 ! it.significanceComment)) {
				complete = false
			}
		}
		
		return complete
	}
	
	public List getTumourTypes() {
		return frequencies.sort { a,b -> 
			a.frequency == null ? -1 : b.frequency == null ? 1 : b.frequency.compareTo(a.frequency)
		}.collect { it.tumourType }
	}
	
	static KnownMutation findMutationByLabel(String label) {
		def matcher = label =~ /(\w+)\s+(.*)/
		if (matcher.matches()) {
			
			def genePart = matcher.group(1)
			def mutationPart = matcher.group(2)
			
			def criteria = KnownMutation.createCriteria()
			def mutation = criteria.get {
				eq("mutation", mutationPart.trim())
				knownGene {
					eq("name", genePart.trim())
				}
			}
			
			return mutation
		} else {
			return null
		}
	}
	
	/**
	 * Returns a list of known mutations identified by a query. There is a risk that
	 * this generates too large a list, so arbitrarily a maximum of 1000 mutations will
	 * be returned. If the term contains a mutation part, the gene is assumed to be
	 * complete and exact. Otherwise, the gene part is a partial match too. Really, only
	 * visible mutations should be returned. 
	 * 
	 * @param term the query term, in the form "gene mutation" where the mutation bit is optional
	 * @return a list of known mutations
	 */
	static List<KnownMutation> findMutations(String term) {
		def matcher = term =~ /(\w+)?(?:\s+)?(.+)?/
		
		if (! matcher.matches()) {
			return []
		} else {
			def genePart = matcher.group(1)?.trim()
			def mutationPart = matcher.group(2)?.trim()
			
			def criteria = KnownMutation.createCriteria()

			// Really, sorting ought to be part of the criteria, but Hibernate's criteria API doesn't 
			// seem to allow sorting by a property of an associated object. This seems really bad, 
			// the more time I spend with Hibernate, the more it feels a bit crap, really. 
			def mutationList = criteria.list {
				eq("visible", true)
				if (genePart) {
					knownGene {
						if (mutationPart) {
							ilike("name", genePart + "%")
						} else {
							eq("name", genePart, [ignoreCase: true])
						}
 					}
 				}
				if (mutationPart) {
					ilike("mutation", mutationPart + "%")
				}
				maxResults(1000)
			}
			
			//*****************************************
			// Handle GPS-87 here by if genePart & mutationPart & ! mutationList, do
			// a single find and locate the one record we need for display. 
			
			if (! mutationList && genePart && mutationPart) {
				mutationList = KnownMutation.createCriteria().get {
					knownGene {
						eq("name", genePart, [ignoreCase: true])
					}
					eq("mutation", mutationPart, [ignoreCase: true])
				}
				mutationList = (mutationList) ? [mutationList] : [] 
			}
			
			return mutationList.sort { a,b ->
				int geneCmp = a.knownGene.name.compareTo(b.knownGene.name)
				return geneCmp != 0 ? geneCmp : a.mutation.compareTo(b.mutation)
			}
		}
	}
	
	/**
	 * Returns a complete list of clinical significances, ordered by frequency of the accompanying
	 * tumour type - which is a little hard to do trivially.
	 * @return ordered list of clinical significances
	 */
	List<ClinicalSignificance> getOrderedSignificances() {
		List<ClinicalSignificance> sigs = significance.asList()
		Map<String, Float> freqs = frequencies.collectEntries { [ it.tumourType.name.toLowerCase(), it.frequency] }
		return significance.asList().sort { a,b -> 
			String aTumourName = a.tumourType.name.toLowerCase()
			String bTumourName = b.tumourType.name.toLowerCase()
			Float aFreq = freqs[aTumourName]
			Float bFreq = freqs[bTumourName]
			(aFreq == null) ? 1 : (bFreq == null) ? -1 : bFreq.compareTo(aFreq)
		}
	}
	
	/**
	 * For a mutation, returns the corresponding frequency (if there is one).
	 * @param sig the clinical significance 
	 * @return either null (no frequency) or the appropriate mutation frequency object
	 */
	KnownMutationFrequency getSignificanceFrequency(ClinicalSignificance sig) {
		String tumourTypeName = sig.tumourType.name
		return frequencies.find {
			it.tumourType.name.equalsIgnoreCase(tumourTypeName)
		}
	}
}
