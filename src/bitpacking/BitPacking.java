package bitpacking;

/**
 * Classe de base abstraite pour les différentes implémentations de Bit Packing.
 * Contient aussi une méthode factory pour créer le bon type de compression.
 */
public abstract class BitPacking {

    /**
     * Compresse le tableau d'entiers et retourne le tableau d'entiers compressés (32-bit blocks).
     */
    public abstract int[] compress(int[] array);

    /**
     * Décompresse la donnée compressée et écrit le résultat dans "out".
     */
    public abstract void decompress(int[] out);

    /**
     * Renvoie la valeur i-ème directement depuis le tableau compressé, sans décompresser tout.
     */
    public abstract int get(int index);

   /**************************FACTORISATION*********************/

    /**
     * Crée une instance de la bonne implémentation de BitPacking selon le type.
     * Type de compression : "chevau", "sanschevau", "overflow"
     */
    public static BitPacking create(String type) {
        type = type.toLowerCase();
        if (type.contains("chevau") && !type.contains("sans")) {
            return new BitPackingChevau();
        } else if (type.contains("sanschevau") || type.contains("sans")) {
            return new BitPackingSansChevau();
        } else if (type.contains("overflow")) {
            return new BitPackingOverflow();
        } else {
            throw new IllegalArgumentException("Type de compression inconnu : " + type);
        }
    }
}
