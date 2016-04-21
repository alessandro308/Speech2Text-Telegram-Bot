package s2t;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class GoogleTranscription implements Transcription {
    JSONObject trascrizione;

    public String getText(){
        if(trascrizione == null){
            return "";
        }else{
            //Parsa JSON
            JSONObject temp = ((JSONObject) ((JSONArray) ((JSONObject) ((JSONArray) trascrizione.get("result")).get(0)).get("alternative")).get(0));
            return (String) temp.get("transcript");
        }
    }

    public String transcript(String fileName) throws IOException {
        String chromeDevKey = PARAM.chromeKey1;

        String params = "output=json&lang=it-IT&key="+chromeDevKey;
        String url = "https://www.google.com/speech-api/v2/recognize";

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

}
