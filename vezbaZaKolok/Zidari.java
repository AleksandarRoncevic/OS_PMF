package vezbaZaKolok;

import java.util.ArrayList;

public class Zidari {
    public static void main(String[] args) {
        Gajba g = new Gajba();

        Thread mladi = new Thread(new MladiZidar(g));
        mladi.setDaemon(true);
        mladi.setName("Gule");

        mladi.start();
        
        StariZidar Joza = new StariZidar(g, "Joza");
        StariZidar Muhi = new StariZidar(g, "Muhi");

        Joza.start();
        Muhi.start();
    }
}

class MladiZidar implements Runnable {
    private Gajba gajba;

    public MladiZidar(Gajba g) {
        this.gajba = g;
    }

    @Override
    public void run() {
        while(!Thread.interrupted()) {
            Pivo[] piva = Pivo.kupi();
            
            for(Pivo p : piva) {
                gajba.stavi(p);
            }
        }
    }
}

class StariZidar extends Thread {
    private Gajba gajba;
    private int maxDailyBeer = 5; 
    private int currentBeer = 0;

    public StariZidar(Gajba g, String name) {
        setName(name);
        this.gajba = g;
    }

    @Override
    public void run() {
        while(currentBeer <= maxDailyBeer && !Thread.interrupted()) {
            Pivo p = gajba.popij();
            currentBeer++;
            p.ispij();
        }
        System.out.println(Thread.currentThread().getName() + " je zavrsio smenu.");
    }
}



class Gajba {

    private int maxBeer = 12;
    ArrayList<Pivo> sadrzaj = new ArrayList<Pivo>(maxBeer);
    
    public synchronized void stavi(Pivo pivo) {
        while(sadrzaj.size() >= maxBeer) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        sadrzaj.add(pivo);
        System.out.println("Trenutno ima "+ sadrzaj.size() + " piva");
        notifyAll(); //ako su cekali na pivo obojica 

    }

    public synchronized Pivo popij() {
        Pivo p = null;
        while(sadrzaj.isEmpty())
            try {
                wait();
            } catch (InterruptedException e) {

            }
        p = sadrzaj.remove(0); //skini prvi po redosledu
        System.out.println("Trenutno ima "+ sadrzaj.size() + " piva");
        notify(); //obavesti klinca da moze po jos
        return p;
    }
}


class Pivo {

	public void ispij() {
		System.out.println(Thread.currentThread().getName() + " pije " + opis + " pivo.");
		try {
			Thread.sleep((long) (500 + 500 * Math.random()));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public static Pivo[] kupi() {
		int n = (int) (1 + 5 * Math.random());
		Pivo[] piva = new Pivo[n];
		for (int i = 0; i < n; i++) {
			piva[i] = new Pivo();
		}
		System.out.println(Thread.currentThread().getName() + " kupuje " + n + " piva.");
		try {
			Thread.sleep((long) (1000 + 1000 * Math.random()));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return piva;
	}

	private final String opis;

	private Pivo() {
		opis = VRSTE[(int) (VRSTE.length * Math.random())];
	}

	private static final String[] VRSTE = {
			"Lav",
			"Lav Twist",
			"Lav Swing",
			"Lav Tamni",
			"Dundjerski",
			"Jelen",
			"Jelen Cool",
			"Jelen Warm",
			"Jelen Fresh",
			"Vajfert",
	};
}