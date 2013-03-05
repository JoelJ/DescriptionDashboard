package com.attask.descriptiondashboard;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Joel Johnson
 * Date: 9/7/12
 * Time: 8:22 PM
 */
@ExportedBean
public class Table implements Serializable {
	private final List<Header> headers;
	private final List<Row> rows;
	private final Set<String> allBranches;
	private transient final CustomColumn customColumn;

	public static Table createFromCellMap(int maxRowCount, List<Header> jobs, Map<String, Map<String, Cell>> cellMap, CustomColumn customColumn, Collection<Rule> rules, Pattern descriptionPattern, String branchGroups) {
		List<Row> rows = new ArrayList<Row>();
		Set<String> allBranches = new TreeSet<String>();

		for (Map.Entry<String, Map<String, Cell>> entry : cellMap.entrySet()) {
			String rowID = entry.getKey();
			Map<String, Cell> builds = entry.getValue();
			Cell first = builds.values().iterator().next();
			String description = first.getDescription();

			Set<String> extraClasses = new HashSet<String>();
			findAllBranches(descriptionPattern, branchGroups, description, allBranches, extraClasses);

			Row row = new Row(rowID, description, builds, jobs, customColumn, rules, extraClasses);
			rows.add(row);
		}

		Collections.sort(rows);
		if(rows.size() > maxRowCount) {
			Logger.finer("rows.size() > maxRowCount... truncating. rows.size()="+rows.size() + "; maxRowCount="+maxRowCount);
			rows = rows.subList(0, maxRowCount);
		}

		return new Table(jobs, rows, customColumn, allBranches);
	}

	private static void findAllBranches(Pattern descriptionPattern, String branchGroups, String description, Set<String> allBranches, Set<String> extraClasses) {
		if(descriptionPattern != null && branchGroups != null && !branchGroups.isEmpty()) {
			String[] split = branchGroups.split("\\s+");
			List<Integer> branchGroupInts = new LinkedList<Integer>();
			for (String group : split) {
				branchGroupInts.add(Integer.parseInt(group));
			}
			Matcher matcher = descriptionPattern.matcher(description);
			while(matcher.find()) {
				for (int group : branchGroupInts) {
					String classValue = matcher.group(group);
					extraClasses.add(classValue);
					allBranches.add(classValue);
				}
			}
		}
	}

	protected Table(List<Header> headers, List<Row> rows, CustomColumn customColumn, Set<String> allBranches) {
		this.headers = Collections.unmodifiableList(headers);
		this.rows = Collections.unmodifiableList(rows);
		this.customColumn = customColumn;
		this.allBranches = allBranches;
	}

	@Exported
	public List<Row> getRows() {
		return rows;
	}

	@Exported
	public List<Header> getHeaders() {
		return headers;
	}

	@Exported
	public Set<String> getAllBranches() {
		return allBranches;
	}

	public CustomColumn getCustomColumn() {
		return customColumn;
	}

	public Row findPassedRow() {
		for (Row row : rows) {
			if(row.getPassed()) {
				return row;
			}
		}
		return null;
	}
}
