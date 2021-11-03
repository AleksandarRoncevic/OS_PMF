package vezbe1;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class Program {
    private Spil spil;
    private String[] imena = 
        {"Aleksa","Blagoje","Cile","Dragan","Emir","Fratko","Goran","Herkules","Ivan","Jelenko","Kornelije"};
    private ArrayList<Igrac> igraci = new ArrayList<Igrac>();

    public Program() {
        this.spil = new Spil();
        for(String s : imena) {
            this.igraci.add(new Igrac(s));
        }
    }
    public int brojIgraca() {
        return igraci.size();
    }
    public void simulirajPotez(Scanner sc) {
        // Scanner sc = new Scanner(System.in);
        for(Igrac i : igraci) {
            System.out.println(i+" je na potezu.");
            System.out.println("Da li zeli da izvuce kartu sa vrha, iz sredine ili sa dna spila?");
            System.out.println("Akcije su redom oznacene brojem 1,2,3");

            int opcija = sc.nextInt();
            // int opcija = 1;
            switch(opcija) {
                case 1: i.izvuciKartu(spil.uzmiOdGore()); break;
                case 2: i.izvuciKartu(spil.uzmiIzSredine()); break;
                case 3: default: i.izvuciKartu(spil.uzmiOdDole());
            }
        }
        // sc.close();
        Rang max  = najjaciRang();
        ispisiTrenutneKarte();
        izbaciGubitnike(max);
    }
    private void ispisiTrenutneKarte() {
        for(Igrac i : igraci) 
            System.out.println(i+" je izvukao "+ i.getTrenKartu());
    }
    private void izbaciGubitnike(Rang max) {
        for(Iterator<Igrac> iterator = igraci.iterator(); iterator.hasNext();) {
            Igrac currIgrac = iterator.next();
            Karta curr = currIgrac.getTrenKartu();
            if(curr.getRang().compareTo(max) < 0 ) {
                iterator.remove();  //izbaci igraca iz igre
            } else
                currIgrac.vratiKartuUSpil(); //resetuj ako je preziveo
            spil.staviUSredinu(curr); //vracanje karte nazad u spil
        }
        igraci.trimToSize();
    };
    private Rang najjaciRang() {
        Rang r = Rang.DVOJKA;
        for(Igrac i : igraci) 
            if(r.compareTo(i.getTrenKartu().getRang()) < 0)
                r = i.getTrenKartu().getRang();
        return r;
    }
    public static void main(String[] args) {
        Program p = new Program();
        Scanner sc = new Scanner(System.in);
        while(p.brojIgraca() > 1) {
            p.spil.promesaj();
            p.simulirajPotez(sc);
            System.out.println("Preziveli su: \n");
            System.out.println(p.igraci);
        }
        sc.close();
        System.out.println(p.igraci+" je pobednik.");
    }
}

