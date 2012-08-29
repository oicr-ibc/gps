package ca.on.oicr.gps.model.knowledge

import java.util.Date;

class MutationConfirmation implements Comparable<MutationConfirmation> {
	
	static belongsTo = [ mutation: KnownMutation ]

	String userName
	String name
	String comment
	byte[] body
	byte[] pdf
	Date date
	
    static constraints = {
		userName(nullable: false, maxSize: 32)
		name(nullable: true, maxSize: 255)
		comment(nullable: true)
		date(nullable: false)
		body(nullable: false, maxSize: 10000000)
		pdf(nullable: false, maxSize: 10000000)
    }

	static mapping = {
        comment type:"text"
    }
	
	/**
	* Implements Comparable, so that we order by date, in a descending order - bad I know
	*/
   public int compareTo(MutationConfirmation obj) {
	   obj.date.compareTo(date)
   }
}
