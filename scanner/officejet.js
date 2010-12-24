var page = require('webpage').create(),
    system = require('system'),
    fs = require('fs'),
    base64 = require('./base64'),
    wait = require('./wait'),
    width = 2480,
    height = 3508;

if (system.args.length != 2) {
  console.log('Usage: officejet.js <output>');
  console.log('Example: officejet.js Blah_20121101.png');
  phantom.exit(1);
}

var path = system.args[1];

page.viewportSize = { width: 600, height : 600 };

page.onConsoleMessage = function(msg) {
    console.log(msg);

    if (msg.match("<scan:ScanJob.*") != null) {
	width = parseInt(msg.match("<scan:Width>(.*)</scan:Width>")[1]);
	height = parseInt(msg.match("<scan:Height>(.*)</scan:Height>")[1]);
	console.log("Output width: " + width);
	console.log("Output height: " + height);
    }
}

page.open('http://officejet.gedanken.org/#hId-webscanPage', function (status) {
    if (status !== 'success') {
	console.log('Unable to access network: ' + status);
	phantom.exit(1);
    } else {
	wait.waitFor(function() {
	    return page.evaluate(function() {
		return document.querySelector("#app-page-btns") != null;
	    });
	}, function() {
	    page.evaluate(function() {
		document.querySelector("#webscan-ifield-media-size select").value = 'letter';
	    });

	    page.evaluate(function() {
		$(".scan").click();
	    });

	    wait.waitFor(function() {
		return page.content.match("Scan Done") != null;
	    }, function() {
		var image =
		    page.evaluate(function(width, height) {
		    var img = document.querySelector("#webscan-frame").contentWindow.document.querySelector("img");
		    var canvas = document.createElement("canvas");
		    canvas.width = width;
		    canvas.height = height;

		    var ctx = canvas.getContext("2d");
		    ctx.drawImage(img, 0, 0);

		    return canvas.toDataURL("image/png");
		    }, width, height);

		fs.write(path, base64.decode(image.replace(/^data:image\/(png|jpg);base64,/, "")), 'wb');

		phantom.exit(1);
	    }, 60000);
	}, 15000);
    }
});
