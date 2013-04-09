Event.observe(window, 'load', function() {
	var now = new Date();
	var nowUtcStr = now.toUTCString();
	var semiColonIndex = nowUtcStr.indexOf(':');
	if(semiColonIndex >= 2) {
		var hours = nowUtcStr.substring(semiColonIndex-2, semiColonIndex);
		var timezone = now.getHours() - parseInt(hours);
		createCookie('timezone', timezone);
	} else {
		console.log("couldn't parse utc string: ", nowUtcStr);
	}
});
