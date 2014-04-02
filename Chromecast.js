var Chromecast = function () {
	EventEmitter.call(this);
	if (!Chromecast.instance) {
		Chromecast.instance = this;
		return this;
	}
	return Chromecast.instance;
}
Chromecast.prototype = Object.create(EventEmitter.prototype);
Chromecast.prototype.echo = function(str) {
	this.exec("echo", str, function (str) {
		alert(str);
	})
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