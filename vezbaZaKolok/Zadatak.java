package vezbaZaKolok;

class IzvidjacPecurke extends Thread {

	private Suma suma;
	private Kamp kamp;

	public IzvidjacPecurke(Suma suma, Kamp kamp) {
		this.suma = suma;
		this.kamp = kamp;
	}

	@Override
	public void run() {
		for(int i = 0; i < 25 && !Thread.interrupted(); i++) {
			int skupljeno = suma.traziPecurke();
			kamp.donesiPecurke(skupljeno);
		}
	}
}

class IzvidjacDrva implements Runnable {

	private Suma suma;
	private Kamp kamp;

	public IzvidjacDrva(Suma suma, Kamp kamp) {
		this.suma = suma;
		this.kamp = kamp;
	}

	@Override
	public void run() {
		for(int i = 0; i < 20; i++) {
			int skupljeno = suma.traziDrva();
			kamp.donesiDrva(skupljeno);
		}
	}
}


public class Zadatak {

	public static void main(String[] args) throws InterruptedException {

		Suma suma = new Suma();
		Kamp kamp = new Kamp();
		
		IzvidjacPecurke[] izvidjacPecurke = new IzvidjacPecurke[12];
		Thread[] izvidjacDrva = new Thread[12];

		for(int i = 0; i < 12; i++) {
			izvidjacPecurke[i] = new IzvidjacPecurke(suma, kamp);
			izvidjacPecurke[i].setName("Pecurke "+ i);
			izvidjacPecurke[i].setDaemon(true);
			izvidjacPecurke[i].start();

			Runnable r = new IzvidjacDrva(suma, kamp);
			izvidjacDrva[i] = new Thread(r);
			izvidjacDrva[i].setName("Drvar "+i);
			izvidjacDrva[i].setDaemon(false);
			izvidjacDrva[i].start();
			

			izvidjacDrva[i].interrupt();
		}

		Thread.sleep(3000);

		kamp.ispis();
	}
}

class Kamp {

	private int pecurke = 0;
	private int drva = 0;

	public synchronized void donesiPecurke(int koliko) {
		try {
			while(pecurke + koliko  > drva) 
				wait();
		} catch(Exception e) {
			e.printStackTrace();
		}
		pecurke += koliko;
	}

	public synchronized void donesiDrva(int koliko) {
		drva += koliko;
		notifyAll();
	}


	public void ispis() {
		System.out.println("U kampu je skupljeno: "+ pecurke + " pecuraka i "+drva+" drva.");

	}
}

class Suma {

	public int traziPecurke() {
		System.out.println(Thread.currentThread().getName() + " trazi pecurke.");
		try {
			Thread.sleep((long) (500 + 500 * Math.random()));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return (int) (Math.random() * 3);
	}

	public int traziDrva() {
		System.out.println(Thread.currentThread().getName() + " trazi drva.");
		try {
			Thread.sleep((long) (500 + 500 * Math.random()));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return (int) (Math.random() * 3);
	}
}