package main;

import java.util.*;

public class CoursePlan {
	
	List<List<Course>> semesters = new ArrayList<List<Course>>();
	int units = 0;
	
	public CoursePlan() {
		List<Course> sem1 = new ArrayList<Course>();
		this.semesters.add(new ArrayList<Course>());
	}
	
	public int addNextCourse(Course c) {
		int l = this.semesters.size() - 1;
		
		for (List<Course> q: this.semesters) {
			if (q.contains(c)) {
				return 0;
			}
		}
		
		if (l + 1 % 2 == 0) {
			// this is a 2nd semester
			if (!c.getSem().contains(1)) {
				return 0;
			}
		} else {
			// this is a 1st semester
			if (!c.getSem().contains(0)) {
				return 0;
			}
		}
		
		if (this.semesters.get(l).size() < 4) {
			if (c.getLength() == 2) {
				this.semesters.add(new ArrayList<Course>());
				this.semesters.get(l + 1).add(c);
			}
			this.semesters.get(l).add(c);
			this.units += c.getUnits();
			return 1;
		} else {
			return 0;
		}
	}
	
	public int addCourseAtSemester(Course c, int i) {
		
		for (List<Course> q: this.semesters) {
			if (q.contains(c)) {
				return 0;
			}
		}
		
		if (i % 2 == 0) {
			// this is a 2nd semester
			if (!c.getSem().contains(1)) {
				return 0;
			}
		} else {
			// this is a 1st semester
			if (!c.getSem().contains(0)) {
				return 0;
			}
		}
		
		while (this.semesters.size() <= i - 1) {
			this.semesters.add(new ArrayList<Course>());
		}
		if (this.semesters.get(i - 1).size() < 4) {
			if (c.getLength() == 2) {
				if (this.semesters.size() <= i) {
					this.semesters.add(new ArrayList<Course>());
				} else if (this.semesters.get(i).size() > 3) {
					//The next semester is full and this course can't be slotted in here
					return 0;
				}
				this.semesters.get(i).add(c);
			}
			this.semesters.get(i - 1).add(c);
			this.units += c.getUnits();
			return 1;
		} else {
			return 0;
		}
	}
	
	public int getPlanLength() {
		return this.semesters.size();
	}
	
	public int getPlanUnits() {
		return this.units;
	}
	
	public void addPlanUnits(int u) {
		this.units += u;
	}
	
	public List<Course> getSemester(int i) {
		
		List<Course> l = new ArrayList<Course>();
		
		while (this.semesters.size() <= i - 1) {
			this.semesters.add(new ArrayList<Course>());
		}
		
		for (Course c: this.semesters.get(i - 1)) {
			l.add(c);
		}
		
		return l;
	}
	
}
