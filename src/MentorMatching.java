import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MentorMatching {
    private String fileName;
    private List<Mentor> mentors;

    public MentorMatching() {
        mentors = new ArrayList<>();
        fileName = "./data/mentor.json";
        readData();
    }

    private void readData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mentors = mapper.readValue(Paths.get(fileName).toFile(), new TypeReference<List<Mentor>>() {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Mentor findBestMatch(int[] userInterestScore) {
        Mentor bestMatch = null;
        double smallestDistance = Double.MAX_VALUE;

        for (Mentor mentor : mentors) {
            int[] mentorScores = mentor.getScores();
            double distance = calculateEuclideanDistance(userInterestScore, mentorScores);
            if (distance < smallestDistance) {
                smallestDistance = distance;
                bestMatch = mentor;
            }
        }

        return bestMatch;
    }

    private double calculateEuclideanDistance(int[] userScores, int[] mentorScores) {
        double sum = 0;
        for (int i = 0; i < userScores.length; i++) {
            sum += Math.pow(userScores[i] - mentorScores[i], 2);
        }
        return sum;
    }
}
