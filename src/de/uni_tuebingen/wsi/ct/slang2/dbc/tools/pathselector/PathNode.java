/*
 * Erstellt: 28.04.2005
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.tools.pathselector;

import java.io.Serializable;
import java.util.Vector;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;

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
   public PathNode(DBC_Key key, int id, PathNode root, String name, String description) {
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
    * Gibt den vollen Pfad des Knotens als String zurück
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
   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
	   final int PRIME = 31;
	   int result = 1;
	   result = PRIME * result + ((children == null) ? 0 : children.hashCode());
	   result = PRIME * result + id;
	   result = PRIME * result + ((name == null) ? 0 : name.hashCode());
	   result = PRIME * result + ((root == null) ? 0 : root.hashCode());
	   return result;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
	   if (this == obj)
		   return true;
	   if (obj == null)
		   return false;
	   if (getClass() != obj.getClass())	
		   return false;
	   final PathNode other = (PathNode) obj;
	   if (children == null) {
		   if (other.children != null)
			   return false;
	   } else if (!children.equals(other.children))
		   return false;
	   if (id != other.id)
		   return false;
	   if (name == null) {
		   if (other.name != null)
			   return false;
	   } else if (!name.equals(other.name))
		   return false;
	   if (root == null) {
		   if (other.root != null)
			   return false;
	   } else if (!root.equals(other.root))
		   return false;
	   return true;
   }

}
