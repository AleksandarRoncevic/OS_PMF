package teorijskeVezbe;
class Brojac2 {
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

class Nit2 extends Thread {
    private Brojac2 brojac;

    public Nit2(Brojac2 brojac) {
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
        Brojac2 brojac = new Brojac2();

        Nit2 n1 = new Nit2(brojac);
        Nit2 n2 = new Nit2(brojac);
        Nit2 n3 = new Nit2(brojac);

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