import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    private Scanner scanner;
    private boolean isRunning;

    public void runProgram() {
        scanner = new Scanner(System.in);
        isRunning = true;

        System.out.println("Welcome to the MCIT Course Helper!\n");
        while (isRunning) {
            optionPrompt();
            CourseCatalog courseCatalog = new CourseCatalog();
            courseCatalog.readDataIntoStructures();
            int option = readIntegerInput();

            switch (option) {
                case 1:
                    // Get suggested CIS courses
                    boolean quit = false;
                    while (!quit) {
                        System.out.println("Please select an option between 'course workload' and 'course quality':");
                        System.out.println("Type 'workload' or 'quality'");
                        String choice = scanner.next();
                        courseCatalog.getCourseSuggestions(choice);
                        System.out.println("Do you want to search again? (y/n)");
                        String response = scanner.next();
                        if (response.equals("n")) {
                            quit = true;
                        }
                    }
                    break;

                case 2:
                    // Search for Non-CIS courses
                    quit = false;
                    scanner.nextLine();
                    while (!quit) {
                        System.out.println("Please enter a search term:");
                        System.out.println("You can search by department code, course code or course name");
                        String search = scanner.nextLine();
                        courseCatalog.searchApprovedElective(search);
                        System.out.println("Do you want to search again? (y/n)");
                        String response = scanner.nextLine();
                        if (response.equals("n")) {
                            quit = true;
                        }
                    }
                    break;

                case 3:
                    // Make course cart
                    quit = false;
                    scanner.nextLine();
                    makeCart(courseCatalog);

                    while (!quit) {
                        courseCatalog.displayCart();
                        System.out.println();
                        System.out.println("Do you want to modify your cart? (y/n)");
                        String response = scanner.nextLine();
                        if (response.equals("y")) {
                            System.out.println("Please enter a course code to remove from your cart:");
                            String courseCode = scanner.nextLine();
                            courseCatalog.removeCourseFromCart(courseCode);
                            makeCart(courseCatalog);
                        }
                        if (response.equals("n")) {
                            courseCatalog.displayCart();
                            quit = true;
                        }
                    }
                    break;

                case 4:
                    // Pair with a mentor

                    break;

                case 5:
                    // Exit
                    System.out.println("Thanks for using!");
                    isRunning = false;
            }
        }

    }

    private void makeCart(CourseCatalog courseCatalog) {
        int cartSize = courseCatalog.getCourseCartSize();
        while (cartSize < 4) {
            int dif = 4 - cartSize;
            System.out.println("You need to add " + dif + " more courses to your cart.");
            System.out.println("Please enter a course code to add to your cart:");
            String courseCode = scanner.nextLine();
            if (courseCatalog.allowToCart(courseCode)) {
                courseCatalog.addCourseToCart(courseCode);
                cartSize++;
            }
        }
    }

    private void optionPrompt() {
        System.out.println("Please select an option:");
        System.out.println("1. Get suggested CIS courses");
        System.out.println("2. Search for Non-CIS courses");
        System.out.println("3. Modify course cart");
        System.out.println("4. Pair with a mentor");
        System.out.println("5. Exit\n");
    }

    private int readIntegerInput() {
        int option = 0;
        boolean isValidInput = false;
        while (!isValidInput) {
            try {
                option = scanner.nextInt();
                isValidInput = true;
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("Invalid input. Please enter a valid option (1 - 5).");
            }
        }
        return option;
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.runProgram();
    }
}