package bitpacking;

/** Méthode sans chevauchement : chaque entier est stocké entièrement dans un mot de 32 bits, aucun chevauchement */

public class BitPackingSansChevau extends BitPacking {

    private int[] liste_compresse;
    private int nbr_bit; // nombre de bits pour chaque élément (k)
    private int nbr_ele; // nombre d'éléments dans ma liste (nombre d'entiers) n

    @Override
    public int[] compress(int[] liste_non_compresse){
        long debut = System.nanoTime();

        nbr_ele = liste_non_compresse.length;

        // on trouve le plus grand élément pour calculer le nombre de bits nécessaires
        int max = 0;
        for (int i = 0; i < nbr_ele; i++) {
            if (liste_non_compresse[i] > max) max = liste_non_compresse[i];
        }

        nbr_bit = (int) Math.ceil(Math.log(max + 1) / Math.log(2));

        // combien d'éléments peuvent tenir dans un entier 32 bits
        int elementsParMot = 32 / nbr_bit;

        // je calcul le nombre d'entier de 32 bits nécessaires
        int nbrMots = (int) Math.ceil((double) nbr_ele / elementsParMot);

        //je définis ma liste avce la taille qu'il faut
        liste_compresse = new int[nbrMots];

        int entierCourant = 0; // indice dans la liste compressée
        int bitPosition = 0; // position du prochain élément dans le mot courant

        for (int i = 0; i < nbr_ele; i++) {
            //la différence enre le cas avec chevauchement, ici on vérifie d'abord si l'entier courant est pleint on passe au suivant 
            // si le mot courant est plein, passer au suivant

            if (bitPosition + nbr_bit > 32) {
                entierCourant++;
                bitPosition = 0;
            }

            // placer l'élément dans le mot courant
            liste_compresse[entierCourant] |= (liste_non_compresse[i] & ((1 << nbr_bit) - 1)) << bitPosition;
            bitPosition += nbr_bit;
        }

        long fin = System.nanoTime();
        System.out.printf("temps de compression : %.3f ms%n", (fin - debut) / 1e6);

        return liste_compresse;
    }

    @Override
    public void decompress(int[] liste_decompresse) {
        long debut = System.nanoTime();
        // Dans la version sans chevauchement :
        // On calcule combien d'éléments complets peuvent tenir dans un entier de 32 bits.
        // Ici, pas de chevauchement : un élément ne peut jamais être divisé entre deux mots.
        int elementsParMot = 32 / nbr_bit;

        int indiceEle = 0; // indice dans la liste décompressée
        for (int j=0; j< liste_compresse.length;j++) {
            int bitPosition = 0;

            for (int i = 0; i < elementsParMot && indiceEle < nbr_ele; i++) {
                liste_decompresse[indiceEle++] = (liste_compresse[j] >> bitPosition) & ((1 << nbr_bit) - 1);
                bitPosition += nbr_bit;
            }
        }

        long fin = System.nanoTime();
        System.out.printf("temps de décompression : %.3f ms%n", (fin - debut) / 1e6);
    }

    @Override
    public int get(int indice) {
        if (indice < 0 || indice >= nbr_ele) throw new IndexOutOfBoundsException();

        int elementsParMot = 32 / nbr_bit;
        //je cherche dans quel entier est situé mon element rehcerché
        int indiceMot = indice / elementsParMot;
        int bitPosition = (indice % elementsParMot) * nbr_bit;
        //j'extrait l'élément je décale et j'applique le masque 
        return (liste_compresse[indiceMot] >> bitPosition) & ((1 << nbr_bit) - 1);
    }
}
