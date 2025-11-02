package bitpacking;

import java.util.*;

/** Deuxième méthode : avec chevauchelent */

public class BitPackingChevau extends BitPacking {

    private int[] liste_compresse;
    private int nbr_bit; // nombre de bits pour chaque élément (entier)
    private int nbr_ele; // nombre d'éléments dans ma liste (nombre d'entiers)

    @Override
    public int[] compress(int[] liste_non_compresse){
        //mesure du temps trouvé sur chatgpt (.nanoTime)
        long debut = System.nanoTime();

        nbr_ele = liste_non_compresse.length;

        // je trouve le plus grand élément de la liste qui a besoin de plus de bit (pour définir la taille de mon paquet)
        int max = 0;
        for (int i = 0; i < nbr_ele; i++) {
            if (liste_non_compresse[i] > max) max = liste_non_compresse[i];
        }

        // nombre de bits nécessaires pour le plus grand entier (fonction de calcul trouvé sur chat gpt aussi)
        nbr_bit = (int) Math.ceil(Math.log(max + 1) / Math.log(2));

        // je définis mon masque 
        int mask = (1 << nbr_bit) - 1;
        
        // combien d'entiers 32 bits je dois avoir dans ma liste compressée 
        int nbr_totalBit = nbr_ele * nbr_bit;
        int nbr_ele_listeCompresse = (int) Math.ceil(nbr_totalBit / 32.0);

        // je définis la liste compressée
        liste_compresse = new int[nbr_ele_listeCompresse];

        long entierCoourant = 0; // changé en long parce que j'ai rencontré des problemes de débordements au chevau_test3(grands nombres → chevauchement important)
        int bits_occupes = 0;
        int indice = 0;

        for (int i = 0; i < nbr_ele; i++)  {
            // je place chaque élément dans l'entier courant tant que y a de la place 
            entierCoourant |= ((long)liste_non_compresse[i] & mask) << bits_occupes;

            // je compte combien de bits déjà occupés
            bits_occupes += nbr_bit; //a chaque fois j'augmente avec la taille de mon paquet)

            if (bits_occupes >= 32) { // si mon entier courant est plein 
                //je le place déja dans ma liste
                liste_compresse[indice++] = (int) entierCoourant; // cast en int
                // ensuite je m'occupe du dernier paquet placé s'il dépasse de mon entier (débordement)

                // Combien de bits dépassent l'entier actuel
                int bitsDepasses = bits_occupes - 32;

                // Combien de bits de la valeur tiennent dans le mot actuel
                int bitsEntierActuel = nbr_bit - bitsDepasses;

                // Décaler la valeur pour garder uniquement la partie qui dépasse
                int bitsRestants = liste_non_compresse[i] >> bitsEntierActuel;

                // Mettre ces bits dans le nouveau entier de 32 bits
                entierCoourant = bitsRestants;

                // Mettre à jour le compteur de bits utilisés dans le nouveau entier
                bits_occupes = bitsDepasses;
            }
        }

        // si il reste un entier partiellement rempli
        if (indice < nbr_ele_listeCompresse) {
            liste_compresse[indice] = (int) entierCoourant; // cast en int
        }

        long fin = System.nanoTime();
        System.out.printf("temps de compression : %.3f ms%n", (fin - debut) / 1e6);

        return liste_compresse;
    }

    @Override
    public void decompress(int[] liste_decompresse) {
        long debut = System.nanoTime();

        // Décompression simplifiée en utilisant get() pour gérer les chevauchements correctement
        //avant j'essaiyais de gerer ça tt seule et puis chat gpt m'a dit d'utiliser get pour pas se tromper et récupérer les bons éléments
        for (int i = 0; i < nbr_ele; i++) {
            liste_decompresse[i] = get(i); // get() sait déjà extraire correctement les éléments
        }

        long fin = System.nanoTime();

        System.out.printf("temps de décompression : %.3f ms%n", (fin - debut) / 1e6);
    }

    @Override
    public int get(int indice) {
        //indice valide
        if (indice < 0 || indice >= nbr_ele) throw new IndexOutOfBoundsException();

        int indiceBit = indice * nbr_bit;//indice du premier bit si taille du paquet=5 et indice =2 donc indiceBit=10
        int indiceInt = indiceBit / 32; // dans quel entier est situé cet élément
        int bitOffset = indiceBit % 32;//le décalage à l’intérieur de cet entier 32 bits
        //indiceBit = 35, indiceInt = 1 et bitOffset = 35 % 32 = 3 donc il est commence au 3 eme bit

        if (bitOffset + nbr_bit <= 32) { // si l'element est contenu entièrement dans le même entier de 32 bit
            return (liste_compresse[indiceInt] >>> bitOffset) & ((1 << nbr_bit) - 1); 
            // utilisé >>> pour éviter propagation du signe
            //Si l’élément fait 10 bits et commence au bit 5, on décale de 5 bits et on masque 0b1111111111 pour récupérer exactement 10 bits.
        } else { // si l'élément recherché est divisé entre deux entiers
            int prePartie = liste_compresse[indiceInt] >>> bitOffset; // >>> pour décalage logique
            int shiftDeuxPartie = nbr_bit - (32 - bitOffset);
            int maskDeuxPartie;
            if (shiftDeuxPartie >= 32) {
                maskDeuxPartie = -1; // tous les bits
            } else {
                maskDeuxPartie = (1 << shiftDeuxPartie) - 1;
            }
            int deuxPartie = liste_compresse[indiceInt + 1] & maskDeuxPartie;
            return prePartie | (deuxPartie << (32 - bitOffset));
        }
    }

}
