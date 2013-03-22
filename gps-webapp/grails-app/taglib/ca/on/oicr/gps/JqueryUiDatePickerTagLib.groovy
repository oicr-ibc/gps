package ca.on.oicr.gps

import java.text.DateFormat
import java.text.SimpleDateFormat

class JqueryUiDatePickerTagLib {
	
	DateFormat dateFormatter = new SimpleDateFormat('dd MMM, yyyy @ hh:mm a') 
	DateFormat yearFormatter = new SimpleDateFormat('yyyy') 
	DateFormat monthFormatter = new SimpleDateFormat('MM')
	DateFormat dayFormatter = new SimpleDateFormat('dd')
	DateFormat hourFormatter = new SimpleDateFormat('HH')
	DateFormat minuteFormatter = new SimpleDateFormat('mm')
	
	def jqDateAndTimePicker = {attrs, body -> 
		def name = attrs.name 
		def id = attrs.id ?: name
		Date value = attrs.value as Date
		def disabled = attrs.disabled ?: ''
		
		def date = value ? dateFormatter.format(value) : ''
		def year = value ? yearFormatter.format(value) : ''
		def month = value ? monthFormatter.format(value) : ''
		def day = value ? dayFormatter.format(value) : ''
		def hour = value ? hourFormatter.format(value) : ''
		def minute = value ? minuteFormatter.format(value) : ''
		
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
		Date value = attrs.value as Date
		def disabled = attrs.disabled ?: ''
		
		def date = value ? dateFormatter.format(value) : ''
		def year = value ? yearFormatter.format(value) : ''
		def month = value ? monthFormatter.format(value) : ''
		def day = value ? dayFormatter.format(value) : ''
		
		//Create date text field and supporting hidden text fields need by grails
		out.println "<input autocomplete=\"off\" class=\"datepicker\" ${disabled} type=\"text\" name=\"${name}\" id=\"${id}\" value=\"${date}\" />"
		out.println "<input type=\"hidden\" name=\"${name}_day\" id=\"${id}_day\" value=\"${day}\"/>"
		out.println "<input type=\"hidden\" name=\"${name}_month\" id=\"${id}_month\" value=\"${month}\"/>"
		out.println "<input type=\"hidden\" name=\"${name}_year\" id=\"${id}_year\" value=\"${year}\"/>"
	}
}
