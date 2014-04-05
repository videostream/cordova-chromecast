exports.init = function() {
  eval(require('org.apache.cordova.test-framework.test').injectJasmineInterface(this, 'this'));
  jasmine.DEFAULT_TIMEOUT_INTERVAL = 25000;

  // var cc = require('acidhax.cordova.chromecast.Chromecast');

  var applicationID = 'CC1AD845';
  var videoUrl = 'http://s3.nwgat.net/flvplayers3/bbb.mp4';


  describe('chrome.cast', function() {

    var _session;
    var _receiverAvailability;
    var _currentMedia;

    it('should contain definitions', function(done) {
      expect(chrome.cast.VERSION).toBeDefined();
      expect(chrome.cast.ReceiverAvailability).toBeDefined();
      expect(chrome.cast.ReceiverType).toBeDefined();
      expect(chrome.cast.SenderPlatform).toBeDefined();
      expect(chrome.cast.AutoJoinPolicy).toBeDefined();
      expect(chrome.cast.Capability).toBeDefined();
      expect(chrome.cast.DefaultActionPolicy).toBeDefined();
      expect(chrome.cast.ErrorCode).toBeDefined();
      expect(chrome.cast.timeout).toBeDefined();
      expect(chrome.cast.isAvailable).toBeDefined();
      expect(chrome.cast.ApiConfig).toBeDefined();
      expect(chrome.cast.Receiver).toBeDefined();
      expect(chrome.cast.DialRequest).toBeDefined();
      expect(chrome.cast.SessionRequest).toBeDefined();
      expect(chrome.cast.Error).toBeDefined();
      expect(chrome.cast.Image).toBeDefined();
      expect(chrome.cast.SenderApplication).toBeDefined();
      expect(chrome.cast.Volume).toBeDefined();
      expect(chrome.cast.media).toBeDefined();
      expect(chrome.cast.initialize).toBeDefined();
      expect(chrome.cast.requestSession).toBeDefined();
      expect(chrome.cast.setCustomReceivers).toBeDefined();
      expect(chrome.cast.Session).toBeDefined();
      expect(chrome.cast.media.PlayerState).toBeDefined();
      expect(chrome.cast.media.ResumeState).toBeDefined();
      expect(chrome.cast.media.MediaCommand).toBeDefined();
      expect(chrome.cast.media.MetadataType).toBeDefined();
      expect(chrome.cast.media.StreamType).toBeDefined();
      expect(chrome.cast.media.timeout).toBeDefined();
      expect(chrome.cast.media.LoadRequest).toBeDefined();
      expect(chrome.cast.media.PlayRequest).toBeDefined();
      expect(chrome.cast.media.SeekRequest).toBeDefined();
      expect(chrome.cast.media.VolumeRequest).toBeDefined();
      expect(chrome.cast.media.StopRequest).toBeDefined();
      expect(chrome.cast.media.PauseRequest).toBeDefined();
      expect(chrome.cast.media.GenericMediaMetadata).toBeDefined();
      expect(chrome.cast.media.MovieMediaMetadata).toBeDefined();
      expect(chrome.cast.media.MusicTrackMediaMetadata).toBeDefined();
      expect(chrome.cast.media.PhotoMediaMetadata).toBeDefined();
      expect(chrome.cast.media.TvShowMediaMetadata).toBeDefined();
      expect(chrome.cast.media.MediaInfo).toBeDefined();
      expect(chrome.cast.media.Media).toBeDefined();
      expect(chrome.cast.Session.prototype.setReceiverVolumeLevel).toBeDefined();
      expect(chrome.cast.Session.prototype.setReceiverMuted).toBeDefined();
      expect(chrome.cast.Session.prototype.stop).toBeDefined();
      expect(chrome.cast.Session.prototype.sendMessage).toBeDefined();
      expect(chrome.cast.Session.prototype.addUpdateListener).toBeDefined();
      expect(chrome.cast.Session.prototype.removeUpdateListener).toBeDefined();
      expect(chrome.cast.Session.prototype.addMessageListener).toBeDefined();
      expect(chrome.cast.Session.prototype.removeMessageListener).toBeDefined();
      expect(chrome.cast.Session.prototype.addMediaListener).toBeDefined();
      expect(chrome.cast.Session.prototype.removeMediaListener).toBeDefined();
      expect(chrome.cast.Session.prototype.loadMedia).toBeDefined();
      expect(chrome.cast.media.Media.prototype.play).toBeDefined();
      expect(chrome.cast.media.Media.prototype.pause).toBeDefined();
      expect(chrome.cast.media.Media.prototype.seek).toBeDefined();
      expect(chrome.cast.media.Media.prototype.stop).toBeDefined();
      expect(chrome.cast.media.Media.prototype.setVolume).toBeDefined();
      expect(chrome.cast.media.Media.prototype.supportsCommand).toBeDefined();
      expect(chrome.cast.media.Media.prototype.getEstimatedTime).toBeDefined();
      expect(chrome.cast.media.Media.prototype.addUpdateListener).toBeDefined();
      expect(chrome.cast.media.Media.prototype.removeUpdateListener).toBeDefined();
      done();
    });

    it('api should be available', function(done) {
      setTimeout(function() {
        expect(chrome.cast.isAvailable).toEqual(true);
        done();
      }, 1000)
    });

    it('initialize should succeed', function(done) {
      var sessionRequest = new chrome.cast.SessionRequest(applicationID);
      var apiConfig = new chrome.cast.ApiConfig(sessionRequest, function(session) {
        _session = session;
      }, function(available) {
        _receiverAvailability = available;
      });

      chrome.cast.initialize(apiConfig, function() {
        done();
      }, function(err) {
        expect(err).toBe(null);
        done();
      });
    });

    it('receiver available', function(done) {
      setTimeout(function() {
        expect(_receiverAvailability).toEqual(chrome.cast.ReceiverAvailability.AVAILABLE);
        done();
      }, 2000);
    });


    it('requestSession should succeed', function(done) {
      chrome.cast.requestSession(function(session) {
        expect(session).toBeDefined();
        expect(session.appId).toBeDefined();
        exepct(session.displayName).toBeDefined();
        expect(session.receiver).toBeDefined();
        expect(session.receiver.friendlyName).toBeDefined();
        done();
      }, function(err) {
        expect(err).toBe(null);
        done();
      })
    });

    it('loadRequest should work', function(done) {
      var mediaInfo = new chrome.cast.media.MediaInfo(videoUrl);
      var request = new chrome.cast.media.LoadRequest(mediaInfo);
      _session.loadMedia(request, function(media) {
        _currentMedia = media;
        expect(_currentMedia instanceof chrome.cast.media.Media).toBe(true);
        expect(_currentMedia.sessionId).toEqual(_session.sessionId);
        done();
      }, function(err) {
        expect(err).toBeNull();
        done();
      });

    });

    it('pause media should succeed', function(done) {
      setTimeout(function() {
        _currentMedia.pause(null, function() {
          done();
        }, function(err) {
          expect(err).toBeNull();
          done();
        });
      }, 1000);
    });

    it('play media should succeed', function(done) {
      setTimeout(function() {
        _currentMedia.play(null, function() {
          done();
        }, function(err) {
          expect(err).toBeNull();
          done();
        });
      }, 1000);
    });

    it('seek media should succeed', function(done) {
      setTimeout(function() {
        var request = new chrome.cast.media.SeekRequest();
        request.currentTime = 10;

        _currentMedia.seek(request, function() {
          done();
        }, function(err) {
          expect(err).toBeNull();
          done();
        });
      }, 1000);
    });
    
  });


  xdescribe('Chromecast', function () {
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
        if (deviceName.indexOf('Ugly') > -1 || deviceName.indexOf('Graham') > -1) {
          chromecast.removeListener('device', onDevice);

          expect(deviceId).toBeDefined();
          expect(deviceName).toBeDefined();

          console.log('ON DEVICE', deviceId, deviceName);
          setTimeout(function() {
            chromecast.getRoute(deviceId, function(err, name) {
              expect(err).toBe(null);
              expect(name).not.toBe(null);
              console.log('GET ROUTE', err, name);
              
              chromecast.launch(deviceId, applicationID, function(err) {
                expect(err).toEqual(null);
                setTimeout(done, 2000);
              });
            })
          }, 800);
        }
      };

      chromecast.on('device', onDevice);
      chromecast.getDevices(applicationID);
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
        setTimeout(done, 2000);
      })
    });

    it('play', function(done) {
      chromecast.play(function(err) {
        expect(err).toEqual(null);
        setTimeout(done, 2000);
      })
    });

    it('setVolume', function(done) {
      chromecast.setVolume(0.5, function(err) {
        expect(err).toEqual(null);
        setTimeout(done, 1000);
      });
    });

    xit('getVolume', function(done) {
      chromecast.setVolume(0.2, function(err) {
        expect(err).toEqual(null);
        setTimeout(function() {
          chromecast.getVolume(function(err, volume) {
            expect(err).toEqual(null);
            expect(volume).toEqual(0.2);
            done(); 
          });
        }, 500);
      });
    });

    it('seek', function(done) {
      chromecast.seek(15, function(err) {
        expect(err).toEqual(null);
        setTimeout(done, 2000);
      });
    });

    it('kill', function(done) {
      chromecast.kill(function(err) {
        expect(err).toEqual(null);
        done();
      });
    });

  });
};

