package teorijskeVezbe;

class Suma extends Thread {

    private long suma = 0;

    @Override
    public void run() {
        long s = 1;
        for (long i = 2; !interrupted(); i++) {
            s += i;
        }
        suma = s;
    }

    public long getRezultat() {
        return suma;
    }
}

class Ispis extends Thread {

    private Suma nit;

    public Ispis(Suma nit) {
        this.nit = nit;
    }

    @Override
    public void run() {
        while (nit.getRezultat() == 0) {
            // Čekamo rezultat
        }
        System.out.println(nit.getRezultat());
    }
}

public class Vezbe3_1 {
    public static void main(String[] arguments) throws InterruptedException {

        Suma n1 = new Suma();
        Ispis n2 = new Ispis(n1);
        n1.start();
        n2.start();

        Thread.sleep(100);

        n1.interrupt();

    }
} 
