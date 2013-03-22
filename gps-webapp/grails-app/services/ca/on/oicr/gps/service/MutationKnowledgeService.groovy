package ca.on.oicr.gps.service

import ca.on.oicr.gps.model.knowledge.KnownGene;
import ca.on.oicr.gps.model.knowledge.KnownMutation;
import ca.on.oicr.gps.model.knowledge.MutationCharacteristics;

/**
 * Initial implementation of the active part of the GPS system knowledge base. The most important part 
 * of this is finding an mutation from the data that we get in an observation. Often this is straightforward
 * but it could be a little more sophisticated in some cases. 
 * 
 * @author swatt
 */

class MutationKnowledgeService {

    static transactional = true
	
	/**
	 * findBy encapsulates the logic needed to find a mutation by a map containing
	 * a set of criteria. Why? Well, we can't be certain that locating a mutation
	 * is easy enough to do without any domain knowledge, so this way, any required
	 * domain knowledge can be encapsulated here rather than naively using the
	 * domain class queries. 
	 * 
	 * @param criteria a Map of keys and values
	 * @return the frst matching record that is found
	 */
	
	/*
	 * Current implementation uses the criteria directly. (I tried the query by example
	 * but it failed to find anything. This appears to be a bit better, and much more
	 * amenable to domain logic.)
	 */
	public KnownMutation findBy(Map<String, Object> criteria) {
		log.trace("Looking for known mutation: " + criteria.get("gene") + " " + criteria.get("mutation"));
		
		def search = KnownMutation.createCriteria()
		KnownMutation result
		
		try {
			result = search.get(searchCriteriaClosure(criteria))
		} catch (Exception e) {
			e.printStackTrace()
			throw e
		}
		
		return result
	}
	
	/*
	 * Current implementation uses the criteria directly. (I tried the query by example
	 * but it failed to find anything. This appears to be a bit better, and much more
	 * amenable to domain logic.)
	 */
	public List<KnownMutation> findAllBy(Map<String, Object> criteria) {
		log.trace("Looking for known mutation: " + criteria);
		
		def search = KnownMutation.createCriteria()
		
		return search.list(searchCriteriaClosure(criteria))
	}
	
	private searchCriteriaClosure(Map<String, Object> criteria) {
		return { ->
			
			// This could be a list, but, meh - and this way we emphasise the fields we are using
			//if (criteria.containsKey("gene")) {
			//	eq('gene', criteria.getAt("gene"))
			//}
			//if (criteria.containsKey("mutation")) {
			//	eq('mutation', criteria.getAt("mutation"))
			//}
			if (criteria.containsKey("start")) {
				eq('start', criteria.getAt("start"))
			}
			if (criteria.containsKey("stop")) {
				eq('stop', criteria.getAt("stop"))
			}
			if (criteria.containsKey("chromosome")) {
				eq('chromosome', criteria.getAt("chromosome"))
			}
			if (criteria.containsKey("refAllele")) {
				eq('refAllele', criteria.getAt("refAllele"))
			}
			if (criteria.containsKey("varAllele")) {
				eq('varAllele', criteria.getAt("varAllele"))
			}
		}
	}
	
	/**
	 * Creates a new KnownMutation, with as many properties as possible. These come in
	 * the same form as a set of criteria, but we require at least the chromosome, start,
	 * end, gene, reference allele, allele, and mutation. 
	 * 
	 * KnownMutations are created through sequencing rather than genotyping technologies,
	 * and are flagged according to their origin. 
	 */
	
	public KnownMutation newKnownMutation(Map<String, Object> criteria) {
		log.info("Creating new known mutation: " + criteria.get("gene") + " " + criteria.get("mutation"));
		
		def gene = KnownGene.findByName(criteria.get("gene"))
		if (! gene) {
			gene = new KnownGene(criteria.get("gene"))
			gene.save(failOnError: true)
		}
		
		def chars = new MutationCharacteristics()
		chars.setAction(MutationCharacteristics.ACTION_UNKNOWN)
		chars.setActionReference("")
		chars.setActionComment("")
		
		def mut = new KnownMutation(criteria)
		mut.setOrigin(KnownMutation.ORIGIN_OBSERVED);
		mut.setKnownGene(gene)
		mut.setCharacteristics(chars)
		
		chars.setMutation(mut)
		mut.save(failOnError: true)
		chars.save()
		
		mut.publicId = KnownMutation.generatePublicId(mut)
		mut.guid = KnownMutation.generateGuid(mut)
		mut.save(failOnError: true)
		
		return mut
	}
}
