package s2t;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static s2t.Bot.callJSON;

/**
 * Created by alessandro on 19/04/16.
 */
public class TranscriptAudio implements Runnable {
    String url;

    public TranscriptAudio(Object res, String telegramUrl) throws IOException {
        url = telegramUrl;
        JSONObject message = (JSONObject) ((JSONObject)res).get("message");
        JSONObject file = (JSONObject) (message.get("voice"));

        JSONObject chat = (JSONObject) message.get("chat");
        Long chatID = (Long) chat.get("id");

        if(file != null){
            JSONObject filePath = (JSONObject) callJSON(new URL(url+"getFile?file_id="+file.get("file_id"))).get("result");
            URL fileToGet = new URL("https://api.telegram.org/file/bot"+PARAM.botToken+"/"+filePath.get("file_path"));
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

            Transcription trasc = new WitTranscription();
            String text;

            try{
                trasc.transcript("audio/"+file.get("file_id")+".wav");
                text = trasc.getText();
                sendMessage(chatID, text);
            } catch (NullPointerException e){
                sendMessage(chatID, "Errore interno al server (NullPointerException)");
                e.printStackTrace();
            }

            new File("audio/"+file.get("file_id")+".oga").delete();
            new File("audio/"+file.get("file_id")+".wav").delete();
        }
    }

    private String sendMessage(Long chat_id, String text){
        try {
            if(!text.equals(""))
                return Bot.callString(new URL(url+"sendMessage?chat_id="+chat_id+"&text="+ URLEncoder.encode(text, "UTF-8")));
            else
                return Bot.callString(new URL(url+"sendMessage?chat_id="+chat_id+"&text="+URLEncoder.encode("Riprova. Non sono riuscito a tradurre.", "ISO-8859-1")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    @Override
    public void run() {

    }
}
