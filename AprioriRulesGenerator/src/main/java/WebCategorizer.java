import jdk.nashorn.internal.parser.JSONParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;

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
            con.setRequestProperty("User-agent", "Web Categorizer");
            int responseCode = con.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();



                JSONArray inputData = new JSONArray(response.toString());
                for (Object currObj: inputData
                     ) {
                    JSONObject tempObj = (JSONObject)currObj;
                    webCatList.put(String.format("%02X", tempObj.get("num")).toLowerCase(),tempObj.get("name"));
                }

                FileWriter file = new FileWriter(categoriesFile);
                file.write(webCatList.toString());
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
        try{
            BufferedReader br = new BufferedReader(new FileReader(categoriesFile));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            return new JSONObject(sb.toString());
        }
        catch (Exception ex){
            return new JSONObject();
        }
    }

    private String breakString(String s){
        return s.substring(0,2);
    }

    public HashMap<String, String> getWebCategories(HashSet<String> urlList){
        HashMap<String, String> urlCategoryList = new HashMap<String, String>();
        try{
            File file = new File(categoriesFile);
            JSONObject webCatList = new JSONObject();
            if(file.exists() && file.length() !=0){
                webCatList = loadCategories();
            }
            else{
                webCatList = fetchCategories();
            }
            for(String url : urlList){
                if(urlCategoryList.containsKey(url))
                    continue;
                String host = "";
                String port = "";
                if (url.indexOf(':') > -1) {
                    String[] urlAddr = url.split(":");
                    host = urlAddr[0];
                    port = urlAddr[1];
                } else {
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

                //Fetch Key for URL
                URL obj = new URL(remoteUrl.toString());
                URLConnection connection = obj.openConnection();

                Document doc = parseXML(connection.getInputStream());

                NodeList descNodes = doc.getElementsByTagName("DomC");
                if(descNodes.getLength()!=0){
                    String cat = (String) webCatList.get(breakString(descNodes.item(0).getTextContent().toLowerCase()));
                    cat.replace("\\/","/");
                    urlCategoryList.put(url, cat);
                }
                else{
                    descNodes = doc.getElementsByTagName("DirC");
                    String cat = (String) webCatList.get(breakString(descNodes.item(0).getTextContent().toLowerCase()));
                    cat.replace("\\/","/");
                    urlCategoryList.put(url, cat);
                }
            }
            return urlCategoryList;
        }
        catch(Exception ex) {
            System.out.println(ex.getClass().getName().toString());
            System.out.println("Not working");
            return urlCategoryList;
        }
    }

    private Document parseXML(InputStream stream)
            throws Exception
    {
        DocumentBuilderFactory objDocumentBuilderFactory = null;
        DocumentBuilder objDocumentBuilder = null;
        Document doc = null;
        try
        {
            objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
            objDocumentBuilder = objDocumentBuilderFactory.newDocumentBuilder();

            doc = objDocumentBuilder.parse(stream);
        }
        catch(Exception ex)
        {
            throw ex;
        }

        return doc;
    }
}
