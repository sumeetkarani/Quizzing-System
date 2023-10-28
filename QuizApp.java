import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class QuizApp {
    JFrame frame;
    JPanel panel;
    JLabel questionLabel;
    JRadioButton[] optionButtons;
    JButton nextButton;
    ButtonGroup optionGroup;
    int currentQuestion = 0;
    int score = 0;

    List<Question> questions = new ArrayList<>();
    HashMap<String, String> users = new HashMap<>();
    List<QuizResult> quizResults = new ArrayList<>();
    String currentUser;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            QuizApp quizApp = new QuizApp();
            quizApp.createAndShowGUI();
        });
    }

    private QuizApp() {
        questions.add(new Question("What is the capital of France?",
                new String[] { "London", "Berlin", "Madrid", "Paris" }, 3));
        questions.add(new Question("Which planet is known as the Red Planet?",
                new String[] { "Mars", "Venus", "Jupiter", "Saturn" }, 0));
        questions.add(new Question("What is 2^3-2?",
                new String[] { "3", "4", "5", "6" }, 3));
        questions.add(new Question("Which is the most popular sport in the world??",
                new String[] { "Basketball", "Football", "Cricket", "Baseball" }, 1));

        users.put("user1", "pass1");
        users.put("user2", "pass2");
        users.put("user3", "pass3");
        users.put("user4", "pass4");
    }

    private void createAndShowGUI() {
        frame = new JFrame("Quiz App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel();
        panel.setLayout(new FlowLayout());

        questionLabel = new JLabel();
        panel.add(questionLabel);

        optionButtons = new JRadioButton[4];
        optionGroup = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JRadioButton();
            optionGroup.add(optionButtons[i]);
            panel.add(optionButtons[i]);
            int finalI = i;
            optionButtons[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    checkAnswer(finalI);
                }
            });
        }

        nextButton = new JButton("Next");
        panel.add(nextButton);
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currentQuestion < questions.size() - 1) {
                    currentQuestion++;
                    displayQuestion(currentQuestion);
                } else {
                    displayResult();
                }
            }
        });

        frame.getContentPane().add(panel);
        frame.setSize(400, 300);
        frame.setVisible(true);

        loadQuizResults();

        String username = JOptionPane.showInputDialog("Enter your username:");
        String password = JOptionPane.showInputDialog("Enter your password:");
        currentUser = authenticateUser(username, password);

        if (currentUser != null) {
            displayQuestion(currentQuestion);
        } else {
            JOptionPane.showMessageDialog(null, "Authentication failed. Invalid username or password.");
            frame.dispose();
        }
    }

    private void displayQuestion(int questionIndex) {
        Question question = questions.get(questionIndex);
        questionLabel.setText(question.getQuestion());
        String[] options = question.getOptions();
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(options[i]);
        }
    }

    private void checkAnswer(int selectedOption) {
        Question currentQuestion = questions.get(this.currentQuestion);
        if (selectedOption == currentQuestion.getCorrectOption()) {
            score++;
        }
    }

    private void displayResult() {
        String resultMessage = "Quiz Over! Your Score: " + score;
        questionLabel.setText(resultMessage);
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setEnabled(false);
        }
        nextButton.setEnabled(false);

        if (currentUser != null) {
            quizResults.add(new QuizResult(currentUser, score));
            displayRanking();
            saveQuizResults();
        }
    }

    private String authenticateUser(String username, String password) {
        if (users.containsKey(username) && users.get(username).equals(password)) {
            return username;
        }
        return null;
    }

    private void displayRanking() {
        Collections.sort(quizResults, Collections.reverseOrder());

        StringBuilder rankingMessage = new StringBuilder("Ranking:\n");
        for (int i = 0; i < quizResults.size(); i++) {
            QuizResult result = quizResults.get(i);
            rankingMessage.append(i + 1).append(". ").append(result.getUsername()).append(" - ")
                    .append(result.getScore()).append(" points\n");
        }

        JOptionPane.showMessageDialog(null, rankingMessage.toString());
    }

    private void saveQuizResults() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("quizResults.ser"))) {
            oos.writeObject(quizResults);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadQuizResults() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("quizResults.ser"))) {
            quizResults = (List<QuizResult>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private class Question {
        private String question;
        private String[] options;
        private int correctOption;

        public Question(String question, String[] options, int correctOption) {
            this.question = question;
            this.options = options;
            this.correctOption = correctOption;
        }

        public String getQuestion() {
            return question;
        }

        public String[] getOptions() {
            return options;
        }

        public int getCorrectOption() {
            return correctOption;
        }
    }

    private class QuizResult implements Comparable<QuizResult>, Serializable {
        private String username;
        private int score;

        public QuizResult(String username, int score) {
            this.username = username;
            this.score = score;
        }

        public String getUsername() {
            return username;
        }

        public int getScore() {
            return score;
        }

        public int compareTo(QuizResult other) {
            return Integer.compare(other.score, this.score);
        }
    }
}
