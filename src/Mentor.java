import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Mentor {
    private String name;
    private int[] scores = new int[6];

    @JsonCreator
    public Mentor(@JsonProperty("Name") String name,
                  @JsonProperty("sde") int sde,
                  @JsonProperty("research") int research,
                  @JsonProperty("data science") int dataScience,
                  @JsonProperty("computer graphics") int computerGraphics,
                  @JsonProperty("UI/UX") int uiUx,
                  @JsonProperty("project manager") int projectManager) {
        this.name = name;
        this.scores = new int[]{sde, research, dataScience, computerGraphics, uiUx, projectManager};
    }

    public String getName() {
        return this.name;
    }

    public int[] getScores() {
        return this.scores;
    }
    
}