package vezbe3;
import java.util.ArrayList;
import java.util.List;

import vezbe1.*;

class IgracThr extends Thread {
	
	private Karta karta; //ovde ne treba volatile jer polje koristi samo IgracThr. 
						//u prethodnom zadatku je polje karta su istovremeno!! koristili i Diler i Igrac
	private Talon talon;
	private Spil spil;

	public IgracThr(String ime, Spil spil, Talon talon) {
		setName(ime);
		this.spil = spil;
		this.talon = talon;
	}

	@Override
	public void run() {
		primiKartu();
		talon.staviKartu(imaKartu());
		//nakon sto postavi svoju kartu Igrac ceka ostale, i da ne bi bili u praznoj while petlji
		//u metodi cekajOstale smo dodali wait() i time smanjili opterecenje na procesor
		try {
			talon.cekajOstale();
			if(talon.jeNajjaca(imaKartu())) {
				System.out.println(getName()+" je imao najjacu kartu sa: "+ imaKartu());
			} else {
				System.out.println(getName()+" Nije imao najjacu kartu sa: "+ imaKartu());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void primiKartu() { 
		//nije smeo this jer svaki igraci ima razlicitu-svoju this referencu
		//pa onda ne bi bilo nista synced po pitanju primanja karata
		synchronized (spil) {
			this.karta = spil.uzmiOdGore();
		}
	}
	public Karta imaKartu() {
		return this.karta;
	}
}

class Talon {

	private Rang najjaciRang;
	private int brojKarata;

	public Talon() {
		this.najjaciRang = Rang.DVOJKA;
		this.brojKarata = 0;
	}

	public synchronized void staviKartu(Karta karta) {
		if(karta.getRang().compareTo(najjaciRang) > 0) {
			najjaciRang = karta.getRang();
		}	
		brojKarata++;
		if( brojKarata == 12 ) { 
			//logicno kada svi stave kartu na talon, obavesti ih i nastavi sa izvrsavanjem
			notifyAll();
		}
	}	

	// private synchronized boolean proveraVelicine() {
	// 	return brojKarata < 12;
	// }

	public synchronized void cekajOstale() throws InterruptedException {
		while(brojKarata < 12) {
			wait();
		};
	}
	public synchronized boolean jeNajjaca(Karta karta) {
		return karta.getRang().equals(najjaciRang);
	}
}
public class Program {

	public static void main(String[] args) {
		Spil spil = new Spil();
		spil.promesaj();

		Talon talon = new Talon();	

		List<IgracThr> igraci = new ArrayList<IgracThr>();
		for(int i = 0; i < 12; i++) {
			IgracThr igrac = new IgracThr("Igrac" + i,spil,talon);
			igraci.add(igrac);
			igrac.start();
		}
	}
}
