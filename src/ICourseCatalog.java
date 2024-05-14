import java.util.List;

/**
 * Interface for the Course Catalog.
 * @author Andrew, Dennis & Thomas
 */

public interface ICourseCatalog {
    /**
     * Reads data from external sources and initializes the data structures
     * used by the Course Catalog.
     */
    public void readDataIntoStructures();

    /**
     * Returns the 4 suggested CIS-elective courses based on the user's choice.
     * @param choice the user's choice
     * @return a list of course suggestions
     */
    public List<Course> getCourseSuggestions(String choice);

    /**
     * Searches for non-CIS courses based on the user's search term.
     * @param search the user's search term
     * @return a list of non-CIS courses if type by department ID,
     * the course name if search by course name, or the course code
     * empty if no match is found
     */
    public List<Course> searchElective(String search);

    public List<Course> getCourses();

    public List<String> getTopics();

    public List<String> courseTopics(String courseName);

    public List<String> topicCourses(String topic);
}
