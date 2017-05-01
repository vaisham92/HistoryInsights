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
		// alert(JSON.stringify(data));
		$scope.loginUI = true;
		//console.log("$scope.loginUI: " + $scope.loginUI);
	}, function(err) {
		//alert(JSON.stringify(err));
		$scope.loginUI = false;
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
		$scope.loginUI = false;
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
	
	//updateT1();
	//updateWebsite();
	chrome.tabs.onActivated.addListener(function callback(activeInfo) {
		updateT2();
		updateWebsite();
		sendData(profile_id, newT1, newT2, website);
		//sendGet();
		updateT1();
		updateWebsite();
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
		}, function(err){});
	};

	var sendData = function(profile_id, newT1, newT2, website) {
        $http({
        	method: 'POST',
            url: 'http://localhost:3000/history',
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
		var get = $http.get("http://localhost:3000/get");
		get.success(function(data) {
			console.log("get call done");
		});
	};
});