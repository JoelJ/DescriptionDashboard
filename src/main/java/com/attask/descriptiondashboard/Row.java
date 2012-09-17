package com.attask.descriptiondashboard;

import hudson.Util;
import hudson.model.Project;
import hudson.model.Run;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: Joel Johnson
 * Date: 9/7/12
 * Time: 8:22 PM
 */
@ExportedBean
public class Row implements Serializable {
	private final String id;
	private final String description;
	private final Map<String, Cell> cells;
	private final transient List<Header> headers;

	public Row(String id, String description, Map<String, Cell> cells, List<Header> headers) {
		if(id == null) {
			throw new IllegalArgumentException("id cannot be null");
		}
		if(description == null) {
			throw new IllegalArgumentException("description cannot be null");
		}
		if (cells == null || cells.isEmpty()) {
			throw new IllegalArgumentException("cells cannot be null or empty");
		}
		if (headers == null || headers.isEmpty()) {
			throw new IllegalArgumentException("headers cannot be null or empty");
		}

		this.id = id;
		this.description = description;
		this.cells = cells;
		this.headers = headers;
	}

	@Exported
	public String getId() {
		return id;
	}

	@Exported
	public String getDescription() {
		return description;
	}

	@Exported
	public Map<String, Cell> getCells() {
		return cells;
	}

	public Cell getCell(String name) {
		if (cells.containsKey(name)) {
			return cells.get(name);
		}
		return null;
	}

	public Date findDate() {
		for (Header header : headers) {
			Cell cell = cells.get(header.getName());
			if (cell != null) {
				return cell.getDate();
			}
		}
		return new Date(0);
	}

	public String findDateFormatted() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm z", Locale.US);
		String format = simpleDateFormat.format(findDate());
		return format;
	}

	public String findDatePretty() {
		long duration = new GregorianCalendar().getTimeInMillis()-findDate().getTime();
		return Util.getPastTimeString(duration);
	}

	public List<Header> getHeaders() {
		return headers;
	}

	public boolean getPassed() {
		for (Header header : headers) {
			Cell cell = cells.get(header.getName());
			if(cell == null) {
				if(header.getRequirement() != Header.Requirement.NotRequired && header.getRequirement() != Header.Requirement.RequiredWhenPresent) {
					return false;
				}
			} else {
				String result = cell.getResult();
				if("SUCCESS".equals(result)) {
					continue;
				}

				if(header.getRequirement() != Header.Requirement.NotRequired) {
					return false;
				}
			}
		}
		return true;
	}

	public int findNumberCommitters() {
		Iterator<Header> iterator = headers.iterator();
		if(iterator.hasNext()) {
			Cell cell = cells.get(iterator.next().getName());
			return cell.getNumberCommitters();
		}
		return 0;
	}

	/**
	 * Used from Row/render.jelly. Added to the class so we can style rows based on committer.
	 * @return HTML-safe set of the names and IDs of everyone who contributed to all the builds in the entire row.
	 */
	public String findCommitters() {
		Set<String> result = new HashSet<String>();
		for (Cell cell : cells.values()) {
			for (String committer : cell.getCommitters()) {
				String cleanCommitter = committer.replaceAll("\\s+", ""); //no spaces in the class name
				cleanCommitter = Util.escape(cleanCommitter); //no XSS attacks, please
				result.add(cleanCommitter);
			}
		}
		return Util.join(result, " ");
	}

	public List<Change> findChangeSet() {
		List<Change> allChanges = new LinkedList<Change>();
		for (Cell cell : cells.values()) {
			String projectName = cell.getProjectName();
			int buildNumber = cell.getBuildNumber();
			Project project = ProjectUtils.findProject(projectName);
			if(project != null) {
				Run buildByNumber = project.getBuildByNumber(buildNumber);
				if(buildByNumber != null) {
					List<Change> changeSet = ProjectUtils.findChangeSet(buildByNumber);
					allChanges.addAll(changeSet);
				}
			}
		}
		return Collections.unmodifiableList(allChanges);
	}

	public boolean idEquals(String[] ids) {
		if(ids != null) {
			for (String id : ids) {
				if(this.id != null && ("row-" + this.id).equals(id)) {
					return true;
				}
			}
		}
		return false;
	}

	public int findNumberVisibleColumns() {
		int total = 0;
		for (Header header : headers) {
			if(header.getVisible()) {
				total++;
			}
		}
		return total;
	}

	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof Row)) {
			return false;
		}
		Row row = (Row) o;
		return this.getId().equals(row.getId());
	}

	@Override
	public int hashCode() {
		return this.getId() != null ? this.getId().hashCode() : 0;
	}
}
