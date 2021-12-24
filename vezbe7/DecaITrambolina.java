package vezbe7;

import java.util.concurrent.Semaphore;

import os.simulation.Application;
import os.simulation.AutoCreate;
import os.simulation.Container;
import os.simulation.Item;
import os.simulation.Operation;
import os.simulation.Thread;

/*
 * Na jednom seoskom vašaru, najinteresantnija atrakcija desetogodišnjacima je
 * velika trambolina starog Pere. Stari Pera je dobrog srca i pušta mališane da
 * se besplatno zabavljaju na njoj, na opštu radost njihovih roditelja. Mališa-
 * ni takođe koriste ovo i non-stop skaču na trambolini, silazeći jedino kada
 * se umore od skakanja kako bi se malo odmorili za novu rundu zabave.
 * 
 * A)
 * 
 * Nažalost, i trambolina starog Pere je dosta stara pa ne može izdržati više
 * od 300 kila. Prilikom implementacije rešenja imati ovo u vidu i ne dozvoliti
 * da se trambolina pokida. Potrebno je blokirati mališane koji žele da skaču
 * na trambolini ako bi ukupna težina prešla 300 kila.
 * 
 * B)
 * 
 * Zbog povećane mogućnosti povreda kada više dece skače na trambolini, stari
 * Pera ne dozvoljava da na njoj bude više od 5 dece. Takođe, ne terati mališa-
 * ne da nepotrebno čekaju ako ima mesta na trambolini.
 * 
 * C)
 * 
 * Kako su dečaci nestašniji i manje paze da nekog slučajno ne udare tokom ska-
 * kanja, potrebno je odvojiti dečake i devojčice, tj. blokirati ulaz devojči-
 * cama ako na trambolini trenutno skaču dečaci, odnosno dečacima ako je trenu-
 * tno koriste devojčice.
 */
class Pristup {

	public Pristup(int max, int brD) {
		this.maxTezina = max;
		this.maxBrojDece = brD;
	};

	private double maxTezina;
	private int maxBrojDece;
	private int brDecaka = 0;
	private int brDevojcica = 0;
	private double tezina;

	private Semaphore maxTezinaSemaphore = new Semaphore(300);
	private Semaphore maxDeceSemaphore = new Semaphore(5);

	private Semaphore mutexDecaci = new Semaphore(1);
	private Semaphore mutexDevojcice = new Semaphore(1);


	//A)
	public void uvediDete(int t) throws InterruptedException {
		maxTezinaSemaphore.acquire(t);
	}

	public void izvediDete(int t) {
		maxTezinaSemaphore.release(t);
	}

	//B)
	public void uvediDete2() throws InterruptedException {
		maxDeceSemaphore.acquire();
	};
	public void izvediDete2() {
		maxDeceSemaphore.release();
	};

	//C)
	public void uvediDecaka() throws InterruptedException {
		mutexDecaci.acquireUninterruptibly();
		try {
			brDecaka++;
			if(brDecaka == 1) {
				mutexDevojcice.acquireUninterruptibly();
			}
		} finally {
			mutexDecaci.release();
		}
	}

	public void izvediDecaka() {
		mutexDecaci.acquireUninterruptibly();
		try {
			brDecaka--;
			if(brDecaka == 0) 
				mutexDevojcice.release();
		} finally {
			mutexDecaci.release();
		}
	}

	public void uvediDevojcicu() {
		mutexDevojcice.acquireUninterruptibly();
		try {
			brDevojcica++;
			if(brDevojcica == 1) mutexDecaci.acquireUninterruptibly();
		} finally {
			mutexDevojcice.release();
		}
	}

	public void izvediDevojcicu() {
		mutexDevojcice.acquireUninterruptibly();
		try{
			brDevojcica--;
			if(brDevojcica == 0) 
				mutexDecaci.release();
		} finally {
			mutexDevojcice.release();
		}
	}

}



public class DecaITrambolina extends Application {

	protected final int MAX_TEZINA = 300;
	protected final int MAX_BR_DECE = 5;
	protected Pristup pristup = new Pristup(MAX_TEZINA,MAX_BR_DECE);
	
	protected enum Pol {
		MUSKI, ZENSKI;
	}

	@AutoCreate(26)
	protected class Dete extends Thread {

		private final Pol pol = randomElement(Pol.values());
		private final int tezina = randomInt(25, 60);

		public Dete() {
			setName(String.format("%4.1f kg", 1.0 * tezina));
			setColor(pol == Pol.MUSKI ? AZURE : ROSE);
		}

		@Override
		protected void step() {
			odmara();
			try {
				if(this.pol == Pol.MUSKI) 
					pristup.uvediDecaka();
				else
					pristup.uvediDevojcicu();
				
				pristup.uvediDete2(); //paziti na redosled!
				try{
				skace();
				} finally {
				pristup.izvediDete2();
					if(this.pol == Pol.MUSKI) 
					pristup.izvediDecaka();
				else
					pristup.izvediDevojcicu();
				}
			} catch(InterruptedException e) {}
		}
	}

	// ------------------- //
	//    Sistemski deo    //
	// ------------------- //
	// Ne dirati kod ispod //
	// ------------------- //

	protected final Container van       = box("Клупе").color(OLIVE);
	protected final Container unutra    = box("Трамболина").color(ARMY);
	protected final Container main      = column(van, unutra);
	protected final Operation dete      = init().container(van).name("Дете %d").color(ORANGE);

	protected final Operation odmaranje = duration("7±2").text("Одмара").textAfter("Чека");
	protected final Operation skakanje  = duration("5±2").text("Скаче").container(unutra).update(this::azuriraj);

	protected void odmara() {
		odmaranje.performUninterruptibly();
	}

	protected void skace() {
		skakanje.performUninterruptibly();
	}

	protected void azuriraj(Item item) {
		int br = 0;
		double tezina = 0.0;
		for (Dete dete : unutra.getItems(Dete.class)) {
			br += 1;
			tezina += dete.tezina;
		}
		unutra.setText(String.format("%4.2f kg / %d", tezina, br));
		if (tezina > MAX_TEZINA || br > MAX_BR_DECE) {
			unutra.setColor(MAROON);
		} else {
			unutra.setColor(ARMY);
		}
	}

	@Override
	protected void initialize() {
		azuriraj(null);
	}

	public static void main(String[] arguments) {
		launch("Деца и трамболина");
	}
}
