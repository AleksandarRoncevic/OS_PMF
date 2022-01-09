package problemiSinhronizacije;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import os.simulation.Application;
import os.simulation.Container;
import os.simulation.Item;
import os.simulation.Operation;
import os.simulation.Thread;

/*
 * Pet filozofa sede za okruglim stolom na kojem se nalazi cinija spageta.
 * Izmedju svaka dva filozofa se nalazi po jedna viljuska. U toku svog zivota
 * svaki filozof naizmenicno razmislja i jede. Filozof moze da jede samo ako
 * ima dve viljuske onu sa svoje leve strane i onu sa svoje desne strane. Svaku
 * viljusku u nekom trenutku moze da koristi samo jedan filozof. Kada filozof
 * zavrsi jedenje, vraca viljuske koje je koristio na sto i time one postaju
 * raspolozive za ostale filozofe.
 * 
 * Filozof moze da uzme svaku viljusku pored njega cim se oslobodi ali ne sme
 * da jede pre nego sto drzi obe viljuske. Pretpostaviti da je kolicina spageta
 * neogranicena i da se filozofi nikada nece zasititi.
 * 
 * Implementirati sinhronizaciju filozofa tako da niko od njih ne gladuje.
 */
public class Filozofi extends Application {

    protected final class StoSync {
        private final int brojFilozofa;
        private boolean[] viljuske;

        public StoSync(int brojFilozofa) {
            this.brojFilozofa = brojFilozofa;
            this.viljuske = new boolean[brojFilozofa];
        }

        private int leva(int i) {
            return (i + 1) % brojFilozofa;
        }

        private int desna(int i) {
            return i;
        }

        public synchronized void uzmiViljuske(int id) throws InterruptedException {
            while(viljuske[leva(id)] || viljuske[desna(id)]) {
                wait(); //cekamo dok su zauzete bilo koja od viljuski
            }
            viljuske[leva(id)] = true; //zauzimamo viljuske za sebe
            viljuske[desna(id)] = true; //zauzimamo viljuske za sebe
        }

        public synchronized void vratiViljuske(int id) {
            viljuske[leva(id)] = false;
            viljuske[desna(id)] = false; //vracamo viljuske
            notifyAll(); //obavestavamo sve koji su eventualno cekali na viljuske
        }
    }

    protected final class StoLock {
        private Lock brava = new ReentrantLock();
        private Condition imaViljuski = brava.newCondition();

        private final int brojFilozofa;
        private boolean[] viljuske;

        public StoLock(int brojFilozofa) {
            this.brojFilozofa = brojFilozofa;
            this.viljuske = new boolean[brojFilozofa];
        }

        private int leva(int i) {
            return (i + 1) % brojFilozofa;
        }

        private int desna(int i) {
            return i;
        }

        public void uzmiViljuske(int id) throws InterruptedException {
            brava.lock(); //ulazimo u K.O
            try{
                while(viljuske[leva(id)] || viljuske[desna(id)])
                    imaViljuski.await(); //cekamo dok nema viljuski
                viljuske[leva(id)] = true;
                viljuske[desna(id)] = true; //zauzemo viljuske
            } finally {
                brava.unlock(); //uvek moramo izaci van K.O.
            }
        }

        public void vratiViljuske(int id) {
            brava.lock();
            try{
                viljuske[leva(id)] = false;
                viljuske[desna(id)] = false;
                imaViljuski.signalAll();
            } finally {
                brava.unlock();
            }
        }
    }

    protected enum Stanje {
        MISLI,GLADAN,JEDE;
    }

    protected final class StoSemafor {
        private final int brojFilozofa;
        private Stanje[] filozofi;

        private Semaphore mutex;
        private Semaphore[] sem;

        public StoSemafor(int brojFilozofa) {
            this.brojFilozofa = brojFilozofa;
            this.mutex = new Semaphore(1); //Kriticna oblast
            this.filozofi = new Stanje[brojFilozofa];
            this.sem = new Semaphore[brojFilozofa];
            for(int i = 0; i < brojFilozofa; i++) {
                this.filozofi[i] = Stanje.MISLI;
                this.sem[i] = new Semaphore(0);
            }
        }

        private int levi(int id) {
            return (id + 1) % brojFilozofa;
        }

        private int desni(int id) {
            return (id + brojFilozofa - 1) % brojFilozofa;
        }

        private void test(int id) {
            if(filozofi[levi(id)] != Stanje.JEDE && 
                filozofi[desni(id)] != Stanje.JEDE && 
                filozofi[id] == Stanje.GLADAN) {
                    filozofi[id] = Stanje.JEDE; //ako susedi ne jedu i trenutni je gladan kreni da jedes
                    sem[id].release(); //probudi filozofa sa id ako je cekao da jede
                }
        }

        public void uzmiViljuske(int id) throws InterruptedException {
            mutex.acquire();
            try{
                filozofi[id] = Stanje.GLADAN;
                test(id);
            } finally {
                mutex.release(); //uvek napustamo K.O.
            }
            sem[id].acquire(); 
            //cekaj ako susedi jedu tj. ako se if-blok unutar test metode se nije izvrsio
            //i samim time nije uradio release() i digao vrednost na 1
            //pa ce se ovde onda ovde uspavati
        }

        public void vratiViljuske(int id) {
            mutex.acquireUninterruptibly();
            try{
                filozofi[id] = Stanje.MISLI; //resetujemo stanje filozofa
                test(levi(id));
                test(desni(id)); //obavesti susede da je zavrsio sa jelom
                //pa ce oni ako su se slucajno uspavali ovde moci da se probude i odrade onaj release.
            } finally {
                mutex.release();
            }
        }
    }
    // protected StoSync sto = new StoSync(5);
    // protected StoLock sto = new StoLock(5);
    protected StoSemafor sto = new StoSemafor(5);

	protected class Filozof extends Thread {

		protected final int id = getID();

		@Override
		protected void step() {
			misli();
			try{ //pokusaj da uzmes viljuske
                sto.uzmiViljuske(id); //uzmi viljuske sa stola i pokusaj da jedes bez prekida
                try{
                    jede();
                } finally {
                    sto.vratiViljuske(id); //u slucaju da se desila greska a i inace moramo vratiti viljuske
                }
            } catch(InterruptedException e) {
                stopGracefully();
            }
			// Sinhronizacija
		}
	}

	// ------------------- //
	//    Sistemski deo    //
	// ------------------- //
	// Ne dirati kod ispod //
	// ------------------- //

	protected final String[] IMENA          = { "Аристотел", "Диоген", "Еуклид", "Питагора", "Талес" };
	protected final Container main          = circle("Филозофи").color(WARM_GRAY);
	protected final Operation filozof       = init().name(IMENA).color(ROSE);
	protected final Operation razmisljanje  = duration("3±2").color(AZURE).text("Мисли").colorAfter(ROSE).textAfter("Гладан");
	protected final Operation jedenje       = duration("3±1").color(CHARTREUSE).text("Једе").colorAfter(CHARTREUSE).textAfter("Јео");

	protected void misli() {
		razmisljanje.performUninterruptibly();
	}

	protected void jede() {
		jedenje.performUninterruptibly();
	}

	protected class Viljuske extends Item {

		private Filozof[] filozofi;
		private int max;

		public Viljuske(int max, Filozof... filozofi) {
			setName("Виљушке");
			this.max = max;
			this.filozofi = filozofi;
			for (Filozof filozof : filozofi) {
				filozof.addPropertyChangeListener(PROPERTY_TEXT, e -> azuriraj());
			}
			azuriraj();
		}

		private void azuriraj() {
			int broj = 0;
			for (Filozof filozof : filozofi) {
				if (jedenje.getTextBefore().equals(filozof.getText())) {
					broj++;
				}
			}
			int slobodno = max - broj;
			if (slobodno > 0) {
				setColor(GREEN);
			} else if (slobodno == 0) {
				setColor(YELLOW);
			} else {
				setColor(RED);
			}
			setText("Слободно: " + slobodno);
		}
	}

	@Override
	protected void initialize() {
		Filozof[] filozofi = new Filozof[IMENA.length];
		for (int i = 0; i < filozofi.length; i++) {
			filozofi[i] = new Filozof();
		}
		Viljuske[] viljuske = new Viljuske[IMENA.length];
		for (int i = 0; i < viljuske.length; i++) {
			viljuske[i] = new Viljuske(1, filozofi[i], filozofi[(i + 1) % filozofi.length]);
		}
		for (int i = 0; i < filozofi.length; i++) {
			filozofi[i].setContainer(main);
			viljuske[i].setContainer(main);
		}
		for (int i = 0; i < filozofi.length; i++) {
			filozofi[i].start();
		}
	}

	public static void main(String[] arguments) {
		launch("Филозофи");
	}
}