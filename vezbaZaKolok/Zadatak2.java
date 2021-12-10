package vezbaZaKolok;

public class Zadatak2 {

    
    public static void main(String[] args) {
        Suma suma = new Suma();
		Kamp2 kamp = new Kamp2();
		
		IzvidjacPecurke2[] izvidjacPecurke = new IzvidjacPecurke2[12];
		Thread[] izvidjacDrva = new Thread[12];

		for(int i = 0; i < 12; i++) {
			izvidjacPecurke[i] = new IzvidjacPecurke2(suma, kamp);
			izvidjacPecurke[i].setName("Pecurke "+ i);
			izvidjacPecurke[i].setDaemon(true); //pozadinski odnosno nekorisnički proces
			izvidjacPecurke[i].start();

			Runnable r = new IzvidjacDrva2(suma, kamp); //Runnable prosledimo u Thread konstruktor i tjt.
			izvidjacDrva[i] = new Thread(r);
			izvidjacDrva[i].setName("Drvar "+i);
			izvidjacDrva[i].setDaemon(false); //korisnički proces
			izvidjacDrva[i].start();
		}

		kamp.ispis();
    }
}

class IzvidjacPecurke2 extends IzvidjacPecurke {

    private Suma suma;
	private Kamp2 kamp;

    public IzvidjacPecurke2(Suma suma, Kamp2 kamp) {
        super(suma, kamp);
        this.kamp = kamp;
        this.suma = suma;
    }

    @Override
    public void run() {
       for(int i = 0; i < 25 && !interrupted(); i++) {
            int koliko = suma.traziPecurke();
            kamp.donesiPecurke(koliko);
       }
        try {
            System.out.println(Thread.currentThread().getName() + " je gotov -----------");
            kamp.sacekajOstale();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class IzvidjacDrva2 extends IzvidjacDrva{

    private Suma suma;
	private Kamp2 kamp;

    public IzvidjacDrva2(Suma suma, Kamp2 kamp) {
        super(suma, kamp);
        this.kamp = kamp;
        this.suma = suma;
    }

    @Override
    public void run() {
        for(int i = 0; i < 25; i++) {
            int koliko = suma.traziDrva();
            kamp.donesiDrva(koliko);
        }
        try {
            System.out.println(Thread.currentThread().getName() + " je gotov -----------");
            kamp.sacekajOstale();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

class Kamp2 extends Kamp {

    private int pecurke = 0;
	private int drva = 0;
    private int brojGotovih = 0;

    @Override
    public synchronized void donesiDrva(int koliko) {
        this.drva += koliko; 
    }
    @Override
    public synchronized void donesiPecurke(int koliko) {
        this.pecurke += koliko;
    }

    public synchronized void sacekajOstale() throws InterruptedException {
        brojGotovih++;
        if(brojGotovih == 24) notifyAll();
        else {
            wait();
        }
    }

    public void ispis() {
        System.out.println("U kampu je skupljeno: "+ pecurke + " pecuraka i "+drva+" drva.");
    }
}