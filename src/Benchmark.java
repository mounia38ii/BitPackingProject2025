
import bitpacking.*;
import java.util.*;

public class Benchmark {

    public static void main(String[] args) {
        // Création de listes de tests
        int[] petit = genererListe(100, 0, 255);          // petites valeurs
        int[] moyen = genererListe(1000, 0, 2048);        // valeurs moyennes
        int[] grand = genererListe(1000, 0, 8000);    // grandes valeurs

        tester("petit", petit);
        tester("moyen", moyen);
        tester("grand", grand);
    }

    private static void tester(String nom, int[] liste) {
        System.out.println("\n=== Test sur jeu de données " + nom + " ===");
        String[] types = {"chevau", "sans", "overflow"};

        for (String type : types) {
            BitPacking bp = BitPacking.create(type);

            long debutC = System.nanoTime();
            int[] compresse = bp.compress(liste);
            long finC = System.nanoTime();

            int[] decompresse = new int[liste.length];
            long debutD = System.nanoTime();
            bp.decompress(decompresse);
            long finD = System.nanoTime();

            System.out.printf(Locale.US,
                "%-12s | compression = %.3f ms | décompression = %.3f ms | taille compressée = %d mots%n",
                type,
                (finC - debutC) / 1e6,
                (finD - debutD) / 1e6,
                compresse.length
            );

            // Vérification d'intégrité 
            if (!Arrays.equals(liste, decompresse)) {
                System.out.println("⚠️ Erreur de décompression pour " + type);
            }
        }
    }

    private static int[] genererListe(int taille, int min, int max) {
        Random r = new Random(0);
        int[] tab = new int[taille];
        for (int i = 0; i < taille; i++) {
            tab[i] = r.nextInt(max - min + 1) + min;
        }
        return tab;
    }
}
