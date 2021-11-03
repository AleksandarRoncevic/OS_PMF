package vezbe1;
public class Igrac {
    private String ime;
    private Karta trenKarta;

    public Igrac(String ime) {
        this.ime = ime;
        this.trenKarta = null;
    }

    public Karta getTrenKartu() {
        return this.trenKarta;
    }
    public void izvuciKartu(Karta k) {
        this.trenKarta = k;
    }
    public void vratiKartuUSpil() {
        this.trenKarta = null;
    }
    @Override
    public String toString() {
        return "Igrac " + ime;
    }
    
    
}
