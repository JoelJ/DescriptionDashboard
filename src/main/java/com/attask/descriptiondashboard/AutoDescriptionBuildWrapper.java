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

	@DataBoundConstructor
	public AutoDescriptionBuildWrapper(boolean useUpstreamDescription, String description) {
		this.useUpstreamDescription = useUpstreamDescription;
		this.description = description;
	}

	@Override
	public void preCheckout(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		if(this.useUpstreamDescription) {
			@SuppressWarnings("unchecked")
			Cause.UpstreamCause cause = (Cause.UpstreamCause) build.getCause(Cause.UpstreamCause.class);
			if(cause != null) {
				Run upstreamBuild = findUpstreamBuild(cause);
				String description = upstreamBuild == null ? "{ no upstream build }" : upstreamBuild.getDescription();
				build.setDescription(description);
			}
		} else {
			EnvVars environment = build.getEnvironment(listener);
			String expanded = environment.expand(description);
			build.setDescription(expanded);
		}
	}

	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		return new Environment() {
			@Override
			public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
				return true;
			}
		};
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
