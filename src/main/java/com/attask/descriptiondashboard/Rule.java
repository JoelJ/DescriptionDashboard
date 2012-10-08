package com.attask.descriptiondashboard;

import hudson.Util;
import hudson.model.Job;
import hudson.model.Project;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

/**
 * User: Joel Johnson
 * Date: 10/3/12
 * Time: 11:22 PM
 */
@ExportedBean
public class Rule implements Serializable {
	private final String name;
	private final String message;
	private final Severity severity;
	private final Collection<RuleMatcher> ruleMatchers;
	private final Collection<Header> headers;

	public Rule(String name, String message, Severity severity, Collection<RuleMatcher> ruleMatchers, Collection<Header> headers) {
		this.name = name;
		this.message = message;
		this.severity = severity;
		this.ruleMatchers = ruleMatchers;
		this.headers = headers;
	}

	public static Collection<Rule> matches(Collection<Rule> rules, Run<?, ?> build) {
		List<Rule> result = new ArrayList<Rule>(rules.size());

		Scanner scanner = null;
		Set<Rule> allRules = new HashSet<Rule>(rules);
		try {
			InputStream logInputStream = build.getLogInputStream();
			scanner = new Scanner(logInputStream);
			while(!allRules.isEmpty() && scanner.hasNextLine()) {
				String logLine = scanner.nextLine();
				Iterator<Rule> rulesIterator = allRules.iterator();
				Set<Rule> toRemove = new HashSet<Rule>();
				while(rulesIterator.hasNext()) {
					Rule rule = rulesIterator.next();
					if(headerMatches(rule.getHeaders(), build.getParent().getName())) {
						if(RuleMatcher.matches(rule.getRuleMatchers(), logLine)) {
							result.add(rule);
							toRemove.add(rule);
							//don't break here. We want to check to see if all rules apply to this line
						}
					}
				}
				allRules.removeAll(toRemove);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if(scanner != null) {
				scanner.close();
			}
		}

		return result;
	}

	private static boolean headerMatches(Collection<Header> headers, String name) {
		for (Header header : headers) {
			if(header.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	@Exported
	public String getName() {
		return name;
	}

	@Exported
	public String getMessage() {
		return message;
	}

	@Exported
	public Severity getSeverity() {
		return severity;
	}

	@Exported
	public Collection<RuleMatcher> getRuleMatchers() {
		return ruleMatchers;
	}

	@Exported
	public Collection<Header> getHeaders() {
		return headers;
	}

	public static List<Rule> createFromRequest(StaplerRequest request, List<Header> allHeaders) {
		List<Rule> listToPopulate = createArrayList();
		if(request != null) {
			String parameter = request.getParameter("_.rules");
			if(parameter != null && !parameter.isEmpty()) {
				String[] rules = parameter.split(",");
				for (String ruleName : rules) {
					String message = request.getParameter("_.rule_"+ruleName+"_message");

					Severity severity = parseSeverity(request, ruleName);
					Collection<RuleMatcher> ruleMatchers = parsePatterns(request, ruleName);
					List<Header> theHeaders = parseHeaders(request, allHeaders, ruleName);
					Rule rule = new Rule(ruleName, message, severity, ruleMatchers, theHeaders);
					listToPopulate.add(rule);
				}
			}
		}
		return listToPopulate;
	}

	private static Collection<RuleMatcher> parsePatterns(StaplerRequest request, String ruleName) {
		Collection<RuleMatcher> ruleMatchers = new ArrayList<RuleMatcher>();
		String patternsParameter = request.getParameter("_.rule_" + ruleName + "_patterns");
		if(patternsParameter != null && !patternsParameter.isEmpty()) {
			String[] split = patternsParameter.split("(\r)?\n");
			for (String pattern : split) {
				ruleMatchers.add(RuleMatcher.createRuleMatcher(pattern, '*'));
			}
		}
		return ruleMatchers;
	}

	public String getPatterns() {
		StringBuilder sb = new StringBuilder();
		for (RuleMatcher ruleMatcher : ruleMatchers) {
			sb.append(ruleMatcher.getPattern()).append("\n");
		}
		return sb.toString().trim();
	}

	private static Severity parseSeverity(StaplerRequest request, String ruleName) {
		Severity severity = Severity.Info;
		String severityParameter = request.getParameter("_.rule_"+ruleName+"_severity");
		if(severityParameter != null && !severityParameter.isEmpty()) {
			severity = Severity.valueOf(severityParameter);
		}
		return severity;
	}

	private static List<Header> parseHeaders(StaplerRequest request, List<Header> allHeaders, String ruleName) {
		List<Header> theHeaders = createArrayList();
		String headerParameter = request.getParameter("_.rule_"+ruleName+"_headers");
		if(headerParameter != null) {
			Set<String> headersToUse = new HashSet<String>(Arrays.asList(headerParameter.split(",")));

			for (Header allHeader : allHeaders) {
				if(headersToUse.contains(allHeader.getName())) {
					theHeaders.add(allHeader);
				}
			}
		}
		return theHeaders;
	}

	public static enum Severity {
		Info, Warning, Error
	}

	private static <T> ArrayList<T> createArrayList() {
		return new ArrayList<T>() {
			@Override
			public String toString() {
				return Util.join(this, ",");
			}
		};
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Rule)) return false;

		Rule rule = (Rule) o;

		if (name != null ? !name.equals(rule.name) : rule.name != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}
}