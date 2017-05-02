import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by maverick on 5/1/17.
 */
public class HomeClass {
    public static void main(String[] args){
        MongoHelper mongoObject = new MongoHelper();
        try{
            // Get Users list from MongoDb
            mongoObject.connect();
            // Get previous day browsing data for each User

            // Generate transactions from browsing data
            List<String> input = breakData();
            for (String s: input
                    ) {
                System.out.println(s);
            }
            // Generate rules
            //List<String> input = new ArrayList<String>();
        /*
        input.add("Noodles Pickles Milk");
        input.add("Noodles Cheese");
        input.add("Noodles Pickles Cheese");
        input.add("Noodles Pickles Clothes Cheese Milk");
        input.add("Pickles Clothes Milk");
        input.add("Pickles Milk Clothes");*/

            GenerateRules gr = new GenerateRules();
            ArrayList<JSONObject> rules = gr.generateRules(input);
            for(JSONObject rule:rules) {
                System.out.println(rule);
            }

            // Save rules in MongoDb



            mongoObject.disconnect();
        }
        catch(Exception ex) {
            if(ex.getClass().getName().equals("UnknownHostException")){
                mongoObject.disconnect();
                System.out.println("Error in connecting to MongoDb");
            }
        }
    }

    private static List<String> breakData(){
        //Dummy Data
        String s = "[" +
                "                {" +
                "                        \"T1\" : 1493624668716," +
                "                        \"T2\" : 1493624704594," +
                "                        \"hostname\" : \"mongodb.github.io\"," +
                "                        \"pathname\" : \"/node-mongodb-native/2.2/api/\"" +
                "                }," +
                "                {" +
                "                        \"T1\" : 1493624711112," +
                "                        \"T2\" : 1493624765099," +
                "                        \"hostname\" : \"angular-tutorial.quora.com\"," +
                "                        \"pathname\" : \"/Make-a-Todo-Chrome-Extension-with-AngularJS-1\"" +
                "                }," +
                "                {" +
                "                        \"T1\" : 1493624766449," +
                "                        \"T2\" : 1493624778930," +
                "                        \"hostname\" : \"angular-tutorial.quora.com\"," +
                "                        \"pathname\" : \"/Make-a-Todo-Chrome-Extension-with-AngularJS-1\"" +
                "                }," +
                "                {" +
                "                        \"T1\" : 1493624782293," +
                "                        \"T2\" : 1493624802577," +
                "                        \"hostname\" : \"newtab\"," +
                "                        \"pathname\" : \"/\"" +
                "                }," +
                "                {" +
                "                        \"T1\" : 1493624802578," +
                "                        \"T2\" : 1493624807890," +
                "                        \"hostname\" : \"www.google.com\"," +
                "                        \"pathname\" : \"/search\"" +
                "                }," +
                "                {" +
                "                        \"T1\" : 1493624811631," +
                "                        \"T2\" : 1493624838607," +
                "                        \"hostname\" : \"developer.chrome.com\"," +
                "                        \"pathname\" : \"/extensions/tabs\"" +
                "                }," +
                "                {" +
                "                        \"T1\" : 1493624838607," +
                "                        \"T2\" : 1493624884959," +
                "                        \"hostname\" : \"developer.chrome.com\"," +
                "                        \"pathname\" : \"/extensions/tabs\"" +
                "                }," +
                "                {" +
                "                        \"T1\" : 1493624884959," +
                "                        \"T2\" : 1493624897009," +
                "                        \"hostname\" : \"newtab\"," +
                "                        \"pathname\" : \"/\"" +
                "                }," +
                "                {" +
                "                        \"T1\" : 1493624897010," +
                "                        \"T2\" : 1493624908105," +
                "                        \"hostname\" : \"www.google.com\"," +
                "                        \"pathname\" : \"/search\"" +
                "                }," +
                "                {" +
                "                        \"T1\" : 1493624908106," +
                "                        \"T2\" : 1493624923002," +
                "                        \"hostname\" : \"www.google.com\"," +
                "                        \"pathname\" : \"/search\"" +
                "                }," +
                "                {" +
                "                        \"T1\" : 1493624923003," +
                "                        \"T2\" : 1493624955170," +
                "                        \"hostname\" : \"developer.chrome.com\"," +
                "                        \"pathname\" : \"/extensions/idle\"" +
                "                }," +
                "                {" +
                "                        \"T1\" : 1493624970728," +
                "                        \"T2\" : 1493624997953," +
                "                        \"hostname\" : \"www.responsivemiracle.com\"," +
                "                        \"pathname\" : \"/best-materialize-css-templates/\"" +
                "                }," +
                "                {" +
                "                        \"T1\" : 1493624997953," +
                "                        \"T2\" : 1493625006126," +
                "                        \"hostname\" : \"maxartkiller.in\"," +
                "                        \"pathname\" : \"/rock-on-materialize-responsive-admin-html-template/\"" +
                "                }," +
                "                {" +
                "                        \"T1\" : 1493625006127," +
                "                        \"T2\" : 1493625011257," +
                "                        \"hostname\" : \"maxartkiller.in\"," +
                "                        \"pathname\" : \"/website/rockon/rockon_blue/pages/index.html\"" +
                "                }," +
                "                {" +
                "                        \"T1\" : 1493531780065," +
                "                        \"T2\" : 1493532100139," +
                "                        \"hostname\" : \"newtab\"," +
                "                        \"pathname\" : \"/\"" +
                "                }" +
                "        ]";
        try{
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(s);
            JSONArray inputData = (JSONArray)obj;
            List<String> transactionList = new ArrayList<String>();
            HashMap<Integer, JSONObject> historyList = new HashMap<Integer, JSONObject>();
            for(Object urlHistory : inputData){
                JSONObject historyRecord = (JSONObject)urlHistory;
                addToSlot(historyList, historyRecord);
                Timestamp T1 = new Timestamp((long) historyRecord.get("T1"));
                Timestamp T2 = new Timestamp((long) historyRecord.get("T2"));
            }
            for(int i : historyList.keySet()){
                JSONObject tempJSON = historyList.get(i);
                StringBuilder tempTransaction = new StringBuilder();
                for(Object key : tempJSON.keySet()){
                    if((long)tempJSON.get(key) > 30) {
                        tempTransaction.append((String)key+" ");
                    }
                }
                if(tempTransaction.toString().length() != 0)
                    transactionList.add(tempTransaction.toString());
            }
            transactionList.set(1,"newtab mongodb.github.io");
            return transactionList;
        }
        catch(Exception ex) {
            System.out.println(ex.getClass().getName().toString());
            return new ArrayList<String>();
        }
    }

    private static void addToSlot(HashMap<Integer, JSONObject> historyList, JSONObject historyRecord){
        long t1 = (long) historyRecord.get("T1");
        long t2 = (long) historyRecord.get("T2");
        Timestamp T1 = new Timestamp(t1);
        Timestamp T2 = new Timestamp(t2);
        int key = T1.getHours();
        String url = (String)historyRecord.get("hostname");
        if(historyList.containsKey(key/2)){
            JSONObject tempJSON = (JSONObject) historyList.get(key/2);
            if(tempJSON.containsKey(url)){
                tempJSON.put(url, (long)tempJSON.get(url) + (t2-t1)/1000);
            }
            else{
                tempJSON.put(url, (t2-t1)/1000);
            }
        }
        else{
            JSONObject tempJSON = new JSONObject();
            tempJSON.put(url, (t2-t1)/1000 );
            historyList.put(key/2, tempJSON);
        }
    }
}
