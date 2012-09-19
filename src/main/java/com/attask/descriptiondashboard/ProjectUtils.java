package com.attask.descriptiondashboard;

import hudson.model.*;
import hudson.plugins.git.GitChangeSet;
import hudson.scm.ChangeLogSet;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.test.AbstractTestResultAction;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public static int grepFailureCount(Run run, Pattern testStatusRegex, int testStatusGroup, int lines) {
		List<String> log;
		try {
			//for some reason this gives me a warning in IntelliJ
			//noinspection unchecked
			log = (List<String>)run.getLog(lines);
		} catch (IOException e) {
			return -1;
		}

		for (int i = log.size() - 1; i >= 0; i--) {
			String line = log.get(i);
			Matcher matcher = testStatusRegex.matcher(line);
			if(matcher.find()) {
				String result = matcher.group(testStatusGroup);
				return Integer.parseInt(result);
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

	public static Set<SimpleUser> findCommitters(Run build) {
		Set<SimpleUser> result = new HashSet<SimpleUser>();
		if(build instanceof AbstractBuild) {
			for (Object changeObj : ((AbstractBuild)build).getChangeSet()) {
				ChangeLogSet.Entry change = (ChangeLogSet.Entry)changeObj;
				User culprit = change.getAuthor();
				String imageUrl = CustomGreenUserProperty.getImgUrl(culprit);
				result.add(new SimpleUser(culprit.getId(), culprit.getFullName(), imageUrl));
			}
		}

		return Collections.unmodifiableSet(result);
	}

	public static List<Change> findChangeSet(Run build) {
		List<Change> changes = new LinkedList<Change>();
		if(build instanceof AbstractBuild) {
			for (Object changeObj : ((AbstractBuild)build).getChangeSet()) {
				ChangeLogSet.Entry changeSet = (ChangeLogSet.Entry)changeObj;
				Change change;
				if(changeSet instanceof GitChangeSet) {
					change = Change.createChangeFromGitChangeSet((GitChangeSet) changeSet);
				} else {
					change = Change.createChangeFromChangeSet(changeSet);
				}
				changes.add(change);
			}
		}
		return Collections.unmodifiableList(changes);
	}

	public static boolean hasAgeOver(Run build, int age) {
		if(age <= 0) {
			return false;
		}

		AbstractTestResultAction testResultAction = build.getAction(AbstractTestResultAction.class);
		if(testResultAction == null) {
			return false;
		}


		List<CaseResult> failedTests = testResultAction.getFailedTests();
		if(failedTests == null) {
			return false;
		}

		for (CaseResult failedTest : failedTests) {
			if(failedTest.getAge() >= age) {
				return true;
			}
		}

		return false;
	}
}
