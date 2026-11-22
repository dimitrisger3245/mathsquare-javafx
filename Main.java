package gerovasileioufx; 

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main extends Application {

    private static final int TILE_SIZE = 85;
    private static final int GAP = 6;

    private int size = 3; 
    private int[][] board;
    private Button[][] buttons;

    private int emptyRow;
    private int emptyCol;
    private int moves = 0;
    private int secondsElapsed = 0;

    private boolean gameActive = true;
    private boolean isAnimating = false;

    private GridPane gridPane;
    private Label movesLabel;
    private Label timerLabel;
    private Timeline timeline;
    private Stage primaryStage;

    private final Random random = new Random();

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2c3e50;");

        
        movesLabel = new Label("Κινήσεις: 0");
        movesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        movesLabel.setStyle("-fx-text-fill: white;");

        timerLabel = new Label("Χρόνος: 0s");
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        timerLabel.setStyle("-fx-text-fill: #1abc9c;");

        HBox statsBox = new HBox(25, movesLabel, timerLabel);
        statsBox.setAlignment(Pos.CENTER);

        
        ComboBox<String> sizeSelector = new ComboBox<>();
        sizeSelector.getItems().addAll("3×3 (8-Puzzle)", "4×4 (15-Puzzle)");
        sizeSelector.setValue("3×3 (8-Puzzle)");
        sizeSelector.setStyle("-fx-font-size: 13pt; -fx-font-weight: bold;");
        sizeSelector.setOnAction(e -> {
            size = sizeSelector.getValue().startsWith("3") ? 3 : 4;
            setupGrid();
            startNewGame();
            primaryStage.sizeToScene();
        });

        Button shuffleButton = new Button("Νέο Παιχνίδι");
        shuffleButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        shuffleButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 14;");
        shuffleButton.setOnAction(e -> startNewGame());

        HBox controlsBox = new HBox(15, sizeSelector, shuffleButton);
        controlsBox.setAlignment(Pos.CENTER);

        VBox topContainer = new VBox(12, statsBox, controlsBox);
        topContainer.setAlignment(Pos.CENTER);
        topContainer.setPadding(new Insets(0, 0, 20, 0));
        root.setTop(topContainer);

        
        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(GAP);
        gridPane.setVgap(GAP);
        gridPane.setStyle("-fx-background-color: #34495e; -fx-padding: 12; -fx-background-radius: 8;");
        root.setCenter(gridPane);

        
        setupTimeline();

        
        setupGrid();
        startNewGame();

        Scene scene = new Scene(root);
        primaryStage.setTitle("Sliding Puzzle");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void setupTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsElapsed++;
            timerLabel.setText("Χρόνος: " + secondsElapsed + "s");
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void setupGrid() {
        gridPane.getChildren().clear();
        board = new int[size][size];
        buttons = new Button[size][size];

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Button btn = new Button();
                btn.setPrefSize(TILE_SIZE, TILE_SIZE);
                btn.setFont(Font.font("Arial", FontWeight.BOLD, 22));

                final int row = r;
                final int col = c;
                btn.setOnAction(e -> handleTileClick(row, col));

                buttons[r][c] = btn;
                gridPane.add(btn, c, r);
            }
        }
    }

    private void startNewGame() {
        timeline.stop();
        isAnimating = false;

        
        int val = 1;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (r == size - 1 && c == size - 1) {
                    board[r][c] = 0;
                    emptyRow = r;
                    emptyCol = c;
                } else {
                    board[r][c] = val++;
                }
            }
        }

        
        int shuffleSteps = (size == 3) ? 150 : 350;
        shuffleBoard(shuffleSteps);

       
        moves = 0;
        secondsElapsed = 0;
        timerLabel.setText("Χρόνος: 0s");
        gameActive = true;
        updateUI();
        timeline.play();
    }

    private void shuffleBoard(int steps) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int prevRow = -1, prevCol = -1;

        for (int i = 0; i < steps; i++) {
            List<int[]> validNeighbors = new ArrayList<>();
            for (int[] dir : directions) {
                int nr = emptyRow + dir[0];
                int nc = emptyCol + dir[1];
                if (isValid(nr, nc) && !(nr == prevRow && nc == prevCol)) {
                    validNeighbors.add(new int[]{nr, nc});
                }
            }
            int[] chosen = validNeighbors.get(random.nextInt(validNeighbors.size()));
            prevRow = emptyRow;
            prevCol = emptyCol;
            swapInBoard(chosen[0], chosen[1]);
        }
    }


    private void handleTileClick(int r, int c) {
        if (!gameActive || isAnimating || board[r][c] == 0) {
            return;
        }

        if (Math.abs(r - emptyRow) + Math.abs(c - emptyCol) == 1) {
            animateAndSwap(r, c);
        }
    }

    private void animateAndSwap(int r, int c) {
        isAnimating = true;
        Button clickedBtn = buttons[r][c];
        clickedBtn.toFront(); 

        double deltaX = (emptyCol - c) * (TILE_SIZE + GAP);
        double deltaY = (emptyRow - r) * (TILE_SIZE + GAP);

        TranslateTransition transition = new TranslateTransition(Duration.millis(120), clickedBtn);
        transition.setByX(deltaX);
        transition.setByY(deltaY);

        transition.setOnFinished(event -> {
            clickedBtn.setTranslateX(0);
            clickedBtn.setTranslateY(0);

            swapInBoard(r, c);
            moves++;
            updateUI();
            isAnimating = false;

            if (checkWin()) {
                gameActive = false;
                timeline.stop();
                showWinAlert();
            }
        });

        transition.play();
    }

    private void swapInBoard(int r, int c) {
        board[emptyRow][emptyCol] = board[r][c];
        board[r][c] = 0;
        emptyRow = r;
        emptyCol = c;
    }

    private boolean isValid(int r, int c) {
        return r >= 0 && r < size && c >= 0 && c < size;
    }

    private void updateUI() {
        movesLabel.setText("Κινήσεις: " + moves);

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int value = board[r][c];
                Button btn = buttons[r][c];

                if (value == 0) {
                    btn.setText("");
                    btn.setStyle("-fx-background-color: #1a252f; -fx-background-radius: 6;");
                } else {
                    btn.setText(String.valueOf(value));
                    btn.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; " +
                                 "-fx-background-radius: 6; -fx-cursor: hand;");
                }
            }
        }
    }

    private boolean checkWin() {
        int expected = 1;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (r == size - 1 && c == size - 1) {
                    return board[r][c] == 0;
                }
                if (board[r][c] != expected++) {
                    return false;
                }
            }
        }
        return true;
    }

private void showWinAlert() {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.initOwner(primaryStage);
    alert.setTitle("Νίκη!");
    alert.setHeaderText(null);
    alert.setContentText(
        "Συγχαρητήρια! Το παζλ (" + size + "×" + size + ") λύθηκε σε " +
        moves + " κινήσεις και " + secondsElapsed + " δευτερόλεπτα!"
    );
    alert.showAndWait();
}

    public static void main(String[] args) {
        launch(args);
    }
}

    public static void main(String[] args) {
        launch(args);
    }
}
