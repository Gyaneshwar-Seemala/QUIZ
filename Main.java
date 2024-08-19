import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {
    private static final Map<String, Map<String, String>> quizzes = new HashMap<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static final String CSV_DIR = "quizzes/";
    private static final String REPORT_DIR = "reports/";
    private static final int TOTAL_QUESTIONS = 5;
    private static final Set<String> teachers = new HashSet<>(Arrays.asList("teacher1", "teacher2")); // Sample teacher names

    private static final String RESET = "\033[0m";
    private static final String GREEN = "\033[0;32m";
    private static final String RED = "\033[0;31m";
    private static final String CYAN = "\033[0;36m";
    private static final String BOLD = "\033[1m";

    public static void main(String[] args) {
        File quizDir = new File(CSV_DIR);
        if (!quizDir.exists()) {
            quizDir.mkdirs();
        }
        File reportDir = new File(REPORT_DIR);
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }

        loadQuizzesFromCSV();

        System.out.println(BOLD + CYAN + "Welcome to the Quiz Competition System!" + RESET);
        System.out.println("Enter your role (teacher/student):");
        String role = scanner.nextLine().trim().toLowerCase();

        if (role.equals("teacher")) {
            handleTeacher();
        } else if (role.equals("student")) {
            handleStudent();
        } else {
            System.out.println(RED + "Invalid role. Please enter 'teacher' or 'student'." + RESET);
        }
    }

    private static void loadQuizzesFromCSV() {
        File quizDir = new File(CSV_DIR);
        for (File file : quizDir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".csv")) {
                String quizName = file.getName().replace(".csv", "");
                Map<String, String> questions = new HashMap<>();
                try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
                    String line;
                    reader.readLine(); // Skip header
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",", 2);
                        if (parts.length == 2) {
                            String question = parts[0].replace("\"", "");
                            String answer = parts[1].replace("\"", "");
                            questions.put(question, answer);
                        }
                    }
                } catch (IOException e) {
                    System.out.println(RED + "Error reading CSV file: " + e.getMessage() + RESET);
                }
                quizzes.put(quizName, questions);
            }
        }
    }

    private static void handleTeacher() {
        System.out.println(BOLD + CYAN + "Teacher Authentication" + RESET);
        System.out.println("Enter your name:");
        String name = scanner.nextLine().trim();

        if (teachers.contains(name)) {
            System.out.println(BOLD + GREEN + "Teacher authenticated." + RESET);
            System.out.println("Would you like to add questions or view quiz reports? (add/view)");
            String action = scanner.nextLine().trim().toLowerCase();

            if (action.equals("add")) {
                addQuestions();
            } else if (action.equals("view")) {
                viewQuizReports();
            } else {
                System.out.println(RED + "Invalid option. Please enter 'add' or 'view'." + RESET);
            }
        } else {
            System.out.println(RED + "Unauthorized access." + RESET);
        }
    }

    private static void addQuestions() {
        System.out.println(BOLD + CYAN + "Add Questions" + RESET);
        System.out.println("Enter the quiz name:");
        String quizName = scanner.nextLine().trim();

        Map<String, String> questions = new HashMap<>();
        System.out.println("Enter questions and answers. Type 'done' when finished.");

        while (true) {
            System.out.println("Enter a question:");
            String question = scanner.nextLine().trim();
            if (question.equalsIgnoreCase("done")) break;
            System.out.println("Enter the answer:");
            String answer = scanner.nextLine().trim();
            questions.put(question, answer);
        }

        quizzes.put(quizName, questions);
        saveQuestionsToCSV(quizName, questions);
        System.out.println(BOLD + GREEN + "Questions added and saved to quiz: " + quizName + RESET);
    }

    private static void saveQuestionsToCSV(String quizName, Map<String, String> questions) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(CSV_DIR + quizName + ".csv"))) {
            writer.write("Question,Answer\n");
            for (Map.Entry<String, String> entry : questions.entrySet()) {
                writer.write(String.format("\"%s\",\"%s\"\n", entry.getKey(), entry.getValue()));
            }
        } catch (IOException e) {
            System.out.println(RED + "Error writing CSV file: " + e.getMessage() + RESET);
        }
    }

    private static void handleStudent() {
        System.out.println(BOLD + CYAN + "Student Quiz" + RESET);
        System.out.println("Enter your name:");
        String name = scanner.nextLine().trim();

        System.out.println(BOLD + GREEN + "Welcome " + name + "! Select a quiz to take:" + RESET);
        List<String> quizNames = new ArrayList<>(quizzes.keySet());
        if (quizNames.isEmpty()) {
            System.out.println(RED + "No quizzes available." + RESET);
            return;
        }
        for (int i = 0; i < quizNames.size(); i++) {
            System.out.println((i + 1) + ". " + quizNames.get(i));
        }

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character
        if (choice < 1 || choice > quizNames.size()) {
            System.out.println(RED + "Invalid choice." + RESET);
            return;
        }

        String quizName = quizNames.get(choice - 1);
        System.out.println(BOLD + GREEN + "Starting quiz: " + quizName + RESET);

        int score = conductQuiz(name, quizName);
        System.out.println(BOLD + GREEN + name + "'s final score: " + score + "/" + TOTAL_QUESTIONS + RESET);

        saveStudentScore(quizName, name, score);
    }

    private static int conductQuiz(String studentName, String quizName) {
        Map<String, String> questions = quizzes.get(quizName);
        if (questions == null) {
            System.out.println(RED + "Quiz not found." + RESET);
            return 0;
        }

        List<String> questionsList = new ArrayList<>(questions.keySet());
        Collections.shuffle(questionsList); // Shuffle the list of questions to ensure random questions

        int score = 0;
        int count = 0;
        for (String question : questionsList) {
            if (count >= TOTAL_QUESTIONS) break; // Limit the number of questions
            System.out.println(BOLD + "Question " + (count + 1) + ":" + RESET + " " + BOLD + question + RESET);
            String userAnswer = scanner.nextLine().trim();
            if (userAnswer.equalsIgnoreCase(questions.get(question))) {
                System.out.println(GREEN + "Correct!" + RESET);
                score++;
            } else {
                System.out.println(RED + "Incorrect. The correct answer is " + BOLD + questions.get(question) + RESET + ".");
            }
            count++;
        }

        return score;
    }

    private static void saveStudentScore(String quizName, String studentName, int score) {
        File reportFile = new File(REPORT_DIR + quizName + "_report.csv");
        boolean fileExists = reportFile.exists();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile, true))) {
            if (!fileExists) {
                writer.write("Student Name,Marks Obtained,Total Marks,Percentage\n");
            }

            int totalMarks = TOTAL_QUESTIONS;
            double percentage = (score / (double) totalMarks) * 100;
            writer.write(String.format("%s,%d,%d,%.2f%%\n", studentName, score, totalMarks, percentage));
        } catch (IOException e) {
            System.out.println(RED + "Error writing report file: " + e.getMessage() + RESET);
        }
    }

    private static void viewQuizReports() {
        File reportDir = new File(REPORT_DIR);
        File[] reportFiles = reportDir.listFiles((dir, name) -> name.endsWith("_report.csv"));

        if (reportFiles == null || reportFiles.length == 0) {
            System.out.println(CYAN + "No saved reports available." + RESET);
            return;
        }

        System.out.println(BOLD + CYAN + "Saved Reports:" + RESET);
        for (int i = 0; i < reportFiles.length; i++) {
            System.out.println((i + 1) + ". " + reportFiles[i].getName().replace("_report.csv", ""));
        }

        System.out.println("Select a report to view:");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        if (choice < 1 || choice > reportFiles.length) {
            System.out.println(RED + "Invalid choice." + RESET);
            return;
        }

        File selectedFile = reportFiles[choice - 1];
        System.out.println(BOLD + CYAN + "Viewing report: " + selectedFile.getName() + RESET);

        List<String[]> rows = new ArrayList<>();
        String[] header = null;

        try (BufferedReader reader = Files.newBufferedReader(selectedFile.toPath())) {
            String line;
            boolean isStatistics = false;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Cumulative Statistics")) {
                    isStatistics = true;
                    continue;
                }

                if (header == null) {
                    header = line.split(",");
                } else {
                    rows.add(line.split(","));
                }
            }
        } catch (IOException e) {
            System.out.println(RED + "Error reading report file: " + e.getMessage() + RESET);
        }

        if (header != null) {
            System.out.printf("%-20s%-20s%-20s%-20s%n", header[0], header[1], header[2], header[3]);
            for (String[] row : rows) {
                System.out.printf("%-20s%-20s%-20s%-20s%n", row[0], row[1], row[2], row[3]);
            }
            System.out.println();
        }
    }
}
