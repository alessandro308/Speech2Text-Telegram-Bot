package s2t;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class GoogleTranscription implements Transcription {
    JSONObject trascrizione;

    public String transcript(String fileName) throws IOException {
        String key = PARAM.GoogleSpeechAPIKey;
        String charset = StandardCharsets.UTF_8.name();

        String url = "https://www.google.com/speech-api/v2/recognize";
        String output = "json";
        String lang = "it-IT";
        String params = String.format("output=%s&lang=%s&key=%s",
                URLEncoder.encode(output, charset),
                URLEncoder.encode(lang, charset),
                URLEncoder.encode(key, charset));

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "audio/l16; rate=16000;");

        try{
            Post post = new Post(url, params, headers, Files.readAllBytes(Paths.get(fileName)));
            String res = post.execute();

            JSONParser p = new JSONParser();
            if(!res.equals("")){
                System.out.println(res);
                this.trascrizione = (JSONObject) p.parse(res);
                return res;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Bot.addLog("ECCEZIONE "+ ex.getMessage() +"\n"+ ex.getStackTrace());
        }
        return "";
    }

    public String getText(){
        if(trascrizione == null){
            return "";
        }else{
            JSONObject temp =
                    ((JSONObject)
                            ((JSONArray)
                                    ((JSONObject)
                                            ((JSONArray)
                                                    trascrizione.get("result")).get(0)).get("alternative")).get(0));
            return (String) temp.get("transcript");
        }
    }


}
