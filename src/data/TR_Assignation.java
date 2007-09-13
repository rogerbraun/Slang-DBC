/*
 * Erstellt: 28.05.2006
 */

package data;

import java.io.Serializable;
import java.util.Vector;

/*
 * Traditionelle Bestimmungen. Werden �ber ConstitutiveWords, FunctionWords oder WordListElements mit Wortformen assoziiert.
 * 
 * author: Martin Schaefer
*/
	

public class TR_Assignation
      implements
         Serializable {

   //Intern werden die Bestimmungen jeder Kategorie in einer Variablen gespeichert, deren Wert als bin�re Folge
   //interpretiert wird. Das erlaubt auch die Zuweisung mehrerer Bestimmungen innerhalb einer Kategorie. Auf diese Variablen kann von aussen
   //nicht direkt zugegriffen werden. Daf�r stehen Methoden zur Verf�gung, denen Listen von den unten aufgef�hrten Bestimmungskonstanten �bergeben
   //werden.
	
	
   /**
	 * 
	 */
   private static final long serialVersionUID = 1L;
   
   public static final int TYPE_UNKNOWN                 = 0;	
   public static final int TYPE_CONSTITUTIVE_WORD       = 1;
   public static final int TYPE_FUNCTION_WORD           = 2;
   public static final int TYPE_META_WORD           = 3;
	   
   private byte type;
   private String[] typeOptions = {"Unknown","Constitutive Word","Function Word"};
   
   
   
   //Bestimmungen f�r CW
	   
   public static final int GENUS_UNKNOWN                = 0;
   public static final int GENUS_MASCULIN               = 1;
   public static final int GENUS_FEMININ                = 2;
   public static final int GENUS_NEUTRUM                = 3;
   
   private byte genus;
   private String[] genusOptions = {"Unknown","Masculin","Feminin","Neutrum"};

   
   public static final int NUMERUS_UNKNOWN              = 0;
   public static final int NUMERUS_SINGULAR             = 1;
   public static final int NUMERUS_PLURAL               = 2;
   public static final int NUMERUS_DUAL                 = 3;
   
   private byte numerus;
   private String[] numerusOptions = {"Unknown","Singular","Plural","Dual"};
   
   
   public static final int DETERMINATION_UNKNOWN        = 0;
   public static final int DETERMINATION_DETERMINATED   = 1;
   public static final int DETERMINATION_INDETERMINATED = 2;
   
   private byte determination;
   private String[] determinationOptions = {"Unknown","Determinated","Indeterminated"};
   
   
   public static final int  CASE_UNKNOWN                	= 0;
   public static final int  CASE_NOMINATIV              	= 1;
   public static final int  CASE_GENITIV               	= 2;
   public static final int  CASE_DATIV                   	= 3;
   public static final int  CASE_AKKUSATIV              	= 4;
   public static final int  CASE_ABLATIV                 	= 5;
   public static final int  CASE_ABESSIV                 	= 6;
   public static final int  CASE_ABSOLUTIV              	= 7;
   public static final int  CASE_ADESSIV                 	= 8;
   public static final int  CASE_ALLATIV                 	= 9;
   public static final int  CASE_DELIMATIV               = 10;
   public static final int  CASE_ELATIV                  	= 11;
   public static final int  CASE_ERGATIV                 	= 12;
   public static final int  CASE_ESSIV                   	= 13;
   public static final int  CASE_ILLATIV                 	= 14;
   public static final int  CASE_INESSIV                 	= 15;
   public static final int  CASE_INSTRUKTIV             	= 16;
   public static final int  CASE_INSTRUMENTAL      		= 17;
   public static final int  CASE_KOMITATIV               	= 18;
   public static final int  CASE_LOKATIV                 	= 19;
   public static final int  CASE_OBLIQUUS                	= 20;
   public static final int  CASE_PARTITIV                	= 21;
   public static final int  CASE_PERLATIV                	= 22;
   public static final int  CASE_POSSESSIV               = 23;
   public static final int  CASE_POSTPOSITIONAL    		= 24;
   public static final int  CASE_PRAEPOSITIV           	= 25;
   public static final int  CASE_PROLATIV                	= 26;
   public static final int  CASE_RESPEKTIV              	= 27;
   public static final int  CASE_SUBLATIV                	= 28;
   public static final int  CASE_SUPERESSIV           	= 29;
   public static final int  CASE_TENDENZIAL            	= 30;
   public static final int  CASE_TERMINATIV             	= 31;
   public static final int  CASE_TRANSLATIV             	= 32;
   
   private long case_;
   private String[] caseOptions = {"Unknown","Nominativ","Genitiv","Dativ","Akkusativ","Ablativ","Abessiv","Absolutiv","Adessiv","Allativ","Delimativ","Elativ","Ergativ",
		   "Essiv","Illativ","Inessiv","Instruktiv","Instrumental","Komitativ","Lokativ","Obliquus","Partitiv","Perlativ","Possessiv","Postpostional","Praepositiv",
		   "Prolativ","Respektiv","Sublativ","Superessiv","Tendenzial","Terminativ","Translativ"};
   
   
   public static final int PERSON_UNKNOWN               = 0;
   public static final int PERSON_1                     = 1;
   public static final int PERSON_2                     = 2;
   public static final int PERSON_3                     = 3;
   
   private byte person;
   private String[] personOptions = {"Person 1","Person 2","Person 3"};
   
   
   public static final int CONJUGATION_UNKNOWN          = 0;
   public static final int CONJUGATION_COMPARATIVE      = 1;
   public static final int CONJUGATION_CONSECUTIVE      = 2;
   public static final int CONJUGATION_CAUSAL           = 3;
   public static final int CONJUGATION_CONZESSIVE       = 4;
   public static final int CONJUGATION_CONDITIONAL      = 5;
   public static final int CONJUGATION_FINAL            = 6;
   public static final int CONJUGATION_KNOWLEDGE        = 7;
   public static final int CONJUGATION_TEMPORAL         = 8;
   public static final int CONJUGATION_LOCATIVE         = 9;
   public static final int CONJUGATION_OTHER            = 10;
   
   private short conjugation;
   private String[] conjugationOptions = {"Unknown","Comparative","Consecutive","Causal","Conzessive","Conditional","Final","Knowledge","Temporal","Locative","Other"};
   
   
   public static final int TEMPUS_UNKNOWN               = 0;
   public static final int TEMPUS_PAST                  = 1;
   public static final int TEMPUS_PRESENCE              = 2;
   public static final int TEMPUS_FUTURE                = 3;

   private byte tempus;
   private String[] tempusOptions = {"Unknown","Past","Presence","Future"};
   
   
   public static final int DIATHESE_UNKNWON             = 0;
   public static final int DIATHESE_ACTIVE              = 1;
   public static final int DIATHESE_PASSIVE             = 2;
   
   private byte diathese;
   private String[] diatheseOptions = {"Unknown","Active","Passive"};
   
   
   public static final int WORDCLASS_UNKNOWN            = 0;
   public static final int WORDCLASS_CONNECTOR          = 1;
   public static final int WORDCLASS_VERB               = 2;
   public static final int WORDCLASS_NOUN               = 3;
   public static final int WORDCLASS_ADJECTIVE          = 4;
   public static final int WORDCLASS_ARTICLE            = 5;
   public static final int WORDCLASS_PREPOSITION        = 6;
   public static final int WORDCLASS_PARTICLE           = 7;
   public static final int WORDCLASS_ADVERB             = 8;
   public static final int WORDCLASS_CONJUNCTION        = 9;
   public static final int WORDCLASS_INTERJECTION       = 10;
   public static final int WORDCLASS_PRONOUN            = 11;
   public static final int WORDCLASS_NEGATION           = 12;
   public static final int WORDCLASS_SIGN 			   = 13;
   
   private int wordclass;
   private String[] wordclassOptions = {"Unknown","Connector","Verb","Noun","Adjective","Article","Preposition","Particle","Adverb","Conjunction","Interjection",
		   "Pronoun","Negation","Sign"};
   
   
   public static final int WORDSUBCLASS_CONNECTOR_UNKNOWN             = 0;
   public static final int WORDSUBCLASS_CONNECTOR_KOPULATIV           = 1;
   public static final int WORDSUBCLASS_CONNECTOR_DISJUNKTIV          = 2;
   public static final int WORDSUBCLASS_CONNECTOR_ADVERSATIV          = 3;
   public static final int WORDSUBCLASS_CONNECTOR_NEKTIV              = 4;
   
   private byte wordsubclassConnector;
   private String[] wordsubclassConnectorOptions = {"Unknown","Kopulativ","Disjunktiv","Adversativ","Nektiv"};
   
   
   public static final int WORDSUBCLASS_VERB_UNKNOWN        		= 0;
   public static final int WORDSUBCLASS_VERB_STRONG        		= 1;
   public static final int WORDSUBCLASS_VERB_WEAK              	= 2;
   public static final int WORDSUBCLASS_VERB_AUXILIARY         	= 3;
   
   private byte wordsubclassVerb;
   private String[] wordsubclassVerbOptions = {"Unknown","Strong","Weak","Auxiliary"};
   
   
   public static final int WORDSUBCLASS_ADJECTIVE_UNKNOWN             = 0;
   public static final int WORDSUBCLASS_ADJECTIVE_ADVERSATIV          = 1;
   public static final int WORDSUBCLASS_ADJECTIVE_NEKTIV              = 2;
   
   private byte wordsubclassAdjective;
   private String[] wordsubclassAdjectiveOptions = {"Unknonw","Adversativ","Nektiv"};
   
   
   public static final int WORDSUBCLASS_PREPOSITION_UNKNOWN        = 0;
   public static final int WORDSUBCLASS_PREPOSITION_TEMPORAL       = 1;
   public static final int WORDSUBCLASS_PREPOSITION_LOCATIVE       = 2;
   
   private byte wordsubclassPreposition;
   private String[] wordsubclassPrepositionOptions = {"Unknown","Temporal","Locative"};
   
   
   public static final int WORDSUBCLASS_PRONOUN_UNKNOWN              = 0;
   public static final int WORDSUBCLASS_PRONOUN_PERSONAL             = 1;
   public static final int WORDSUBCLASS_PRONOUN_POSSESSIVE           = 2;
   public static final int WORDSUBCLASS_PRONOUN_DEMONSTRATIVE        = 3;
   public static final int WORDSUBCLASS_PRONOUN_RELATIVE             = 4;
   public static final int WORDSUBCLASS_PRONOUN_RELATIVE2            = 5; //f�r pronouns, die auch ARTICLE sein k�nnen, fraglich, 
   																			//ob notwendig, wenn eine wortform alternative Bestimmungen erhalten kann
   public static final int WORDSUBCLASS_PRONOUN_DEICTICON            = 6;
   
   private short wordsubclassPronoun;
   private String[] wordsubclassPronounOptions = {"Unknown","Personal","Possessive","Demonstrative","Relative","Relative2","Deicticon"};
   
   
   public static final int WORDSUBCLASS_SIGN_UNKNOWN              	 	= 0;
   public static final int WORDSUBCLASS_SIGN_SEPARATOR              		= 1;
   public static final int WORDSUBCLASS_SIGN_SEPARATOR_STRONG       		= 2;
   public static final int WORDSUBCLASS_SIGN_SEPARATOR_QUOTATION       	= 3;
   public static final int WORDSUBCLASS_SIGN_COMMA        				= 4;
   public static final int WORDSUBCLASS_SIGN_DASH             			= 5;
   public static final int WORDSUBCLASS_SIGN_WHITESPACE             		= 6;
   
   private short wordsubclassSign;
   private String[] wordsubclassSignOptions = {"Unknown","Separator","Separator strong","Separator quotation","Comma","Dash","Whitespace"};
   
   
   // Bestimmungen f�r FW
   
   public static final int WORTART1_UNKNOWN              	= 0;
   public static final int WORTART1_PRAEPOSITION           	= 1;
   public static final int WORTART1_KONJUNKTION            	= 2;
   public static final int WORTART1_ARTIKEL				  	= 3;
   public static final int WORTART1_INTERJEKTION          	= 4;
   public static final int WORTART1_NEGATIONSWORT         	= 5;
   public static final int WORTART1_HILFSVERB				= 6;
   public static final int WORTART1_PARTIKEL				= 7;
   public static final int WORTART1_CONNECTOR 				= 8;
   
   private short wortart1;
   private String[] wortart1Options = {"Unknown","Praeposition","Konjunktion","Artikel","Interjektion","Negationswort","Hilfsverb","Partikel","Connector"};
   
   public static final int WORTART2_UNKNOWN              	= 0;
   public static final int WORTART2_CONCESSIVE           	= 1;
   public static final int WORTART2_CAUSAL                 	= 2;
   public static final int WORTART2_FINAL				  	= 3;
   public static final int WORTART2_CONSECUTIVE             = 4;
   public static final int WORTART2_COMPARATIVE             = 5;
   public static final int WORTART2_CONDITIONAL				= 6;
   public static final int WORTART2_INDIRECT_KNOWLEDGE		= 7;
   public static final int WORTART2_TEMPORAL 				= 8;
   public static final int WORTART2_LOCATIVE				= 9;
   public static final int WORTART2_OTHERS 			  		= 10;
   
   private short wortart2;
   private String[] wortart2Options = {"Unknown","Concessive","Causal","Final","Consecutive","Comparative","Conditional","Indirect knowledge","Temporal",
		   "Locative","Others"};
   
   public static final int WORTART3_UNKNOWN             = 0;
   public static final int WORTART3_KOPULATIV           = 1;
   public static final int WORTART3_DISJUNKTIV          = 2;
   public static final int WORTART3_ADVERSATIV		   = 3;
   public static final int WORTART3_NEKTIV              = 4;
   
   private byte wortart3;
   private String[] wortart3Options = {"Unknown","Kopulativ","Disjunktiv","Adversativ","Nektiv"};
   
   
   public static final int WORTART4_UNKNOWN              = 0;
   public static final int WORTART4_EXPLICATIVE          = 1;
   public static final int WORTART4_EMPHATIC             = 2;
   
   private byte wortart4;
   private String[] wortart4Options = {"Unknown","Explicative","Emphatic"};
   
   
   private String etymol;
   private String description;
   private String content; //muss nicht (von Anfang an) gesetzt sein
   private int category; //todo
   
   public int DB_ID = -1;
   
   public TR_Assignation(){};
   	public TR_Assignation(byte type, byte genus, byte numerus, byte determination, long case_, byte person, short conjugation, byte tempus, 
   			byte diathese, int wordclass, byte wordsubclassConnector, byte wordsubclassVerb, byte wordsubclassAdjective, 
   			short wordsubclassPronoun, short wordsubclassSign, short wortart1, short wortart2, byte wortart3, byte wortart4, String etymol, String description) {
   		this.type = type;
   		this.genus = genus;
   		this.numerus = numerus;
   		this.determination = determination;
   		this.case_ = case_;
   		this.person = person;
   		this.conjugation = conjugation;
   		this.tempus = tempus;
   		this.diathese = diathese;
   		this.wordclass = wordclass;
   		this.wordsubclassConnector = wordsubclassConnector;
   		this.wordsubclassVerb = wordsubclassVerb;
   		this.wordsubclassAdjective = wordsubclassAdjective;
   		this.wordsubclassPronoun = wordsubclassPronoun;
   		this.wordsubclassSign = wordsubclassSign;
   		this.wortart1 = wortart1;
   		this.wortart2 = wortart2;
   		this.wortart3 = wortart3;
   		this.wortart4 = wortart4;
   		this.etymol = etymol;
   		this.description = description;
   		content = null;
   	}
   	public TR_Assignation(byte type, byte genus, byte numerus, byte determination, long case_, byte person, short conjugation, byte tempus, 
   			byte diathese, int wordclass, byte wordsubclassConnector, byte wordsubclassVerb, byte wordsubclassAdjective, 
   			short wordsubclassPronoun, short wordsubclassSign, short wortart1, short wortart2, byte wortart3, byte wortart4, String etymol, String description, String content) {
   		this.type = type;
   		this.genus = genus;
   		this.numerus = numerus;
   		this.determination = determination;
   		this.case_ = case_;
   		this.person = person;
   		this.conjugation = conjugation;
   		this.tempus = tempus;
   		this.diathese = diathese;
   		this.wordclass = wordclass;
   		this.wordsubclassConnector = wordsubclassConnector;
   		this.wordsubclassVerb = wordsubclassVerb;
   		this.wordsubclassAdjective = wordsubclassAdjective;
   		this.wordsubclassPronoun = wordsubclassPronoun;
   		this.wordsubclassSign = wordsubclassSign;
   		this.wortart1 = wortart1;
   		this.wortart2 = wortart2;
   		this.wortart3 = wortart3;
   		this.wortart4 = wortart4;
   		this.etymol = etymol;
   		this.description = description;
   		this.content = content;
   	}

   	public Vector getCases() {
		return getContainerContent(case_);
	}
	
	public long getCasesBinary() {
		return case_;
	}
	
	public void addCases(Vector in) {
		this.case_ = addNewAssignations(case_,in);
	}
	
	public void removeCases(Vector in){
		removeAssignations(case_,in);
	}
	
	public String[] getCaseOptions(){
		return caseOptions;
	}
	
	public Vector getConjugations() {
		return getContainerContent(conjugation);
	}
	
	public long getConjugationsBinary() {
		return conjugation;
	}
	
	public void addConjugations(Vector in) {
		this.conjugation = (short) addNewAssignations(conjugation,in);
	}
	
	public void removeConjugations(Vector in){
		removeAssignations(conjugation,in);
	}
	
	public String[] getConjugationOptions(){
		return conjugationOptions;
	}
	
	public Vector getDeterminations() {
		return getContainerContent(determination);
	}
	
	public long getDeterminationBinary() {
		return determination;
	}
	
	public void addDeterminations(Vector in) {
		this.determination = (byte) addNewAssignations(determination,in);
	}
	
	public void removeDeterminations(Vector in){
		removeAssignations(determination,in);
	}
	
	public String[] getDeterminationOptions(){
		return determinationOptions;
	}
	
	public Vector getDiatheses() {
		return getContainerContent(diathese);
	}
	
	public long getDiathesesBinary() {
		return diathese;
	}
	
	public void addDiatheses(Vector in) {
		this.diathese = (byte) addNewAssignations(diathese,in);
	}
	
	public void removeDiatheses(Vector in){
		removeAssignations(diathese,in);
	}
	
	public String[] getDiathesesOptions(){
		return diatheseOptions;
	}
	
	public Vector getGenus() {
		return getContainerContent(genus);
	}
	
	public long getGenusBinary() {
		return genus;
	}
	
	public void addGenus(Vector in) {
		this.genus = (byte) addNewAssignations(genus,in);
	}
	
	public void removeGenus(Vector in){
		removeAssignations(genus,in);
	}
	
	public String[] getGenusOptions(){
		return genusOptions;
	}
	
	public Vector getNumerus() {
		return getContainerContent(numerus);
	}
	
	public long getNumerusBinary() {
		return numerus;
	}
	
	public void addNumerus(Vector in) {
		this.numerus = (byte) addNewAssignations(numerus,in);
	}
	
	public void removeNumerus(Vector in){
		removeAssignations(numerus,in);
	}
	
	public String[] getNumerusOptions(){
		return numerusOptions;
	}
	
	public Vector getPersons() {
		return getContainerContent(person);
	}
	
	public long getPersonsBinary() {
		return person;
	}
	
	public void addPersons(Vector in) {
		this.person = (byte) addNewAssignations(person,in);
	}
	
	public void removePersons(Vector in){
		removeAssignations(person,in);
	}
	
	public String[] getPersonsOptions(){
		return personOptions;
	}
	
	public Vector getTempus() {
		return getContainerContent(tempus);
	}
	
	public long getTempusBinary() {
		return tempus;
	}
	
	public void addTempus(Vector in) {
		this.tempus = (byte) addNewAssignations(tempus,in);
	}
	
	public void removeTempus(Vector in){
		removeAssignations(tempus,in);
	}
	
	public String[] getTempusOptions(){
		return tempusOptions;
	}
	
	public Vector getTypes() {
		return getContainerContent(type);
	}
	
	public long getTypesBinary() {
		return type;
	}
	
	public void addTypes(Vector in) {
		this.type = (byte) addNewAssignations(type,in);
	}
	
	public void removeTypes(Vector in){
		removeAssignations(type,in);
	}
	
	public String[] getTypeOptions(){
		return typeOptions;
	}
	
	public Vector getWordclasses() {
		return getContainerContent(wordclass);
	}
	
	public long getWordclassesBinary() {
		return wordclass;
	}
	
	public void addWordclasses(Vector in) {
		this.wordclass = (int) addNewAssignations(wordclass,in);
	}
	
	public void removeWordclasses(Vector in){
		removeAssignations(wordclass,in);
	}
	
	public String[] getWordclassOptions(){
		return wordclassOptions;
	}
	
	public Vector getWordsubclassAdjectives() {
		return getContainerContent(wordsubclassAdjective);
	}
	
	public long getWordsubclassAdjectivesBinary() {
		return wordsubclassAdjective;
	}
	
	public void addWordsubclassAdjectives(Vector in) {
		this.wordsubclassAdjective = (byte) addNewAssignations(wordsubclassAdjective,in);
	}
	
	public void removeWordsubclassAdjectives(Vector in){
		removeAssignations(wordsubclassAdjective,in);
	}
	
	public String[] getWordsubclassAdjectiveOptions(){
		return wordsubclassAdjectiveOptions;
	}
	
	public Vector getWordsubclassConnectors() {
		return getContainerContent(wordsubclassConnector);
	}
	
	public long getWordsubclassConnectorsBinary() {
		return wordsubclassConnector;
	}
	
	public void addWordsubclassConnectors(Vector in) {
		this.wordsubclassConnector = (byte) addNewAssignations(wordsubclassConnector,in);
	}
	
	public void removeWordsubclassConnectors(Vector in){
		removeAssignations(wordsubclassConnector,in);
	}
	
	public String[] getWordsubclassConnectorOptions(){
		return wordsubclassConnectorOptions;
	}
	
	public Vector getWordsubclassPrepositions() {
		return getContainerContent(wordsubclassPreposition);
	}
	
	public long getWordsubclassPrepositionsBinary() {
		return wordsubclassPreposition;
	}
	
	public void addWordsubclassPrepositions(Vector in) {
		this.wordsubclassPreposition = (byte) addNewAssignations(wordsubclassPreposition,in);
	}
	
	public void removeWordsubclassPrepositions(Vector in){
		removeAssignations(wordsubclassPreposition,in);
	}
	
	public String[] getWordsubclassPrepositionOptions(){
		return wordsubclassPrepositionOptions;
	}
	
	public Vector getWordsubclassPronouns() {
		return getContainerContent(wordsubclassPronoun);
	}
	
	public long getWordsubclassPronounsBinary() {
		return wordsubclassPronoun;
	}
	
	public void addWordsubclassPronouns(Vector in) {
		this.wordsubclassPronoun = (short) addNewAssignations(wordsubclassPronoun,in);
	}
	
	public void removeWordsubclassPronouns(Vector in){
		removeAssignations(wordsubclassPronoun,in);
	}
	
	public String[] getWordsubclassPronounOptions(){
		return wordsubclassPronounOptions;
	}
	
   	public Vector getWordsubclassSigns() {
		return getContainerContent(wordsubclassSign);
	}
	
	public long getWordsubclassSignsBinary() {
		return wordsubclassSign;
	}
	
	public void addWordsubclassSigns(Vector in) {
		this.wordsubclassSign = (short) addNewAssignations(wordsubclassSign,in);
	}
	
	public void removeWordsubclassSigns(Vector in){
		removeAssignations(wordsubclassSign,in);
	}
	
	public String[] getWordsubclassSignOptions(){
		return wordsubclassSignOptions;
	}
	
   	public Vector getWordsubclassVerbs() {
		return getContainerContent(wordsubclassVerb);
	}
	
	public long getWordsubclassVerbsBinary() {
		return wordsubclassVerb;
	}
	
	public void addWordsubclassVerbs(Vector in) {
		this.wordsubclassVerb = (byte) addNewAssignations(wordsubclassVerb,in);
	}
	
	public void removeWordsubclassVerbs(Vector in){
		removeAssignations(wordsubclassVerb,in);
	}
	
	public String[] getWordsubclassVerbOptions(){
		return wordsubclassVerbOptions;
	}
	public Vector getWortarten1() {
		return getContainerContent(wortart1);
	}
	
	public long getWortarten1Binary() {
		return wortart1;
	}
	
	public void addWortarten1(Vector in) {
		this.wortart1 = (short) addNewAssignations(wortart1,in);
	}
	
	public void removeWortarten1(Vector in){
		removeAssignations(wortart1,in);
	}
	
	public String[] getWortart1Options(){
		return wortart1Options;
	}
	
	public Vector getWortarten2() {
		return getContainerContent(wortart2);
	}
	
	public long getWortarten2Binary() {
		return wortart2;
	}
	
	public void addWortarten2(Vector in) {
		this.wortart2 = (short) addNewAssignations(wortart2,in);
	}
	
	public void removeWortarten2(Vector in){
		removeAssignations(wortart2,in);
	}
	
	public String[] getWortart2Options(){
		return wortart2Options;
	}
	
	public Vector getWortarten3() {
		return getContainerContent(wortart3);
	}
	
	public long getWortarten3Binary() {
		return wortart3;
	}
	
	public void addWortarten3(Vector in) {
		this.wortart3 = (byte) addNewAssignations(wortart3,in);
	}
	
	public void removeWortarten3(Vector in){
		removeAssignations(wortart3,in);
	}
	
	public String[] getWortart3Options(){
		return wortart3Options;
	}
	
	public Vector getWortarten4() {
		return getContainerContent(wortart4);
	}
	
	public long getWortarten4Binary() {
		return wortart4;
	}
	
	public void addWortarten4(Vector in) {
		this.wortart4 = (byte) addNewAssignations(wortart4,in);
	}
	
	public void removeWortarten4(Vector in){
		removeAssignations(wortart4,in);
	}
	
	public String[] getWortart4Options(){
		return wortart4Options;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEtymol() {
		return etymol;
	}

	public void setEtymol(String etymol) {
		this.etymol = etymol;
	}
	
	//folgende Getter sind nur f�r die Datenbank gedacht
	public void setCasesBinary(long case_) {
		this.case_ = case_;
	}

	public void setConjugationsBinary(short conjugation) {
		this.conjugation = conjugation;
	}

	public void setDeterminationsBinary(byte determination) {
		this.determination = determination;
	}

	public void setDiathesesBinary(byte diathese) {
		this.diathese = diathese;
	}

	public void setGenussBinary(byte genus) {
		this.genus = genus;
	}

	public void setNumerussBinary(byte numerus) {
		this.numerus = numerus;
	}

	public void setPersonsBinary(byte person) {
		this.person = person;
	}

	public void setTempussBinary(byte tempus) {
		this.tempus = tempus;
	}

	public void setTypesBinary(byte type) {
		this.type = type;
	}

	public void setWordclasssBinary(int wordclass) {
		this.wordclass = wordclass;
	}

	public void setWordsubclassAdjectivesBinary(byte wordsubclassAdjective) {
		this.wordsubclassAdjective = wordsubclassAdjective;
	}

	public void setWordsubclassConnectorsBinary(byte wordsubclassConnector) {
		this.wordsubclassConnector = wordsubclassConnector;
	}

	public void setWordsubclassPrepositionsBinary(byte wordsubclassPreposition) {
		this.wordsubclassPreposition = wordsubclassPreposition;
	}

	public void setWordsubclassPronounsBinary(short wordsubclassPronoun) {
		this.wordsubclassPronoun = wordsubclassPronoun;
	}

	public void setWordsubclassSignsBinary(short wordsubclassSign) {
		this.wordsubclassSign = wordsubclassSign;
	}

	public void setWordsubclassVerbsBinary(byte wordsubclassVerb) {
		this.wordsubclassVerb = wordsubclassVerb;
	}

	public void setWortart1Binary(short wortart1) {
		this.wortart1 = wortart1;
	}

	public void setWortart2Binary(short wortart2) {
		this.wortart2 = wortart2;
	}

	public void setWortart3Binary(byte wortart3) {
		this.wortart3 = wortart3;
	}

	public void setWortart4Binary(byte wortart4) {
		this.wortart4 = wortart4;
	}

	private static long addNewAssignations(long oldValue, Vector newValues){
		for(int i = 0; i != newValues.size();i++){
			int newValue = (Integer)newValues.get(i);
			oldValue |= ((long)2 << ((long)newValue - (long)1));
		}
		return oldValue;
	}
	
	private static long removeAssignations(long oldValue, Vector removeValues){
		for(int i = 0; i != removeValues.size();i++){
			int newValue = (Integer)removeValues.get(i);
			if(getContainerContent(oldValue).contains(newValue)){
				oldValue ^= ((long)1 << ((long)newValue)); //- (long)1));
			}
		}
		return oldValue;
	}
	
	private static Vector getContainerContent(long container){
		Vector out = new Vector();
		int counter = 0; //=1	
		for(;;){
				if(container%2==1){
					out.add(counter);
				}
				if(container == 0){
					break;
				}
				container = container >> 1;
				counter++;
			}
		return out;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

}
