package gui;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.*;
import java.util.*;

/**
 * Created by Szamani on 6/26/2015.
 */
public class Snake {
    private Group group;
    private Scene scene;
    private Map<String, Integer> directionsMapping;
    private Deque<Block> blocks;
    private Deque<Rectangle> rectangles;
    private Rectangle feedRectangle;
    private Rectangle regimeRectangle;
    private Rectangle obstacle1;
    private Rectangle obstacle2;
    private int feedX, feedY, regimeX, regimeY, obstacle1X, obstacle1Y, obstacle2X, obstacle2Y;
    private int feedCounter1 = 0;
    private int feedCounter2 = 0;
    private long currentTime;
    private Block head, tale;
    private boolean isFeed, isRegime;
    private boolean isEnd = false;
    private boolean isPause = false;
    private boolean isReload = false;
    private Random random;
    private final int down = 0;
    private final int right = 1;
    private final int up = 2;
    private final int left = 3;
    private final int[] dx = {0, 10, 0, -10};
    private final int[] dy = {10, 0, -10, 0};
    private int inputDirection;
    private String playerName;
    private int bestRecord;
    private String bestPlayerName;
    private Label scoreLabel;
    private int score = 0;
    private Timer timer;

    public Snake(String playerName, String inputDirection, Scene scene, Group group) {
        this.playerName = playerName;
        this.scene = scene;
        this.group = group;

        switch (inputDirection) {
            case "s":
                this.inputDirection = 0;
                break;

            case "d":
                this.inputDirection = 1;
                break;

            case "w":
                this.inputDirection = 2;
                break;

            case "a":
                this.inputDirection = 3;
                break;
        }

        directionsMapping = new HashMap<>();
        directionsMapping.put("S", 0);
        directionsMapping.put("D", 1);
        directionsMapping.put("W", 2);
        directionsMapping.put("A", 3);

        blocks = new ArrayDeque<>();
        rectangles = new ArrayDeque<>();

        head = new Block(100, 150);
        tale = new Block(90, 150);

        if (this.inputDirection != 3) {
            blocks.push(tale);
            blocks.push(head);

            rectangles.push(new Rectangle(tale.getX(), tale.getY(), 10, 10));
            rectangles.push(new Rectangle(head.getX(), head.getY(), 10, 10));
        } else {
            blocks.push(head);
            blocks.push(tale);

            rectangles.push(new Rectangle(head.getX(), head.getY(), 10, 10));
            rectangles.push(new Rectangle(tale.getX(), tale.getY(), 10, 10));
        }

        random = new Random(System.currentTimeMillis());

        isFeed = false;
        isRegime = false;

        scoreLabel = new Label(String.valueOf(score));
        scoreLabel.setLayoutX(240);
        scoreLabel.setLayoutY(0);

        group.getChildren().add(scoreLabel);

        feedRectangle = new Rectangle(10, 10);
        feedRectangle.setFill(Color.RED);
        feedRectangle.setX(-20);
        group.getChildren().add(feedRectangle);

        regimeRectangle = new Rectangle(10, 10);
        regimeRectangle.setFill(Color.GREEN);
        regimeRectangle.setX(-20);
        group.getChildren().add(regimeRectangle);

        obstacle1 = new Rectangle(150, 10);
        obstacle1.setFill(Color.BLUE);
        obstacle1.setY(-20);
        group.getChildren().add(obstacle1);

        obstacle2 = new Rectangle(10, 150);
        obstacle2.setFill(Color.BLUE);
        obstacle2.setX(-20);
        group.getChildren().add(obstacle2);

        setBestPlayer();
        setBoardListener();
        addFeed();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> play());
            }
        }, 1, 100);
    }

    private void setBestPlayer() {
        List<String> names = new ArrayList<>();
        List<Integer> scores = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("C:\\Users\\Szamani\\IdeaProjects\\tamrin32Clar\\src\\best.txt")));
            String nameTemp = br.readLine();
            String scoreTemp = br.readLine();

            String second = br.readLine();

            names.add(nameTemp);
            scores.add(Integer.parseInt(scoreTemp));

            while (second != null) {
                names.add(second);
                scores.add(Integer.parseInt(br.readLine()));

                second = br.readLine();
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        bestRecord = 0;
        for (int i = 0; i < scores.size(); i++)
            if (scores.get(i) > bestRecord) {
                bestRecord = scores.get(i);
                bestPlayerName = names.get(i);
            }
    }

    private void play() {
        if (isReload)
            reloadGame();
        if (isEnd|| isPause)
            return;

        addFeed();
        doesEatFeed();
        addRegime();
        doesEatRegime();
        crossBorder();

        head = blocks.peekFirst();

        Block newHead = new Block(head.getX() + dx[inputDirection], head.getY() + dy[inputDirection]);
        Rectangle headRect = new Rectangle(head.getX(), head.getY(), 10, 10);

        if (isEnd(newHead)) {
            isEnd = true;
//            Image image = new Image(this.getClass().getResourceAsStream("/gameOver.png"));
//            ImageView imageView = new ImageView(image);
//            group.getChildren().add(imageView);
//            scene.setOnKeyPressed(null);
            saveRecord();
            showRecord();
            return;
        } else {
            blocks.push(newHead);
            rectangles.push(headRect);

            blocks.pollLast();

            updateGui();
        }

    }

    private void reloadGame() {
        if (!isReload)
            return;

        score = 0;
        isReload = false;
        isEnd = false;
        feedCounter1 = 0;
        feedCounter2 = 0;

        List<Rectangle> rectangleList = new ArrayList<>(rectangles);
        for (Rectangle r:rectangleList)
            group.getChildren().remove(r);

        regimeRectangle.setX(-20);

        Deque<Block> blocksTemp = new ArrayDeque<>();
        Deque<Rectangle> rectanglesTemp = new ArrayDeque<>();

        head = new Block(100, 150);
        tale = new Block(90, 150);

        if (inputDirection != 3) {
            blocksTemp.push(tale);
            blocksTemp.push(head);

            rectanglesTemp.push(new Rectangle(tale.getX(), tale.getY(), 10, 10));
            rectanglesTemp.push(new Rectangle(head.getX(), head.getY(), 10, 10));
        } else {
            blocksTemp.push(head);
            blocksTemp.push(tale);

            rectanglesTemp.push(new Rectangle(head.getX(), head.getY(), 10, 10));
            rectanglesTemp.push(new Rectangle(tale.getX(), tale.getY(), 10, 10));
        }

        blocks = blocksTemp;
        rectangles = rectanglesTemp;

        group.getChildren().add(rectangles.getFirst());
        group.getChildren().add(rectangles.getLast());

        timer.cancel();
        timer = null;
        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> play());
            }
        }, 1, 100);

    }

    private void showRecord() {
        Label playerNameLabel = new Label(playerName);
        Label bestPlayerNameLabel = new Label(bestPlayerName);
        Label bestPlayerScoreLabel = new Label(String.valueOf(bestRecord));

        HBox hBox1 = new HBox(playerNameLabel, new Separator(Orientation.VERTICAL), scoreLabel);
        HBox hBox2 = new HBox(bestPlayerNameLabel, new Separator(Orientation.VERTICAL), bestPlayerScoreLabel);
        VBox vBox = new VBox(hBox1, new Separator(), hBox2);
        vBox.setLayoutX(200);
        vBox.setLayoutY(0);
        group.getChildren().add(vBox);
    }

    private void saveRecord() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("C:\\Users\\Szamani\\IdeaProjects\\tamrin32Clar\\src\\best.txt", true));

            bw.write(playerName);
            bw.newLine();
            bw.write(String.valueOf(score));
            bw.newLine();

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateGui() {
        if (isEnd)
            return;

        group.getChildren().remove(rectangles.peekLast());
        rectangles.pollLast();
        List<Rectangle> rectangleList = new ArrayList<>(rectangles);

        try {
            for (int i = 0; i < rectangleList.size() - 1; i++)
                group.getChildren().add(rectangleList.get(i));
        } catch (Exception e) {

        }
    }

    private boolean isEnd(Block newHead) {
        List<Block> blockTemp = new ArrayList<>(blocks);

        for (int i = 0; i < blockTemp.size(); i++)
            if (blockTemp.get(i).getX() == newHead.getX()&& blockTemp.get(i).getY() == newHead.getY())
                return true;

        if (newHead.getX() >= obstacle1.getX()&& newHead.getX() <= obstacle1.getX() + 150&&(
                (newHead.getY() >= obstacle1.getY()&& newHead.getY() <= obstacle1.getY() + 5)||
                (newHead.getY() <= obstacle1.getY()&& newHead.getY() >= obstacle1.getY() - 5)))
            return true;

        if (newHead.getY() >= obstacle2.getY()&& newHead.getY() <= obstacle2.getY() + 150&&(
                (newHead.getX() >= obstacle2.getX()&& newHead.getX() <= obstacle2.getX() + 5)||
                (newHead.getX() <= obstacle2.getX()&& newHead.getX() >= obstacle2.getX() - 5)))
            return true;

        return false;
    }

    private void doesEatRegime() {
        if (isEnd)
            return;

        if (Math.abs(blocks.peekFirst().getX() - regimeRectangle.getX()) < 10&& Math.abs(blocks.peekFirst().getY() - regimeRectangle.getY()) < 10&&
                System.currentTimeMillis() - currentTime <= 10000) {

            regimeRectangle.setX(1200);
            isRegime = false;
            ++score;
            scoreLabel.setText(String.valueOf(score));

            blocks.pollLast();
            blocks.pollLast();
            blocks.pollLast();

            group.getChildren().remove(rectangles.peekLast());
            rectangles.pollLast();
            group.getChildren().remove(rectangles.peekLast());
            rectangles.pollLast();
            group.getChildren().remove(rectangles.peekLast());
            rectangles.pollLast();
        }

        if (System.currentTimeMillis() - currentTime > 10000) {
            regimeRectangle.setX(1200);
            isRegime = false;
        }
    }

    private void addRegime() {
        if (isEnd)
            return;

        if (feedCounter2 - feedCounter1 != 4|| isRegime|| feedCounter2 == 0)
            return;

        feedCounter1 = feedCounter2;

        currentTime = System.currentTimeMillis();

        regimeX = Math.abs(random.nextInt()) % 400 + 10;
        regimeY = Math.abs(random.nextInt()) % 400 + 10;

        List<Block> blockTemp = new ArrayList<>(blocks);

        for (int i = 0; i < blockTemp.size(); i++)
            if (blockTemp.get(i).getX() < regimeX&& blockTemp.get(i).getY() < regimeY&& blockTemp.get(i).getX() + 10 > regimeX&& blockTemp.get(i).getY() + 10 > regimeY) {
                regimeX = Math.abs(random.nextInt()) % 400 + 10;
                regimeY = Math.abs(random.nextInt()) % 400 + 10;
            }

        regimeRectangle.setX(regimeX);
        regimeRectangle.setY(regimeY);

        isRegime = true;
    }

    private void doesEatFeed() {
        if (isEnd)
            return;

        if (Math.abs(blocks.peekFirst().getX() - feedRectangle.getX()) < 10&& Math.abs(blocks.peekFirst().getY() - feedRectangle.getY()) < 10) {
            feedRectangle.setX(1200);
            isFeed = false;
            ++feedCounter2;
            ++score;
            scoreLabel.setText(String.valueOf(score));

            head = blocks.peekFirst();
            Block newHead = new Block(head.getX() + dx[inputDirection], head.getY() + dy[inputDirection]);
            Rectangle headRect = new Rectangle(head.getX(), head.getY(), 10, 10);
            blocks.push(newHead);
            rectangles.push(headRect);
        }
    }

    private void addFeed() {
        if (isEnd)
            return;

        if (isFeed)
            return;

        feedX = Math.abs(random.nextInt()) % 400 + 10;
        feedY = Math.abs(random.nextInt()) % 400 + 60;

        List<Block> blockTemp = new ArrayList<>(blocks);

        for (int i = 0; i < blockTemp.size(); i++)
            if (blockTemp.get(i).getX() < feedX&& blockTemp.get(i).getY() < feedY&& blockTemp.get(i).getX() + 10 > feedX&& blockTemp.get(i).getY() + 10 > feedY) {
                feedX = Math.abs(random.nextInt()) % 400 + 10;
                feedY = Math.abs(random.nextInt()) % 400 + 60;
            }

        feedRectangle.setX(feedX);
        feedRectangle.setY(feedY);

        isFeed = true;

        addObstacle();
    }

    private void addObstacle() {
        if (isEnd)
            return;

        if (!isFeed)
            return;

        List<Block> blockTemp = new ArrayList<>(blocks);

        obstacle1X = Math.abs(random.nextInt()) % 400 + 10;
        obstacle1Y = Math.abs(random.nextInt()) % 400 + 10;

        for (int i = 0; i < blockTemp.size(); i++)
            if (((blockTemp.get(i).getX() < obstacle1X + 150&& blockTemp.get(i).getY() < obstacle1Y + 150)|| (blockTemp.get(i).getX() > obstacle1X + 150&& blockTemp.get(i).getY() > obstacle1Y + 150))&&
                    (feedRectangle.getX() < obstacle1X&& feedRectangle.getY() < obstacle1Y)|| (feedRectangle.getX() + 20 > obstacle1X&& feedRectangle.getY() + 20 > obstacle1Y)) {
                obstacle1X = Math.abs(random.nextInt()) % 400 + 10;
                obstacle1Y = Math.abs(random.nextInt()) % 400 + 10;
            }

        obstacle2X = Math.abs(random.nextInt()) % 400 + 10;
        obstacle2Y = Math.abs(random.nextInt()) % 400 + 10;

        for (int i = 0; i < blockTemp.size(); i++)
            if (((blockTemp.get(i).getX() < obstacle2X + 150&& blockTemp.get(i).getY() < obstacle2Y + 150) ||(blockTemp.get(i).getX() > obstacle2X + 150&& blockTemp.get(i).getY() > obstacle2Y + 150)) &&
                    (feedRectangle.getX() < obstacle2X - 10&& feedRectangle.getY() < obstacle2Y)|| (feedRectangle.getX() + 20 > obstacle2X&& feedRectangle.getY() + 20 > obstacle2Y)) {
                obstacle2X = Math.abs(random.nextInt()) % 400 + 10;
                obstacle2Y = Math.abs(random.nextInt()) % 400 + 10;
            }

        obstacle1.setX(obstacle1X);
        obstacle1.setY(obstacle1Y);
        obstacle2.setX(obstacle2X);
        obstacle2.setY(obstacle2Y);
    }

    private void setBoardListener() {
        scene.setOnKeyPressed(event -> {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    if (directionsMapping.keySet().contains(event.getCode().toString())) {
                        if ((inputDirection == 0&& event.getCode().toString().equals("W"))||
                                (inputDirection == 1&& event.getCode().toString().equals("A"))||
                                (inputDirection == 2&& event.getCode().toString().equals("S"))||
                                (inputDirection == 3&& event.getCode().toString().equals("D")))
                            return;

                        inputDirection = directionsMapping.get(event.getCode().toString());

                    }

                    if (event.getCode().equals(KeyCode.Q)) {
                        if (isPause)
                            isPause = false;
                        else isPause = true;
                    }

                    if (event.getCode().equals(KeyCode.R)) {
                        isReload = true;
                    }
                }
            };
            thread.start();
        });
    }

    private void crossBorder() {
        if (isEnd)
            return;

        List<Block> blockTemp = new ArrayList<>(blocks);
        switch (inputDirection) {
            case 0:
                for (int i = 0; i < blockTemp.size(); i++)
                    if (blockTemp.get(i).getY() >= 460) {
                        Block temp = blockTemp.get(i);
                        temp.setY(0);
                        blockTemp.set(i, temp);
                    }
                break;

            case 1:
                for (int i = 0; i < blockTemp.size(); i++)
                    if (blockTemp.get(i).getX() >= 490) {
                        Block temp = blockTemp.get(i);
                        temp.setX(0);
                        blockTemp.set(i, temp);
                    }
                break;

            case 2:
                for (int i = 0; i < blockTemp.size(); i++)
                    if (blockTemp.get(i).getY() <= 0) {
                        Block temp = blockTemp.get(i);
                        temp.setY(450);
                        blockTemp.set(i, temp);
                    }
                break;

            case 3:
                for (int i = 0; i < blockTemp.size(); i++)
                    if (blockTemp.get(i).getX() <= 0) {
                        Block temp = blockTemp.get(i);
                        temp.setX(480);
                        blockTemp.set(i, temp);
                    }
                break;
        }

        Deque<Block> blocksTemp = new ArrayDeque<>();

        for (int i = blockTemp.size() - 1; i >= 0; i--)
            blocksTemp.push(blockTemp.get(i));

        blocks = blocksTemp;
    }
}
