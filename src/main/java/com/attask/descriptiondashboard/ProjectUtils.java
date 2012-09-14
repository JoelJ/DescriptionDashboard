package com.attask.descriptiondashboard;

import hudson.model.*;
import hudson.scm.ChangeLogSet;
import hudson.tasks.test.AbstractTestResultAction;
import jenkins.model.Jenkins;

import java.util.*;

/**
 * User: Joel Johnson
 * Date: 9/13/12
 * Time: 4:06 PM
 */
public class ProjectUtils {
	public static int getFailureCount(Run run) {
		if(run.getResult() == Result.SUCCESS || run.getResult() == Result.UNSTABLE) {
			AbstractTestResultAction testResultAction = run.getAction(AbstractTestResultAction.class);
			if(testResultAction != null) {
				return testResultAction.getFailCount();
			}
		}
		return -1;
	}

	public static Map<String, Project> findProjects() {
		return findProjects(Jenkins.getInstance());
	}

	public static Project findProject(String name) {
		return findProjects().get(name);
	}

	protected static Map<String, Project> findProjects(ItemGroup<?> app) {
		Collection<? extends Item> items = app.getItems();
		Map<String, Project> projectMap = new HashMap<String, Project>();
		for (Item project : items) {
			if(project instanceof Project) {
				projectMap.put(project.getName(), (Project) project);
			}
		}
		return projectMap;
	}

	public static Set<String> findCommitters(Run build) {
		Set<String> result = new HashSet<String>();
		if(build instanceof AbstractBuild) {
			for (Object changeObj : ((AbstractBuild)build).getChangeSet()) {
				ChangeLogSet.Entry change = (ChangeLogSet.Entry)changeObj;
				User culprit = change.getAuthor();
				result.add(culprit.getId());
				result.add(culprit.getFullName());
			}
		}

		return Collections.unmodifiableSet(result);
	}


}
