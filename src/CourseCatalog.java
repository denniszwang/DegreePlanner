import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Paths;

public class CourseCatalog {
    private List<Course> cisCourses;
    private List<String> cisCourseCodes;
    private List<Course> nonCisCourses;
    private List<Course> courseCart;
    private Map<String, String> courseMap;

    private String cisFilePath;
    private String nonCisFilePath;

    private CourseGraph courseGraph;
    private CourseNode courseRoot;
    private final int numElectives = 4;
    private int cisCourseCount = 0;

    public CourseCatalog() {
        this.cisFilePath = "./data/CIS.json";
        this.nonCisFilePath = "./data/non_cis_elective.csv";
        this.courseCart = new ArrayList<>();
        this.courseMap = new HashMap<>();
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
                courseMap.put(course.getCode(), course.getName());
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
                courseRoot = new CourseNode();
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length < 4) {
                        continue;
                    }
                    String departId = data[0].trim();
                    String courseId = data[1].substring(0, 3).trim();
                    String code = departId + " " + courseId;
                    String name = data[2].trim();
                    Course course = new Course(code, name);
                    nonCisCourses.add(course);
                    courseMap.put(code, name);
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

    // add course to the trie data structure
    private void addCourse(String courseCode) {
        if (courseCode == null || courseCode.isEmpty()) {
            return;
        }

        if (!isValidCourse(courseCode)) {
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

    // check if the course code is valid
    private boolean isValidCourse(String courseCode) {
        for (char c : courseCode.toCharArray()) {
            if (!Character.isLetter(c) && !Character.isDigit(c) && c != ' ') {
                return false;
            }
        }
        return true;
    }

    // get the index of the character in the trie
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

    // get the subtrie based on the prefix
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

    // get the list of suggestions based on the prefix
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

    // traverse the trie to get the suggestions
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

    // get the character based on the index
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
            String name = courseMap.get(code);
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

    //Allow to add course to cart
    public boolean allowToCart(String courseCode) {
        courseCode = courseCode.toUpperCase();
        if (courseCart.size() >= 4) {
            System.out.println("You have reached the maximum number of courses in your cart.");
            return false;
        }
        if (courseCart.size() - cisCourseCount > 0 && !isCISElective(courseCode)) {
            System.out.println("You can only add 1 non-CIS courses to your cart.");
            return false;
        }
        for (Course c : courseCart) {
            if (c.getCode().equals(courseCode)) {
                System.out.println(c + " already in cart.");
                return false;
            }
        }
        String name = courseMap.get(courseCode);
        if (name == null) {
            System.out.println("Course not found in catalog.");
            return false;
        }
        if (isCISElective(courseCode)) {
            int id = cisCourseCodes.indexOf(courseCode);
            Course course = cisCourses.get(id);
            if (course.getPrereqs() != null) {
                boolean flag = false;
                for (String prereq : course.getPrereqs()) {
                    if (prereq.equals("CIT 59X") || courseCart.contains(new Course(prereq, courseMap.get(prereq)))) {
                        flag = true;
                    }
                }
                if (!flag) {
                    System.out.println("You need to add the prerequisites for " + courseCode + " to your cart.");
                    return false;
                }
            }
        }
        return true;
    }

    public void addCourseToCart(String courseCode) {
        courseCode = courseCode.toUpperCase();
        Course course;
        if (isCISElective(courseCode)) {
            cisCourseCount++;
            course = cisCourses.get(cisCourseCodes.indexOf(courseCode));
        } else {
            course = new Course(courseCode, courseMap.get(courseCode));
        }
        courseCart.add(course);
        System.out.println(course + " has been added to your cart.");
    }

    public void removeCourseFromCart(String courseCode) {
        courseCode = courseCode.toUpperCase();
        for (Course course : courseCart) {
            if (course.getCode().equals(courseCode)) {
                courseCart.remove(course);
                if (isCISElective(courseCode)) {
                    cisCourseCount--;
                }
                System.out.println(courseCode + " has been removed from your cart.");
                return;
            }
        }
        System.out.println("Course not found in cart.");
    }

    public void displayCart() {
        System.out.println("Your cart contains the following courses:");
        for (Course course : courseCart) {
            System.out.println(course);
        }
    }

    public void clearCart() {
        courseCart.clear();
        cisCourseCount = 0;
        System.out.println("Your cart has been cleared.");
    }

    public int getCourseCartSize() {
        return courseCart.size();
    }

    //Check if the course is a CIS elective
    private boolean isCISElective(String courseCode) {
        return cisCourseCodes.contains(courseCode);
    }

    //Calculate the interest scores based on the courses in the cart
    public int[] convertToInterestScores() {
        int[] interestScores = new int[6];
        int sde = 0;
        int ds = 0;
        int research = 0;
        for (Course course : courseCart) {
            if (!isCISElective(course.getCode())) {
               interestScores[5] = 5;
               break;
            }
            List<String> topics = course.getTopics();
            if (topics.contains("Web")) {
               interestScores[4] = 5;
            }
            if (topics.contains("Graphics")) {
               interestScores[3] = 5;
            }
            if (topics.contains("Theory") || topics.contains("Systems")) {
                research++;
            }
            if (topics.contains("ai") || course.getName().toLowerCase().contains("data")) {
               ds++;
            }
            if (topics.contains("Security") || course.getName().toLowerCase().contains("software")) {
               sde++;
            }
        }
        if (research >= 2) {
            interestScores[2] = 5;
        } else if (research > 0) {
            interestScores[2] = 3;
        }

        if (ds >= 2) {
            interestScores[1] = 5;
        } else if (ds > 0) {
            interestScores[1] = 3;
        }

        if (sde >= 2) {
            interestScores[0] = 5;
        } else if (sde > 0) {
            interestScores[0] = 3;
        }

        return interestScores;
    }

}
