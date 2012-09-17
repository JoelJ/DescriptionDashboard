package com.attask.descriptiondashboard;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;

/**
 * User: Joel Johnson
 * Date: 9/7/12
 * Time: 8:25 PM
 */
@ExportedBean
public class Header implements Serializable {
	private final String name;
	private final String alias;
	private final Requirement requirement;
	private final boolean visible;

	public static Header parseFromRequest(String job, StaplerRequest request) {
		String alias = request.getParameter("job_" + job + "_alias");
		if(alias == null) {
			alias = job;
		}

		String visibleString = request.getParameter("job_" + job + "_visible");
		boolean visible;
		visible = visibleString == null || Boolean.parseBoolean(visibleString);

		String requirementString = request.getParameter("job_" + job + "_requirement");
		Header.Requirement requirement;
		if(requirementString != null) {
			requirement = Header.Requirement.valueOf(requirementString);
		} else {
			requirement = Requirement.Required;
		}

		return new Header(job, alias, requirement, visible);
	}

	public Header(String name, String alias, Requirement requirement, boolean visible) {
		this.name = name;
		this.alias = alias;
		this.requirement = requirement;
		this.visible = visible;
	}

	@Exported
	public String getName() {
		return name;
	}

	@Exported
	public String getAlias() {
		return alias;
	}

	@Exported
	public boolean getVisible() {
		return visible;
	}

	@Override
	public String toString() {
		return name;
	}

	@Exported
	public Requirement getRequirement() {
		return requirement;
	}

	public static enum Requirement {
		Required, RequiredWhenPresent, NotRequired
	}
}
