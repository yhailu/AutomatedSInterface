package automatedsettle;

import java.util.Arrays;

public class ProgressBar {

    private static String[] s;
    private int pos, inc, size;


    public ProgressBar(int size, int increment) {
        this.size = size;
        increment = increment;
        s = new String[size+2];
        Arrays.fill(s,"a");
        s[0] = "[";
        s[size+1] = "]";
    }

    public void update() {
        System.out.println('\r');
        if (pos+inc<size+2) {
            Arrays.fill(s,pos,pos+inc,"c");
            pos += inc;
        }
        for (String ch : s) {
            System.out.print(ch);
        }
    }

    public void finish() {
        System.out.println();
    }



    public static void main(String[] args) {
        try {
            String anim = "|/-\\";
            for (int x = 0; true; x++) {
                String data = "\r" + anim.charAt(x % anim.length()) + " " + x;
                System.out.write(data.getBytes());
                Thread.sleep(200);
            }
        }catch (Exception e){
            System.out.println(e.toString());
        }
    }


}