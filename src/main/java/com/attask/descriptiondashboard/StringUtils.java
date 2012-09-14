package com.attask.descriptiondashboard;

import java.util.Collection;
import java.util.List;

/**
 * User: Joel Johnson
 * Date: 9/7/12
 * Time: 9:02 PM
 */
public class StringUtils {
	public static <T> String join(Collection<T> objects, String join) {
		if(objects == null || objects.size() <= 0) {
			return "";
		}
		if(objects.size() == 1) {
			return objects.iterator().next().toString();
		}

		StringBuilder sb = new StringBuilder();

		for (Object job : objects) {
			sb.append(job).append(join);
		}

		return sb.substring(0, sb.length() - objects.size() + 1);
	}
}
