package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

/*
 * Erstellt: 28.05.2006
 */


import java.io.Serializable;
import java.util.BitSet;


import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Chapter;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DB_Element;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


/*
 * Traditionelle Bestimmungen. Werden über ConstitutiveWords, FunctionWords oder WordListElements mit Wortformen assoziiert.
 * 
 * author: Martin Schaefer
 */


public class TR_Assignation extends DB_Element
implements
Serializable {

    //Intern werden die Bestimmungen jeder Kategorie in einer Variablen gespeichert, deren Wert als binäre Folge
    //interpretiert wird. Das erlaubt auch die Zuweisung mehrerer Bestimmungen innerhalb einer Kategorie. Auf diese Variablen kann von aussen
    //nicht direkt zugegriffen werden. Dafür stehen Methoden zur Verfügung, denen Listen von den unten aufgeführten Bestimmungskonstanten übergeben
    //werden.


    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static enum Type {
	CONSTITUTIVE_WORD ("Constitutive Word"),
	FUNCTION_WORD ("Function Word"),
	META_WORD ("Meta Word");
	private String name;
	public String toString() {
	    return this.name;
	}
	private Type(String s) {
	    this.name = s;
	}
    };


    public static enum Genus {
	MASCULIN ("Masculin"),
	FEMININ ("Feminin"),
	NEUTRUM ("Neutrum");
	private String name;
	public String toString() {
	    return this.name;
	}
	private Genus(String s) {
	    this.name = s;
	}
    };


    public static enum Numerus {
	SINGULAR ("Singular"),
	PLURAL ("Plural"),
	DUAL ("Dual");
	private String name;
	public String toString() {
	    return this.name;
	}
	private Numerus(String s) {
	    this.name = s;
	}
    };


    public static enum Determination {
	DETERMINATED  ("Determinated"),
	INDETERMINATED ("Indeterminated");
	private String name;
	public String toString() {
	    return this.name;
	}
	private Determination(String s) {
	    this.name = s;
	}
    };


    public static enum Case {
	// The most common cases should be listed first
	// in order to set their bits in the first byte of the corresponding byte[],
	// so that often only one byte has to be transfered

	// most common 
	NOMINATIV ("Nominativ"),
	GENITIV ("Genitiv"),	   
	DATIV ("Dativ"),
	AKKUSATIV ("Akkusativ"),

	// lesser common
	ABLATIV ("Ablativ"),
	ABESSIV ("Abessiv"),
	ABSOLUTIV ("Absolutiv"),
	ADESSIV ("Adessiv"),		
	ALLATIV ("Allativ"),
	DELIMATIV ("Delimativ"),
	ELATIV ("Elativ"),
	ERGATIV ("Ergativ"),
	ESSIV ("Essiv"),		
	ILLATIV ("Illativ"),
	INESSIV ("Inessiv"),
	INSTRUKTIV ("Instruktiv"),
	INSTRUMENTAL ("Instrumental"),
	KOMITATIV ("Komitativ"),
	LOKATIV ("Lokativ"),		
	OBLIQUUS ("Obliquus"),
	PARTITIV ("Partitiv"),
	PERLATIV ("Perlativ"),
	POSSESSIV ("Possessiv"),
	POSTPOSITIONAL ("Postpositional"),
	PRAEPOSITIV ("Praepositiv"),
	PROLATIV ("Prolativ"),
	RESPEKTIV ("Respektiv"),
	SUBLATIV ("Sublativ"),
	SUPERESSIV ("Superessiv"),
	TENDENZIAL ("Tendenzial"),
	TERMINATIV ("Terminativ"),
	TRANSLATIV ("Translativ");

	private String name;

	private Case(String name) {
	    this.name = name;
	}
	public String toString() {
	    return this.name;
	}
    }


    public static enum Person {
	FIRST ("Erste Person"),
	SECOND ("Zweite Person"),
	THIRD ("Dritte Person");
	private String name;
	public String toString() {
	    return this.name;
	}
	private Person(String s) {
	    this.name = s;
	}
    };


    public static enum Conjugation {
	COMPARATIVE ("Comparative"),
	CONSECUTIVE ("Consecutive"),
	CAUSAL ("Causal"),
	CONZESSIVE ("Conzessive"),
	CONDITIONAL ("Conditional"),
	FINAL ("Final"),
	KNOWLEDGE ("Knowledge"),
	TEMPORAL ("Temporal"),
	LOCATIVE ("Locative"),
	OTHER ("Other");
	private String name;
	public String toString() {
	    return this.name;
	}
	private Conjugation(String s) {
	    this.name = s;
	}
    };


    public static enum Tempus {
	PAST ("Past"),
	PRESENT ("Presence"),
	FUTURE ("Future");
	private String name;
	public String toString() {
	    return this.name;
	}
	private Tempus(String s) {
	    this.name = s;
	}
    };


    public static enum Diathese {
	ACTIVE ("Active"),
	PASSIVE ("Passive");
	private String name;
	public String toString() {
	    return this.name;
	}
	private Diathese(String s) {
	    this.name = s;
	}
    };



    public static enum Wordclass {
	CONNECTOR ("Connector"),	   
	VERB ("Verb"),
	NOUN ("Nomen"),
	ADJECTIVE ("Adjektiv"),
	ARTICLE ("Artikel"),
	PREPOSITION ("Präposition"),
	PARTICLE ("Partikel"),
	ADVERB ("Adverb"),
	CONJUNCTION ("Konjuktion"),
	INTERJECTION ("Interjektion"),
	PRONOUN ("Pronomen"),
	NEGATION ("Negation"),
	SIGN ("Satzzeichen");
	private String name;
	public String toString() {
	    return this.name;
	}
	private Wordclass(String s) {
	    this.name = s;
	}
    };




    public static enum WordsubclassConnector {
	KOPULATIV ("Kopulativ"),
	DISJUNKTIV ("Disjunktiv"),
	DVERSATIV ("Adversativ"),
	NEKTIV ("Nektiv");
	private String name;
	protected static final Wordclass parent = Wordclass.CONNECTOR;
	public String toString() {
	    return this.name;
	}
	private WordsubclassConnector(String s) {
	    this.name = s;
	}
    }


    public static enum WordsubclassVerb {
	STRONG ("Starkes Verb"),
	WEAK ("Weiches Verb"),
	AUXILIARY ("Auxiliarverb");
	private String name;
	protected static final Wordclass parent = Wordclass.VERB;
	public String toString() {
	    return this.name;
	}
	private WordsubclassVerb(String s) {
	    this.name = s;
	}
    }


    public static enum WordsubclassAdjective {
	ADVERSATIV ("Adversativ"),
	NEKTIV ("Nektiv");
	private String name;
	protected static final Wordclass parent = Wordclass.ADJECTIVE;
	public String toString() {
	    return this.name;
	}
	private WordsubclassAdjective(String s) {
	    this.name = s;
	}
    }


    public static enum WordsubclassPreposition {
	TEMPORAL ("Temporal"),
	LOCATIVE ("Locative");
	private String name;
	protected static final Wordclass parent = Wordclass.PREPOSITION;
	public String toString() {
	    return this.name;
	}
	private WordsubclassPreposition(String s) {
	    this.name = s;
	}
    }

    public static enum WordsubclassPronoun {
	PERSONAL ("Personal"),
	POSSESSIVE ("Possessive"),
	DEMONSTRATIVE ("Demonstrative"),
	RELATIVE ("Relative"),
	RELATIVE2 ("Relative2"),
	DEICTICON ("Deicticon");
	private String name;
	protected static final Wordclass parent = Wordclass.PRONOUN;
	public String toString() {
	    return this.name;
	}
	private WordsubclassPronoun(String s) {
	    this.name = s;
	}
    }

    public static enum WordsubclassSign {
	SEPARATOR ("Separator"),
	SEPARATOR_STRONG ("Separator strong"),
	SEPARATOR_QUOTATION ("Separator quotation"),
	COMMA ("Comma"),
	DASH ("Dash"),
	WHITESPACE ("Whitespace");
	private String name;
	protected static final Wordclass parent = Wordclass.SIGN;
	public String toString() {
	    return this.name;
	}
	private WordsubclassSign(String s) {
	    this.name = s;
	}
    }

    // Bestimmungen für FW

    public static enum Wortart1 {
	PRAEPOSITION ("Praeposition"),
	KONJUNKTION ("Konjunktion"),
	ARTIKEL ("Artikel"),
	INTERJEKTION ("Interjektion"),
	NEGATIONSWORT ("Negationswort"),
	HILFSVERB ("Hilfsverb"),
	PARTIKEL ("Partikel"),
	CONNECTOR ("Connector");
	private String name;
	public String toString() {
	    return this.name;
	}
	private Wortart1(String s) {
	    this.name = s;
	}
    }

    public static enum Wortart2 {
	CONCESSIVE ("Concessive"),
	CAUSAL ("Causal"),
	FINAL ("Final"),
	CONSECUTIVE ("Consecutive"),
	COMPARATIVE ("Comparative"),
	CONDITIONAL ("Conditional"),
	INDIRECT_KNOWLEDGE ("Indirect knowledge"),
	TEMPORAL ("Temporal"),
	LOCATIVE ("Locative"),
	OTHERS ("Others");
	private String name;
	public String toString() {
	    return this.name;
	}
	private Wortart2(String s) {
	    this.name = s;
	}
    }


    public static enum Wortart3 {
	KOPULATIV ("Kopulativ"),
	DISJUNKTIV ("Disjunktiv"),
	ADVERSATIV ("Adversativ"),
	NEKTIV ("Nektiv");
	private String name;
	public String toString() {
	    return this.name;
	}
	private Wortart3(String s) {
	    this.name = s;
	}
    }

    public static enum Wortart4 {
	EXPLICATIVE ("Explicative"),
	EMPHATIC ("Emphatic");
	private String name;
	public String toString() {
	    return this.name;
	}
	private Wortart4(String s) {
	    this.name = s;
	}
    }

    // short variable names => small objects

    /**
     * cases
     */
    protected byte[] ca;
    /**
     * Conjugation
     */
    protected byte[] co;
    /**
     * Determination
     */
    protected byte[] de;
    /**
     * Diathese
     */
    protected byte[] di;
    /**
     * Genus
     */
    protected byte[] g;
    /**
     * Numerus
     */
    protected byte[] n;
    /**
     * Person
     */
    protected byte[] p;
    /**
     * Tempus
     */
    protected byte[] te;
    /**
     * Type
     */
    protected byte[] ty;
    /**
     * Wordclass
     */
    protected byte[] wc;

    // Word-Subclasses
    /**
     * 
     */
    protected byte[] wsa;
    protected byte[] wsc;
    protected byte[] wspre;
    protected byte[] wspro;
    protected byte[] wss;
    protected byte[] wsv;

    // Function Word characterisations
    protected byte[] wa1;
    protected byte[] wa2;
    protected byte[] wa3;
    protected byte[] wa4;

    private String y; // Etymol
    private String d; // Description
    private String a; // abbreviation

    public TR_Assignation()
    {
	super(-1);
	y="";
	d="";
	a="";
    };

    // *********** Cases *************  

    /**
     * Clears and sets new cases
     * @param cases
     */
    public void setCases(Case ... cases) {
	this.ca = null;
	if(cases == null)
	    return;
	for (int i = 0; i < cases.length; i++) {
	    this.ca = setBit(this.ca, cases[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a Case
     * @param c The Case
     * @param b Indicates whether to set or remove the Case
     */
    public void setCase(Case c, boolean b) {
	if( c == null || b == false && ! this.hasCase(c))
	    return;
	this.ca = setBit(this.ca, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the Cases
     * @return All Cases that have been set
     */
    public Case[] getCases() {
    Enum<?>[] ea = filterConstants(Case.values(), this.ca);
    if( this.ca == null || ea == null)
	    return new Case[0];
	//Enum<?>[] ea = filterConstants(Case.values(), this.ca);
	Case[] ret = new Case[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if Case is set
     * @param c
     * @return <code>true</code> if Case is enabled, <code>false</code> otherwise
     */
    public boolean hasCase(Case c) {
	if( this.ca == null || c == null)
	    return false;
	return getBit(this.ca, c.ordinal());
    }



    // *********** conjugation *************  

    /**
     * Clears and sets new cases
     * @param conjugations
     */
    public void setConjugations(Conjugation ... conjugations) {
	this.co = null;
	if(conjugations == null)
	    return;
	for (int i = 0; i < conjugations.length; i++) {
	    this.co = setBit(this.co, conjugations[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a Conjugation
     * @param c The Conjugation
     * @param b Indicates whether to set or remove the Conjugation
     */
    public void setConjugation(Conjugation c, boolean b) {
	if( c == null || b == false && ! this.hasConjugation(c))
	    return;
	this.co = setBit(this.co, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the Conjugations
     * @return All Conjugations that have been set
     */
    public Conjugation[] getConjugations() {
  	Enum<?>[] ea = filterConstants(Conjugation.values(), this.co);
    if( this.co == null || ea == null )
	    return new Conjugation[0];
	//Enum<?>[] ea = filterConstants(Conjugation.values(), this.co);
	Conjugation[] ret = new Conjugation[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if Conjugation is set
     * @param c
     * @return <code>true</code> if Conjugation is enabled, <code>false</code> otherwise
     */
    public boolean hasConjugation(Conjugation c) {
	if( this.co == null || c == null)
	    return false;
	return getBit(this.co, c.ordinal());
    }

    // *********** determination *************  

    /**
     * Clears and sets new cases
     * @param determinations
     */
    public void setDeterminations(Determination ... determinations) {
	this.de = null;
	if(determinations == null)
	    return;
	for (int i = 0; i < determinations.length; i++) {
	    this.de = setBit(this.de, determinations[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a Determination
     * @param c The Determination
     * @param b Indicates whether to set or remove the Determination
     */
    public void setDetermination(Determination c, boolean b) {
	if( c == null || b == false && ! this.hasDetermination(c))
	    return;
	this.de = setBit(this.de, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the Determinations
     * @return All Determinations that have been set
     */
    public Determination[] getDeterminations() {
   	Enum<?>[] ea = filterConstants(Determination.values(), this.de);
	if( this.de == null || ea == null )
	    return new Determination[0];
	//Enum<?>[] ea = filterConstants(Determination.values(), this.de);
	Determination[] ret = new Determination[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if Determination is set
     * @param c
     * @return <code>true</code> if Determination is enabled, <code>false</code> otherwise
     */
    public boolean hasDetermination(Determination c) {
	if( this.de == null || c == null)
	    return false;
	return getBit(this.de, c.ordinal());
    }


    // *********** diathese *************  

    /**
     * Clears and sets new cases
     * @param diatheses
     */
    public void setDiatheses(Diathese ... diatheses) {
	this.di = null;
	if(diatheses == null)
	    return;
	for (int i = 0; i < diatheses.length; i++) {
	    this.di = setBit(this.di, diatheses[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a Diathese
     * @param c The Diathese
     * @param b Indicates whether to set or remove the Diathese
     */
    public void setDiathese(Diathese c, boolean b) {
	if( c == null || b == false && ! this.hasDiathese(c))
	    return;
	this.di = setBit(this.di, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the Diatheses
     * @return All Diatheses that have been set
     */
    public Diathese[] getDiatheses() {
   	Enum<?>[] ea = filterConstants(Diathese.values(), this.di);
	if( this.di == null || ea == null )
	    return new Diathese[0];
	//Enum<?>[] ea = filterConstants(Diathese.values(), this.di);
	Diathese[] ret = new Diathese[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if Diathese is set
     * @param c
     * @return <code>true</code> if Diathese is enabled, <code>false</code> otherwise
     */
    public boolean hasDiathese(Diathese c) {
	if( this.di == null || c == null)
	    return false;
	return getBit(this.di, c.ordinal());
    }

    // *********** genus *************  

    /**
     * Clears and sets new cases
     * @param genera
     */
    public void setGenera(Genus ... genera) {
	this.g = null;
	if(genera == null)
	    return;
	for (int i = 0; i < genera.length; i++) {
	    this.g = setBit(this.g, genera[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a Genus
     * @param c The Genus
     * @param b Indicates whether to set or remove the Genus
     */
    public void setGenus(Genus c, boolean b) {
	if( c == null || b == false && ! this.hasGenus(c))
	    return;
	this.g = setBit(this.g, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the Genera
     * @return All Genera that have been set
     */
    public Genus[] getGenera() {
    Enum<?>[] ea = filterConstants(Genus.values(), this.g);
    if( this.g == null || ea == null )
	    return new Genus[0];
	//Enum<?>[] ea = filterConstants(Genus.values(), this.g);
	Genus[] ret = new Genus[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if Genus is set
     * @param c
     * @return <code>true</code> if Genus is enabled, <code>false</code> otherwise
     */
    public boolean hasGenus(Genus c) {
	if( this.g == null || c == null)
	    return false;
	return getBit(this.g, c.ordinal());
    }

    // *********** numerus *************  

    /**
     * Clears and sets new cases
     * @param numeri
     */
    public void setNumeri(Numerus ... numeri) {
	this.n = null;
	if(numeri == null)
	    return;
	for (int i = 0; i < numeri.length; i++) {
	    this.n = setBit(this.n, numeri[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a Numerus
     * @param c The Numerus
     * @param b Indicates whether to set or remove the Numerus
     */
    public void setNumerus(Numerus c, boolean b) {
	if( c == null || b == false && ! this.hasNumerus(c))
	    return;
	this.n = setBit(this.n, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the Numeri
     * @return All Numeri that have been set
     */
    public Numerus[] getNumeri() {
    Enum<?>[] ea = filterConstants(Numerus.values(), this.n);
    if( this.n == null || ea == null )
	    return new Numerus[0];
	//Enum<?>[] ea = filterConstants(Numerus.values(), this.n);
	Numerus[] ret = new Numerus[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if Numerus is set
     * @param c
     * @return <code>true</code> if Numerus is enabled, <code>false</code> otherwise
     */
    public boolean hasNumerus(Numerus c) {
	if( this.n == null || c == null)
	    return false;
	return getBit(this.n, c.ordinal());
    }


    // *********** person *************  

    /**
     * Clears and sets new cases
     * @param persons
     */
    public void setPersons(Person ... persons) {
	this.p = null;
	if(persons == null)
	    return;
	for (int i = 0; i < persons.length; i++) {
	    this.p = setBit(this.p, persons[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a Person
     * @param c The Person
     * @param b Indicates whether to set or remove the Person
     */
    public void setPerson(Person c, boolean b) {
	if( c == null || b == false && ! this.hasPerson(c))
	    return;
	this.p = setBit(this.p, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the Persons
     * @return All Persons that have been set
     */
    public Person[] getPersons() {
   	Enum<?>[] ea = filterConstants(Person.values(), this.p);
    if( this.p == null || ea == null)
	    return new Person[0];
	//Enum<?>[] ea = filterConstants(Person.values(), this.p);
	Person[] ret = new Person[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if Person is set
     * @param c
     * @return <code>true</code> if Person is enabled, <code>false</code> otherwise
     */
    public boolean hasPerson(Person c) {
	if( this.p == null || c == null)
	    return false;
	return getBit(this.p, c.ordinal());
    }


    // *********** tempus *************  

    /**
     * Clears and sets new cases
     * @param tempora
     */
    public void setTempora(Tempus ... tempora) {
	this.te = null;
	if(tempora == null)
	    return;
	for (int i = 0; i < tempora.length; i++) {
	    this.te = setBit(this.te, tempora[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a Tempus
     * @param c The Tempus
     * @param b Indicates whether to set or remove the Tempus
     */
    public void setTempus(Tempus c, boolean b) {
	if( c == null || b == false && ! this.hasTempus(c))
	    return;
	this.te = setBit(this.te, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the Tempora
     * @return All Tempora that have been set
     */
    public Tempus[] getTempora() {
   	Enum<?>[] ea = filterConstants(Tempus.values(), this.te);
	if( this.te == null || ea == null )
	    return new Tempus[0];
	//Enum<?>[] ea = filterConstants(Tempus.values(), this.te);
	Tempus[] ret = new Tempus[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if Tempus is set
     * @param c
     * @return <code>true</code> if Tempus is enabled, <code>false</code> otherwise
     */
    public boolean hasTempus(Tempus c) {
	if( this.te == null || c == null)
	    return false;
	return getBit(this.te, c.ordinal());
    }

    // *********** type *************  

    /**
     * Clears and sets new cases
     * @param types
     */
    public void setTypes(Type ... types) {
	this.ty = null;
	if(types == null)
	    return;
	for (int i = 0; i < types.length; i++) {
	    this.ty = setBit(this.ty, types[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a Type
     * @param c The Type
     * @param b Indicates whether to set or remove the Type
     */
    public void setType(Type c, boolean b) {
	if( c == null || b == false && ! this.hasType(c))
	    return;
	this.ty = setBit(this.ty, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the Types
     * @return All Types that have been set
     */
    public Type[] getTypes() {
  	Enum<?>[] ea = filterConstants(Type.values(), this.ty);
    if( this.ty == null || ea == null )
	    return new Type[0];
	//Enum<?>[] ea = filterConstants(Type.values(), this.ty);
	Type[] ret = new Type[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if Type is set
     * @param c
     * @return <code>true</code> if Type is enabled, <code>false</code> otherwise
     */
    public boolean hasType(Type c) {
	if( this.ty == null || c == null)
	    return false;
	return getBit(this.ty, c.ordinal());
    }

    // *********** wordclass *************  

    /**
     * Clears and sets new cases
     * @param wordclasses
     */
    public void setWordclasses(Wordclass ... wordclasses) {
	this.wc = null;
	if(wordclasses == null)
	    return;
	for (int i = 0; i < wordclasses.length; i++) {
	    this.wc = setBit(this.wc, wordclasses[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a Wordclass
     * @param c The Wordclass
     * @param b Indicates whether to set or remove the Wordclass
     */
    public void setWordclass(Wordclass c, boolean b) {
	if( c == null || b == false && ! this.hasWordclass(c))
	    return;
	this.wc = setBit(this.wc, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the Wordclasses
     * @return All Wordclasses that have been set
     */
    public Wordclass[] getWordclasses() {
	Enum<?>[] ea = filterConstants(Wordclass.values(), this.wc);
    if( this.wc == null || ea == null )
	    return new Wordclass[0];
	//Enum<?>[] ea = filterConstants(Wordclass.values(), this.wc);
	Wordclass[] ret = new Wordclass[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if Wordclass is set
     * @param c
     * @return <code>true</code> if Wordclass is enabled, <code>false</code> otherwise
     */
    public boolean hasWordclass(Wordclass c) {
	if( this.wc == null || c == null)
	    return false;
	return getBit(this.wc, c.ordinal());
    }

    // *********** WordsubclassAdjective *************  

    /**
     * Clears and sets new cases
     * @param WordsubclassesAdjective
     */
    public void setWordsubclassesAdjective(WordsubclassAdjective ... WordsubclassesAdjective) {
	this.wsa = null;
	if(WordsubclassesAdjective == null)
	    return;
	for (int i = 0; i < WordsubclassesAdjective.length; i++) {
	    this.wsa = setBit(this.wsa, WordsubclassesAdjective[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a WordsubclassAdjective
     * @param c The WordsubclassAdjective
     * @param b Indicates whether to set or remove the WordsubclassAdjective
     */
    public void setWordsubclassAdjective(WordsubclassAdjective c, boolean b) {
	if( c == null || b == false && ! this.hasWordsubclassAdjective(c))
	    return;
	this.wsa = setBit(this.wsa, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the WordsubclassesAdjective
     * @return All WordsubclassesAdjective that have been set
     */
    public WordsubclassAdjective[] getWordsubclassesAdjective() {
    Enum<?>[] ea = filterConstants(WordsubclassAdjective.values(), this.wsa);
    if( this.wsa == null || ea == null )
	    return new WordsubclassAdjective[0];
	//Enum<?>[] ea = filterConstants(WordsubclassAdjective.values(), this.wsa);
	WordsubclassAdjective[] ret = new WordsubclassAdjective[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if WordsubclassAdjective is set
     * @param c
     * @return <code>true</code> if WordsubclassAdjective is enabled, <code>false</code> otherwise
     */
    public boolean hasWordsubclassAdjective(WordsubclassAdjective c) {
	if( this.wsa == null || c == null)
	    return false;
	return getBit(this.wsa, c.ordinal());
    }

    // *********** WordsubclassConnector *************  

    /**
     * Clears and sets new cases
     * @param WordsubclassesConnector
     */
    public void setWordsubclassesConnector(WordsubclassConnector ... WordsubclassesConnector) {
	this.wsc = null;
	if(WordsubclassesConnector == null)
	    return;
	for (int i = 0; i < WordsubclassesConnector.length; i++) {
	    this.wsc = setBit(this.wsc, WordsubclassesConnector[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a WordsubclassConnector
     * @param c The WordsubclassConnector
     * @param b Indicates whether to set or remove the WordsubclassConnector
     */
    public void setWordsubclassConnector(WordsubclassConnector c, boolean b) {
	if( c == null || b == false && ! this.hasWordsubclassConnector(c))
	    return;
	this.wsc = setBit(this.wsc, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the WordsubclassesConnector
     * @return All WordsubclassesConnector that have been set
     */
    public WordsubclassConnector[] getWordsubclassesConnector() {
   	Enum<?>[] ea = filterConstants(WordsubclassConnector.values(), this.wsc);
   	if( this.wsc == null || ea == null )
	    return new WordsubclassConnector[0];
	//Enum<?>[] ea = filterConstants(WordsubclassConnector.values(), this.wsc);
	WordsubclassConnector[] ret = new WordsubclassConnector[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if WordsubclassConnector is set
     * @param c
     * @return <code>true</code> if WordsubclassConnector is enabled, <code>false</code> otherwise
     */
    public boolean hasWordsubclassConnector(WordsubclassConnector c) {
	if( this.wsc == null || c == null)
	    return false;
	return getBit(this.wsc, c.ordinal());
    }

    // *********** WordsubclassPreposition *************  

    /**
     * Clears and sets new cases
     * @param WordsubclassesPreposition
     */
    public void setWordsubclassesPreposition(WordsubclassPreposition ... WordsubclassesPreposition) {
	this.wspre = null;
	if(WordsubclassesPreposition == null)
	    return;
	for (int i = 0; i < WordsubclassesPreposition.length; i++) {
	    this.wspre = setBit(this.wspre, WordsubclassesPreposition[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a WordsubclassPreposition
     * @param c The WordsubclassPreposition
     * @param b Indicates whether to set or remove the WordsubclassPreposition
     */
    public void setWordsubclassPreposition(WordsubclassPreposition c, boolean b) {
	if( c == null || b == false && ! this.hasWordsubclassPreposition(c))
	    return;
	this.wspre = setBit(this.wspre, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the WordsubclassesPreposition
     * @return All WordsubclassesPreposition that have been set
     */
    public WordsubclassPreposition[] getWordsubclassesPreposition() {
   	Enum<?>[] ea = filterConstants(WordsubclassPreposition.values(), this.wspre);
    if( this.wspre == null || ea == null)
	    return new WordsubclassPreposition[0];
	//Enum<?>[] ea = filterConstants(WordsubclassPreposition.values(), this.wspre);
	WordsubclassPreposition[] ret = new WordsubclassPreposition[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if WordsubclassPreposition is set
     * @param c
     * @return <code>true</code> if WordsubclassPreposition is enabled, <code>false</code> otherwise
     */
    public boolean hasWordsubclassPreposition(WordsubclassPreposition c) {
	if( this.wspre == null || c == null)
	    return false;
	return getBit(this.wspre, c.ordinal());
    }

    // *********** WordsubclassPronoun *************  

    /**
     * Clears and sets new cases
     * @param WordsubclassesPronoun
     */
    public void setWordsubclassesPronoun(WordsubclassPronoun ... WordsubclassesPronoun) {
	this.wspro = null;
	if(WordsubclassesPronoun == null)
	    return;
	for (int i = 0; i < WordsubclassesPronoun.length; i++) {
	    this.wspro = setBit(this.wspro, WordsubclassesPronoun[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a WordsubclassPronoun
     * @param c The WordsubclassPronoun
     * @param b Indicates whether to set or remove the WordsubclassPronoun
     */
    public void setWordsubclassPronoun(WordsubclassPronoun c, boolean b) {
	if( c == null || b == false && ! this.hasWordsubclassPronoun(c))
	    return;
	this.wspro = setBit(this.wspro, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the WordsubclassesPronoun
     * @return All WordsubclassesPronoun that have been set
     */
    public WordsubclassPronoun[] getWordsubclassesPronoun() {
   	Enum<?>[] ea = filterConstants(WordsubclassPronoun.values(), this.wspro);
    if( this.wspro == null || ea == null )
	    return new WordsubclassPronoun[0];
	//Enum<?>[] ea = filterConstants(WordsubclassPronoun.values(), this.wspro);
	WordsubclassPronoun[] ret = new WordsubclassPronoun[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if WordsubclassPronoun is set
     * @param c
     * @return <code>true</code> if WordsubclassPronoun is enabled, <code>false</code> otherwise
     */
    public boolean hasWordsubclassPronoun(WordsubclassPronoun c) {
	if( this.wspro == null || c == null)
	    return false;
	return getBit(this.wspro, c.ordinal());
    }

    // *********** WordsubclassSign *************  

    /**
     * Clears and sets new cases
     * @param WordsubclassesSign
     */
    public void setWordsubclassesSign(WordsubclassSign ... WordsubclassesSign) {
	this.wss = null;
	if(WordsubclassesSign == null)
	    return;
	for (int i = 0; i < WordsubclassesSign.length; i++) {
	    this.wss = setBit(this.wss, WordsubclassesSign[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a WordsubclassSign
     * @param c The WordsubclassSign
     * @param b Indicates whether to set or remove the WordsubclassSign
     */
    public void setWordsubclassSign(WordsubclassSign c, boolean b) {
	if( c == null || b == false && ! this.hasWordsubclassSign(c))
	    return;
	this.wss = setBit(this.wss, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the WordsubclassesSign
     * @return All WordsubclassesSign that have been set
     */
    public WordsubclassSign[] getWordsubclassesSign() {
   	Enum<?>[] ea = filterConstants(WordsubclassSign.values(), this.wss);
    if( this.wss == null || ea == null)
	    return new WordsubclassSign[0];
	//Enum<?>[] ea = filterConstants(WordsubclassSign.values(), this.wss);
	WordsubclassSign[] ret = new WordsubclassSign[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if WordsubclassSign is set
     * @param c
     * @return <code>true</code> if WordsubclassSign is enabled, <code>false</code> otherwise
     */
    public boolean hasWordsubclassSign(WordsubclassSign c) {
	if( this.wss == null || c == null)
	    return false;
	return getBit(this.wss, c.ordinal());
    }

    // *********** WordsubclassVerb *************  

    /**
     * Clears and sets new cases
     * @param WordsubclassesVerb
     */
    public void setWordsubclassesVerb(WordsubclassVerb ... WordsubclassesVerb) {
	this.wsv = null;
	if(WordsubclassesVerb == null)
	    return;
	for (int i = 0; i < WordsubclassesVerb.length; i++) {
	    this.wsv = setBit(this.wsv, WordsubclassesVerb[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a WordsubclassVerb
     * @param c The WordsubclassVerb
     * @param b Indicates whether to set or remove the WordsubclassVerb
     */
    public void setWordsubclassVerb(WordsubclassVerb c, boolean b) {
	if( c == null || b == false && ! this.hasWordsubclassVerb(c))
	    return;
	this.wsv = setBit(this.wsv, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the WordsubclassesVerb
     * @return All WordsubclassesVerb that have been set
     */
    public WordsubclassVerb[] getWordsubclassesVerb() {
   	Enum<?>[] ea = filterConstants(WordsubclassVerb.values(), this.wsv);
    if( this.wsv == null || ea == null )
	    return new WordsubclassVerb[0];
	//Enum<?>[] ea = filterConstants(WordsubclassVerb.values(), this.wsv);
	WordsubclassVerb[] ret = new WordsubclassVerb[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if WordsubclassVerb is set
     * @param c
     * @return <code>true</code> if WordsubclassVerb is enabled, <code>false</code> otherwise
     */
    public boolean hasWordsubclassVerb(WordsubclassVerb c) {
	if( this.wsv == null || c == null)
	    return false;
	return getBit(this.wsv, c.ordinal());
    }

    // *********** Wortart1 *************  

    /**
     * Clears and sets new cases
     * @param Wortarten1
     */
    public void setWortarten1(Wortart1 ... Wortarten1) {
	this.wa1 = null;
	if(Wortarten1 == null)
	    return;
	for (int i = 0; i < Wortarten1.length; i++) {
	    this.wa1 = setBit(this.wa1, Wortarten1[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a Wortart1
     * @param c The Wortart1
     * @param b Indicates whether to set or remove the Wortart1
     */
    public void setWortart1(Wortart1 c, boolean b) {
	if( c == null || b == false && ! this.hasWortart1(c))
	    return;
	this.wa1 = setBit(this.wa1, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the Wortarten1
     * @return All Wortarten1 that have been set
     */
    public Wortart1[] getWortarten1() {
   	Enum<?>[] ea = filterConstants(Wortart1.values(), this.wa1);
	if( this.wa1 == null || ea == null )
	    return new Wortart1[0];
	//Enum<?>[] ea = filterConstants(Wortart1.values(), this.wa1);
	Wortart1[] ret = new Wortart1[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if Wortart1 is set
     * @param c
     * @return <code>true</code> if Wortart1 is enabled, <code>false</code> otherwise
     */
    public boolean hasWortart1(Wortart1 c) {
	if( this.wa1 == null || c == null)
	    return false;
	return getBit(this.wa1, c.ordinal());
    }

    // *********** Wortart2 *************  

    /**
     * Clears and sets new cases
     * @param Wortarten2
     */
    public void setWortarten2(Wortart2 ... Wortarten2) {
	this.wa2 = null;
	if(Wortarten2 == null)
	    return;
	for (int i = 0; i < Wortarten2.length; i++) {
	    this.wa2 = setBit(this.wa2, Wortarten2[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a Wortart2
     * @param c The Wortart2
     * @param b Indicates whether to set or remove the Wortart2
     */
    public void setWortart2(Wortart2 c, boolean b) {
	if( c == null || b == false && ! this.hasWortart2(c))
	    return;
	this.wa2 = setBit(this.wa2, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the Wortarten2
     * @return All Wortarten2 that have been set
     */
    public Wortart2[] getWortarten2() {
 	Enum<?>[] ea = filterConstants(Wortart2.values(), this.wa2);
    if( this.wa2 == null || ea == null )
	    return new Wortart2[0];
	//Enum<?>[] ea = filterConstants(Wortart2.values(), this.wa2);
	Wortart2[] ret = new Wortart2[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if Wortart2 is set
     * @param c
     * @return <code>true</code> if Wortart2 is enabled, <code>false</code> otherwise
     */
    public boolean hasWortart2(Wortart2 c) {
	if( this.wa2 == null || c == null)
	    return false;
	return getBit(this.wa2, c.ordinal());
    }

    // *********** Wortart3 *************  

    /**
     * Clears and sets new cases
     * @param Wortarten3
     */
    public void setWortarten3(Wortart3 ... Wortarten3) {
	this.wa3 = null;
	if(Wortarten3 == null)
	    return;
	for (int i = 0; i < Wortarten3.length; i++) {
	    this.wa3 = setBit(this.wa3, Wortarten3[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a Wortart3
     * @param c The Wortart3
     * @param b Indicates whether to set or remove the Wortart3
     */
    public void setWortart3(Wortart3 c, boolean b) {
	if( c == null || b == false && ! this.hasWortart3(c))
	    return;
	this.wa3 = setBit(this.wa3, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the Wortarten3
     * @return All Wortarten3 that have been set
     */
    public Wortart3[] getWortarten3() {
   	Enum<?>[] ea = filterConstants(Wortart3.values(), this.wa3);
	if( this.wa3 == null || ea == null )
	    return new Wortart3[0];
	//Enum<?>[] ea = filterConstants(Wortart3.values(), this.wa3);
	Wortart3[] ret = new Wortart3[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if Wortart3 is set
     * @param c
     * @return <code>true</code> if Wortart3 is enabled, <code>false</code> otherwise
     */
    public boolean hasWortart3(Wortart3 c) {
	if( this.wa3 == null || c == null)
	    return false;
	return getBit(this.wa3, c.ordinal());
    }

    // *********** Wortart4 *************  

    /**
     * Clears and sets new cases
     * @param Wortarten4
     */
    public void setWortarten4(Wortart4 ... Wortarten4) {
	this.wa4 = null;
	if(Wortarten4 == null)
	    return;
	for (int i = 0; i < Wortarten4.length; i++) {
	    this.wa4 = setBit(this.wa4, Wortarten4[i].ordinal());
	}
	this.changeState(CHANGE);
    }

    /**
     * Sets a Wortart4
     * @param c The Wortart4
     * @param b Indicates whether to set or remove the Wortart4
     */
    public void setWortart4(Wortart4 c, boolean b) {
	if( c == null || b == false && ! this.hasWortart4(c))
	    return;
	this.wa4 = setBit(this.wa4, c.ordinal(), b);
	this.changeState(CHANGE);
    }

    /**
     * Get the Wortarten4
     * @return All Wortarten4 that have been set
     */
    public Wortart4[] getWortarten4() {
   	Enum<?>[] ea = filterConstants(Wortart4.values(), this.wa4);
	if( this.wa4 == null || ea == null )
	    return new Wortart4[0];
	//Enum<?>[] ea = filterConstants(Wortart4.values(), this.wa4);
	Wortart4[] ret = new Wortart4[ea.length];
	System.arraycopy(ea, 0, ret, 0, ea.length);
	return ret;
    }

    /**
     * Check if Wortart4 is set
     * @param c
     * @return <code>true</code> if Wortart4 is enabled, <code>false</code> otherwise
     */
    public boolean hasWortart4(Wortart4 c) {
	if( this.wa4 == null || c == null)
	    return false;
	return getBit(this.wa4, c.ordinal());
    }

    /**
     * @param enums This should be MyEnum.values()
     * @param enumsBinary
     * @return The subset of Constants in <code>enums</code> which have an index value equal to the position of a bit set in enumsBinary
     */
    public static Enum<?>[] filterConstants( Enum<?>[] enums, byte[] enumsBinary) {
	BitSet bs = byteArrayToBitSet(enumsBinary);
	Enum<?>[] ret = new Enum[bs.cardinality()];
	int j=0;
	for(int i=bs.nextSetBit(0); i>=0; i=bs.nextSetBit(i+1), j++) {
	    if(i > enums.length)
	    	return null;
	    else
	    	ret[j] = enums[i];
	}
	return ret;
    }

    /**
     * Set the bit at <code>position</code> in a copy of <code>binaryString</code> to 1 if <code>bool</code> is true, 0 otherwise
     * @param binaryString
     * @param position
     * @param bool
     * @return A copy of <code>binaryString</code> with bit at <code>position</code> set to 1 if <code>bool</code> is true, 0 otherwise
     */
    public static byte[] setBit(byte[] binaryString, int position, boolean bool) {
	BitSet bs = byteArrayToBitSet(binaryString);
	bs.set(position, bool);
	return toByteArray(bs);
    }

    /**
     * Set the bit at <code>position</code> in a copy of <code>binaryString</code> to 1
     * @param binaryString
     * @param position
     * @param bool
     * @return A copy of <code>binaryString</code> with bit at <code>position</code> set to 1
     */
    public static byte[] setBit(byte[] binaryString, int position) {
	return setBit(binaryString, position, true);
    }

    /**
     * @param binaryString
     * @param position
     * @return true if the bit at <code>position</code> in <code>binaryString</code> is 1, false otherwise
     */
    public static boolean getBit(byte[] binaryString, int position) {
	BitSet bs = byteArrayToBitSet(binaryString);
	return bs.get(position);
    }

    /**
     * Returns a bitset containing the values in bytes.
     * The byte-ordering of bytes must be big-endian which means the most significant bit is in element 0.
     * @param bytes
     * @return
     */
    public static BitSet byteArrayToBitSet(byte[] bytes) {
	BitSet bits = new BitSet();
	if( bytes == null )
	    return bits;
	for (int i=0; i<bytes.length<<3; i++) {
	    if ((bytes[bytes.length-(i>>3)-1]&(1<<(i%8))) > 0) {
		bits.set(i);
	    }
	}
	return bits;
    }

    /**
     * Returns a byte array of at least length 1.
     * The most significant bit in the result is guaranteed not to be a 1
     * (since BitSet does not support sign extension).
     * The byte-ordering of the result is big-endian which means the most significant bit is in element 0.
     * The bit at index 0 of the bit set is assumed to be the least significant bit.
     * @param bits
     * @return
     */
    public static byte[] toByteArray(BitSet bits) {
	byte[] bytes = new byte[(bits.length()>>3)+1];
	for(int i=bits.nextSetBit(0); i>=0; i=bits.nextSetBit(i+1)) {
	    bytes[bytes.length-(i>>3)-1] |= 1<<(i%8);
	}
	return bytes;
    }

    @Override
    public int getIndex() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public boolean remove() {
	this.changeState(REMOVE);
	return true;
    }

    @Override
    public void setChapter(DBC_Key key, Chapter chapter) {

    }

    /**
     * @return the etymol
     */
    public String getEtymol() {
	return y;
    }

    /**
     * @return the description
     */
    public String getDescription() {
	return d;
    }

    /**
     * @param etymol the etymol to set
     */
    public void setEtymol(String etymol) {
	this.y = etymol;
	this.changeState(CHANGE);
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
	this.d = description;
	this.changeState(CHANGE);
    }

    /**
     * @return the a
     */
    public String getAbbreviation() {
	return a;
    }

    /**
     * @param a the a to set
     */
    public void setAbbreviation(String a) {
	this.a = a;
    }

    public class TR_Assignation_DB extends TR_Assignation {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1013017658925193474L;

	public TR_Assignation_DB() {

	    // TR_Assignation
	    this.ca = TR_Assignation.this.ca;
	    this.co = TR_Assignation.this.co;
	    this.de = TR_Assignation.this.de;
	    this.di = TR_Assignation.this.di;
	    this.g = TR_Assignation.this.g;
	    this.n = TR_Assignation.this.n;
	    this.p = TR_Assignation.this.p;
	    this.te = TR_Assignation.this.te;
	    this.ty = TR_Assignation.this.ty;
	    this.wa1 = TR_Assignation.this.wa1;
	    this.wa2 = TR_Assignation.this.wa2;
	    this.wa3 = TR_Assignation.this.wa3;
	    this.wa4 = TR_Assignation.this.wa4;
	    this.wc = TR_Assignation.this.wc;
	    this.wsa = TR_Assignation.this.wsa;
	    this.wsc = TR_Assignation.this.wsc;
	    this.wspre = TR_Assignation.this.wspre;
	    this.wspro = TR_Assignation.this.wspro;
	    this.wss = TR_Assignation.this.wss;
	    this.wsv = TR_Assignation.this.wsv;
	    this.setEtymol(TR_Assignation.this.getEtymol());
	    this.setAbbreviation(TR_Assignation.this.getAbbreviation());
	    this.setDescription(TR_Assignation.this.getDescription());

	    // DB_Element
	    this.setDB_ID(TR_Assignation.this.getDB_ID());
	    this.changeState(TR_Assignation.this.getStateAsInt());
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getCasesBinary() {
	    return (this.ca == null) ? new byte[0] : this.ca;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setCasesBinary(byte[] binary) {

	    this.ca = binary;
	    this.changeState(CHANGE);
	}


	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getConjugationsBinary() {
	    return (this.co == null) ? new byte[0] : this.co;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setConjugationsBinary(byte[] binary) {

	    this.co = binary;
	    this.changeState(CHANGE);
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getDeterminationsBinary() {
	    return (this.de == null) ? new byte[0] : this.de;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setDeterminationsBinary(byte[] binary) {

	    this.de = binary;
	    this.changeState(CHANGE);
	}


	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getDiathesesBinary() {
	    return (this.di == null) ? new byte[0] : this.di;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setDiathesesBinary(byte[] binary) {

	    this.di = binary;
	    this.changeState(CHANGE);
	}


	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getGeneraBinary() {
	    return (this.g == null) ? new byte[0] : this.g;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setGeneraBinary(byte[] binary) {

	    this.g = binary;
	    this.changeState(CHANGE);
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getNumeriBinary() {
	    return (this.n == null) ? new byte[0] : this.n;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setNumeriBinary(byte[] binary) {

	    this.n = binary;
	    this.changeState(CHANGE);
	}


	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getPersonsBinary() {
	    return (this.p == null) ? new byte[0] : this.p;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setPersonsBinary(byte[] binary) {

	    this.p = binary;
	    this.changeState(CHANGE);
	}


	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getTemporaBinary() {
	    return (this.te == null) ? new byte[0] : this.te;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setTemporaBinary(byte[] binary) {

	    this.te = binary;
	    this.changeState(CHANGE);
	}


	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getTypesBinary() {
	    return (this.ty == null) ? new byte[0] : this.ty;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setTypesBinary(byte[] binary) {

	    this.ty = binary;
	    this.changeState(CHANGE);
	}


	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getWordclassesBinary() {
	    return (this.wc == null) ? new byte[0] : this.wc;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setWordclassesBinary(byte[] binary) {

	    this.wc = binary;
	    this.changeState(CHANGE);
	}


	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getWordsubclassesAdjectiveBinary() {
	    return (this.wsa == null) ? new byte[0] : this.wsa;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setWordsubclassesAdjectiveBinary(byte[] binary) {

	    this.wsa = binary;
	    this.changeState(CHANGE);
	}


	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getWordsubclassesConnectorBinary() {
	    return (this.wsc == null) ? new byte[0] : this.wsc;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setWordsubclassesConnectorBinary(byte[] binary) {

	    this.wsa = binary;
	    this.changeState(CHANGE);
	}


	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getWordsubclassesPrepositionBinary() {
	    return (this.wspre == null) ? new byte[0] : this.wspre;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setWordsubclassesPrepositionBinary(byte[] binary) {

	    this.wspre = binary;
	    this.changeState(CHANGE);
	}


	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getWordsubclassesPronounBinary() {
	    return (this.wspro == null) ? new byte[0] : this.wspro;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setWordsubclassesPronounBinary(byte[] binary) {

	    this.wspro = binary;
	    this.changeState(CHANGE);
	}


	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getWordsubclassesSignBinary() {
	    return (this.wss == null) ? new byte[0] : this.wss;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setWordsubclassesSignBinary(byte[] binary) {

	    this.wss = binary;
	    this.changeState(CHANGE);
	}


	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getWordsubclassesVerbBinary() {
	    return (this.wsv == null) ? new byte[0] : this.wsv;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setWordsubclassesVerbBinary(byte[] binary) {

	    this.wsv = binary;
	    this.changeState(CHANGE);
	}


	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getWortarten1Binary() {
	    return (this.wa1 == null) ? new byte[0] : this.wa1;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setWortarten1Binary(byte[] binary) {

	    this.wa1 = binary;
	    this.changeState(CHANGE);
	}


	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getWortarten2Binary() {
	    return (this.wa2 == null) ? new byte[0] : this.wa2;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setWortarten2Binary(byte[] binary) {

	    this.wa2 = binary;
	    this.changeState(CHANGE);
	}


	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getWortarten3Binary() {
	    return (this.wa3 == null) ? new byte[0] : this.wa3;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setWortarten3Binary(byte[] binary) {

	    this.wa3 = binary;
	    this.changeState(CHANGE);
	}


	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @return
	 */
	public byte[] getWortarten4Binary() {
	    return (this.wa4 == null) ? new byte[0] : this.wa4;
	}

	/**
	 * Direct value access for DBC_Server
	 * @param key
	 * @param binary
	 */
	public void setWortarten4Binary(byte[] binary) {

	    this.wa4 = binary;
	    this.changeState(CHANGE);
	}

    }

}
