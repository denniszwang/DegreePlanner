import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Paths;

public class CourseCatalog {

    private Map<String, List<String>> courseTopics;
    private List<Course> cisCourses;
    private List<String> cisCourseCodes;
    private List<Course> nonCisCourses;

    private String cisFilePath;
    private String nonCisFilePath;

    private CourseGraph courseGraph;
    private final int numElectives = 4;

    public CourseCatalog() {
        this.cisFilePath = "./data/CIS.json";
        this.nonCisFilePath = "./data/non_cis_elective.csv";
    }

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



    // search for elective courses based on the keyword
    public Course searchElective(String search) {
        Course matchingCourse = null;
        String matchingWord = "";
        int minDistance = Integer.MAX_VALUE;

        // Convert the search term to uppercase for case-insensitive comparison
        String searchedItem = search.toUpperCase();

        for (Course course : nonCisCourses) {
            String courseName = course.getName().toUpperCase();
            if (courseName.contains(searchedItem)) {
                matchingCourse = course;
                minDistance = 0;
                break;
            }

            // Calculate Levenshtein distance between the search term and course name
            for (String word : courseName.split("\\s+")) { // Split the course name into individual words
                int wordLen = word.length();
                int searchLen = searchedItem.length();
                if (wordLen < searchLen - 1) {
                    continue;
                }
                int distance = levenshteinDistance(word, searchedItem);
                if (distance < 3 && distance < minDistance) {
                    minDistance = distance;
                    matchingCourse = course;
                    matchingWord = word;
                }
            }
        }

        if (minDistance == 0) {
            System.out.println(matchingCourse + " is a pre-approved elective!");
        }
        else if (minDistance <= 2){
            System.out.println("No exact match found for \"" + search.toLowerCase() + "\". Did you mean \"" +
                    matchingWord.toLowerCase() + "\"?");
        }

        System.out.println("No matching elective found!");
        return matchingCourse;
    }

    // Calculate Levenshtein distance between two strings
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = s1.charAt(i - 1) == s2.charAt(j - 1) ?
                            dp[i - 1][j - 1] :
                            1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i][j - 1], dp[i - 1][j]));
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

}
