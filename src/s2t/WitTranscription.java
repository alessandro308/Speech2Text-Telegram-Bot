package s2t;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class WitTranscription implements Transcription {
    JSONObject trascrizione;

    public String transcript(String fileName) throws IOException {
        try {
            String params = "v=20141022";

            String url = "https://api.wit.ai/speech";
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + PARAM.witServerAPI);
            headers.put("Content-Type", "audio/wav");
            Post post = new Post(url, params, headers, Files.readAllBytes(Paths.get(fileName)));
            String res = post.execute();

            JSONParser p = new JSONParser();

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
