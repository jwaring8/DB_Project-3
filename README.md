# DB_Project-3
Repo for CSCI 4370 Project 3

CSCI X370: Database Management
Spring 2017

       Project 3: B+tree Indexing 
       Due: Mar 19 (11:59 pm)

1.     The project needs to be implemented and tested for creating indexes using 2 data structures: 
a.     Treemap – Java collection
b.     B+tree – custom implementation
2.     Check whether it is behaving the same for both implementations
3.     The resultant tuples generated from the queries must be stored in a file rather than running them on the console.
4.     The save option is already there in the Table.java. You may have to modify it to your own implementation (if necessary). 
5.     For submission, each team should submit two folders with appropriate project structure.
1.     With implementation using Treemap – should include files (Table.java, MovieDB.java etc.) with indexing using Treemap (comment out other indexes that you are not using) 
2.     With implementation using B+tree – same as TreeMap – should include all files but the indexing should use your implementation of B+tree.
The project folder should have 2 sub-folders named as the following: 1. TreeMap-src-files, 2. BPlusTree-src-files. So, each team should be submitting 2 folders. However, the implementation in Table.java will be same for all two apart from modifying the index initialization.
How the project is going to be tested:
1.  The testing will be same as before in Project 1. MovieDB.java which is the entry point for the project will be used for testing.
2.  However, it will be tested again for each of the indexes – (TreeMap, and B+tree) in each of the folders separately.
3.  A written test case document is provided to indicate what all functionalities should be working (more or less same as before as you implemented for Project 1).
4.  You may change the code that you submitted in the Project 1 if you observe that it is not working appropriate for the indexes.
5.  Also, it is suggested to test your files with large number of tuples to ensure that your indexes are working appropriately. 
6.  The current submission files should be working good for at least 100 to 200 tuples to check the effectiveness of indexing.
7.  Previously provided Test tuple generator files can be used for this purpose.

