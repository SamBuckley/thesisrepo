package main;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.*;
import java.util.*;
import java.nio.file.*;

public class CourseScheduler  {
	
	public static void main(String args[]) {
		
		Map<String, String> courses = new HashMap<String, String>();
		List<Course> courseList = new ArrayList<Course>();
		
		try {
			System.out.println("Initializing...");
			URL UQUrl = new URL("https://www.uq.edu.au/study/plan_display.html?acad_plan=SOFTWX2342");
			
	        BufferedReader in = new BufferedReader(
	        new InputStreamReader(UQUrl.openStream()));
	        
	        String inputLine;
	        String courseCode = "";
	        String courseName = "";
	        int j = -4;
	        int i = 0;
	        System.out.println("Reading...");

	        while ((inputLine = in.readLine()) != null) {
	        	
	        	// Stop when electives are reached - electives aren't important to the goal
	        	if (inputLine.contains("<h2>Part B - Electives</h2>")) {
	        		break;
	        	}
	        	
	        	if (inputLine.startsWith(" <td>	<a href=\"/study/course.html?course_code=")) {
	        		j = i;
	        		courseCode = inputLine.substring(46, 54);
	        	}
	        	
	        	if (inputLine.startsWith(" <td>[	<a href=\"/study/course.html?course_code=")) {
	        		j = i;
	        		courseCode = inputLine.substring(47, 55);
	        	}
	        	
	        	if (i == j + 3) {
	        		courseName = inputLine.substring(5, inputLine.length() - 6);
	        		courses.put(courseCode, courseName);
	        	}
	        	//out.write(inputLine);
	        	//out.newLine();
	        	i++;
	        }
	        System.out.println("Populating List...");	
    		for (String k:courses.keySet()) {
    			Course c = new Course(k, courses.get(k));
    			if (c.getName().contains("Thesis")) {
    				c.setLength(2);
    			}
    			switch (c.getCode()) {
				case "ENGG1100": c.setRecSem(1);
					break;
				case "ENGG1300": c.setRecSem(1);
					break;
				case "MATH1051": c.setRecSem(1);
					break;
				case "CSSE1001": c.setRecSem(1);
					break;
				case "ENGG1200": c.setRecSem(2);
					break;
				case "MATH1052": c.setRecSem(2);
					break;
				case "INFS1200": c.setRecSem(2);
					break;
				case "MATH1061": c.setRecSem(2);
					break;
				case "CSSE2010": c.setRecSem(3);
					break;
				case "CSSE2002": c.setRecSem(3);
					break;
				case "COMP3506": c.setRecSem(4);
					break;
				case "CSSE2310": c.setRecSem(4);
					break;
				case "DECO2800": c.setRecSem(4);
					break;
				case "STAT2203": c.setRecSem(4);
					break;
				case "CSSE3002": c.setRecSem(5);
					break;
				case "DECO2500": c.setRecSem(5);
					break;
				case "ENGG2800": c.setRecSem(5);
					break;
				case "ENGG3800": c.setRecSem(6);
					break;
				case "ENGG4801": c.setRecSem(7);
					break;
				case "ENGG4900": c.setRecSem(8);
					break;
				default: break;
			}	
    			courseList.add(c);
    		}
    		
    		List<Course> toRemove = new ArrayList<Course>();
    		
    		for (Course c: courseList) {
    			if (c.getCode().equals("ENGG1211") || c.getCode().equals("MATH1071") || c.getCode().equals("MATH1072") || c.getCode().equals("ENGG4802") || c.getCode().equals("ENGG4805")) {
    				toRemove.add(c);
    			}
    			if (c.getCode().equals("ENGG4801")) {
    				for (Course d: courseList) {
    					if (d.getCode().equals("ENGG3800")) {
    						c.addPreReq(d);
    					}
    				}
    			}
    		}
    		
    		for (Course c: toRemove) {
    			courseList.remove(c);
    		}
    		
    		System.out.println("Getting Semesters and Prereqs...");
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
	        in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// There are some "OR" cases which i'll need to offer options to take - for now i'll just make the decision to always
		// take 1051 and 1052, as well as 1100 and 1200
		// and ENGG4801 - need to take care of it's double length too
		CourseWeb web = new CourseWeb();
		
		// Populate web of courses
		for (Course c: courseList) {
			web.add(c);
		}
		
		int currentSemester = 0;
		//Input completed courses from text file - first line is current semester
		Path inputFile = Paths.get(System.getProperty("user.home"), "output", "input.txt");
		List<String> completedCourseCodes = new ArrayList<String>();
		try {
			completedCourseCodes = Files.readAllLines(inputFile);
		} catch (IOException e) {
			System.out.println("No input file found...");
		}
		List<Course> completedCourses = new ArrayList<Course>();
		int q = 0;
		for (String s: completedCourseCodes) {
			if (q == 0) {
				currentSemester = Integer.parseInt(s);
			} else {
				for (Course c: courseList) {
					if (c.getCode().equals(s)) {
						completedCourses.add(c);
						c.allocate();
					}
				}
			}
			q++;
		}
		
		for (Course c: completedCourses) {
			if (courseList.contains(c)) {
				courseList.remove(c);
			}
		}
		
		CoursePlan plan = new CoursePlan();
		
		if (completedCourses.isEmpty()) {
			//Return default recommended order of courses
			//skip rest of steps
			
			for (Course c: courseList) {
				plan.addCourseAtSemester(c, c.getRecSem());
			}
			// Add Electives to pad semesters up to full course
			for (int i = 0; i < plan.getPlanLength(); i++) {
				while (plan.getSemester(i + 1).size() < 4 && plan.getPlanUnits() < 64) {
					Course c = new Course("Elective", "Elective");
					c.addSem(0);
					c.addSem(1);
					plan.addCourseAtSemester(c, i + 1);
				}
			}
			
			while (plan.getPlanUnits() < 64) {
				Course c = new Course("Elective", "Elective");
				c.addSem(0);
				c.addSem(1);
				if (plan.addNextCourse(c) == 0) {
					plan.addCourseAtSemester(c, plan.getPlanLength() + 1);
				}
			}
			
			// Method test / print test
			for (int i = 0; i < plan.getPlanLength(); i++) {
				List<Course> l = plan.getSemester(i + 1);
				System.out.println("Semester: " + (i + 1));
				for (Course c: l) {
					System.out.println(c.getCode());
				}
			}
			
			//Output plan
			System.out.println("Outputting...");
				Path outputFile = Paths.get(System.getProperty("user.home"), "output", "output.txt");
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
				Double s = 0.0;
				Double percentage = 0.0;
				for (Course c: courseList) {
					percentage = BigDecimal.valueOf((s/courseList.size()) * 100).setScale(2, RoundingMode.HALF_UP).doubleValue();
					System.out.println(percentage + "%...");
					out.write(c.getCode());
					out.newLine();
					out.write(c.getName());
					out.newLine();
					out.write("Level: " + c.getLvl());
					out.newLine();
					if (c.getSem().contains(0)) {
						out.write("Offered Summer");
						out.newLine();
					}
					if (c.getSem().contains(1)) {
						out.write("Offered Semester 1");
						out.newLine();
					}
					if (c.getSem().contains(2)) {
						out.write("Offered Semester 2");
						out.newLine();
					}
					out.write("Prerequisities (in the course list):");
					out.newLine();
					for (Course d:c.getPreReqs()) {
						out.write(d.getCode());
						out.newLine();
					}
					s++;
				}
				for (int i = 0; i < plan.getPlanLength(); i++) {
					List<Course> l = plan.getSemester(i + 1);
					out.write("Semester: " + (i + 1));
					out.newLine();
					for (Course c: l) {
						out.write(c.getCode());
						out.newLine();
					}
				}
				out.close();
				// Exit as we're done
				System.exit(0);
			} catch (IOException e) {
				System.out.println("Error outputting");
				System.exit(2);
			}
		} else {
			for (Course c: completedCourses) {
				plan.addPlanUnits(c.getUnits());
			}
			if (currentSemester % 2 == 0) {
				plan.addPlanUnits(-8);
			}
		}
		 
		for (Course c: courseList) {
			// Offset Recommended semester to rebase with current semester as semester 1
			c.setRecSem(c.getRecSem() - currentSemester + 1);
			if (c.getRecSem() < 0) {
				c.setRecSem(0);
			}
		}
		
		// Iterate over the courses finding the perfect spot for each one before placing any others
		// Get courses in recommended order
		
		List<Course> oList = new ArrayList<Course>();
		
		for (int i = 0; i < 12; i++) {
			for (Course c: courseList) {
				if (c.getRecSem() == i) {
					oList.add(c);
				}
			}
		}
		for (Course c: oList) {
			int i;
			if (currentSemester % 2 == 1) {
				//Starting in semester 1
				i = 0;
				while (c.getAlcSem() == 0) {
					if (plan.getSemester(i + 1).size() > 3) {
						i++;
						continue;
					}
					if (c.getSem().contains(i % 2)) {
						// Valid for this semester
						boolean check = true;
						for (Course d: c.getPreReqs()) {
							if (!d.isAllocated() || d.getAlcSem() > i + 1) {
								check = false;
							}
						}
						if (check) {
							if (plan.addCourseAtSemester(c, i + 1) == 0) {
								i++;
								continue;
							}
							c.setAlcSem(i + 1);
							c.allocate();
						}
					}
					i++;
				}
			} else {
				//Starting on semester 2
				i = 1;
				while (c.getAlcSem() == 0) {
					if (plan.getSemester(i + 1).size() > 3) {
						i++;
						continue;
					}
					if (c.getSem().contains(i % 2)) {
						// Valid for this semester
						boolean check = true;
						for (Course d: c.getPreReqs()) {
							if (!d.isAllocated() || d.getAlcSem() > i + 1) {
								check = false;
							}
						}
						if (check) {
							if (plan.addCourseAtSemester(c, i + 1) == 0) {
								i++;
								continue;
							}
							c.setAlcSem(i);
							c.allocate();
						}
					}
					i++;
				}
			}
		}
		
//		for (Course c: courseList) {
//			plan.addCourseAtSemester(c, c.getAlcSem());
//		}
		// Add Electives to pad semesters up to full course
		for (int i = 0; i < plan.getPlanLength(); i++) {
			while (plan.getSemester(i + 1).size() < 4 && plan.getPlanUnits() < 64) {
				Course c = new Course("Elective", "Elective");
				c.addSem(0);
				c.addSem(1);
				plan.addCourseAtSemester(c, i + 1);
			}
		}
		
		while (plan.getPlanUnits() < 64) {
			Course c = new Course("Elective", "Elective");
			c.addSem(0);
			c.addSem(1);
			if (plan.addNextCourse(c) == 0) {
				plan.addCourseAtSemester(c, plan.getPlanLength() + 1);
			}
		}
		
		// Method test / print test
		for (int i = 0; i < plan.getPlanLength(); i++) {
			List<Course> l = plan.getSemester(i + 1);
			System.out.println("Semester: " + (i + 1));
			for (Course c: l) {
				System.out.println(c.getCode());
			}
		}
		
		//Output plan
		System.out.println("Outputting...");
			Path outputFile = Paths.get(System.getProperty("user.home"), "output", "output.txt");
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
			Double s = 0.0;
			Double percentage = 0.0;
			for (Course c: courseList) {
				percentage = BigDecimal.valueOf((s/courseList.size()) * 100).setScale(2, RoundingMode.HALF_UP).doubleValue();
				System.out.println(percentage + "%...");
				out.write(c.getCode());
				out.newLine();
				out.write(c.getName());
				out.newLine();
				out.write("Level: " + c.getLvl());
				out.newLine();
				if (c.getSem().contains(0)) {
					out.write("Offered Summer");
					out.newLine();
				}
				if (c.getSem().contains(1)) {
					out.write("Offered Semester 1");
					out.newLine();
				}
				if (c.getSem().contains(2)) {
					out.write("Offered Semester 2");
					out.newLine();
				}
				out.write("Prerequisities (in the course list):");
				out.newLine();
				for (Course d:c.getPreReqs()) {
					out.write(d.getCode());
					out.newLine();
				}
				s++;
			}
			if (currentSemester % 2 == 1) {
				for (int i = 0; i < plan.getPlanLength(); i++) {
					List<Course> l = plan.getSemester(i + 1);
					out.write("Semester: " + (i + 1));
					out.newLine();
					for (Course c: l) {
						out.write(c.getCode());
						out.newLine();
					}
				}
			} else {
				for (int i = 1; i < plan.getPlanLength(); i++) {
					List<Course> l = plan.getSemester(i + 1);
					out.write("Semester: " + (i));
					out.newLine();
					for (Course c: l) {
						out.write(c.getCode());
						out.newLine();
					}
				}
			}
			out.close();
			// Exit as we're done
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Error outputting");
			System.exit(2);
		}
		
		// old method - this ordered the courses prioritising the ones that were the most required for other courses
		// this effectively finds the shortest path, however results in some hard courses without prerequisites like the
		// thesis being abnormally prioritised to like year 1
		
		// we want to still use the graph we made, but base our choices off of the recommended order which is 
		// fairly arbitrary but is a good way to complete the program
		
		// good idea - ability to provide a desired elective and integrate it into the solution, along with all requirements
		// for that elective - end goal to be able to specify all desired electives.
		
//		Map<Course, Set<Course>> joinsList = web.getJoinsList();
//		//Make list ordered on number of dependencies; number of dependencies := the class's "importance"
//		List<Course> oList = new LinkedList<Course>();
//		int max = 0;
//		Course mostRequired = new Course("CSSE2310", "dummy course");
//		for (int i = 0; i < courseList.size(); i++) {
//			for (Course c : courseList) {
//				// sort based on size of list of dependencies
//				if (joinsList.get(c).size() >= max) {
//					max = joinsList.get(c).size();
//					mostRequired = c;
//				}
//			}
//			// move the "most important" course left into the ordered list
//			oList.add(mostRequired);
//			courseList.remove(mostRequired);
//			max = 0;
//		}
//		//add the rest
//		for (Course c: courseList) {
//			oList.add(c);
//		}
//		System.out.println("Ordered List: ");
//		for (Course c: oList) {
//			System.out.println(c.getCode());
//		}
//		for (Course c: web.getCourseList()) {
//			System.out.println("Course: " + c.getCode());
//			System.out.println("Used for: ");
//			for (Course d: web.getJoins(c)) {
//				System.out.println(d);
//			}
//		}
//		
//		
//		
//		// Result List
//		List<Course> rList = new LinkedList<Course>();
//		// True if the current course can't be taken this semester
//		boolean skip = false;
//		// Number of courses enrolled in the current semester
//		int count = 0;
//		// Current semester number - 1
//		int semester = 0;
//		// Whether a course as been added on this round through the ordered list
//		boolean courseAdded = false;
//		// Continue until all courses are allocated (or until 30 semesters have been tried)
//		while (!oList.isEmpty()) {
//			//System.out.println("Semester " + (semester + 1) + ": ");
//			// Continue trying to fill semester until no courses can be added
//			while (true) {
//				courseAdded = false;
//				// Iterate over the list ordered on "course importance"
//				for (Course c: oList) {
//					skip = false;
//					// Check all c's prereqs are already done
//					for (Course d: c.getPreReqs()) {
//						if (!rList.contains(d)) {
//							skip = true;
//						}
//					}
//					// Check if c is already in the study plan
//					if (rList.contains(c)) {
//						skip = true;
//					}
//					// Check if c is offered in the current semester
//					if (!c.getSem().contains(semester % 2)) {
//						skip = true;
//					}
//					// Add c if it passes all checks
//					if (!skip) {
//						rList.add(c);
//						//System.out.println(c.getCode());
//						count++;
//						courseAdded = true;
//						if (count == 4) {
//							// Break from the run through the list if 4 courses are allocated
//							break;
//						}
//					}
//				}
//				
//				// Remove courses that have been added from the list
//				for (Course c: rList) {
//					oList.remove(c);
//				}
//				
//				// 4 Courses have been allocated to the current semester so move on
//				if (count == 4) {
//					count = 0;
//					break;
//				}
//				// All courses have been iterated over and none are viable so move on to the next semester
//				if (courseAdded == false) {
//					count = 0;
//					break;
//				}
//			}
////			for (Course c: oList) {
////				System.out.println("Course Unfitting: " + c.getCode());
////			}
//			semester++;
//			if (semester > 30) {
//				break;
//			}
//		}
//		System.out.println("Semesters: " + semester);
		
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
