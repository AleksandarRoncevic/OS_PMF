package teorijskeVezbe;
class Brojac {

	private volatile int br = 0;

	public void inc() {
		br++;
	}

	@Override
	public String toString() {
		return "Vrednost: " + br;
	}
}

class Nit extends Thread {

	private Brojac brojac;

	public Nit(Brojac brojac) {
		this.brojac = brojac;
	}

	@Override
	public void run() {
		for (int i = 0; i < 1000000; i++) {
			brojac.inc();
		}
	}
}

public class Vezbe3_2 {

	public static void main(String[] arguments)
			throws InterruptedException {

		Brojac brojac = new Brojac();

		Nit n1 = new Nit(brojac);
		Nit n2 = new Nit(brojac);
		Nit n3 = new Nit(brojac);
		
		n1.start();
		n2.start();
		n3.start();

		n1.join();
		n2.join();
		n3.join();

		System.out.println(brojac);

	}
}
