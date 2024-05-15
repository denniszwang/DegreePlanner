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
        List<Course> result = cc.searchApprovedElective("leadership");
        assertEquals(1, result.size());
        result = cc.searchApprovedElective("basketball");
        assertEquals(0, result.size());
        List<Course> suggestion = cc.searchApprovedElective("EAS");
        assertEquals(8, suggestion.size());
        suggestion = cc.searchApprovedElective("EAS 50");
        assertEquals(2, suggestion.size());
        suggestion = cc.searchApprovedElective("CIS");
        assertEquals(0, suggestion.size());
    }

    @Test
    public void testCourseCart() {
        CourseCatalog cc = new CourseCatalog();
        cc.readDataIntoStructures();
        cc.addCourseToCart("CIS 550");
        cc.addCourseToCart("CIS 557");
        cc.addCourseToCart("CIS 559");
        cc.addCourseToCart("CIS 573");
        cc.removeCourseFromCart("CIS 557");
        cc.addCourseToCart("EAS 512");
        cc.displayCart();
        assertEquals(4, cc.getCourseCount());
        cc.clearCart();
        assertEquals(0, cc.getCourseCount());
        assertTrue(cc.isCISElective("CIS 550"));
        assertFalse(cc.isCISElective("CIS"));
        assertFalse(cc.isCISElective("EAS 512"));
    }
}
