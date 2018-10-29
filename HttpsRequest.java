package automatedsettle;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class HttpsRequest {
    public URL url;
    public HttpURLConnection con;
    public BufferedReader rd;
    public String line;
    public String result = "";
    public String userCredentials = "dGVzdGluZzp0ZXN0aW5nMTIz";
    public String retref = "";
    public String batchid = "";
    public String merchid= "";
    public String baseSpec = "https://qa.cardconnect.com:443/cardconnect/rest";

    public void get(String endpoint){

        try {

            this.url = new URL(this.baseSpec + "/" + endpoint);
           // System.out.print(" " + this.url.toString());
            this.con = (HttpsURLConnection)this.url.openConnection();
            this.con.setRequestProperty("Accept", "application/json");
            this.con.setRequestProperty("Authorization", "Basic " + this.userCredentials);
            this.con.setRequestMethod("GET");
            int response = this.con.getResponseCode();
            if(response != 200){
                //return "";

            }

            this.response();
            //this.rd.close();
        } catch (IOException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    public void put(String endpoint, String payload){

        try {
            //System.out.println("payload:        "+payload);
            this.url = new URL(this.baseSpec + "/"+ endpoint);
            //System.out.print("url: " + this.url.toString());
            this.con = (HttpURLConnection)this.url.openConnection();
            this.con.setDoOutput(true);
            this.con.setRequestMethod("PUT");
            this.con.setRequestProperty("Accept", "application/json");
            this.con.setRequestProperty("Content-Type", "application/json");
            this.con.setRequestProperty("Authorization", "Basic " + this.userCredentials);
            OutputStream out = this.con.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out);
            writer.write(payload);
            writer.flush();
            this.response();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void response(){
        try{
           // System.out.println(" -> "+ this.con.getResponseCode());
            BufferedReader in = new BufferedReader(new InputStreamReader(this.con.getInputStream()));
            String temp = null;
            StringBuilder sb = new StringBuilder();
            while((temp = in.readLine()) != null){
                sb.append(temp);
            }
            result = sb.toString();
            //System.out.println("result: "+ result);
            in.close();

            this.jsonify(result);
        } catch (Exception e){

        }

    }

    public void jsonify(String response){
        //System.out.println("resp:          " + response);

        Map<String,String> map = new Gson().fromJson(response, new TypeToken<HashMap<String, String>>(){}.getType());
        //System.out.println("JSONIFY" + map.toString());
        //do not delete these conditional statements
        if(map.get("batchid") == null){
            //System.out.println("batchid WAS NOT RETURNED FOR REQUEST");
        }

        if(map.get("merchid") == null){
            //System.out.println("merchid WAS NOT RETURNED FOR REQUEST");
        }


        this.batchid = map.get("batchid");
        this.retref = map.get("retref");
        this.merchid = map.get("merchid");

        //System.out.println(map.toString());
    }


    public void auth(String payload){
       // System.out.print("running auth... ");
        this.put("auth", payload);
    }

    public void closeBatch(){
       // System.out.print("closing batch... ");
        this.get("closebatch" + "/" + this.merchid);
    }

    public void capture(String payload){
       // System.out.println("running capture... ");
        this.put("capture", payload);
    }

    public void refund(String payload){
       // System.out.print("running refund... ");
        this.put("refund", payload);
    }

    public void void_txn(String payload){
       // System.out.println("running void... ");
        this.put("void", payload);
    }

    public static void main(String[] args) {

        //HttpsRequest https = new HttpsRequest();
       // https.get("https://qa.cardconnect.com:443/cardconnect/rest/closebatch/835484848444");
        //https.put("https://qa.cardconnect.com:443/cardconnect/rest/auth");
       //System.out.println(SettleRunner.level_3_txn);
       // https.auth(SettleRunner.level_3_txn);
        ///https.closeBatch();
        String lines = "|---------------------------------------|";
        System.out.println(lines);
        System.out.println("|\t" +"batch#3445 is now a good batch  \t|");
        System.out.println(lines);


        //            this.rd = new BufferedReader(
//                    new InputStreamReader(this.con.getInputStream()));
//            while ((this.line = this.rd.readLine()) != null) {
//                this.result += this.line;
//            }

    }


}


