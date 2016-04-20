package s2t;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

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

        //Creo la richiesta
        //NB: Setto la Content-Lenght a mano, Microzozz la richiede
        byte[] postData = tokenQuery.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;
        URL tokenUrl = new URL( tokenRequest );
        HttpsURLConnection con = (HttpsURLConnection) tokenUrl.openConnection();
        con.setDoOutput( true );
        con.setInstanceFollowRedirects( false );
        con.setRequestMethod( "POST" );
        con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty( "charset", "utf-8");
        con.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
        con.setUseCaches( false );
        try( DataOutputStream wr = new DataOutputStream( con.getOutputStream())) {
            wr.write( postData );
        }

        BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String token = rd.readLine();

        JSONParser p = new JSONParser();
        JSONObject tokenjson;
        try{
            tokenjson = (JSONObject)p.parse(token);
        }catch (ParseException ex){
            ex.printStackTrace();
            return "";
        }

        token = (String)tokenjson.get("access_token");

        /// --- Eseguo l'effettiva richiesta ---
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

        String requestUrl = "https://speech.platform.bing.com/recognize/query?" + query;

        HttpsURLConnection reqcon = (HttpsURLConnection) new URL(requestUrl).openConnection();
        reqcon.setRequestMethod("POST");
        reqcon.addRequestProperty("Authorization", "Bearer " + token);
        reqcon.addRequestProperty("Content-Type", "audio/wav; samplerate=16000; sourcerate=8000; trustsourcerate=true");

        reqcon.setDoInput(true);
        reqcon.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(reqcon.getOutputStream());
        wr.write(Files.readAllBytes(Paths.get(fileName)));

        BufferedReader rd2 = new BufferedReader(new InputStreamReader(reqcon.getInputStream()));
        String line;
        String res = "";
        while ((line = rd2.readLine()) != null)
            res += line;

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
