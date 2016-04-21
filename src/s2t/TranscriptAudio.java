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

public class TranscriptAudio implements Runnable {
    String url;
    Object res;
    public TranscriptAudio(Object res, String telegramUrl) throws IOException {
        url = telegramUrl;
        this.res = res;
    }

    private String sendMessage(Long chat_id, String text) throws IOException {
        try {
            if(!text.equals(""))
                return Bot.callString(new URL(url+"sendMessage?chat_id="+chat_id+"&text="+ URLEncoder.encode(text, "UTF-8")));
            else
                return Bot.callString(new URL(url+"sendMessage?chat_id="+chat_id+"&text="+URLEncoder.encode("Riprova. Non sono riuscito a tradurre.", "ISO-8859-1")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return "";
    }
    @Override
    public void run() {
        JSONObject message = (JSONObject) ((JSONObject)res).get("message");
        boolean forward = (message.get("forward_from") != null);

        try{
            JSONObject voice = (JSONObject) (message.get("voice"));
            if(voice != null){

                if(!downloadAndConvert(voice))
                    return;
                transcript(message, voice, forward);

            }else{
                JSONObject document = (JSONObject) (message.get("document"));
                if(document != null){
                    try {
                        if (document.get("mime_type").equals("application/octet-stream")) {
                            if(!downloadAndConvert(document))
                                return;
                            transcript(message, document, forward);
                        }
                    } catch (NullPointerException e){
                        return;
                    }
                }
            }
        } catch (IOException e) {
            Bot.addLog("ECCEZIONE \n"+e.getStackTrace());
        }
    }

    private boolean downloadAndConvert(JSONObject voice){
        JSONObject filePath;
        URL fileToGet;
        ReadableByteChannel rbc;
        try {
            filePath = (JSONObject) callJSON(new URL(url+"getFile?file_id="+voice.get("file_id"))).get("result");
            fileToGet = new URL("https://api.telegram.org/file/bot"+ PARAM.botToken+"/"+filePath.get("file_path"));
            rbc = Channels.newChannel(fileToGet.openStream());
        } catch (MalformedURLException e) {
            System.err.println("Errore formattazione url richiesta file Telegram");
            return false;
        } catch (IOException e){
            return false;
        }

        FileOutputStream fos;
        try {
            fos = new FileOutputStream("audio/"+voice.get("file_id") + ".oga");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Process p = Runtime.getRuntime().exec("opusdec --rate 16000 "+"audio/"+voice.get("file_id")+".oga"+" "+"audio/"+voice.get("file_id")+".wav");
            p.waitFor();
            return true;
        } catch (InterruptedException e) {
            return false;
        } catch ( IOException e){
            System.err.println("Errore opusdec execution");
            return false;
        }
    }

    private void transcript(JSONObject message, JSONObject voice, boolean forward) throws IOException {
        JSONObject chat = (JSONObject) message.get("chat");
        Long chatID = (Long) chat.get("id");

        //SCEGLIERE SERVIZIO
        Transcription trasc = new WitTranscription();
        //Transcription trasc = new GoogleTranscription();
        String text = "";

        try{
            trasc.transcript("audio/"+voice.get("file_id")+".wav");
            if(forward){
                String name = (String) ((JSONObject) message.get("forward_from")).get("first_name");
                if( name != null)
                    text += name+" ha detto: \n";
            }
            text += trasc.getText();
            sendMessage(chatID, text);
        } catch (NullPointerException e){
            sendMessage(chatID, "Errore interno al server (NullPointerException)");
            e.printStackTrace();
        }

        new File("audio/"+voice.get("file_id")+".oga").delete();
        new File("audio/"+voice.get("file_id")+".wav").delete();
    }
}
