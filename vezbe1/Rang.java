package vezbe1;
public enum Rang {
    DVOJKA(2),
    TROJKA(3),
    CETVORKA(4),
    PETICA(5),
    SESTICA(6),
    SEDMICA(7),
    OSMICA(8),
    DEVETKA(9),
    DESETKA(10),
    ZANDAR(11),
    KRALJICA(12),
    KRALJ(13),
    KEC(14),
    JOKER(15);

    private Rang(int rang) {
        this.rang = rang;
    }
    public final int rang;  
}