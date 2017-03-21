
/*****************************************************************************************
 * @file  TestTupleGenerator.java
 *
 *
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.lang.System.out;

/*****************************************************************************************
 * This class tests the TupleGenerator on the Student Registration Database defined in the
 * Kifer, Bernstein and Lewis 2006 database textbook (see figure 3.6).  The primary keys
 * (see figure 3.6) and foreign keys (see example 3.2.2) are as given in the textbook.
 */
public class TestTupleGenerator
{
    /*************************************************************************************
     * The main method is the driver for TestGenerator.
     * @param args  the command-line arguments
     */
    public static void main (String [] args)
    {
        TupleGenerator test = new TupleGeneratorImpl ();
        // TupleGenerator selectRange = new TupleGeneratorImpl() ;


        test.addRelSchema ("Student",
                "id name address status",
                "Integer String String String",
                "id",
                null);

        test.addRelSchema ("Professor",
                "id name deptId",
                "Integer String String",
                "id",
                null);

        test.addRelSchema ("Course",
                "crsCode deptId crsName descr",
                "String String String String",
                "crsCode",
                null);

        test.addRelSchema ("Teaching",
                "crsCode semester profId",
                "String String Integer",
                "crcCode semester",
                new String [][] {{ "profId", "Professor", "id" },
                        { "crsCode", "Course", "crsCode" }});

        test.addRelSchema ("Transcript",
                "studId crsCode semester grade",
                "Integer String String String",
                "studId crsCode semester",
                new String [][] {{ "studId", "Student", "id"},
                        { "crsCode", "Course", "crsCode" },
                        { "crsCode semester", "Teaching", "crsCode semester" }});





        String [] tableStudent = { "Student"};

        int[] size = new int[] {150};

        for(int s = 0; s < size.length; s++) {

            int tup[] = new int[]{size[s]};

            Comparable[][][] resultTestSelect = test.generate(tup);
            Table studentTable = new Table("Student_Table", "id name address status", "Integer String String String", "id");

            //    Table StudentTable100, StudentTable200, StudentTable500, StudentTable1000, StudentTable2000, StudentTable5000, StudentTable10000, StudentTable50000;


            for (int i = 0; i < resultTestSelect.length; i++) {
                for (int j = 0; j < resultTestSelect[i].length; j++) {
                    studentTable.insert(resultTestSelect[0][j]);
                } // for
            } // for


            //--------------------- select: <

            out.println( "Point "+ (s+1) + " Number of tuples :" + size[s]);
            out.println();
            for (int i = 0; i < 13; i++) {
                Long time_start = System.currentTimeMillis();
                Table t_select2 = studentTable.select(t -> (Integer) t[studentTable.col("id")] >= 2000 && (Integer) t[studentTable.col("id")] <= 20000);

                Long time_end = System.currentTimeMillis();
                Long time_taken = time_end - time_start;
                //t_select2.print ();
                //t_select2.printIndex ();

                //System.out.println("Time taken for the select query: " +i+ "th time is " + time_taken);
                System.out.println("Sample " + (i+1) + " takes " + time_taken + " msecs.");
            }//for
        }//for




    } // main

} // TestTupleGenerator
