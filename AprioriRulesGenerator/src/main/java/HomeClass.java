import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by maverick on 5/1/17.
 */

public class HomeClass {
    // historyList - HashMap for slot, url-time
    // transactionList - List of transactions
    // categoryList - HashMap for url, category
    private static MongoClient mongoClient;

    public static String getCollectionName(){
        StringBuilder collectionName = new StringBuilder("h");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        DateFormat dF = new SimpleDateFormat("MM/dd/yyyy");
        collectionName.append(dF.format(cal.getTime()).toString().replace("/",""));
        return collectionName.toString();
    }

    private static MongoDatabase connectToDB(){
        MongoClientURI mongoClientURI = new MongoClientURI("mongodb://vaisham92:marias@ds131041.mlab.com:31041/history");
        mongoClient = new MongoClient(mongoClientURI);
        MongoDatabase db = mongoClient.getDatabase("history");
        return db;
    }

    private static void disconnectDB(){
        mongoClient.close();
    }

    public static void main(String[] args){
        try{

            // Get Users list from MongoDb
            MongoDatabase mDB = connectToDB();

            // Retrieving Collection Name
            String yesterday = getCollectionName();
            //MongoCollection<Document> collection = mDB.getCollection(yesterday);
            MongoCollection<Document> collection = mDB.getCollection("h05032017");

            MongoCursor<Document> cursor = collection.find().iterator();
            JSONArray usersResultSet = new JSONArray();
            while (cursor.hasNext()){
                JSONObject currentRecord = new JSONObject(cursor.next().toJson());
                for(String user : (Set<String>)currentRecord.keySet()){
                    if(user.equals("_id")) continue;
                    JSONObject tempJSON = new JSONObject();
                    JSONArray userData = currentRecord.getJSONArray(user);
                    JSONObject userResult = getRulesAndAnalysis(userData);
                    tempJSON.put(user, userResult);
                    usersResultSet.put(tempJSON);
                }
            }
            System.out.println(usersResultSet);

            // Save rules & url category count in MongoDb


        }
        catch(Exception ex) {
            if(ex.getClass().getName().equals("UnknownHostException")){
                System.out.println("Error in connecting to MongoDb");
            }
        }
        finally {
            disconnectDB();
        }
    }

    private static JSONObject getRulesAndAnalysis(JSONArray userData){
        // Break data into time slots and time spent
        HashMap<Integer, JSONObject> historyList = breakData(userData);


        // Retrieve category of the url
        HashSet<String> urlList = new HashSet<String>();
        for(int i : historyList.keySet()) {
            urlList.addAll(historyList.get(i).keySet());
        }
        WebCategorizer wc = new WebCategorizer();
        HashMap<String, String> categoryList = wc.getWebCategories(urlList);

        // Generate time spent on each category in a time slot
        HashMap<Integer, JSONObject> urlTimeMap = new HashMap<Integer, JSONObject>();
        for(int i : historyList.keySet()){
            JSONObject urlData = historyList.get(i);
            for (Object url : urlData.keySet()) {
                JSONObject tempJSON = new JSONObject();
                String currentUrl = (String) url;
                String category = categoryList.get(currentUrl);
                if(urlTimeMap.containsKey(i)){
                    tempJSON = urlTimeMap.get(i);
                    if(tempJSON.has(category)){
                        tempJSON.put(category, tempJSON.getLong(category) + urlData.getLong(currentUrl));
                    }
                    else {
                        tempJSON.put(category, urlData.getLong(currentUrl) );
                    }
                }
                else {
                    tempJSON.put(category, urlData.getLong(currentUrl) );
                    urlTimeMap.put(i, tempJSON);
                }
            }
        }
        JSONArray urlCategoryTime = new JSONArray();
        for(int i : urlTimeMap.keySet()){
            JSONObject tempJSON = new JSONObject();
            tempJSON.put("slot", i);
            tempJSON.put("slot_categories", urlTimeMap.get(i));
            urlCategoryTime.put(tempJSON);
        }

        // Generate transactional data from historyList
        List<String> transactionList = new ArrayList<String>();
        for(int i : historyList.keySet()){
            JSONObject tempJSON = historyList.get(i);
            StringBuilder tempTransaction = new StringBuilder();
            for(String key : tempJSON.keySet()){
                if(tempJSON.getLong(key) > 30) {
                    tempTransaction.append((String)key+" ");
                }
            }
            if(tempTransaction.toString().length() != 0)
                transactionList.add(tempTransaction.toString());
        }

        // Generate rules
        GenerateRules gr = new GenerateRules();
        ArrayList<JSONObject> rules = gr.generateRules(transactionList);
        JSONObject rulesAndCategoryData = new JSONObject();
        rulesAndCategoryData.put("rules", rules);
        rulesAndCategoryData.put("categories", urlCategoryTime);
        return rulesAndCategoryData;
    }

    // Function to break data set into HashMap
    private static HashMap<Integer, JSONObject> breakData(JSONArray userData){
        try{
            HashMap<Integer, JSONObject> historyList = new HashMap<Integer, JSONObject>();
            for(Object urlHistory : userData){
                JSONObject historyRecord = (JSONObject)urlHistory;
                addToSlot(historyList, historyRecord);
                Timestamp T1 = new Timestamp(historyRecord.getLong("T1"));
                Timestamp T2 = new Timestamp(historyRecord.getLong("T2"));
            }
            return historyList;
        }
        catch(Exception ex) {
            System.out.println(ex.getClass().getName().toString());
            return new HashMap<Integer, JSONObject>();
        }
    }

    // Function to generate slots for url
    private static void addToSlot(HashMap<Integer, JSONObject> historyList, JSONObject historyRecord){
        Timestamp T1 = new Timestamp(historyRecord.getLong("T1"));
        Timestamp T2 = new Timestamp(historyRecord.getLong("T2"));
        int key = T1.getHours();
        String url = (String)historyRecord.get("hostname");
        Timestamp T3 = T1;
        Calendar cal = Calendar.getInstance();
        Calendar cal1 = Calendar.getInstance();
        cal.setTimeInMillis(T3.getTime());
        cal1.setTimeInMillis(T2.getTime());
        while(T3.getHours() != T2.getHours()) {
            T3 = addTransaction(url,T3,T2,historyList,historyRecord);
        }
        if(T3!=T2){
            if(historyList.containsKey(key/2)){
                JSONObject tempJSON = historyList.get(key/2);
                if(tempJSON.has(url)){
                    tempJSON.put(url, (Long)tempJSON.get(url) + (T2.getTime()-T3.getTime())/1000);
                }
                else{
                    tempJSON.put(url, (T2.getTime()-T3.getTime())/1000);
                }
            }
            else{
                JSONObject tempJSON = new JSONObject();
                tempJSON.put(url, (T2.getTime()-T3.getTime())/1000 );
                historyList.put(key/2, tempJSON);
            }
        }
    }

    // Function to add url to a specific transaction slot
    private static Timestamp addTransaction(String url, Timestamp T1,  Timestamp T2, HashMap<Integer, JSONObject> historyList, JSONObject historyRecord) {
        int key = T1.getHours();
        Timestamp T3 = null;
        if(T2.getHours() - T1.getHours() > 2) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(T1.getTime());
            cal.add(Calendar.HOUR, 2);
            cal.add(Calendar.MINUTE, 60-T1.getMinutes());
            cal.add(Calendar.SECOND, 59-T1.getSeconds());
            T3 = new Timestamp(cal.getTime().getTime());
        } else {
            T3 = T2;
        }
        if(historyList.containsKey(key/2)){
            JSONObject tempJSON = historyList.get(key/2);
            if(tempJSON.has(url)){
                tempJSON.put(url, (Long)tempJSON.get(url) + (T3.getTime()-T1.getTime())/1000);
            }
            else{
                tempJSON.put(url, (T3.getTime()-T1.getTime())/1000);
            }
        }
        else{
            JSONObject tempJSON = new JSONObject();
            tempJSON.put(url, (T3.getTime()-T1.getTime())/1000 );
            historyList.put(key/2, tempJSON);
        }
        return T3;
    }
}
