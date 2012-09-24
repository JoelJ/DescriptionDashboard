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

		if(window.location.hash.indexOf('#showAll') >= 0) {
			DescriptionDashboard.showAllColumns();
		}
		if(window.location.hash.indexOf('#fullScreen') >= 0) {
			DescriptionDashboard.fillScreen();
		}

		$('DescriptionDashboard').observe('click', DescriptionDashboard.onRelayClick);

		$(document.body).observe('keyup', DescriptionDashboard.onKeyPressed);
	},

	onRelayClick: function(e) {
		if(e.target.tagName == "A" || e.target.hasClassName('status')) {
			//let normal 'a' tags click as normal
		} else if(e.target.hasClassName('showAll')) {
			DescriptionDashboard.onShowAllClicked();
		} else if(e.target.hasClassName('fullScreen')) {
			DescriptionDashboard.onFullScreenClicked();
		} else if(e.target.up('.row')) {
			DescriptionDashboard.onRowClick(e);
		}
	},

	onKeyPressed: function(e) {
		console.log(e);
		if(e.keyCode == 27) {
			DescriptionDashboard.collapseAllRows();
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

		var url = window.location.pathname + '/tableOnly' + window.location.search;
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

		if(window.location.hash.indexOf('#showAll') >= 0) {
			DescriptionDashboard.showAllColumns();
		}

		DescriptionDashboard.refreshHandle = setTimeout(DescriptionDashboard.enableAutoRefresh, 10000);
	},

	onUpdateError: function(transport) {
		console.log(transport);
		setTimeout(DescriptionDashboard.enableAutoRefresh, 10000);
	},

	onHashChange: function(event) {
		var found = false;
		var newUrl = event.newURL;
		if(newUrl.indexOf('#showAll') >= 0) {
			DescriptionDashboard.showAllColumns();
			found = true;
		}
		if(newUrl.indexOf('#hideAll') >= 0) {
			DescriptionDashboard.hideAllColumns();
			found = true;
		}
		if(newUrl.indexOf('#fullScreen') >= 0){
			DescriptionDashboard.fillScreen();
			found = true;
		}

		if(!found) {
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
		DescriptionDashboard.toggleRow(row);
	},

	toggleRow: function(row) {
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

	collapseAllRows: function() {
		$$('.row.expanded').each(function(row) {
			DescriptionDashboard.toggleRow(row);
		});
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

	onFullScreenClicked: function() {
		var dashboard = $('DescriptionDashboard');
		if(dashboard.webkitRequestFullScreen) {
			dashboard.webkitRequestFullScreen(Element.ALLOW_KEYBOARD_INPUT);
		}
		if(dashboard.mozRequestFullScreen) {
			dashboard.mozRequestFullScreen();
		}
	},

	fillScreen: function() {
		var dashboard = $('DescriptionDashboard');
		dashboard.addClassName('inFullScreen');

		var links = $$('#DescriptionDashboard a');
		links.each(function(link) {
			link.setAttribute('target', '_blank');
			console.log(link);
		});

		$('side-panel').hide();
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
			it.setAttribute('colspan', columnCount+2);
		});
	}
};