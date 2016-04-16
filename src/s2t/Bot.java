package s2t;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import java.nio.charset.Charset;
import java.util.Vector;


public class Bot {
    final private String TOKEN = PARAM.botToken;
    private String url = "https://api.telegram.org/bot"+TOKEN+"/";
    static JSONParser parser = new JSONParser();
    int lastOffset = 857551151;

    public void start() throws IOException {
        //Prendiamo l'ultimo update_ID

        while(true){
            int offset = lastOffset+1;
            URL update = new URL(url+"getUpdates?offset="+offset);
            JSONObject response = callJSON(update);
            JSONArray results = (JSONArray) response.get("result");
            lastOffset = getLastID(response);

            for(Object res : results){
                JSONObject message = (JSONObject) ((JSONObject)res).get("message");
                JSONObject file = (JSONObject) (message.get("voice"));

                JSONObject chat = (JSONObject) message.get("chat");
                Long chatID = (Long) chat.get("id");

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
                    Transcription trasc = new GoogleTranscription();
                    String text;

                    trasc.transcript("audio/"+file.get("file_id")+".wav");
                    text = trasc.getText();
                    sendMessage(chatID, text);
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
        if (results.isEmpty())
            return this.lastOffset;
        return Integer.parseInt( (((JSONObject)results.get(results.size()-1)).get("update_id")).toString());
    }


    static String callString(URL url) throws IOException{
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String res = "";
        BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("ISO-8859-1")));
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

     private String sendMessage(Long chat_id, String text){
        try {
            if(!text.equals(""))
                return callString(new URL(url+"sendMessage?chat_id="+chat_id+"&text="+URLEncoder.encode(text, "ISO-8859-1")));
            else
                return callString(new URL(url+"sendMessage?chat_id="+chat_id+"&text="+URLEncoder.encode("Riprova. Non sono riuscito a tradurre.", "ISO-8859-1")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
         return "";
     }

    public static void main(String[] args){
        try {
            new Bot().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
