package graviton.game.job;

/**
 * Created by Botan on 13/04/2016.
 */
public enum JobsTemplate {
    BUCHERON(2, true, new int[] { 454, 8539, 1378, 2608, 478, 2593, 2592, 2600, 2604, 456, 502, 675, 674, 923, 927, 515, 782, 673, 676, 771 }),
    FORGEUR_EPEES(11, true, new int[] { 494 }),
    SCULPTEUR_ARCS(13, true, new int[] { 500 }),
    FORGEUR_MARTEAUX(14, true, new int[] { 493 }),
    CORDONNIER(15, true, new int[] { 579 }),
    BIJOUTIER(16, true, new int[] { 491 }),
    FORGEUR_DAGUES(17, true, new int[] { 495 }),
    SCULPTEUR_BATONS(18, true, new int[] { 498 }),
    SCULPTEUR_BAGUETTES(19, true, new int[] { 499 }),
    FORGEUR_PELLES(20, true, new int[] { 496 }),
    MINEUR(24, true, new int[] { 497 }),
    BOULANGER(25, true, new int[] { 492 }),
    ALCHIMISTE(26, true, new int[] { 1473, 8542 }),
    TAILLEUR(27, true, new int[] { 951 }),
    PAYSAN(28, true, new int[] { 577, 765, 8127, 8540, 8992 }),
    FORGEUR_HACHES(31, true, new int[] { 922 }),
    PECHEUR(36, true, new int[] { 596, 1860, 1863, 1865, 1866, 1868, 2188, 2366, 6661, 8541 }),
    CHASSEUR(41, true),
    FOREMAGE_DAGUES(43, false, new int[] { 1520 }),
    FOREMAGE_EPEES(44, false, new int[] { 1539 }),
    FOREMAGE_MARTEAUX(45, false, new int[] { 1561 }),
    FOREMAGE_PELLES(46, false, new int[] { 1560 }),
    FOREMAGE_HACHES(47, false, new int[] { 1562 }),
    SCULPTEMAGE_ARCS(48, false, new int[] { 1563 }),
    SCULPTEMAGE_BAGUETTES(49, false, new int[] { 1564 }),
    SCULPTEMAGE_BATONS(50, false, new int[] { 1565 }),
    BOUCHER(56, true, new int[] { 1945 }),
    POISSONNIER(58, true, new int[] { 1946 }),
    FORGEUR_BOUCLIERS(60, true, new int[] { 7098 }),
    CORDOMAGE(62, false, new int[] { 7495 }),
    JOAILLOMAGE(63, false, new int[] { 7493 }),
    COSTUMAGE(64, false, new int[] { 7494 }),
    BRICOLEUR(65, true, new int[] { 7650 }),
    JOAILLER(66, false);

    private final int id;
    private final boolean basic;
    private final int[] tools;

    JobsTemplate(final int id, final boolean basic, final int[] tools) {
        this.id = id;
        this.basic = basic;
        this.tools = tools;
    }

    JobsTemplate(final int id, final boolean basic) {
        this.id = id;
        this.basic = basic;
        this.tools = new int[0];
    }

    public static JobsTemplate get(final int id) {
        for (final JobsTemplate job : values()) {
            if (job.getId() == id) {
                return job;
            }
        }
        return null;
    }

    public int getId() {
        return this.id;
    }

    public boolean isBasic() {
        return this.basic;
    }

    public int[] getTools() {
        return this.tools;
    }
}
