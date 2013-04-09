package com.attask.descriptiondashboard;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;

/**
 * User: Joel Johnson
 * Date: 9/13/12
 * Time: 10:06 PM
 */
public class AutoDescriptionBuildWrapper extends BuildWrapper {
	private final boolean useUpstreamDescription;
	private final String description;
	private final String jobName;
	private final String buildNumber;

	@DataBoundConstructor
	public AutoDescriptionBuildWrapper(boolean useUpstreamDescription, String description, String jobName, String buildNumber) {
		this.useUpstreamDescription = useUpstreamDescription;
		this.description = description;
		this.jobName = jobName;
		this.buildNumber = buildNumber;
	}

	@Override
	public void preCheckout(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		setDescription(build, listener);
	}

	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		setDescription(build, listener);
		return new Environment() {
			@Override
			public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
				return true;
			}
		};
	}

	private void setDescription(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
		EnvVars environment = build.getEnvironment(listener);

		if(getJobName() != null && !getJobName().isEmpty() && getBuildNumber() != null && !getBuildNumber().isEmpty()) {
			String jobNameExpanded = environment.expand(getJobName());
			String buildNumberExpandedStr = environment.expand(getBuildNumber());
			int buildNumberExpanded = Integer.parseInt(buildNumberExpandedStr);
			Run foundBuild = ProjectUtils.findBuild(jobNameExpanded, buildNumberExpanded);
			String description = foundBuild.getDescription();
			build.setDescription(description);
			return;
		}

		if(this.useUpstreamDescription) {
			@SuppressWarnings("unchecked")
			Cause.UpstreamCause cause = (Cause.UpstreamCause) build.getCause(Cause.UpstreamCause.class);
			if(cause != null) {
				Run upstreamBuild = findUpstreamBuild(cause);
				String description = upstreamBuild == null ? "{ no upstream build }" : upstreamBuild.getDescription();
				build.setDescription(description);
				return;
			}
		}

		if(description != null) {
			//If we're not using Upstream Description OR there is no upstream job.
			String expanded = environment.expand(description);
			if(expanded != null && !expanded.isEmpty()) {
				build.setDescription(expanded);
			}
		}
	}

	private Run findUpstreamBuild(Cause.UpstreamCause cause) {
		String upstreamProject = cause.getUpstreamProject();
		int upstreamBuildNumber = cause.getUpstreamBuild();
		AbstractProject project = ProjectUtils.findProject(upstreamProject);
		if(project == null) {
			return null;
		}
		return project.getBuildByNumber(upstreamBuildNumber);
	}

	@Exported
	public boolean isUseUpstreamDescription() {
		return useUpstreamDescription;
	}

	@Exported
	public String getDescription() {
		return description;
	}

	@Exported
	public String getJobName() {
		return jobName;
	}

	@Exported
	public String getBuildNumber() {
		return buildNumber;
	}

	@Extension
	public static class DescriptorImpl extends BuildWrapperDescriptor {
		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Auto set description";
		}
	}
}
