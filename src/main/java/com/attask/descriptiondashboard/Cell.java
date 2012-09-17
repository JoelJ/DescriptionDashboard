package com.attask.descriptiondashboard;

import hudson.model.Result;
import hudson.model.Run;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * User: Joel Johnson
 * Date: 9/7/12
 * Time: 8:23 PM
 */
@ExportedBean
public class Cell implements Serializable {
	private final String name;
	private final int failures;
	private final String result;
	private final String description;
	private final Date date;
	private final String projectName;
	private final int buildNumber;
	private final boolean running;
	private final Set<String> committers;
	private final int numberCommitters;

	public static Cell createFromBuild(Run build) {
		String description = build.getDescription();
		String name = build.getFullDisplayName();
		int failureCount = ProjectUtils.getFailureCount(build);
		Result result = build.getResult();
		String resultString = result == null ? "RUNNING" : result.toString();
		boolean running = build.isBuilding();
		Set<String> committers = ProjectUtils.findCommitters(build);
		int numberCommitters = ProjectUtils.findNumberCommitters(build);
		return new Cell(name, failureCount, resultString, description, build.getTime(), build.getParent().getName(), build.getNumber(), running, committers, numberCommitters);
	}

	private Cell(String name, int failures, String result, String description, Date date, String projectName, int buildNumber, boolean running, Set<String> committers, int numberCommitters) {
		this.name = name;
		this.failures = failures;
		this.result = result;
		this.description = description;
		this.date = date;
		this.projectName = projectName;
		this.buildNumber = buildNumber;
		this.running = running;
		this.committers = committers;
		this.numberCommitters = numberCommitters;
	}

	@Exported
	public String getName() {
		return name;
	}

	@Exported
	public int getFailures() {
		return failures;
	}

	@Exported
	public String getResult() {
		return result;
	}

	@Exported
	public String getDescription() {
		return description;
	}

	@Exported
	public Date getDate() {
		return date;
	}

	@Exported
	public String getProjectName() {
		return projectName;
	}

	@Exported
	public int getBuildNumber() {
		return buildNumber;
	}

	@Exported
	public boolean getRunning() {
		return running;
	}

	@Exported
	public Set<String> getCommitters() {
		return committers;
	}

	@Exported
	public int getNumberCommitters() {
		return numberCommitters;
	}
}
