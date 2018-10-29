package automatedsettle;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class SettleRunner {
    public static String level_1_txn  = "{\"merchid\":\"831831012001\",\"amount\":\"1000\",\"account\":\"344512208673714\",\"name\":\"cm\",\"expiry\":\"1299\",\"currency\":\"USD\",\"capture\":\"Y\"}";
    public static String level_2_txn = "{\"merchid\":\"831831831000\",\"amount\":\"2200\",\"cardtype\":\"VISA\",\"account\":\"4275330012345675\",\"expiry\":\"1299\",\"currency\":\"USD\",\"taxamnt\":\"5.00\",\"name\":\"CML2 Data\",\"ponumber\":\"jumanji001\",\"address\":\"123 Main St.\",\"city\":\"anytown\",\"region\":\"PA\",\"capture\":\"Y\"}";
    public  static String level_3_txn = "{\"merchid\":\"831831831000\",\"account\":\"341111597241002\",\"motoeci\":\"E\",\"expiry\":\"1299\",\"amount\":\"4430\",\"cvv2\":\"123\",\"currency\":\"USD\",\"name\":\"sample L3 test\",\"address\":\"10001 Continental Dr.\",\"city\":\"King of Prussia\",\"region\":\"PA\",\"country\":\"US\",\"postal\":\"19054\",\"capture\" : \"Y\",\"orderdate\" : \"01052016\", \"shipfromzip\" : \"19020\", \"taxamnt\" : \"5.00\", \"taxexempt\" : \"Y\", \"ponumber\" : \"ImBatman007\",\"items\":[{\"unitcost\":\"1.00\",\"description\":\"cm Item 8\",\"taxamnt\":\"0.10\",\"quantity\":\"2\",\"netamnt\":\"220\"}]}";

    public HttpsRequest request;
    public Map<String,String> payload = new Gson().fromJson(this.level_1_txn, new TypeToken<HashMap<String, String>>(){}.getType());
    public Map<String,String> payload2 = new Gson().fromJson(this.level_2_txn, new TypeToken<HashMap<String, String>>(){}.getType());
    public Map<String,Object> payload3 = new Gson().fromJson(this.level_3_txn, new TypeToken<HashMap<String, Object>>(){}.getType());


    public Gson json;
    public String batchid;
    public AutomatedSettle autoSettle;
    public String merchid = "";
    public String lineToWrite;


    public SettleRunner(String mid){
        this.payload.put("merchid", mid);
        this.payload2.put("merchid", mid);
        this.payload3.put("merchid", mid);
        this.request = new HttpsRequest();
        this.json =  new Gson();
        this.autoSettle = new AutomatedSettle();
        this.merchid = mid;
        this.lineToWrite = this.merchid;
        runTestCases();
    }

    public void setNewPayload(){
       //this.payload.put("merchid", this.merchid);
       this.payload.put("retref", this.request.retref);
    }
    public void authCapCloseBatch(Map payload){
        this.request.auth(this.json.toJson(payload));
        payload.put("retref", this.request.retref);
        this.request.closeBatch();
        this.autoSettle.watchGlog(this.request.batchid, this.merchid);
        this.lineToWrite = this.lineToWrite + ","+ this.request.batchid;
    }

    public void testCase1(Map payload){
        //Auth + Cap + CloseBatch
        System.out.println(" ");
        this.authCapCloseBatch(payload);
    }

    public void testCase2(Map payload){
        //FullRefund + CloseBatch for FullRefund
        System.out.println(" ");
        System.out.println("Test 2: full refund + closeBatch");
        this.request.refund(this.json.toJson(payload));
        this.request.closeBatch();
        this.autoSettle.watchGlog(this.request.batchid, this.merchid);
        this.lineToWrite = this.lineToWrite + ","+ this.request.batchid;
    }


    public void testCase3(Map payload){
        //Auth+Cap+CloseBatch + partial refund
        System.out.println(" ");
        System.out.println("Test 3: auth + capture + closeBatch + partial refund + closeBatch");
        payload.remove("retref");
        this.authCapCloseBatch(payload);
        payload.put("amount", 100);
        this.request.refund(this.json.toJson(payload));
        this.request.closeBatch();
        this.autoSettle.watchGlog(this.request.batchid, this.merchid);
        this.lineToWrite = this.lineToWrite+","+this.request.batchid;
    }

    public void testCase4(Map payload){
        //auth no cap, partial
        System.out.println(" ");
        System.out.println("Test 4: auth + no capture + void + capture + closeBatch");
        payload.remove("retref");
        payload.put("capture", "N");
        payload.put("amount", "4430");
        this.request.auth(this.json.toJson(payload));
        payload.put("amount", "1000");
        payload.put("merchid", this.merchid);
        payload.put("retref", this.request.retref);
        this.request.void_txn(this.json.toJson(payload));
        payload.put("merchid", this.merchid);
        payload.put("retref", this.request.retref);
        payload.remove("amount");
        this.request.capture(this.json.toJson(payload));
        this.request.closeBatch();
        this.autoSettle.watchGlog(this.request.batchid, this.merchid);
        this.lineToWrite = this.lineToWrite+","+this.request.batchid;
    }

    public void testCase5(Map payload){
        //force credit
        payload.put("amount", "-200");
        this.authCapCloseBatch(payload);
    }

    public void writeBIDMID(){
        this.autoSettle.write(this.lineToWrite);
    }

    public void runTestCases(){
        //this.deleteThis(this.payload);

       this.testCase1(this.payload);
       this.testCase2(this.payload);
       this.testCase3(this.payload);
       this.testCase4(this.payload);
       this.testCase1(this.payload2);
       this.testCase2(this.payload2);
       this.testCase3(this.payload2);
       this.testCase4(this.payload2);
       this.testCase1(this.payload3);
       this.testCase2(this.payload3);
       this.testCase3(this.payload3);
       this.testCase4(this.payload3);
        String lines = "-----------------------------------------------------------";
        System.out.println(lines);
        System.out.println("\tThread:"+Thread.currentThread().getName().trim() +" is DONE");
        System.out.println(lines);
    }


}
