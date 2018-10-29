import automatedcompare.AutomatedCompare;
import automatedresettle.*;
import automatedsettle.*;
import javafx.concurrent.Worker;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;

public class Solution {

    public static FileWriter fw = null;
    public static BufferedWriter bw = null;
    public static LocalCommands lc= new LocalCommands();
    public static ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>();
    public static ConcurrentHashMap<String, List<String>> map2 = new ConcurrentHashMap<String, List<String>>();
    /**
     * flow : auth cap closebatch via restapi
     * go into settlement server and rename the file -->
     * save file location and name of the file
     * rename file ---- working on renaming convention(I was thinking XXX.level_1_fnor_transaction
     *
     *reset the batch <- you can use chris's utility
     *
     * then do the compare <- updater.java
     *
     *
     *
     * **/

    public static class MyRunnable implements Runnable{
        private String mid = null;
        MyRunnable(String mid){
            this.mid = mid;
        }
        @Override
        public void run(){
            try {

                    Thread.currentThread().setName(this.mid);
                    System.out.println("Thread for : " + Thread.currentThread().getName().trim() + " has started ...");
                    SettleRunner settleRunner = new SettleRunner(this.mid.trim());
                    map.put(Thread.currentThread().getName().trim(), settleRunner.lineToWrite);
                    map2.put(Thread.currentThread().getName().trim(), settleRunner.autoSettle.fileLocations);

            }catch (Exception e){
                System.out.println("Something went wrong MyRunnable. Here is what I know." + e.toString());
            }


        }
    }
    public static void main(String [] args) throws IOException {


        System.out.println("       ------------------------------------ Preparing AutoSettle---------------------------------------------");
        System.out.println("Please enter which portion of AutoSettle you'd like to run: ");
        System.out.println("1: Pre-Release Automated Settlement Testing ");
        System.out.println("2: Post-Release Automated Re-settlement Testing ");
        System.out.println("3: Post-Release Automated File Compare Testing ");


        ///String [] tokens;
        //DataRead midReader = new DataRead();
       // tokens = midReader.tokenize();
        //SettleRunner settleRunner;
        //for(String token:tokens){
        //    settleRunner = new SettleRunner(token.trim());
        //}

        try {
            while(true) {
                System.out.print("Enter Input: ");
                Scanner s = new Scanner(System.in);
                int input = s.nextInt();
                if (input == 1) {
                    System.out.println("Removing data files ...");
                    lc.run_command("rm output.dat");
                    lc.run_command("rm locations.dat");
                    String[] tokens;
                    DataRead midReader = new DataRead();
                    tokens = midReader.tokenize();
                    SettleRunner settleRunner;
                    for (String token : tokens) {
                        settleRunner = new SettleRunner(token.trim());
                    }
                    break;
                }

                else if (input == 0){
                    System.out.println("Removing data files ...");
                    lc.run_command("rm output.dat");
                    lc.run_command("rm autosettleFiles.txt");
                    String[] tokens;
                    AutomatedSettle settle = new AutomatedSettle();
                    DataRead midReader = new DataRead();
                    tokens = midReader.tokenize();
                    SettleRunner settleRunner;
                    final int MYTHREADS = tokens.length;
                    List<String> locations = new ArrayList<>();
                    //System.out.println(MYTHREADS);

                    ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);


//                    for (int i =0; i < tokens.length; i++){
//                        String token = tokens[i];
//                        Runnable worker = new MyRunnable(token);
//                        executor.execute(worker);
//                    }
                    for (String token: tokens){
                        Runnable worker = new MyRunnable(token);
                        executor.execute(worker);
                    }
                    executor.shutdown();
                    // Wait until all threads are finish
                    while (!executor.isTerminated()) {

                    }

                    System.out.println("Sorted List! Hopefully ....");
                    for (String token: tokens){
                        String key = token.trim();
                        String value = map.get(key).toString();
                        settle.writeBIDMID(value);
                        System.out.println(key + " : " + value);

                    }
                    for (String token: tokens){
                        String key = token.trim();
                        String value = map2.get(key).toString();
                        locations = map2.get(key);
                        for(String loc: locations){
                            settle.writeFileLocation(loc);
                        }
                        System.out.println(key + " : " + value);

                    }


                    System.out.println("printing map");
                    for(String name: map.keySet()){
                        String key = name.toString();
                        String value = map.get(name).toString();
                        System.out.println(key + " : " + value);
                    }

                    List<String> a = new ArrayList<String>();

                    for(String name: map2.keySet()){
                        String key = name.toString();
                        String value = map2.get(name).toString();
                        a = map2.get(name);
                        System.out.println(key + " : " + Arrays.toString(a.toArray()));
                    }
                    System.out.println("\nFinished all threads");

                    break;




                }
                 else if (input == 2) {
                    //implement automation of resettlement
                    s.close();
                    System.out.println("Input was 2 - Running Automated Post-Release Resettlement Tests\n");
                    progress.startProg();
                    System.out.println();

                    // initialize file details, clear old resettleFiles.txt
                    FileWriter fw = new FileWriter("resettleFiles.txt");
                    fw.write("");
                    fw.close();
                    String fileName = "output.dat";

                    //for loop to read next line and start next thread
                    List<String> lines = Files.readAllLines(new File(fileName).toPath(), Charset.defaultCharset());
                    CyclicBarrier barrier = new CyclicBarrier(lines.size()+1);
                    final ArrayList<AutomatedResettle> automatedResettles = new ArrayList<>();
                    for (int i = 0; i < lines.size(); i++)
                    {
                        final int index = i;
                        AutomatedResettle r = new AutomatedResettle();
                        automatedResettles.add(r);
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {

                                String line = lines.get(index);
                                String[] fileInput = line.split(",");

                                //initialize variables for new automatedresettle thread
                                String merchID = fileInput[0];
                                String firstBatch = fileInput[1];
                                String lastBatch = fileInput[fileInput.length-1];
                                r.automatedResettle(merchID, firstBatch, lastBatch);

                                try {
                                    barrier.await();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (BrokenBarrierException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        thread.start();
                    }
                    barrier.await();
                    for(AutomatedResettle ar :automatedResettles){
                        ar.writeSettleFiles(ar.fileList);
                    }
                    break;

                } else if (input == 3) {
                    //implement automated compare tool
                    File autosettleFile = new File("autosettleFiles.txt");
                    File resettleFile = new File("resettleFiles.txt");
                    System.out.println("Input was 3 - running automated compare");
                    progress.startProg();
                    System.out.println();
                    AutomatedCompare c = new AutomatedCompare();
                    c.automatedCompare(autosettleFile, resettleFile);
                    break;
                } else System.out.println("Input not recognized - please make selection again ");
                continue;
            }
        } catch (Exception e ){
            System.out.println(e.toString());
            System.out.println(e.fillInStackTrace());
            System.out.println("Please enter a valid number \n");
        }
    }


}
