import java.util.ArrayList;
import java.util.List;

public class CourseGraph {
    private double[][] graph;
    private List<Course> courses;

    public void init(List<Course> courses) {
        int n = courses.size();
        graph = new double[n][n];
        this.courses = courses;
    }

    public int getNumNodes() {
        return courses.size();
    }

    public Course getCourse(int id) {
        return courses.get(id);
    }

    public Course getRoot() {
        return courses.get(0);
    }

    public void addEdge(int id1, int id2, double weight) {
        graph[id1][id2] = weight;
    }

    public double getWeight(int id1, int id2) {
        return graph[id1][id2];
    }

    public List<Course> getNeighbors(int id) {
        List<Course> neighbors = new ArrayList<>();
        for (int i = 0; i < graph[id].length; i++) {
            if (graph[id][i] != 0) {
                neighbors.add(courses.get(i));
            }
        }
        return neighbors;
    }
}
