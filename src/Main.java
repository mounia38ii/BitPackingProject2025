import bitpacking.*;
import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage : java bitpacking.Main <chemin_du_fichier>");
            return;
        }

        String fichierPath = args[0];
        List<Integer> listeTemp = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(fichierPath))) {
            while (scanner.hasNextInt()) {
                listeTemp.add(scanner.nextInt());
            }
        } catch (FileNotFoundException e) {
            System.err.println("Fichier non trouvé : " + fichierPath);
            return;
        }

        // Conversion en tableau d'entiers
        int[] liste = listeTemp.stream().mapToInt(Integer::intValue).toArray();
        System.out.println("Liste originale : " + Arrays.toString(liste));

        // Utilisation de la factory 
        BitPacking bp = BitPacking.create(fichierPath);

        // Compression
        int[] listeCompresse = bp.compress(liste);
        System.out.println("Liste compressée : " + Arrays.toString(listeCompresse));

        // Décompression 
        int[] listeDecompresse = new int[liste.length];
        bp.decompress(listeDecompresse);
        System.out.println("Liste décompressée : " + Arrays.toString(listeDecompresse));
    }
}
