package s2t;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by alessandro on 16/04/16.
 */
public class GoogleTranscription implements Transcription {

    public GoogleTranscription(){};

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

            if (status == HttpStatus.SC_OK) {
                System.out.println("TUTTO OK");
            } else {
                System.out.println("Non e\' ok un cactus.");
            }

            Scanner in = new Scanner(filePost.getResponseBodyAsStream());

            String res = "";
            while(in.hasNext()){
                res += in.nextLine();
            }
            return res;
        }catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            filePost.releaseConnection();
        }

        return "";
    }

}
