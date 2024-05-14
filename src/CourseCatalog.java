import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Paths;

public class CourseCatalog implements ICourseCatalog{

    private Map<String, List<String>> courseTopics;
    private List<Course> cisCourses;
    private List<String> cisCourseCodes;
    private List<Course> nonCisCourses;

    private String cisFilePath;
    private String nonCisFilePath;

    private CourseGraph courseGraph;
    private Scanner scanner;
    private final int numElectives = 4;

    public CourseCatalog() {
        this.cisFilePath = "./data/CIS.json";
        this.nonCisFilePath = "./data/non_cis_elective.csv";
    }

    @Override
    public void readDataIntoStructures() {
        readCISElectiveData();
        readNonCISElectiveData();
    }

    private void readCISElectiveData() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            cisCourses = mapper.readValue(Paths.get(cisFilePath).toFile(), new TypeReference<List<Course>>() {});
            cisCourseCodes = new ArrayList<>();
            for (Course course : cisCourses) {
                cisCourseCodes.add(course.getCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readNonCISElectiveData() {
            try (BufferedReader br = new BufferedReader(new FileReader(nonCisFilePath))) {
                br.readLine();
                String line;
                nonCisCourses = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length < 4) {
                        continue;
                    }
                    String departId = data[0].trim();
                    String courseId = data[1].trim();
                    String code = departId + " " + courseId;
                    String name = data[2].trim();
                    Course course = new Course(code, name);
                    nonCisCourses.add(course);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    //get the course suggestions based on the choice
    public List<Course> getCourseSuggestions(String choice) {
        buildGraph(choice);
        int rootIdx = cisCourses.indexOf(courseGraph.getRoot());
        List<Course> suggestions = modifiedPrime(courseGraph, rootIdx);
        return suggestions;
    }

    // build the graph based on the choice for the cis elective search
    private void buildGraph(String choice) {
        int size = cisCourses.size();
        courseGraph = new CourseGraph();
        courseGraph.init(cisCourses);
        for (int i = 0; i < size; i++) {
            Course course = cisCourses.get(i);
            if (course.getPrereqs() != null) {
                for (String prereq : course.getPrereqs()) {
                    int prereqIndex = cisCourseCodes.indexOf(prereq);
                    double edgeWeight = getEdgeWeight(course, choice);
                    courseGraph.addEdge(prereqIndex, i, edgeWeight);
                }
            }
        }
    }

    // get the edge weight based on the choice
    private double getEdgeWeight(Course course, String choice) {
        if (choice.equals("quality")) {
            return -course.getQuality();
        } else {
            return course.getWorkload();
        }
    }

    // modified prime algorithm to get the 4 suggested elective courses
    private List<Course> modifiedPrime(CourseGraph graph, int root) {
        int numNodes = graph.getNumNodes();
        double[] key = new double[numNodes];
        boolean[] visited = new boolean[numNodes];
        Arrays.fill(key, Double.MAX_VALUE);
        Arrays.fill(visited, false);

        key[root] = 0;
        int elecCount = 0;
        List<Course> suggestedCourses = new ArrayList<>();
        Queue<Integer> queue = new PriorityQueue<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return Double.compare(key[o1], key[o2]);
            }
        });

        System.out.println("Here are the 4 Suggested Elective Courses: ");
        queue.add(root);
        while (!queue.isEmpty() && elecCount <= numElectives) {
            int u = queue.poll();
            visited[u] = true;
            if (elecCount > 0) {
                suggestedCourses.add(cisCourses.get(u));
                System.out.println(cisCourses.get(u));
            }
            List<Course> neighbors = graph.getNeighbors(u);
            for (Course neighbor : neighbors) {
                int v = cisCourses.indexOf(neighbor);
                double weight = graph.getWeight(u, v);
                key[v] = weight;
                queue.add(v);
            }
            elecCount++;
        }
        System.out.println();
        return suggestedCourses;
    }

    // search for elective courses based on the department id, or course id, or course name
    public List<Course> searchElective(String search) {
        List<Course> electiveCourses = new ArrayList<>();
        int inputSize = search.length();

        for (Course course : nonCisCourses) {
            String courseName = course.getName().toUpperCase();
            String courseId = course.getCode();
            String departmentId = courseId.substring(0, 4).trim();
            String searchedItem = search.toUpperCase();
            if (inputSize <= 4) {
                if (departmentId.equals(searchedItem)) {
                    electiveCourses.add(course);
                }
            } else if (inputSize <= 9) {
                if (courseId.equals(searchedItem)) {
                    electiveCourses.add(course);
                    break;
                }
            } else {
                if (courseName.contains(searchedItem)) {
                    electiveCourses.add(course);
                    break;
                }

            }
        }

        if (inputSize <= 4) {
            //search by department id
            if (electiveCourses.size() > 1) {
                System.out.println("Here are pre-approved elective courses in " + search + " department: ");
                for (Course course : electiveCourses) {
                    System.out.println(course);
                }
            } else {
                System.out.println("No matching elective courses in " + search+ " department found!");
            }
        }  else if (electiveCourses.size() == 1) {
            //search by course id
            Course course = electiveCourses.get(0);
            System.out.println(course + " is a pre-approved elective course!");
        } else {
            System.out.println(search + " is not a pre-approved elective course!");
        }
        System.out.println();
        return electiveCourses;
    }

    @Override
    public List<Course> getCourses() {
        return null;
    }

    @Override
    public List<String> getTopics() {
        return null;
    }

    @Override
    public List<String> courseTopics(String courseName) {
        return null;
    }

    @Override
    public List<String> topicCourses(String topic) {
        return null;
    }
}
