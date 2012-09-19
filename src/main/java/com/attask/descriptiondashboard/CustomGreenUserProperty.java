package com.attask.descriptiondashboard;

import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * User: Joel Johnson
 * Date: 9/19/12
 * Time: 1:44 PM
 */
public class CustomGreenUserProperty extends UserProperty {
	private final String imgUrl;

	public CustomGreenUserProperty() {
		this("");
	}

	public CustomGreenUserProperty(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public static String getImgUrl(User user) {
		CustomGreenUserProperty property = user.getProperty(CustomGreenUserProperty.class);
		if(property != null) {
			return property.getImgUrl();
		}
		return null;
	}

	@Extension
	public static final class DescriptorImpl extends UserPropertyDescriptor {
		public String getDisplayName() {
			return "Custom Green Images";
		}

		@Override
		public CustomGreenUserProperty newInstance(StaplerRequest req, JSONObject formData) throws hudson.model.Descriptor.FormException {
			if (formData.has("imgUrl")) {
				return new CustomGreenUserProperty((String) formData.get("imgUrl"));
			} else {
				return new CustomGreenUserProperty();
			}
		}

		public CustomGreenUserProperty newInstance(User user) {
			return new CustomGreenUserProperty();
		}
	}
}
