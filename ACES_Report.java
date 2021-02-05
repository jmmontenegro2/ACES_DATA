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
		try {
			String filepath = FilePathGUI();
			File file = new File(filepath); //creates new file
			FileInputStream fis = new FileInputStream(file); //gets bytes from file
			XSSFWorkbook wb = new XSSFWorkbook(fis); //gets workbook from file
			XSSFSheet sheet = wb.getSheetAt(2); //gets third page (indexes start at 0; the first page is at index 0, the third page is at index 2)
			globals message = new globals();
			
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
				SignIn(sheet,headers,rowsAndCols[0],message);
			
			/*Requests by Subject*/
				ReqBySubj(sheet,headers,rowsAndCols[0],message);
				ReqBySubj_MATH(sheet,headers,rowsAndCols[0],message);
				ReqBySubj_CHEM(sheet,headers,rowsAndCols[0],message);
				ReqBySubj_PHYS(sheet,headers,rowsAndCols[0],message);	
			
			/*Tutoring Requests by Hour*/
				Date(sheet,headers,rowsAndCols[0],message);
				
			/*Unique Students*/
				ArrayList<UniqueStudent> students = createStudentArray(sheet,headers,rowsAndCols[0],message);
				BySubjects(students,message);
				Majors(students,message);
				EngrBySubjects(students,message);
				Classification(students,message);
				
			GUI(message);
			
		} catch(Exception e) {
			System.err.println("Couldn't read that file.");
			e.printStackTrace();
		}
	}
	
	public static int searchForHeadersIndex(String search, String[] headers) {
		for(int i = 0; i<headers.length; i++) {
			if(headers[i].indexOf(search) != -1)
				return i;
		}
		return -1;
	}
	
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
	
	public static void GUI(globals message) {
		String text = "";
		for (int i = 0; i < message.message.size(); i++) {
			text += message.message.get(i);
		}
		
		JTextArea ta = new JTextArea(text);
		JScrollPane scrollPane = new JScrollPane(ta);
		ta.setLineWrap(true);  
		ta.setWrapStyleWord(true); 
		scrollPane.setPreferredSize( new Dimension( 600, 500 ) );
		JOptionPane.showMessageDialog(null, scrollPane, "ACES DATA", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static void Print2DArray(Object[][] array, globals message) {
		for (int i = 0; i<array.length;i++) {
			for (int j = 0; j<array[i].length;j++) {
				message.addToMessage(array[i][j]+"\t");
			}
			message.addToMessage("\n");
		}
		message.addToMessage("\n");
	}
	
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
			message.addToMessage("\n");
		}
		message.addToMessage("\n");
	}
	
	//returns the number of rows and columns of the spreadsheet
	public static int[] Row_Col_Count(XSSFSheet sheet) {
		int rowsAndCols[] = new int[2];
		
		Iterator<Row> itr = sheet.iterator(); //itr iterates through the spreadsheet
		int rowCount = 0;
		int colCount = 0;
		int maxColCount  = 0;
		//gets number of rows (top to bottom)
		while (itr.hasNext()) {
			rowCount++;
			Row row = itr.next();
			Iterator<Cell> cellIterator = row.cellIterator(); //cellIterator iterates through the columns (We'll use this to count the columns)
			colCount = 0;
			//gets number of columns (left to right)
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
	
	//prints the headers of each column
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
	
	public static String[] HeadersArray(Object[][] spreadsheet, int numCols){
		String headers[] = new String[numCols];
		
		for (int i = 0; i<numCols; i++) {
			headers[i] = String.valueOf(spreadsheet[1][i]);
		}
		
		return headers;
	}

	public static void SignIn(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		
		message.addToMessage("SignIn\n");
		Object[][] SignIn = {
							{"Volunteer:",0},
							{"Room Reservation:",0},
							{"Student Organizations:",0},
							{"Study:",0},
							{"Tutor:",0},
							{"Tutoring:",0},
							{"Other:",0}};
		int volunteer= 0;
		int roomRes= 0;
		int studentOrg= 0;
		int study = 0;
		int tutor = 0;
		int tutoring= 0;
		int other = 0;
		
		String search = "Please select primary reason for your visit:";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
		
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
						
			try {
				if ((Double)cell.getNumericCellValue() == 3.0)
					volunteer++;
				else if ((Double)cell.getNumericCellValue() == 4.0)
					roomRes++;
				else if ((Double)cell.getNumericCellValue() == 5.0)
					studentOrg++;
				else if ((Double)cell.getNumericCellValue() == 6.0)
					study++;
				else if ((Double)cell.getNumericCellValue() == 7.0)
					tutor++;
				else if ((Double)cell.getNumericCellValue() == 8.0)
					tutoring++;
				else 
					other++;
			} catch (Exception e) {
				continue;
			}
		}
		
		SignIn[0][1] = volunteer;
		SignIn[1][1] = roomRes;
		SignIn[2][1] = studentOrg;
		SignIn[3][1] = study;
		SignIn[4][1] = tutor;
		SignIn[5][1] = tutoring;
		SignIn[6][1] = other;
		
		Print2DArray(SignIn,message);
	}
	
	public static int TotalRequests(Object[][] array, globals message) {
		int sum = 0;
		for (int i = 0; i<array.length;i++) {
			sum += (int)array[i][1];
		}
		message.addToMessage("Total Requests: "+sum);
		message.addToMessage("\n");
		return sum;
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
		int CE = 0;
		int CS = 0;
		int EE = 0;
		int EEL = 0;
		int IMSE = 0;
		int MME = 0;
		int ME = 0;
		int Other = 0;
		
		String search = "What Engineering discipline do you need help with?";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
		
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
						
			try {
				if ((Double)cell.getNumericCellValue() == 1.0)
					CE++;
				else if ((Double)cell.getNumericCellValue() == 2.0)
					CS++;
				else if ((Double)cell.getNumericCellValue() == 3.0)
					EE++;
				else if ((Double)cell.getNumericCellValue() == 4.0)
					EEL++;
				else if ((Double)cell.getNumericCellValue() == 5.0)
					IMSE++;
				else if ((Double)cell.getNumericCellValue() == 7.0)
					MME++;
				else if ((Double)cell.getNumericCellValue() == 6.0)
					ME++;
				else 
					Other++;
			} catch (Exception e) {
				continue;
			}
		}
		
		Subjects[0][1] = CE;
		Subjects[1][1] = CS;
		Subjects[2][1] = EE;
		Subjects[3][1] = EEL;
		Subjects[4][1] = IMSE;
		Subjects[5][1] = MME;
		Subjects[6][1] = ME;
		Subjects[7][1] = Other;
		
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
		int preCal = 0;
		int cal = 0;
		int cal2 = 0;
		int cal3 = 0;
		int difEq = 0;
		int Other = 0;
		
		String search = "What Math class do you need help with?";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
				
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
						
			try {
				if ((Double)cell.getNumericCellValue() == 1.0)
					preCal++;
				else if ((Double)cell.getNumericCellValue() == 2.0)
					cal++;
				else if ((Double)cell.getNumericCellValue() == 3.0)
					cal2++;
				else if ((Double)cell.getNumericCellValue() == 4.0)
					cal3++;
				else if ((Double)cell.getNumericCellValue() == 5.0)
					difEq++;
				else 
					Other++;
			} catch (Exception e) {
				continue;
			}
		}
		
		Subjects[0][1] = preCal;
		Subjects[1][1] = cal;
		Subjects[2][1] = cal2;
		Subjects[3][1] = cal3;
		Subjects[4][1] = difEq;
		Subjects[5][1] = Other;
		
		Print2DArrayWithPercents(Subjects,message);
	}
	
	public static void ReqBySubj_CHEM(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		message.addToMessage("Requests by Subject (CHEM)\n");
		Object[][] Subjects = {
							{"CHEM1 1305:",0},
							{"CHEM2 1306:",0},
							{"CHEM 1407:",0},
							{"Other:",0}};
		int chem1 = 0;
		int chem2 = 0;
		int chem1407 = 0;
		int Other = 0;
		
		String search = "What Chem class do you need help with?";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
		
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
						
			try {
				if ((Double)cell.getNumericCellValue() == 1.0)
					chem1++;
				else if ((Double)cell.getNumericCellValue() == 2.0)
					chem2++;
				else if ((Double)cell.getNumericCellValue() == 3.0)
					chem1407++;
				else 
					Other++;
			} catch (Exception e) {
				continue;
			}
		}
		
		Subjects[0][1] = chem1;
		Subjects[1][1] = chem2;
		Subjects[2][1] = chem1407;
		Subjects[3][1] = Other;
		
		Print2DArrayWithPercents(Subjects,message);
	}
	
	public static void ReqBySubj_PHYS(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		message.addToMessage("Requests by Subject (PHYS)\n");
		Object[][] Subjects = {
							{"PHYS:",0},
							{"Other:",0}};
		int phys = 0;
		int Other = 0;
		
		String search = "What subject do you need tutoring for?";
		int index = searchForHeadersIndex(search,headers);	//returns the index of the header, or if the header isn't found, returns -1	
		
		for (int rows = 0; rows < numRows; rows++) {
			Row row = sheet.getRow(rows); //gets rows
			Cell cell = row.getCell(index);
						
			try {
				if ((Double)cell.getNumericCellValue() == 4.0)
					phys++;
				else if ((Double)cell.getNumericCellValue() == 5.0)
					Other++;
			} catch (Exception e) {
				continue;
			}
		}
		
		Subjects[0][1] = phys;
		Subjects[1][1] = Other;
		
		Print2DArrayWithPercents(Subjects,message);
	}
	
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
	
	public static ArrayList<UniqueStudent> createStudentArray(XSSFSheet sheet, String[] headers, int numRows, globals message) {
		message.addToMessage("\n");
		message.addToMessage("SECTION FOR UNIQUE STUDENTS");
		message.addToMessage("\n");
		message.addToMessage("Number of Unique Students:\t");
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
	
	public static void BySubjects(ArrayList<UniqueStudent> students, globals message) {
		message.addToMessage("By Subjects\n");
		Object subj[][] = {
							{"ENGR",0},
							{"MATH",0},
							{"CHEM",0},
							{"PHYS",0},
							{"Other",0},
							};
		int ENGR = 0;
		int MATH = 0;
		int CHEM = 0;
		int PHYS = 0;
		int Other = 0;
		
		for (UniqueStudent student : students) {
			if(student.getSubj() == 1.0)
				MATH++;
			else if(student.getSubj() == 2.0)
				ENGR++;
			else if(student.getSubj() == 3.0)
				CHEM++;
			else if(student.getSubj() == 4.0)
				PHYS++;
			else
				Other++;
		}
		
		subj[0][1] = ENGR;
		subj[1][1] = MATH;
		subj[2][1] = CHEM;
		subj[3][1] = PHYS;
		subj[4][1] = Other;
		
		Print2DArrayWithPercents(subj,message);
	}

	public static void EngrBySubjects(ArrayList<UniqueStudent> students, globals message) {
		
		message.addToMessage("By Majors and Subjects\n");
		Object subj[][] = {
							{"CE",0,0,0,0,0},//
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

	public static void Majors(ArrayList<UniqueStudent> students, globals message) {
		message.addToMessage("Majors");
		Object majors[][] = {
							{"Civil Engineering",0},
							{"Electrical Engineering",0},
							{"Mechanical Engineering",0},
							/*{"Civil Engineering - Construction Management",0},*/
							{"Education and Engineering Leadership",0},
							{"Metallurgical and Materials Engineering",0},
							{"Computer Science",0},
							{"Industrial Engineering",0},
							{"Other",0},
							};
		int CE = 0;
		int EE = 0;
		int ME = 0;
		int EEL = 0;
		int MME = 0;
		int CS = 0;
		int IMSE = 0;
		int Other = 0;
		
		for (UniqueStudent student : students) {
			if(student.getMajor() == 1.0)
				CE++;
			else if(student.getMajor() == 2.0)
				EE++;
			else if(student.getMajor() == 3.0)
				ME++;
			else if(student.getMajor() == 4.0)
				CE++;
			else if(student.getMajor() == 5.0)
				EEL++;
			else if(student.getMajor() == 6.0)
				MME++;
			else if(student.getMajor() == 7.0)
				CS++;
			else if(student.getMajor() == 8.0)
				IMSE++;
			else
				Other++;
		}
		
		majors[0][1] = CE;
		majors[1][1] = EE;
		majors[2][1] = ME;
		majors[3][1] = EEL;
		majors[4][1] = MME;
		majors[5][1] = CS;
		majors[6][1] = IMSE;
		majors[7][1] = Other;
		
		Print2DArrayWithPercents(majors,message);
	}
	
	public static void Classification(ArrayList<UniqueStudent> students, globals message) {
		
		message.addToMessage("Classification");
		Object classification[][] = {
							{"Freshman",0},
							{"Sophomore",0},
							{"Junior",0},
							{"Senior",0},
							{"Graduate",0},
							};
		int fresh = 0;
		int soph = 1;
		int jun = 2;
		int sen = 3;
		int grad = 4;
		int index = 0;
		
		for (UniqueStudent student : students) {
			if(student.getClassification() == 1.0)
				index = fresh;
			else if(student.getClassification() == 2.0)
				index = soph;
			else if(student.getClassification() == 3.0)
				index = jun;
			else if(student.getClassification() == 4.0)
				index = sen;
			else
				index = grad;
			classification[index][1] = (int)classification[index][1] + 1;
		}
		
		Print2DArrayWithPercents(classification,message);
	}
}
