/*
 * Title: ACES_Report
 * Author: Jacob Montenegro
 * E-mail: jacobmontenegro1999@gmail.com (When emailing about this program put "ACES Prog" as the Subject)
 * Date of Final Revision: 7/1/21
 * Description: This program produces a report of the data acquired from the ACES QuestionPro. It takes an excel spreadsheet and prints out relevant data and
 * text to a dialog box (you should be able to copy/paste from it). The data reported here is assorted to appear similarly to the data report that will be 
 * submitted monthly as of 7/1/21.
 */

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ACES_Report {
	public static void main(String[] args) {
		// initialize reusable variables
		globals message = new globals();
		Subjects subj = new Subjects();
		SearchWords searchfor = new SearchWords();
		String reqBySubText = "Requests by Subject ";

		try {
			String filepath = FilePathGUI();
			File file = new File(filepath); // creates new file
			FileInputStream fis = new FileInputStream(file); // gets bytes from file
			XSSFWorkbook wb = new XSSFWorkbook(fis); // gets workbook from file
			XSSFSheet sheet = wb.getSheetAt(2); // gets third page (indexes start at 0; the first page is at index 0, the third page is at index 2)
												// just be sure it's set to the "Raw Data" sheet.
			int rowsAndCols[] = Row_Col_Count(sheet); //gets number of rows and columns
			//printSheetHeaders(sheet); //prints the headers of each column
			Object spreadsheet[][] = convertSheetToArray(sheet,rowsAndCols);
			
			//The line under this prints the whole spreadsheet
			//printSpreadSheet(spreadsheet);
			
			//The line under this prints the number of rows and columns there are in the spreadsheet
			//System.out.println("Rows: "+rowsAndCols[0]+"\nColumns: "+rowsAndCols[1]);
			String headers[] = HeadersArray(spreadsheet,rowsAndCols[1]);
			
			/*Months*/
				Month(sheet,headers,rowsAndCols[0],message);
			
			/*Sign-Ins*/
				//SignIn(sheet,headers,rowsAndCols[0],message);
				message.addToMessage("SignIn\n");
				ReqBySubj_Universal(sheet,headers,rowsAndCols[0],message,subj.SignIn,searchfor.primaryReason);
			
			/*Requests by Subject*/
				message.addToMessage("Requests by Subject (Engineering)\n");
				ReqBySubj_Universal(sheet,headers,rowsAndCols[0],message,subj.ENGR,searchfor.ENGR);
								
				/*Individual Engineering disciplines*/
				// CE
				message.addToMessage(reqBySubText+"(CE)\n");
				ReqBySubj_Universal(sheet,headers,rowsAndCols[0],message,subj.CESubjects,searchfor.CE);
				
				// CS
				message.addToMessage(reqBySubText+"(CS)\n");
				ReqBySubj_Universal(sheet,headers,rowsAndCols[0],message,subj.CSSubjects,searchfor.CS);
				
				// EE
				message.addToMessage(reqBySubText+"(EE)\n");
				ReqBySubj_Universal(sheet,headers,rowsAndCols[0],message,subj.EESubjects,searchfor.EE);
				
				// EEL
				message.addToMessage(reqBySubText+"(EEL)\n");
				ReqBySubj_Universal(sheet,headers,rowsAndCols[0],message,subj.EELSubjects,searchfor.EEL);
				
				// IMSE
				message.addToMessage(reqBySubText+"(IMSE)\n");
				ReqBySubj_Universal(sheet,headers,rowsAndCols[0],message,subj.IMSESubjects,searchfor.IMSE);
				
				// ME
				message.addToMessage(reqBySubText+"(ME)\n");
				ReqBySubj_Universal(sheet,headers,rowsAndCols[0],message,subj.MESubjects,searchfor.ME);
				
				// MME
				message.addToMessage(reqBySubText+"(MME)\n");
				ReqBySubj_Universal(sheet,headers,rowsAndCols[0],message,subj.MMESubjects,searchfor.MME);
				
				/*Other subjects*/
				// MATH
				message.addToMessage(reqBySubText+"(MATH)\n");
				ReqBySubj_Universal(sheet,headers,rowsAndCols[0],message,subj.MathSubjects,searchfor.MATH);
				
				// CHEM
				message.addToMessage(reqBySubText+"(CHEM)\n");
				ReqBySubj_Universal(sheet,headers,rowsAndCols[0],message,subj.CHEMSubjects,searchfor.CHEM);
				
				// PHYS
				message.addToMessage(reqBySubText+"(PHYS)\n");
				ReqBySubj_Universal(sheet,headers,rowsAndCols[0],message,subj.PHYSSubjects,searchfor.PHYS);
				
			/*Tutoring Requests by Hour*/
				Date(sheet,headers,rowsAndCols[0],message);
				
			/*Unique Students*/
				ArrayList<UniqueStudent> students = createStudentArray(sheet,headers,rowsAndCols[0],message);
				BySubjects(students,message);
				Majors(students,message);
				EngrBySubjects(students,message);
				Classification(students,message);
			wb.close();
		} catch(Exception e) {
			// This exception is thrown when the user chooses a file with the wrong file type. 
			message.addToMessage("Couldn't read that file. Please make sure the file is an excel spreadsheet (.xlsx)");
		}
		// writes out the text to the GUI for the user to see.
		GUI(message);
	}
	
	// Iterates through the headers of the excel file, compares the search keyword with the headers to find the correct header, returns an index to that header or -1 if that header doesn't exist.
	public static int searchForHeadersIndex(String search, String[] headers) {
		for(int i = 0; i<headers.length; i++)
			if(headers[i].indexOf(search) != -1)
				return i;
		return -1;
	}
	
	// returns the file path of the file the user selects.
	public static String FilePathGUI() {
		String filepath = "";
		JFileChooser chooser = new JFileChooser();
		JDialog f = new JDialog();
		int option = chooser.showOpenDialog(f);
		if (option == JFileChooser.APPROVE_OPTION)
		{
			File selectedFile = chooser.getSelectedFile();
			filepath = selectedFile.getAbsolutePath();
		}
		return filepath;
	}
	
	// prints the text previously obtained and stored in message and writes it to the gui so the user can read it.
	public static void GUI(globals message) {
		String text = "";
		for (int i = 0; i < message.message.size(); i++)
			text += message.message.get(i);
		JTextArea ta = new JTextArea(text);
		JScrollPane scrollPane = new JScrollPane(ta);
		ta.setLineWrap(true);  
		ta.setWrapStyleWord(true); 
		scrollPane.setPreferredSize( new Dimension( 600, 500 ) ); //dimensions are set here, change to your liking.
		JOptionPane.showMessageDialog(null, scrollPane, "ACES DATA", JOptionPane.INFORMATION_MESSAGE); // "ACES DATA" is the title of the dialog box displayed.
	}
	
	// takes a 2D array and adds it as a string to the message object. 
	public static void Print2DArray(Object[][] array, globals message) {
		for (int i = 0; i<array.length;i++) {
			for (int j = 0; j<array[i].length;j++) 
				message.addToMessage(array[i][j]+"\t"); // a tab is added for organization
			message.addToMessage("\n"); // Newlines are added to the end of each sentence. 
		}
		message.addToMessage("\n"); // A final newline is added to the block of text to separate each array
	}
	
	// takes a 2D array and adds it as a string to the message object along with the percentages/averages of each component. 
	public static void Print2DArrayWithPercents(Object[][] array, globals message) {
		double total = (double)TotalRequests(array,message);
		double percentage = 0.0;
		DecimalFormat df = new DecimalFormat("##.##");
		for (int i = 0; i<array.length;i++) {
			for (int j = 0; j<array[i].length;j++) {
				message.addToMessage(array[i][j]+"\t");
				if (j == 1) {
					double sum = (int)array[i][1];
					percentage = (double)((sum/total)*100);
					message.addToMessage(""+df.format(percentage));
					message.addToMessage("%");
				}
			}
			message.addToMessage("\n"); // Newlines are added to the end of each sentence. 
		}
		message.addToMessage("\n"); // A final newline is added to the block of text to separate each array
	}
	
	// returns the number of rows and columns of the spreadsheet
	public static int[] Row_Col_Count(XSSFSheet sheet) {
		int rowsAndCols[] = new int[2];
		
		Iterator<Row> itr = sheet.iterator(); // itr iterates through the spreadsheet
		int rowCount = 0;
		int colCount = 0;
		int maxColCount  = 0;
		// gets number of rows (top to bottom)
		while (itr.hasNext()) {
			rowCount++;
			Row row = itr.next();
			Iterator<Cell> cellIterator = row.cellIterator(); // cellIterator iterates through the columns (We'll use this to count the columns)
			colCount = 0;
			// ets number of columns (left to right)
			while (cellIterator.hasNext())   
			{  
				colCount++;
				if (colCount > maxColCount)
					maxColCount = colCount;
				Cell cell = cellIterator.next();
			}
		}
		rowsAndCols[0] = rowCount;
		rowsAndCols[1] = maxColCount;
		return rowsAndCols;
	}
	
	//prints the headers of each column, use this for debugging
	public static void printSheetHeaders(XSSFSheet sheet) {
		Iterator<Row> itr = sheet.iterator();
		Row row = itr.next();
		Iterator<Cell> cellIterator = row.cellIterator();

		int i = 0;
		while (cellIterator.hasNext())   
		{  
			Cell cell = cellIterator.next();
			System.out.println(""+i+" "+cell.getStringCellValue());
			i++;
		}
		System.out.println("\n");
		
	}
	
	// converts Objects into type int; +1 since iteration begins at 0
	// this is used when for the averages/percents
	// maybe use a for-loop for this, change it up as you like.
	private static void subj_switch_case(Object[][] Subjects, int cell_val) {
		switch (cell_val) {
			case 1:
				Subjects[0][1] = (int)Subjects[0][1] + 1;
				break;
			case 2:
				Subjects[1][1] = (int)Subjects[1][1] + 1;
				break;
			case 3:
				Subjects[2][1] = (int)Subjects[2][1] + 1;
				break;
			case 4:
				Subjects[3][1] = (int)Subjects[3][1] + 1;
				break;
			case 5:
				Subjects[4][1] = (int)Subjects[4][1] + 1;
				break;
			case 6:
				Subjects[5][1] = (int)Subjects[5][1] + 1;
				break;
			case 7:
				Subjects[6][1] = (int)Subjects[6][1] + 1;
				break;
			case 8:
				Subjects[7][1] = (int)Subjects[7][1] + 1;
				break;
			case 9:
				Subjects[8][1] = (int)Subjects[8][1] + 1;
				break;
			case 10:
				Subjects[9][1] = (int)Subjects[9][1] + 1;
				break;
			case 11:
				Subjects[10][1] = (int)Subjects[10][1] + 1;
				break;
			case 12:
				Subjects[11][1] = (int)Subjects[11][1] + 1;
				break;
			default: 
				Subjects[12][1] = (int)Subjects[12][1] + 1;
				break;
		}
	}
	
	// this takes all the data from the spreadsheet and stores it in an array we can use
	// instead of using the Iterator<> object we can just use an array, for simplicity.
	public static Object[][] convertSheetToArray(XSSFSheet sheet, int[] rowsAndCols) {
		Object spreadsheet[][] = new Object[rowsAndCols[0]][rowsAndCols[1]];
		
		for (int i = 0; i<spreadsheet.length; i++) {
			for (int j = 0; j<spreadsheet[i].length; j++) {
				spreadsheet[i][j] = null;
			}
		}
		
		Iterator<Row> itr = sheet.iterator(); //itr iterates through the spreadsheet
		int r = 0;
		while (itr.hasNext()) {
			Row row = itr.next();
			Iterator<Cell> cellIterator = row.cellIterator(); //cellIterator iterates through the columns (We'll use this to count the columns)
			r++;
			int c = 0;
			if (r >= rowsAndCols[0])
				break;
			
			while (cellIterator.hasNext() && c < rowsAndCols[1])   
			{  
				
				Cell cell = cellIterator.next();
				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_BLANK:
					spreadsheet[r][c] = null;
					break;
				case Cell.CELL_TYPE_STRING:
					spreadsheet[r][c] = cell.getStringCellValue();
					break;
				case Cell.CELL_TYPE_NUMERIC:
					spreadsheet[r][c] = cell.getNumericCellValue();
					break;
				case Cell.CELL_TYPE_BOOLEAN:
					spreadsheet[r][c] = cell.getBooleanCellValue();
					break;
				default:
					break;
				}
				c++;
			}
		}
		return spreadsheet;
	}
	
	// returns an array of all the headers in the spreadsheet. instead of searching the 2D array for the headers, use this 1D array with the headers
	public static String[] HeadersArray(Object[][] spreadsheet, int numCols){
		String headers[] = new String[numCols];
		for (int i = 0; i<numCols; i++) {
			headers[i] = String.valueOf(spreadsheet[1][i]);
		}
		return headers;
	}

	// pertains to the data, gets the total number of requests.
	public static int TotalRequests(Object[][] array, globals message) {
		int sum = 0;
		for (int i = 0; i<array.length;i++) {
			sum += (int)array[i][1];
		}
		message.addToMessage("Total Requests: "+sum+"\n");
		return sum;
	}
	
	// gets the total number of requests by subject. Subjects and search are used here to make this as dynamic as possible; there are other deprecated methods towards the bottom that dont make use of this.
	public static void ReqBySubj_Universal(XSSFSheet sheet, String[] headers, int numRows, globals message,Object[][] Subjects, String search) {
		int offset = 0;
		if (search.equals("Please select primary reason for your visit:"))
			offset = 3; // the questionpro sign in asks 2 extra questions (career center satellite & interview/meeting) and I just never got around to removing them from, this 3 offset accounts for that, feel free to remove it later.
						// its also 3 instead of 2 since indexing starts at 0 so we remove 1 and remove another 2 since they don't come out in the data report for a total of 3 removed. email me if any questions.
		int index = searchForHeadersIndex(search,headers);	// returns the index of the header, or if the header isn't found, returns -1	
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
			try {
				int cell_val = (int)cell.getNumericCellValue()-offset;
				subj_switch_case(Subjects,cell_val);
			} catch (Exception e) { // there are some holes in the data, this will skip them without errors/breaking.
				continue;
			}
		}
		Print2DArrayWithPercents(Subjects,message);
	}
	
	// this gives you the peak hours for ACES, I recommend doing peak hours for every discipline (EE, CS, ME, etc.) it should help
	public static void Date(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		message.addToMessage("Tutoring Requests by Hour\n");
		Object[][] hours = {
							{"8:00",0,0,0,0,0,0},
							{"9:00",0,0,0,0,0,0},
							{"10:00",0,0,0,0,0,0},
							{"11:00",0,0,0,0,0,0},
							{"12:00",0,0,0,0,0,0},
							{"1:00",0,0,0,0,0,0},
							{"2:00",0,0,0,0,0,0},
							{"3:00",0,0,0,0,0,0},
							{"4:00",0,0,0,0,0,0},
							{"5:00",0,0,0,0,0,0},
							{"6:00",0,0,0,0,0,0},
							{"After Hours",0,0,0,0,0,0}};
		int phys = 0;
		int Other = 0;
		
		String search = "Timestamp";
		
		String search2 = "Please select primary reason for your visit:";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
		int tutoringIndex = searchForHeadersIndex(search2,headers);	//returns the index of the header, or if the header isn't found, returns -1	
		
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
			Cell tutoringCell= row.getCell(tutoringIndex);
						
			try {
		        if (DateUtil.isCellDateFormatted(cell) && (double)tutoringCell.getNumericCellValue() == 8.0) {
		            java.util.Date date = cell.getDateCellValue();
		            int day = date.getDay();
		            int time = date.getHours();
		            if (time > 18 || time < 7)
		            	hours[11][day] = (int)hours[11][day] + 1;
		            else if(time == 7)
		            	hours[0][day] = (int)hours[0][day] + 1;
		            else
		            	hours[time-8][day] = (int)hours[time-8][day] + 1;
		        }
			} catch (Exception e) {
				continue;
			}
		}
		int total = 0;
		for(int i = 0; i<hours.length; i++) {
			for(int j = 0; j<hours[i].length; j++)
				if (j != 0)
					total += (int)hours[i][j];
		}
		message.addToMessage("Total: "+total+"\n");
		Print2DArray(hours,message);
	}
	
	// this gives you the unique students.
	/* an array list is terrible for this application, it's really slow. Honestly, I used this because I got lazy.
	 * I recommend using a hashtable or a map or something similar for this part; it'll run faster and you can explain to future
	 * employers how you fixed some poorly written code cx
	 * Don't get me wrong, it gets the job done but it's far from perfect, use it as inspiration but definitely fix it up cx
	 */
	public static ArrayList<UniqueStudent> createStudentArray(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		message.addToMessage("\nSECTION FOR UNIQUE STUDENTS\nNumber of Unique Students:\t");
		ArrayList<UniqueStudent> students = new ArrayList<UniqueStudent>();
		
		boolean studentIsUnique = true;
		
		String usernameSearch = "Username";
		int usernameIndex = searchForHeadersIndex(usernameSearch,headers);	//returns the index of the header, or if the header isn't found, returns -1	
		
		String majorSearch = "Major";
		int majorIndex = searchForHeadersIndex(majorSearch,headers);	//returns the index of the header, or if the header isn't found, returns -1	
		
		String subjectSearch = "What subject do you need tutoring for?";
		int subjectIndex = searchForHeadersIndex(subjectSearch,headers);	//returns the index of the header, or if the header isn't found, returns -1	
		
		String classSearch = "classification";
		int classIndex = searchForHeadersIndex(classSearch,headers);	//returns the index of the header, or if the header isn't found, returns -1	
		
		UniqueStudent student;
		
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell usernameCell = row.getCell(usernameIndex);
			Cell majorCell= row.getCell(majorIndex);
			Cell subjectCell = row.getCell(subjectIndex);
			Cell classCell= row.getCell(classIndex);
			
			try {
				student = new UniqueStudent(usernameCell.toString(),majorCell.getNumericCellValue(),subjectCell.getNumericCellValue(),classCell.getNumericCellValue());
				if(students.isEmpty())
					students.add(student);
				
				for(UniqueStudent uniqueStudent: students) {
					if(uniqueStudent.getUsername().indexOf(usernameCell.toString()) != -1)
						studentIsUnique = false;
				}
				
				if (studentIsUnique)
					students.add(student);
				
				studentIsUnique = true;
		            
			} catch (Exception e) {
				continue;
			}
		}
		int numOfUniques = 0;
		for(int i = 0; i<students.toArray().length; i++)
			numOfUniques++;
		message.addToMessage(String.valueOf(numOfUniques)+"\n");
		return students;
	}
	
	// returns the number of tutoring requested by subject for the unique students group.
	public static void BySubjects(ArrayList<UniqueStudent> students, globals message) {
		message.addToMessage("By Subjects\n");
		Object subj[][] = {
							{"ENGR",0},
							{"MATH",0},
							{"CHEM",0},
							{"PHYS",0},
							{"Other",0}};
		
		for (UniqueStudent student : students) { // in English this basically says "For each student in the students array list", very similar to Python
			int student_subj = (int)student.getSubj();
			subj_switch_case(subj,student_subj);
		}
		Print2DArrayWithPercents(subj,message);
	}

	// returns the number of tutoring requested by engr subjects for the unique students group.
	public static void EngrBySubjects(ArrayList<UniqueStudent> students, globals message) {
		
		message.addToMessage("By Majors and Subjects\n");
		Object subj[][] = {
							{"CE",0,0,0,0,0},
							{"EE",0,0,0,0,0},
							{"ME",0,0,0,0,0},
							{"CEM",0,0,0,0,0},
							{"EEL",0,0,0,0,0},
							{"MME",0,0,0,0,0},
							{"CS",0,0,0,0,0},
							{"IMSE",0,0,0,0,0},
							{"Other",0,0,0,0,0},
							};
		int ENGR = 1;
		int MATH = 2;
		int CHEM = 3;
		int PHYS = 4;
		int Other = 5;
		
		int majorIndex = 0;
		int subjIndex = 0;
		
		for (UniqueStudent student : students) {
			int major = (int)student.getMajor();
			if(major == 1 || major == 4)
				majorIndex = 0;
			else
				majorIndex = major-1;
			
			if(student.getSubj() == 1.0)
				subjIndex = ENGR;
			else if(student.getSubj() == 2.0)
				subjIndex = MATH;
			else if(student.getSubj() == 3.0)
				subjIndex = CHEM;
			else if(student.getSubj() == 4.0)
				subjIndex = PHYS;
			else
				subjIndex = Other;
			subj[majorIndex][subjIndex] = (int)subj[majorIndex][subjIndex] + 1;
		}
		
		Print2DArray(subj,message);
	}
	
	// returns the number of each major for the unique students group.
	public static void Majors(ArrayList<UniqueStudent> students, globals message) {
		message.addToMessage("Majors:\n\n");
		Object majors[][] = {
							{"Civil Engineering",0},
							{"Electrical Engineering",0},
							{"Mechanical Engineering",0},
							{"Civil Engineering - Construction Management",0},
							{"Education and Engineering Leadership",0},
							{"Metallurgical and Materials Engineering",0},
							{"Computer Science",0},
							{"Industrial Engineering",0},
							{"Other",0}};
		for (UniqueStudent student : students) {
			int maj = (int)student.getMajor();
			subj_switch_case(majors,maj);
		}
		Print2DArrayWithPercents(majors,message);
	}
	
	// returns the number of each classification for the unique students group.
	public static void Classification(ArrayList<UniqueStudent> students, globals message) {
		
		message.addToMessage("Classification:\n\n");
		Object classification[][] = {
							{"Freshman",0},
							{"Sophomore",0},
							{"Junior",0},
							{"Senior",0},
							{"Graduate",0}};
		
		for (UniqueStudent student : students) {
			int student_class = (int)student.getClassification();
			subj_switch_case(classification,student_class);
		}
		
		Print2DArrayWithPercents(classification,message);
	}

	// returns the total number of tutoring sign ins by month so you don't have to go back and figure it out yourself
	public static void Month(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		message.addToMessage("Tutoring Sign In by Month\n");
		Object[][] months = {
							{"Jan",0},
							{"Feb",0},
							{"Mar",0},
							{"Apr",0},
							{"May",0},
							{"Jun",0},
							{"Jul",0},
							{"Aug",0},
							{"Sep",0},
							{"Oct",0},
							{"Nov",0},
							{"Dec",0}};
		
		String search = "Timestamp";
		String search2 = "Please select primary reason for your visit:";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
		int tutoringIndex = searchForHeadersIndex(search2,headers);	//returns the index of the header, or if the header isn't found, returns -1	
		
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
			Cell tutoringCell= row.getCell(tutoringIndex);
						
			try {
		        if (DateUtil.isCellDateFormatted(cell) && (double)tutoringCell.getNumericCellValue() == 8.0) {
		            java.util.Date date = cell.getDateCellValue();
		            int month = date.getMonth();
		            months[month][1] = (int)months[month][1] + 1;
		        }
			} catch (Exception e) {
				continue;
			}
		}
		int total = 0;
		for(int i = 0; i<months.length; i++)
			total += (int)months[i][1];
		Print2DArray(months,message);
		message.addToMessage("Total: "+total+"\n\n\n");
	}
	
	/*
	 * DEPRECATED METHODS, DON'T USE. STUDY HOW THEY WORK IF YOU WISH BUT PLEASE USE THE UNIVERSAL METHOD INSTEAD
	 * Feast your eyes on the blunders of the past: determine how you'd improve and combine these methods.
	 */

	public static void SignIn(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		
		message.addToMessage("SignIn\n");
		Object[][] SignIn = {{"Volunteer:",0},
							{"Room Reservation:",0},
							{"Student Organizations:",0},
							{"Study:",0},
							{"Tutor:",0},
							{"Tutoring:",0},
							{"Other:",0}};
	
		String search = "Please select primary reason for your visit:";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
		
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
			try {
				int cell_val = (int)cell.getNumericCellValue()-3;
				subj_switch_case(SignIn,cell_val);
			} catch (Exception e) {
				continue;
			}
		}
		Print2DArray(SignIn,message);
	}

	public static void ReqBySubj(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		message.addToMessage("Requests by Subject (Engineering)\n");
		Object[][] Subjects = {
							{"CE/CEM:",0},
							{"CS:",0},
							{"EE:",0},
							{"EEL:",0},
							{"IMSE:",0},
							{"MME:",0},
							{"ME:",0},
							{"Other:",0}};

		String search = "What Engineering discipline do you need help with?";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
		
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
						
			try {
				int cell_val = (int)cell.getNumericCellValue();
				subj_switch_case(Subjects,cell_val);
			} catch (Exception e) {
				continue;
			}
		}
		Print2DArrayWithPercents(Subjects,message);
	}
	
	public static void ReqBySubj_MATH(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		message.addToMessage("Requests by Subject (MATH)\n");
		Object[][] Subjects = {
							{"Pre Cal 1508:",0},
							{"Cal 1411:",0},
							{"Cal2 1312:",0},
							
							{"Cal3 2313:",0},
							{"Dif. Eq. 2326:",0},
							
							{"Other:",0}};
		
		String search = "What Math class do you need help with?";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
				
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
			try {
				int cell_val = (int)cell.getNumericCellValue();
				subj_switch_case(Subjects,cell_val);
			} catch (Exception e) {
				continue;
			}
		}
		Print2DArrayWithPercents(Subjects,message);
	}
	
	public static void ReqBySubj_CE(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		message.addToMessage("Requests by Subject (CE)\n");
		Object[][] Subjects = {
							{"CE 1301:",0},
							{"CE 1313:",0},
							
							{"CE 2315:",0},
							{"CE 2334:",0},
							{"CE 2335:",0},
							{"CE 2338 or PHYS 3331:",0},
							{"CE 2343:",0},
							{"CE 2373:",0},
							{"CE 2375:",0},
							{"CE 2385:",0},
							{"ACCT 2301:",0},
							
							{"Upper Division:",0}};
		
		String search = "Which Civil Engineering/Construction Engineering and Management course did you need assistance with?";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
				
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
			try {
				int cell_val = (int)cell.getNumericCellValue();
				subj_switch_case(Subjects,cell_val);
			} catch (Exception e) {
				continue;
			}
		}
		Print2DArrayWithPercents(Subjects,message);
	}
	
	public static void ReqBySubj_CS(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		message.addToMessage("Requests by Subject (CS)\n");
		Object[][] Subjects = {
							{"CS 1301:",0},
							{"CS 1101:",0},
							
							{"CS 2401:",0},
							{"EE 2369:",0},
							{"EE 2169:",0},
							{"CS 2302:",0},
							
							{"Upper Division:",0}};

		String search = "Which Computer Science course did you need assistance with?";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
				
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
			try {
				int cell_val = (int)cell.getNumericCellValue();
				subj_switch_case(Subjects,cell_val);
			} catch (Exception e) {
				continue;
			}
		}		
		Print2DArrayWithPercents(Subjects,message);
	}

	public static void ReqBySubj_EE(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		message.addToMessage("Requests by Subject (EE)\n");
		Object[][] Subjects = {
							{"EE 1105:",0},
							{"EE 1305:",0},
							
							{"EE 2369:",0},
							{"EE 2169:",0},
							{"EE 2372:",0},
							{"EE 2350:",0},
							{"EE 2351:",0},
							{"EE 2151:",0},
							{"EE 2353:",0},
							
							{"Upper Division:",0}};
		
		String search = "Which Electrical Engineering course did you need assistance with?";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
				
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
			try {
				int cell_val = (int)cell.getNumericCellValue();
				subj_switch_case(Subjects,cell_val);
			} catch (Exception e) {
				continue;
			}
		}		
		Print2DArrayWithPercents(Subjects,message);
	}
	
	//UNIMP
	public static void ReqBySubj_EEL(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		message.addToMessage("Requests by Subject (EEL)\n");
		Object[][] Subjects = {
							{"EL 1405:",0},
							{"EL 1302:",0},
							
							{"EL 2301:",0},
							{"MME 2303:",0},
							{"MME 2434:",0},
							{"CE 2377:",0},
							{"CE 2338:",0},
							{"MECH 2311:",0},
							
							{"Upper Division:",0}};
		
		String search = "Which Engineering Education and Leadership course did you need assistance with?";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
				
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
			try {
				int cell_val = (int)cell.getNumericCellValue();
				subj_switch_case(Subjects,cell_val);
			} catch (Exception e) {
				continue;
			}
		}
		Print2DArrayWithPercents(Subjects,message);
	}
	//UNIMP
	public static void ReqBySubj_IMSE(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		message.addToMessage("Requests by Subject (IMSE)\n");
		Object[][] Subjects = {
							{"IE 1333:",0},
							{"MECH 1305:",0},
							
							{"MECH 2131:",0},
							{"IE 2333:",0},
							{"IE 2303/MECH 2331/MME 2303:",0},
							{"CE 2315/MECH 1321:",0},
							{"IE 2377/MECH 2342:",0},
							
							{"Upper Division",0}};
		
		
		String search = "Which Industrial and Systems Engineering course did you need assistance with?";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
				
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
			try {
				int cell_val = (int)cell.getNumericCellValue();
				subj_switch_case(Subjects,cell_val);
			} catch (Exception e) {
				continue;
			}
		}
		Print2DArrayWithPercents(Subjects,message);
	}
	//UNIMP
	public static void ReqBySubj_ME(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		message.addToMessage("Requests by Subject (ME)\n");
		Object[][] Subjects = {
							{"MECH 1305:",0},
							{"MECH 1321:",0},
							
							{"MECH 2103:",0},
							{"MECH 2131/MECH 2132/ MECH 2133",0},
							{"MECH 2311:",0},
							{"MECH 2322",0},
							{"MECH 2331",0},
							{"MECH 2340",0},
							{"MECH 2342",0},
							
							{"Upper Division",0}};
		
		String search = "Which Mechanical Engineering course did you need assistance with?";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
				
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
			try {
				int cell_val = (int)cell.getNumericCellValue();
				subj_switch_case(Subjects,cell_val);
			} catch (Exception e) {
				continue;
			}
		}
		Print2DArrayWithPercents(Subjects,message);
	}
	
	//UNIMP
	public static void ReqBySubj_MME(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		message.addToMessage("Requests by Subject (MME)\n");
		Object[][] Subjects = {
							{"MME 1401:",0},
							{"MME 1205:",0},
							
							{"MME 2303:",0},
							{"MME 2305:",0},
							{"MME 2434:",0},
							
							{"Upper Division:",0}};
		
		String search = "Which Metallurgical and Materials Engineering course did you need assistance with?";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
				
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
			try {
				int cell_val = (int)cell.getNumericCellValue();
				subj_switch_case(Subjects,cell_val);
			} catch (Exception e) {
				continue;
			}
		}
		Print2DArrayWithPercents(Subjects,message);
	}
	
	public static void ReqBySubj_CHEM(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		message.addToMessage("Requests by Subject (CHEM)\n");
		Object[][] Subjects = {
							{"CHEM1 1305:",0},
							{"CHEM2 1306:",0},
							{"CHEM 1407:",0},
							{"Other:",0}};
		
		String search = "What Chem class do you need help with?";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
		
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
						
			try {
				int cell_val = (int)cell.getNumericCellValue();
				subj_switch_case(Subjects,cell_val);
			} catch (Exception e) {
				continue;
			}
		}
		Print2DArrayWithPercents(Subjects,message);
	}
	
	public static void ReqBySubj_PHYS(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		message.addToMessage("Requests by Subject (PHYS)\n");
		Object[][] Subjects = {
							{"PHYS:",0},
							{"Other:",0}};
		
		String search = "What subject do you need tutoring for?";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
		
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
			try {
				int cell_val = (int)cell.getNumericCellValue();
				subj_switch_case(Subjects,cell_val);
			} catch (Exception e) {
				continue;
			}
		}
		Print2DArrayWithPercents(Subjects,message);
	}
	
	/*
	 * END OF DEPRECATED METHODS, DON'T USE. STUDY HOW THEY WORK IF YOU WISH BUT PLEASE USE THE UNIVERSAL METHOD INSTEAD
	 */
}
	
