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
 * U frizerskom salonu rade dva berberina. Ako nema musterija, berber sedi u
 * svojoj stolici i spava. Kada musterija udje, ako neki od berbera spava, budi
 * ga, seda za stolicu i berber je sisa. Ako su svi berberi zauzeti, musterija
 * seda za stolicu u cekaonici i ceka da se oslobodi neko od berbera. Kada
 * berber zavrsi sisanje musterije, ako ima musterija koje cekaju, krece da
 * sisa jednu od musterija koje cekaju. Ako nema vise musterija koje cekaju,
 * berber seda u svoju stolicu i spava.
 * 
 * Implementirati sinhronizaciju ove dve vrste procesa kako je opisano.
 */
public class SleepingBarber extends Application {

    protected final class SalonSync {

        private int brBerbera; //broj berbera koji cekaju/spavaju
        private int brMusterija; //broj musterija koji cekaju

        public synchronized void cekajMusteriju() throws InterruptedException {
            brBerbera++; //jedan berber je spreman da radi
            while(brMusterija == 0) {
                wait(); //cekamo da stigne musterija
            }
            brMusterija--; //smanji broj musterija koji ceka
            notify(); //obavesti berbera koji ceka musteriju
        }

        public synchronized void cekajBerbera() throws InterruptedException {
            brMusterija++;
            while(brBerbera == 0) {
                wait(); //cekamo da berber bude slobodan
            }
            brBerbera--; //berber postaje zauzet jer sisa musteriju
            notify(); //obavesti musteriju da moze da sedne na stolicu
        }
        
    } 

    protected final class SalonLock {

        protected Lock brava = new ReentrantLock();
        protected Condition imaBerbera = brava.newCondition();
        protected Condition imaMusterija = brava.newCondition();

        private int brBerbera;
        private int brMusterija;

        public void cekajMusteriju() throws InterruptedException {
            brava.lock();
            try{
                brBerbera++; //jedan berber je trenutno slobodan
                while(brMusterija == 0) {
                    imaMusterija.await(); //cekaj dok se ne pojavi musterij
                }
                brMusterija--; //dosao je neko ko zeli da se sisa
                imaBerbera.signal(); //obavesti musteriju da ima slob. berber 
            } finally {
                brava.unlock();
            }
        }

        public void cekajBerbera() throws InterruptedException {
            brava.lock();
            try{
                brMusterija++;
                while(brBerbera == 0)
                    imaBerbera.await();
                brBerbera--; //jedan berber postaje zauzet
                imaMusterija.signal(); //obavesti berbera da ima musterija
            } finally {
                brava.unlock();
            }
        }
    }

    protected final class SalonSem {

        protected Semaphore mutexMusterija = new Semaphore(0); //
        protected Semaphore mutexBerber = new Semaphore(0);

        //release = Up()
        //acquire = Down()
        //Jedina kombinacija koja nije dozvoljena jeste da 
        //svi prvo pozovu acquire jer ce onda ostati zauvek tako
        public void cekajBerbera() throws InterruptedException {
            mutexBerber.acquire(); 
            //cekaj dok se berber ne javi ili zaposli jednog berberina
            mutexMusterija.release(); 
            //obavesti berberina da je stigla jedna musterija
        }
        public void cekajMusteriju() throws InterruptedException{
            mutexBerber.release(); //berberin se javi da je spreman da sisa musteriju koja ceka
            mutexMusterija.acquire(); //primi jednu musteriju na stolicu
        }

    }
    // protected final SalonSync salon = new SalonSync();
    // protected final SalonLock salon = new SalonLock();
    protected final SalonSem salon = new SalonSem();

	@AutoCreate(2)
	protected class Berber extends Thread {

		@Override
		protected void step() {
			try{
                salon.cekajMusteriju();
                sisa();
            } catch( InterruptedException e) {
                stopGracefully();
            }
		}
	}

	@AutoCreate
	protected class Musterija extends Thread {

		@Override
		protected void run() {
            try{
                salon.cekajBerbera();
                sisaSe();
            } catch (InterruptedException e) {
                //samo odlazi nema potrebe da nesto dodatno radimo
            }
		}
	}

	// ------------------- //
	//    Sistemski deo    //
	// ------------------- //
	// Ne dirati kod ispod //
	// ------------------- //

	protected final Container cekaonica = box("Чекаоница");
	protected final Container stolice   = box("Салон");
	protected final Container main      = column(cekaonica, stolice);
	protected final Operation berber    = init().name("Бербер %d").color(ROSE).text("Спава").container(stolice).update(this::azuriraj);
	protected final Operation musterija = duration("1±1").name("Мушт. %d").color(AZURE).text("Чека").container(cekaonica).update(this::azuriraj);
	protected final Operation sisanjeB  = duration("7").text("Шиша").update(this::azuriraj);
	protected final Operation sisanjeM  = duration("7").text("Шиша се").container(stolice).colorAfter(CHARTREUSE).textAfter("Ошишао се").update(this::azuriraj);

	protected void sisa() {
		sisanjeB.performUninterruptibly();
	}

	protected void sisaSe() {
		sisanjeM.performUninterruptibly();
	}

	protected void azuriraj(Item item) {
		long brB1 = 0;
		long brB2 = 0;
		for (Berber t : stolice.getItems(Berber.class)) {
			if (sisanjeB.getTextBefore().equals(t.getText())) {
				brB1++;
			} else {
				brB2++;
			}
		}
		long brM1 = stolice.stream(Musterija.class).count();
		long brM2 = cekaonica.stream(Musterija.class).count();
		cekaonica.setText(String.format("%d", brM2));
		stolice.setText(String.format("%d : %d", brB1, brM1));
		long razlika = brB1 - brM1;
		if (brB2 > 0 && brM2 > 0) {
			cekaonica.setColor(MAROON);
		} else {
			cekaonica.setColor(OLIVE);
		}
		if (razlika == 0) {
			stolice.setColor(ARMY);
		} else {
			stolice.setColor(MAROON);
		}
	}

	@Override
	protected void initialize() {
		azuriraj(null);
	}

	public static void main(String[] arguments) {
		launch("Успавани бербери");
	}
}