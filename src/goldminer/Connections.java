package goldminer;

import util.FP;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.function.BiConsumer;

public class Connections {
    public interface ConnectionBase {
        void send(String string);

        boolean isUp();

        boolean waitForUp(int timeout);
    }

    public static class TCPServer implements ConnectionBase {
        ServerSocket serverSocket;
        Socket clientSocket;
        PrintWriter out;
        BufferedReader in;
        boolean isUp = false;
        BiConsumer<ConnectionBase, String> callback;

        TCPServer(int portNumber, BiConsumer<ConnectionBase, String> callback) {
            try {
                this.serverSocket = new ServerSocket(portNumber);
            } catch (IOException e) {
                System.exit(-1);
            }
            this.callback = callback;
        }

        @Override
        public synchronized void send(String string) {
            this.out.write(string + "\n");
            this.out.flush();
        }

        @Override
        public boolean waitForUp(int timeout) {
            try {
                this.serverSocket.setSoTimeout(timeout);
            } catch (IOException e) {
                System.exit(-1);
            }
            try {
                this.clientSocket = this.serverSocket.accept();
                this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (InterruptedIOException e) {
                return false;
            } catch (IOException e) {
                System.exit(-1);
            }
            new Thread(() -> {
                String line = FP.liftExp(() -> this.in.readLine()).get().get();
                this.callback.accept(this, line);
            }).start();
            this.isUp = true;
            return true;
        }

        @Override
        public boolean isUp() {
            return this.isUp;
        }
    }

    public static class TCPClient implements ConnectionBase {
        String hostname;
        Socket clientSocket;
        int port;
        PrintWriter out;
        BufferedReader in;
        boolean isUp = false;
        BiConsumer<ConnectionBase, String> callback;

        TCPClient(String hostname, int port, BiConsumer<ConnectionBase, String> callback) {
            this.hostname = hostname;
            this.port = port;
            this.callback = callback;
        }

        @Override
        public boolean waitForUp(int timeout) {
            long beginTime = Calendar.getInstance().getTimeInMillis();
            try {
                this.clientSocket = new Socket();
                this.clientSocket.connect(new InetSocketAddress(this.hostname, this.port), timeout);
                this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (InterruptedIOException e) {
                return false;
            } catch (ConnectException e) {
                FP.liftExp(() -> Thread.sleep(timeout - (Calendar.getInstance().getTimeInMillis() - beginTime)))
                        .run();
                return false;
            } catch (IOException e) {
                System.exit(-1);
            }
            this.isUp = true;
            new Thread(() -> {
                String line = FP.liftExp(() -> this.in.readLine()).get().get();
                this.callback.accept(this, line);
            }).start();
            return true;
        }

        @Override
        public synchronized void send(String string) {
            this.out.write(string + "\n");
            this.out.flush();
        }

        @Override
        public boolean isUp() {
            return this.isUp;
        }
    }
}
