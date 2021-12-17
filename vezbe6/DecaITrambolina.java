package vezbe6;
import os.simulation.Container;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer.ConditionObject;

import os.simulation.Application;
import os.simulation.AutoCreate;
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
	private boolean decaciPrednost;

	private Lock brava = new ReentrantLock();
	private Condition deca = brava.newCondition();
	private Condition decaci = brava.newCondition();
	private Condition devojcice = brava.newCondition();

	public void uvediDete(int t) throws InterruptedException {
		brava.lock();
		try{
			while(tezina + t > maxTezina) {
				deca.await();
			}
			tezina += t;
		} finally {
			brava.unlock();
		}
	}
	public void izvediDete(int t) {
		brava.lock();
		try{
			deca.signalAll();
			tezina -= t;
		} finally {
			brava.unlock();
		}
	}

	public void uvediDete2() throws InterruptedException {
		brava.lock();
		try{
			while(brDecaka >= maxBrojDece) {
				deca.await();
			}
			brDecaka++;
		} finally {
			brava.unlock();
		}
	}
	public void izvediDete2() {
		brava.lock();
		try{
			deca.signal();
			brDecaka--;
		} finally {
			brava.unlock();
		}
	}

	public void uvediDecaka() throws InterruptedException {
		brava.lock();
		try{
			while(brDevojcica > 0 || !decaciPrednost || brDecaka >= 5) {
				decaci.await();
			}
			brDecaka++;
		} finally {
			brava.unlock();
		}
	};
	public void izvediDecaka() {
		brava.lock();
		try{
			brDecaka--;
			if(brDecaka == 0) {
				devojcice.signalAll(); 
				decaciPrednost = false;
			}
			if(brDecaka == 4) decaci.signal();
		} finally {
			brava.unlock();
		}
	}

	public void uvediDevojcicu() throws InterruptedException {
		brava.lock();
		try{
			while(brDecaka > 0 || decaciPrednost || brDevojcica >= 5) {
				devojcice.await();
			}
			brDevojcica++;
		} finally {
			brava.unlock();
		}
	}
	public void izvediDevojcicu() {
		brava.lock();
		try {
			brDevojcica--;
			if(brDevojcica == 0) {
				decaci.signalAll();
				decaciPrednost = true;
			}
			if(brDevojcica == 4) devojcice.signal();
		} finally {
			brava.unlock();
		}
	}


};
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
			// Sinhronizacija
			// Blokirati decu koja ne smeju trenutno da skacu
			try{
				// pristup.uvediDete(this.tezina);
				// pristup.uvediDete2();
				if(this.pol == Pol.MUSKI) pristup.uvediDecaka();
				else pristup.uvediDevojcicu();

				try{
					skace();
				} finally {
					// pristup.izvediDete(this.tezina);
					// pristup.izvediDete2();
					if(this.pol == Pol.MUSKI) pristup.izvediDecaka();
					else pristup.izvediDevojcicu();
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

	protected final Operation odmaranje = duration("15±2").text("Одмара").textAfter("Чека");
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
