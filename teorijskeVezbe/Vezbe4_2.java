package teorijskeVezbe;

import java.util.ArrayList;
import java.util.List;

class Red<T> {
    private List<T> lista = new ArrayList<>();

    public synchronized boolean jePrazan() {
        return lista.size() == 0;
    }

    public void dodaj(T element) {
        synchronized (this){
            lista.add(element);
        }
    }
    
    public synchronized T izbaci() {
        return lista.remove(0);
    }
}

public class Vezbe4_2 {

    public static void main(String[] args) {
        Red<Integer> red = new Red<>();
        red.dodaj(5);
        if(!red.jePrazan()){ 
            Integer x = red.izbaci();
            System.out.println(x);
        } //ovo nije jedna atomska operacija, te postoji šansa da nešto ode po zlu?
        //pa bismo morali i ceo if da wrappujemo u sync blok nad istim kontrol objektom 'red'
    }
}
