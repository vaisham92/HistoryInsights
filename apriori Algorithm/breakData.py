import json
import time
import datetime
from collections import defaultdict

def load_data():
	return [
                {
                        "T1" : 1493624668716,
                        "T2" : 1493624704594,
                        "hostname" : "mongodb.github.io",
                        "pathname" : "/node-mongodb-native/2.2/api/"
                },
                {
                        "T1" : 1493624711112,
                        "T2" : 1493624765099,
                        "hostname" : "angular-tutorial.quora.com",
                        "pathname" : "/Make-a-Todo-Chrome-Extension-with-AngularJS-1"
                },
                {
                        "T1" : 1493624766449,
                        "T2" : 1493624778930,
                        "hostname" : "angular-tutorial.quora.com",
                        "pathname" : "/Make-a-Todo-Chrome-Extension-with-AngularJS-1"
                },
                {
                        "T1" : 1493624782293,
                        "T2" : 1493624802577,
                        "hostname" : "newtab",
                        "pathname" : "/"
                },
                {
                        "T1" : 1493624802578,
                        "T2" : 1493624807890,
                        "hostname" : "www.google.com",
                        "pathname" : "/search"
                },
                {
                        "T1" : 1493624811631,
                        "T2" : 1493624838607,
                        "hostname" : "developer.chrome.com",
                        "pathname" : "/extensions/tabs"
                },
                {
                        "T1" : 1493624838607,
                        "T2" : 1493624884959,
                        "hostname" : "developer.chrome.com",
                        "pathname" : "/extensions/tabs"
                },
                {
                        "T1" : 1493624884959,
                        "T2" : 1493624897009,
                        "hostname" : "newtab",
                        "pathname" : "/"
                },
                {
                        "T1" : 1493624897010,
                        "T2" : 1493624908105,
                        "hostname" : "www.google.com",
                        "pathname" : "/search"
                },
                {
                        "T1" : 1493624908106,
                        "T2" : 1493624923002,
                        "hostname" : "www.google.com",
                        "pathname" : "/search"
                },
                {
                        "T1" : 1493624923003,
                        "T2" : 1493624955170,
                        "hostname" : "developer.chrome.com",
                        "pathname" : "/extensions/idle"
                },
                {
                        "T1" : 1493624970728,
                        "T2" : 1493624997953,
                        "hostname" : "www.responsivemiracle.com",
                        "pathname" : "/best-materialize-css-templates/"
                },
                {
                        "T1" : 1493624997953,
                        "T2" : 1493625006126,
                        "hostname" : "maxartkiller.in",
                        "pathname" : "/rock-on-materialize-responsive-admin-html-template/"
                },
                {
                        "T1" : 1493625006127,
                        "T2" : 1493625011257,
                        "hostname" : "maxartkiller.in",
                        "pathname" : "/website/rockon/rockon_blue/pages/index.html"
                },
                {
                        "T1" : 1493531780065,
                        "T2" : 1493532100139,
                        "hostname" : "newtab",
                        "pathname" : "/"
                }
        ]

# Function to add a website to specific time slots the website belongs to
def add_to_slot(historyList, historyRecord):
	T1 = historyRecord['T1']
	T2 = historyRecord['T2']
	startT = time.ctime(T1/1000)
	endT = time.ctime(T2/1000)
	startTime = datetime.datetime.strptime(startT, "%a %b %d %H:%M:%S %Y")
	endTime = datetime.datetime.strptime(endT, "%a %b %d %H:%M:%S %Y")
	startSlot = (startTime.hour)/2
	endSlot = (endTime.hour)/2

	# Change this for adding to more than 1 slot
	dataObject = historyList[startSlot]
	if historyRecord['hostname'] in dataObject.keys():
		dataObject[historyRecord['hostname']] = dataObject[historyRecord['hostname']] + (T2-T1)/1000
	else:
		dataObject[historyRecord['hostname']] = (T2-T1)/1000


# Function to break the JSON input of a specific user into a series of transactions based on timeslots
def break_data():
	#Loading Data Set from load_data
	jsonData = load_data()
	historyList = [{},{},{},{},{},{},{},{},{},{},{},{}]
	result = []
	for historyRecord in jsonData:
		
		# Adding the website to a Time slot
		add_to_slot(historyList, historyRecord)

	for historyRecord in historyList:
		temp_result = []
		for url in historyRecord.keys():
			if historyRecord[url] > 30:
				temp_result.append(url)
		if len(temp_result) > 0:
			result.append(temp_result)

	#result[1].append('mongodb.github.io')
	urlSet = set()
	transactionList = list()
	for record in result:
		transaction = frozenset(record)
		transactionList.append(transaction)
		for url in transaction:
			urlSet.add(frozenset([url]))
	return urlSet, transactionList