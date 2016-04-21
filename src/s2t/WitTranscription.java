package s2t;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class WitTranscription implements Transcription {
    JSONObject trascrizione;

    public String transcript(String fileName) throws IOException {
        try {
            String charset = StandardCharsets.UTF_8.name();

            String url = "https://api.wit.ai/speech";
            String version = "20141022";
            String params = String.format("v=%s", URLEncoder.encode(version, charset));

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + PARAM.WitServerAPI);
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
            String text = (String) this.trascrizione.get("_text");
            if(text == null)
                 return "";
            return text;
        }
    }
}
