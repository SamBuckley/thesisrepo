package CourseScheduler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Crawler {
	public static void main(String args[]) {
		
		Map<String, String> courses = new HashMap<String, String>();
		List<Course> courseList = new ArrayList<Course>();
		
		try {
			
			// Dialog to input plan code
			String plan = (String)JOptionPane.showInputDialog(new JFrame("Enter a program code"), "Please enter the desired program code.", "Enter a program code", JOptionPane.QUESTION_MESSAGE);
			
			System.out.println("Initializing...");
			URL UQUrl = new URL("https://www.uq.edu.au/study/plan_display.html?acad_plan=" + plan);
			
	        BufferedReader in = new BufferedReader(
	        new InputStreamReader(UQUrl.openStream()));
	        
	        String inputLine;
	        String courseCode = "";
	        int i = 0;
	        System.out.println("Reading...");

	        while ((inputLine = in.readLine()) != null) {
	        	
	        	// Stop when electives are reached - electives aren't important to the goal
//	        	if (inputLine.contains("<h2>Part B - Electives</h2>")) {
//	        		break;
//	        	}
	        	
	        	if (inputLine.startsWith(" <td>	<a href=\"/study/course.html?course_code=")) {
	        		courseCode = inputLine.substring(46, 54);
	        	}
	        	
	        	if (inputLine.startsWith(" <td>[	<a href=\"/study/course.html?course_code=")) {
	        		courseCode = inputLine.substring(47, 55);
	        	}
	        	i++;
	        	boolean skip = false;
	        	
	        	if (courseCode == "") {
	        		skip = true;
	        	}
	        	
	        	for (Course d: courseList) {
	        		if (d.getCode().equals(courseCode)) {
	        			skip = true;
	        		}
	        	}
	        	
	        	if (!skip) {
	        		System.out.println(courseCode);
	        		Course c = new Course(courseCode);
	        		courseList.add(c);
	        	}
	        }
	        
    		Double s = 0.0;
			Double percentage = 0.0;  
	 		for (Course c: courseList) {
    			percentage = BigDecimal.valueOf((s/courseList.size()) * 100).setScale(2, RoundingMode.HALF_UP).doubleValue();
				System.out.println(percentage + "%...");
    			Set<Integer> sems = new TreeSet<Integer>();
    			sems = getSemOffered(c);
    			c.setSem(sems);
    			List<Course> pre = getPreReqs(c, courseList);
    			for (Course d:pre) {
    				c.addPreReq(d);
    			}
    			s++;
    		}
	        
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Output Results
		System.out.println("Outputting...");
		
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int returnVal = fileChooser.showOpenDialog(fileChooser);
		
		Path outputFile = Paths.get(System.getProperty("user.home"));
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			outputFile = fileChooser.getSelectedFile().toPath();
			System.out.println(outputFile.toString());
		} else {
			System.exit(0);
		}
		
		outputFile = Paths.get(outputFile.toString(), "retrieverOutput.txt");
        if (!new File (outputFile.toString()).exists()) {
        	try {
        		Files.createFile(outputFile);
        	} catch (IOException e) {
        		System.out.println("Could not create output file");
        		System.exit(1);
        	}
        }
		
		try {
			BufferedWriter out = Files.newBufferedWriter(outputFile);
			
			out.write("64 0 0");
			out.newLine();
			
			for (Course c: courseList) {
				
				int sem1 = 0;
				int sem2 = 0;
				
				if (c.getSem().contains(0)) {
					sem1 = 1;
				}
				
				if (c.getSem().contains(1)) {
					sem2 = 1;
				}
				
				out.write(c.getCode() + " 0 " + sem1 + " " + sem2 + " " + c.getLength() + " " + c.getUnits());
				
				for (Course d: c.getPreReqs()) {
					out.write(" " + d.getCode());
				}
				out.newLine();
				out.flush();
			}
			
			out.close();
		} catch (IOException e) {
			System.out.println("Error outputting");
			System.exit(2);
		}
	}

	private static Set<Integer> getSemOffered(Course c) {
		/** Reads the offering semester of the course c from it's page on the university web site.
		 * @param c course who's semester offered is being searched for.
		 * @return the semester c is offered in. 0 is semester 1, 1 is semester 2, 2 is summer, and -1 if an error occurs
		 */
		Set<Integer> sems = new TreeSet<Integer>();
		try {
			URL courseUrl = new URL("https://www.uq.edu.au/study/course.html?course_code=" + c.getCode());
			
			BufferedReader in = new BufferedReader(
			new InputStreamReader(courseUrl.openStream()));
			
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.contains("Semester 1")) {
					sems.add(0);
				} else if (inputLine.contains("Semester 2")) {
					sems.add(1);
				} else if (inputLine.contains("Summer")) {
					sems.add(2);
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sems;
	}

	private static List<Course> getPreReqs(Course c, List<Course> cList) {
		/** Reads the prerequisite list of the course c from it's page on the university web site.
		 * @param c course who's prereq list is being searched for.
		 * @return a list of the courses that are part of this program that are prerequisites to c
		 */
		List<Course> pre = new ArrayList<Course>();
		try {
			URL courseUrl = new URL("https://www.uq.edu.au/study/course.html?course_code=" + c.getCode());
			
			BufferedReader in = new BufferedReader(
			new InputStreamReader(courseUrl.openStream()));
			
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.contains("course-prerequisite")) {
					for (Course d: cList) {
						if (inputLine.contains(d.getCode())) {
							if (!d.equals(c)) {
								pre.add(d);
							}
						}
					}
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pre;
	}
}
