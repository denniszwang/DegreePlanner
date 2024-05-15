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
    private Map<String, String> nonCisMap;

    private String cisFilePath;
    private String nonCisFilePath;

    private CourseGraph courseGraph;
    private CourseNode courseRoot;
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
                nonCisMap = new HashMap<>();
                courseRoot = new CourseNode();
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
                    nonCisMap.put(code, name);
                    addCourse(code);
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

    private void addCourse(String courseCode) {
        if (courseCode == null || courseCode.isEmpty()) {
            return;
        }

        if (!isCisCourse(courseCode)) {
            return;
        }

        CourseNode current = courseRoot;
        for (int i = 0; i < courseCode.length(); i++) {
            char c = courseCode.charAt(i);
            int index = getCharIndex(c);
            if (current.getReferences()[index] == null) {
                current.getReferences()[index] = new CourseNode();
            }
            current.setPrefixes(current.getPrefixes() + 1);
            current = current.getReferences()[index];
        }
        current.setWords(current.getWords() + 1);
    }

    private boolean isCisCourse(String courseCode) {
        for (char c : courseCode.toCharArray()) {
            if (!Character.isLetter(c) && !Character.isDigit(c) && c != ' ') {
                return false;
            }
        }
        return true;
    }

    private int getCharIndex(char c) {
        if (c >= 'A' && c <= 'Z') {
            return c - 'A';
        } else if (c >= '0' && c <= '9') {
            return 26 + (c - '0');
        } else if (c == ' ') {
            return 36;
        } else {
            throw new IllegalArgumentException("Invalid character in course ID or name.");
        }
    }

    private CourseNode getSubTrie(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return null;
        }

        CourseNode current = courseRoot;
        for (int i = 0; i < prefix.length(); i++) {
            char c = prefix.charAt(i);
            int index = getCharIndex(c);
            if (index < 0 || index > 36) {
                return null;
            }
            if (current.getReferences()[index] == null) {
                return null;
            }
            current = current.getReferences()[index];
        }
        return current;
    }

    private List<String> autoComplete(String prefix) {
        prefix = prefix.toUpperCase();
        List<String> suggestions = new ArrayList<>();
        CourseNode current = getSubTrie(prefix);
        if (current == null) {
            return suggestions;
        }
        traverseTrie(current, suggestions, prefix);
        return suggestions;
    }

    private void traverseTrie(CourseNode node, List<String> suggestions, String prefix) {
        if (node.getWords() > 0) {
            suggestions.add(prefix);
        }
        for (int i = 0; i < node.getReferences().length; i++) {
            CourseNode next = node.getReferences()[i];
            if (next != null) {
                char nextChar = getIndexChar(i);
                traverseTrie(next, suggestions, prefix + nextChar);
            }
        }
    }

    private char getIndexChar(int index) {
        if (index >= 0 && index < 26) {
            return (char) ('A' + index);
        } else if (index >= 26 && index < 36) {
            return (char) ('0' + (index - 26));
        } else if (index == 36) {
            return ' ';
        } else {
            throw new IllegalArgumentException("Invalid index in trie.");
        }
    }

    // search for non-cis elective courses based on department id & course id
    private List<Course> searchElective(List<String> courseCode) {
        List<Course> coursesFound = new ArrayList<>();
        for (String code: courseCode) {
            String name = nonCisMap.get(code);
            Course course = new Course(code, name);
            coursesFound.add(course);
        }
        return coursesFound;
    }

    // search for non-cis elective courses based on the keyword
    private List<Course> searchElective(String search) {
        List<Course> matchingCourses = new ArrayList<>();
        String matchingWord = "";
        int minDistance = Integer.MAX_VALUE;

        // Convert the search term to uppercase for case-insensitive comparison
        String searchedItem = search.toLowerCase();

        for (Course course : nonCisCourses) {
            String[] courseNameWords = course.getName().toLowerCase().split("\\s+");
            for (String word: courseNameWords) {
                if (word.equals(searchedItem)) {
                    matchingCourses.add(course);
                    minDistance = 0;
                }
            }

            // Calculate Levenshtein distance between the search term and course name
            for (String word : courseNameWords) { // Split the course name into individual words
                int wordLen = word.length();
                int searchLen = searchedItem.length();
                if (wordLen < searchLen - 1 || wordLen > searchLen + 1) {
                    continue;
                }
                int distance = levenshteinDistance(word, searchedItem);
                if (distance < 3 && distance < minDistance) {
                    minDistance = distance;
                    matchingCourses.add(course);
                    matchingWord = word;
                }
            }
        }

        if (minDistance == 0) {
            for (Course c: matchingCourses) {
                System.out.println(c + "is a pre-approved non-CIS elective!");
            }
        }
        else if (minDistance <= 2){
            System.out.println("No exact match found for \"" + search.toLowerCase() + "\". Did you mean \"" +
                    matchingWord.toLowerCase() + "\"?");
        } else {
            System.out.println("No matching elective found!");
        }

        return matchingCourses;
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

    //Combine non-cis searching function
    public List<Course> searchApprovedElective(String prefix) {
        String header = "";
        if (prefix.length() < 4) {
            header = prefix.toUpperCase();
        } else if (prefix.contains(" ")){
            header = prefix.substring(0, 4).trim().toUpperCase();
        }

        String pattern = "^[A-Z]{2,4}$";
        boolean match = header.matches(pattern);

        if (match) {
            List<String> courseCode = autoComplete(prefix);
            return searchElective(courseCode);
        } else {
            return searchElective(prefix);
        }
    }

}
