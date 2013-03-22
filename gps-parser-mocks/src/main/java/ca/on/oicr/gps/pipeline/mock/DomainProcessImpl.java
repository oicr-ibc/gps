package ca.on.oicr.gps.pipeline.mock;

import java.util.Date;

import ca.on.oicr.gps.pipeline.domain.DomainProcess;

public class DomainProcessImpl implements DomainProcess {
	
	private String chipcode = null;
	private Date date = null;
	
	private String runId = null;
	private String panelName = null;
	private String panelVersion = null;

	public DomainProcessImpl(String runId, String panelName, String panelVersion) {
		super();
		this.runId = runId;
		this.panelName = panelName;
		this.panelVersion = panelVersion;
	}

	public void setChipcode(String value) {
		chipcode = value;
	}

	public void setDate(Date value) {
		date = value;
	}

	public String getChipcode() {
		return chipcode;
	}

	public Date getDate() {
		return date;
	}

	public String getRunId() {
		return runId;
	}

	public String getPanelName() {
		return panelName;
	}

	public String getPanelVersion() {
		return panelVersion;
	}

}
