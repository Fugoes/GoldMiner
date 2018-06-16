package goldminer;

import util.Coordinate;
import util.FP;
import util.ResTools;
import util.Tuple2;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class GUI {
    final static long END_TIME = 60 * 1000;
    final static Font FONT = ResTools.getFontFromRes("/xkcd.otf");
    final static BufferedImage IMAGE_ARROW_LEFT
            = ResTools.shrinkTo(ResTools.getImageFromRes("/arrow.png"), 80, 50);
    final static BufferedImage IMAGE_ARROW_RIGHT = ResTools.flipByY(IMAGE_ARROW_LEFT);
    final static float blurData[] = {0.0625f, 0.1250f, 0.0625f, 0.1250f, 0.2500f, 0.1250f, 0.0625f, 0.1250f, 0.0625f};
    final static ConvolveOp blurConvolveOp = new ConvolveOp(new Kernel(3, 3, blurData), ConvolveOp.EDGE_NO_OP, null);
    static AudioInputStream HOOK_SOUND;

    static {
        try {
            GUI.HOOK_SOUND = AudioSystem.getAudioInputStream(
                    GUI.class.getClassLoader().getResource("freesound-xylophone-a3-by-juancamiloorjuela.wav")
            );
        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    final Dimension vDim = new Dimension(1920, 1080);
    final Dimension rDim = new Dimension(1920, 1080);
    final BufferedImage image = new BufferedImage(vDim.width, vDim.height, BufferedImage.TYPE_INT_ARGB);
    final BufferedImage blurImage = new BufferedImage(vDim.width, vDim.height, BufferedImage.TYPE_INT_ARGB);
    final java.util.Timer timer = new java.util.Timer();

    int playerID;
    Connections.ConnectionBase connection;

    Frame frame;
    long lastSpaceDownTime = 0;
    boolean isPaused = false;

    interface Paintable {
        void paint(Graphics g, State state, long time);
    }

    void startTimerTask(int FPS) {
        this.timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                GUI.this.frame.repaint();
            }
        }, 0, 1000 / FPS);
    }

    GUI(int playerID) {
        this.frame = new Frame();
        this.frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension dim = GUI.this.frame.getContentPane().getSize();
                synchronized (GUI.this.rDim) {
                    GUI.this.rDim.width = dim.width;
                    GUI.this.rDim.height = dim.height;
                }
            }
        });
        this.frame.setSize(1280, 720);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setVisible(true);
        try {
            AudioInputStream bgm = AudioSystem.getAudioInputStream(
                    this.getClass().getResource("/bgm.wav")
            );
            Clip clip = AudioSystem.getClip();
            clip.open(bgm);
            FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            control.setValue(-15.0f);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
        this.playerID = playerID;
    }

    void beginWelcomeScreen() {
        final long zeroTime = Calendar.getInstance().getTimeInMillis();
        this.frame.setPaintFunction(g -> {
            Rectangle2D geom;
            Graphics bufferedG = GUI.this.image.getGraphics();
            bufferedG.setColor(Color.WHITE);
            bufferedG.fillRect(0, 0, GUI.this.vDim.width, GUI.this.vDim.height);
            bufferedG.setFont(GUI.FONT.deriveFont(Font.BOLD, 100));
            long time = Calendar.getInstance().getTimeInMillis() - zeroTime;
            if (time < 3000) {
                bufferedG.setColor(Color.BLACK);
            } else {
                int t = (int) (255 - 255 * (5000 - time) / 2000.0);
                t = t < 256 ? t : 255;
                t = t >= 0 ? t : 0;
                bufferedG.setColor(new Color(t, t, t));
            }
            geom = bufferedG.getFontMetrics().getStringBounds("Gold  Miner", bufferedG);
            bufferedG.drawString(
                    "Gold  Miner",
                    GUI.this.vDim.width / 2 - (int) (geom.getWidth() / 2),
                    GUI.this.vDim.height / 2
            );
            bufferedG.setFont(GUI.FONT.deriveFont(Font.BOLD, 40));
            geom = bufferedG.getFontMetrics().getStringBounds("Created  by  Fugoes  with  Love", bufferedG);
            bufferedG.drawString(
                    "Created  by  Fugoes  with  Love",
                    GUI.this.vDim.width - (int) geom.getWidth() - 10,
                    GUI.this.vDim.height - 10
            );
            GUI.this.drawBufferToScreen();
        });
    }

    void beginWaitingConnectionScreen() {
        this.frame.setPaintFunction(g -> {
            Rectangle2D geom;
            Graphics bufferedG = GUI.this.image.getGraphics();
            bufferedG.setColor(Color.WHITE);
            bufferedG.fillRect(0, 0, GUI.this.vDim.width, GUI.this.vDim.height);
            bufferedG.setFont(GUI.FONT.deriveFont(Font.BOLD, 100));
            bufferedG.setColor(Color.BLACK);
            geom = bufferedG.getFontMetrics().getStringBounds("Waiting  for  Connection...", bufferedG);
            bufferedG.drawString(
                    "Waiting  for  Connection...",
                    GUI.this.vDim.width / 2 - (int) (geom.getWidth() / 2),
                    GUI.this.vDim.height / 2
            );
            bufferedG.setFont(GUI.FONT.deriveFont(Font.BOLD, 40));
            geom = bufferedG.getFontMetrics().getStringBounds("Created  by  Fugoes  with  Love", bufferedG);
            bufferedG.drawString(
                    "Created  by  Fugoes  with  Love",
                    GUI.this.vDim.width - (int) geom.getWidth() - 10,
                    GUI.this.vDim.height - 10
            );
            GUI.this.drawBufferToScreen();
        });
    }

    void beginCountDownScreen() {
        final long time = Calendar.getInstance().getTimeInMillis();
        this.frame.setPaintFunction(g -> {
            Graphics bufferedG = GUI.this.image.getGraphics();
            bufferedG.setColor(Color.WHITE);
            bufferedG.fillRect(0, 0, GUI.this.vDim.width, GUI.this.vDim.height);
            bufferedG.setColor(Color.BLACK);
            bufferedG.setFont(GUI.FONT.deriveFont(Font.BOLD, 100));
            int n = (int) ((Calendar.getInstance().getTimeInMillis() - time) / 1000);
            n = 3 - n >= 0 ? 3 - n : 0;
            String s = Integer.toString(n);
            Rectangle2D geom = bufferedG.getFontMetrics().getStringBounds(s, bufferedG);
            bufferedG.drawString(s, 1920 / 2 - (int) geom.getWidth() / 2, 1080 / 2);
            GUI.this.drawBufferToScreen();
        });
    }

    void beginGameScreen() {
        this.frame.setPaintFunction(g -> {
            Graphics bufferedG = GUI.this.image.getGraphics();
            bufferedG.setColor(Color.WHITE);
            bufferedG.fillRect(0, 0, GUI.this.vDim.width, GUI.this.vDim.height);
            State state = State.getSnapshot();
            long time = state.getTime();
            {
                Hook hook = state.hooks[GUI.this.playerID];
                if (GUI.this.playerID == 0) {
                    bufferedG.drawImage(GUI.IMAGE_ARROW_LEFT,
                            hook.x - Hook.IMAGE_BOY.getWidth() - GUI.IMAGE_ARROW_LEFT.getWidth(),
                            hook.y - Hook.IMAGE_BOY.getHeight() - GUI.IMAGE_ARROW_LEFT.getHeight(), null);
                } else {
                    bufferedG.drawImage(GUI.IMAGE_ARROW_RIGHT,
                            hook.x + Hook.IMAGE_GIRL.getWidth(),
                            hook.y - Hook.IMAGE_GIRL.getHeight() - GUI.IMAGE_ARROW_RIGHT.getHeight(), null);
                }
            }
            if (time >= GUI.END_TIME) {
                int[] scores = state.getScores(GUI.END_TIME);
                if (scores[0] > scores[1]) {
                    GUI.this.beginResultScreen(0, scores[this.playerID], scores[1 - this.playerID]);
                } else if (scores[0] < scores[1]) {
                    GUI.this.beginResultScreen(1, scores[this.playerID], scores[1 - this.playerID]);
                } else {
                    GUI.this.beginResultScreen(-1, scores[this.playerID], scores[1 - this.playerID]);
                }
            } else {
                String s;
                Rectangle2D geom;
                state.traverseEntities(entity -> entity.paint(bufferedG, state, time));
                state.traverseHook(hook -> hook.paint(bufferedG, state, time));
                bufferedG.setFont(GUI.FONT.deriveFont(Font.BOLD, 45));
                bufferedG.setColor(Color.BLACK);
                int[] scores = state.getScores(time);
                s = "Score: " + scores[0];
                bufferedG.drawString(s, 20, 60);
                s = "Score: " + scores[1];
                geom = bufferedG.getFontMetrics().getStringBounds(s, bufferedG);
                bufferedG.drawString(s, 1900 - (int) geom.getWidth(), 60);
                s = "-" + Long.toString(60 - time / 1000 >= 0 ? 60 - time / 1000 : 0) + " S";
                geom = bufferedG.getFontMetrics().getStringBounds(s, bufferedG);
                bufferedG.drawString(s, 1920 / 2 - (int) geom.getWidth() / 2, 60);
                GUI.this.drawBufferToScreen();
            }
        });
        this.frame.setKeyAdapter(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                long realTime = Calendar.getInstance().getTimeInMillis();
                long time = State.getSnapshot().getTime();
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                        State.move(GUI.this.playerID, time);
                        GUI.this.connection.sendMove(GUI.this.playerID, time);
                        try {
                            Clip clip = AudioSystem.getClip();
                            synchronized (GUI.HOOK_SOUND) {
                                GUI.HOOK_SOUND.mark(Integer.MAX_VALUE);
                                clip.open(GUI.HOOK_SOUND);
                                GUI.HOOK_SOUND.reset();
                            }
                            clip.start();
                        } catch (LineUnavailableException | IOException e1) {
                            e1.printStackTrace();
                        }
                        break;
                    case KeyEvent.VK_SPACE:
                        if (GUI.this.playerID == 0) {
                            synchronized (GUI.this) {
                                if (GUI.this.lastSpaceDownTime + 400 < realTime) {
                                    GUI.this.lastSpaceDownTime = realTime;
                                    if (GUI.this.isPaused) {
                                        GUI.this.connection.sendResume();
                                        State.resume();
                                    } else {
                                        GUI.this.connection.sendPause(time + 300);
                                        State.pause(time + 300);
                                    }
                                    GUI.this.isPaused = !GUI.this.isPaused;
                                }
                                break;
                            }
                        } else {
                            GUI.this.connection.sendSpace();
                        }
                }
            }
        });
    }

    void beginResultScreen(int playerID, int score, int theOtherScore) {
        StringBuilder stringBuilder = new StringBuilder();
        this.frame.setPaintFunction(g -> {
            Rectangle2D geom;
            String s;
            Graphics bufferedG = GUI.this.image.getGraphics();
            bufferedG.setColor(Color.WHITE);
            bufferedG.fillRect(0, 0, GUI.this.vDim.width, GUI.this.vDim.height);
            bufferedG.setFont(GUI.FONT.deriveFont(Font.BOLD, 45));
            bufferedG.setColor(Color.BLACK);
            if (playerID == -1) {
                s = "Draw !";
            } else {
                if (playerID == GUI.this.playerID) {
                    s = "You  won !";
                } else {
                    s = "You  lose !";
                }
            }
            geom = bufferedG.getFontMetrics().getStringBounds(s, bufferedG);
            bufferedG.drawString(s, GUI.this.vDim.width / 2 - (int) (geom.getWidth() / 2),
                    GUI.this.vDim.height / 2 - 3 * (int) geom.getHeight());
            s = "Opponent  player's  Score:  " + Integer.toString(theOtherScore);
            geom = bufferedG.getFontMetrics().getStringBounds(s, bufferedG);
            bufferedG.drawString(s, GUI.this.vDim.width / 2 - (int) (geom.getWidth() / 2),
                    GUI.this.vDim.height / 2 - (int) geom.getHeight());
            s = "Your  Score:  " + Integer.toString(score);
            geom = bufferedG.getFontMetrics().getStringBounds(s, bufferedG);
            bufferedG.drawString(s, GUI.this.vDim.width / 2 - (int) (geom.getWidth() / 2),
                    GUI.this.vDim.height / 2 + (int) geom.getHeight());
            synchronized (stringBuilder) {
                s = "Your  Name:  " + stringBuilder.toString();
            }
            geom = bufferedG.getFontMetrics().getStringBounds(s, bufferedG);
            bufferedG.drawString(s, GUI.this.vDim.width / 2 - (int) (geom.getWidth() / 2),
                    GUI.this.vDim.height / 2 + 3 * (int) (geom.getHeight()));
            GUI.this.drawBufferToScreen();
        });
        this.frame.setKeyAdapter(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                synchronized (stringBuilder) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        GUI.this.beginBillboardScreen(stringBuilder.toString().trim(), score);
                    } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                        if (stringBuilder.length() > 0) {
                            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                        }
                    } else {
                        char c = e.getKeyChar();
                        if (stringBuilder.length() < 12) {
                            if ((c >= 'a' && c <= 'z')
                                    || (c >= 'A' && c <= 'Z')) {
                                stringBuilder.append(e.getKeyChar());
                            } else if (c == ' ') {
                                stringBuilder.append("  ");
                            }
                        }
                    }
                }
            }
        });
    }

    void beginBillboardScreen(String name, int score) {
        FP.liftExp(() -> {
            BufferedWriter bw = new BufferedWriter(new FileWriter("billboard.txt", true));
            bw.write(name + "," + score + "\n");
            bw.flush();
            bw.close();
        }).run();
        BufferedReader br = FP.liftExp(() -> new BufferedReader(new FileReader("billboard.txt"))).get().get();
        Vector<Tuple2<String, Integer>> history = new Vector<>();
        while (true) {
            Optional<String> l = FP.liftExp(br::readLine).get();
            l.ifPresent(line -> {
                String[] ss = line.split(",");
                history.add(new Tuple2<>(ss[0], Integer.valueOf(ss[1])));
            });
            if (!l.isPresent()) {
                break;
            }
        }
        FP.liftExp(br::close).run();
        // Collections.sort is stable
        Collections.sort(history, (o1, o2) -> {
            if (o1.t2 > o2.t2) {
                return -1;
            } else if (o1.t2 < o2.t2) {
                return 1;
            } else {
                return 0;
            }
        });
        Map<String, Integer> playerToScore = new HashMap<>();
        for (Tuple2<String, Integer> s : history) {
            if (playerToScore.containsKey(s.t1)) {
                playerToScore.replace(s.t1, playerToScore.get(s.t1) + s.t2);
            } else {
                playerToScore.put(s.t1, s.t2);
            }
        }
        Vector<Tuple2<String, Integer>> historyOfPlayer = new Vector<>();
        playerToScore.forEach((player, s) -> {
            historyOfPlayer.add(new Tuple2<>(player, s));
        });
        Collections.sort(historyOfPlayer, (o1, o2) -> {
            if (o1.t2 > o2.t2) {
                return -1;
            } else if (o1.t2 < o2.t2) {
                return 1;
            } else {
                return 0;
            }
        });

        StringBuilder sb;

        sb = new StringBuilder("\nPlayer\n\n");
        for (int i = 0; i < 5; i++) {
            if (i < history.size()) {
                Tuple2<String, Integer> entry = history.get(i);
                sb.append(entry.t1 + "\n");
            } else {
                break;
            }
        }
        String historyPlayers = sb.toString();

        sb = new StringBuilder("\nScore\n\n");
        for (int i = 0; i < 5; i++) {
            if (i < history.size()) {
                Tuple2<String, Integer> entry = history.get(i);
                sb.append(entry.t2 + "\n");
            } else {
                break;
            }
        }
        String historyScores = sb.toString();

        sb = new StringBuilder("\nPlayer\n\n");
        for (int i = 0; i < 5; i++) {
            if (i < historyOfPlayer.size()) {
                Tuple2<String, Integer> entry = historyOfPlayer.get(i);
                sb.append(entry.t1 + "\n");
            } else {
                break;
            }
        }
        String historyOfPlayerPlayers = sb.toString();

        sb = new StringBuilder("Total\nScore\n\n");
        for (int i = 0; i < 5; i++) {
            if (i < historyOfPlayer.size()) {
                Tuple2<String, Integer> entry = historyOfPlayer.get(i);
                sb.append(entry.t2 + "\n");
            } else {
                break;
            }
        }
        String historyOfPlayerScores = sb.toString();

        this.frame.setPaintFunction(g -> {
            Rectangle2D geom;
            String s;
            Graphics bufferedG = GUI.this.image.getGraphics();
            bufferedG.setColor(Color.WHITE);
            bufferedG.fillRect(0, 0, GUI.this.vDim.width, GUI.this.vDim.height);
            bufferedG.setFont(GUI.FONT.deriveFont(Font.BOLD, 60));
            bufferedG.setColor(Color.BLACK);

            s = "Billboard";
            geom = bufferedG.getFontMetrics().getStringBounds(s, bufferedG);
            bufferedG.drawString(s, GUI.this.vDim.width / 2 - (int) (geom.getWidth() / 2),
                    40 + 2 * ((int) geom.getHeight()));

            Tuple2<Integer, Integer> t1;
            Tuple2<Integer, Integer> t2;

            t1 = GUI.getStringBounds(historyPlayers, bufferedG);
            t2 = GUI.getStringBounds(historyScores, bufferedG);
            GUI.drawString(bufferedG, historyPlayers,
                    GUI.this.vDim.width / 4 - ((t1.t1 + t2.t1 + 50) / 2),
                    GUI.this.vDim.height / 2 - ((int) geom.getHeight()) * 5 / 2);
            GUI.drawString(bufferedG, historyScores,
                    GUI.this.vDim.width / 4 - ((t1.t1 + t2.t1 + 50) / 2) + t1.t1 + 50,
                    GUI.this.vDim.height / 2 - ((int) geom.getHeight()) * 5 / 2);

            t1 = GUI.getStringBounds(historyOfPlayerPlayers, bufferedG);
            t2 = GUI.getStringBounds(historyOfPlayerScores, bufferedG);
            GUI.drawString(bufferedG, historyOfPlayerPlayers,
                    GUI.this.vDim.width * 3 / 4 - ((t1.t1 + t2.t1 + 50) / 2),
                    GUI.this.vDim.height / 2 - ((int) geom.getHeight()) * 5 / 2);
            GUI.drawString(bufferedG, historyOfPlayerScores,
                    GUI.this.vDim.width * 3 / 4 - ((t1.t1 + t2.t1 + 50) / 2) + t1.t1 + 50,
                    GUI.this.vDim.height / 2 - ((int) geom.getHeight()) * 5 / 2);
            GUI.this.drawBufferToScreen();
        });
        this.frame.setKeyAdapter(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
            }
        });
    }

    class Frame extends JFrame {
        AtomicReference<Consumer<Graphics>> paintFunctionRef = new AtomicReference<>();
        AtomicReference<KeyAdapter> keyAdapterRef = new AtomicReference<>();

        Frame() {
            super("Gold Miner");
            this.paintFunctionRef.set(g -> {
            });
            this.keyAdapterRef.set(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                }
            });
            this.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    Frame.this.keyAdapterRef.get().keyPressed(e);
                }
            });
        }

        public void setPaintFunction(Consumer<Graphics> paintFunction) {
            this.paintFunctionRef.set(paintFunction);
        }

        public void setKeyAdapter(KeyAdapter keyAdapter) {
            this.keyAdapterRef.set(keyAdapter);
        }

        @Override
        public void paint(Graphics g) {
            this.paintFunctionRef.get().accept(g);
        }
    }

    private void drawBufferToScreen() {
        int width, height;
        int x, y;
        Graphics g = this.frame.getContentPane().getGraphics();
        g.setColor(Color.WHITE);
        synchronized (this.rDim) {
            x = this.rDim.width;
            y = this.rDim.height;
        }
        if (x * 1080 > y * 1920) {
            width = y * 1920 / 1080;
            height = y;
            x = (x - width) / 2;
            y = 0;
            g.fillRect(0, 0, x, height);
            g.fillRect(x + width, 0, x, height);
        } else {
            width = x;
            height = x * 1080 / 1920;
            x = 0;
            y = (y - height) / 2;
            g.fillRect(0, 0, width, y);
            g.fillRect(0, y + height, width, y);
        }
        GUI.blurConvolveOp.filter(this.image, this.blurImage);
        g.drawImage(this.blurImage, x, y, width, height, this.frame.getContentPane());
    }

    private static void drawString(Graphics g, String s, int x, int y) {
        for (String line : s.split("\n")) {
            g.drawString(line, x, y);
            y += g.getFontMetrics().getHeight();
        }
    }

    private static Tuple2 getStringBounds(String s, Graphics g) {
        double width = 0, height = 0;
        for (String line : s.split("\n")) {
            Rectangle2D geom = g.getFontMetrics().getStringBounds(line, g);
            if (geom.getWidth() > width) {
                width = geom.getWidth();
            }
            height += geom.getHeight();
        }
        return new Tuple2<>((int) width, (int) height);
    }
}
