package ca.on.oicr.gps.controller

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.on.oicr.gps.model.data.Sample;
import ca.on.oicr.gps.model.data.Subject;

import grails.plugins.springsecurity.Secured

/**
 * Generates reports in a variety of forms. 
 * @author swatt
 *
 */

@Secured(['ROLE_GPS-CONTRIBUTORS'])
class ReportController {

    def index = { 
		
	}
	
	/**
	 * Generates a subject report, this is what is to be uploaded to Medidata. The subject report
	 * is sent as a zip file, containing files for each type of sample, listing the mutations 
	 * identified, broken down by the technology used. For Sequenom and PacBio this is relatively
	 * straightforward, for Sanger it is a little more complex, as we need to ensure that it 
	 * confirms a mutation rather than simply reporting it.
	 */
	def subject = {
		
		Subject sub = Subject.get(params.id)
		assert sub
		
		// Otherwise, fall through to a more HTML based representation. This can be done using
		// HTML, or something similar. 
		
		def data = sub.reports.first().data
		[reportData: data, subject: sub]
	}

	private reportAsZip(Subject sub) {

		String patientId = sub.patientId
		
		response.contentType = "application/zip"
		response.setHeader("Content-disposition", "attachment; filename=${patientId}.zip")
		
		OutputStream output = response.getOutputStream()
		
		// Here follows piloting code that demonstrates how to write a zip file for download
		// from a request. This is probably the best way to ship out a set of files for
		// uploading. However, now that we have access to Medidata Rave, that might not even
		// be the best approach.
		
		ZipOutputStream zip = new ZipOutputStream(output)
		
		PrintStream out = openZipEntry(zip, "${patientId}.germline.txt")
		out.println("Germline file text here")
		closeZipEntry(zip, out)
		
		out = openZipEntry(zip, "${patientId}.frozen.txt")
		out.println("Frozen file text here")
		closeZipEntry(zip, out)
		
		zip.close()
	}
		
	private PrintStream openZipEntry(ZipOutputStream zip, String fileName) {
		zip.putNextEntry(new ZipEntry(fileName))
		out = new PrintStream(zip)
		return out
	}
	
	private void closeZipEntry(ZipOutputStream zip, PrintStream out) {
		out.flush()
		zip.closeEntry()
	}
}
