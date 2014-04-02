var Chromecast = function () {

}
Chromecast.prototype.exec = function(action) {
	var args = [].slice.call(arguments);
	args.shift();
	var callback;
	if (args[args.length-1] instanceof Function) {
		callback = args.pop();
	}
	cordova.exec(function (err, result) { callback && callback(err, result); }, function(err) { callback && callback(err); }, "Chromecast", action, args);
}