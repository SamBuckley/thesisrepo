package main;

import java.util.*;

public class CourseWeb {
	private List<Course> courses = new ArrayList<Course>();
	private Map<Course, Set<Course>> joins = new HashMap<Course, Set<Course>>();
	
	public CourseWeb() {
		
	}
	
	public void add(Course c) {
		courses.add(c);
		Set<Course> l = new HashSet<Course>();
		joins.put(c, l);
		this.updateJoins();
	}
	
	public void remove(Course c) {
		courses.remove(c);
		joins.remove(c);
		this.updateJoins();
	}
	
	public void updateJoins() {
		for (Course c: joins.keySet()){
			
			for (Course d: joins.get(c)) {
				if (!courses.contains(d)) {
					joins.get(c).remove(d);
				}
			}
		}
		for (Course c: courses) {
			for (Course p: c.getPreReqs()) {
				if (courses.contains(p)) {
					joins.get(p).add(c);
				}
			}
		}
	}
	
	public Set<Course> getJoins(Course c) {
		Set<Course> l = new HashSet<Course>();
		for (Course d: joins.get(c)) {
			l.add(d);
		}
		return l;
	}
	public Map<Course, Set<Course>> getJoinsList() {
		Map<Course, Set<Course>> j = new HashMap<Course, Set<Course>>();
		for (Course c: joins.keySet()) {
			j.put(c, joins.get(c));
		}
		return j;
	}
	
	public List<Course> getCourseList() {
		List<Course> l = new ArrayList<Course>();
		for (Course c: courses) {
			l.add(c);
		}
		return l;
	}
}
