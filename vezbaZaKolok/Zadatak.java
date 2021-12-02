package vezbaZaKolok;

class IzvidjacPecurke extends Thread {

	private Suma suma;
	private Kamp kamp;

	public IzvidjacPecurke(Suma suma, Kamp kamp) {
		this.suma = suma;
		this.kamp = kamp;
	}

	/*
		Izvidjač koji skuplja pečurke treba da se u slučaju uzbune vrati u kamp
		što predstavljamo tako što u uslovu za for petlju imamo i !Thread.interrupted();
		pored standardnog i < 25.
		Takođe kada pokuša da donese pečurke i vidi da će prekoračiti broj donesenih drva
		poziva se wait() i čekamo da se donesu drva i onda notifyAll obaveštava sve koji čekaju sa pečurkama
		da pokušaju opet, pa će barem jedan ako ne više.
	*/
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

	/* 
		U zadatku je rečeno da se Izviđač koji skuplja drva ne obazire na uzbune
		i to predstavljamo time što u for-u sada nemamo uslov !Thread.interrupted();
		Ovako ne reagujemo na prekide i uopšte ih ne proveravamo. 
	*/
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
			izvidjacPecurke[i].setDaemon(true); //pozadinski odnosno nekorisnički proces
			izvidjacPecurke[i].start();

			Runnable r = new IzvidjacDrva(suma, kamp); //Runnable prosledimo u Thread konstruktor i tjt.
			izvidjacDrva[i] = new Thread(r);
			izvidjacDrva[i].setName("Drvar "+i);
			izvidjacDrva[i].setDaemon(false); //korisnički proces
			izvidjacDrva[i].start();
		}

		kamp.ispis();
	}
}

class Kamp {

	private int pecurke = 0;
	private int drva = 0;

	public synchronized void donesiPecurke(int koliko) {
		while(pecurke + koliko  > drva) 
		try {	
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