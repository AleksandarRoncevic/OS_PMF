package vezbe2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
class Karta {

	private String vrednost;

	public Karta(String vrednost) {
		this.vrednost = vrednost;
	}

	@Override
	public String toString() {
		return vrednost;
	}
}

class Spil {

	private static final String[] BOJE = "\u2660,\u2665,\u2666,\u2663".split(",");
	private static final String[] RANGOVI = "2,3,4,5,6,7,8,9,10,J,Q,K,A".split(",");
	private static final String[] DZOKERI = "\u2605,\u2606".split(",");
	private static final Random random = new Random();

	public Karta uzmi() {
		int id = random.nextInt(54);
		if (id == 53) {
			return new Karta(DZOKERI[0]);
		}
		if (id == 52) {
			return new Karta(DZOKERI[1]);
		}
		String boja = BOJE[id / 13];
		String rang = RANGOVI[id % 13];
		return new Karta(rang + boja);
	}
}
class Igrac extends Thread {

	private volatile Karta karta;

	public Igrac(String ime) {
		setName(ime);
	}

	@Override
	public void run() {
		while(karta == null) {
			//cekamo da dobijemo kartu
		}
		System.out.printf("%-8s je dobio: %3s%n", getName(), karta);
	}
	public void primiKartu(Karta karta) {
		this.karta = karta;
	}
}

class Diler extends Thread {
	private List<Igrac> igraci;
	private Spil spil;
	
	public Diler(List<Igrac> igraci) {
		this.igraci = igraci;
		this.spil = new Spil();
	}

	public void run() {
		for(Igrac igrac : igraci) {
			Karta karta = spil.uzmi(); //izvlacenje karte
			igrac.primiKartu(karta); //dodeljivanje karte
		}
	}
}

public class Program {
	public static void main(String[] args) {
		List<Igrac> igraci = new ArrayList<Igrac>();
		for(int i = 0; i < 12; i++) {
			Igrac igrac = new Igrac("Igrac" + i);
			igraci.add(igrac);
			igrac.start(); //ovde zapocinjemo ove niti?
		}

		//Napravimo dilera i pokrenemo ga
		Diler diler = new Diler(igraci);
		diler.start();
	}
}

