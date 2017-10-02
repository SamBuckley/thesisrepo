package CourseScheduler;

import java.util.Comparator;

public class ReqComparator implements Comparator<Course>{
	@Override
	public int compare(Course x, Course y) {
		if (x.getReqLevel() > y.getReqLevel()) {
			return -1;
		}
		if (x.getReqLevel() < y.getReqLevel()) {
			return 1;
		}
		return 0;
	}
}
