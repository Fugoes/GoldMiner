package goldminer;

import util.FP;

public class Main {
    public static void main(String[] argv) {
        int FPS = Integer.valueOf(argv[argv.length - 1]);

        if (argv.length == 1) {
            GUI gui = new GUI(0);
            gui.startTimerTask(FPS);
            State.init();
            State.randomInit();
            State.start();
            gui.beginGameScreen();
            return;
        }

        int playerID = -1;
        switch (argv.length) {
            case 2:
                playerID = 0;
                break;
            case 3:
                playerID = 1;
                break;
        }
        GUI gui = new GUI(playerID);
        gui.startTimerTask(FPS);
        gui.beginWelcomeScreen();
        FP.liftExp(() -> Thread.sleep(5000)).run();
        gui.beginWaitingConnectionScreen();
        if (playerID == 0) {
            gui.connection = new Connections.TCPServer(
                    Integer.valueOf(argv[0]),
                    State::move,
                    () -> {
                        gui.beginGameScreen();
                        State.start();
                    },
                    () -> {
                        State.init();
                        State.randomInit();
                        return State.getSnapshot().dumpEntities();
                    },
                    null,
                    State::pause,
                    State::resume,
                    () -> {
                        gui.beginCountDownScreen();
                        FP.liftExp(() -> Thread.sleep(3000)).run();
                    }
            );
        } else {
            gui.connection = new Connections.TCPClient(
                    argv[0],
                    Integer.valueOf(argv[1]),
                    State::move,
                    () -> {
                        gui.beginGameScreen();
                        State.start();
                    },
                    null,
                    s -> {
                        State.init();
                        State.loadEntities(s.split(","));
                    },
                    State::pause,
                    State::resume,
                    () -> {
                        gui.beginCountDownScreen();
                        FP.liftExp(() -> Thread.sleep(3000)).run();
                    }
            );
        }
    }
}
