
<div class="blockPanel" id="linkBlockPanel">
	<div class="blockTitle">
		<h3>Reports</h3>
	</div>
	<div class="blockBody">
		<p><g:link action="index" controller="observedMutation">Show mutation summary</g:link></p>
		<p><g:link action="mutations" controller="summary">Show mutations by patient</g:link></p>
        <sec:ifAnyGranted roles="ROLE_GPS-CONTRIBUTORS">
		<p><g:link action="index" controller="query">Show custom queries</g:link></p>
		</sec:ifAnyGranted>
        <sec:ifAnyGranted roles="ROLE_GPS-ADMINS">
		<p><g:link action="index" controller="status">Show Medidata report status</g:link></p>
		</sec:ifAnyGranted>
	</div>
</div>