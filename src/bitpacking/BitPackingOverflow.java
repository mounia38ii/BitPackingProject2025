package bitpacking;

import java.util.*;

/**
 * Troisième méthode : Bit Packing avec zone de débordement (overflow area)
 */
public class BitPackingOverflow extends BitPacking {

    private int[] liste_compresse;
    private int[] overflow; // zone de débordement
    private int nbr_bit_princi; // nombre de bits pour les valeurs donc la zone principales
    private int nbr_bit_overflow; // nombre de bits pour les indices overflow
    private int nbr_ele; // nombre d'éléments dans le tableau original
    private int nbr_ele_overflow; // nombre d'éléments dans la zone de débordement

    @Override
    public int[] compress(int[] liste_non_compresse) {
        long debut = System.nanoTime();

        nbr_ele = liste_non_compresse.length;

        // Trouver la valeur maximale
        int max = 0;
        for (int val : liste_non_compresse) {
            if (val > max) max = val;
        }

        // seuil : ici j'ai choisi  max/8 comme seuil entre petit/grand
        int seuil = Math.max(1, max / 8);

        // trouver les éléments grands à mettre dans l'overflow
        List<Integer> overflowList = new ArrayList<>();
        for (int val : liste_non_compresse) {
            if (val > seuil) overflowList.add(val);
        }

        nbr_ele_overflow = overflowList.size();
        //mainetant je fais mon tableau pour la zone overflow et je stocke les grand éléments dedans
        overflow = new int[nbr_ele_overflow];
        for (int i = 0; i < nbr_ele_overflow; i++) overflow[i] = overflowList.get(i);

        // Calcul du nombre de bits nécessaires pour compresser les petits ele
        nbr_bit_princi = (int) Math.ceil(Math.log(seuil + 1) / Math.log(2));
        // pour les indices d'overflow : nombre de bits pour représenter nbr_ele_overflow
        //si on a 8 élément dans la zone overflow donc on a besoin de 3 bits
        nbr_bit_overflow = (int) Math.ceil(Math.log(Math.max(nbr_ele_overflow, 1)) / Math.log(2));
        if (nbr_bit_overflow == 0) nbr_bit_overflow = 1; // au moins 1 bit

        // chaque valeur principale a 1 bit de signal + k bits de valeur ou d'indice
        int bitsParValeur = 1 + Math.max(nbr_bit_princi, nbr_bit_overflow);

        // Calcul du nombre total de mots 32 bits nécessaires
        int totalBits = nbr_ele * bitsParValeur;
        int nbrMots = (int) Math.ceil(totalBits / 32.0);
        liste_compresse = new int[nbrMots];

        // Encodage
        long buffer = 0;
        int bitsOccupes = 0;
        int indiceMot = 0;

        int overflowIndex = 0; // index pour attribuer les indices d'overflow

        for (int val : liste_non_compresse) {
            int flag;
            int valeurAEncoder;

            if (val > seuil) { // si grand
                flag = 1;
                valeurAEncoder = overflowIndex++; // on encode l'indice dans overflow
            } else { // si petit 
                flag = 0;
                valeurAEncoder = val;
            }

            int paquet = (flag << Math.max(nbr_bit_princi, nbr_bit_overflow)) | valeurAEncoder;

            buffer |= ((long) paquet) << bitsOccupes;
            bitsOccupes += bitsParValeur;

            // Si on dépasse 32 bits, on écrit un mot et on garde le reste
            while (bitsOccupes >= 32) {
                liste_compresse[indiceMot++] = (int) (buffer & 0xFFFFFFFFL);
                buffer >>>= 32;
                bitsOccupes -= 32;
            }
        }

        if (bitsOccupes > 0 && indiceMot < nbrMots) {
            liste_compresse[indiceMot] = (int) (buffer & 0xFFFFFFFFL);
        }

        long fin = System.nanoTime();
        System.out.printf("temps de compression (avec overflow): %.3f ms%n", (fin - debut) / 1e6);

        return liste_compresse;
    }

    @Override
    public void decompress(int[] liste_decompresse) {
        long debut = System.nanoTime();

        for (int i = 0; i < nbr_ele; i++) {
            liste_decompresse[i] = get(i);
        }

        long fin = System.nanoTime();
        System.out.printf("temps de décompression (avec overflow): %.3f ms%n", (fin - debut) / 1e6);
    }

    @Override
    public int get(int indice) {
        if (indice < 0 || indice >= nbr_ele) throw new IndexOutOfBoundsException();

        int bitsParValeur = 1 + Math.max(nbr_bit_princi, nbr_bit_overflow);

        int globalBitPos = indice * bitsParValeur;
        int intIndex = globalBitPos / 32;
        int bitOffset = globalBitPos % 32;

        long mot = ((long) liste_compresse[intIndex]) & 0xFFFFFFFFL;
        long next = 0;
        if (bitOffset + bitsParValeur > 32 && intIndex + 1 < liste_compresse.length) {
            next = ((long) liste_compresse[intIndex + 1]) & 0xFFFFFFFFL;
        }

        long combine = mot | (next << 32);
        int paquet = (int) ((combine >>> bitOffset) & ((1L << bitsParValeur) - 1));

        int flag = paquet >>> Math.max(nbr_bit_princi, nbr_bit_overflow);
        int valBits = paquet & ((1 << Math.max(nbr_bit_princi, nbr_bit_overflow)) - 1);

        if (flag == 0) {
            return valBits;
        } else {
            if (valBits < nbr_ele_overflow) {
                return overflow[valBits];
            } else {
                return -1;
            }
        }
    }
}
