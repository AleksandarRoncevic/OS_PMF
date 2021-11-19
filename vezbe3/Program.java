package vezbe3;
import java.util.ArrayList;
import java.util.List;

import vezbe1.*;

class IgracThr extends Thread {
	
	private Karta karta;
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
		talon.cekajOstale();
		if(talon.jeNajjaca(imaKartu())) {
			System.out.println(getName()+" je imao najjacu kartu sa: "+ imaKartu());
		} else {
			System.out.println(getName()+" Nije imao najjacu kartu sa: "+ imaKartu());
		}
	}

	public void primiKartu() {
		synchronized (spil) {
			this.karta = spil.uzmiOdGore();
		}
	}
	public Karta imaKartu() {
		return this.karta;
	}
}

class Talon{

	private Rang najjaciRang;
	private int brojKarata;

	public Talon() {
		this.najjaciRang = Rang.DVOJKA;
		this.brojKarata = 0;
	}

	public synchronized void staviKartu(Karta karta) {
		brojKarata++;
		if(karta.getRang().compareTo(najjaciRang) > 0) {
			najjaciRang = karta.getRang();
		}	
	}	

	private synchronized boolean proveraVelicine() {
		return brojKarata < 12;
	}

	public void cekajOstale() {
		while(proveraVelicine()) {};
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
