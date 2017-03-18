/****************************************************************************************
 * @file  main_package.Table.java
 *
 * @author   John Miller
 */

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.System.out;

/****************************************************************************************
 * This class implements relational database tables (including attribute names, domains
 * and a list of tuples.  Five basic relational algebra operators are provided: project,
 * select, union, minus and join. The insert data manipulation operator is also provided.
 * Missing are update and delete data manipulation operators.
 */
public class Table
        implements Serializable
{
    /** Relative path for storage directory
     */
    private static final String DIR = "store" + File.separator;

    /** Filename extension for database files
     */
    private static final String EXT = ".dbf";

    /** Counter for naming temporary tables.
     */
    private static int count = 0;

    /** main_package.Table name.
     */
    private final String name;

    /** Array of attribute names.
     */
    private final String [] attribute;

    /** Array of attribute domains: a domain may be
     *  integer types: Long, Integer, Short, Byte
     *  real types: Double, Float
     *  string types: Character, String
     */
    private final Class [] domain;

    /** Collection of tuples (data storage).
     */
    private final List <Comparable []> tuples;

    /** Primary key. 
     */
    private final String [] key;

    /** Index into tuples (maps key to tuple number).
     */
    private final Map <KeyType, Comparable []> index;

    //----------------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------------

    /************************************************************************************
     * Construct an empty table from the meta-data specifications.
     *
     * @param _name       the name of the relation
     * @param _attribute  the string containing attributes names
     * @param _domain     the string containing attribute domains (data types)
     * @param _key        the primary key
     */
    public Table (String _name, String [] _attribute, Class [] _domain, String [] _key)
    {
        name      = _name;
        attribute = _attribute;
        domain    = _domain;
        key       = _key;
        tuples    = new ArrayList <> ();
        index     = new TreeMap <> ();       // also try BPTreeMap, LinHashMap or ExtHashMap
        // index     = new LinHashMap <> (test_package.KeyType.class, Comparable [].class);

    } // constructor

    /************************************************************************************
     * Construct a table from the meta-data specifications and data in _tuples list.
     *
     * @param _name       the name of the relation
     * @param _attribute  the string containing attributes names
     * @param _domain     the string containing attribute domains (data types)
     * @param _key        the primary key
     * @param _tuples      the list of tuples containing the data
     */
    public Table (String _name, String [] _attribute, Class [] _domain, String [] _key,
                  List <Comparable []> _tuples)
    {
        name      = _name;
        attribute = _attribute;
        domain    = _domain;
        key       = _key;
        tuples    = _tuples;
        index     = new TreeMap <> ();       // also try BPTreeMap, LinHashMap or ExtHashMap
    } // constructor

    /************************************************************************************
     * Construct an empty table from the raw string specifications.
     *
     * @param name        the name of the relation
     * @param attributes  the string containing attributes names
     * @param domains     the string containing attribute domains (data types)
     */
    public Table (String name, String attributes, String domains, String _key)
    {
        this (name, attributes.split (" "), findClass (domains.split (" ")), _key.split(" "));

        out.println ("DDL> create table " + name + " (" + attributes + ")");
    } // constructor

    //----------------------------------------------------------------------------------
    // Public Methods
    //----------------------------------------------------------------------------------
    /************************************************************************************
     * Project the tuples onto a lower dimension by keeping only the given attributes.
     * Check whether the original key is included in the projection.
     *
     * #usage movie.project ("title year studioNo")
     *
     * @param attributes  the attributes to project onto
     * @return  a table of projected tuples
     *
     * Implementation: iterate over all tuples in relation
     * get original tuple
     * keep track of new row to project
     * keep track of index of new row for adding elements
     * iterate over all original attributes
     * condition for projection
     * add projected elements to our new row
     * increment index
     * add new row to our new tuple set for new table
     *
     * @author Jonathan Waring
     *
     */
    public Table project (String attributes)
    {
        out.println ("RA> " + name + ".project (" + attributes + ")");
        String [] attrs     = attributes.split (" ");
        Class []  colDomain = extractDom (match (attrs), domain);
        String [] newKey    = (Arrays.asList (attrs).containsAll (Arrays.asList (key))) ? key : attrs;

        List <Comparable []> rows = new ArrayList <> ();
        //IMPLEMENTED HERE
        for(int i = 0; i < this.tuples.size(); i++) { //iterate over all tuples in relation
            Comparable [] tuple = this.tuples.get(i); //get original tuple
            Comparable [] row = new Comparable[attrs.length]; //this will be the new row to project
            int rowIndex = 0; //keep track of index of new row for adding elements
            for(int j = 0; j < this.attribute.length; j++) { //iterate over all original attributes
                if(Arrays.asList(attrs).contains(this.attribute[j])) { //condition for projection
		    row[rowIndex] = tuple[j]; //add projected elements to our new row
		    rowIndex ++; //increment index
                }
            }
	    boolean duplicate = false;
	    Comparable[] one = new Comparable[rowIndex];
	    for(int j = 0; j < rowIndex; j++)
		one[j] = row[j];
	    for(int k = 0; k < rows.size(); k++) {
		Comparable[] two = rows.get(k);
		if(Arrays.equals(one, two)) {
		    duplicate = true;
		}
	    }
	    if(!duplicate)  //check for duplicate elements
		rows.add(row); //add new row to our new tuple set for new table
        }
        return new Table (name + count++, attrs, colDomain, newKey, rows);
    } // project


    /************************************************************************************
     * Select the tuples satisfying the given predicate (Boolean function).
     *
     * @param predicate  the check condition for tuples
     * @return  a table with tuples satisfying the predicate
     */
    public Table select (Predicate <Comparable []> predicate)
    {
        out.println ("RA> " + name + ".select (" + predicate + ")");

        return new Table (name + count++, attribute, domain, key,
                tuples.stream ().filter (t -> predicate.test (t))
                        .collect (Collectors.toList ()));
    } // select

    /************************************************************************************
     * Select the tuples satisfying the given key predicate (key = value).  Use an index
     * (Map) to retrieve the tuple with the given key value.
     *
     * @param keyVal  the given key value
     * @return  a table with the tuple satisfying the key predicate
     *
     * @author Phillip Griggs
     * 
     * Finds all the tuples satisfying the given key. Then adds the results to the array list. 
     * 
     * 
     * 
     */
    public Table select (KeyType keyVal)
    {
        out.println ("RA> " + name + ".select (" + keyVal + ")");

        List <Comparable []> rows = new ArrayList <> ();

        //finds all the tuples satisfying the given key predicate
        Comparable[] results = index.get(keyVal);
        //adds the results to rows
        rows.add(results);

        return new Table (name + count++, attribute, domain, key, rows);
    } // select

    /************************************************************************************
     * Union this table and table2.  Check that the two tables are compatible.
     *
     * #usage movie.union (show)
     *
     * @param table2  the rhs table in the union operation
     * @return  a table representing the union
     *
     *
     *@author James Griffin
     *
     *Adds all values of table one to the result. Iterates through table one while comparing
     *each value with table two.
     *
     *If the first table does not contain tuple that matches the key, k, add the row to the result.
     *
     *Adds the key to table2
     *
     *Returns the results 
     *
     */
    public Table union (Table table2)
    {
        out.println ("RA> " + name + ".union (" + table2.name + ")");
        if (! compatible (table2)) return null;

        List <Comparable []> rows = new ArrayList <> ();

        //  Implemented by James Griffin

        rows.addAll(tuples); // Adds the tuples from first table to the result
        for (KeyType k : table2.index.keySet()) // Iterates through keys of the second table
        {
            // If the first table does not contain tuple that matches the key, k, add the row to the result.
            if (!index.containsKey(k))
            {
                rows.add(table2.index.get(k)); // Adds the key to table2
            } // if
        }// for loop

        return new Table (name + count++, attribute, domain, key, rows);
    } // union
    /************************************************************************************
     * Take the difference of this table and table2.  Check that the two tables are
     * compatible.
     *
     * #usage movie.minus (show)
     *
     * @param table2  The rhs table in the minus operation
     * @return  a table representing the difference
     *
     * Implementation: iterate over all tuples in relation 1
     *get original tuple from table 1
     *keep track of whether to add tuple to result
     *iterate over all tuples in relation 2
     *get original tuple from table 2
     *check for set difference
     *add rows that satisfy condition
     *
     * @author Jonathan Waring
     */
    public Table minus (Table table2)
    {
        out.println ("RA> " + name + ".minus (" + table2.name + ")");
        if (! compatible (table2)) return null;

        List <Comparable []> rows = new ArrayList <> ();
        //IMPLEMENTED HERE
        for(int i = 0; i < this.tuples.size(); i++) { //iterate over all tuples in relation 1
            Comparable [] tuple = this.tuples.get(i);//get original tuple from table 1
            boolean toAdd = true; //keep track of whether to add tuple to result
            for(int j = 0; j < table2.tuples.size(); j++) { //iterate over all tuples in relation 2
                Comparable [] tuple2 = table2.tuples.get(j); //get original tuple from table 2
                if(Arrays.deepEquals(tuple, tuple2)) { //check for set difference
                    toAdd = false; //not a different tuple
                }
            }
            if(toAdd)
                rows.add(tuple);
        }

        return new Table (name + count++, attribute, domain, key, rows);
    } // minus

    /************************************************************************************
     * Join this table and table2 by performing an "equi-join".  Tuples from both tables
     * are compared requiring attributes1 to equal attributes2.  Disambiguate attribute
     * names by append "2" to the end of any duplicate attribute name.
     *
     * #usage movie.join ("studioNo", "name", studio)
     *
     * @param attributes1  the attributes of this table to be compared (Foreign Key)
     * @param attributes2  the attributes of table2 to be compared (Primary Key)
     * @param table2      the rhs table in the join operation
     * @return  a table with tuples satisfying the equality predicate
     *
     *
     * My implementation: I decided to take the route of first sifting through multiple attributes each attribute
     * at a time. So, I made two arrays and stored the columns associated with the attributes listed in the parameters.
     * IF there were multiple attributes given in the attributes1 string, the attributes were split by spaces and put
     * into arrays. I then check the tuples from all the attributes passed in from table1 with the table2 attributes.
     * If the attribute names in table1 and table2 are the same, then I concatenated the tuples and added it to a row of
     * the new table. I then checked for ambiguities by checking each character in each string from table 1 and table 2.
     * If there is a duplicate then I added a 2 to the table2 attribute name.
     *
     *
     * @author  Asheton Harrell
     */
    public Table join (String attributes1, String attributes2, Table table2)
    {
        out.println ("RA> " + name + ".join (" + attributes1 + ", " + attributes2 + ", "
                + table2.name + ")");

        String [] t_attrs = attributes1.split (" ");
        String [] u_attrs = attributes2.split (" ");

        List <Comparable []> rows = new ArrayList <> ();
        // thisTuple will be our table1 tuple.
        Comparable[] thisTuple;
        //thisTuple will be our table2 tuple.
        Comparable[] table2Tuple;
        //this will be our concatenated tuples from table1 and table2.
        Comparable [] finalRowsTable;
        //get the column position of the attribute from table1 (which is the 4th indexed column, or studioName)
        int size = t_attrs.length;
        int size2 = u_attrs.length;

        int [] getAttrCol1 = new int[size];
        for(int i=0;i<size;i++){
            getAttrCol1[i] = col(t_attrs[i]);
        }

        int [] getAttrCol2 = new int[size2];
        for(int i=0;i<size2;i++) {
            getAttrCol2[i] = table2.col(u_attrs[i]);
        }
        // get the column position of the attribute from table2 being joined to table1 (0th indexed col, or name)
        /**
        Check each tuple from the studioName attribute column with the table2 name attribute column.
         */
        try {
            for (int i = 0; i < tuples.size(); i++) {
                thisTuple = tuples.get(i);
                for (int j = 0; j < table2.tuples.size(); j++) {
                    table2Tuple = table2.tuples.get(j);
                    // if attribute names are the same then concatenate the tuples and add to the arraylist.
                    for (int y = 0; y < size; y++) {
                        for (int k = 0; k < size2; k++) {
                            if (thisTuple[getAttrCol1[y]].equals(table2Tuple[getAttrCol2[k]])) {
                                finalRowsTable = ArrayUtil.concat(thisTuple, table2Tuple);
                                rows.add(finalRowsTable);
                            }
                        }
                    }
                }
            }
        } catch (Exception e){
            System.out.println("Error finding attribute.");
        }
        /**
        Check for ambiguous attribute names. If there are duplicates then concat 2 to the table2 attribute name. This
        will then be concatenated to the new table to evade duplicate attribute names.
         */
        for(int i = 0;i < attribute.length;i++)
        {
            for(int j = 0;j< table2.attribute.length;j++)
            {
                if(this.attribute[i].equals(table2.attribute[j]))
                {
                    table2.attribute[j]+="2";
                }
            }
        }

        return new Table (name + count++, ArrayUtil.concat (attribute, table2.attribute),
                ArrayUtil.concat (domain, table2.domain), key, rows);
    } // equi-join

    /************************************************************************************
     * Join this table and table2 by performing an "natural join".  Tuples from both tables
     * are compared requiring common attributes to be equal.  The duplicate column is also
     * eliminated.
     *
     * #usage movieStar.join (starsIn)
     * @author Edvin Dizdarevic
     *
     * @param table2  the rhs table in the join operation
     * @return  a table with tuples satisfying the equality predicate
     */
    public Table join (Table table2)
    {
        out.println ("RA> " + name + ".join (" + table2.name + ")");

        List <Comparable []> rows = new ArrayList <> ();

        List<Integer> match_1 = new ArrayList<>();
        List<Integer> match_2 = new ArrayList<>();

        /**
         * Check which attributes in table1 are in table2.
         * Puts all the matches from table1 in ArrayList match_1
         * Puts all the matches from table2 in ArrayList match_2
         */
        int matched = 0;
        for(int i=0; i < this.attribute.length; i++){
            for(int j=0; j < table2.attribute.length; j++){
                if((table2.attribute[j]).equals(this.attribute[i])){
                    match_1.add(i);
                    match_2.add(j);
                }
            }
        }

        /**
         * Checks for all tuples in both tables to acquire the intersection,
         * then adds the row to our result table.
         */
        for(int i=0; i < this.tuples.size(); i++){
            for(int j=0; j < table2.tuples.size(); j++){
                matched = 0;
                for(int y=0; y < match_1.size(); y++){
                    if(this.tuples.get(i)[match_1.get(y)].equals(table2.tuples.get(j)[match_2.get(y)])){
                        matched++;
                    }
                }
                if(matched == match_1.size()){
                    Comparable[] table2_tuple = new Comparable[table2.attribute.length-match_1.size()];
                    int col_counter = 0;
                    int table2_col_counter = 0;

                    if(table2_tuple.length != 0){
                        for(int y=0; y < table2.attribute.length; y++){
                            if(col_counter >= match_2.size()){
                                table2_tuple[table2_col_counter] = table2.tuples.get(j)[y];
                                table2_col_counter++;
                            }
                            else if(match_2.get(col_counter) != y){
                                table2_tuple[table2_col_counter] = table2.tuples.get(j)[y];
                                table2_col_counter++;
                            }else{
                                col_counter++;
                            }
                        }
                    }
                    rows.add(ArrayUtil.concat(this.tuples.get(i), table2_tuple));
                }
            }
        }

        /**
         * Checks for duplicate names in the second table and appends a '2'
         * to the end of the name to later be distinguished and not used as
         * new attributes for the new table.
         */
        int duplicates = 0;
        for(int i=0; i < this.attribute.length; i++){
            for( int j=0; j < table2.attribute.length; j++){
                if(this.attribute[i].equals(table2.attribute[j])){
                    table2.attribute[j] = table2.attribute[j] + "2";
                    duplicates++;
                }
            }
        }

        /**
         * Checks if the attributes from table two were duplicates
         * by checking if a 2 was at the end of the attribute name
         * and only adds to the new attributes arraylist if it isn't
         * a duplicate.
         */
        String[] newAttr = new String[table2.attribute.length - duplicates];
        int attr_to_add=0;
        for(int i=0; i < table2.attribute.length; i++){
            char charCheck = table2.attribute[i].charAt(table2.attribute[i].length()-1);
            if(charCheck != '2'){
                newAttr[attr_to_add] = table2.attribute[i];
                attr_to_add++;
            }
        }

        // FIX - eliminate duplicate columns
        return new Table (name + count++, ArrayUtil.concat (attribute, table2.attribute),
                ArrayUtil.concat (domain, table2.domain), key, rows);
    } // join

    /************************************************************************************
     * Return the column position for the given attribute name.
     *
     * @param attr  the given attribute name
     * @return  a column position
     */
    public int col (String attr)
    {
        for (int i = 0; i < attribute.length; i++) {
            if (attr.equals (attribute [i])) return i;
        } // for

        return -1;  // not found
    } // col

    /************************************************************************************
     * Insert a tuple to the table.
     *
     * #usage movie.insert ("'Star_Wars'", 1977, 124, "T", "Fox", 12345)
     *
     * @param tup  the array of attribute values forming the tuple
     * @return  whether insertion was successful
     */
    public boolean insert (Comparable [] tup)
    {
        out.println ("DML> insert into " + name + " values ( " + Arrays.toString (tup) + " )");

        if (typeCheck (tup)) {
            tuples.add (tup);
            Comparable [] keyVal = new Comparable [key.length];
            int []        cols   = match (key);
            for (int j = 0; j < keyVal.length; j++) keyVal [j] = tup [cols [j]];
            index.put (new KeyType(keyVal), tup);
            return true;
        } else {
            return false;
        } // if
    } // insert

    /************************************************************************************
     * Get the name of the table.
     *
     * @return  the table's name
     */
    public String getName ()
    {
        return name;
    } // getName

    /************************************************************************************
     * Print this table.
     */
    public void print ()
    {
        out.println ("\n main_package.Table " + name);
        out.print ("|-");
        for (int i = 0; i < attribute.length; i++) out.print ("---------------");
        out.println ("-|");
        out.print ("| ");
        for (String a : attribute) out.printf ("%15s", a);
        out.println (" |");
        out.print ("|-");
        for (int i = 0; i < attribute.length; i++) out.print ("---------------");
        out.println ("-|");
        for (Comparable [] tup : tuples) {
            out.print ("| ");
            for (Comparable attr : tup) out.printf ("%15s", attr);
            out.println (" |");
        } // for
        out.print ("|-");
        for (int i = 0; i < attribute.length; i++) out.print ("---------------");
        out.println ("-|");
    } // print

    /************************************************************************************
     * Print this table's index (Map).
     */
    public void printIndex ()
    {
        out.println ("\n Index for " + name);
        out.println ("-------------------");
        for (Map.Entry <KeyType, Comparable []> e : index.entrySet ()) {
            out.println (e.getKey () + " -> " + Arrays.toString (e.getValue ()));
        } // for
        out.println ("-------------------");
    } // printIndex

    /************************************************************************************
     * Load the table with the given name into memory.
     *
     * @param name  the name of the table to load
     */
    public static Table load (String name)
    {
        Table tab = null;
        try {
            ObjectInputStream ois = new ObjectInputStream (new FileInputStream (DIR + name + EXT));
            tab = (Table) ois.readObject ();
            ois.close ();
        } catch (IOException ex) {
            out.println ("load: IO Exception");
            ex.printStackTrace ();
        } catch (ClassNotFoundException ex) {
            out.println ("load: Class Not Found Exception");
            ex.printStackTrace ();
        } // try
        return tab;
    } // load

    /************************************************************************************
     * Save this table in a file.
     */
    public void save ()
    {
        try {
            ObjectOutputStream oos = new ObjectOutputStream (new FileOutputStream (DIR + name + EXT));
            oos.writeObject (this);
            oos.close ();
        } catch (IOException ex) {
            out.println ("save: IO Exception");
            ex.printStackTrace ();
        } // try
    } // save

    //----------------------------------------------------------------------------------
    // Private Methods
    //----------------------------------------------------------------------------------

    /************************************************************************************
     * Determine whether the two tables (this and table2) are compatible, i.e., have
     * the same number of attributes each with the same corresponding domain.
     *
     * @param table2  the rhs table
     * @return  whether the two tables are compatible
     */
    private boolean compatible (Table table2)
    {
        if (domain.length != table2.domain.length) {
            out.println ("compatible ERROR: table have different arity");
            return false;
        } // if
        for (int j = 0; j < domain.length; j++) {
            if (domain [j] != table2.domain [j]) {
                out.println ("compatible ERROR: tables disagree on domain " + j);
                return false;
            } // if
        } // for
        return true;
    } // compatible

    /************************************************************************************
     * Match the column and attribute names to determine the domains.
     *
     * @param column  the array of column names
     * @return  an array of column index positions
     */
    private int [] match (String [] column)
    {
        int [] colPos = new int [column.length];

        for (int j = 0; j < column.length; j++) {
            boolean matched = false;
            for (int k = 0; k < attribute.length; k++) {
                if (column [j].equals (attribute [k])) {
                    matched = true;
                    colPos [j] = k;
                } // for
            } // for
            if ( ! matched) {
                out.println ("match: domain not found for " + column [j]);
            } // if
        } // for

        return colPos;
    } // match

    /************************************************************************************
     * Extract the attributes specified by the column array from tuple t.
     *
     * @param t       the tuple to extract from
     * @param column  the array of column names
     * @return  a smaller tuple extracted from tuple t
     */
    private Comparable [] extract (Comparable [] t, String [] column)
    {
        Comparable [] tup = new Comparable [column.length];
        int [] colPos = match (column);
        for (int j = 0; j < column.length; j++) tup [j] = t [colPos [j]];
        return tup;
    } // extract

    /************************************************************************************
     * Check the size of the tuple (number of elements in list) as well as the type of
     * each value to ensure it is from the right domain.
     *
     * @param t  the tuple as a list of attribute values
     * @return  whether the tuple has the right size and values that comply
     *          with the given domains
     *
     *@author Phillip Griggs
     *
     *The first if statement compares the domain length to the length of t.
     *If they don't equal each other, print an error and return false. 
     *
     *Second if statement, ensures domain and t lengths are the same. Goes through and compares 
     *values.
     *
     *
     */
    private boolean typeCheck (Comparable [] t)
    {
        //check to make sure that the lengths are the same, if not return error
        if(domain.length != t.length){
            System.err.println("Length is not the same");
            return false;
        }

        //checks to ensure lengths are the same
        if(domain.length == t.length){
            //iterates through  domain
            int j = 0;
            for(int i = 0; i < domain.length; i++){
                //compares the objects to see if they're the same
                //if not, return an error and return false
                if(domain[i] != t[j].getClass())
                    System.err.println("Error, not in the proper domain");
                j++;
            }
        }
        return true;
    } // typeCheck

    /************************************************************************************
     * Find the classes in the "java.lang" package with given names.
     *
     * @param className  the array of class name (e.g., {"Integer", "String"})
     * @return  an array of Java classes
     */
    private static Class [] findClass (String [] className)
    {
        Class [] classArray = new Class [className.length];

        for (int i = 0; i < className.length; i++) {
            try {
                classArray [i] = Class.forName ("java.lang." + className [i]);
            } catch (ClassNotFoundException ex) {
                out.println ("findClass: " + ex);
            } // try
        } // for

        return classArray;
    } // findClass

    /************************************************************************************
     * Extract the corresponding domains.
     *
     * @param colPos the column positions to extract.
     * @param group  where to extract from
     * @return  the extracted domains
     */
    private Class [] extractDom (int [] colPos, Class [] group)
    {
        Class [] obj = new Class [colPos.length];

        for (int j = 0; j < colPos.length; j++) {
            obj [j] = group [colPos [j]];
        } // for

        return obj;
    } // extractDom

	public boolean equalsTo(Comparable[] expected2, Comparable[] actual2) {
		// TODO Auto-generated method stub
		return false;
	}

	public Comparable[] getTupleList(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getColNumber() {
		// TODO Auto-generated method stub
		return null;
	}

} // main_package.Table class

