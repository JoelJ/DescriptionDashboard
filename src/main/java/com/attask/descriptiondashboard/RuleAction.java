package com.attask.descriptiondashboard;

import hudson.model.Action;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: Joel Johnson
 * Date: 10/8/12
 * Time: 4:48 PM
 */
@ExportedBean
public class RuleAction implements Action {
	private final Collection<Rule> violatedRules;

	public RuleAction(Collection<Rule> violatedRules) {
		this.violatedRules = new ArrayList<Rule>(violatedRules);
	}

	@Exported
	public Collection<Rule> getViolatedRules() {
		return violatedRules;
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
