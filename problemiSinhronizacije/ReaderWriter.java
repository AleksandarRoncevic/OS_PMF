package problemiSinhronizacije;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import os.simulation.Application;
import os.simulation.AutoCreate;
import os.simulation.Container;
import os.simulation.Item;
import os.simulation.Operation;
import os.simulation.Thread;

/*
 * Data je zajednicka baza podataka. Vise procesa zeli da istovremeno pristupa
 * ovoj bazi kako bi citali ili upisivali podatke u nju. Kako bi korektno
 * realizovali ove istovremene pristupe bez rizika da dodje do problema,
 * procesi moraju da postuju sledeca pravila: istovremena citanja su dozvoljena
 * posto citaoci ne smetaju jedan drugom, istovremeno citanje i pisanje nije
 * dozvoljeno jer se moze desiti da citalac procita pogresne podatke (do pola
 * upisane), istovremena pisanja takodje nisu dozvoljena jer mogu prouzrokovati
 * ostecenje podataka.
 * 
 * Implementirati sinhronizaciju procesa pisaca i procesa citalaca tako da se
 * postuju opisana pravila.
 */
public class ReaderWriter extends Application {

    protected final class BazaSync {
        private int brPisaca;
        private int brCitalaca;

        private boolean jeZauzeto(boolean zaPisanje) { 
            //boolean zaPisanje je tu kako bismo izbegli favorizaciju citalaca
            return (brPisaca > 0) || (zaPisanje && brCitalaca > 0);
        }

        public synchronized void zapocniPisanje() throws InterruptedException {
            while(jeZauzeto(true)) {
                wait(); //ako ima nekoga u bazi cekaj
            }
            brPisaca++; //zauzimamo bazu ulaskom pisca
        }

        public synchronized void zapocniCitanje() throws InterruptedException {
            while(jeZauzeto(false))
                wait();
            brCitalaca++;
        }

        public synchronized void zavrsiPisanje() {
            brPisaca--;
            notifyAll(); //obavesti sve citaoce ili jednog pisca koji cekaju
        }

        public synchronized void zavrsiCitanje() {
            brCitalaca--;
            if(brCitalaca == 0)
                notify(); //obavesti pisca koji ceka 
        }
    } 

    protected final class BazaLock {
        protected Lock brava = new ReentrantLock();
        protected Condition pisci = brava.newCondition();
        protected Condition citaoci = brava.newCondition();

        private int brPisaca;
        private int brCitalaca;

        public void zapocniPisanje() throws InterruptedException {
            brava.lock();
            try{
                while(brCitalaca + brPisaca > 0) { //dok je iko drugi u bazi moramo da cekamo
                    pisci.awaitUninterruptibly();
                }
                brPisaca++; //usli smo u bazu i povecavamo brojac
            } finally {
                brava.unlock();
            }
        }

        public void zapocniCitanje() throws InterruptedException {
            brava.lock();
            try{
                while(brPisaca > 0) { //dok ima pisaca ne mozemo uci u bazu
                    citaoci.awaitUninterruptibly();
                }
                brCitalaca++; //usli smo u bazu
            } finally {
                brava.unlock();
            }
        }

        public void zavrsiPisanje() {
            brava.lock();
            try{
                brPisaca--;
                pisci.signal(); //obavestimo jednog od sledecih mogucih pisaca 
                citaoci.signalAll(); //obavestimo sve citaoce koji cekaju
            } finally {
                brava.unlock();
            }
        }

        public void zavrsiCitanje() {
            brava.lock();
            try{
                brCitalaca--;
                if(brCitalaca == 0)
                    pisci.signalAll(); //obavestimo jednog pisca da moze da udje u bazu
                    //mislim da nema potrebe da stavimo signalAll();
            } finally {
                brava.unlock();
            }
        }

    }

    protected final class BazaSemafor {

        protected Semaphore baza = new Semaphore(1); //da li je baza zauzeta ili ne
        protected Semaphore mutex = new Semaphore(1); //da li je neko u K.O.

        private int brCitalaca; //nema potrebe da pratimo broj pisaca u bazi jer je ili 1 ili 0

        public void zapocniPisanje() throws InterruptedException {
            baza.acquire(); //pisac samo zauzima bazu ako je to moguce
        }

        public void zapocniCitanje() throws InterruptedException {
            mutex.acquire(); //zauzmi K.O. zbog brojaca citalaca
            try{
                brCitalaca++;
                if(brCitalaca == 1) {
                    //ako je ovo prvi koji ulazi u bazu moramo azurirati vrednost semfora-baza
                    try{
                        baza.acquire();
                    } catch(InterruptedException e) {
                        brCitalaca--; //citalac nije uspeo da udje u bazu
                        throw e;
                    }
                }
            } finally {
                mutex.release(); //izlazimo iz K.O.
            }
        }

        public void zavrsiPisanje() {
            baza.release(); //oslobadjamo bazu izlaskom pisca
        }

        public void zavrsiCitanje() {
            mutex.acquireUninterruptibly(); //zauzmi K.O. 
            try{
                brCitalaca--;
                if(brCitalaca == 0) {
                    //poslednji je izasao iz baze
                    baza.release(); //oslobadjamo bazu
                }
            } finally {
                mutex.release();
            }
        }
    }

    // protected final BazaSync baza = new BazaSync();
    // protected final BazaLock baza = new BazaLock();
    protected final BazaSemafor baza = new BazaSemafor();

	@AutoCreate(2)
	protected class Pisac extends Thread {

		@Override
		protected void step() {
			radiNestoDrugo();
            try { //try-catch blok za
                baza.zapocniPisanje(); //zauzimamo bazu
                try{ //try-finally za ono sto zeli da uradi proces
			        pise();
                } finally {
                    baza.zavrsiPisanje(); //sta god da se desi moramo da izadjemo iz baze
                }
            } catch(InterruptedException e) {
                stopGracefully();
            }
		}
	}

	@AutoCreate(5)
	protected class Citalac extends Thread {

		@Override
		protected void step() {
			radiNestoDrugo();
			try{
                baza.zapocniCitanje();
                try{
                    cita();
                } finally {
                    baza.zavrsiCitanje();
                }
            } catch(InterruptedException e) {
                stopGracefully();
            }
		}
	}

	// ------------------- //
	//    Sistemski deo    //
	// ------------------- //
	// Ne dirati kod ispod //
	// ------------------- //

	protected final Container pisci   = box("Писци").color(MAROON);
	protected final Container citaoci = box("Читаоци").color(NAVY);
	protected final Container resurs  = box("База").color(ROYAL);
	protected final Container main    = column(row(pisci, citaoci), resurs);
	protected final Operation pisac   = init().name("Писац %d").color(ROSE).container(pisci);
	protected final Operation citalac = init().name("Читалац %d").color(AZURE).container(citaoci);
	protected final Operation pisanje = duration("7±2").text("Пише").container(resurs).textAfter("Завршио").update(this::azuriraj);;
	protected final Operation citanje = duration("5±2").text("Чита").container(resurs).textAfter("Завршио").update(this::azuriraj);;
	protected final Operation posao   = duration("6±2").text("Ради").textAfter("Чека");

	protected void pise() {
		pisanje.performUninterruptibly();
	}

	protected void cita() {
		citanje.performUninterruptibly();
	}

	protected void radiNestoDrugo() {
		posao.performUninterruptibly();
	}

	protected void azuriraj(Item item) {
		long brP = resurs.stream(Pisac.class).count();
		long brC = resurs.stream(Citalac.class).count();
		resurs.setText(String.format("%d : %d", brP, brC));
		if (brP == 0 && brC == 0) {
			resurs.setColor(NEUTRAL_GRAY);
		} else if (brP > 0 && brC == 0) {
			resurs.setColor(MAROON);
		} else if (brP == 0 && brC > 0) {
			resurs.setColor(NAVY);
		} else {
			resurs.setColor(ROYAL);
		}
	}

	@Override
	protected void initialize() {
		azuriraj(null);
	}

	public static void main(String[] arguments) {
		launch("Писци и читаоци");
	}
}