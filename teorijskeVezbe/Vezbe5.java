package teorijskeVezbe;

import java.util.ArrayList;
import java.util.List;

class Bafer<T> {

    protected final List<T> lista;

    public Bafer() {
        this.lista = new ArrayList<>();
    }

    public synchronized boolean jePrazan() {
        return lista.size() == 0;
    }

    public synchronized void dodaj(T element) {   
            lista.add(element);
    }
    
    public synchronized T izbaci() {
        return lista.remove(0);
    }
}

class Proizvodjac extends Thread {
    private Bafer<String> bafer;

    public Proizvodjac() {

    }
}

public class Vezbe5 {

    public static final int BR_PROIZVĐAČA = 3;
    public static final int BR_POTROŠAČA = 2;

    public static void main(String[] args) {
        for(int i = 0; i < BR_PROIZVĐAČA; i++) {

        }

        for(int i = 0; i < BR_POTROŠAČA; i++) {
            
        }
    }
    
}
