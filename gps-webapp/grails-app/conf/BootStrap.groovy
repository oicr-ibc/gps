import org.apache.log4j.Logger;

import grails.util.Environment
import org.apache.log4j.Logger
import au.com.bytecode.opencsv.CSVReader
import ca.on.oicr.gps.model.data.Sample
import ca.on.oicr.gps.model.data.Subject
import ca.on.oicr.gps.model.data.Submission
import ca.on.oicr.gps.model.data.Summary
import ca.on.oicr.gps.model.system.AppConfig
import ca.on.oicr.gps.model.system.AuditRecord
import ca.on.oicr.gps.model.system.SecRole
import ca.on.oicr.gps.model.system.SecUser
import ca.on.oicr.gps.model.system.SecUserSecRole
import ca.on.oicr.gps.system.DataSourceUtils

class BootStrap {

    static final Logger log = Logger.getLogger(this)

	def springSecurityService
	def loadingService
	
	def init = { servletContext ->
		DataSourceUtils.tune(servletContext)
		
		switch (Environment.current.getName()) {
			
			case ['development']:
				seedTestUsers()
				seedTestConfig()
				seedTestData()
				seedTestMutationsAndAssays()
				seedTestKnowledgeBase()
				break;
			
			case ['test']:
				seedTestUsers()
				seedTestConfig()
				seedTestData()
				seedTestMutationsAndAssays()
				seedTestKnowledgeBase()
				break;
			
			case ['production']:
				break;
				
			case ['staging']:
				break;
		}
	}
	
	def destroy = {
	}
	
	def dataBaseDirectory() {
		if (new File("gps-webapp").exists()) {
			return "gps-webapp/data/"
		} else {
			return "data/"
		}
	}
	
	/*
	 * In test mode, we use the loading service to set up the panels and assays that we
	 * use for integration testing. This will not be used in production, although the 
	 * service used might well be applied to achieve a similar effect.
	 */
	def seedTestMutationsAndAssays() {
		
		def base = dataBaseDirectory()
		
		def oncoCartaPanelFile = new File(base + 'panels/oncocarta_v1.0/panel_targets.csv')
		assert oncoCartaPanelFile.exists()
		loadingService.loadPanelAndTargets("OncoCarta", "1.0.0", "Sequenom", oncoCartaPanelFile)
		
		def oncoCartaPacBioPanelFilev1_0 = new File(base + 'panels/oncocarta_pacbio_v1.0/panel_targets.csv')
		assert oncoCartaPacBioPanelFilev1_0.exists()
		loadingService.loadPanelAndTargets("OncoCartaPacBio", "1.0.0", "PacBio", oncoCartaPacBioPanelFilev1_0)
		
		def oncoCartaPacBioPanelFilev1_2 = new File(base + 'panels/oncocarta_pacbio_v1.2/panel_targets.csv')
		assert oncoCartaPacBioPanelFilev1_2.exists()
		loadingService.loadPanelAndTargets("OncoCartaPacBio", "1.2.0", "PacBio", oncoCartaPacBioPanelFilev1_2)
		
		def oncoCartaSangerPanelFile = new File(base + 'panels/oncocarta_sanger_v1.0/panel_targets.csv')
		assert oncoCartaSangerPanelFile.exists()
		loadingService.loadPanelAndTargets("Sanger", "1.0.0", "ABI", oncoCartaSangerPanelFile)

		def solidPanelFile = new File(base + 'panels/solidtumor_v1.0/panel_targets.csv')
		assert solidPanelFile.exists()
		loadingService.loadPanelAndTargets("SolidTumor", "1.0.0", "Sequenom", solidPanelFile)
	}
	
	def loadMutations() {
	}
	
	def seedTestKnowledgeBase() {

		def base = dataBaseDirectory()

		File file = new File(base + 'mutations/known_mutations.csv')
		loadingService.loadKnownMutations(new FileReader(file))
		
		File kbFile = new File(base + 'mutations/knowledge.xml')
		loadingService.loadKnowledgeData(new FileReader(kbFile))
		
		File cosmicFile = new File(base + 'mutations/cosmic.xml')
		loadingService.loadCosmicData(new FileReader(cosmicFile))
		// Order here does matter, as we need to preserve referential integrity
	}
	
	def seedTestUsers() {
		
		log.info("Creating users...")
		
		def contributorRole = SecRole.findByAuthority('ROLE_GPS-CONTRIBUTORS') ?: new SecRole(authority: 'ROLE_GPS-CONTRIBUTORS').save(failOnError: true)
		def oicrRole = SecRole.findByAuthority('ROLE_GPS-OICR') ?: new SecRole(authority: 'ROLE_GPS-OICR').save(failOnError: true)
		def adminRole = SecRole.findByAuthority('ROLE_GPS-ADMINS') ?: new SecRole(authority: 'ROLE_GPS-ADMINS').save(failOnError: true)
		def managerRole = SecRole.findByAuthority('ROLE_GPS-MANAGERS') ?: new SecRole(authority: 'ROLE_GPS-MANAGERS').save(failOnError: true)
		def userRole = SecRole.findByAuthority('ROLE_GPS-USERS') ?: new SecRole(authority: 'ROLE_GPS-USERS').save(failOnError: true)
		
		def adminUser = SecUser.findByUsername('admin') ?: new SecUser(
			username: 'admin',
			password: springSecurityService.encodePassword('admin'),
			enabled: true).save(failOnError: true)
		if (!adminUser.authorities.contains(adminRole)) {
			SecUserSecRole.create adminUser, adminRole
		}

		// Added a manager role: this is someone who is allowed to manage the
		// patient information, and view most of the rest of the system.
		
		def managerUser = SecUser.findByUsername('manager') ?: new SecUser(
			username: 'manager',
			password: springSecurityService.encodePassword('manager'),
			enabled: true).save(failOnError: true)
		if (!managerUser.authorities.contains(managerRole)) {
			SecUserSecRole.create managerUser, managerRole
		}

		// Added a guest role, to allow people to see certain parts of GPS
		// without giving them the right to change anything. This requires
		// something of an overhaul as we want to restrict most of the rest
		// of the application to users. This seed is a test user for guests.
		
		def userUser = SecUser.findByUsername('user') ?: new SecUser(
			username: 'user',
			password: springSecurityService.encodePassword('user'),
			enabled: true).save(failOnError: true)
		if (!userUser.authorities.contains(userRole)) {
			SecUserSecRole.create userUser, userRole
		}
		if (!userUser.authorities.contains(contributorRole)) {
			SecUserSecRole.create userUser, contributorRole
		}
	}
	
	void seedTestConfig() {
		log.info("Adding config settings")
		
		seedConfig('createSampleEmail', 'Stuart.Watt@oicr.on.ca')
		seedConfig('reminderSampleEmail', 'Stuart.Watt@oicr.on.ca')
		seedConfig('receivedSampleEmail', 'Stuart.Watt@oicr.on.ca')
		seedConfig('informationEmail', 'Stuart.Watt@oicr.on.ca')
		seedConfig('updateEmail', 'Stuart.Watt@oicr.on.ca')
		seedConfig('reminderDelayInMinutes', '120')
		seedConfig('emailSubjectPrefix', 'gps test')
		seedConfig('institutions', 'UHN/PMH,Hamilton,London,Ottawa')
	}
	
	private void seedConfig(String key, String value) {
		AppConfig.findByConfigKey(key) ?: new AppConfig(configKey: key, configValue: value).save(failOnError: true)
	}
	
	void seedTestData() {
		// Seed only when there's no data
		if(Sample.count() == 0) {
			log.info("Seeding database with some data")
			def random = new Random()
			def types = ['FFPE','Frozen','Blood','FNA']
			def dna = ['Good', 'Bad', 'Ugly']
			def genders = ['M', 'F']
			def dataTypes = ['Sequenom', 'PacBio', 'ABI']
			
			// Use an odd subject count, so we can test page conditions properly
			for (i in 0..17) {
				
				// This was previously random, a formatted test identifier is more
				// obviously for test, and more consistent with the GPS trials
				def patientId = "TST" + String.format('%03d', i)
				def gender = genders[i%2]
				
				def newSubject = new Subject()
				newSubject.patientId = patientId
				newSubject.gender = gender
				newSubject.summary = new Summary()
				newSubject.summary.subject = newSubject
				newSubject.save(validate: true, failOnError:true)
				
				def submission = new Submission(
								dataType: dataTypes[i%3],
								userName: 'username ' + i,
								dateSubmitted: new Date(),
								fileName: 'filename.txt',
								fileContents: 1 as byte
								)
				submission.save(failOnError:true)

				def sample = new Sample(
								barcode: random.nextInt(100000) ,
								type: types[i%4],
								dnaConcentration: random.nextFloat(),
								dnaQuality: dna[i%3],
								dateReceived: i%2 ? null : new Date(),
								dateCreated: new Date(),
								lastUpdated: new Date()
								)
				
				newSubject.addToSamples(sample)
				
				sample.save(failOnError:true)
			}
		}
		
		
		// Yes, I know this delete is discouraged, but seeding is for testing only, and we don't want to notify
		// about seed data. 
		AuditRecord.executeUpdate("delete AuditRecord ar")
	}
}
