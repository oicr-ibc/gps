<%@ page import="ca.on.oicr.gps.model.data.Subject" %>
<div class="blockPanel" id="summaryBlockPanel">
	<div class="blockTitle">
		<h3>GPS trial report</h3>
	</div>
	<div class="blockBody">
		<p>Number of participants: <b>${Subject.countAll()}</b></p>
		<p>Number still in processing: <b>${Subject.countActive()}</b></p>
	</div>
</div>