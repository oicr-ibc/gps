<%@ page import="ca.on.oicr.gps.model.system.AppConfig" %>
<!DOCTYPE html>
<html>
    <head>
        <title><g:layoutTitle default="Grails" /></title>
        <link rel="stylesheet" href="${resource(dir:'css/jqueryui/smoothness',file:'jquery-ui-1.8.12.custom.css')}" />
        <link rel="stylesheet" href="${resource(dir:'css',file:'ui.jqgrid.css')}" />
        <link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
        <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
        <g:javascript src='libs/jquery-1.7.2.min.js'/>
		    <g:javascript>
		     jQuery.noConflict();
		    </g:javascript>
        <g:javascript src='libs/jquery-ui-1.8.12.custom.min.js'/>
        <g:javascript src='libs/jquery-ui-timepicker-addon.js'/>
        <g:javascript src='libs/i18n/grid.locale-en.js'/>
        <g:javascript src='libs/jquery.jqGrid.min.js'/>
        <g:javascript src='libs/jquery.address-1.4.min.js'/>
        <g:javascript src='gps.js'/>
        <g:layoutHead />
    </head>
    <body>
        <div id="spinner" class="spinner" style="display:none;">
            <img src="${resource(dir:'images',file:'spinner.gif')}" alt="${message(code:'spinner.alt',default:'Loading...')}" />
        </div>
        <div id="logo">
            <a href="http://www.oicr.on.ca/">
                <img src="${resource(dir:'images',file:'oicr_logo_small.jpg')}" alt="OICR" border="0" />
            </a>
        </div>
   				<div id='loginLinkContainer'>
	   				<sec:ifLoggedIn>
						Logged in as <sec:username/> (<g:link controller='logout'>Logout</g:link>)
					</sec:ifLoggedIn>
				</div>
				<div id="versionContainer">
				<g:if env="test">Test</g:if>
				<g:if env="development">Development</g:if>
				<g:if env="staging">Staging</g:if>
				<g:message code="Version"/> <g:meta name="app.version"/></div>
		<div class="nav">
           	<sec:ifAnyGranted roles="ROLE_GPS-USERS">
            <span class="menuButton"><g:link class="list" controller="summary" action="home"><g:message code="Home" /></g:link></span>
            </sec:ifAnyGranted>
            <sec:ifAnyGranted roles="ROLE_GPS-USERS">
            <span class="menuButton"><g:link class="list" controller="summary" action="index"><g:message code="Patients" /></g:link></span>
            <span class="menuButton"><g:link class="list" controller="sample" action="list"><g:message code="Samples" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="list" controller="submission" action="list"><g:message code="Submissions" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="list" controller="observedMutation" action="list"><g:message code="Mutations" args="[entityName]" /></g:link></span>
           	</sec:ifAnyGranted>
           	<sec:ifAnyGranted roles="ROLE_GPS-USERS">
            <span class="menuButton"><g:link class="list" controller="knowledge" action="index"><g:message code="Knowledge Base" /></g:link></span>
			</sec:ifAnyGranted>
        </div>
        <g:layoutBody />
        <div id="jqdialog"></div>
        <div id="postJQuery">
            <g:pageProperty name="page.postJQuery" />
        </div>
    </body>
</html>
