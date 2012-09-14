package com.attask.descriptiondashboard;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;
import java.util.*;

/**
 * User: Joel Johnson
 * Date: 9/7/12
 * Time: 8:22 PM
 */
@ExportedBean
public class Table implements Serializable {
	private final List<Header> headers;
	private final List<Row> rows;

	public static Table createFromCellMap(int maxRowCount, List<Header> jobs, Map<String, Map<String, Cell>> cellMap) {
		List<Row> rows = new ArrayList<Row>();
		for (Map.Entry<String, Map<String, Cell>> entry : cellMap.entrySet()) {
			String rowID = entry.getKey();
			Map<String, Cell> builds = entry.getValue();
			Cell first = builds.values().iterator().next();
			String description = first.getDescription();
			Row row = new Row(rowID, description, builds, jobs);
			rows.add(row);
		}

		Collections.sort(rows, new Comparator<Row>() {
			public int compare(Row row1, Row row2) {
				if (row1 == null) {
					if (row2 == null) {
						return 0;
					}
					return -1;
				}
				if (row2 == null) {
					return 1;
				}
				return row2.findDate().compareTo(row1.findDate());
			}
		});
		if(rows.size() > maxRowCount) {
			rows = rows.subList(0, maxRowCount);
		}

		return new Table(jobs, rows);
	}

	private Table(List<Header> headers, List<Row> rows) {
		this.headers = Collections.unmodifiableList(headers);
		this.rows = Collections.unmodifiableList(rows);
	}

	@Exported
	public List<Row> getRows() {
		return rows;
	}

	@Exported
	public List<Header> getHeaders() {
		return headers;
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
