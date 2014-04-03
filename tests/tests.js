exports.init = function() {
  eval(require('org.apache.cordova.test-framework.test').injectJasmineInterface(this, 'this'));
  jasmine.DEFAULT_TIMEOUT_INTERVAL = 5000;

  var cc = require('acidhax.cordova.chromecast.Chromecast');

  describe('Chromecast', function () {
    var fail = function(done, why) {
      if (typeof why !== 'undefined') {
        console.error(why);
      }
      expect(true).toBe(false);
      done();
    };

    it("should contain definitions", function() {
      expect(typeof cc.initialize).toBeDefined();
      expect(typeof cc.play).toBeDefined();
      expect(typeof cc.pause).toBeDefined();
      expect(typeof cc.stop).toBeDefined();
      expect(typeof cc.seek).toBeDefined();
      expect(typeof cc.volume).toBeDefined();
      expect(typeof cc.loadUrl).toBeDefined();
      expect(typeof cc.launch).toBeDefined();
      expect(typeof cc.echo).toBeDefined();
      expect(typeof cc.getDevices).toBeDefined();
      expect(typeof cc.initialize).toBeDefined();
    });
  });
};

