public class CourseNode {
    private int words;
    private int prefixes;
    private CourseNode[] references;

    public CourseNode() {
        this.words = 0;
        this.prefixes = 0;
        this.references = new CourseNode[37]; // 26 letters + 10 digits + 1 space
    }

    public int getWords() {
        return words;
    }

    public void setWords(int words) {
        this.words = words;
    }

    public int getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(int prefixes) {
        this.prefixes = prefixes;
    }

    public CourseNode[] getReferences() {
        return references;
    }

    public void setReferences(CourseNode[] references) {
        this.references = references;
    }
}
