package automatedsettle;

public class LocalCommands {
    Process process;

    public Process run_command(String cmd){
        try{
            this.process = Runtime.getRuntime().exec(cmd);
            return this.process;
        } catch (Exception e){
            System.out.println("Exception was thrown run_command" + e.toString());
        }
        return this.process;
    }
    public void cd(String dir) {run_command("cd" + dir);}
    public void smbUser(String user){ run_command("sudo  -i -u " + user); }
    public Process glog(){ return run_command("tail -20f /app/log/smb/gateway.log");}
    public Process runPy(String script_location){ return run_command(script_location); }
}
