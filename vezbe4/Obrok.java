package vezbe4;

public enum Obrok {
    POTAZ(340),
    SENDVIC(230),
    TOFU(520);

    int cena;

    Obrok(int cena) {
        this.cena = cena;
    }
}
