package ca.on.oicr.gps

class ReferenceLinkTagLib {
	def pubmed = { attrs, body ->
		def bodyString = body().toString()
		
		out << bodyString.replaceAll( /(pmid:)(\d+)/, { "${it[1]}<a href='http://www.ncbi.nlm.nih.gov/pubmed/${it[2]}'>${it[2]}</a>" })
	}
}
