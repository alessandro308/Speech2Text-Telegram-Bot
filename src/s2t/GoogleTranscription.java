package s2t;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * Created by alessandro on 16/04/16.
 */
public class GoogleTranscription implements Transcription {
    JSONObject trascrizione;

    public GoogleTranscription(){};

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

        String params = "?output=json&lang=it-IT&key="+chromeDevKey;
        String url = "https://www.google.com/speech-api/v2/recognize"+params;

        File binfile = new File(fileName);

        PostMethod filePost = new PostMethod(url);

        filePost.setRequestHeader("Content-Type","audio/l16; rate=16000;");

        try {
            Part[] parts = { new FilePart(binfile.getName(), binfile) };

            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));

            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

            int status = client.executeMethod(filePost);

            if (status != HttpStatus.SC_OK) {
                System.out.println("Non e\' ok un cactus.");
            }

            Scanner in = new Scanner(filePost.getResponseBodyAsStream(), "UTF-8");

            String res = "";
            System.out.println("Primo ris: "+in.nextLine());//Salto la prima riga che è un {result: []}
            String line;
            while(in.hasNextLine()){
                    res += in.nextLine();
            }
            JSONParser p = new JSONParser();

            //Scarto primo carattere perchè è un EOF
            System.out.println("Risultato "+res);
            if(!res.equals(""))
                this.trascrizione = (JSONObject) p.parse(res);

        }catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            filePost.releaseConnection();
            return " ";
        }
    }

}
