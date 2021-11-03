package vezbe1;
public class Karta implements Comparable<Karta> {
    private Boja boja;
    private Rang rang;

    public Karta(Boja boja, Rang rang) {
        this.boja = boja;
        this.rang = rang;
    }
    public String toString() {
        return "Karta "+this.boja.ime+" "+this.rang.name();
    }
    public Rang getRang() {
        return this.rang;
    }

    @Override
    public int compareTo(Karta o) {
        return this.rang.compareTo(o.rang);
    };

    public static void main(String[] args) {
        Karta k1 = new Karta(Boja.HERC,Rang.CETVORKA);
        Karta k2 = new Karta(Boja.HERC,Rang.TROJKA);

        if(k1.compareTo(k2) > 0) {
            System.out.println("k1 je veca");
        } else {
            System.out.println("k2 veca");
        }

    }

}

