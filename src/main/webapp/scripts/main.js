var DescriptionDashboard = {
	init: function() {
		DescriptionDashboard.hackGreenRow();

		if(window.location.search.indexOf('disableAutoRefresh=true') == -1) {
			DescriptionDashboard.refreshHandle = setTimeout(DescriptionDashboard.enableAutoRefresh, 10000);
		}

		Event.observe(window, 'hashchange', DescriptionDashboard.onHashChange);

		if(DescriptionDashboard.toggleRowSelect(document.URL, true)) {
			window.scrollBy(0,-50);
		}

		$$('.row').each(function(it) {
			it.observe('click', DescriptionDashboard.onRowClick);
		});
	},

	forceRefresh: function() {
		DescriptionDashboard.killRefresh();
		DescriptionDashboard.enableAutoRefresh();
	},

	killRefresh: function() {
		if(DescriptionDashboard.refreshHandle) {
			clearTimeout(DescriptionDashboard.refreshHandle);
		}
	},

	enableAutoRefresh: function() {
		var currentlyExpanded = $$('.row.expanded');
		var id = '';
		if(currentlyExpanded.length > 0) {
			id = currentlyExpanded[0].id;
		}

		var url = window.location.pathname + '/tableOnly?id='+id;
		new Ajax.Request(url, {
			method: 'get',
			evalJS: 'false',
			onSuccess: DescriptionDashboard.onUpdateSuccess,
			onError: DescriptionDashboard.onUpdateError
		});
	},

	onUpdateSuccess: function(transport) {
		var resultBody = transport.responseText;
		$('DescriptionDashboard').innerHTML = resultBody;

		$$('.row').each(function(it) {
			it.observe('click', DescriptionDashboard.onRowClick);
		});
		DescriptionDashboard.hackGreenRow();

		DescriptionDashboard.toggleRowSelect(document.URL, true);
		DescriptionDashboard.refreshHandle = setTimeout(DescriptionDashboard.enableAutoRefresh, 10000);
	},

	onUpdateError: function(transport) {
		console.log(transport);
		setTimeout(DescriptionDashboard.enableAutoRefresh, 10000);
	},

	onHashChange: function(event) {
		var newUrl = event.newURL;
		var oldUrl = event.oldURL;
		DescriptionDashboard.toggleRowSelect(oldUrl, false);
		if(DescriptionDashboard.toggleRowSelect(newUrl, true)) {
			window.scrollBy(0,-25);
		}
	},

	toggleRowSelect: function(url, doSelect) {
		var result = false;
		var split = url.split('#', 2);
		if(split.length >= 2) {
			var hash = split[1];
			if(hash.startsWith('row-')) {
				var row = $(hash);
				var toggle = row.removeClassName;
				if(doSelect) {
					row.addClassName('selected');
				} else {
					row.removeClassName('selected');
				}
				result = true;
			}
		}
		return result;
	},

	onRowClick: function(event) {
		var row = event.target.up('.row');
		var rowId = row.id;
		var extraId = rowId.replace("row-", "extra-");
		var extra = $(extraId);
		if(extra == null) {
			return;
		}

		var currentlyExpanded = $$('.row.expanded');

		var url = window.location.pathname + "/rowExtras?id="+rowId;
		new Ajax.Request(url, {
			method: 'get',
			evalJS: 'false',
			onSuccess: function(transport) {
				var resultBody = transport.responseText;
				extra.down('td').innerHTML = resultBody;
			},
			onError: function(transport) {
				extra.down('td').innerHTML = "An error occurred.";
				console.log(transport);
			}
		});

		if(row.hasClassName('expanded')) {
			row.removeClassName('expanded');
			extra.addClassName('hidden');
		} else {
			row.addClassName('expanded');
			extra.removeClassName('hidden');
		}

		currentlyExpanded.each(function(it) {
			var itExtraId = it.id.replace("row-", "extra-");
			var itExtra = $(itExtraId);
			it.removeClassName('expanded');
			itExtra.addClassName('hidden');
		});
	},

	hackGreenRow:function () {
		var lastSuccessfulRow = $$('.hasLastSuccess tr:nth-child(2)');
		if (lastSuccessfulRow.length > 0) {
			lastSuccessfulRow[0].id = "";
			lastSuccessfulRow[0].removeClassName('expanded');

			var lastSuccessfulRowExtra = $$('.hasLastSuccess tr:nth-child(3)');
			lastSuccessfulRowExtra[0].id = "";
		}
	}
};