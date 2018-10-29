import com.mysql.jdbc.StringUtils;

import javax.xml.crypto.Data;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

class DataRead {
    public String json = " ";
    public ConcurrentHashMap<String, String> map;
    public String[] tokens;

    public DataRead(){
        this.read();
    }

    public void read(){
        try{
            this.json = new String(Files.readAllBytes(Paths.get("payload.txt")));
            //tokenize();

        }catch (Exception e){
            System.out.println(e.toString());
        }
    }

    public String[] tokenize(){
        this.tokens = this.json.split(",");
        return this.tokens;
    }

    public static void main(String [] args){

        DataRead r = new DataRead();
        r.read();
        r.tokenize();

    }
}