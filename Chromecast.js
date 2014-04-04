var EventEmitter = require('acidhax.cordova.chromecast.EventEmitter');

var Chromecast = function () {
	EventEmitter.call(this);
	if (!Chromecast.instance) {
		Chromecast.instance = this;
		this.initialize();
		// this.getDevices();
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
	this.exec("play", cb);
};
Chromecast.prototype.pause = function(cb) {
	this.exec("pause", cb);
};
Chromecast.prototype.stop = function(cb) {
	this.exec("stop", cb);
};
Chromecast.prototype.seek = function(pos, cb) {
	this.exec("seek", pos, cb);
};
Chromecast.prototype.setVolume = function(volume, cb) {
	this.exec("setVolume", volume, cb);
};
Chromecast.prototype.loadUrl = function(url, cb) {
	this.exec("loadUrl", url || "http://192.168.1.104:5556/", cb || function(err) {
		if (err) {
			console.log("error", err);
		}
	});
};
Chromecast.prototype.launch = function(castId, appId, cb) {
	// body...
	this.exec("launch", castId || 0, appId, cb);
};
Chromecast.prototype.echo = function(str) {
	this.exec("echo", str, function (err, str) {
		alert(str);
	})
};
Chromecast.prototype.getDevices = function(appId) {
	this.exec("getDevices", appId);
};
Chromecast.prototype.exec = function(action) {
	var args = [].slice.call(arguments);
	args.shift();
	var callback;
	if (args[args.length-1] instanceof Function) {
		callback = args.pop();
	}
	cordova.exec(function (result) { callback && callback(null, result); }, function(err) { callback && callback(err); }, "Chromecast", action, args);
}

module.exports = new Chromecast();