package vezbe4;
import java.util.Random;

class Konobar extends Thread {

    private Kuhinja kuhinja;
    // private String[] jela = {"potaz", "tofu", "sendvic"};

    public Konobar(String name, Kuhinja k) {
        setName(name);
        this.kuhinja = k;
    }

    @Override
    public void run() {
        Random r = new Random();
        while(!interrupted()) {
            try {
                kuhinja.prodajJelo(Obrok.values()[r.nextInt(3)]);
                Thread.sleep(10*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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
        while(!interrupted()) {
            kuhinja.dodajSastojak(sastojak);
            try {
                Thread.sleep(brzinaKuvanja*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Kuhinja {
    private int brTofu;
    private int brHleb;
    private int litPotaza;
    private double kolPovrca;

    private int promet;

    public void ispisPrometa() {
        System.out.println("Promet je "+ promet);
    }

    public synchronized void dodajSastojak(Sastojak s) {
        switch (s) {
           case TOFU : brTofu++; break;
           case HLEB : brHleb += 6; break;
           case POTAZ : litPotaza += 10; break;
           case POVRCE : kolPovrca += 1; break;
        }
        notifyAll();
    }

    public synchronized void prodajJelo(Obrok o) throws InterruptedException {
        switch (o) {
            case SENDVIC : {
                while(brHleb < 2 && brTofu < 1 && kolPovrca < 0.1) wait();
                brHleb -= 2; brTofu--; kolPovrca -= 0.1;
                promet += o.cena;
                break;
            }
            case POTAZ : {
                while(litPotaza < 0.5 && brHleb < 1 ) wait();
                litPotaza -= 0.5; brHleb--;
                promet += o.cena; 
                break;
            }
            case TOFU : {
                while(brTofu < 1 && kolPovrca < 0.3) wait();
                brTofu--; kolPovrca -= 0.3;
                promet += o.cena; 
                break;
            }
        }
    }

    
}

public class Program {


    public static void main(String[] args) {
        Kuhinja kuhinja = new Kuhinja();

        Kuvar Miki = new Kuvar("Miki",90, Sastojak.POVRCE,kuhinja);
        Kuvar Mica = new Kuvar("Mica",240, Sastojak.POTAZ,kuhinja);
        Kuvar Joki = new Kuvar("Joki",60, Sastojak.HLEB,kuhinja);
        Kuvar Vule = new Kuvar("Gule",60, Sastojak.TOFU,kuhinja);
        Kuvar Gule = new Kuvar("Vule",60, Sastojak.TOFU,kuhinja);

        Kuvar[] kuvari = {Miki,Mica,Joki,Vule,Gule};

        Konobar Rada = new Konobar("Rada",kuhinja);
        Konobar Dara = new Konobar("Dara",kuhinja);

        for(Kuvar k : kuvari) {
            k.start();
        }

        Rada.start();
        Dara.start();
    }
    
}
