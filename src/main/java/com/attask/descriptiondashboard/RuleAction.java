package com.attask.descriptiondashboard;

import hudson.model.Action;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Joel Johnson
 * Date: 10/8/12
 * Time: 4:48 PM
 */
@ExportedBean
public class RuleAction implements Action {
	private final Set<Rule> violatedRules;
	private final boolean wasRunning;

	public RuleAction(Collection<Rule> violatedRules, boolean wasRunning) {
		this.violatedRules = new HashSet<Rule>(violatedRules);
		this.wasRunning = wasRunning;
	}

	@Exported
	public Set<Rule> getViolatedRules() {
		return violatedRules;
	}

	/**
	 * @return Returns true if the job was running when the rules were applied.
	 */
	@Exported
	public boolean getWasRunning() {
		return wasRunning;
	}

	public String getIconFileName() {
		return null;
	}

	public String getDisplayName() {
		return null;
	}

	public String getUrlName() {
		return "ruleBlame";
	}
}
