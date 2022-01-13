package vezbaZaKolok;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Data je simulacija rada Ledene šume u Dunavskom parku. Korisnici mogu dola-
 * ziti bilo kada u toku radnog vremena i koristiti usluge klizališta. Prilikom
 * ulaska korišćenje klizaljki je obavezno.
 * 
 * Poznavajući kućni red, korisnici prilikom dolaska prvo iznajmljuju klizaljke,
 * obuvaju ih i tek potom stupaju na stazu za klizanje. Takođe, prilikom odla-
 * ska korisnici obuvaju svoju obuću, vraćaju klizaljke i tek potom odlaze.
 * 
 * Ledena šuma se skoro otvorila i prve nedelje je velika gužva jer je većina
 * mladih parova u gradu odlučila da se malo oproba na ledu. Svi parovi se sa-
 * staju ispred ulaza na klizalište.
 * 
 * A)
 * 
 * Sinhronizovati ove mlade parove tako da momak neće iznajmiti klizaljke i
 * početi da kliza bez da sačeka devojku, niti će se obuti i otići bez devojke.
 * Analogno ni devojka neće uzeti klizaljke i klizati, ili otići iz dvorane bez
 * da i momak uradi to isto.
 * 
 * B)
 * 
 * Toplo vreme je učinilo da led na klizalištu ne bude dovoljno debeo i čvrst.
 * Ovakav led može da izdrži najviše 300 kilograma težine. Sinhronizovati kli-
 * zače tako da ne stupaju na klizalište ako bi pri tome pukao led.
 * 
 * Uključiti animaciju kršenja uslova postavljanjem polja MAX_TEZINA na 300.
 * 
 * C)
 * 
 * Umesto osnovnog problema, sinhronizovati klizače tako da dok klizaju momci,
 * devojke ne stupaju na klizalište. Takođe, dok devojke klizaju, momci ne stu-
 * paju na klizalište.
 * 
 * Uključiti animaciju kršenja uslova postavljanjem polja JEDAN_POL na true.
 * 
 * D)
 *
 * Zbog velike gužve, klizalište ima samo 10 parova klizaljki na raspolaganju
 * za iznajmljivanje.
 * 
 * Uključiti animaciju kršenja uslova postavljanjem polja MAX_BR na 10.
 * 
 * E)
 * 
 * Zbog velike gužve, klizalište ima ograničen broj klizaljki, tj. po 2 para od
 * sledećih veličina: 28‐31, 32‐35, 36‐39, 40‐43, 44‐47, 48‐51 i 52‐55.
 *
 * Uključiti animaciju kršenja uslova postavljanjem polja MAX_CIPELA na 2.
 * 
 * X) * - tezak
 * 
 * Rešiti A) i B) zajedno ako su tezine tipa double.
 * 
 * Promeniti polje tezina na: private double tezina = randomDouble(...);
 * 
 * Y) ** - jako tezak
 * 
 * Rešiti A) tako da umesto u parovima, klizaci klizaju u grupama od tri:
 * jedan momak i dve devojke ili jedna devojka i dva momka. Takodje u takvim
 * grupama i odlaze sa klizalista.
 */
public class LedenaSuma extends Application {

	protected final class Pristup {
		
		protected Semaphore momakUlaz = new Semaphore(0);
		protected Semaphore devojkaUlaz = new Semaphore(0);
		protected Semaphore momakIzlaz = new Semaphore(0);
		protected Semaphore devojkaIzlaz = new Semaphore(0);

		protected Lock brava = new ReentrantLock();
		protected Condition maxTezina = brava.newCondition();

		protected Semaphore osobaUlaz = new Semaphore(4);

		private double currTezina;
		private int brojMomaka;
		private int brojDevojaka;
		private int brojOsoba;
		
		// A)
		public void ulaziMomak() throws InterruptedException {
			momakUlaz.acquire();
			devojkaUlaz.release();
		}

		public void ulaziDevojka() throws InterruptedException {
			momakUlaz.release();
			devojkaUlaz.acquire();
		}

		public void izlaziMomak() throws InterruptedException {
			momakIzlaz.acquire();
			devojkaIzlaz.release();
		}

		public void izlaziDevojka() throws InterruptedException {
			momakIzlaz.release();
			devojkaIzlaz.acquire();
		}

		// B)
		public void ulaziOsoba(double t) throws InterruptedException {
			brava.lock();
			try{
				while(currTezina + t > 300 || brojOsoba == 4)
					maxTezina.await();
				brojOsoba++;
				currTezina += t; //dete uslo
			} finally {
				brava.unlock();
			}
		}

		public void izlaziOsoba(double t) throws InterruptedException {
			brava.lock();
			try{
				currTezina -= t;
				brojOsoba--;
				maxTezina.signalAll();
			} finally {
				brava.unlock();
			}
		}

		// C)
		public synchronized void ulaziMomak2() throws InterruptedException {
			if(brojDevojaka > 0)
				wait();
			brojMomaka++;
		}

		public synchronized void ulaziDevojka2() throws InterruptedException {
			if(brojMomaka > 0)
				wait();
			brojDevojaka++;
		}

		public synchronized void izlaziMomak2() {
			brojMomaka--;
			if(brojMomaka == 0)
				notifyAll();
		}
		public synchronized void izlaziDevojka2() {
			brojDevojaka--;
			if(brojDevojaka == 0)
				notifyAll();
		}

		// D)
		public void ulaziOsoba2() throws InterruptedException {
			osobaUlaz.acquire();
		}

		public void izlaziOsoba2() {
			osobaUlaz.release();
		}
		
		//E)

	}

	protected final class LockA {

		protected Lock bravaUlaz = new ReentrantLock();
		protected Condition momakUlazCondition = bravaUlaz.newCondition();
		protected Condition devojkaUlazCondition = bravaUlaz.newCondition();

		protected Lock bravaIzlaz = new ReentrantLock();
		protected Condition momakIzlazCondition = bravaIzlaz.newCondition();
		protected Condition devojkaIzlazCondition = bravaIzlaz.newCondition();		

		private int brojMomakaUlaz;
		private int brojDevojakaUlaz;

		private int brojMomakaIzlaz;
		private int brojDevojakaIzlaz;

		public void momakUlazi() throws InterruptedException {
			bravaUlaz.lock();
			try{
				brojMomakaUlaz++;
				while(brojDevojakaUlaz == 0)
					devojkaUlazCondition.await();
				brojDevojakaUlaz--;
				momakUlazCondition.signal();
			} finally {
				bravaUlaz.unlock();
			}
		}

		public void devojkaUlazi() throws InterruptedException {
			bravaUlaz.lock();
			try{
				brojDevojakaUlaz++;
				while(brojMomakaUlaz == 0)
					momakUlazCondition.await();
				brojMomakaUlaz--;
				devojkaUlazCondition.signal();
			} finally {
				bravaUlaz.unlock();
			}
		}

		public void momakIzlazi() throws InterruptedException {
			bravaIzlaz.lock();
			try {
				brojMomakaIzlaz++;
				while(brojDevojakaIzlaz == 0 )
					devojkaIzlazCondition.await();
				brojDevojakaIzlaz--;
				momakIzlazCondition.signal();
			} finally {
				bravaIzlaz.unlock();
			}
		}

		public void devojkaIzlazi() throws InterruptedException {
			bravaIzlaz.lock();
			try{
				brojDevojakaIzlaz++;
				while(brojMomakaIzlaz == 0) 
					momakIzlazCondition.await();
				brojMomakaIzlaz--;
				devojkaIzlazCondition.signal();
			} finally {
				bravaIzlaz.unlock();
			}
		}
	}

	// protected Pristup p = new Pristup();
	protected LockA p = new LockA();

	@AutoCreate
	protected class Momak extends Thread {

		private int brojCipela = randomInt(38, 56);
		private int tezina = randomInt(70, 100);

		@Override
		protected void run() {
			obuvaSe();
			try{
				p.momakUlazi();
				// p.ulaziOsoba(tezina);
				// p.ulaziOsoba2();
			} catch(InterruptedException e) {
				stopGracefully();
			}
			kliza();
			try{
				p.momakIzlazi();
				// p.izlaziOsoba(tezina);
				// p.izlaziMomak2();
			} catch(InterruptedException e) {
				stopGracefully();
			}
			// p.izlaziOsoba2();
			izuvaSe();
		}
	}

	@AutoCreate
	protected class Devojka extends Thread {

		private int brojCipela = randomInt(28, 46);
		private int tezina = randomInt(60, 80);

		@Override
		protected void run() {
			obuvaSe();
			try{
				p.devojkaUlazi();
				// p.ulaziDevojka2();
				// p.ulaziOsoba(tezina);
				// p.ulaziOsoba2();
			} catch (InterruptedException e) {
				stopGracefully();
			}
			kliza();
			try{
				// p.izlaziOsoba(tezina);
				p.devojkaIzlazi();
				// p.izlaziDevojka2();
			} catch(InterruptedException e) {
				stopGracefully();
			}
			// p.izlaziOsoba2();
			izuvaSe();
		}
	}

	protected final int MAX_TEZINA = 0;
	protected final int MAX_BR = 0;
	protected final int MAX_CIPELA = 0;
	protected final boolean JEDAN_POL = false;

	// ------------------- //
	//    Sistemski deo    //
	// ------------------- //
	// Ne dirati kod ispod //
	// ------------------- //

	protected final Container ulaz      = box("Улаз").color(NAVY);
	protected final Container garderoba = box("Клизаљке").color(OLIVE);
	protected final Container izlaz     = box("Излаз").color(NAVY);
	protected final Container dvorana   = box("Ледена дворана");
	protected final Container main      = column(row(ulaz, garderoba, izlaz), dvorana);
	protected final Operation momak     = duration("2±1").container(ulaz).name("Момак %d").text("Чека").color(AZURE);
	protected final Operation devojka   = duration("2±1").container(ulaz).name("Девојка %d").text("Чека").color(ROSE);
	protected final Operation obuvanje  = duration("4").text("Узима").container(garderoba).containerAfter(garderoba);
	protected final Operation klizanje  = duration("6±2").text("Клиза").container(dvorana).containerAfter(dvorana).updateBefore(this::azuriraj);
	protected final Operation izuvanje  = duration("4").text("Враћа").container(izlaz).containerAfter(izlaz).updateBefore(this::azuriraj);

	protected void obuvaSe() {
		obuvanje.performUninterruptibly();
	}

	protected void kliza() {
		klizanje.performUninterruptibly();
	}

	protected void izuvaSe() {
		izuvanje.performUninterruptibly();
	}

	protected void azuriraj(Item item) {
		int br = 0;
		int brM = 0;
		int brD = 0;
		double tezina = 0.0;
		List<Integer> cipele = new ArrayList<>();
		Map<Integer, Integer> grupe = new HashMap<>();
		for (Momak momak : dvorana.getItems(Momak.class)) {
			br += 1;
			brM += 1;
			tezina += momak.tezina;
			cipele.add(momak.brojCipela);
			grupe.compute(momak.brojCipela / 4, (k, v) -> v == null ? 1 : v + 1);
		}
		for (Devojka devojka : dvorana.getItems(Devojka.class)) {
			br += 1;
			brD += 1;
			tezina += devojka.tezina;
			cipele.add(devojka.brojCipela);
			grupe.compute(devojka.brojCipela / 4, (k, v) -> v == null ? 1 : v + 1);
		}
		Collections.sort(cipele);
		int max = grupe.values().stream().mapToInt(Integer::intValue).max().orElse(0);
		boolean tezinaOk = MAX_TEZINA == 0 || tezina <= MAX_TEZINA;
		boolean brOk = MAX_BR == 0 || br <= MAX_BR;
		boolean cipeleOk = MAX_CIPELA == 0 || max <= MAX_CIPELA;
		boolean polOk = !JEDAN_POL || brM == 0 || brD == 0;
		StringBuilder builder = new StringBuilder();
		if (MAX_BR != 0) {
			builder.append(String.format("%d", br));
		}
		if (MAX_CIPELA != 0) {
			if (builder.length() != 0) {
				builder.append("  ");
			}
			builder.append(String.format("%s", cipele));
		}
		if (MAX_TEZINA != 0) {
			if (builder.length() != 0) {
				builder.append("  ");
			}
			builder.append(String.format("%4.2f kg", tezina));
		}
		dvorana.setText(builder.toString());
		if (tezinaOk && brOk && cipeleOk && polOk) {
			dvorana.setColor(SILVER);
		} else {
			dvorana.setColor(MAROON);
		}
	}

	@Override
	protected void initialize() {
		azuriraj(null);
	}

	public static void main(String[] arguments) {
		launch("Клизалиште");
	}
}