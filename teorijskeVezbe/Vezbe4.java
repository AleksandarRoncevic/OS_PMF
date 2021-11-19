package teorijskeVezbe;
class Brojac {
    private int br = 0;

    public synchronized void inc() {
        br++;
    }

    public synchronized void dec() {
        br--;
    }

    @Override
    public synchronized String toString() {
        return "Vrednost "+ br;
    }
}

class Nit extends Thread {
    private Brojac brojac;

    public Nit(Brojac brojac) {
        this.brojac = brojac;
    }
    
    @Override
    public void run() {
        for(int i = 0; i < 1_000_000; i++) {
            brojac.inc();
        }
    }
}

public class Vezbe4 {
    public static void main(String[] args) throws InterruptedException {
        Brojac brojac = new Brojac();

        Nit n1 = new Nit(brojac);
        Nit n2 = new Nit(brojac);
        Nit n3 = new Nit(brojac);

         n1.start();
         n2.start();
         n3.start();

        //  n1.join();
        //  n2.join();
        //  n3.join();
        Thread.sleep(1000);
        
         System.out.println(brojac);
    }
}