package ca.on.oicr.gps

class JqueryUiDatePickerTagLib {
	def jqDateAndTimePicker = {attrs, body -> 
		def name = attrs.name 
		def id = attrs.id ?: name
		def value = attrs.value
		def disabled = attrs.disabled ?: ''
		
		def date = value ? formatDate(format:'dd MMM, yyyy @ hh:mm a',date:value) : ''
		def year = value ? formatDate(format:'yyyy',date:value) : ''
		def month = value ? formatDate(format:'MM',date:value) : ''
		def day = value ? formatDate(format:'dd',date:value) : ''
		def hour = value ? formatDate(format:'HH',date:value) as int : ''
		def minute = value ? formatDate(format:'mm',date:value) as int : ''
		
		//Create date text field and supporting hidden text fields need by grails
		out.println "<input autocomplete=\"off\" class=\"datetimepicker\" ${disabled} type=\"text\" name=\"${name}\" id=\"${id}\" value=\"${date}\" />"
		out.println "<input type=\"hidden\" name=\"${name}_day\" id=\"${id}_day\" value=\"${day}\"/>"
		out.println "<input type=\"hidden\" name=\"${name}_month\" id=\"${id}_month\" value=\"${month}\"/>"
		out.println "<input type=\"hidden\" name=\"${name}_year\" id=\"${id}_year\" value=\"${year}\"/>"
		out.println "<input type=\"hidden\" name=\"${name}_hour\" id=\"${id}_hour\" value=\"${hour}\"/>"
		out.println "<input type=\"hidden\" name=\"${name}_minute\" id=\"${id}_minute\" value=\"${minute}\"/>"
	}

	def jqDatePicker = {attrs, body -> 
		def name = attrs.name 
		def id = attrs.id ?: name
		def value = attrs.value
		def disabled = attrs.disabled ?: ''
		
		def date = value ? formatDate(format:'dd MMM, yyyy',date:value) : ''
		def year = value ? formatDate(format:'yyyy',date:value) : ''
		def month = value ? formatDate(format:'MM',date:value) : ''
		def day = value ? formatDate(format:'dd',date:value) : ''
		
		//Create date text field and supporting hidden text fields need by grails
		out.println "<input autocomplete=\"off\" class=\"datepicker\" ${disabled} type=\"text\" name=\"${name}\" id=\"${id}\" value=\"${date}\" />"
		out.println "<input type=\"hidden\" name=\"${name}_day\" id=\"${id}_day\" value=\"${day}\"/>"
		out.println "<input type=\"hidden\" name=\"${name}_month\" id=\"${id}_month\" value=\"${month}\"/>"
		out.println "<input type=\"hidden\" name=\"${name}_year\" id=\"${id}_year\" value=\"${year}\"/>"
	}
}
