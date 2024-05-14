import java.util.List;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Course {
    private String Code;
    private String Name;
    private List<String> PreReqs;
    private double Quality;
    private double Workload;
    private List<String> Tags;

    public Course(String Code, String Name) {
        this.Code = Code;
        this.Name = Name;
    }

    @JsonCreator
    public Course(@JsonProperty("Code") String Code,
                  @JsonProperty("Name") String Name,
                  @JsonProperty("Pre-reqs") List<String> PreReqs,
                  @JsonProperty("Quality") double Quality,
                  @JsonProperty("Workload") double Workload,
                  @JsonProperty("Tags") List<String> Tags) {
        this.Code = Code;
        this.Name = Name;
        this.PreReqs = PreReqs;
        this.Quality = Quality;
        this.Workload = Workload;
        this.Tags = Tags;
    }

    public String getCode() {
        return this.Code;
    }

    public String getName() {
        return this.Name;
    }

    public List<String> getPrereqs() {
        return this.PreReqs;
    }

    public double getQuality() {
        return this.Quality;
    }

    public double getWorkload() {
        return this.Workload;
    }

    public List<String> getTopics() {
        return this.Tags;
    }

    @Override
    public String toString() {
        return this.Code + " " + this.Name;
    }
}
