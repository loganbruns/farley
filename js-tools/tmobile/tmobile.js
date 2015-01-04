
var page = require('webpage').create(),
    system = require('system');

if (system.args.length < 3) {
  console.log('Usage: tmobile.js number password');
  console.log('Example: tmobile.js "7777777777" "password"');
  phantom.exit(1);
}

var phone = system.args[1];
var password = system.args[2];

page.viewportSize = { width: 600, height : 600 };

page.onConsoleMessage = function(msg) {
    console.log(msg);
}

function waitFor(testFx, onReady, timeOutMillis) {
    var maxtimeOutMillis = timeOutMillis ? timeOutMillis : 3000, //< Default Max Timout is 3s
        start = new Date().getTime(),
        condition = false,
        interval = setInterval(function() {
            if ( (new Date().getTime() - start < maxtimeOutMillis) && !condition ) {
                // If not time-out yet and condition not yet fulfilled
                condition = (typeof(testFx) === "string" ? eval(testFx) : testFx()); //< defensive code
            } else {
                if(!condition) {
                    // If condition still not fulfilled (timeout but condition is 'false')
                    var t = (new Date().getTime() - start);
                    console.log("'waitFor()' timeout after " + t + "ms.");
                page.render('waitfor-timeout-' + t + '.png');
                    phantom.exit(1);
                } else {
                    // Condition fulfilled (timeout and/or condition is 'true')
                    console.log("'waitFor()' finished in " + (new Date().getTime() - start) + "ms.");
                    typeof(onReady) === "string" ? eval(onReady) : onReady(); //< Do what it's supposed to do once the condition is fulfilled
                    clearInterval(interval); //< Stop this interval
                }
            }
        }, 250); //< repeat check every 250ms
};

function login(phone, password, handler) {
  page.open('https://www.t-mobile.com/Login', function (status) {
    if (status !== 'success') {
      console.log('Unable to access network: ' + status);
      handler(status);
    } else {
      page.evaluate(function(phone, password) {
        document.querySelector('#Login1_txtMSISDN').value = phone;
        document.querySelector('#Login1_txtPassword').value = password;
      }, phone, password);

      waitFor(function() {
	return page.evaluate(function() {
	    return ((typeof $('#lnkBtnLogin')["0"].classList) === 'object') && ($("#lnkBtnLogin")["0"].classList[0] == "button-genericsprite");
	});
      }, function() {
	page.evaluate(function() {
	    $("#lnkBtnLogin").click();
	});

        waitFor(function() {
          return page.evaluate(function() {
	    return document.querySelector("#accountInformation") != null;
	  });
        }, function() {
          console.log("Logged in.");

          handler('success');

/*
          page.evaluate(function() {
	      location.href = "https://my.t-mobile.com/Default.aspx?rp.Logon=true#";
	  });

	  waitFor(function() {
	    return page.evaluate(function() {
	      return document.querySelector("#lnkBtnLogin") != null;
	    });
	  }, function() {
            console.log("Logged out.");
            return true;
  	  }, 10000);
	  */
	}, 45000);        
      }, 10000);        
    }
  });
}

function exportCalls(phone, password, handler) {
  login(phone, password, function(status) {
    if (status !== 'success') {
      console.log('Unable to login: ' + status);
      handler(status);
    } else {
      page.render('tmobile-after-handle-login.png');

    setTimeout(function() {
	waitFor(function() {
	    return page.evaluate(function() {
		return document.querySelector("#usageLink") != null;
	    });
	}, function() {
	    page.render('tmobile-after-devices-listed.png');

	    page.evaluate(function() {
		var evObj = document.createEvent('Events');
		evObj.initEvent('click', true, false);
		document.querySelector("#usageLink").dispatchEvent(evObj);
	    });

	    waitFor(function() {
		return page.evaluate(function() {
		    return document.querySelector("#MinutesLink") != null;
		});
	    }, function() {
		page.render('tmobile-line-summary.png');

		page.evaluate(function() {
		    var evObj = document.createEvent('Events');
		    evObj.initEvent('click', true, false);
		    document.querySelector("#MinutesLink").dispatchEvent(evObj);
		});

		waitFor(function() {
		    return page.evaluate(function() {
			return document.querySelector("#unbilledMsisdnMinutesUsageSummary") != null;
		    });
		}, function() {
		    page.render('tmobile-download-calls.png');

		    page.onConsoleMessage = function(msg) {
			// TODO: write to file instead
			console.log(msg);
			handler('success');

			page.onConsoleMessage = function(msg) {
			    console.log(msg);
			}
		    };

		    page.evaluate((function () {
			var xhr = new XMLHttpRequest();
			xhr.open("GET", 
				 "https://ebill.t-mobile.com/myTMobile/onDownloadPage.do",
				 true);
			xhr.onreadystatechange = function() {
			    if (xhr.readyState == 4) {
				if (xhr.status == 200) {
				    console.log(xhr.responseText);
				} else {
				    console.log("ERROR");
				}
			    }
			};
			xhr.send(null);
		    }));
		}, 15000);
	    }, 15000);
	}, 15000);
    }, 20000);
    }
  });
}

console.log('attempting export.');


exportCalls(phone, password, function(status) {
    if (status !== 'success') {
      console.log('Unable to export: ' + status);
    }

    console.log('finished export.');

    //phantom.exit(1);
});
