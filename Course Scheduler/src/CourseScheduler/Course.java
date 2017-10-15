package CourseScheduler;

import java.util.*;

public class Course {
	
	private String code;
	private String name;
	private Set<Integer> sem = new HashSet<Integer>();
	private int level = 0;
	private List<Course> preReqs = new ArrayList<Course>();
	private List<Course> reqs = new ArrayList<Course>();
	private int length = 1;
	private int units = 2;
	private int reqLevel = 0;
	private int leastSemester = 0;
	
	public Course(String code, String name) {
		/* Constructor for the Course class. The Course Code is an 8-character code of the form XXXX0000, where
		 * XXXX is the course department's 4 letter code, and 0000 is a 4-digit number where the 1st digit is the course's
		 * level, and the other three distinguish the course uniquely from other courses of the same department and level
		 * The Course name is the English title of the course.
		 */
		this.code = code;
		this.name = name;
		this.level = code.charAt(4) - 48;
	}
	
	public Course(String code) {
		this.code = code;
	}
	
	public Course(String code, int leastSem, int sem1, int sem2, int length, int units) {
		this.code = code;
		this.leastSemester = leastSem;
		if (sem1 == 1) {
			this.sem.add(0);
		}
		if (sem2 == 1) {
			this.sem.add(1);
		}
		this.length = length;
		this.units = units;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int setLeastSem() {
		return this.leastSemester;
	}
	
	public int getLeastSem() {
		return this.leastSemester;
	}
	
	public void setLength(int length) {
		this.length = length;
		this.units = length * 2;
	}
	
	public int getLength() {
		return this.length;
	}
	
	public int getUnits() {
		return this.units;
	}
	
	public void setSem(Set<Integer> sem) {
		/* Sets the offering semester of the course to 0: sem 1, 1: sem 2, 2: summer. */
		this.sem = sem;
	}
	
	public void addSem(int s) {
		this.sem.add(s);
	}
	
	public Set<Integer> getSem() {
		/* Returns the semester the course is offered in. */
		Set<Integer> sems = new TreeSet<Integer>();
		for (int c : this.sem) {
			sems.add(c);
		}
		return sems;
	}
	
	public int getLvl() {
		/* Returns the course level as parsed from the 5th character of the course code. */
		int lvl = this.level;
		return lvl;
	}
	
	public void addPreReq(Course pre) {
		/* Adds pre as a prerequisite of this course. */
		preReqs.add(pre);
	}
	
	public void addReq(Course req) {
		if (!reqs.contains(req)) {
			reqs.add(req);
			reqLevel = reqs.size();
		}
	}
	
	public int getReqLevel() {
		return this.reqLevel;
	}
	
	public void addAllReqs(List<Course> req) {
		for (Course c: req) {
			if (!reqs.contains(c)) {
				reqs.add(c);
				reqLevel = reqs.size();
			}
		}
	}
	
	public List<Course> getPreReqs() {
		/* Returns the list of this course's prerequisites. */
		List<Course> pre = new ArrayList<Course>();
		for (Course i:this.preReqs) {
			pre.add(i);
		}
		return pre;
	}
	
	public List<Course> getReqs() {
		/* Returns the list of the courses that require this course, recursively. */
		List<Course> req = new ArrayList<Course>();
		for (Course i:this.reqs) {
			req.add(i);
		}
		return req;
	}
	
	private boolean isPreReq(Course pre) {
		/* Return 1 if the Course pre is a prerequisite of this course. */
		return this.preReqs.contains(pre);
	}
	
	private boolean isReq(Course req) {
		return this.reqs.contains(req);
	}
	
	public String toString() {
		return this.code;
	}
	
	public boolean equals(Course c) {
		if (this.code.equals(c.code)) {
			return true;
		}
		return false;
	}
	
	public int hashCode() {
		return this.code.hashCode() + this.name.hashCode();
	}
}
