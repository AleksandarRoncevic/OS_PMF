package vezbe1;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

/* Implementirati klasu 'Karta' sa osobinama 'boja' i 'rang' koje predstavljaju
 * standardne osobine karata klasicnog spila od 52+2 karte.
 * 
 * Potrebno je predstaviti sledece boje: pik, karo, herc i tref, dok su dozvo-
 * ljene vrednosti za rang poredjane po velicini: brojevi od 2 do 10, zandar,
 * kraljica, kralj i kec. Takodje je potrebno predstaviti i dva dzokera, jedan
 * u boji, jedan ne.
 * 
 * Implementirati klasu 'Spil' ciji konstruktor kreira nov spil koji sadrzi sve
 * 54 razlicite karte. Takodje, implementirati sledece operacije:
 * 
 *   int velicina()            - vraca broj karata trenutno u spilu
 *   Karta uzmiOdGore()        - ukljanja gornju kartu i vraca je kao rezultat
 *   Karta uzmiOdDole()        - ukljanja donju kartu i vraca je kao rezultat
 *   Karta uzmiIzSredine()     - ukljanja nasumicno izabranu kartu i vraca je
 *   void staviGore(Karta)     - dodaje kartu na vrh spila
 *   void staviDole(Karta)     - dodaje kartu na dno spila
 *   void staviUSredinu(Karta) - dodaje kartu na nasumicno izabrao mesto u spilu
 *   void promesaj()           - nasumicno rasporedjuje karte trenutno u spilu
 * 
 * Napisati program koji implementira sledecu igru za 12 igraca. Igraci redom
 * vuku po jednu kartu sa vrha spila i okrecu je. Program ispisuje koji igrac
 * je izvukao koju kartu. Pobednik je onaj igrac (ili igraci) cija je karta
 * najjaca, pri cemu se ne gleda boja karte a dzokeri su jaci od svih ostalih
 * karata. Ako je bilo vise pobednika igra se ponavlja samo sa pobednicima dok
 * ne ostane samo jedan. Program ispisuje ime konacnog pobednika.
 * 
 * Unapred smisliti imena za igrace, kreirati jedan spil i promesati ga pre
 * igre. Pretpostaviti da u toku igre nece nestati karata u spilu.
 */
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

