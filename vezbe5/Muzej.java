package vezbe5;

import os.simulation.Application;
import os.simulation.AutoCreate;
import os.simulation.Container;
import os.simulation.Item;
import os.simulation.Operation;
import os.simulation.Thread;

/*
 * U okviru maturske ekskurzije, za djake iz tri evropske drzave - Engleske,
 * Nemacke i Italije - je organizovan obilazak muzeja Louvre u Parizu. Sve tri
 * grupe djaka borave neko vreme ispred muzeja, nakon cega ulaze u muzej i uzi-
 * vaju u izlozenim umetnickim delima. Medjutim, u jednom momentu samo djaci
 * jedne drzave mogu boraviti u muzeju, jer bi se u suprotnom njihovi vodici
 * morali nadvikivati i niko nista ne bi cuo.
 * 
 * Sinhronizovati boravak djaka u muzeju tako da u jednom momentu samo jedna
 * grupa bude unutar muzeja. Svaki djak je predstavljen jednom niti cija klasa
 * odredjuje drzavu iz koje on dolazi.
 */

class Pristup {
	private int brEngleza = 0;
	private int brNemaca = 0;
	private int brItalijana = 0;

	public synchronized void uvediEngleza() throws InterruptedException {
		while(brNemaca + brItalijana > 0) { 
			//ako ima nekoga ko nije englez u muzeju cekaj
			wait();
		}
		brEngleza++;
	}
	public synchronized void izvediEngleza() {
		brEngleza--;
		if ( brEngleza == 0)
			notifyAll(); //obavesti sve iz ostale 2 grupe pa ce jedna uci
	}

	public synchronized void uvediNemca() throws InterruptedException {
		while(brEngleza + brItalijana > 0) { 
			//ako ima nekoga ko nije nemac u muzeju cekaj
			wait();
		}
		brNemaca++;
	}
	public synchronized void izvediNemca() {
		brNemaca--;
		if(brNemaca == 0)
			notifyAll(); //obavesti sve iz ostale 2 grupe pa ce jedna uci
	}

	public synchronized void uvediItalijana() throws InterruptedException {
		while(brNemaca + brEngleza > 0) { 
			//ako ima nekoga ko nije italijan u muzeju cekaj
			wait();
		}
		brItalijana++;
	}
	public synchronized void izvediItalijana() {
		brItalijana--;
		if( brItalijana == 0 )
			notifyAll(); //obavesti sve iz ostale 2 grupe pa ce jedna uci
	}


}

public class Muzej extends Application {

	private Pristup pristup = new Pristup();

	@AutoCreate(8)
	protected class Englez extends Thread {

		@Override
		protected void step() {
			odmara();
			try {
				pristup.uvediEngleza();
				try {
					obilazi();
				} finally {
					pristup.izvediEngleza();
				}
			} catch (InterruptedException e) {
		}
			
		}
	}

	@AutoCreate(8)
	protected class Nemac extends Thread {

		@Override
		protected void step() {
			odmara();
			try {
				pristup.uvediNemca();
				try {
					obilazi();
				} finally {
					pristup.izvediNemca();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@AutoCreate(8)
	protected class Italijan extends Thread {

		@Override
		protected void step() {
			odmara();
			try { //samo za izuzetak
				pristup.uvediItalijana();
				try {		
					obilazi();
				}
				finally {
					pristup.izvediItalijana();
				}
			} catch(InterruptedException e) {}
		} 
	}

	// ------------------- //
	//    Sistemski deo    //
	// ------------------- //
	// Ne dirati kod ispod //
	// ------------------- //

	protected final Container englezi   = box("Енглези").color(MAROON);
	protected final Container nemci     = box("Немци").color(ROYAL);
	protected final Container italijani = box("Италијани").color(ARMY);
	protected final Container muzej     = box("Музеј").color(NAVY);
	protected final Container main      = column(row(englezi, nemci, italijani), muzej);
	protected final Operation englez    = init().container(englezi).name("Енглез %d").color(RED);
	protected final Operation nemac     = init().container(nemci).name("Немац %d").color(PURPLE);
	protected final Operation italijan  = init().container(italijani).name("Италијан %d").color(GREEN);

	protected final Operation odmaranje = duration("7±2").text("Одмара").textAfter("Чека");
	protected final Operation obilazak  = duration("5±2").text("Обилази").container(muzej).textAfter("Обишао").update(this::azuriraj);

	protected void odmara() {
		odmaranje.performUninterruptibly();
	}

	protected void obilazi() {
		obilazak.performUninterruptibly();
	}

	protected void azuriraj(Item item) {
		long brE = muzej.stream(Englez.class).count();
		long brN = muzej.stream(Nemac.class).count();
		long brI = muzej.stream(Italijan.class).count();
		muzej.setText(String.format("%d / %d / %d", brE, brN, brI));
		if (brE == 0 && brN == 0 && brI == 0) {
			muzej.setColor(NAVY);
		} else if (brE > 0 && brN == 0 && brI == 0) {
			muzej.setColor(MAROON);
		} else if (brE == 0 && brN > 0 && brI == 0) {
			muzej.setColor(ROYAL);
		} else if (brE == 0 && brN == 0 && brI > 0) {
			muzej.setColor(ARMY);
		} else {
			muzej.setColor(CARBON);
		}
	}

	@Override
	protected void initialize() {
		azuriraj(null);
	}

	public static void main(String[] a) {
		launch("Музеј");
	}
}
