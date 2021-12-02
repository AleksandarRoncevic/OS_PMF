package vezbe4;
import java.util.Random;

class Konobar extends Thread {

    private Kuhinja kuhinja;

    public Konobar(String name, Kuhinja k) {
        setName(name);
        this.kuhinja = k;
    }

    @Override
    public void run() {
        Random r = new Random();
        try {
            while(!interrupted()) {
                sleep(10*100);
                kuhinja.prodajJelo(Obrok.values()[r.nextInt(3)]);
            }
        } catch (Exception e) {
            //Izlazimo iz petlje i zavrsavamo rad
        }
    }
}

class Kuvar extends Thread {

    private int brzinaKuvanja;
    private Sastojak sastojak;

    private Kuhinja kuhinja;

    public Kuvar(String name, int brzinaKuvanja, Sastojak sastojak, Kuhinja k) {
        setName(name);
        this.brzinaKuvanja = brzinaKuvanja;
        this.sastojak = sastojak;
        this.kuhinja = k;
    }

    @Override
    public void run() {
        try {
            while(!interrupted()) {
                sleep(brzinaKuvanja*100); 
                //prvo "spavamo" jer je to koliko vremena mu je potrebno da napravi sastojak!!!!
                kuhinja.dodajSastojak(sastojak); 
            }
        } catch (Exception e) {
                // e.printStackTrace();
                //Zavrsavamo sa radom i izlazimo iz petlje
        }
    }
}

class Kuhinja {
    private int brTofu;
    private int brHleb;
    private int litPotaza;
    private double kolPovrca;

    private int promet;

    public int ispisPrometa() {
        return promet;
    }

    public synchronized void dodajSastojak(Sastojak s) {
        switch (s) {
           case TOFU : brTofu++; break;
           case HLEB : brHleb += 6; break;
           case POTAZ : litPotaza += 10; break;
           case POVRCE : kolPovrca += 1; break;
        }
        System.out.println("Trenutno stanje u kuhinji je:");
        System.out.print(" Kolicina povrca: "+ String.format("%.2f", kolPovrca));
        System.out.print(" Kolicina hleba: "+ brHleb);
        System.out.print(" Kolicina tofu: "+ brTofu);
        System.out.println(" Kolicina potaza: "+ litPotaza);
        notifyAll();
        //svi kuvari su sinhronizovani na this referencu Kuhinje kako se ne bi
        //izgubili neki sastojci ako npr oba kuvara spreme tofu. Takođe 
        //konobarice su takođe na istom tom bloku i one su obaveštene pomoću
        //notifyAll svaki put kada se napravi neki sastojak, da provere
        //da li je sada moguće da naprave porudžbinu koju čekaju
    }

    public synchronized void prodajJelo(Obrok o) throws InterruptedException {
        /* Ovde ustvari govorimo konobaricama da sačekaju sastojke za porudžbinu.
        Druga stvar koju postižemo jeste da ne mogu da se naprave 2 porudžbine za koje u zbiru
        nemamo sastojke i odemo u minus sa količinom nekih proizvoda */
        switch (o) {
            case SENDVIC : {
                while(brHleb < 2 || brTofu < 1 || kolPovrca < 0.1) {
                    System.out.println(Thread.currentThread().getName()+ " čeka sendvič");
                    wait();
                };
                brHleb -= 2; brTofu--; kolPovrca -= 0.1;
                promet += o.cena;
                System.out.println("Prodao sam sendvic");
                break;
            }
            case POTAZ : {
                while(litPotaza < 0.5 || brHleb < 1 ) {
                    System.out.println(Thread.currentThread().getName()+ " čeka potaž");
                    wait();
                }
                litPotaza -= 0.5; brHleb--;
                promet += o.cena;
                System.out.println("Prodao sam potaž");
                break;
            }
            case TOFU : {
                while(brTofu < 1 || kolPovrca < 0.3) {
                    System.out.println(Thread.currentThread().getName()+ " čeka potaž");
                    wait();
                }
                brTofu--; kolPovrca -= 0.3; 
                promet += o.cena; 
                System.out.println("Prodao asm tofu");
                break;
            }
        }
    }

    
}

public class Program {


    public static void main(String[] args) throws InterruptedException {
        Thread.currentThread().setName("Gane");
        System.out.println(Thread.currentThread().getName()+ " je otvorio restoran.");
        Kuhinja kuhinja = new Kuhinja();

        Kuvar Miki = new Kuvar("Miki",90, Sastojak.POVRCE,kuhinja);
        Kuvar Mica = new Kuvar("Mica",240, Sastojak.POTAZ,kuhinja);
        Kuvar Joki = new Kuvar("Joki",60, Sastojak.HLEB,kuhinja);
        Kuvar Vule = new Kuvar("Gule",60, Sastojak.TOFU,kuhinja);
        Kuvar Gule = new Kuvar("Vule",60, Sastojak.TOFU,kuhinja);

        Kuvar[] kuvari = {Miki,Mica,Joki,Vule,Gule};

        Konobar rada = new Konobar("Rada",kuhinja);
        Konobar dara = new Konobar("Dara",kuhinja);

        rada.start();
        dara.start();

        for(Kuvar k : kuvari) {
            k.start();
        }

        Thread.sleep(300*100); 
        // ovo je trajanje poslovnog dana i samo je main
        //uspavan, a ostale niti rade svoje

        rada.interrupt(); dara.interrupt();
        for(Kuvar k : kuvari) {
            k.interrupt();
        }
        for(Kuvar k : kuvari) {
            k.join();
        }

        rada.join(); dara.join();
        
        System.out.printf("Ukupan pazar: %d din%n", kuhinja.ispisPrometa());
    }
    
}
