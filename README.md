View plugin for Jenkins that sorts the builds of jobs into a table. Each row being bound by the description.

The plugin includes a pre-build step that allows you to dynamically set the description of the build.

An example usecase is automatically set the description of the build to be the SCM revision.
Then all the builds with the same build number are grouped together.
Giving you a view of the entire pipeline and it's history on one page. 

Screenshot:
<a href="http://imgur.com/lYslonG"><img src="http://i.imgur.com/lYslonG.png" /></a>

To use:

* Add the view and give it a name like any view.
* 'Jobs (comma separated)': put in the jobs you want displayed on the dashboard.
* 'Description Pattern': put in a regex to use to determine which part of the description should be used.
** For example, if your description is "Commit: <COMMIT NUMBER>", your regular expression would be: "Commit: (\d+)"
* 'Description Pattern Group': The regular expression group used to grab the value. In the above example, this value would be '1' and then the rows will be grouped ONLY by the number. If this value were zero, the values would be grouped by the entire string.
* 'Category Pattern Groups': These are optional additional groups, similar to above but are used for filtering. So in your description you can also have the Branch run and a dropdown will appear of all the unique branches allowing you to filter.
* 'Test Status Pattern': Used to find live updates of the tests while a build is still running. This value is optional.
* 'Test Status Group': Used with the above pattern. Determines which group contains the number of failures to display.
** For example, if your test suite prints out "Pass: <#> Fail: <#>" every time a test finishes. You can set the pattern to be: "Fail: (\d+)" and the group to be "1". The group must match an integer value.
* '# Log lines to check': This is the max number of log lines to look for the above test pattern failure.
* 'Cache Table Time (seconds)': The table is generated once and kept for this amount of time. This is nice if your Jenkins instance has a high number of requests to the dashboard a second. This way it doesn't have to re-iterate all the builds.
* 'Custom Column Class': This is for installed extensions for the Dashboard. The Dashboard comes bundled with an example Code Review plugin.

