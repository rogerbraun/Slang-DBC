/*
 * Erstellt: 28.04.2005
 */

package pathselector;

import java.io.Serializable;
import java.util.Vector;

import connection.DBC_Key;

/**
 * Ein Knoten in dem Pfad-Baum
 * 
 * @author Volker Klöbb
 */
public class PathNode
      implements
         Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 6373447266591593463L;
   private int               id;
   private PathNode          root;
   private Vector            children;
   private String            name;
   private String            description;

   /**
    * Wird vom DBC benötigt
    */
   public PathNode(DBC_Key key,
         int id,
         PathNode root,
         String name,
         String description) {
      key.unlock();
      this.id = id;
      this.root = root;
      this.name = name;
      this.description = description;

      children = new Vector(10);

      if (root != null)
         root.add(this);
   }

   /**
    * Die ID des Knotens
    */
   public int getId() {
      return id;
   }

   /**
    * Der Name des Knotens
    */
   public String getName() {
      return name;
   }

   /**
    * Die Beschreibung zu dem Knoten
    */
   public String getDescription() {
      return description;
   }

   /**
    * Gibt einen untergeordneten Knoten mit der ID zurück
    * 
    * @param id
    *        die ID des zu suchenden Knoten
    * @return Der Knoten oder null, falls es unter diesem Knoten keinen mit der
    *         ID gibt.
    */
   public PathNode getNode(int id) {
      if (this.id == id)
         return this;

      for (int i = 0; i < children.size(); i++) {
         PathNode res = ((PathNode) children.get(i)).getNode(id);
         if (res != null)
            return res;
      }
      return null;
   }

   void add(PathNode node) {
      children.add(node);
   }

   /**
    * Der Elterknoten
    */
   public PathNode getRoot() {
      return root;
   }

   /**
    * Alle Kinder von diesem Knoten
    */
   public Vector getChildren() {
      return children;
   }

   public String toString() {
      return name;
   }

   /**
    * Gibt den vollen Pfad des Knotesn als String zurück
    */
   public String getFullPath() {
      if (root == null)
         return null;
      String rs = root.getFullPath();
      if (rs != null)
         return root.getFullPath() + " " + name;
      return name;
   }

   public String toString(String tab) {
      StringBuffer sb = new StringBuffer(tab + name);
      tab += "...";
      sb.append('\n');
      for (int i = 0; i < children.size(); i++) {
         PathNode node = (PathNode) children.get(i);
         sb.append(node.toString(tab));
      }
      return sb.toString();
   }

}
