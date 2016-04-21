package s2t;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Bot {
    final private String TOKEN = PARAM.botToken;
    private String url = "https://api.telegram.org/bot"+TOKEN+"/";
    static JSONParser parser = new JSONParser();
    int lastOffset = 857551151;
    static int trascrizioniEffettuate = 0;

    public void start() throws IOException {
        Bot.addLog("AVVIO");
        System.out.println("AVVIO");
        ExecutorService ex = Executors.newFixedThreadPool(6);
        File dir = new File("audio");
        if(!dir.exists()){
            try{
                dir.mkdir();
                File f = new File("bot_log.txt");
                f.createNewFile();
            }
            catch (SecurityException e){
                Bot.addLog("Non hai i permessi per creare la cartella audio");
                System.out.println("Non hai i permessi per creare la cartella audio");
                return;
            }
        }
        newIteration();

        while(true){
            int offset = lastOffset+1;
            URL update = new URL(url+"getUpdates?offset="+offset);
            JSONObject response = callJSON(update);
            JSONArray results = (JSONArray) response.get("result");
            lastOffset = getLastID(response);

            for(Object res : results)
                ex.submit(new TranscriptAudio(res, url));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Bot.addLog("Interruped Exception");
                System.out.println("Interruped Exception");
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
        BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
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
            addLog("Errore parsing risposta da "+url);
            return null;
        }
    }

    public static void main(String[] args){
        try {
            new Bot().start();
        } catch (Exception e) {
            addLog("ECCEZIONE \n"+e.getStackTrace());
            e.printStackTrace();
        }
    }

    public static synchronized void newIteration(){
        trascrizioniEffettuate++;
        if(trascrizioniEffettuate % 50 == 0){
            try {
                FileOutputStream out = new FileOutputStream("bot_log.txt", true);
                out.write(
                        ("Effettuata "+trascrizioniEffettuate+" al tempo UNIX "+
                                new java.util.Date(System.currentTimeMillis())+"\n").getBytes()
                        );
                out.close();
            } catch (IOException e) {
                e.getCause();
                System.err.println("Errore scrittura "+e.getMessage());
            }
        }
    }

    public static synchronized void addLog(String text){
        try {
            FileOutputStream out = new FileOutputStream("bot_log.txt", true);
            out.write(
                    (text+" at"+
                            new java.util.Date(System.currentTimeMillis())+"\n").getBytes()
            );
            out.close();
        } catch (IOException e) {
            e.getCause();
            System.err.println("Errore scrittura "+e.getMessage());
        }
    }
}
