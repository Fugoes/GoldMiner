package goldminer;

import util.FP;

public class Main {
    public static void main(String[] argv) {
        if (argv.length == 0) {
            GUI gui = new GUI(0);
            gui.startTimerTask(30);
            State.getInstance().init();
            State.getInstance().randomInit();
            State.getInstance().start();
            gui.beginGameScreen();
            return;
        }

        int playerID = -1;
        switch (argv.length) {
            case 1:
                playerID = 0;
                break;
            case 2:
                playerID = 1;
                break;
        }
        GUI gui = new GUI(playerID);
        gui.startTimerTask(30);
        gui.beginWelcomeScreen();
        FP.liftExp(() -> Thread.sleep(4000)).run();
        gui.beginWaitingConnectionScreen();
        if (playerID == 0) {
            gui.connection = new Connections.TCPServer(
                    Integer.valueOf(argv[0]),
                    (id, time) -> State.getInstance().move(id, time),
                    () -> {
                        gui.beginGameScreen();
                        State.getInstance().start();
                    },
                    () -> {
                        State.getInstance().init();
                        State.getInstance().randomInit();
                        return State.getInstance().dumpEntities();
                    },
                    null,
                    State::pause,
                    State::resume
            );
        } else {
            gui.connection = new Connections.TCPClient(
                    argv[0],
                    Integer.valueOf(argv[1]),
                    (id, time) -> State.getInstance().move(id, time),
                    () -> {
                        gui.beginGameScreen();
                        State.getInstance().start();
                    },
                    null,
                    s -> {
                        State.getInstance().init();
                        State.getInstance().loadEntities(s.split(","));
                    },
                    State::pause,
                    State::resume
            );
        }
    }
}
