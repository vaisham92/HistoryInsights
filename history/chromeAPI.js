angular.module('history').service('chromeAPI', function ($q) {

    var _this = this;
    this.data = [];
    this.authToken = "";
    this.profile = "";

    /**
    * checks whether user is already authenticated or not
    */
    this.isLoggedIn = function(success, failure) {
    	chrome.storage.sync.get("auth_token", function(keys) {
    		console.log("feteched auth_token: " + JSON.stringify(keys));
    		if (keys === null || keys === undefined) failure("undefined");
    		else success(keys);
    	});
    };

    /**
    * performs login action for the user
    */
    this.doLogin = function(success, failure) {
    	chrome.identity.getAuthToken({ 'interactive' : true }, function (token) {
		      if (chrome.runtime.lastError) {
		          alert(chrome.runtime.lastError.message);
		          failure(chrome.runtime.lastError.message);
		      }
		      else {
					var x = new XMLHttpRequest();
					x.open('GET', 'https://www.googleapis.com/oauth2/v2/userinfo?alt=json&access_token=' + token);
					x.onload = function() {
						//alert("auth_token: " + x.response);
                        console.log(token);
						saveData("auth_token", token);
                        saveData("profile", x.response);
						this.authToken = token;
                        this.profile = x.response;
						console.log(this.authToken);
					};
					x.send();
					success(token);
		      }
		  });
    };

    /**
    * fetches the current url of the browser
    */
    this.getCurrentURL = function(success, failure) {
        /*chrome.tabs.onUpdated.addListener(function(tabId, changeInfo, tab)  {
            console.log(changeInfo);
            if (info.status == "complete") {
                success(info);
            }
        });
        chrome.tabs.query({'active': true, 'lastFocusedWindow': true}, function (tabs) {
            success(tabs[0].url);
        });*/
        chrome.tabs.query({'active': true, 'windowId': chrome.windows.WINDOW_ID_CURRENT},
           function(tabs) {
              success(tabs[0].url);
           }
        );
    };

    /**
    * fetches the profile if of the user
    */
    this.getProfileId = function(success, failure) {
        chrome.identity.getAuthToken({ 'interactive' : true }, function (token) {
              if (chrome.runtime.lastError) {
                  alert(chrome.runtime.lastError.message);
                  failure(chrome.runtime.lastError.message);
              }
              else {
                    var x = new XMLHttpRequest();
                    x.open('GET', 'https://www.googleapis.com/oauth2/v2/userinfo?alt=json&access_token=' + token);
                    x.onload = function() {
                        //alert("auth_token: " + x.response);
                        //console.log(token);
                        saveData("auth_token", token);
                        saveData("profile", x.response);
                        this.authToken = token;
                        this.profile = x.response;
                        console.log(this.authToken);
                        console.log(this.profile);
                        var profile = JSON.parse(this.profile);
                        success(profile.id);
                    };
                    x.send();
              }
          });
    };
});