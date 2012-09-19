package com.attask.descriptiondashboard;

import hudson.model.Result;
import hudson.model.Run;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.regex.Pattern;

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
	private final Set<SimpleUser> committers;
	private final boolean visible;

	public static Cell createFromBuild(Run build, boolean visible, Pattern testStatusRegex, int testStatusGroup, int logLinesToSearch) {
		String description = build.getDescription();
		String name = build.getFullDisplayName();
		boolean running = build.isBuilding();

		int failureCount;
		Result result;
		if(running) {
			failureCount = ProjectUtils.grepFailureCount(build, testStatusRegex, testStatusGroup, logLinesToSearch);
			if(failureCount == -1) {
				result = Result.NOT_BUILT;
			} else if(failureCount == 0) {
				result = Result.SUCCESS;
			} else {
				result = Result.UNSTABLE;
			}
		} else {
			failureCount = ProjectUtils.getFailureCount(build);
			result = build.getResult();
		}
		String resultString = result == null ? "RUNNING" : result.toString();
		Set<SimpleUser> committers = ProjectUtils.findCommitters(build);
		return new Cell(name, failureCount, resultString, description, build.getTime(), build.getParent().getName(), build.getNumber(), running, committers, visible);
	}

	private Cell(String name, int failures, String result, String description, Date date, String projectName, int buildNumber, boolean running, Set<SimpleUser> committers, boolean visible) {
		this.name = name;
		this.failures = failures;
		this.result = result;
		this.description = description;
		this.date = date;
		this.projectName = projectName;
		this.buildNumber = buildNumber;
		this.running = running;
		this.committers = committers;
		this.visible = visible;
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
	public Set<SimpleUser> getCommitters() {
		return committers;
	}

	@Exported
	public int getNumberCommitters() {
		return committers.size();
	}

	@Exported
	public boolean getVisible() {
		return visible;
	}
}
