package s2t;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.IOException;
import java.net.InterfaceAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

public class BingTranscription implements Transcription{

    JSONObject trascrizione;

    public String transcript(String fileName) throws IOException {
        String charset = StandardCharsets.UTF_8.name();

        /// --- PRENDO IL TOKEN ---
        //Stringa a cui fare la richiesta
        String tokenRequest = "https://oxford-speech.cloudapp.net/token/issueToken";

        //Popolo le variabili con il contenuto della richiesta
        String grant_type = "client_credentials";
        String client_id = PARAM.key1;
        String client_secret = PARAM.key2;
        String scope = "https://speech.platform.bing.com";

        //Costruisco la query con i parametri sopra popolati
        String tokenQuery = String.format("grant_type=%s&client_id=%s&client_secret=%s&scope=%s",
                URLEncoder.encode(grant_type, charset),
                URLEncoder.encode(client_id, charset),
                URLEncoder.encode(client_secret, charset),
                URLEncoder.encode(scope, charset));

        //Carico i dati da postare
        byte[] postData = tokenQuery.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;

        //Creo l'header
        Map tokenHeader = new HashMap<String,String>();
        tokenHeader.put("Content-Type", "application/x-www-form-urlencoded");
        tokenHeader.put("charset", "utf-8");
        tokenHeader.put( "Content-Length", Integer.toString( postDataLength ));

        //Eseguo la richiesta
        Post tokenPost = new Post(tokenRequest,tokenQuery,tokenHeader,postData);
        String token = tokenPost.execute();

        JSONParser p = new JSONParser();
        JSONObject tokenjson;
        try{
            tokenjson = (JSONObject)p.parse(token);
        }catch (ParseException ex){
            ex.printStackTrace();
            return "";
        }
        token = (String)tokenjson.get("access_token");
        System.out.println(token);


        /// ######### Richiesta di trascrizione #########

        //Url dove fare la richiesta
        String requestUrl = "https://speech.platform.bing.com/recognize/query";

        //Popolo le variabili con il contenuto della richiesta
        String version = "3.0";
        String requestid = UUID.randomUUID().toString(); //uuid da generare random
        String appID = "D4D52672-91D7-4C74-8AD8-42B1D98141A5"; //a Magic string that's needed
        String format = "json"; //json or xml
        String locale = "it-IT";
        String deviceos = "wp7"; //Because we don't want a sad Microsoft :)
        String scenarios = "ulm";
        String instanceid = UUID.randomUUID().toString(); //uuid da generare random
        String profanitymarkup = "0"; //We like profanity :D

        //Costruisco la query con i parametri sopra popolati
        String query = String.format("version=%s&requestid=%s&appID=%s&format=%s&locale=%s&device.os=%s&scenarios=%s&instanceid=%s&result.profanitymarkup=%s",
                URLEncoder.encode(version, charset),
                URLEncoder.encode(requestid, charset),
                URLEncoder.encode(appID, charset),
                URLEncoder.encode(format, charset),
                URLEncoder.encode(locale, charset),
                URLEncoder.encode(deviceos, charset),
                URLEncoder.encode(scenarios, charset),
                URLEncoder.encode(instanceid, charset),
                URLEncoder.encode(profanitymarkup, charset));

        byte[] audioFile = Files.readAllBytes(Paths.get(fileName));
        int audioFileLength = audioFile.length;

        Map header = new HashMap<String,String>();
        header.put("Authorization", "Bearer " + token);
        header.put("Content-Type", "audio/wav; samplerate=16000; sourcerate=8000; trustsourcerate=true");
        header.put("Content-Length", Integer.toString(audioFileLength));

        Post post = new Post(requestUrl,query,header,audioFile);
        String res= post.execute();

        JSONParser p2 = new JSONParser();
        try{
            if (!res.equals(""))
                this.trascrizione = (JSONObject) p2.parse(res);
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
            //Mi merito l'inferno per questa riga, mi dispiace
            String text = (String)((JSONObject)(((JSONArray)this.trascrizione.get("results")).get(0))).get("name");
            if(text == null)
                return "";
            return text;
        }
    }
}
