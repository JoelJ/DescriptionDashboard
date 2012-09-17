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

		if(window.location.hash == '#showAll') {
			DescriptionDashboard.showAllColumns();
		}

		$('DescriptionDashboard').observe('click', DescriptionDashboard.onRelayClick);
	},

	onRelayClick: function(e) {
		if(e.target.up('.row')) {
			DescriptionDashboard.onRowClick(e);
		} else if(e.target.hasClassName('showAll')) {
			DescriptionDashboard.onShowAllClicked();
		}
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
		var id = [];
		for(var i = 0; i < currentlyExpanded.length; i++) {
			id.push(currentlyExpanded[i].id);
		}

		var url = window.location.pathname + '/tableOnly';
		new Ajax.Request(url, {
			method: 'get',
			evalJS: 'false',
			parameters: {id: id},
			onSuccess: DescriptionDashboard.onUpdateSuccess,
			onError: DescriptionDashboard.onUpdateError
		});
	},

	onUpdateSuccess: function(transport) {
		var resultBody = transport.responseText;
		$('DescriptionDashboard').innerHTML = resultBody;

		DescriptionDashboard.hackGreenRow();

		DescriptionDashboard.toggleRowSelect(document.URL, true);

		if(window.location.hash == '#showAll') {
			DescriptionDashboard.showAllColumns();
		}

		DescriptionDashboard.refreshHandle = setTimeout(DescriptionDashboard.enableAutoRefresh, 10000);
	},

	onUpdateError: function(transport) {
		console.log(transport);
		setTimeout(DescriptionDashboard.enableAutoRefresh, 10000);
	},

	onHashChange: function(event) {
		var newUrl = event.newURL;
		if(newUrl.endsWith('#showAll')) {
			DescriptionDashboard.showAllColumns();
		} else if(newUrl.endsWith('#hideAll')) {
			DescriptionDashboard.hideAllColumns();
		} else {
			var oldUrl = event.oldURL;
			DescriptionDashboard.toggleRowSelect(oldUrl, false);
			if(DescriptionDashboard.toggleRowSelect(newUrl, true)) {
				window.scrollBy(0,-25);
			}
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
		if(row == null) {
			return;
		}
		var rowId = row.id;

		var extraId = rowId.replace("row-", "extra-");
		var extra = $(extraId);
		if(extra == null) {
			return;
		}

		if(row.hasClassName('expanded')) {
			row.removeClassName('expanded');
			extra.addClassName('hidden');
		} else {
			row.addClassName('expanded');
			extra.removeClassName('hidden');

			var url = window.location.pathname + "/rowExtras?id="+rowId;
			$(document.body).addClassName('loading');
			new Ajax.Request(url, {
				method: 'get',
				evalJS: 'false',
				onSuccess: function(transport) {
					var resultBody = transport.responseText;
					extra.down('td').innerHTML = resultBody;
					$(document.body).removeClassName('loading');
				},
				onError: function(transport) {
					extra.down('td').innerHTML = "An error occurred.";
					$(document.body).removeClassName('loading');
					console.log(transport);
				}
			});
		}
	},

	hackGreenRow: function () {
		var lastSuccessfulRow = $$('.hasLastSuccess tr:nth-child(2)');
		if (lastSuccessfulRow.length > 0) {
			lastSuccessfulRow[0].id = "";
			lastSuccessfulRow[0].removeClassName('expanded');

			var lastSuccessfulRowExtra = $$('.hasLastSuccess tr:nth-child(3)');
			lastSuccessfulRowExtra[0].id = "";
		}
	},

	onShowAllClicked: function() {
		if(window.location.hash.indexOf("#showAll") >= 0) {
			window.location.hash = window.location.hash.replace("#showAll", "#hideAll");
		} else if(window.location.hash.indexOf("#hideAll") >= 0) {
			window.location.hash = window.location.hash.replace("#hideAll", "#showAll");
		} else {
			window.location.hash = window.location.hash + "#showAll";
		}
	},

	showAllColumns: function() {
		$$(".shouldBeHidden").each(function(it) {
			it.removeClassName("hidden");
		});

		var totalColumnCount = $$('th.cellHeader').size();
		DescriptionDashboard.resetColumnSpan(totalColumnCount)
	},

	hideAllColumns: function() {
		$$(".shouldBeHidden").each(function(it) {
			it.addClassName("hidden");
		});

		var allColumnCount = $$('th.cellHeader').size();
		var hiddenColumnCount = $$('th.cellHeader.shouldBeHidden').size();
		var totalColumnCount = allColumnCount - hiddenColumnCount;
		DescriptionDashboard.resetColumnSpan(totalColumnCount)
	},

	resetColumnSpan: function(columnCount) {
		$$('.extra-details td').each(function(it) {
			it.setAttribute('colspan', columnCount+1);
		});
	}
};