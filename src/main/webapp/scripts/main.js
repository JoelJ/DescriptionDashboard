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

		DescriptionDashboard.defaultFilter = "DescriptionDashboard.defaultFilter";
		var filterCookie = readCookie(DescriptionDashboard.defaultFilter);
		if(filterCookie) {
			DescriptionDashboard.loadDefaultFilter(filterCookie);
		}

		var hashes = window.location.hash.split('#')
		for(var i = hashes.length-1; i >= 0; i--) {
			var hash = hashes[i];
			if(hash == "fullScreen") {
				DescriptionDashboard.fillScreen();
			} else if(hash == "showAll") {
				DescriptionDashboard.showAllColumns();
			} else if(hash.startsWith("branch=")) {
				var split = hash.split('=', 2);
				var branch = split[1];
				DescriptionDashboard.loadDefaultFilter(branch);
			}
		}

		$('DescriptionDashboard').observe('click', DescriptionDashboard.onRelayClick);
		$('branchSelector').observe('change', DescriptionDashboard.onBranchSelectorChange);

		$(document.body).observe('keyup', DescriptionDashboard.onKeyPressed);

		DescriptionDashboard.animatedImages = $$('.running img.status');
		DescriptionDashboard.animateInterval = 0.1;
		DescriptionDashboard.currentAnimation = 0.4;
		DescriptionDashboard.animateOrbs();
	},

	loadDefaultFilter: function(branch) {
		var select = $("branchSelector");
		for(var optionIndex = select.options.length-1; optionIndex >= 0; optionIndex--) {
			var option = select[optionIndex];
			if(option.value == branch) {
				DescriptionDashboard.previouslySelectedBranch = branch;
				DescriptionDashboard.filterList(branch, "{all}");
				select.selectedIndex = optionIndex;
				break;
			}
		}
	},

	animateOrbs: function() {
		DescriptionDashboard.currentAnimation += DescriptionDashboard.animateInterval;
		if(DescriptionDashboard.currentAnimation <= 0.2 || DescriptionDashboard.currentAnimation >= 0.8) {
			DescriptionDashboard.animateInterval *= -1;
		}

		var imagesToAnimate = DescriptionDashboard.animatedImages;
		for(var i = imagesToAnimate.length-1; i >= 0; i--) {
			imagesToAnimate[i].setOpacity(DescriptionDashboard.currentAnimation);
		}
		setTimeout(DescriptionDashboard.animateOrbs, 100);
	},

	onRelayClick: function(e) {
		if(e.target.tagName == "A" || e.target.hasClassName('status') || e.target.up('.customColumn')) {
			//let normal 'a' tags click as normal
		} else if(e.target.hasClassName('showAll')) {
			DescriptionDashboard.onShowAllClicked();
		} else if(e.target.hasClassName('fullScreen')) {
			DescriptionDashboard.onFullScreenClicked();
        } else if(e.target.hasClassName('toggleJustMe')) {
            DescriptionDashboard.onShowJustMeClicked();
		} else if(e.target.up('.row')) {
			DescriptionDashboard.onRowClick(e);
		}
	},

	onKeyPressed: function(e) {
		if(e.keyCode == 27) {
			if(!DescriptionDashboard.collapseAllRows()) {
				window.location.hash = window.location.hash.replace("#showAll", "#hideAll");
			}
		}
	},

	forceRefresh: function() {
		DescriptionDashboard.killRefresh();
		DescriptionDashboard.enableAutoRefresh();

		if(window.location.search.indexOf('disableAutoRefresh=true') <= -1) {
			DescriptionDashboard.killRefresh();
		}
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

		var rootPath = window.location.pathname;
		if(!rootPath.endsWith("/")) {
			rootPath = rootPath + '/';
		}
		var url = rootPath + 'tableOnly' + window.location.search;
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

		DescriptionDashboard.animatedImages = $$('.running img.status');
		DescriptionDashboard.refreshHandle = setTimeout(DescriptionDashboard.enableAutoRefresh, 10000);

		DescriptionDashboard.filterList(DescriptionDashboard.previouslySelectedBranch, "{all}");
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

			var rootPath = window.location.pathname;
			if(!rootPath.endsWith("/")) {
				rootPath = rootPath + '/';
			}
			var url = rootPath + "rowExtras?id="+rowId;
			$(document.body).addClassName('loading');
			new Ajax.Request(url, {
				method: 'get',
				evalJS: 'false',
				onSuccess: function(transport) {
					var resultBody = transport.responseText;
					resultBody = DescriptionDashboard.onDrawerRendered(resultBody);
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

	onDrawerRendered: function(html) {
		return html;
	},

	collapseAllRows: function() {
		var expandedRows = $$('.row.expanded');
		if(expandedRows.length > 0) {
			expandedRows.each(function(row) {
				DescriptionDashboard.toggleRow(row);
			});
			return true;
		} else {
			return false;
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

	onFullScreenClicked: function() {
		var dashboard = $('DescriptionDashboard');
		if(dashboard.webkitRequestFullScreen) {
			dashboard.webkitRequestFullScreen(Element.ALLOW_KEYBOARD_INPUT);
		}
		if(dashboard.mozRequestFullScreen) {
			dashboard.mozRequestFullScreen();
		}
	},

    onShowJustMeClicked: function() {
        $('DescriptionDashboard').toggleClassName('showCurrentUsername');
	},

	fillScreen: function() {
		var dashboard = $('DescriptionDashboard');
		dashboard.addClassName('inFullScreen');

		var links = $$('#DescriptionDashboard a');
		links.each(function(link) {
			link.setAttribute('target', '_blank');
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
	},

	onBranchSelectorChange: function(event) {
		var selectBox = event.target;

		var newValue = selectBox[selectBox.selectedIndex].value;
		var previousValue = DescriptionDashboard.previouslySelectedBranch == null || DescriptionDashboard == "{all}" ? "{all}" : DescriptionDashboard.previouslySelectedBranch;
		DescriptionDashboard.previouslySelectedBranch = newValue;

		DescriptionDashboard.collapseAllRows();
		DescriptionDashboard.filterList(newValue, previousValue);
        if(window.location.hash.indexOf("branch=") > 0) {
            window.location.hash = window.location.hash.replace(new RegExp('#branch='+previousValue+'(?=(#|$))'), '#branch='+newValue);
        } else {
            window.location.hash = "branch="+newValue;
        }

	},

	filterList: function(newValue, previousValue) {
		if(newValue == undefined || newValue == null) {
			return;
		}

		var rowSelector = "#DescriptionDashboard table tr[class*='row']";
		var rowsToHideSelector = rowSelector;
		if(previousValue != "{all}") {
			rowsToHideSelector += "[class~='"+previousValue+"']";
		}

		var rowsToHide = $$(rowsToHideSelector);
		for(var i = rowsToHide.length-1; i >= 0; i--) {
			rowsToHide[i].addClassName('hidden');
		}

		var rowsToShowSelector = rowSelector;
		if(newValue != "{all}") {
			rowsToShowSelector += "[class*='"+newValue+"']";
		}

		var rowsToShow = $$(rowsToShowSelector);
		for(var j = rowsToShow.length-1; j >= 0; j--) {
			rowsToShow[j].removeClassName('hidden');
		}

		createCookie(DescriptionDashboard.defaultFilter, newValue);
	}
};