package teorijskeVezbe;
public class Vezbe1{

	public static void main(String[] arguments) {

		NitPozitivni p = new NitPozitivni();
		p.setName("pozitivni");

		NitNegativni r = new NitNegativni();
		Thread n = new Thread(r);
		n.setName("negativni");

		p.start();
		n.start();

	}
}

class NitPozitivni extends Thread {

	@Override
	public void run() {
		for (int i = 1; i <= 10_000; i++) {
			System.out.println(i);
		}
	}
}

class NitNegativni implements Runnable {

	@Override
	public void run() {
		for (int i = -1; i >= -10_000; i--) {
			System.out.println(i);
		}
	}
}