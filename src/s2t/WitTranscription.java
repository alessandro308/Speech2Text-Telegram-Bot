package s2t;
/* CURL:
    curl -i -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: audio/wav" --data-binary "@amico.wav" 'https://api.wit.ai/speech?v=20141022'
*/
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;


public class WitTranscription implements Transcription {
    JSONObject trascrizione;

    public String transcript(String fileName) throws IOException {
        String params = "?v=20141022";
        String url = "https://api.wit.ai/speech"+params;

        File binfile = new File(fileName);

        PostMethod filePost = new PostMethod(url);

        filePost.addRequestHeader("Authorization", "Bearer " + PARAM.witServerAPI);
        filePost.addRequestHeader("Content-Type", "audio/wav");

        try {
            Part[] parts = {new FilePart(binfile.getName(), binfile)};

            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));

            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(15000);

            int status = client.executeMethod(filePost);

            if (status != HttpStatus.SC_OK) {
                System.out.println("Errore");
            }

            Scanner in = new Scanner(filePost.getResponseBodyAsStream(), "UTF-8");

            String res = "";
            while (in.hasNextLine()) {
                res += in.nextLine();
            }
            JSONParser p = new JSONParser();

            System.out.println("Risultato " + res);
            if (!res.equals(""))
                this.trascrizione = (JSONObject) p.parse(res);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            filePost.releaseConnection();
            return " ";
        }
    }

    public String getText() {
        if(trascrizione == null){
            return "";
        }else{
            System.out.println(trascrizione);
            String text = (String) this.trascrizione.get("_text");
            if(text == null)
                 return "";
            return text;
        }
    }
}
