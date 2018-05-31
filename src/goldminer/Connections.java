package goldminer;

import util.FP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * master: send state
 */
public class Connections {
    public static abstract class ConnectionBase {
        boolean isMaster;
        BiConsumer<Integer, Long> receiveMoveCallback;
        Runnable gameStartFunction;
        Supplier<String> stateSupplier;
        Consumer<String> stateConsumer;
        Consumer<Long> pauseFunction;
        Runnable resumeFunction;
        Runnable countDownFunction;

        ConnectionBase(boolean isMaster,
                       BiConsumer<Integer, Long> receiveMoveCallback,
                       Runnable gameStartFunction,
                       Supplier<String> stateSupplier,
                       Consumer<String> stateConsumer,
                       Consumer<Long> pauseFunction,
                       Runnable resumeFunction,
                       Runnable countDownFunction) {
            this.isMaster = isMaster;
            this.receiveMoveCallback = receiveMoveCallback;
            this.gameStartFunction = gameStartFunction;
            this.stateSupplier = stateSupplier;
            this.stateConsumer = stateConsumer;
            this.pauseFunction = pauseFunction;
            this.resumeFunction = resumeFunction;
            this.countDownFunction = countDownFunction;
        }

        abstract void sendMove(int playerID, long time);

        abstract void sendPause(long time);

        abstract void sendResume();

        abstract Optional<String> receiveOneLine();

        void mainLoop() {
            while (true) {
                Optional<String> msg = this.receiveOneLine();
                if (msg.isPresent()) {
                    String _msg = msg.get();
                    System.err.println(_msg);
                    String[] args = _msg.split(",");
                    switch (args[0]) {
                        case "MOVE":
                            this.receiveMoveCallback.accept(Integer.valueOf(args[1]), Long.valueOf(args[2]));
                            break;
                        case "PAUSE":
                            this.pauseFunction.accept(Long.valueOf(args[1]));
                            break;
                        case "RESUME":
                            this.resumeFunction.run();
                            break;
                        default:
                            break;
                    }
                } else {
                    return;
                }
            }
        }
    }

    public static class TCPServer extends ConnectionBase {
        ServerSocket serverSocket;
        Socket socket;
        BufferedReader in;
        PrintWriter out;
        int port;

        TCPServer(int port,
                  BiConsumer<Integer, Long> receiveMoveCallback,
                  Runnable gameStartFunction,
                  Supplier<String> stateSupplier,
                  Consumer<String> stateConsumer,
                  Consumer<Long> pauseFunction,
                  Runnable resumeFunction,
                  Runnable countDownFunction) {
            super(
                    true,
                    receiveMoveCallback,
                    gameStartFunction,
                    stateSupplier,
                    stateConsumer,
                    pauseFunction,
                    resumeFunction,
                    countDownFunction
            );
            this.port = port;
            new Thread(() -> {
                this.waitForUp();
                String state = this.stateSupplier.get();
                this.out.println(state);
                String answer = FP.liftExp(() -> this.in.readLine()).get().get();
                this.countDownFunction.run();
                if (answer.equals("START")) {
                    this.gameStartFunction.run();
                    this.mainLoop();
                } else {
                    System.exit(-1);
                }
            }).start();
        }

        @Override
        void sendMove(int playerID, long time) {
            this.out.println("MOVE," + playerID + "," + time);
        }

        @Override
        void sendPause(long time) {
            this.out.println("PAUSE," + time);
        }

        @Override
        void sendResume() {
            this.out.println("RESUME");
        }

        @Override
        Optional<String> receiveOneLine() {
            return FP.liftExp(() -> this.in.readLine()).get();
        }

        private void waitForUp() {
            try {
                this.serverSocket = new ServerSocket();
                this.serverSocket.bind(new InetSocketAddress("0.0.0.0", this.port));
                this.socket = this.serverSocket.accept();
                this.out = new PrintWriter(this.socket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            } catch (IOException e) {
                System.exit(-1);
            }
        }
    }

    public static class TCPClient extends ConnectionBase {
        Socket socket;
        BufferedReader in;
        PrintWriter out;
        String addr;
        int port;

        TCPClient(
                String addr,
                int port,
                BiConsumer<Integer, Long> receiveMoveCallback,
                Runnable gameStartFunction,
                Supplier<String> stateSupplier,
                Consumer<String> stateConsumer,
                Consumer<Long> pauseFunction,
                Runnable resumeFunction,
                Runnable countDownFunction) {
            super(
                    false,
                    receiveMoveCallback,
                    gameStartFunction,
                    stateSupplier,
                    stateConsumer,
                    pauseFunction,
                    resumeFunction,
                    countDownFunction
            );
            this.addr = addr;
            this.port = port;
            new Thread(() -> {
                this.waitForUp();
                Optional<String> state = this.receiveOneLine();
                state.ifPresent(_state -> {
                    this.stateConsumer.accept(_state);
                    this.out.println("START");
                    this.countDownFunction.run();
                    this.gameStartFunction.run();
                    this.mainLoop();
                });
            }).start();
        }

        @Override
        void sendMove(int playerID, long time) {
            this.out.println("MOVE," + playerID + "," + time);
        }

        @Override
        void sendPause(long time) {
            this.out.println("PAUSE," + time);
        }

        @Override
        void sendResume() {
            this.out.println("RESUME");
        }

        @Override
        Optional<String> receiveOneLine() {
            return FP.liftExp(() -> this.in.readLine()).get();
        }

        private void waitForUp() {
            while (true) {
                try {
                    this.socket = new Socket(this.addr, this.port);
                    break;
                } catch (IOException e) {
                    FP.liftExp(() -> Thread.sleep(100)).run();
                }
            }
            try {
                this.out = new PrintWriter(this.socket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            } catch (IOException e) {
                System.exit(-1);
            }
        }
    }
}
