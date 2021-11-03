package vezbe1;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Spil {

    private ArrayList<Karta> spil; 
    public Spil() {
        spil = new ArrayList<Karta>();
        for(Boja b : Boja.values()) {
            if(b == Boja.BEZBOJE || b == Boja.SVEBOJE) continue;
            for(Rang r : Rang.values()) {
                if(r == Rang.JOKER) continue;
                Karta k = new Karta(b, r);
                this.spil.add(k);
            }
        }
        this.spil.add(new Karta(Boja.BEZBOJE, Rang.JOKER));
        this.spil.add(new Karta(Boja.SVEBOJE, Rang.JOKER));
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(Karta k : spil) {
            sb.append(k+"\n");
        }
        return sb.toString();
    }

    public int velicina() {
        return spil.size();
    }
    public Karta uzmiOdGore() {
        return spil.remove(0);
    }
    public Karta uzmiOdDole() {
        return spil.remove(spil.size() - 1);
    }
    public Karta uzmiIzSredine() {
        Random r = new Random();
        return spil.remove(r.nextInt(velicina()));
    }
    public void staviGore(Karta k) {
        spil.add(0, k);
    }
    public void staviDole(Karta k) {
        spil.add(k);
    }
    public void staviUSredinu(Karta k) {
        Random r = new Random();
        spil.add(r.nextInt(velicina()),k);
    }
    public void promesaj() {
        Collections.shuffle(this.spil);
    }

    public static void main(String[] args) {
        Spil s = new Spil();
        System.out.println(s);
        s.promesaj();
        System.out.println(s);
    }
}
