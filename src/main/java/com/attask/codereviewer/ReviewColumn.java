package com.attask.codereviewer;

import com.attask.descriptiondashboard.*;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Run;

/**
 * User: Joel Johnson
 * Date: 7/3/12
 * Time: 2:13 PM
 */
@Extension
public class ReviewColumn extends CustomColumn {
	private Run currentBuild;

	public Run getCurrentBuild() {
		return currentBuild;
	}

	@Override
	public String getHeaderName() {
		return "Review";
	}

	@Override
	public void beforeTable(Table table) {
	}

	@Override
	public void beforeRow(Row row) {
		Header firstHeader = row.getHeaders().iterator().next();
		Cell cell = row.getCell(firstHeader.getName());
		if (cell != null) {
			AbstractProject project = ProjectUtils.findProject(cell.getProjectName());
			if (project != null) {
				Run build = project.getBuildByNumber(cell.getBuildNumber());
				if (build != null) {
					currentBuild = build;
					return;
				}
			}
		}
		currentBuild = null;
	}

	/**
	 * Called by cell.jelly
	 */
	public BaseCodeReviewAction getReviewAction() {
		if (currentBuild != null) {
			BaseCodeReviewAction action = currentBuild.getAction(BaseCodeReviewAction.class);
			if (action != null) {
				return action;
			}
		}
		return null;
	}

	@Override
	public void afterRow(Row row) {
		currentBuild = null;
	}

	@Override
	public void afterTable(Table table) {
	}
}
