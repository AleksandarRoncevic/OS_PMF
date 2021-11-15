package teorijskeVezbe;

public class Vezbe2{

	public static void main(String[] arguments) throws InterruptedException {

		NitPozitivni p = new NitPozitivni();
		p.setName("pozitivni");

		NitNegativni r = new NitNegativni();
		Thread n = new Thread(r);
		n.setName("negativni");

		p.start();
		n.start();

		Thread.sleep(2000);

		p.interrupt();
		n.interrupt();

		p.join();
		n.join();

		System.out.println("Kraj");

	}
}

class NitPozitivni extends Thread {

	@Override
	public void run() {
		for (int i = 1; i <= 10_000_000 && !interrupted(); i++) {
			System.out.println(i);
		}
	}
}

class NitNegativni implements Runnable {

	@Override
	public void run() {
		for (int i = -1; i >= -10_000_000 && !Thread.interrupted(); i--) {
			System.out.println(i);
		}
	}
}