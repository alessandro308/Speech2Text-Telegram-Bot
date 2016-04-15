package s2t;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.omg.CORBA.NameValuePair;
import sun.jvm.hotspot.debugger.cdbg.basic.BasicNamedFieldIdentifier;


import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class Bot {
    final private String TOKEN = "195506532:AAFL384xjcf0N0KltcCWf3jsPAaVfJi62PE";
    private String url = "https://api.telegram.org/bot"+TOKEN+"/";
    static JSONParser parser = new JSONParser();

    public Bot() throws IOException {
        //Prendiamo l'ultimo update_ID
        int lastOffset = 0;  getUpdateID().lastElement();

        while(true){

            JSONObject response = callJSON(new URL(url+"getUpdates?offset"+lastOffset));
            JSONArray results = (JSONArray) response.get("result");

            for(Object res : results){
                JSONObject message = (JSONObject) ((JSONObject)res).get("message");
                JSONObject file = (JSONObject) (message.get("voice"));
                if(file != null){
                    JSONObject filePath = (JSONObject) callJSON(new URL(url+"getFile?file_id="+file.get("file_id"))).get("result");
                    URL fileToGet = new URL("https://api.telegram.org/file/bot"+TOKEN+"/"+filePath.get("file_path"));
                    //Download File
                    //System.out.println(fileToGet);
                    ReadableByteChannel rbc = Channels.newChannel(fileToGet.openStream());
                    FileOutputStream fos = new FileOutputStream(file.get("file_id") + ".oga");
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    Process p = Runtime.getRuntime().exec("opusdec --rate 16000 "+file.get("file_id")+".oga"+" "+file.get("file_id")+".wav");
                    try {
                        p.waitFor();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(googleTranscription(file.get("file_id")+".wav"));
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private Vector<Integer> getUpdateID() {
        String line = "";
        try {
            URL getUpdate = new URL(url+"getUpdates");
            HttpURLConnection connection;
            connection = (HttpURLConnection) getUpdate.openConnection();
            connection.connect();
            BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String nline;
            while(  (nline = bf.readLine()) != null ){
                line += nline;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        JSONParser parser = new JSONParser();
        Vector<Integer> returnarray = new Vector<>();
        try {
            JSONObject response = (JSONObject) parser.parse(line);
            JSONArray results = (JSONArray) response.get("result");
            for(Object res : results){
               returnarray.add(Integer.parseInt( (((JSONObject)res).get("update_id")).toString()) );
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return returnarray;
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

    static JSONObject googleTranscription(String fileName) throws IOException {
        String chromeDevKey = "AIzaSyCx0Wgvm954KQnHRG7SYJxysjSW59oQ1yk";
        String url = "https://www.google.com/speech-api/v2/";
        String params = "recognize?output=json&lang=it-IT&key="+chromeDevKey;

        File binfile = new File(fileName);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "audio/l16");
            conn.setRequestProperty("rate", "16000");

            conn.setDoOutput(true);
            conn.setDoInput(true);
            DataOutputStream paramConn = new DataOutputStream(conn.getOutputStream());
            paramConn.writeBytes(params);
            paramConn.flush();
            paramConn.close();

            OutputStream out = conn.getOutputStream();
            Files.copy(binfile.toPath(), out);
            out.flush();
            System.out.println(conn.getResponseMessage());

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    public static void main(String[] args){
        try{
            new Bot();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
