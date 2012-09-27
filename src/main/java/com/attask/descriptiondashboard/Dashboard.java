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
	private String testStatusPattern;
	private int testStatusGroup;
	private int logLinesToSearch;
	private String injectTop;
	private String injectBottom;
	private int maxAge;

	private transient Pattern descriptionPatternRegex;
	private transient Table table;
	private transient long tableCreateTime = -1;
	private transient long cacheTime;
	private transient CustomColumn customColumnCached;
	private transient Pattern testStatusRegex;

	@DataBoundConstructor
	public Dashboard(String name) {
		super(name);
		jobs = createArrayList();
	}

	@Exported
	public Table getTable() {
		return getTable(this.count, Filter.getNull());
	}

	public Table getTable(StaplerRequest request) {
		int count = this.count;
		String requestCount = request.getParameter("count");
		if(requestCount != null) {
			count = Integer.parseInt(requestCount);
		}
		return getTable(count, Filter.fromRequest(request));
	}

	public Table getTable(int count, Filter filter) {
		try {
			if(this.count != count || filter != Filter.getNull()) {
				//don't use the cache and don't update the cache if the request is a custom size or has a custom filter
				Logger.finest("not using table cache. this.count=" + this.count + "; count=" + count + "; filter==null? " + (filter == Filter.getNull()));
				return generateTable(count, filter, this.jobs);
			}

			Date startTime = new Date();
			long time = startTime.getTime();
			if(table == null || tableCreateTime < 0 || cacheTime <= 0 || tableCreateTime + (cacheTime*1000) <= time) {
				Logger.finest("table cache expired. Rebuilding. cacheTime="+cacheTime + "seconds; time=" + time);
				tableCreateTime = time;
				table = generateTable(count, filter, this.jobs);
			} else {
				Logger.finest("Using table cache. cacheTime="+cacheTime + "; time=" + time);
			}
			return table;
		} catch(Exception e) {
			Logger.warn("There was an error while creating the table", e);
			return ErrorTable.createErrorTable("There was an error while creating the table", e);
		}
	}

	private Table generateTable(int count, Filter filter, List<Header> jobs) {
		if(this.testStatusPattern != null && !this.testStatusPattern.isEmpty()) {
			if(this.testStatusRegex == null) {
				this.testStatusRegex = Pattern.compile(this.testStatusPattern);
				Logger.finest("re-compiling testStatusPattern");
			}
		} else {
			this.testStatusRegex = null;
		}

		long start = new Date().getTime();
		Map<String, Map<String, Cell>> cellMap = generateCellMap(count + 10, filter, this.testStatusRegex, this.testStatusGroup, this.logLinesToSearch); // Add 10 to help prevent the bottom from being jagged
		long total = new Date().getTime() - start;
		if(total > 1000) {
			Logger.error("generateCellMap took " + total + " ms");
		}

		start = new Date().getTime();
		Table fromCellMap = Table.createFromCellMap(count, jobs, cellMap, this.createCustomColumn());
		total = new Date().getTime() - start;
		if(total > 1000) {
			Logger.error("createFromCellMap took " + total + "ms");
		}

		return fromCellMap;
	}

	@WebMethod(name = "json")
	public void doJson(StaplerRequest request, StaplerResponse response) throws IOException {
		int count = this.count;
		if(request.getParameterMap().containsKey("count")) {
			String countParam = request.getParameter("count");
			Logger.finest("parameter 'count' provided: " + countParam);
			count = Integer.parseInt(countParam);
		}

		Filter filter = Filter.fromRequest(request);

		response.setContentType("application/json");
		ServletOutputStream outputStream = response.getOutputStream();
		try {
			Table table = getTable(count, filter);
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
			Logger.error("unexpected exception when generating json", e);
		} finally {
			outputStream.flush();
			outputStream.close();
		}
	}

	private Map<String, Map<String, Cell>> generateCellMap(int count, Filter filter, Pattern testStatusRegex, int testStatusGroup, int logLinesToSearch) {
		assert count > 0 : "Must request more than 0 rows";
		assert filter != null : "Filter must not be null";
		assert testStatusGroup >= 0 : "testStatusGroup should be greater than or equal to 0";
		assert logLinesToSearch >= 0 : "logLinesToSearch should be greater than or equal to 0";

		if(descriptionPatternRegex == null) {
			Logger.info("descriptionPatternRegex not compiled");
			descriptionPatternRegex = Pattern.compile(descriptionPattern);
		}

		Map<String, Map<String, Cell>> cellMap = new HashMap<String, Map<String, Cell>>();
		Map<String, Project> projects = ProjectUtils.findProjects();
		for (Header jobHeader : jobs) {
			long start = new Date().getTime();

			int foundCount = 0;
			String jobName = jobHeader.getName();
			Project project = projects.get(jobName);

			int iterated = 0;
			for(int i = project.getNextBuildNumber() - 1; i >= 0; i--) {
				Run currentBuild = project.getBuildByNumber(i);
				if(currentBuild == null) {
					continue;
				}

				iterated++;

				long startBuild = new Date().getTime();

				String description = currentBuild.getDescription();
				if(description != null) {
					String rowID = findMatch(descriptionPatternRegex, description, descriptionPatternGroup);
					if(rowID != null) {
						long startCell = new Date().getTime();

						Cell cell = Cell.createFromBuild(currentBuild, jobHeader.getVisible(), testStatusRegex, testStatusGroup, logLinesToSearch, maxAge);

						long totalCell = new Date().getTime() - startCell;
						if(totalCell > 1000) {
							Logger.error("finished generating cell for " + currentBuild.getFullDisplayName() + " in " + totalCell + "ms.");
						}

						if(filter.matches(cell)) {
							if(!cellMap.containsKey(rowID)) {
								cellMap.put(rowID, new HashMap<String, Cell>());
							}
							Map<String, Cell> cells = cellMap.get(rowID);
							Cell oldCell = cells.get(jobName);
							if(oldCell == null || oldCell.getDate().before(cell.getDate())) {
								cells.put(jobName, cell);
							}
							foundCount++;
						}
					}
				}

				long totalBuild = new Date().getTime() - startBuild;
				if(totalBuild > 1000) {
					Logger.error("finished " + currentBuild.getFullDisplayName() + " in " + totalBuild + "ms.");
				}

				if(foundCount >= count) {
					break;
				}
			}

			long total = new Date().getTime() - start;
			if(total > 1000) {
				Logger.error("finished " + jobHeader.getName() + " in " + total + "ms. Iterated: " + iterated);
			}
		}
		return cellMap;
	}

	private String findMatch(Pattern pattern, String text, int group) {
		Matcher matcher = pattern.matcher(text);
		if(matcher.find()) {
			return matcher.group(group);
		}
		return null;
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

		this.testStatusPattern = request.getParameter("_.testStatusPattern");
		if(this.testStatusPattern != null && !this.testStatusPattern.isEmpty()) {
			if(this.testStatusRegex == null) {
				Logger.finest("Precompiling testStatusRegex.");
				this.testStatusRegex = Pattern.compile(this.testStatusPattern);
			}
		} else {
			Logger.finest("No testStatusRegex provided.");
			this.testStatusRegex = null;
		}

		String testStatusGroup = request.getParameter("_.testStatusGroup");
		if(testStatusGroup == null || testStatusGroup.isEmpty()) {
			Logger.finest("defaulting testStatusGroup to 0.");
			this.testStatusGroup = 0;
		} else {
			this.testStatusGroup = Integer.parseInt(testStatusGroup);
			if(this.testStatusGroup < 0) {
				Logger.finest("Negative number provided. defaulting testStatusGroup to 0.");
				this.testStatusGroup = 0;
			}
		}

		String logLinesToSearch = request.getParameter("_.logLinesToSearch");
		if(logLinesToSearch == null || logLinesToSearch.isEmpty()) {
			Logger.finest("defaulting logLinesToSearch to 100");
			this.logLinesToSearch = 100;
		} else {
			this.logLinesToSearch = Integer.parseInt(logLinesToSearch);
			if(this.logLinesToSearch <= 1) {
				Logger.finest("non-positive number provided. Defaulting logLinesToSearch to 1");
				this.logLinesToSearch = 1;
			}
		}

		this.injectTop = request.getParameter("_.injectTop");
		this.injectBottom = request.getParameter("_.injectBottom");

		String maxAge = request.getParameter("_.maxAge");
		if(maxAge != null && !maxAge.isEmpty()) {
			this.maxAge = Integer.parseInt(maxAge);
		} else {
			Logger.finest("defaulting maxAge to 0");
			this.maxAge = 0;
		}

		//invalidate cached table
		table = null;
		tableCreateTime = -1;

		String cacheTime = request.getParameter("_.cacheTime");
		if(cacheTime == null || cacheTime.isEmpty()) {
			Logger.finest("defaulting cacheTime to 0");
			this.cacheTime = 0;
		} else {
			this.cacheTime = Integer.parseInt(cacheTime);
		}
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
			if(imageUrl != null && !imageUrl.trim().isEmpty()) {
				users.add(new SimpleUser(user.getId(), user.getFullName(), imageUrl.trim()));
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

	public String getInjectTop() {
		return injectTop;
	}

	public String getInjectBottom() {
		return injectBottom;
	}

	public long getCacheTime() {
		return cacheTime;
	}

	@SuppressWarnings("UnusedDeclaration")
	public CustomColumn createCustomColumn()  {
		if(customColumnCached != null) {
			Logger.finest("using cached custom column");
			return customColumnCached;
		}

		if (getCustomColumn() == null || getCustomColumn().isEmpty()) {
			Logger.finest("no custom column defined");
			return null;
		}

		try {
			@SuppressWarnings("unchecked")
			Class<? extends CustomColumn> customColumnClass = (Class<? extends CustomColumn>) Class.forName(getCustomColumn());
			CustomColumn customColumn = customColumnClass.newInstance();
			this.customColumnCached = customColumn;
			return customColumn;
		} catch (ClassNotFoundException e) {
			Logger.error("Exception was thrown while creating custom column", e);
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			Logger.error("Exception was thrown while creating custom column", e);
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			Logger.error("Exception was thrown while creating custom column", e);
			throw new RuntimeException(e);
		}

	}

	@Exported
	public int getOrbSize() {
		return orbSize;
	}

	@Exported
	public String getTestStatusPattern() {
		return testStatusPattern;
	}

	@Exported
	public int getTestStatusGroup() {
		return testStatusGroup;
	}

	@Exported
	public int getLogLinesToSearch() {
		return logLinesToSearch;
	}

	@Exported
	public int getMaxAge() {
		return maxAge;
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
