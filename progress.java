import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class progress {




    public static void main(String[] args) {

        try {


            String anim = "|/-\\";
            for (int x = 0; x < 100; x++) {
                String data = "\r" + anim.charAt(x % anim.length()) + " " ;
                System.out.write(data.getBytes());
                Thread.sleep(100);
            }
        }catch(Exception e){

        }
    }

    public static void startProg(){
        long total = 50;
        long startTime = System.currentTimeMillis();

        for (int i = 2; i <= total; i = i + 3) {
            try {
                Thread.sleep(100);
                printProgress(startTime, total, i);
            } catch (InterruptedException e) {
            }
        }
    }


    private static void printProgress(long startTime, long total, long current) {
        long eta = current == 0 ? 0 :
                (total - current) * (System.currentTimeMillis() - startTime) / current;

        String etaHms = current == 0 ? "N/A" :
                String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
                        TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

        StringBuilder string = new StringBuilder(140);
        int percent = (int) (current * 100 / total);
        string
                .append('\r')
                .append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
                .append(String.format(" %d%% [", percent))
                .append(String.join("", Collections.nCopies(percent, "=")))

                .append('>')
                .append(String.join("", Collections.nCopies(100 - percent, " ")))
                .append(']')
                .append(String.join("", Collections.nCopies(current == 0 ? (int) (Math.log10(total)) : (int) (Math.log10(total)) - (int) (Math.log10(current)), " ")))
                .append(String.format(" %d/%d, ETA: %s", current, total, etaHms));

        System.out.print(string);
    }


}

class Test
{
    public static void main(String[] args) throws Exception
    {
        String regex = "FOMS Dump to file /app/jboss/smb/cardconnect/settle/FOMA20180830.006001.0205.1";
        String regex1 = "FOMS Dump to file /app/jboss/smb/cardconnect/settle/FOMA20180830.006001.0206.1";

        String txt2 = "08-30-2018 15:47:35,290 TRACE [CC] (OmahaSettle) FOMS Dump to file /app/jboss/smb/cardconnect/settle/FOMA20180830.006001.0206.1";
        String txt3 = "08-30-2018 15:49:09,573 INFO  [CORE] (Timer-2) Manager added system property pps.cardsecure.api.kp.allow.ips=127.0.0.1 10.1.25.1 10.1.25.201";

        String re1=".*?";	// Non-greedy match on filler
        String re2="([+-]?\\d*\\.\\d+)(?![0205\\.])";



        String txt="FOMA20180830.006001.0205.1";
        if(txt2.matches(re1+re2) && txt2.contains("Dump")){
            //System.out.println("FSDFSDFSDFSDFSAFS");
        }

        String batch = "0206";
        String endAnchor = ".*\\."+ batch+"\\.1"+ "$";

        if(txt3.matches(endAnchor)){
            System.out.println("perfect match!");
        }



    }
}

