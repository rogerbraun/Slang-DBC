/*
 * Erstellt: 21.03.2006
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;

import de.uni_tuebingen.wsi.ct.slang2.dbc.client.DBC;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_KeyAcceptor;


/**
 * Dient zur Zusammenfassung aller Analysen eines Kapitels. Diese
 * Zusammenfassung kann dann in eine Datei geschrieben werden.
 * 
 * @author Volker Klöbb
 */
public class CompleteAnalyse
      implements
         Serializable, DBC_KeyAcceptor {

   private Chapter             chapter;
   private Comments            comments = new Comments();
   private IllocutionUnitRoots roots;
   private DirectSpeeches      directSpeeches;
   private Dialogs             dialogs;
   private Isotopes            isotopes;
   private Vector<PronounComplex>              complexes;
   private Vector              themas;
   private DBC_Key key;

   public CompleteAnalyse(String server, int chapterID) throws Exception {
      DBC dbc = new DBC(server);

      chapter = dbc.loadChapter(chapterID);
      roots = dbc.loadIllocutionUnitRoots(chapter);
      directSpeeches = dbc.loadDirectSpeeches(chapter);
      dialogs = dbc.loadDialogs(chapter);
      isotopes = dbc.loadIsotopes(chapter);
      complexes = dbc.loadComplexes(roots);
      themas = dbc.loadThemas(chapter);

      dbc.loadDialogComments(comments, chapter);
      dbc.loadDirectSpeechComments(comments, chapter);
      dbc.loadIllocutionUnitComments(comments, chapter);

      dbc.close();
   }

   public CompleteAnalyse(File file) throws Exception {
      DBC_Key.makeKey(this);
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));

      chapter = (Chapter) in.readObject();
      roots = (IllocutionUnitRoots) in.readObject();
      roots.setChapter(key, chapter);
      directSpeeches = (DirectSpeeches) in.readObject();
      directSpeeches.setChapter(key, chapter);
      dialogs = (Dialogs) in.readObject();
      dialogs.setChapter(key, chapter);
      isotopes = (Isotopes) in.readObject();
      isotopes.setChapter(key, chapter);
      complexes = (Vector) in.readObject();
      themas = (Vector) in.readObject();
      comments = (Comments) in.readObject();

      in.close();
   }

   public void resetIDs() {
      chapter.resetIDs();
      roots.resetIDs();
      directSpeeches.resetIDs();
      dialogs.resetIDs();
      isotopes.resetIDs();

      for (PronounComplex iterable_element : complexes) {
    	  iterable_element.resetIDs();
	}

      for (int i = 0; i < themas.size(); i++)
         ((Thema_DB) themas.get(i)).id = -1;
   }

   public void save(String server, boolean saveChapter)
         throws Exception {
      DBC dbc = new DBC(server);

      if (saveChapter) {
         DBC_Key.makeKey(this);
         dbc.saveChapter(key, chapter);
      }
      dbc.saveIllocutionUnitRoots(roots);
      dbc.saveDirectSpeeches(chapter, directSpeeches, directSpeeches);
      dbc.saveDialogs(chapter, dialogs);
      dbc.saveIsotopes(isotopes);
      dbc.saveComplexes(complexes);
      dbc.saveThemas(chapter, themas);
      dbc.saveComments(comments);

      dbc.close();
   }

   public Chapter getChapter() {
      return chapter;
   }

   public Vector getComplexes() {
      return complexes;
   }

   public Dialogs getDialogs() {
      return dialogs;
   }

   public DirectSpeeches getDirectSpeeches() {
      return directSpeeches;
   }

   public Isotopes getIsotopes() {
      return isotopes;
   }

   public IllocutionUnitRoots getIllocutionUnitRoots() {
      return roots;
   }

   public Vector getThemas() {
      return themas;
   }

   public void setThemas(Vector themas) {
      this.themas = themas;
   }

   public Comments getComments() {
      return comments;
   }

   public void setComplexes(Vector complexes) {
      this.complexes = complexes;
   }

   public void export(File file)
         throws Exception {
      ObjectOutputStream out = new ObjectOutputStream(
            new FileOutputStream(file));
      out.writeObject(chapter);
      out.writeObject(roots);
      out.writeObject(directSpeeches);
      out.writeObject(dialogs);
      out.writeObject(isotopes);
      out.writeObject(complexes);
      out.writeObject(themas);
      out.writeObject(comments);
      out.close();
   }

   public void setKey(DBC_Key key) {
       this.key = key;
   }

}
