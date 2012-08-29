var GPS = {
    ajaxSetup:
        function() {
    		jQuery('#spinner').ajaxStart(function() {jQuery(this).show();});
    		jQuery('#spinner').ajaxStop(function() {jQuery(this).hide();});
            
    		jQuery('body').delegate('.dialog-trigger', 'click', GPS.juiDialogTrigger);
    		jQuery('#jqdialog').delegate('form', 'submit', GPS.juiDialogForm);
        },
	juiDatePicker:
		function() {
			jQuery('.datepicker').datepicker({
				dateFormat: 'd M, yy',
				onClose: function(dateText, inst) {
					//console.log("#" + inst.id + "_day");
					jQuery("#" + inst.id + "_day").attr("value",inst.selectedDay);
					jQuery("#" + inst.id + "_month").attr("value",inst.selectedMonth + 1);
					jQuery("#" + inst.id + "_year").attr("value",inst.selectedYear); 
				}
			});
			jQuery('.datetimepicker').datetimepicker({
				ampm:true,
				dateFormat: 'd M, yy',
				timeFormat: 'hh:mm TT',
				separator: ' @ ',
				onClose: function(dateText, inst) {
					//console.log("#" + inst.id + "_day");
					jQuery("#" + inst.id + "_day").attr("value",inst.selectedDay);
					jQuery("#" + inst.id + "_month").attr("value",inst.selectedMonth + 1);
					jQuery("#" + inst.id + "_year").attr("value",inst.selectedYear); 
					jQuery("#" + inst.id + "_hour").attr("value",inst.settings.timepicker.hour); 
					jQuery("#" + inst.id + "_minute").attr("value",inst.settings.timepicker.minute); 
				}
			});
		},
	patientIdPicker:
		function() {
			// TODO: get rid of this absolute URL
			
			//console.log("Registering patientId picker");
			jQuery("#patientIdPicker").autocomplete({
				source: function(req, add) {  
						jQuery.getJSON("../subject/query", req, function(data) {
								var suggestions = [];
								jQuery.each(data, function(i, val){
									suggestions.push(val.patientId);
								});
								add(suggestions);
							});
						}
			});
		},
    juiDialogInit:
        function() {
			// Init Dialog
    		jQuery("#jqdialog").dialog({
				autoOpen: false,
		        resizable: false,
		        modal: false,
		        width:'auto'
			});
        },
	juiDialogTrigger:
		function() {
			jQuery('#jqdialog').html(jQuery('#spinner').html());
			jQuery('#jqdialog').dialog("open");
			jQuery('#jqdialog').load(jQuery(this).attr('href') + ' .body', function() {
                // adds juiDatePicker events to dialog
                GPS.juiDatePicker();
                // adds subject picking events and handling to dialog
                GPS.patientIdPicker();
            });
            return false;
        },
    juiDialogForm:
        function() {
    	jQuery('#jqdialog').load(jQuery(this).data('url') + ' .body', jQuery(this).serialize(), function() {
    		jQuery('#page').load(window.location.href + ' #page .body');
                GPS.juiDatePicker();    // adds juiDatePicker events to dialog
            });
            // do not submit the form
            return false;
        }
}

jQuery(function(){
	GPS.ajaxSetup();
    GPS.juiDatePicker();
	GPS.juiDialogInit();
});

// Here follows code for the more dynamic elements of the system. These are all based
// on jQuery, and we do kind of hope that they work

function adjust_tumour_sections(field_id) {
    var a  = field_id.split('_');
    var num = a[1];
    var tumour_type = jQuery('#tumour_'+num).val();

    //console.log("Field: " + num + ", type: " + tumour_type + ", freq: " + freq);
	var divName = "#tumour_div_" + num;
    if (tumour_type == '') {
    	jQuery(divName).hide();
    	jQuery('#tumour_selected_'+num).removeAttr("checked");
    } else {
    	
    	var mutation = jQuery('#mutation').val();
    	//console.log("adjust_tumour_sections: " + mutation);
    	
    	jQuery.ajax({
    		url: 'getTumourTypeFrequency', 
    		data: {"mutation": mutation, "tumour_type": tumour_type},
    		type: "POST",
    		error: function(jqXHR, textStatus, errorThrown) {
    			jQuery('#tumour_'+num).val('');
    	    	jQuery('#tumour_selected_'+num).removeAttr("checked");
    		},
    		success: function(data) {
    			var freq = data.frequency;
    			//console.log("Frequency: " + freq);
    	    	jQuery(divName + " .tumour_type_header .tumour_type_index").empty().append(num + '. ');
    	    	jQuery(divName + " .tumour_type_header .tumour_type_name").empty().append(tumour_type);
    	    	jQuery(divName + " .tumour_type_header .tumour_type_frequency").empty().append('');
    	    	jQuery(divName).show();
    	    	
    	    	jQuery('#tumour_selected_'+num).attr("checked", "checked");
    	    	toggle_tumour_type(num, true);
    		}});
    }
}

function delete_clinical_significance(div) {
	var divElement = jQuery("#" + div);
	divElement.remove();
}

function insert_clinical_significance(mutation, div) {
	//var tumour_type = jQuery("#" + div + " .tumour_type_header .tumour_type_name").html()
	jQuery.post('insertSignificance', {},
		function(data) {
			jQuery("#" + div).after(data);
		});
}

function insert_mutation_agent(mutation,div) {
	jQuery.post('insertMutationAgent', 
		function(data) {
			jQuery("#" + div).before(data);
		});
}

function insert_sensitivity(type) {
	jQuery.post('insertSensitivity', {"type": type},
		function(data) {
			jQuery("#sensitivity_resistance").append(data);
		});
}

function toggle_tumour_type(num, activate) {
	//console.log("Toggle tumout type: " + num + ", " + activate);
    var tumour_type = jQuery('tumour_'+num).val();
    var freq        = jQuery('frequency_'+num).val();
    if (activate) {
    	jQuery('#tumour_div_'+num).show();	
		var i = parseInt(num)+1;
		jQuery('#tumour_type_'+num).innerHTML=i+'. '+tumour_type+' ('+freq+'%)';
		//console.log("About to show: " + '#tumour_type_'+num)
		jQuery('#tumour_div_'+num).show();
		jQuery('#tumour_'+num).removeAttr("disabled");
		//jQuery('#frequency_'+num).removeAttr("disabled");
    } else {
    	jQuery('#tumour_div_'+num).hide();	
    	jQuery('#tumour_'+num).attr("disabled", "disabled");
    	//jQuery('#frequency_'+num).attr("disabled", "disabled");
    }
}

function adjust_significance_visibility(div, value) {
	//console.log("Adjusting significance visibility: " + div + ", " + value)
	if (value!='-' && value!='unknown') {
		jQuery(div).show()
	} else {
		jQuery(div).hide()
	}
}

function confirm_submit(action) {
	jQuery("#submit_action").val(action);
	return true;
}

function tumour_type_autocomplete_callback(request, response) {
	var term = request.term;
	var mutation = jQuery('#mutation').val();
	jQuery.get('autocompleteTumourType', {"term": term, "mutation": mutation},
		function(data) { response(data) }
	);
}