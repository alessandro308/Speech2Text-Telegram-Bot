package s2t;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.omg.CORBA.NameValuePair;
//import sun.jvm.hotspot.debugger.cdbg.basic.BasicNamedFieldIdentifier;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;


import javax.swing.text.html.parser.Parser;
import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Scanner;

public class Bot {
    final private String TOKEN = PARAM.botToken;
    private String url = "https://api.telegram.org/bot"+TOKEN+"/";
    static JSONParser parser = new JSONParser();
    int x = 0;
    public void start() throws IOException {
        //Prendiamo l'ultimo update_ID
        int lastOffset = 857551151;

        while(true){
            JSONObject response = callJSON(new URL(url+"getUpdates?offset="+lastOffset));
            JSONArray results = (JSONArray) response.get("result");
            lastOffset = getLastID(response);

            for(Object res : results){
                JSONObject message = (JSONObject) ((JSONObject)res).get("message");
                JSONObject file = (JSONObject) (message.get("voice"));
                if(file != null){
                    JSONObject filePath = (JSONObject) callJSON(new URL(url+"getFile?file_id="+file.get("file_id"))).get("result");
                    URL fileToGet = new URL("https://api.telegram.org/file/bot"+TOKEN+"/"+filePath.get("file_path"));
                    //Download File
                    //System.out.println(fileToGet);
                    ReadableByteChannel rbc = Channels.newChannel(fileToGet.openStream());
                    FileOutputStream fos = new FileOutputStream("audio/"+file.get("file_id") + ".oga");
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    Process p = Runtime.getRuntime().exec("opusdec --rate 16000 "+"audio/"+file.get("file_id")+".oga"+" "+"audio/"+file.get("file_id")+".wav");
                    try {
                        p.waitFor();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Transcription google = new GoogleTranscription();
                    String text;

                    if(x == 0){ //DEBUG: eseguire solo una chiamata a Google per esecuzione
                        google.transcript("audio/"+file.get("file_id")+".wav");
                        text = google.getText();
                        x++; //SEMPRE PER DEBUG
                    }
                }
            }
            try {

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private int getLastID(JSONObject updateResult) {

        JSONParser parser = new JSONParser();
        Vector<Integer> returnarray = new Vector<>();

        JSONObject response = updateResult;

        JSONArray results = (JSONArray) response.get("result");
        return Integer.parseInt( (((JSONObject)results.get(results.size()-1)).get("update_id")).toString());
    }


    static String callString(URL url) throws IOException{
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String res = "";
        BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String nline;
        while(  (nline = bf.readLine()) != null ){
            res += nline;
        }
        return res;
    }

    static JSONObject callJSON(URL url) throws IOException{
        try {
            return (JSONObject) parser.parse(callString(url));
        } catch (ParseException e) {
            return null;
        }
    }

    public static void main(String[] args){
        try{
            new Bot().start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
