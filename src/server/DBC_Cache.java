/*
 * Erstellt: 11.05.2005
 */

package server;

/**
 * @author Volker Klöbb
 */
class DBC_Cache {

   private CacheElement[] cache;

   DBC_Cache(int size) {
      cache = new CacheElement[size];
   }

   void set(int id, Object object) {
      if (getIndex(id) == -1) {
         int m = getEmptyIndex();
         int v = 0;

         if (m == -1)
            m = cache.length / 2;
         if (cache[m] != null)
            v = cache[m].value;

         shift(m);
         cache[m] = new CacheElement(id, v, object);
         resetValues();
      }
   }

   Object get(int id) {
      int index = getIndex(id);
      if (index >= 0) {
         Object o = cache[index].object;
         cache[index].value++;
         sort(index);
         return o;
      }
      return null;
   }

   private void sort(int a) {
      for (int i = a; i > 0; i--)
         if (cache[i].value > cache[i - 1].value)
            swap(i - 1, i);
         else
            break;
   }

   private void swap(int a, int b) {
      CacheElement tmp = cache[b];
      cache[b] = cache[a];
      cache[a] = tmp;
   }

   private void shift(int a) {
      for (int i = cache.length - 1; i > a; i--)
         cache[i] = cache[i - 1];
   }

   private void resetValues() {
      int sub = getLastValue();
      for (int i = 0; i < cache.length; i++)
         if (cache[i] != null)
            cache[i].value -= sub;
   }

   private int getLastValue() {
      for (int i = cache.length - 1; i >= 0; i--)
         if (cache[i] != null)
            return cache[i].value;
      return 0;
   }

   private int getIndex(int id) {
      for (int i = 0; i < cache.length; i++)
         if (cache[i] != null && cache[i].id == id)
            return i;
      return -1;
   }

   private int getEmptyIndex() {
      for (int i = 0; i < cache.length; i++)
         if (cache[i] == null)
            return i;
      return -1;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < cache.length; i++) {
         sb.append(i + "\t");
         if (cache[i] != null) {
            sb.append("value: " + cache[i].value);
            sb.append("\tid: " + cache[i].id);
         }
         else
            sb.append("leer");
         sb.append('\n');
      }
      return sb.toString();
   }

   private class CacheElement {

      int    id;
      int    value;
      Object object;

      CacheElement(int id, int value, Object object) {
         this.id = id;
         this.value = value;
         this.object = object;
      }
   }

}
