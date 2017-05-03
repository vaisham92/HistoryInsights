import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by maverick on 5/2/17.
 */
public class WebCategorizer {
    String categoriesFile = "categories.txt";
    String categoriesUrl = "http://sitereview.bluecoat.com/rest/categoryList?alpha=true";
    String k9License = "K9DFC8228A";

    public JSONObject fetchCategories(){
        try{
            JSONObject webCatList = new JSONObject();
            URL obj = new URL(categoriesUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-agent", "Web Category Agent");
            int responseCode = con.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();



                JSONParser parser = new JSONParser();
                Object jsonObj = parser.parse(response.toString());
                JSONArray inputData = (JSONArray)jsonObj;
                for (Object currObj: inputData
                     ) {
                    JSONObject tempObj = (JSONObject)currObj;
                    webCatList.put(tempObj.get("num"),tempObj.get("name"));
                }

                FileWriter file = new FileWriter(categoriesFile);
                file.write(webCatList.toJSONString());
                file.flush();
                file.close();
            }
            else{
                System.out.println("Not working");
            }
            return webCatList;
        }
        catch(Exception ex){
            return new JSONObject();
        }
    }

    public JSONObject loadCategories(){
        JSONParser parser = new JSONParser();
        try{
            Object obj = parser.parse(new FileReader(categoriesFile));
            JSONObject jsonObject = (JSONObject) obj;
            System.out.println(jsonObject);
            return jsonObject;
        }
        catch (Exception ex){
            return new JSONObject();
        }
    }

    public void getWebCategories(ArrayList<String> urlList){
        File file = new File(categoriesFile);
        JSONObject webCatList = new JSONObject();
        if(file.exists()){
            webCatList = loadCategories();
        }
        else{
            webCatList = fetchCategories();
        }

        for(String url : urlList){
            String host = "";
            String port = "";
            if(url.indexOf(':') > -1){
                String[] urlAddr = url.split(":");
                host = urlAddr[0];
                port = urlAddr[1];
            }
            else{
                host = url;
                port = "80";
            }
            StringBuilder remoteUrl = new StringBuilder("http://sp.cwfservice.net/1/R/");
            remoteUrl.append(k9License);
            remoteUrl.append("/K9-00006/0/GET/HTTP/");
            remoteUrl.append(host);
            remoteUrl.append("/");
            remoteUrl.append(port);
            remoteUrl.append("///");
            System.out.println(remoteUrl.toString());
        }
    }
}
