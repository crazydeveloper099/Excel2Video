package intern;


import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.awt.Color;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.Voice;
import com.amazonaws.util.IOUtils;


import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class selector {
	
//Basic credentials and deceleration Variables.	
	public static final String access_key_id="**************";                            //AWS CREDINTIALS HAVING ACCESS FOR POLY ROLE.
	public static final String secret_access_key="************************";	//AWS CREDINTIALS HAVING ACCESS FOR POLY ROLE.
	
	static AmazonPollyClient polly;																//Declaring GOLOBAL AmazonPollyClient variable
	static Voice voice;																			//Declaring GOLOBAL Voice variable which is part of AWS Polly library
	
	static int row=1;																			//Integer variable row will store the current row values 
	
	static boolean all_rows=false;																//This variable will store if you selected same voice for all the rows or not
	static int def_voice_no=0;																	//If you selected a voice for all the rows it will store that
	
	static File f=null;																			//this var will create a local text file which will store the name of files to be merged in a row
	
	
	//This constructor is provided by AWS to select the region, voice , and to validate the AWS credentials.
	
	public selector(Region region,int voice_no) {											
		BasicAWSCredentials cred=new BasicAWSCredentials(access_key_id,secret_access_key);		//validating AWS credentials. 
		polly = new AmazonPollyClient(cred, new ClientConfiguration());							//Init polly.
		polly.setRegion(region);																//setting polly region.
		
		DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();				
		DescribeVoicesResult describeVoicesResult = polly.describeVoices(describeVoicesRequest);
		voice = describeVoicesResult.getVoices().get(voice_no);									//se.tting voice_no to voice to get user selected voice for each row
		
		}
		
	
	
	
	//Main function starts here
	
	public static void main(String args[]) throws IOException, JavaLayerException, UnsupportedAudioFileException
	{
		
		 System.out.println();
        System.out.println("Please select an excel file.");
        System.out.println();
        try
        {
		JFileChooser chooser = new JFileChooser();												//JFileChooser to open the dialog box initially to let user choose excel file.
        FileNameExtensionFilter filter = new FileNameExtensionFilter("xlsx","xlsx");			//setting extensions for the file.
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {											//checking if file selected is of the required format
        	file_handler(chooser.getSelectedFile());											//returning the choosen file to file handler
        }
        }
        catch(Exception e)
        {
        	 System.out.println();
        	System.out.println(e);
        	 System.out.println();
        	 System.out.println("Enter any char to exit.");
        	 Scanner sc=new Scanner(System.in);
        	 String a=sc.nextLine();
        }
        
	}
	
	//this function takes input as STRING and return output as INPUT STREAM for the audio generated  
	public InputStream synthesize(String text, OutputFormat format) throws IOException {	
		
		SynthesizeSpeechRequest synthReq = new SynthesizeSpeechRequest()
				.withText(text).withVoiceId(voice.getId())
				.withOutputFormat(format);
		
		SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);
		
		return synthRes.getAudioStream();
	}

	
	
	
	
	//This function takes input from the JFileChooser and extracts the image and text from each row and store those in an arrayList.
	public static void file_handler(File file) throws IOException, UnsupportedAudioFileException, JavaLayerException
	{	
		//Creating the output folder where to store the output video created for each rows.
		
		Date date= new Date();
	      long time = date.getTime();
	      String s=date.getDate()+"_"+date.getHours()+"_"+date.getMinutes();
		File dir = new File("O_"+s);
		dir.mkdir();
	
		//Using Apache POI library to read the excel file and store the info in type workbook.
		Workbook workbook = WorkbookFactory.create(file);
		
		//This variable will determine if the row has been changed or not
		int temp_row=1;
		
		//This array list of array list to store the images present in a row in form of a byte array
		ArrayList<ArrayList<byte[]>> row_collection=new ArrayList<ArrayList<byte[]>>();
		
		//This array list is to store the String text in whole row
		ArrayList<String> arr1=new ArrayList<>();
		
		//iterating for each sheet in a workbook 
		for (Sheet sheet : workbook) {
			  
			//getting the drawing patriarch for each sheet as it is the layer where images are placed in an excel file
			 String sheetname = sheet.getSheetName();
			   Drawing drawing = sheet.getDrawingPatriarch();
			   ArrayList<byte[]> arr=new ArrayList<byte[]>();
			   
			   int i1=0;int j1=0;
				   for (Row row: sheet) {															//iterating through each row in a sheet
					   ArrayList<byte[]> arr_temp=new ArrayList<>();i1=1;															
					   
					   
					   if(j1>=1){																	//ignoring the first row
						   
						    
			            for(Cell cell: row) {														//iterating through each cell in a row
			            	
			                if(i1>=6 && i1%2==0 && cell != null && cell.getCellType()!=CellType.BLANK){		//add to arr1 only if i>=7 and i is odd and cell is neither null nor blank
			  
			                	
			                	arr_temp.add(Files.readAllBytes(Paths.get(cell.getStringCellValue())));
			                	}
			               
			                i1++;
			            }
			          }
					   j1++;
					   if(!arr_temp.isEmpty())																//only if arr1 has elements 
					   {
						  	row_collection.add(arr_temp);
					   }
					   
			        }
			  						
			   
			   int i=0;int j=0;int k=0;String video_name = "";String color_overlay = "";String pctg="";		//int j to check row and ,i to check cell ,k to iterate through the row_collection having image data  
			   
			   for (Row row: sheet) {															//iterating through each row in a sheet
				   
				   arr1.clear(); i=1;															//clearing arr1 after each row iteration
				   
				   
				   if(j>=1){																	//ignoring the first row
					   
					    
		            for(Cell cell: row) {														//iterating through each cell in a row
		            	
		                if(i>=7 && i%2!=0 && cell != null && cell.getCellType()!=CellType.BLANK){		//add to arr1 only if i>=7 and i is odd and cell is neither null nor blank
		                	
		                	arr1.add(cell.getStringCellValue());}
		               
		                i++;
		            }
		          }
				   j++;
				   if(!arr1.isEmpty())																//only if arr1 has elements 
				   {
					   pctg=String.valueOf( sheet.getRow(k+1).getCell(4).getNumericCellValue());	
					   video_name=sheet.getRow(k+1).getCell(1).getStringCellValue();				//get video_name from second cell
					   color_overlay=sheet.getRow(k+1).getCell(3).getStringCellValue();				//get color overlay from fourth cell
					   send_text_row_list(arr1,row_collection.get(k++),video_name,color_overlay,pctg);	//after each row iteration call send_text_row_list()
				   }
				   
		        }
			   for(int x=0;x<row_collection.size();x++)
			   {
				  for(int y=0;y<row_collection.get(x).size();y++)
				  {
					  File f=new File((x+1)+"_"+(y+1)+".mp4");
					  f.delete();
				  }
			   }
			   }
		 System.out.println();
		System.out.println("Created video files for all the rows successfully.");
		 System.out.println();
	}
	
	
	
	
	
	
	
	public static void send_text_row_list(ArrayList<String> arr1,ArrayList<byte[]> arr2,String video_name,String color_overlay,String pctg) throws IOException, UnsupportedAudioFileException, JavaLayerException
	{
			
			f=new File("video_parts.txt"); 		//init f to video_parts.txt
			
		int voice_no=0;							
		int polly_voice_no=-1;					//to store polly's voice index 
		selector aws_polly_init=null;
		
		if(all_rows==false)						//if user has choosen different voice for all the rows or it is first row
		{
			 System.out.println();
		System.out.println("Please choose one of Polly's voice for row "+ row);
		System.out.println();
		System.out.println("1    {Gender: Female , LanguageName: US English , Name: Salli}   ");
		System.out.println();
		System.out.println("2    {Gender: Female , LanguageName: US English,Name: Joanna} ");
		System.out.println();
		System.out.println("3    {Gender: Female , LanguageName: Indian English,Name: Raveena} ");
		System.out.println();
		System.out.println("4    {Gender: Female , LanguageName: Indian English,Name: Aditi}  ");
		System.out.println();
		System.out.println("5    {Gender: Male , LanguageName: US English,Name: Matthew}    ");
		System.out.println();
		System.out.println("6    {Gender: Female , LanguageName: US English,Name: Ivy}");
		System.out.println();
		System.out.println("7    {Gender: Male , LanguageName: US English,Name: Justin} ");
		System.out.println();
		System.out.println("8    {Gender: Female , LanguageName: US English,Name: Kimberly} ");
		System.out.println();
		
		
		Scanner sc=new Scanner(System.in);
		voice_no=sc.nextInt();
		
		if(voice_no==1)
		{
			polly_voice_no=2;
		}
		else if(voice_no==2)
		{
			polly_voice_no=12;
		}
		else if(voice_no==3)
		{
			polly_voice_no=20;
		}
		else if(voice_no==4)
		{
			polly_voice_no=25;
		}
		else if(voice_no==5)
		{
			polly_voice_no=26;
		}
		else if(voice_no==6)
		{
			polly_voice_no=31;
		}
		else if(voice_no==7)
		{
			polly_voice_no=37;
		}
		else if(voice_no==8)
		{
			polly_voice_no=2;
		}
		else
		{
			 System.out.println();
			System.out.println("Taking default voice..");
			polly_voice_no=2;
		}
		 System.out.println();
		System.out.println("Choose this voice for all the rows? Y/N");
		sc.nextLine();
		String yn=sc.nextLine();
		System.out.println();
		System.out.println("Connecting to AWS...");
		System.out.println();
		if(yn.equalsIgnoreCase("Y"))
		{
			all_rows=true;							//if user chooses all rows voice to be same
			def_voice_no=polly_voice_no;			//then store that voice number
			
		}
		aws_polly_init =new selector(Region.getRegion(Regions.US_EAST_1),polly_voice_no);		//init selector with diff voice no for each row
	}
		else
		{
			aws_polly_init =new selector(Region.getRegion(Regions.US_EAST_1),def_voice_no);		//if voice is same for each row
		}
		 System.out.println();
		 System.out.println("***********************");
	     System.out.println("Working on Row "+row+"");
	     System.out.println("***********************");
	     System.out.println();
	     PrintWriter fwr = new PrintWriter("video_parts.txt");									//writing the name of video file for the row in a text file
	    
			for(int x=0;x<arr1.size();x++)														//iterating for each index in arr1(text list)
			{
					
						String s="file '"+String.valueOf(row)+"_"+String.valueOf(x+1)+".mp4'";
				    
				        fwr.println(s);    
				         
			        InputStream ais = (InputStream) aws_polly_init.synthesize(arr1.get(x), OutputFormat.Mp3);			//now getting each string in arr1 to be synthesized by polly
			        byte[] bytes = IOUtils.toByteArray(ais);															//returning a byte array
			       
			       convertMP3(bytes);																					//calling  convertMP3 fun
			       
			       send_image_row_list(arr2,video_name,color_overlay,x,pctg);												//now for each index call arr2 for the image 
			       
			}																											//for ends
      row++;																											//increment row
      fwr.close();																										//close the printwriter
      
      //Combining the video files of each row//
      
      Date date= new Date();
      long time = date.getTime();
      String s=date.getDate()+"_"+date.getHours()+"_"+date.getMinutes();

 
      	
		String cmd="C:\\ffmpeg\\bin\\ffmpeg.exe -f concat -i video_parts.txt -c copy O_"+s+"/"+video_name+".mp4"; //making a command for FFMPEG to merge each video parts into a single video for a row reading the file name from video_parts.txt file
		String[] exeCmd = cmd.split(" ");																							//storing the command in a string array 
		audioVideoMerger avm1=new audioVideoMerger();																				//creating the instance of audiovideomerger class
		if(avm1.merge(video_name,exeCmd))																							//if the operation is successful
		   {
			 System.out.println();
			   System.out.println("done merging video for row "+(row-1));
			   System.out.println();
		   }
		
		File aud= new File("audio.Mp3");
		aud.delete();
		
		File img= new File("output.jpg");
		img.delete();
	
		File txt= new File("video_parts.txt");
		txt.delete();
		
		
	}
	
	//Function to convert byte array to MP3 file. 
	public static void convertMP3(byte[] bytes) throws IOException, JavaLayerException, UnsupportedAudioFileException{
			   String FILEPATH = "audio.Mp3";			
		       File file = new File(FILEPATH); 							//creating a new file
		       OutputStream os = new FileOutputStream(file); 				
		       os.write(bytes); 										//writing to that file
		       os.close();
		
	}
	
	//for each iteration of for loop it gets called
	public static void send_image_row_list(ArrayList<byte[]> arr,String video_name,String colorStr,int x,String pctg) throws IOException
	{
		System.out.println();
		System.out.println("Working on Row "+row+" image "+(x+1)+".....");
		 System.out.println();
		ByteArrayInputStream bis = new ByteArrayInputStream(arr.get(x));						//get arr index for x cell 
	    BufferedImage bImage1 = ImageIO.read(bis);												//converting byte array to image and 
	  
	    									
	   double a=(Double.valueOf(pctg))*100;
	   int b=(int)a;
		//System.out.println(a);
	    
	    Color color =new Color(Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
	            Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
	            Integer.valueOf( colorStr.substring( 5, 7 ), 16 ),b);
		tint(bImage1,color);
		 ImageIO.write(bImage1, "jpg", new File("output.jpg") );
		 
		 
	    audioVideoMerger avm1=new audioVideoMerger();											//instance of audiovideomerger class
		String cmd="C:\\ffmpeg\\bin\\ffmpeg.exe -loop 1 -y -i output.jpg -i audio.Mp3 -shortest -acodec copy -vcodec mjpeg "+row+"_"+(x+1)+".mp4";    //command to combine output.jpg and audio.mp3 outputing a video file using FFMPEG
		
		String[] exeCmd = cmd.split(" ");
		if(avm1.merge(video_name,exeCmd))														//if execution is successful 
		   {
			 System.out.println();
			   System.out.println("success.");
			   System.out.println();
		   }
	
		
			
		   
	}
	public static void tint(BufferedImage image, Color color) {									//function to put color transprancy
	    for (int x = 0; x < image.getWidth(); x++) {
	        for (int y = 0; y < image.getHeight(); y++) {
	            Color pixelColor = new Color(image.getRGB(x, y), true);
	            int r = (pixelColor.getRed() + color.getRed()) / 2;
	            int g = (pixelColor.getGreen() + color.getGreen()) / 2;
	            int b = (pixelColor.getBlue() + color.getBlue()) / 2;
	            int a = pixelColor.getAlpha();
	            int rgba = (a << 24) | (r << 16) | (g << 8) | b;
	            image.setRGB(x, y, rgba);
	        }
	    }
	}
	
	
}
	
