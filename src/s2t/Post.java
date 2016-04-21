package s2t;

import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class Post {
    URL url;
    Map<String,String> header;
    byte[] postData;

    int responseCode;

    public Post (String url, String urlParameter, Map<String,String> header, byte[] postData){
        try{
            //Anche se <url> finisce già con un "?" non importa, un doppio "??" viene considerato come uno solo
            this.url= new URL(url+"?"+urlParameter);
        }catch (MalformedURLException ex){
            ex.printStackTrace();
        }
        this.header = header;
        this.postData = postData;
    }

    public String execute(){
        try{
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setDoInput(true);
            con.setDoOutput( true );
            con.setInstanceFollowRedirects( false );
            con.setRequestMethod("POST");
            for (Map.Entry<String, String> entry : header.entrySet())
                con.setRequestProperty(entry.getKey(),entry.getValue());
            con.setUseCaches( false );
            try( DataOutputStream wr = new DataOutputStream( con.getOutputStream())) {
                wr.write(postData);
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            String res = "";
            while ((line = rd.readLine()) != null)
                res += line;
            responseCode = con.getResponseCode();
            return res;

        }catch(IOException ex){
            ex.printStackTrace();
            responseCode= -1;
            return "";
        }
    }

    public int getResponseCode(){
        return responseCode;
    }
}
