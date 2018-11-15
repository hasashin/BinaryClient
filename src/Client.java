import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client implements Runnable {

    private int idsesji;
    private Socket socket;
    private DataInputStream sin;
    private DataOutputStream sout;
    private static boolean cond = true;

    private Client(String inet, int port) {
        try {
            System.out.println("Oczekiwanie na połączenie...");
            socket = new Socket(inet, port);
            sin = new DataInputStream(socket.getInputStream());
            sout = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    private void decode(byte[] data) {
        int odpowiedz, sesja, operacja;
        odpowiedz = (data[0] & 0b00111000) >> 3;
        operacja = data[0] & 0b00000111;
        sesja = data[1];
        if(idsesji != 0 && sesja == idsesji) {
            switch (operacja) {
                case 0:
                    this.idsesji = sesja;
                    break;
                case 1:
                    int czas = data[2];
                    System.out.println("Pozostało " + czas + " sekund");
                    break;
                case 2:
                    System.out.println("Zła liczba 😢");
                    break;
                case 3:
                    System.out.println("Wygrałeś!");
                    send(0, 6);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                    cond = false;
                    break;
                case 4:
                    System.out.println("Start!");
                    break;
                case 5:
                    System.out.println("Wygrał drugi gracz, poprawna liczba to: " + odpowiedz);
                    send(0, 6);
                    try {
                        socket.close();
                    } catch (IOException eeeee) {
                        System.err.println(eeeee.getMessage());
                    }
                    cond = false;
                    break;
                case 6:
                    System.out.println("Czas się skończył.");
                    send(0, 6);
                    cond = false;
                    break;
                default:
                    break;
            }
        }

    }

    private void send(int liczba, int operacja) {
        byte[] packet = new byte[2];

        packet[0] = (byte) operacja;

        packet[0] = (byte) (packet[0] | ((liczba & 0b00000111) << 3));

        packet[1] = (byte) (idsesji & 0b00011111);

        try {
            sout.write(packet, 0, 2);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length == 2) {
            Client client = new Client(args[0], Integer.parseInt(args[1]));
            if (client.socket != null) {
                System.out.println("Połączono z serwerem " + args[0] + ":" + args[1]);
                new Thread(client).start();
            } else {
                System.out.println("Nie można było połączyć z serwerem");
                cond = false;
            }
        }
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        int liczba, len;
        byte[] data = new byte[3];
        while (cond) {
            try {
                if (System.in.available() > 0) {
                    liczba = scanner.nextInt();
                    send(liczba, 7);
                }
            } catch (Throwable e) {
                scanner.next();
            }
            try {
                if (sin.available() > 0) {
                    len = sin.read(data);
                    if (len == -1) {
                        cond = false;
                    } else decode(data);
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }


    }

}
