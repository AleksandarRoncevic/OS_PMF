package problemiSinhronizacije;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import os.simulation.Application;
import os.simulation.AutoCreate;
import os.simulation.Color;
import os.simulation.Container;
import os.simulation.Item;
import os.simulation.Operation;
import os.simulation.Thread;

/*
 * Dat je bafer fiksne velicine. Vise procesa zeli da istovremeno dodaje i
 * uklanja elemente sa ovog bafera.
 * 
 * Realizovati operaciju dodavanja tako da, ako je bafer pun, blokira proces
 * dok se ne oslobodi mesto za novi element. Takodje, realizovati operaciju
 * uklanjanja tako da, ako je bafer prazam, blokira proces dok se ne doda novi
 * element. 
 */
public class ProducerConsumer extends Application {

    protected class BaferSync extends Bafer {

        public BaferSync(int velicina) {
            super(velicina);
        }
        
        private void cekajDokIma(int br) { 
            //mozemo pomocu istog koda da proveravamo uslov za oba tipa ucesnika
            boolean interrupted = Thread.interrupted();
            while(lista.size() == br) { //ako je velicina bafera = sa prosledjenim brojem
                try {
                    wait();
                } catch( InterruptedException e) {
                    interrupted = true;
                }
                if(interrupted) {
                    Thread.currentThread().interrupt();  
                    //ako su prekinute treba ih zaustaviti
                }
            }
        }
        
        @Override
        public synchronized void stavi(Element o) {
            cekajDokIma(velicina); //cekaj dok je na stack-u n elemenata
            super.stavi(o);
            if(lista.size() == 1)
                notify(); 
                //obavesti jednog ako je niz prazan bio pre ovog ubacaja jer neko mozda ceka
        }

        @Override
        public synchronized Element uzmi() {
            cekajDokIma(0); //cekaj do je stack prazan
            Element e = super.uzmi();
            if(lista.size() == velicina - 1) notify(); //obavesti ako je do malopre bio pun stack
            return e;
        }
    }

    protected class BaferLock extends Bafer {

        public BaferLock(int velicina) {
            super(velicina);
        }

        protected Lock brava = new ReentrantLock(); //treba nam brava koja kontrolise pristup baferu
        protected Condition imaPraznih = brava.newCondition(); //uslov koji prati da li ima praznih mesta u baferu
        protected Condition imaPunih = brava.newCondition(); //uslov koji prati da li ima punih 'paketa' u baferu

        @Override
        public void stavi(Element o) {
            brava.lock(); //ulazimo u K.O.
            try {
                while(lista.size() == velicina) { //uslov koji pratimo za Producera
                    imaPraznih.awaitUninterruptibly(); //cekamo bez obaziranja na prekide 
                }
                super.stavi(o);
                if(lista.size() == 1)
                    imaPunih.signal(); //obavestavamo consumere da ima jedan paket
            } finally {
                brava.unlock(); //izlazimo iz K.O. sta god se desilo pa cak i neka greska
            }
        }

        @Override
        public Element uzmi() {
            brava.lock(); //ulazimo u K.O.
            try{
                while(lista.size() == 0) {
                    imaPunih.awaitUninterruptibly(); //cekamo dok nema barem jedna puna
                }
                Element e = super.uzmi();
                if(lista.size() == velicina - 1 ) //vrlo bitno da bude n-1 ili ostaju vecno u wait-u produceri
                    imaPraznih.signal(); //obavesti Producera da ima praznih 'kutija'
                return e;
            } finally {
                brava.unlock();
            }
        }
    }

    protected class BaferSemafor extends Bafer {

        public BaferSemafor(int velicina) {
            super(velicina);
            imaPraznih = new Semaphore(velicina); //na pocetku ima n praznih kutija
            imaPunih = new Semaphore(0); //i 0 punih kutija
        }

        //veoma je bitno da mutex sam po sebi ne postoji nego nasa implementacija
        //ogranicava da je interval njegovih vrednosti 0 ili 1
        protected Semaphore mutex = new Semaphore(1); //kontrola ulaza u K.O.
        protected Semaphore imaPraznih; //semafor koji prati broj praznih 'kutija'
        protected Semaphore imaPunih; //semafor koji prati broj punih 'kutija'

        @Override
        public void stavi(Element o) {
            imaPraznih.acquireUninterruptibly(); 
            //BITAN REDOSLED
            //prvo proverimo da li ima praznih kutija pre nego sto zauzmemo K.O. da ne bi ostali zakljucani
            mutex.acquireUninterruptibly(); //zelimo da ignorisemo prekide
            try{
                super.stavi(o);
            } catch(Exception e) { //ako ima greske vrati vrednost semafora
                imaPraznih.release(); //posto nismo uspesno stavili element i mesto je ostalo slobodno
                throw e;
            } finally {
                mutex.release(); //izlazimo iz K.O.
            }
            imaPunih.release(); //povecamo broj punih kutija 
        }

        @Override
        public Element uzmi() {
            imaPunih.acquireUninterruptibly();
            mutex.acquireUninterruptibly();
            try{
                Element e = super.uzmi();
                imaPraznih.release(); //povecali smo broj praznih kutija
                return e;
            } catch(Exception e) { //analogno slucaju stavi
                imaPunih.release();
                throw e;
            } finally {
                mutex.release();
            }
        }
    }

	// protected Bafer bafer = new Bafer(12);
	// protected Bafer bafer = new BaferSync(12);
	// protected Bafer bafer = new BaferLock(12);
    protected Bafer bafer = new BaferSemafor(12);

	protected class Bafer {

		protected final List<Element> lista = new ArrayList<>();
		protected final int velicina;

		public Bafer(int velicina) {
			this.velicina = velicina;
		}

		// Sinhronizacija
		public void stavi(Element o) {
			lista.add(o);
			elementi.addItem(o);
		}

		// Sinhronizacija
		public Element uzmi() {
			Element result = lista.remove(0);
			elementi.removeItem(result);
			return result;
		}
	}

	// ------------------- //
	//    Sistemski deo    //
	// ------------------- //
	// Ne dirati kod ispod //
	// ------------------- //

	@AutoCreate(4)
	protected class Proizvodjac extends Thread {

		private final int id = getID();
		private int br = 0;

		@Override
		protected void step() {
			Element element = proizvedi(id + "x" + (br++));
			bafer.stavi(element);
		}

	}

	@AutoCreate(4)
	protected class Potrosac extends Thread {

		@Override
		protected void step() {
			Element element = bafer.uzmi();
			potrosi(element);
		}
	}

	protected final Container proizvodjaci = box("Произвођачи").color(NAVY);
	protected final Container potrosaci    = box("Потрошачи").color(MAROON);
	protected final Container elementi     = box("Елементи").color(NEUTRAL_GRAY);
	protected final Container main         = row(proizvodjaci, elementi, potrosaci);
	protected final Operation proizvodjac  = init().name("Произв. %d").color(AZURE).text("Чека").container(proizvodjaci);
	protected final Operation potrosac     = init().name("Потр. %d").color(ROSE).text("Чека").container(potrosaci);
	protected final Operation element      = init();
	protected final Operation proizvodnja  = duration("3±1").text("Производи").textAfter("Чека");
	protected final Operation potrosnja    = duration("7±2").text("Троши %s").textAfter("Чека");

	protected Element proizvedi(String vrednost) {
		proizvodnja.performUninterruptibly();
		return new Element(vrednost);
	}

	protected void potrosi(Element element) {
		potrosnja.performUninterruptibly(element.getName());
	}

	protected class Element extends Item {

		public Element(String vrednost) {
			setName(vrednost);
		}

		private int getIndex() {
			return bafer.lista.indexOf(this);
		}

		@Override
		public Color getColor() {
			int index = getIndex();
			if ((index >= 0) && (index < bafer.velicina)) {
				return CHARTREUSE;
			} else {
				return ORANGE;
			}
		}

		@Override
		public String getText() {
			return String.format("Bafer[%d]", getIndex());
		}
	}

	public static void main(String[] arguments) {
		launch("Произвођачи и потрошачи");
	}
}