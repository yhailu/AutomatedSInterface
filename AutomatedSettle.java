package automatedsettle;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.sql.*;


public class AutomatedSettle {

    public AutomatedSettleRemote remote ;
    public LocalCommands cmd;
    public Process event;
    public String batchid;
    public String locationOfSettlementFile;
    public String merchID;
    public HttpsRequest request;
    public static FileWriter fw = null;
    public static BufferedWriter bw = null;
    public  List<String> fileLocations = new ArrayList<>();
    public String outputBidMidLocation = "output.dat";
    public String outputFileLocations ="autosettleFiles.txt";
    Connection conn;
    public String respProc;

    public void start() {
        this.remote = new AutomatedSettleRemote();
        this.request = new HttpsRequest();
        this.request.closeBatch();
        this.remote.batchid = this.request.batchid;
        this.batchid = this.request.batchid;
        this.merchID = this.request.merchid;
        System.out.println("batchid: " + this.batchid + "|" + "merchid: " + this.merchID);
        this.watchGlog(batchid, " ");
        //this.writeToFile();
    }

    public void watchGlog(String batchid, String mid) {

        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://vfq-db-1.ftscc.net:3306/qa2?useSSL=false&characterEncoding=UTF-8", "qa2", "9h3i8VXJMg8hRKSyuaCR");
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select * from batch where merchid ="+ mid + " and batchid =" + batchid);
            while(rs.next()){
                respProc = rs.getString("processor");
            }

        } catch (Exception e){
            System.out.println("SOMETHING WENT WRONG WHEN LOOKING FOR processor");
        }

        System.out.println("Batchid: " + batchid + " Merchid: " + mid + " Processor: " + respProc);
        this.remote = new AutomatedSettleRemote();
        this.cmd = new LocalCommands();
        this.cmd.smbUser("smb");
        this.event = this.cmd.glog();
        String newbatchid = batchid;
        String s = null;
        if(batchid.length() > 4){

            System.out.println("BATCHID EXCEEDS LENGTH OF 5!   --->" + batchid.length());
            System.out.println("Don't worry I got you covered. Spliting batchid for Regex");
            newbatchid = batchid.substring(1);
        }
        String regex1 = ".*?(\\."+newbatchid+ ")";
        //String regex2 = ".*?(\\.0"+batchid+")";
        String regex2 = "(?i).*0" + newbatchid;
        String regex3 = ".*\\."+ newbatchid+"\\.1$";
        String regex4 = ".*\\.0"+ newbatchid+"\\.1$";

        try {
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(this.event.getInputStream()));
            // read the output from the command
            //System.out.print(Thread.currentThread().getName()+": Listening to settlement logs.....                   \n");
            long startTime = System.currentTimeMillis();
            long endTime = startTime + 60*4000;

            search:
            {
                while (startTime < endTime) {
                    while ((s = stdInput.readLine()) != null) {
                        System.out.println("Thread:" + Thread.currentThread().getName().trim() + " reading settle logs.. for respProc: " + respProc + " and batchid: " + newbatchid);
                        Thread.sleep(50);
                        if (s.matches(regex1) || s.matches(regex2) || s.matches(regex3) || s.matches(regex4) && s.contains(respProc)) {
                            System.out.println(" ");
                            System.out.println("-------------------------------------------------------");
                            System.out.println(s);
                            this.locationOfSettlementFile = s;
                            this.remote.locationOfSettlementFile = s;
                            this.locationOfSettlementFile = this.remote.parseSettlementLog();
                            System.out.println(" ");
                            System.out.println("found location of your settlement file: " + this.locationOfSettlementFile);
                            this.fileLocations.add(this.locationOfSettlementFile);
                            System.out.println("-------------------------------------------------------");
                            break search;
                        }
                    }
                }
            }

            this.remote.dbConnect(batchid, mid);

        }
        catch(Exception e){
                System.out.println("exception happened - here's what I know: ");
                e.printStackTrace();
                System.exit(-1);
            }
        }

        public void writeToFile(String batchid, String mid, boolean newLine){
            try{

                    List<String> lines = Arrays.asList(mid + batchid);
                    Path file = Paths.get("output.dat");
                    Files.write(file, lines, Charset.forName("UTF-8"));
                    System.out.println(lines.toString());

                }catch (Exception e){
                System.out.println(e.toString());
                }

                try{

                    List<String> locations = Arrays.asList(this.locationOfSettlementFile);
                    Path fileLocations = Paths.get("locations.dat");
                    Files.write(fileLocations, locations, Charset.forName("UTF-8"));
                    System.out.println(locations.toString());


                }catch (Exception e){
                    System.out.println(e.toString());
                }
        }

        public void write(File file, String line, boolean newLine){
            try {
                // if file doesnt exists, then create it
                if (!file.exists()) {
                    file.createNewFile();
                }

                // true = append file
                fw = new FileWriter(file.getAbsoluteFile(), true);
                bw = new BufferedWriter(fw);
                bw.write(line);

                if (newLine == true) {
                    bw.newLine();
                }
                bw.close();
                fw.close();

            } catch (IOException e) {

                e.printStackTrace();

            } finally {

                try {

                    if (bw != null)
                        bw.close();

                    if (fw != null)
                        fw.close();

                } catch (IOException ex) {

                    ex.printStackTrace();

                }
            }

        }

        public void write(String bid, File file){

            try {

                // true = append file
                fw = new FileWriter(file.getAbsoluteFile(), true);
                bw = new BufferedWriter(fw);
                bw.append(bid);
                bw.close();
                fw.close();

            } catch (IOException e) {

                e.printStackTrace();

            }

        }

        public void writeMIDBID(String mid, String bid){
            File file = new File(this.outputBidMidLocation);

            this.write(file, mid + "," + bid, true);
        }

        public void writeFileLocation(){
            File file = new File(this.outputFileLocations);
            this.write(file, this.locationOfSettlementFile, true);
        }

        public void writeFileLocation(String location){
            File file = new File(this.outputFileLocations);
            this.write(file, location, true);
        }

        public void writeBIDMID(String bidmid){
            File file = new File(this.outputBidMidLocation);
            this.write(file, bidmid, true);
        }

        public void write(String lineToWrite){
            File file = new File(this.outputBidMidLocation);
            this.write(file, lineToWrite, true);
            //this.write(file, " " + bid, false);
        }

    }

    class AutomatedSettleRemote {
        public String mid;
        public String locationOfSettlementFile = "";
        public String methodName;
        public String batchid;
        public boolean showProgress = true;

        public void _progressBar() {
            this.methodName = new Object() {
            }.getClass().getEnclosingMethod().getName();
            String anim = " ";
            int b = 0;
            while (this.showProgress) {
                try {
                    String myDriver = "com.mysql.jdbc.Driver";
                    String myUrl = "jdbc:mysql://vfq-db-1.ftscc.net:3306/qa2?useSSL=false&characterEncoding=UTF-8";
                    Class.forName(myDriver);
                    Connection connection = DriverManager.getConnection(myUrl, "qa2", "9h3i8VXJMg8hRKSyuaCR");
                    Statement statement = connection.createStatement();
                    ResultSet rs = statement.executeQuery("select * from batch where merchid =" + this.mid + " and batchid =" + this.batchid);

                    while (rs.next()) {
                        if (rs.getString("hoststat").compareTo("GB") == 0) {
                            this.showProgress = false;
                            dbConnect(this.batchid, this.mid);
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Something went wrong in" + this.methodName + " " + e.toString());
                }
                if (this.showProgress == false) {
                    break;
                }
                System.out.println("Thread:" + Thread.currentThread().getName().trim() + " waiting for batch #" + this.batchid + " to change to 'Good Batch' "
                        + anim.substring(0, b++ % anim.length())
                        + "");

                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        }

        public void dbConnect(String batchid, String mid) {
            this.batchid = batchid;
            this.mid = mid;
            this.methodName = new Object() {
            }.getClass().getEnclosingMethod().getName();
            System.out.println();
            System.out.println("------------------" + methodName + "-----------------------");

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection("jdbc:mysql://vfq-db-1.ftscc.net:3306/qa2?useSSL=false&characterEncoding=UTF-8", "qa2", "9h3i8VXJMg8hRKSyuaCR");
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("select * from batch where merchid =" + mid +" and batchid =" + batchid);
                //System.out.println("Pinging DB ...");
                //_progressBar();
                while (rs.next()) {
                    //System.out.println("-----------------------------------------------");
                    //System.out.println("Row in DB => |" + " hoststat: " + rs.getString("hoststat") + "| " + "processor: " + rs.getString("processor") + "|" + " batchid: " + rs.getString("batchid"));
                    //System.out.println("-----------------------------------------------");
                    this.batchid = batchid;

                    if (rs.getString("hoststat").compareTo("GB") != 0) {
                        //this.showProgress=false;
                        _progressBar();

                    } else {
                        String lines = "-----------------------------------------------------------";
                        System.out.println(lines);
                        System.out.println("\tBatch #" + batchid + " is now a 'Good Batch'!\t".toUpperCase());
                        System.out.println(lines);
                    }
                }
                connection.close();
            } catch (Exception e) {
                System.out.println("Error in " + this.methodName + " : " + e.toString());
            }
        }

        public String parseSettlementLog() {
            String delims = "[ ]+";
            String[] Tokens = this.locationOfSettlementFile.split(delims);
            return Tokens[Tokens.length - 1];

        }
    }

