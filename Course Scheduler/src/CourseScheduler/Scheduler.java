package CourseScheduler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.swing.JFileChooser;

public class Scheduler {
	
	public static void main(String args[]) {
		
		List<Course> courseList = new ArrayList<Course>();
		Comparator<Course> comparator = new ReqComparator();
		
		int requiredUnits = 0;
		int i;
		int currentSemester = 0;
		int currentUnits = 0;
		int semester = 0;
		int initialSemester = 0;
		
		final JFileChooser fileChooser = new JFileChooser();
		
		int returnVal = fileChooser.showOpenDialog(fileChooser);
		
		File inputFile = new File("schedulerInput.txt");
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			inputFile = fileChooser.getSelectedFile();
		} else {
			System.exit(0);
		}
		
		Path input = inputFile.toPath();
		try (InputStream in = Files.newInputStream(input);
		    BufferedReader reader =
		      new BufferedReader(new InputStreamReader(in))) {
		    String line = null;
		    String parts[];
		    line = reader.readLine();
		    parts = line.split(" ");
		    requiredUnits = Integer.parseInt(parts[0]);
		    currentUnits = Integer.parseInt(parts[1]);
		    currentSemester = Integer.parseInt(parts[2]);
		    
		    while ((line = reader.readLine()) != null) {
		    	System.out.println(line);
		        parts = line.split(" ");
		        Course c = new Course(parts[0], Integer.parseInt(parts[1]) - currentSemester, Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]));
		        for (i = 6; i < parts.length; i++) {
		        	for (Course d: courseList) {
		        		if (d.getCode().equals(parts[i])) {
		        			System.out.println("Adding course " + d.toString() + " as a prereq for course " + c.toString());
		        			c.addPreReq(d);
		        		}
		        	}
		        }
		        courseList.add(c);
		    }
		} catch (IOException x) {
		    System.err.println(x);
		}
		System.out.println("Calculating...");
		//Calculate lists
		// Record one level of requirement
		for (Course c: courseList) {
			for (Course d: courseList) {
				if (c.getPreReqs().contains(d)) {
					System.out.println("Adding course " + d.toString() + " as a requirement for course " + c.toString());
					d.addReq(c);
				}
			}
		}
		System.out.println("Recursing...");
		// Make it recursive
		for (Course c: courseList) {
			recReqs(c);
		}
		System.out.println("Ordering...");
		//Order Courses
		PriorityQueue<Course> orderedList = new PriorityQueue<Course>(20, comparator);

		for (Course c: courseList) {
			orderedList.add(c);
		}
		
		initialSemester = currentSemester;
		
		System.out.println("Scheduling...");
		// Schedule Courses
		List<List<Course>> resultLists = new ArrayList<List<Course>>();
		boolean skip = false;
		boolean	courseAdded = false;
		int dud = 0;
		
		List<Course> list = new ArrayList<Course>();
		
		for (i = 0; i < currentSemester; i++) {
			resultLists.add(list);
		}
		
		
		resultLists.add(list);
		list = new ArrayList<Course>();
		resultLists.add(list);
		
		while (!orderedList.isEmpty() && currentUnits < requiredUnits) {
			while(countUnits(resultLists.get(currentSemester)) < 8) {
				courseAdded = false;
				for (Course c: orderedList) {
					System.out.println("Attempting to place " + c.toString() + " into semester" + currentSemester);
					skip = false;
					
					// Check if course already scheduled
					if (isInResultLists(resultLists, c) >= 0) {
						skip = true;
					}
					
					// Check that all pre reqs are scheduled before it
					for (Course d: c.getPreReqs()) {
						if (isInResultLists(resultLists, d) < 0 || isInResultLists(resultLists, d) >= currentSemester) {
							skip = true;
						}
					}
					
					// Check that the course is offered in this semester
					if (!c.getSem().contains((currentSemester % 2))) {
						skip = true;
					}
					
					// Check that we're late enough in the program to take this course
					if (currentSemester < c.getLeastSem()) {
						skip = true;
					}
					
					// Check there's unit space left in the semester
					if (c.getUnits() > 8 - countUnits(resultLists.get(currentSemester))) {
						skip = true;
					}
					
					// Check there's unit space for the course
					if (currentUnits + c.getUnits() > requiredUnits) {
						skip = true;
					}
					
					// Check if there's space in the next semester (only matter if lots of 2 semester courses)
					if (c.getLength() == 2 && countUnits(resultLists.get(currentSemester + 1)) > 6) {
						skip = true;
					}
					
					// Schedule the course in this semester if no flags are raised
					if (!skip) {
						resultLists.get(currentSemester).add(c);
						courseAdded = true;
						currentUnits += c.getUnits();
						if (c.getLength() == 2) {
							resultLists.get(currentSemester + 1).add(c);
							currentUnits += c.getUnits();
						}
						if (countUnits(resultLists.get(currentSemester)) == 8) {
							//semester full
							break;
						}
					}
				}
				for (Course c: courseList) {
					if (isInResultLists(resultLists, c) >= 0) {
						orderedList.remove(c);
					}
				}
				if (!courseAdded) {
					break;
				}
			}
			if (resultLists.get(currentSemester).size() == 0) {
				// No courses could fit into this semester, this can happen once, but twice means we're stuck
				dud++;
				if (dud == 2) {
					//break;
				}
			} else {
				// One empty semester does not a failed attempt make
				dud = 0;
			}
			// Go on to the next semester - create a new future list
			currentSemester++;
			list = new ArrayList<Course>();
			resultLists.add(list);
			System.out.println("Current allocated units: " + currentUnits);
		}
		
		//Output Results
		
		System.out.println("Outputting...");

		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		returnVal = fileChooser.showOpenDialog(fileChooser);
		
		Path outputFile = Paths.get(System.getProperty("user.home"));
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			outputFile = fileChooser.getSelectedFile().toPath();
		} else {
			System.exit(0);
		}
		
		outputFile = Paths.get(outputFile.toString(), "schedulerOutput.txt");
		
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
			semester = 0;
			for (List<Course> list2 : resultLists) {
				
				if (list2.isEmpty()) {
					continue;
				}
				
				if (0 == (semester + initialSemester) % 2) {
					out.write("Year " + ((semester + initialSemester) / 2 + 1) + ":");
					out.newLine();
				}
				out.write("Semester " + ((semester + initialSemester) % 2 + 1) + ":");
				out.newLine();
				for (Course c: list2) {
					out.write("    " + c.toString());
					out.newLine();
					out.flush();
				}
				int r = countUnits(list2);
				int p;
				if (8 - r < requiredUnits - currentUnits) {
					p = 8 - r;
				} else {
					p = requiredUnits - currentUnits;
				}
				if (r < 8 && currentUnits < requiredUnits) {
					out.write("    Electives - " + p + " Units");
					out.newLine();
					out.flush();
					currentUnits += p;
				}
				semester++;
			}
			
			out.close();
		} catch (IOException e) {
			System.out.println("Error outputting");
			System.exit(2);
		}
		
	}
	
	static int isInResultLists(List<List<Course>> lists, Course c) {
		for (List<Course> list: lists) {
			for (Course d: list) {
				if (d.equals(c)) {
					//System.out.println("Found course " + c.toString() + " in list " + lists.indexOf(list));
					return lists.indexOf(list);
				}
			}
		}
		return -1;
	}
	
	static int countUnits(List<Course> list) {
		int result = 0;
		for (Course c: list) {
			result += c.getUnits();
		}
		return result;
	}
	
	static List<Course> recReqs(Course c) {
		List<Course> reqs = new ArrayList<Course>();
		
		if (c.getReqs().isEmpty()) {
			return reqs;
		} else {
			for (Course d: c.getReqs()) {
				c.addAllReqs(recReqs(d));
			}
		}
		
		return reqs;
	}
}
