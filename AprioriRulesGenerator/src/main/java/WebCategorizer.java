import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by maverick on 5/2/17.
 */
public class WebCategorizer {
    String categoriesFile = "categories.txt";
    String categoriesUrl = "http://sitereview.bluecoat.com/rest/categoryList?alpha=true";
    String k9License = "K9DFC8228A";

    public JSONObject fetchCategories(){
        try{
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
                JSONObject webCat = new JSONObject();
                for (Object currObj: inputData
                     ) {
                    JSONObject tempObj = (JSONObject)currObj;
                    webCat.put(tempObj.get("num"),tempObj.get("name"));
                }

                FileWriter file = new FileWriter(categoriesFile);
                file.write(webCat.toJSONString());
                file.flush();
                file.close();
            }
            else{
                System.out.println("Not working");
            }
            return new JSONObject();
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

    public void getWebCategories(String[] args){

    }
}
