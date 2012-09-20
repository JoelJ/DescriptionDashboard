package com.attask.descriptiondashboard;

import org.kohsuke.stapler.StaplerRequest;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Joel Johnson
 * Date: 9/20/12
 * Time: 3:26 PM
 */
public class Filter {
	private static final Filter NULL_FILTER = new Filter(null);
	private final Map<String, String> filterMap;

	public Filter(Map<String, String> filterMap) {
		this.filterMap = filterMap;
	}

	public static Filter fromRequest(StaplerRequest request) {
		Enumeration parameterNames = request.getParameterNames();
		Map<String, String> filterMap = new HashMap<String, String>();
		while(parameterNames.hasMoreElements()) {
			Object o = parameterNames.nextElement();
			if(o instanceof String && ((String) o).startsWith("filter")) {
				String key = (String) o;
				filterMap.put(key.substring("filter".length()), request.getParameter(key));
			}
		}

		if(filterMap.isEmpty()) {
			return getNull();
		}

		return new Filter(filterMap);
	}

	public static Filter getNull() {
		return NULL_FILTER;
	}

	public boolean matches(Cell cell) {
		if(this == getNull()) {
			return true;
		}

		if(filterMap != null) {
			String userId = filterMap.get("UserId");
			String userName = filterMap.get("UserName");
			for (SimpleUser committer : cell.getCommitters()) {
				if(userId != null && userId.equals(committer.getId())) {
					return true;
				}

				if(userName != null && committer.getName() != null && committer.getName().toLowerCase().contains(userName.toLowerCase())) {
//					System.out.println(committer.getName() + " contains " + userName);
					return true;
				}
			}
		}
		return false;
	}
}
