package com.attask.descriptiondashboard;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.Util;
import hudson.model.*;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Joel Johnson
 * Date: 9/7/12
 * Time: 8:16 PM
 */
@ExportedBean
public class Dashboard extends View {
	private List<Header> jobs;
	private int count;
	private String descriptionPattern;
	private int descriptionPatternGroup;
	private int orbSize;
	private String customColumn;

	private transient Pattern descriptionPatternRegex;
	private transient Table table;
	private transient long tableCreateTime = -1;
	private transient static final long CACHE_TIME = 5000; // 5 seconds
	private transient CustomColumn customColumnCached;

	@DataBoundConstructor
	public Dashboard(String name) {
		super(name);
		jobs = createArrayList();
	}

	@Exported
	public Table getTable() {
		return getTable(this.count);
	}

	public Table getTable(int count) {
		if(this.count != count) {
			//don't use the cache and don't update the cache if the request is a custom size
			return generateTable(count, this.jobs);
		}

		Date startTime = new Date();
		long time = startTime.getTime();
		if(table == null || tableCreateTime < 0 || tableCreateTime + CACHE_TIME <= time) {
			tableCreateTime = time;
			table = generateTable(count, this.jobs);
		}
		return table;
	}

	private Table generateTable(int count, List<Header> jobs) {
		Map<String, Map<String, Cell>> cellMap = generateCellMap(count + 10); // Add 10 to help prevent the bottom from being jagged
		return Table.createFromCellMap(count, jobs, cellMap, this.createCustomColumn());
	}

	@WebMethod(name = "json")
	public void doJson(StaplerRequest request, StaplerResponse response) throws IOException {
		int count = this.count;
		if(request.getParameterMap().containsKey("count")) {
			count = Integer.parseInt(request.getParameter("count"));
		}

		response.setContentType("application/json");
		ServletOutputStream outputStream = response.getOutputStream();
		try {
			Table table = getTable(count);
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.setIgnoreTransientFields(true);
			jsonConfig.setCycleDetectionStrategy(new CycleDetectionStrategy() {
				@Override
				public JSONArray handleRepeatedReferenceAsArray(Object reference) {
					return null;
				}

				@Override
				public JSONObject handleRepeatedReferenceAsObject(Object reference) {
					return null;
				}
			});
			JSONObject jsonObject = JSONObject.fromObject(table, jsonConfig);
			outputStream.print(jsonObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			outputStream.flush();
			outputStream.close();
		}
	}

	private Map<String, Map<String, Cell>> generateCellMap(int count) {
		Map<String, Map<String, Cell>> cellMap = new HashMap<String, Map<String, Cell>>();
		Map<String, Project> projects = ProjectUtils.findProjects();
		int i = 0;
		for (Header jobHeader : jobs) {
			String jobName = jobHeader.getName();
			Project project = projects.get(jobName);
			Run currentBuild = project.getLastBuild();
			while(currentBuild != null) {
				String description = currentBuild.getDescription();
				if(description != null) {
					if(descriptionPatternRegex == null) {
						descriptionPatternRegex = Pattern.compile(descriptionPattern);
					}
					Matcher matcher = descriptionPatternRegex.matcher(description);
					if(matcher.find()) {
						String rowID = matcher.group(descriptionPatternGroup);
						Cell cell = Cell.createFromBuild(currentBuild, jobHeader.getVisible());
						if(!cellMap.containsKey(rowID)) {
							cellMap.put(rowID, new HashMap<String, Cell>());
						}
						Map<String, Cell> cells = cellMap.get(rowID);
						Cell oldCell = cells.get(jobName);
						if(oldCell == null || oldCell.getDate().before(cell.getDate())) {
							cells.put(jobName, cell);
						}
						i++;
					}
				}

				currentBuild = currentBuild.getPreviousBuild();

				if(i >= count) {
					break;
				}
			}
		}
		return cellMap;
	}

	@Override
	protected void submit(StaplerRequest request) throws IOException, ServletException, Descriptor.FormException {
		String jobs = request.getParameter("_.jobs");
		this.jobs = createArrayList();

		for (String job : Arrays.asList(jobs.split(","))) {
			Header header = Header.parseFromRequest(job, request);
			this.jobs.add(header);
		}

		this.count = Integer.parseInt(request.getParameter("_.count"));
		this.descriptionPattern = request.getParameter("_.descriptionPattern");
		this.descriptionPatternRegex = Pattern.compile(this.descriptionPattern);
		this.descriptionPatternGroup = Integer.parseInt(request.getParameter("_.descriptionPatternGroup"));
		this.orbSize = Integer.parseInt(request.getParameter("_.orbSize"));

		this.customColumn = request.getParameter("_.customColumn");
		this.customColumnCached = null;

		//invalidate cached table
		table = null;
		tableCreateTime = -1;
	}

	@SuppressWarnings("UnusedDeclaration")
	public String findUserName() {
		User current = User.current();
		if(current != null) {
			String s = current.getFullName().replaceAll("\\s+", "");
			s = Util.escape(s);
			return s;
		}
		return "anonymous";
	}

	@SuppressWarnings("UnusedDeclaration")
	public String findUserId() {
		User current = User.current();
		if(current != null) {
			return current.getId();
		}
		return "anonymous";
	}

	public Set<SimpleUser> findUsersWithCustomImages() {
		Set<SimpleUser> users = new HashSet<SimpleUser>();
		for (User user : User.getAll()) {
			String imageUrl = CustomGreenUserProperty.getImgUrl(user);
			if(imageUrl != null) {
				users.add(new SimpleUser(user.getId(), user.getFullName(), imageUrl));
			}
		}
		return users;
	}

	@Exported
	public List<Header> getJobs() {
		return jobs;
	}

	@Exported
	public int getCount() {
		return count;
	}

	@Exported
	public String getDescriptionPattern() {
		return descriptionPattern;
	}

	@Exported
	public int getDescriptionPatternGroup() {
		return descriptionPatternGroup;
	}

	@Exported
	public String getCustomColumn() {
		return customColumn;
	}

	@SuppressWarnings("UnusedDeclaration")
	public CustomColumn createCustomColumn()  {
		if(customColumnCached != null) {
			return customColumnCached;
		}

		if (getCustomColumn() == null || getCustomColumn().isEmpty()) {
			return null;
		}

		try {
			@SuppressWarnings("unchecked")
			Class<? extends CustomColumn> customColumnClass = (Class<? extends CustomColumn>) Class.forName(getCustomColumn());
			CustomColumn customColumn = customColumnClass.newInstance();
			this.customColumnCached = customColumn;
			return customColumn;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

	}

	@Exported
	public int getOrbSize() {
		return orbSize;
	}

	@SuppressWarnings("UnusedDeclaration")
	public ExtensionList<CustomColumn> allCustomColumns() {
		return CustomColumn.all();
	}

	@Override
	public Collection<TopLevelItem> getItems() {
		return Collections.emptyList();
	}

	@Override
	public boolean contains(TopLevelItem item) {
		return false;
	}

	@Override
	public void onJobRenamed(Item item, String oldName, String newName) {
	}

	@Override
	public Item doCreateItem(StaplerRequest request, StaplerResponse response) throws IOException, ServletException {
		return Jenkins.getInstance().doCreateItem(request, response);
	}

	private ArrayList<Header> createArrayList() {
		return new ArrayList<Header>() {
			@Override
			public String toString() {
				return Util.join(this, ",");
			}
		};
	}

	@Extension
	public static final class DescriptorImpl extends ViewDescriptor {
		@Override
		public String getDisplayName() {
			return "Description Dashboard";
		}
	}
}
