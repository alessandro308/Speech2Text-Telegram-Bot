package s2t;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Vector;

public class Bot {
    final private String TOKEN = "195506532:AAFL384xjcf0N0KltcCWf3jsPAaVfJi62PE";
    private String url = "https://api.telegram.org/bot"+TOKEN+"/";
    static JSONParser parser = new JSONParser();

    public Bot() throws IOException {
        //Prendiamo l'ultimo update_ID
        int lastOffset = 0; getUpdateID().lastElement();

        while(true){

            JSONObject response = callJSON(new URL(url+"getUpdates?offset"+lastOffset));

            Vector<Integer> returnarray = new Vector<>();

            JSONArray results = (JSONArray) response.get("result");

            for(Object res : results){
                JSONObject message = (JSONObject) ((JSONObject)res).get("message");
                JSONObject file = (JSONObject) (message.get("voice"));
                if(file != null){
                    System.out.println(file);
                    
                }
            }
        }
    }

    private Vector<Integer> getUpdateID() {
        String line = "";
        try {
            URL getUpdate = new URL(url+"getUpdates");
            HttpURLConnection connection;
            connection = (HttpURLConnection) getUpdate.openConnection();
            connection.connect();
            BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String nline;
            while(  (nline = bf.readLine()) != null ){
                line += nline;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        JSONParser parser = new JSONParser();
        Vector<Integer> returnarray = new Vector<>();
        try {
            JSONObject response = (JSONObject) parser.parse(line);
            JSONArray results = (JSONArray) response.get("result");
            for(Object res : results){
               returnarray.add(Integer.parseInt( (((JSONObject)res).get("update_id")).toString()) );
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return returnarray;
    }


    static String callString(URL url) throws IOException{
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String res = "";
        BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String nline;
        while(  (nline = bf.readLine()) != null ){
            res += nline;
        }
        return res;
    }

    static JSONObject callJSON(URL url) throws IOException{
        try {
            return (JSONObject) parser.parse(callString(url));
        } catch (ParseException e) {
            return null;
        }
    }

    public static void main(String[] args){
        try{
            new Bot();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
