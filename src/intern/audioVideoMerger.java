package intern;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;

public class audioVideoMerger {

		 public boolean merge(String file_name,String[] exeCmd) {
			 

		 ProcessBuilder pb = new ProcessBuilder(exeCmd);				//initiating a ProcessBuilder class
		 boolean exeCmdStatus = executeCMD(pb);							//passing pb to executeCMD

		 return exeCmdStatus;
		}

		private boolean executeCMD(ProcessBuilder pb)
		{
		 pb.redirectErrorStream(true);									
		 Process p = null;

		 try {
		  p = pb.start();													//try to start the process

		 } catch (Exception ex) {
		 ex.printStackTrace();
		 System.out.println("oops");
		 p.destroy();														//if exception then destroy p
		 return false;
		}
		// wait until the process is done
		try {
		 p.waitFor();
		} catch (InterruptedException e) {
		e.printStackTrace();
		System.out.println("woopsy");									//if exception then destroy p
		p.destroy();															
		return false;
		}
		
		
		return true;
		 }
		}

		