import java.net.*;
import java.io.*;
import java.time.LocalTime;
import java.util.Scanner;

public class Client implements Runnable {

    int idsesji;
    Socket socket;
    DataInputStream sin;
    DataOutputStream sout;
    byte[] data = new byte[3];
    static boolean cond = true, connected = false;

    Client(String inet, int port) {
        try {
            System.out.println("Oczekiwanie na połączenie...");
            socket = new Socket(inet, port);
            if (socket != null) {
                System.out.println("Połączono z serwerem " + inet + ":" + port);
                sin = new DataInputStream(socket.getInputStream());
                sout = new DataOutputStream(socket.getOutputStream());
            } else System.out.println("Nie można połączyć: przekroczono czas oczekiwania.");
        } catch (IOException e) { }

    }

    void decode(byte[] data) {
        int odpowiedź, sesja, operacja;
        odpowiedź = (data[0] & 0b00111000) >> 3;
        operacja = data[0] & 0b00000111;
        sesja = data[1];
        switch (operacja) {
            case 0:
                connected = true;
                this.idsesji = sesja;
                break;
            case 2:
                System.out.println("Zła liczba 😢");
                break;
            case 3:
                System.out.println("Wygrałeś!");
                send(0,6);
                try {
                    socket.close();
                }catch (IOException e){}
                cond = false;
                break;
            case 4:
                System.out.println("Start!");
                break;
            case 5:
                System.out.println("Wygrał drugi gracz");
                send(0,6);
                try {
                    socket.close();
                }
                catch(IOException eeeee){}
                cond = false;
                break;

            default:
                break;
        }

    }

    void send(int liczba,int operacja) {
        byte[] packet = new byte[2];

        packet[0] = (byte)operacja;

        packet[0] = (byte) (packet[0] | ((liczba & 0b00000111) << 3));

        packet[1] = (byte) (idsesji & 0b00011111);

        try {
            sout.write(packet, 0, 2);
        } catch (IOException e) {

        }
    }

    public static void main(String[] args) {
        if (args.length == 2) {
            Client client = new Client(args[0], Integer.parseInt(args[1]));
            if (client.socket != null) {
                new Thread(client).start();
            } else {
                cond = false;
                return;
            }
        }
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        int liczba;
        while (cond) {
            try {
                if (System.in.available() > 0) {
                    liczba = scanner.nextInt();
                    send(liczba,7);
                }
            } catch (Throwable e) {
                scanner.next();
            }
            try {
                if (sin.available() > 0) {
                    sin.read(data);
                    decode(data);
                }
            } catch (IOException e) {
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
        }


    }

}
