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

	public RuleAction(Collection<Rule> violatedRules) {
		this.violatedRules = new HashSet<Rule>(violatedRules);
	}

	@Exported
	public Set<Rule> getViolatedRules() {
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
