package com.attask.descriptiondashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A rule that defines a match to
 * User: Joel Johnson
 * Date: 10/3/12
 * Time: 10:55 PM
 */
public abstract class RuleMatcher implements Serializable {
	private final String pattern;
	private final char wildcard;

	public static RuleMatcher createRuleMatcher(String pattern, char wildcard) {
		return new RegexRuleMatcher(pattern, wildcard);
	}

	/**
	 * Checks the given String to see if it matches any of the given RuleMatcher instances.
	 * @param rules RuleMatcher to check.
	 * @param toCheck The String to check.
	 * @return True if any matches are found. Otherwise false.
	 */
	public static boolean matches(Collection<RuleMatcher> rules, String toCheck) {
		if(rules == null) {
			throw new IllegalArgumentException("rules cannot be null");
		}
		if(toCheck == null) {
			throw new IllegalArgumentException("toCheck cannot be null");
		}

		for (RuleMatcher rule : rules) {
			if(rule.matches(toCheck)) {
				return true;
			}
		}

		return false;
	}

	RuleMatcher(String pattern, char wildcard) {
		if(pattern == null) {
			throw new IllegalArgumentException("pattern cannot be null");
		}
		this.pattern = pattern;
		this.wildcard = wildcard;
	}

	public String getPattern() {
		return pattern;
	}

	public char getWildcard() {
		return wildcard;
	}

	/**
	 * Checks if this RuleMatcher matches the given string using the pattern and wildcard specified on creation.
	 * @param toCheck The String to match against.
	 * @return Returns true if the given String matches.
	 */
	public abstract boolean matches(String toCheck);

	private static class RegexRuleMatcher extends RuleMatcher {
		private transient Pattern precompiledPattern;

		public RegexRuleMatcher(String pattern, char wildcard) {
			super(pattern, wildcard);
			getPrecompiledPattern(); //Pre-compile now.
		}

		private Pattern getPrecompiledPattern() {
			//in case this object was deserialized rather than constructed.
			if(this.precompiledPattern == null) {
				this.precompiledPattern = Pattern.compile(createRegex(getPattern(), getWildcard()));
			}
			return this.precompiledPattern;
		}

		private String createRegex(String pattern, char wildcard) {
			return ".*?\\Q" + pattern.replace(String.valueOf(wildcard), "\\E.*?\\Q") + "\\E.*?";
		}

		public boolean matches(String toCheck) {
			Matcher matcher = getPrecompiledPattern().matcher(toCheck);
			return matcher.find();
		}
	}
}