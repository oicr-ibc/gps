
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'patients.label', default: 'Patients')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="page">
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="additionalBlock"><g:link action="export" controller="subject"><g:message code="default.export.excel" default="Export data to Excel"/></g:link></div>
	        <div id="tabs">
			    <ul>
			        <li><a href="#summarycontainer"><span>Status</span></a></li>
			        <li><a href="#clinicalcontainer"><span>Clinical</span></a></li>
			        <li><a href="#laboratorycontainer"><span>Laboratory</span></a></li>
			    </ul>
			    <div id="summarycontainer">
			    	<table id="summarygrid" class="summarygrid"></table>
			    	<div id="summarypager"></div>
			    </div>
			    
			    <div id="clinicalcontainer">
			    	<table id="clinicalgrid" class="summarygrid"></table>
			    	<div id="clinicalpager"></div>
			    </div>
			    
			    <div id="laboratorycontainer">
			    	<table id="laboratorygrid" class="summarygrid"></table>
			    	<div id="laboratorypager"></div>
			    </div>
			</div>
        </div>
        </div>
        <content tag="postJQuery">
			<%-- This is included after we have loaded jQuery --%>
        	<g:javascript>

// Added inline rather than in gps.js, at least for now, as this is where the
// view is being defined. And, obviously, all of this needs to be switched
// according to locale. Unfortunately, because jqGrid and jQuery's datepicker
// use a different convention for date parsing, we can't do that. 

var dateHandler = function(elem){ 
                      //setTimeout(function(){ 
                        jQuery(elem).datepicker({
                          dateFormat: 'dd/mm/yy',
                          onClose: function(dateText, inst) { 
                            setTimeout(function(){
                              jQuery(elem).datepicker().trigger({ type: 'keydown', which: 13, keyCode: 13 });
                            }, 100);
                          }
                        });
                      //}, 100); 
                    };

function handleCellError(res, stat) {
	//console.log(res);
	//console.log(stat);
	var data = jQuery.parseJSON(res.responseText);
	var responses = data.messages.join("<br/>");
	jQuery.jgrid.info_dialog(jQuery.jgrid.errors.errcap, responses, jQuery.jgrid.edit.bClose);
}

var allowEditing = false;

function ISODateString(d){
    function pad(n){return n<10 ? '0'+n : n}
    return d.getUTCFullYear()+'-'
        + pad(d.getUTCMonth()+1)+'-'
        + pad(d.getUTCDate())+'T'
        + pad(d.getUTCHours())+':'
        + pad(d.getUTCMinutes())+':'
        + pad(d.getUTCSeconds())+'Z'
}

function handleBeforeSubmitCell(rowid,celname,value,iRow,iCol) {
    var cellOptions = jQuery("#summarygrid").jqGrid('getColProp', celname);
    var result = value;
    if (cellOptions.formatter == "date") {
        var formatter = jQuery.jgrid.formatter.date;
        var cellFormat = cellOptions.datefmt;
        result = ISODateString(jQuery.jgrid.parseDate(cellFormat, value));
    }
    return {parsedValue: result};
};

function linkFormatter (cellvalue, options, rowObject) {
   return "<a href=\"${createLinkTo(dir:"summary", file:"show")}/" + cellvalue + "\">" 
     + "<img class=\"linkicon\" src=\"${createLinkTo(dir:"images", file:"information.png")}\">" 
     + "</a>";
};

<sec:ifAnyGranted roles="ROLE_GPS-MANAGERS">
allowEditing = true;
</sec:ifAnyGranted>
var modelShowPage =         {name:'id', index:'id', width: 30, editable: false,
                             formatter: linkFormatter};
var modelPatientId = 		{name:'patientId', index:'patientId', width: 90, editable: allowEditing,
	                  		 editoptions: {size: 16, maxlength: 16}};
var modelElapsedWorkingDays = 
							{name:'summary.elapsedWorkingDays', index:'summary.elapsedWorkingDays', width: 70, editable: false};
var modelGender =    		{name:'gender', index:'gender', width: 60, editable: allowEditing, edittype: 'select',
	                  		 editoptions: {
	     				  		size: 1, maxlength: 1, dataUrl: "${createLinkTo(dir:'data',file:'sexes')}"
	   				 		}};
var modelInstitution = 		{name:'summary.institution', index:'summary.institution', width:85, editable: allowEditing, edittype: 'select',
	   						 editoptions: {
	     					 	dataUrl: "${createLinkTo(dir:'data',file:'institutions')}"
	   						}};
var modelPsychosocial =     {name:'summary.psychosocial', index:'summary.psychosocial', width:80, editable: allowEditing, edittype:"select", 
							 editoptions: {
							 	dataUrl: "${createLinkTo(dir:'data',file:'psychosocial')}"
							 }};
var modelPrimaryPhysician = {name:'summary.primaryPhysician', index:'summary.primaryPhysician', width:70, editable: allowEditing};
var modelPrimaryTumorSite = {name:'summary.primaryTumorSite', index:'summary.primaryTumorSite', width:95, editable: allowEditing};
var modelConsentDate = 		{name:'summary.consentDate', index:'summary.consentDate', width: 90, editable: allowEditing, formatter: 'date', datefmt: 'd/m/Y', formatoptions: {newformat: 'd/m/Y'},
	   						 editoptions: {size: 11, maxlength: 11, dataInit: dateHandler}, editrules: {date: true, required: false}};
var modelBiopsyDate = 		{name:'summary.biopsyDate', index:'summary.biopsyDate', width: 90, editable: allowEditing, formatter: 'date', datefmt: 'd/m/Y', formatoptions: {newformat: 'd/m/Y'},
	   						 editoptions: {size: 11, maxlength: 11, dataInit: dateHandler}, editrules: {date: true, required: false}};
var modelBiopsySite = 		{name:'summary.biopsySite', index:'summary.biopsySite', width: 90, editable: allowEditing};
var modelBiopsyCores = 		{name:'summary.biopsyCores', index:'summary.biopsyCores', width: 60, editable: allowEditing};
var modelPathologyArrivalDate = {name:'summary.pathologyArrivalDate', index:'summary.pathologyArrivalDate', width:90, editable: allowEditing, formatter: 'date', datefmt: 'd/m/Y', formatoptions: {newformat: 'd/m/Y'},
	   						 editoptions: {size: 11, maxlength: 11, dataInit: dateHandler}, editrules: {date: true, required: false}};
var modelSequenomArrivalDate = {name:'summary.sequenomArrivalDate', index:'summary.sequenomArrivalDate', width:90, editable: allowEditing, formatter: 'date', datefmt: 'd/m/Y', formatoptions: {newformat: 'd/m/Y'},
	   						 editoptions: {size: 11, maxlength: 11, dataInit: dateHandler}, editrules: {date: true, required: false}};
var modelPacbioArrivalDate =  {name:'summary.pacbioArrivalDate', index:'summary.pacbioArrivalDate', width:90, editable: allowEditing, formatter: 'date', datefmt: 'd/m/Y', formatoptions: {newformat: 'd/m/Y'},
	   						 editoptions: {size: 11, maxlength: 11, dataInit: dateHandler}, editrules: {date: true, required: false}};
var modelMedidataUploadDate =  {name:'summary.medidataUploadDate', index:'summary.medidataUploadDate', width:90, editable: true, formatter: 'date', datefmt: 'd/m/Y', formatoptions: {newformat: 'd/m/Y'},
	   						 editoptions: {size: 11, maxlength: 11, dataInit: dateHandler}, editrules: {date: true, required: false}};
var modelArchivalArrivalDate =  {name:'summary.archivalArrivalDate', index:'summary.archivalArrivalDate', width:90, editable: allowEditing, formatter: 'date', datefmt: 'd/m/Y', formatoptions: {newformat: 'd/m/Y'},
	   						 editoptions: {size: 11, maxlength: 11, dataInit: dateHandler}, editrules: {date: true, required: false}};
var modelExpertPanelDecisionDate =
						  	{name:'firstDecisionDate', index:'firstDecisionDate', width:90, editable: false, 
						  	 formatter: 'date', 
						  	 datefmt: 'd/m/Y', formatoptions: {newformat: 'd/m/Y'}
	   						 };
var modelExpertPanelDecision = 
							{name:'renderedDecision', index:'renderedDecision', width:140, editable: false,
							 formatter: 'showlink', 
							 formatoptions:{ baseLinkUrl: '${createLinkTo(dir:"summary", file:"show")}', addParam: '#decisions' }
							 };
var modelExpertPanelArchivalDecision = 
							{name:'renderedArchivalDecision', index:'renderedArchivalDecision', width:140, editable: false,
							 formatter: 'showlink', 
							 formatoptions:{ baseLinkUrl: '${createLinkTo(dir:"summary", file:"show")}', addParam: '#decisions' }
							 };
var modelReportedMutations = 
							{name:'summary.reportedMutations', index:'summary.reportedMutations', width:90, editable: true, edittype: 'textarea',
	   						 editoptions: {rows: 4, cols: 50}};
var modelComment =          {name:'summary.comment', index:'summary.comment', width:160, editable: true, edittype: 'textarea',
                             editoptions: {rows: 4, cols: 30}};
var modelMutations =        {name:'mutations', index:'mutations', width:90, editable: false, 
	   						 formatter: 'showlink', formatoptions:{baseLinkUrl: '${createLinkTo(dir:"report", file:"subject")}'}
							 };

jQuery(function(){ 
  jQuery("#summarygrid").jqGrid({
    cellEdit: allowEditing,
    url: "${createLinkTo(dir:'subject',file:'list')}",
    cellurl: "${createLinkTo(dir:'subject',file:'update')}",
    editurl: "${createLinkTo(dir:'subject',file:'edit')}",
    errorCell: handleCellError,
	datatype: 'json',
	mtype: 'GET',
	jsonReader : {
     root: "subjects",
     page: "page",
     total: "total",
     records: "records",
     repeatitems: false,
     id: "id",
     userdata: "userdata"
   },
   beforeSubmitCell: handleBeforeSubmitCell,
   gridComplete: function() {
     jQuery(".summarygrid td input").each(function(){
       jQuery(this).click(function(event) {
          var id = jQuery(this).parents('tr').attr('id');
          var colname = jQuery(this).parents('td').attr('aria-describedby').replace("summarygrid_", "");
          var value = jQuery(this).attr("checked") ? 1 : 0;
          data = {id: id};
          data[colname] = value;
          jQuery.ajax({
            url: '${createLinkTo(dir:'subject',file:'update')}',
            type: 'POST',
            data: data
          });
        });
     });
   },
   colNames:['',
             'Patient Id', 
   			 'Elapsed working days',
	          'Institution', 
	          'Consent date', 
	          'Date of biopsy', 
	          'Tumor arrival in pathology',
	          'Tumor arrival in Sequenom',
	          'DNA arrival in PacBio/OICR',
	          'Expert panel decision date',
	          'Archival tumor received'
	          ],
	colModel :[ 
	  modelShowPage,
	  modelPatientId,
	  modelElapsedWorkingDays,
	  modelInstitution,
	  modelConsentDate,
	  modelBiopsyDate,
	  modelPathologyArrivalDate,
	  modelSequenomArrivalDate,
	  modelPacbioArrivalDate,
	  modelExpertPanelDecisionDate,
	  modelArchivalArrivalDate
	],
	pager: '#summarypager',
	sortname: 'patientId',
	sortorder: 'desc',
	rowNum:20,
	rowList:[10,20,30],
	height: "auto",
	autowidth: false,
	viewrecords: true,
    cellEdit: true,
	gridview: true, 
    /*** remove hide/expand button **/
    hidegrid: false
  });
  jQuery("#clinicalgrid").jqGrid({
    cellEdit: allowEditing,
    url: "${createLinkTo(dir:'subject',file:'list')}",
    cellurl: "${createLinkTo(dir:'subject',file:'update')}",
    editurl: "${createLinkTo(dir:'subject',file:'edit')}",
    errorCell: handleCellError,
	datatype: 'json',
	mtype: 'GET',
	jsonReader : {
     root: "subjects",
     page: "page",
     total: "total",
     records: "records",
     repeatitems: false,
     id: "id",
     userdata: "userdata"
   },
   beforeSubmitCell: handleBeforeSubmitCell,
   colNames:['',
             'Patient Id', 
             'Elapsed working days',
	          'Gender', 
	          'Institution', 
	          'Primary physician', 
	          'Primary tumor site', 
	          'Date of biopsy', 
	          'Biopsy site',
	          'No. of cores biopsied',
	          'Observed mutations',
//	          'Reported mutations',
	          'Date of expert panel decision',
	          'Expert panel decision note',
	          'Expert panel archival decison',
	          'Comments'
	          ],
	colModel :[ 
	  modelShowPage,
	  modelPatientId,
	  modelElapsedWorkingDays,
	  modelGender,
	  modelInstitution,
	  modelPrimaryPhysician,
	  modelPrimaryTumorSite,
	  modelBiopsyDate,
	  modelBiopsySite,
	  modelBiopsyCores,
	  modelMutations,
//	  modelReportedMutations,
	  modelExpertPanelDecisionDate,
	  modelExpertPanelDecision,
	  modelExpertPanelArchivalDecision,
	  modelComment
	],
	pager: '#clinicalpager',
	sortname: 'patientId',
	sortorder: 'desc',
	rowNum:20,
	rowList:[10,20,30],
	height: "auto",
	autowidth: false,
	viewrecords: true,
    cellEdit: true,
	gridview: true, 
    /*** remove hide/expand button **/
    hidegrid: false
  });
  jQuery("#laboratorygrid").jqGrid({
    cellEdit: allowEditing,
    url: "${createLinkTo(dir:'subject',file:'list')}",
    cellurl: "${createLinkTo(dir:'subject',file:'update')}",
    editurl: "${createLinkTo(dir:'subject',file:'edit')}",
    errorCell: handleCellError,
	datatype: 'json',
	mtype: 'GET',
	jsonReader : {
     root: "subjects",
     page: "page",
     total: "total",
     records: "records",
     repeatitems: false,
     id: "id",
     userdata: "userdata"
   },
   beforeSubmitCell: handleBeforeSubmitCell,
   colNames:['',
             'Patient Id', 
   			  'Gender',
	          'Institution', 
	          'Date of biopsy', 
	          'Primary tumor site', 
	          'No. of cores biopsied',
	          'Tumor arrival in pathology',
	          'Tumor arrival in Sequenom',
	          'DNA arrival in PacBio/OICR',
	          'Archival tumor received',
	          'Expert panel decision date',
	          'Observed mutations',
	          'Reported mutations',
	          'Comments'
	          ],
	colModel :[ 
	  modelShowPage,
	  modelPatientId,
	  modelGender,
	  modelInstitution,
	  modelBiopsyDate,
	  modelPrimaryTumorSite,
	  modelBiopsyCores,
	  modelPathologyArrivalDate,
	  modelSequenomArrivalDate,
	  modelPacbioArrivalDate,
	  modelArchivalArrivalDate,
	  modelExpertPanelDecisionDate,
	  modelMutations,
	  modelReportedMutations,
	  modelComment
	],
	pager: '#laboratorypager',
	sortname: 'patientId',
	sortorder: 'desc',
	rowNum:20,
	rowList:[10,20,30],
	height: "auto",
	autowidth: false,
	viewrecords: true,
    cellEdit: true,
	gridview: true, 
    /*** remove hide/expand button **/
    hidegrid: false
  });
  // Cannot let this pass without comment. The width: "50em" doesn't actually set the width, but if 
  // don't do this, the editoptions column count appears to be ignored (!). If you set it to a pixel
  // width that works, but that is just so bad practice. Tests OK on IE7, Chrome, and FF. 
  jQuery("#clinicalgrid").jqGrid('navGrid','#clinicalpager',
	  {
	   search: false, 
	   edit: false, 
	   del: false,
	   add: allowEditing,
	   /*** Add button caption ***/
	   addtext: 'Add a new patient'		
	   },
	  {closeAfterEdit: true},
	  {closeOnEscape:true, closeAfterAdd: true, width: "50em",
	  	beforeShowForm: function(){
	  		/*** Hide unwanted fields - might be a better way to do this but it works ***/
	  		jQuery('#tr_summary\\.expertPanelDecision, #tr_summary\\.psychosocial, #tr_summary\\.reportedMutations, #tr_summary\\.expertPanelDecisionDate, #tr_summary\\.biopsyCores, #tr_summary\\.pathologyArrivalDate, #tr_summary\\.sequenomArrivalDate, #tr_summary\\.pacbioArrivalDate, #tr_summary\\.medidataUploadDate, #tr_summary\\.archivalArrivalDate').hide();
  		}
 	  }
  );
});

jQuery(document).ready(function() {
  jQuery("#tabs").tabs({ cache: false });
  jQuery("#tabs").bind('tabsselect', 
    function(event, ui) {
      var theTabId = "#" + ui.panel.id + " table";
      jQuery(theTabId).trigger("reloadGrid", [{current:true}]);
      window.location.hash = ui.tab.hash;
    });
  jQuery.address.change(function(event){
    jQuery("#tabs").tabs("select", window.location.hash)
  });
});
        	</g:javascript>
		</content>
    </body>
</html>
