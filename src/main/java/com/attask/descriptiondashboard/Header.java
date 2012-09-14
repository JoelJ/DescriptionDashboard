package com.attask.descriptiondashboard;

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
	private String name;
	private String alias;
	private Requirement requirement;

	public Header(String name, String alias, Requirement requirement) {
		this.name = name;
		this.alias = alias;
		this.requirement = requirement;
	}

	@Exported
	public String getName() {
		return name;
	}

	@Exported
	public String getAlias() {
		return alias;
	}

	@Override
	public String toString() {
		return name;
	}

	public Requirement getRequirement() {
		return requirement;
	}

	public static enum Requirement {
		Required, RequiredWhenPresent, NotRequired
	}
}
