import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class testCourseCatalog {
    @Test
    public void testReadData() {
        CourseCatalog cc = new CourseCatalog();
        cc.readDataIntoStructures();
    }

    @Test
    public void testCourseSuggestions() {
        CourseCatalog cc = new CourseCatalog();
        cc.readDataIntoStructures();
        List<Course> result = cc.getCourseSuggestions("workload");
        assertEquals(4, result.size());
        Course lowestWorkload = result.get(0);
        Course thirdLowestWorkload = result.get(2);
        assertTrue(lowestWorkload.getWorkload() < thirdLowestWorkload.getWorkload());

        result = cc.getCourseSuggestions("quality");
        assertEquals(4, result.size());
        Course highestQuality = result.get(0);
        Course thirdHighestQuality = result.get(2);
        assertTrue(highestQuality.getQuality() > thirdHighestQuality.getQuality());
    }

    @Test
    public void testSearchElective() {
        CourseCatalog cc = new CourseCatalog();
        cc.readDataIntoStructures();
        List<Course> result = cc.searchElective("CIS");
        assertEquals(0, result.size());
        result = cc.searchElective("EAS");
        assertEquals(8, result.size());
        result = cc.searchElective("EAS 5000");
        assertEquals(1, result.size());
        result = cc.searchElective("EAS 5010");
        assertEquals(0, result.size());
        result = cc.searchElective("leadership");
        assertEquals(1, result.size());
        result = cc.searchElective("basketball");
        assertEquals(0, result.size());
    }
}
