package main;

import java.util.*;

public class Course {
	
	private String code;
	private String name;
	private Set<Integer> sem = new HashSet<Integer>();
	private int level = 0;
	private List<Course> preReqs = new ArrayList<Course>();
	private int length = 1;
	private int recommendedSemester = 1;
	private int allocatedSemester = 0;
	private boolean allocated = false;
	private int units = 2;
	
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
	
	public String getCode() {
		return this.code;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setRecSem(int s) {
		this.recommendedSemester = s;
	}
	
	public int getRecSem() {
		return this.recommendedSemester;
	}
	
	public void setAlcSem(int s) {
		this.allocatedSemester = s;
	}
	
	public int getAlcSem() {
		return this.allocatedSemester;
	}
	
	public void allocate() {
		this.allocated = true;
	}
	
	public boolean isAllocated() {
		return this.allocated;
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
	
	public List<Course> getPreReqs() {
		/* Returns the list of this course's prerequisites. */
		List<Course> pre = new ArrayList<Course>();
		for (Course i:this.preReqs) {
			pre.add(i);
		}
		return pre;
	}
	
	private boolean isPreReq(Course pre) {
		/* Return 1 if the Course pre is a prerequisite of this course. */
		return this.preReqs.contains(pre);
	}
	
	public String toString() {
		return this.code;
	}
	
	public boolean equals(Course c) {
		if (this.code.equals(c.code)) {
			if (this.name.equals(c.name)) {
				return true;
			}
		}
		return false;
	}
	
	public int hashCode() {
		return this.code.hashCode() + this.name.hashCode();
	}
}
