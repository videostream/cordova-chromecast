var Chromecast = function () {
	EventEmitter.call(this);
	if (!Chromecast.instance) {
		Chromecast.instance = this;
		this.initialize();
		this.getDevices();
		this.devices = {};
		return this;
	}
	return Chromecast.instance;
}
Chromecast.prototype = Object.create(EventEmitter.prototype);
Chromecast.prototype.initialize = function() {
	var self = this;
	this.on("device", function (id, deviceName) {
		if (!self.devices[id]) {
			self.devices[id] = deviceName;
			console.log(id, deviceName);
		}
	});
	this.on("deviceRemoved", function (id, deviceName) {
		// 
	});
	this.on("launch", function () {
		// 
	});
	this.on("launchFailed", function () {
		// 
	});
	this.on("volumeChanged", function () {
		// 
	});
	this.on("applicationStatusChanged", function () {
		// 
	});
	this.on("disconnect", function () {
		// 
	});
	this.on("message", function (namespace, message) {
		// 
	});
};
Chromecast.prototype.play = function(cb) {
	this.exec("mediaControl", "play", cb);
};
Chromecast.prototype.pause = function(cb) {
	this.exec("mediaControl", "pause", cb);
};
Chromecast.prototype.stop = function(cb) {
	this.exec("mediaControl", "stop", cb);
};
Chromecast.prototype.seek = function(pos, cb) {
	this.exec("mediaControl", "seek", pos, cb);
};
Chromecast.prototype.volume = function(level, cb) {
	this.exec("mediaControl", "volume", level, cb);
};
Chromecast.prototype.loadUrl = function(url) {
	this.exec("loadUrl", url || "http://192.168.1.104:5556/", function(err) {
		if (err) {
			console.log("error");
		}
	});
};
Chromecast.prototype.launch = function(castId) {
	// body...
	this.exec("launch", castId || 0);
};
Chromecast.prototype.echo = function(str) {
	this.exec("echo", str, function (str) {
		alert(str);
	})
};
Chromecast.prototype.getDevices = function() {
	this.exec("getDevices");
};
Chromecast.prototype.exec = function(action) {
	var args = [].slice.call(arguments);
	args.shift();
	var callback;
	if (args[args.length-1] instanceof Function) {
		callback = args.pop();
	}
	cordova.exec(function (err, result) { callback && callback(err, result); }, function(err) { callback && callback(err); }, "Chromecast", action, args);
}
Chromecast = new Chromecast();