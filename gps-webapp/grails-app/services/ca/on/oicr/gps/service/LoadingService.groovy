package ca.on.oicr.gps.service

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.jdbc.Work;

import ca.on.oicr.gps.model.knowledge.AgentEffectiveness;
import ca.on.oicr.gps.model.knowledge.AgentSensitivity;
import ca.on.oicr.gps.model.knowledge.ClinicalSignificance;
import ca.on.oicr.gps.model.knowledge.GeneCharacteristics;
import ca.on.oicr.gps.model.knowledge.KnownGene;
import ca.on.oicr.gps.model.knowledge.KnownMutation;
import ca.on.oicr.gps.model.knowledge.KnownMutationFrequency;
import ca.on.oicr.gps.model.knowledge.KnownTumourType;
import ca.on.oicr.gps.model.knowledge.MutationCharacteristics;
import ca.on.oicr.gps.model.knowledge.MutationConfirmation;
import ca.on.oicr.gps.model.laboratory.Panel;
import ca.on.oicr.gps.model.laboratory.Target;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
import au.com.bytecode.opencsv.bean.CsvToBean;
import au.com.bytecode.opencsv.bean.HeaderColumnNameMappingStrategy;
import au.com.bytecode.opencsv.CSVReader;

/**
 * A service that exposes methods and mechanisms that can be used to load initial data into
 * the system. For test purposes, this is used when bootstrapping and testing, but in production,
 * it can also be exposed to load initial data.
 * 
 * @author swatt
 */

class LoadingService {

    static transactional = true

	static final Logger log = Logger.getLogger(this)

	/**
	 * Service method to load a panel. given a panel name, and technology, and CSV-formatted
	 * data exposed through a Reader. 
	 * 
	 * @param name the panel name
	 * @param technology the panel technology
	 * @param dataReader the source of data
	 * @return nothing useful
	 */
    def loadPanelAndTargets(String name, String versionString, String technology, File theFile) {
		
		Reader dataReader = new FileReader(theFile)
		Panel panel = new Panel(name: name, versionString: versionString, technology: technology)
		loadPanelTargets("text/csv", theFile.getPath(), panel, dataReader)
		panel.save(validate: true, failOnError: true )
    }
	
	def loadPanelTargets(String contentType, String fileName, Panel panel, Reader dataReader) {
		log.debug("Loading targets for panel: " + panel.name)
		
		CSVReader reader = new CSVReader(dataReader, ',' as char, '\"' as char)
		
		HeaderColumnNameMappingStrategy<Target> strat = new HeaderColumnNameMappingStrategy<Target>();
		strat.setType(Target.class);
		
		CsvToBean csv = new CsvToBean();
		List<Target> targetList = csv.parse(strat, reader);
		
		for (Target target in targetList) {
			panel.addToTargets(target)
		}

		panel.save(validate: true, failOnError: true )
	}
	
	/**
	 * Service method to load a Hotspot panel. given a panel name, and technology, and .bed file 
	 * exposed through a Reader. 
	 * 
	 * @param name the panel name
	 * @param technology the panel technology
	 * @param dataReader the source of data
	 * @return nothing useful
	 */
	def loadHotSpotPanelTargets(String contentType, String fileName, Panel panel, Reader dataReader) {
		log.debug("Loading HotSpot targets for panel: " + panel.name)
		
		CSVReader reader = new CSVReader(dataReader, '\t' as char)
		
		ColumnPositionMappingStrategy strat = new ColumnPositionMappingStrategy();
		strat.setType(Target.class);
		String[] columns = ["chromosome", "start", "stop"]
		strat.setColumnMapping(columns);
		
		CsvToBean csv = new CsvToBean();
		List<Target> targetList = csv.parse(strat, reader);
		
		for (Target target in targetList) {
			if (target.chromosome.startsWith("chr")) {
				target.chromosome = target.chromosome.substring(3)
			}
			panel.addToTargets(target)
		}

		panel.save(validate: true, failOnError: true )
	}
	
	/**
	 * A new reader, that executes SQL scripts. Because this is all totally non-standard, this scripy
	 * handler assumes that 
	 * @param dataReader
	 * @return
	 */
	def loadSql(String fileName, Reader dataReader) {
		log.debug("Loading SQL file: " + fileName)
		
		def worker = new Work() {
			public void execute(Connection connection) throws SQLException { 
				BufferedReader input = new BufferedReader(dataReader)
				Statement statement = connection.createStatement()
				def line
				while ((line = input.readLine()) != null) {
					log.trace(line);
					statement.execute(line)
				}
				input.close();
			}
		}
		
		KnownMutation.withNewSession { org.hibernate.Session session ->			
			session.doWork(worker)
		}
	}
	
	/**
	 * Service method to load an initial known mutation set, from CSV-formatted data exposed 
	 * through a Reader. 
	 * 
	 * @param dataReader the source of data
	 * @return nothing useful
	 */
	def loadKnownMutations(Reader dataReader) {
		
		def logger = log
		
		def worker = new Work() {
			
			def log = logger
			
			def loadKnownMutationsUsingHibernate(Connection connection, Reader input) {

				// First of all, remove all the current known mutations
				for(KnownMutation mut : KnownMutation.getAll()) {
					mut.delete()
				}
					
				log.debug("Loading known mutations")
		
				CSVReader reader = new CSVReader(input, ',' as char, '\"' as char)
				
				HeaderColumnNameMappingStrategy<KnownMutation> strat = new HeaderColumnNameMappingStrategy<KnownMutation>();
				strat.setType(KnownMutation.class);
				
				CsvToBean csv = new CsvToBean();
				List<Target> mutList = csv.parse(strat, reader);
				
				Map knownGenes = [:]
				
				for (KnownMutation mut in mutList) {
					
					if (mut.knownGene == null) {
						if (knownGenes[mut.gene] == null) {
							knownGenes[mut.gene] = new KnownGene(name: mut.gene, visible: true)
							knownGenes[mut.gene].characteristics = new GeneCharacteristics()
							knownGenes[mut.gene].save(validate:true)
						}
						mut.knownGene = knownGenes[mut.gene]
					}
					
					if (mut.characteristics == null) {
						mut.characteristics = new MutationCharacteristics()
						mut.characteristics.action = MutationCharacteristics.ACTION_UNKNOWN 
					}
					
					mut.visible = true;
					
					if (! mut.publicId) {
						mut.publicId = KnownMutation.generatePublicId(mut)
					}
					if (! mut.guid) {
						mut.guid = KnownMutation.generateGuid(mut)
					}
					
					mut.save(validate:true)
					if (mut.hasErrors()) {
						mut.errors.each {
							log.error it
						}
						throw new Exception("Failed to validate: " + mut.toString())
					}
				}
			}
			
			public void execute(Connection connection) throws SQLException {
				BufferedReader input = new BufferedReader(dataReader)
				loadKnownMutationsUsingHibernate(connection, input)
			}
		}
		
		KnownMutation.withNewSession { org.hibernate.Session session ->
			session.doWork(worker)
			session.flush()
		}
	}
	
	
	
	def loadCosmicData(Reader dataReader) {
		
		def logger = log
		
		def worker = new Work() {
			
			def log = logger
			
			def loadCosmicDataUsingSql(Connection connection, Reader input) {
				XmlParser parser = new XmlParser()
				Node root = parser.parse(input)
		 
				log.info("Loading COSMIC frequency data")
				
				Map<String, Integer> tumourTypeTable = [:]
				Map<String, Integer> geneTable = [:]
				
				Statement stmt = connection.createStatement()
				ResultSet rs = stmt.executeQuery("SELECT ID, NAME FROM KNOWN_GENE");
				while (rs.next()) {
					geneTable.putAt(rs.getString("NAME"), rs.getInt("ID"))
				}
				
				stmt = connection.createStatement()
				rs = stmt.executeQuery("SELECT ID, NAME FROM KNOWN_TUMOUR_TYPE");
				while (rs.next()) {
					tumourTypeTable.putAt(rs.getString("NAME"), rs.getInt("ID"))
				}
				
				PreparedStatement insertGene = connection.prepareStatement("""INSERT INTO KNOWN_GENE ("NAME", "VERSION", "VISIBLE") VALUES (?, 0, 0)""", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement insertTumourType = connection.prepareStatement("""INSERT INTO KNOWN_TUMOUR_TYPE ("NAME", "VERSION") VALUES (?, 0)""", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement getMutations = connection.prepareStatement("""SELECT ID, MUTATION FROM KNOWN_MUTATION WHERE KNOWN_GENE_ID = ?""");
				PreparedStatement insertFrequency = connection.prepareStatement("""INSERT INTO KNOWN_MUTATION_FREQUENCY ("VERSION", "MUTATION_ID", "TUMOUR_TYPE_ID", "FREQUENCY", "SAMPLES", "RELEVANCE") VALUES (0, ?, ?, ?, ?, ?)""", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement insertGeneCharacteristics = connection.prepareStatement("""INSERT INTO GENE_CHARACTERISTICS ("GENE_ID", "VERSION") VALUES (?, 0)""", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement insertKnownMutation = connection.prepareStatement("""INSERT INTO KNOWN_MUTATION ("KNOWN_GENE_ID", "VERSION", "MUTATION", "CHROMOSOME", "GENE", "START", "STOP", "REF_ALLELE", "VAR_ALLELE", "GUID", "BUILD", "ORIGIN", "VISIBLE") VALUES (?, 0, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0)""", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement insertMutationCharacteristics = connection.prepareStatement("""INSERT INTO MUTATION_CHARACTERISTICS ("MUTATION_ID", "VERSION", "ACTION") VALUES (?, 0, 0)""", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement insertSignificance = connection.prepareStatement("""INSERT INTO CLINICAL_SIGNIFICANCE ("VERSION", "MUTATION_ID", "TUMOUR_TYPE_ID", "SIGNIFICANCE") VALUES (0, ?, ?, 0)""", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement getKnownMutation = connection.prepareStatement("""SELECT ID FROM KNOWN_MUTATION WHERE "CHROMOSOME" = ? AND "START" = ? AND "STOP" = ? AND "VAR_ALLELE" = ?""");
				
				for(Node node : root.tumour_type) {
					
					String tumourTypeName = node['@name']
					
					//log.info("Loading frequencies for: " + tumourTypeName)
					
					Integer tumourTypeId = tumourTypeTable.getAt(tumourTypeName)
					if (tumourTypeId == null) {
						insertTumourType.setString(1, tumourTypeName)
						insertTumourType.executeUpdate()
						ResultSet insertTumourResult = insertTumourType.getGeneratedKeys()
						insertTumourResult.first();
						tumourTypeId = insertTumourResult.getInt(1);
						tumourTypeTable.putAt(tumourTypeName, tumourTypeId)
					}
					
					assert tumourTypeId != null
					
					for(Node geneNode : node.gene) {
						String geneName = geneNode['@name']
				
						//log.info("Gene: " + geneName)
						
						Integer geneId = geneTable.getAt(geneName)
						if (geneId == null) {
							insertGene.setString(1, geneName)
							insertGene.executeUpdate()
							ResultSet insertGeneResult = insertGene.getGeneratedKeys()
							insertGeneResult.first();
							geneId = insertGeneResult.getInt(1)
							geneTable.putAt(geneName, geneId)
							
							//log.info("Added: id = " + geneId)
							
							insertGeneCharacteristics.setInt(1, geneId)
							insertGeneCharacteristics.executeUpdate()
						}
						
						assert geneId != null
						
						Map<String, List<Integer>> mutationTable = [:]

						getMutations.setInt(1, geneId)
						ResultSet mutationsRs = getMutations.executeQuery()
						
						while (mutationsRs.next()) {
							Integer mutId = mutationsRs.getInt("ID")
							String mutName = mutationsRs.getString("MUTATION")
							assert mutName != null
							//log.info("Existing mutation: name = " + mutName)
							if (! mutationTable.containsKey(mutName)) {
								mutationTable.putAt(mutName, [] as List<Integer>)
							}
							mutationTable.getAt(mutName).add(mutId)
						}
						
						for(Node mutationNode : geneNode.mutation) {
							String mutationName = mutationNode['@name']
							
							//log.info("Mutation: " + mutationName)
							List<Integer> muts = mutationTable.getAt(mutationName)
							if (muts == null) {
								muts = [] as List<Integer>
							}

							for(Node newKnownMutationNode : mutationNode.known) {
								
								// Don't load mutations when we already know about them
								if (muts.size() > 0) {
									//log.info("Already got mutations: " + geneName + " " + mutationName + " > " + geneId)
									break;
								}
								
								//log.info(geneName + " " + mutationName + " > " + geneId)
								insertKnownMutation.setInt(1, geneId)
								insertKnownMutation.setString(2, mutationName)
								insertKnownMutation.setString(3, newKnownMutationNode['@chromosome'])
								insertKnownMutation.setString(4, geneName)
								insertKnownMutation.setInt(5, newKnownMutationNode['@start'].toInteger())
								insertKnownMutation.setInt(6, newKnownMutationNode['@stop'].toInteger())
								insertKnownMutation.setString(7, newKnownMutationNode['@ref_allele'])
								insertKnownMutation.setString(8, newKnownMutationNode['@var_allele'])
								insertKnownMutation.setString(9, UUID.randomUUID().toString())
								insertKnownMutation.setString(10, 'hg19')
								
								try {
									insertKnownMutation.executeUpdate()
									ResultSet insertKnownMutationResult = insertKnownMutation.getGeneratedKeys()
									insertKnownMutationResult.first();
									Integer knownMutationId = insertKnownMutationResult.getInt(1)
									
									insertMutationCharacteristics.setInt(1, knownMutationId)
									insertMutationCharacteristics.executeUpdate()
									
									muts.add(knownMutationId)
									//log.info("Added")
								} catch (SQLException e) {
									
									// If it's not a constraint violation, rethrow
									if (! e.getSQLState().startsWith("23")) {
										throw e
									}
									// So, Mr Bond, we meet again
									getKnownMutation.setString(1, newKnownMutationNode['@chromosome'])
									getKnownMutation.setInt(2, newKnownMutationNode['@start'].toInteger())
									getKnownMutation.setInt(3, newKnownMutationNode['@stop'].toInteger())
									getKnownMutation.setString(4, newKnownMutationNode['@var_allele'])
									ResultSet mutationsIdRs = getKnownMutation.executeQuery()
									assert mutationsIdRs.first()
									Integer mutId = mutationsIdRs.getInt("ID")
									muts.add(mutId)
								}
							}
							
							for(Integer mutId : muts) {
								insertFrequency.setInt(1, mutId)
								insertFrequency.setInt(2, tumourTypeId)
								insertFrequency.setFloat(3, mutationNode.frequency.text().toFloat())
								insertFrequency.setInt(4, mutationNode.samples.text().toInteger())
								insertFrequency.setFloat(5, mutationNode.relevance.text().toFloat())
								insertFrequency.executeUpdate()
							}
						}
					}
				}
			}
			
			public void execute(Connection connection) throws SQLException {
				BufferedReader input = new BufferedReader(dataReader)
				loadCosmicDataUsingSql(connection, input)
			}
		}
		
		KnownMutation.withNewSession { org.hibernate.Session session ->
			session.doWork(worker)
			session.flush()
		}
	}

	
	/**
	* Service method to load an initial knowledge base from XML data exposed
	* through a Reader.
	*
	* @param dataReader the source of data
	* @return nothing useful
	*/
    def loadKnowledgeData(Reader dataReader) {
	   
	    def logger = log
	    def worker = new Work() {
		   
		    def log = logger
		    def loadKnowledgeDataUsingSql(Connection connection, Reader input) {
			    XmlParser parser = new XmlParser()
			    Node root = parser.parse(input)
			   
  			    log.info("Loading knowledge base")
				Map<String, Integer> tumourTypeTable = [:]
				Map<String, Integer> geneTable = [:]
				 
				Statement stmt = connection.createStatement()
				ResultSet rs = stmt.executeQuery("SELECT ID, NAME FROM KNOWN_GENE");
				while (rs.next()) {
				    geneTable.putAt(rs.getString("NAME"), rs.getInt("ID"))
				}
				 
				stmt = connection.createStatement()
				rs = stmt.executeQuery("SELECT ID, NAME FROM KNOWN_TUMOUR_TYPE");
				while (rs.next()) {
				    tumourTypeTable.putAt(rs.getString("NAME"), rs.getInt("ID"))
				}
				
				PreparedStatement insertGene = connection.prepareStatement("""INSERT INTO KNOWN_GENE ("NAME", "VERSION", "VISIBLE") VALUES (?, 0, 1)""", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement insertGeneCharacteristics = connection.prepareStatement("""INSERT INTO GENE_CHARACTERISTICS ("GENE_ID", "VERSION") VALUES (?, 0)""", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement updateGene = connection.prepareStatement("""UPDATE KNOWN_GENE SET CHROMOSOME = ?, START = ?, STOP = ?, GENE_SIZE = ? WHERE ID = ?""");
				PreparedStatement updateGeneCharacteristics = connection.prepareStatement("""UPDATE GENE_CHARACTERISTICS SET FULL_NAME = ?, SOMATIC_TUMOR_TYPES = ?, GERMLINE_TUMOR_TYPES = ?, CANCER_SYNDROME = ?, DESCRIPTION = ? WHERE GENE_ID = ?""");
				PreparedStatement getMutation = connection.prepareStatement("""SELECT ID FROM KNOWN_MUTATION WHERE GENE = ? AND CHROMOSOME = ? AND MUTATION = ? AND START = ? AND STOP = ? AND VAR_ALLELE = ?""");
				PreparedStatement insertKnownMutation = connection.prepareStatement("""INSERT INTO KNOWN_MUTATION ("KNOWN_GENE_ID", "VERSION", "MUTATION", "CHROMOSOME", "GENE", "START", "STOP", "REF_ALLELE", "VAR_ALLELE", "GUID", "BUILD", "ORIGIN", "VISIBLE") VALUES (?, 0, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 1)""", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement updateMutationCharacteristics = connection.prepareStatement("""UPDATE MUTATION_CHARACTERISTICS SET "ACTION" = ?, "AGENTS_AVAILABLE" = ?, "ACTION_REFERENCE" = ?, "ACTION_COMMENT" = ? WHERE MUTATION_ID = ?""");
				PreparedStatement insertAgentSensitivity = connection.prepareStatement("""INSERT INTO AGENT_SENSITIVITY ("VERSION", "MUTATION_ID", "AGENT_NAME", "SENSITIVITY_TYPE") VALUES (0, ?, ?, ?)""", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement insertAgentEffectiveness = connection.prepareStatement("""INSERT INTO AGENT_EFFECTIVENESS ("VERSION", "MUTATION_ID", "AGENTS", "AGENTS_EFFECTIVE") VALUES (0, ?, ?, ?)""", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement insertTumourType = connection.prepareStatement("""INSERT INTO KNOWN_TUMOUR_TYPE ("VERSION", "NAME") VALUES (0, ?)""", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement insertSignificance = connection.prepareStatement("""INSERT INTO CLINICAL_SIGNIFICANCE ("VERSION", "MUTATION_ID", "TUMOUR_TYPE_ID", "SIGNIFICANCE", "SIGNIFICANCE_COMMENT", "SIGNIFICANCE_EVIDENCE", "SIGNIFICANCE_REFERENCE") VALUES (0, ?, ?, ?, ?, ?, ?)""", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement insertConfirmation = connection.prepareStatement("""INSERT INTO MUTATION_CONFIRMATION ("VERSION", "MUTATION_ID", "BODY", "COMMENT", "DATE", "NAME", "PDF", "USER_NAME") VALUES (0, ?, ?, ?, ?, ?, ?, ?)""", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement updateMutation = connection.prepareStatement("""UPDATE KNOWN_MUTATION SET LAST_EDITED_BY = ?, LAST_UPDATED = ? WHERE ID = ?""");
				PreparedStatement insertTumourTypeCharacteristics = connection.prepareStatement("""INSERT INTO TUMOUR_TYPE_CHARACTERISTICS ("VERSION", "TUMOUR_TYPE_ID", "MALIGNANT") VALUES (0, ?, ?)""");

				for(Node node : root.tumours[0].tumour) {
					String tumourTypeName = node['@name']
					Node characteristics = node.characteristics[0]
					String malignant = characteristics?.maligant[0]?.text() ?: "true"
					
					Integer tumourTypeId = tumourTypeTable.getAt(tumourTypeName)
					if (tumourTypeId == null) {
						insertTumourType.setString(1, tumourTypeName)
						insertTumourType.executeUpdate()
						ResultSet insertTumourTypeResult = insertTumourType.getGeneratedKeys()
						insertTumourTypeResult.first();
						tumourTypeId = insertTumourTypeResult.getInt(1)
						tumourTypeTable.putAt(tumourTypeName, tumourTypeId)

						insertTumourTypeCharacteristics.setInt(1, tumourTypeId)
						insertTumourTypeCharacteristics.setBoolean(2, malignant.toBoolean())
						insertTumourTypeCharacteristics.executeUpdate()
					}
				}
				
			    for(Node node : root.genes[0].gene) {
				    String geneName = node['@name']
				    String chromosome = node.chromosome[0].text()
				    String start = node.start[0].text()
				    String stop = node.stop[0].text()
				    String geneSize = node.geneSize[0].text()
					
					Integer geneId = geneTable.getAt(geneName)
					
					if (geneId == null) {
						insertGene.setString(1, geneName)
						insertGene.executeUpdate()
						ResultSet insertGeneResult = insertGene.getGeneratedKeys()
						insertGeneResult.first();
						geneId = insertGeneResult.getInt(1)
						geneTable.putAt(geneName, geneId)

						insertGeneCharacteristics.setInt(1, geneId)
						insertGeneCharacteristics.executeUpdate()
					}
					
					updateGene.setString(1, chromosome)
					updateGene.setInt(2, start.toInteger())
					updateGene.setInt(3, stop.toInteger())
					updateGene.setInt(4, geneSize.toInteger())
					updateGene.setInt(5, geneId)
					updateGene.executeUpdate()
					
				    Node characteristics = node.characteristics[0]
					updateGeneCharacteristics.setString(1, characteristics.fullName[0].text())
					updateGeneCharacteristics.setString(2, characteristics.somaticTumorTypes[0].text())
					updateGeneCharacteristics.setString(3, characteristics.germlineTumorTypes[0].text())
					updateGeneCharacteristics.setString(4, characteristics.cancerSyndrome[0].text())
					updateGeneCharacteristics.setString(5, characteristics.description[0].text())
					updateGeneCharacteristics.setInt(6, geneId)
					updateGeneCharacteristics.executeUpdate()
			    }
			   
			    for(Node node : root.mutations[0].mutation) {
				    String geneName = node['@gene']
				    String chromosome = node['@chromosome']
				    String start = node['@start']
				    String stop = node['@stop']
				    String mutationName = node['@mutation']
				    String refAllele = node['@refAllele']
				    String varAllele = node['@varAllele']
					
					String lastEditedBy = node.lastEditedBy[0].text()
					String lastUpdated = node.lastUpdated[0].text()

					//log.info("Loading: " + geneName + " " + mutationName + " > " + lastUpdated)
					
					getMutation.setString(1, geneName)
					getMutation.setString(2, chromosome)
					getMutation.setString(3, mutationName)
					getMutation.setInt(4, start.toInteger())
					getMutation.setInt(5, stop.toInteger())
					getMutation.setString(6, varAllele)
					
					ResultSet mutationsRs = getMutation.executeQuery()
					Integer geneId = geneTable.getAt(geneName)
					Integer knownMutationId = null;
					assert geneId
					
					if (mutationsRs.first()) {
						knownMutationId = mutationsRs.getInt("ID")
					} else {
						//log.info(geneName + " " + mutationName + " > " + geneId)
						insertKnownMutation.setInt(1, geneId)
						insertKnownMutation.setString(2, mutationName)
						insertKnownMutation.setString(3, chromosome)
						insertKnownMutation.setString(4, geneName)
						insertKnownMutation.setInt(5, start.toInteger())
						insertKnownMutation.setInt(6, stop.toInteger())
						insertKnownMutation.setString(7, refAllele)
						insertKnownMutation.setString(8, varAllele)
						insertKnownMutation.setString(9, UUID.randomUUID().toString())
						insertKnownMutation.setString(10, 'hg19')
						
						insertKnownMutation.executeUpdate()
						ResultSet insertKnownMutationResult = insertKnownMutation.getGeneratedKeys()
						insertKnownMutationResult.first();
						knownMutationId = insertKnownMutationResult.getInt(1)
					}
						
					Node annotation = node.annotation[0]
				    Node characteristics = annotation.characteristics[0]
				    
					updateMutationCharacteristics.setInt(1, MutationCharacteristics.toAction(characteristics.action[0].text()))
					updateMutationCharacteristics.setInt(2, MutationCharacteristics.toAgentsAvailable(characteristics.agentsAvailable[0].text()))
					updateMutationCharacteristics.setString(3, characteristics.actionReference[0].text())
					updateMutationCharacteristics.setString(4, characteristics.actionComment[0].text())
					updateMutationCharacteristics.setInt(5, knownMutationId)
					updateMutationCharacteristics.executeUpdate()
					
					List<Node> sensitivities = annotation.sensitivity
					for(Node sensitivity : sensitivities) {
						insertAgentSensitivity.setInt(1, knownMutationId)
						insertAgentSensitivity.setString(2, sensitivity.agents[0].text())
						insertAgentSensitivity.setInt(3, AgentSensitivity.toSensitivity(sensitivity.sensitivityType[0].text()))
						insertAgentSensitivity.executeUpdate()
					}

					List<Node> effectivesses = annotation.effectiveness
					for(Node effectiveness : effectivesses) {
						insertAgentEffectiveness.setInt(1, knownMutationId)
						insertAgentEffectiveness.setString(2, effectiveness.agents[0].text())
						insertAgentEffectiveness.setInt(3, AgentEffectiveness.toAgentsEffective(effectiveness.effectiveness[0].text()))
						insertAgentEffectiveness.executeUpdate()
					}
					
					List<Node> significances = annotation.significance
					for(Node significance : significances) {
						
						String tumourTypeName = significance.tumourType[0].text()
						Integer tumourTypeId = tumourTypeTable.getAt(tumourTypeName)
						if (tumourTypeId == null) {
							insertTumourType.setString(1, tumourTypeName)
							insertTumourType.executeUpdate()
							ResultSet insertTumourResult = insertTumourType.getGeneratedKeys()
							insertTumourResult.first();
							tumourTypeId = insertTumourResult.getInt(1);
							tumourTypeTable.putAt(tumourTypeName, tumourTypeId)
						}
						
						insertSignificance.setInt(1, knownMutationId)
						insertSignificance.setInt(2, tumourTypeId)
						insertSignificance.setInt(3, ClinicalSignificance.toSignificance(significance.type[0].text()))
						insertSignificance.setString(4, significance.significanceComment[0].text())
						insertSignificance.setString(5, significance.significanceEvidence[0].text())
						insertSignificance.setString(6, significance.significanceReference[0].text())
						insertSignificance.executeUpdate()
					}
					
					List<Node> confirmations = annotation.confirmation
					for(Node confirmation : confirmations) {
						
						insertConfirmation.setInt(1, knownMutationId)
						insertConfirmation.setBytes(2, confirmation.body[0].text().decodeBase64())
						insertConfirmation.setString(3, confirmation.comment[0].text())
						insertConfirmation.setTimestamp(4, new Timestamp(Date.parse("yyyy-MM-dd HH:mm:ss.S", confirmation.date[0].text()).getTime()))
						insertConfirmation.setString(5, confirmation.name[0].text())
						insertConfirmation.setBytes(6, confirmation.pdf[0].text().decodeBase64())
						insertConfirmation.setString(7, confirmation.userName[0].text())
						insertConfirmation.executeUpdate()
					}
					
					if (lastUpdated) {
						updateMutation.setString(1, lastEditedBy)
						updateMutation.setTimestamp(2, new Timestamp(Date.parse("yyyy-MM-dd HH:mm:ss.S", lastUpdated).getTime()))
						updateMutation.setInt(3, knownMutationId)
						updateMutation.executeUpdate()
					}
			    }
	   		}
	   
			public void execute(Connection connection) throws SQLException {
				BufferedReader input = new BufferedReader(dataReader)
				loadKnowledgeDataUsingSql(connection, input)
			}
		}
		
		KnownMutation.withNewSession { org.hibernate.Session session ->
			session.doWork(worker)
			session.flush()
		}
    }
}
