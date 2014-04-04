exports.init = function() {
  eval(require('org.apache.cordova.test-framework.test').injectJasmineInterface(this, 'this'));
  jasmine.DEFAULT_TIMEOUT_INTERVAL = 25000;

  var cc = require('acidhax.cordova.chromecast.Chromecast');

  var defaultReceiverAppId = 'CC1AD845';
  var videoUrl = 'http://s3.nwgat.net/flvplayers3/bbb.mp4';

  describe('Chromecast', function () {
    var fail = function(done, why) {
      if (typeof why !== 'undefined') {
        console.error(why);
      }
      expect(true).toBe(false);
      done();
    };

    it("should contain definitions", function() {
      expect(typeof chromecast.getDevices).toBeDefined();
      expect(typeof chromecast.launch).toBeDefined();
      expect(typeof chromecast.play).toBeDefined();
      expect(typeof chromecast.pause).toBeDefined();
      expect(typeof chromecast.stop).toBeDefined();
      expect(typeof chromecast.seek).toBeDefined();
      expect(typeof chromecast.volume).toBeDefined();
      expect(typeof chromecast.loadUrl).toBeDefined();
      expect(typeof chromecast.echo).toBeDefined();
    });

    it('discovering and launching', function(done) {
      var onDevice = function(deviceId, deviceName) {
        if (deviceName.indexOf('Ugly') > -1) {
          chromecast.removeListener('device', onDevice);

          expect(deviceId).toBeDefined();
          expect(deviceName).toBeDefined();

          chromecast.launch(deviceId, defaultReceiverAppId, function(err) {
            expect(err).toEqual(null);
            setTimeout(done, 2000);
          });
        }
      };

      chromecast.on('device', onDevice);
      chromecast.getDevices(defaultReceiverAppId);
    });

    it('loading a url', function(done) {
      chromecast.loadUrl(videoUrl, function(err) {
        expect(err).toEqual(null);
        setTimeout(done, 5000);
      })
    });

    it('pause', function(done) {
      chromecast.pause(function(err) {
        expect(err).toEqual(null);
        setTimeout(done, 5000);
      })
    });

    it('play', function(done) {
      chromecast.play(function(err) {
        expect(err).toEqual(null);
        setTimeout(done, 5000);
      })
    });

    it('setVolume', function(done) {
      chromecast.setVolume(0.5, function(err) {
        expect(err).toEqual(null);
        setTimeout(done, 5000);
      });
    });

    it('seek', function(done) {
      chromecast.seek(15, function(err) {
        expect(err).toEqual(null);
        setTimeout(done, 5000);
      });
    });

    it('kill', function(done) {
      chromecast.kill(function(err) {
        expect(err).toEqual(null);
        setTimeout(done, 5000);
      });
    });

  });
};

