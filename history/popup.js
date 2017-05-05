var historyApp = angular.module('history', ['ngRoute']);

historyApp.config(['$routeProvider', '$locationProvider',
    function($routeProvider, $locationProvider, $routeParams) {
        $routeProvider
            .when('/home', {
                templateUrl: 'popup2.html',
                controller: 'mainController'
            });
        $locationProvider.html5Mode(true);
    }
]);

historyApp.controller('mainController', function($scope, $http, $routeParams, chromeAPI) {
	//$scope.loginUI = true;

	chromeAPI.isLoggedIn(function(data) {
		//alert(JSON.stringify(data));
		$scope.loginUI = false;
		//console.log("$scope.loginUI: " + $scope.loginUI);
	}, function(err) {
		//alert(JSON.stringify(err));
		$scope.loginUI = true;
		//console.log("$scope.loginUI: " + $scope.loginUI);
	});
	$scope.login = function() {
		chromeAPI.doLogin(function(data) {
			//alert("login successful: " + data);
		}, function (err) {
			//alert("login failure: " + err);
		});
		//console.log("in here\n");
	};

	$scope.signup = function() {
		// $scope.loginUI = false;
		//console.log("in here 2\n");
	};

	var profile_id;
	chromeAPI.getProfileId(function(id) {
		console.log(id);
		profile_id = id;
	}, function(err) {});
	var current_date = new Date();
	var newT1 = current_date.getTime();
	var website;
	var newT2;
	var isUrlUpdated = false;
	
	//updateT1();
	//updateWebsite();
	chrome.tabs.onActivated.addListener(function callback(activeInfo) {
		if(!isUrlUpdated) {
			updateT2();
			updateWebsite();
			//changeWebsiteIfUpdated();
			sendData(profile_id, newT1, newT2, website);
			//sendGet();
			updateT1();
			updateWebsite();
		}
		else isUrlUpdated = false;
	});
	chrome.tabs.onUpdated.addListener(function(tabId, changeInfo, tab) {
        if(changeInfo.status == "complete") {

            updateT2();
			updateWebsite();
			isUrlUpdated = true;
			//changeWebsiteIfUpdated();
			sendData(profile_id, newT1, newT2, website);
			//sendGet();
			updateT1();
			updateWebsite();
        }
    });

    chrome.idle.onStateChanged.addListener(function callback(newState) {
    	console.log(newState);
    	//"active", "idle", or "locked"
    	if(newState == "idle" || newState == "locked") {
    		updateT2();
			updateWebsite();
			console.log("sending data");
    		sendData(profile_id, newT1, newT2, website);
    		website = null;
    		console.log(newState);
    	}
    	else if(newState == "active") {
    		updateT1();
    		updateWebsite();
    		console.log(newState);
    	}

    });

	var updateT1 = function() {
		current_date = new Date();
		newT1 = current_date.getTime();
	};

	var updateT2 = function() {
		current_date = new Date();
		newT2 = current_date.getTime();
	};

	var updateWebsite = function() {
		chromeAPI.getCurrentURL(function(url) {
			//alert(url);
			website = url;
		}, function(err) {});
	};

	var changeWebsiteIfUpdated = function() {
		chromeAPI.getUpdatedURL(function(url) {
			website = url;
		}, function(err) {});
	};

	var sendData = function(profile_id, newT1, newT2, website) {
		if(website == undefined || website == "chrome://newtab") return;
		if(newT2 - newT1 < 5000) return;
        $http({
        	method: 'POST',
            url: 'http://localhost:5000/history',
            data: {
	            "profile_id": profile_id,
	            "T1": newT1,
	            "T2": newT2,
	            "website": website
	        },
            headers: {
                'Content-Type': 'application/json'
            }
        }).success(function(data) {
        	console.log(data);
        });
	};

	var sendGet = function() {
		var get = $http.get("http://localhost:5000/get");
		get.success(function(data) {
			console.log("get call done");
		});
	};
});