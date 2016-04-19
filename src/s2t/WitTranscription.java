package s2t;
/* CURL:
    curl -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: audio/wav" --data-binary "@amico.wav" 'https://api.wit.ai/speech?v=20141022'
*/

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;


public class WitTranscription implements Transcription {
    JSONObject trascrizione;

    public String transcript(String fileName) throws IOException {
        try {
            String params = "?v=20141022";

            String url = "https://api.wit.ai/speech" + params;

            HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.addRequestProperty("Authorization", "Bearer " + PARAM.witServerAPI);
            con.addRequestProperty("Content-Type", "audio/wav");

            con.setDoInput(true);
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(Files.readAllBytes(Paths.get(fileName)));

            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            String res = "";
            while ((line = rd.readLine()) != null)
                res += line;

            JSONParser p = new JSONParser();

            //Scarto primo carattere perchè è un EOF
            //System.out.println("Risultato " + res);
            if (!res.equals(""))
                this.trascrizione = (JSONObject) p.parse(res);
            Bot.newIteration();
            return res;
        } catch (ParseException ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public String getText() {
        if(trascrizione == null){
            return "";
        }else{
            //System.out.println(trascrizione);
            String text = (String) this.trascrizione.get("_text");
            if(text == null)
                 return "";
            return text;
        }
    }
}
