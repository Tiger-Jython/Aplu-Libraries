# sqlite3.py
# Partial port of Python's sqlite3 module. Inspired by Anthony Hendrickson
# Consult PEP 249 -- Python Database API Specificaton v2.0
# Version 1.07, July 23, 2017

from org.sqlite import SQLiteConnection
import sys
from java.io import *
import jarray
from java.lang import Thread

def getDbInfo(database):
    '''
    Returns a dictionary with all table names as key and attributes as value tuple.
    '''
    with connect(database) as con:
        tables = con.showTables()
        if tables == []:
            return {}
        info = {}
        for table in tables:
            cursor = con.cursor()
            sql = "SELECT * FROM " + table
            cursor.execute(sql)
            info[table] = tuple(cursor.getColumnNames())
        return info

def getBytes(filename):
    '''
    Read data from given binary file into a byte buffer (type array.array) and returns the buffer or None, if an error occurs.
    '''
    currentDir = getTigerJythonPath("main")
    filename = currentDir + filename
    try:
        f = File(filename)
        fis = FileInputStream(f)
        buffer = jarray.zeros(1024, "b")
        bos = ByteArrayOutputStream()
        done = False
        while not done:
            len = fis.read(buffer)
            if len == -1:
                done = True
                continue
            bos.write(buffer, 0, len)
        return bos.toByteArray()
    except:
        print "Error in getBytes(): Failed to read file " + filename
        return None

def storeBytes(buffer, filename):
    '''
    Writes data from given byte buffer (type array.array) into the file (using "wb" attribute that replaces existing file).
    '''
    with open(filename, "wb") as f:
        f.write(buffer)
    

def copyFromJar(source, dest):
    '''
    Copies the given source file that resides in the TigerJython JAR distribution to the given destination.
    '''
    out = File(dest)
    url = Thread.currentThread().getContextClassLoader().getResource(source)
    fis = url.openStream()
    writer = BufferedOutputStream(FileOutputStream(out))
    buffer = jarray.zeros(1024, "b")
    done = False
    while not done:
        len = fis.read(buffer)
        if len == -1:
            done = True
            continue
        writer.write(buffer, 0, len)
    writer.close()

def connect(database, autocommit = False):
    '''
    Returns a Connection with the given SQLite database.
    If the database file does not exist, it is created (with size 0).
    The database is openend in non-auto-commit mode and commit() must be used
    to modify the database (not needed for SELECT commands).
    @param database: the path the the database file
    @param autocommit: if True, the database is set to auto-commit mode (default: False)
    '''
    try:
        conn = Connection("", database)
    except Exception, e:
        print e
        raise Error("Error while creating the database connection using database " + database)
    conn.setAutoCommit(autocommit)
    return conn

def connectTJ(database, autocommit = False):
    '''
    Copies the SQLite database from the distribution JAR to the current directory and 
    returns a Connection with the database.
    If the database file does not exist or cannot be opened, None is returned.
    An exiting database file is replaced without a warning.
    The database is openend in non-auto-commit mode and commit() must be used
    to modify the database (not needed for SELECT commands).
    @param database: the name the the database file
    @param autocommit: if True, the database is set to auto-commit mode (default: False)
    '''
    source = "_databases/" + database
    currentdir = getTigerJythonPath("main")
    destination = currentdir + database

    copyFromJar(source, destination)
    return connect(database, autocommit)

# ---------- class Connection -------------
class Connection(SQLiteConnection):
    '''
    A SQLite database connection. 
    Each SQL command is immediatly executed (auto commitment).
    '''
    def __init__(self, a, b):
        SQLiteConnection.__init__(self, a, b) 
        self.a = a
        self.b = b
        self.dbconn = self    

    # to be used when using 'with' statement
    def __enter__(self):
        return self.dbconn

    # to be used when using 'with' statement
    def __exit__(self, exc_type, exc_val, exc_tb):
        self.dbconn.commit()
        self.dbconn.close()

    def cursor(self):
        '''
        Creates and returns a database cursor.
        '''
        return Cursor(self)

    def execute(self, sql):
        '''
        Creates a internal Cursor object and executes the SQL command. Returns the cursor object.
        @param sql: a string with the SQL command
        '''
        return self.cursor().execute(sql)
    
    def showTables(self):
        '''
        Returns list of all table names.
        '''
        c = self.cursor()
        c.execute("SELECT name FROM sqlite_master WHERE type = 'table'")
        result = c.fetchall()
        if result == []:
            return []
        li = []
        for item in result:
            li.append(item[0])
        return li

    def describeTable(self, table):
        '''
        Returns a SQL command string that could be used to create the table; empty string, if the table does not exit.
        '''
        c = self.cursor()
        c.execute("SELECT sql FROM sqlite_master WHERE name = '" + table + "'")
        result = c.fetchall()
        if result != []:
           return result[0][0]
        return ""
    
# ---------- class Cursor -------------
class Cursor:
    '''
    A database cursor to access the database.
    '''
    def __init__(self, connection):
        self._conn   = connection # connection
        self._result = None  # ResultSet after SELECT
        self._rmeta  = None  # ResultSetMetaData (org.sqlite.jdbc4.JDBC4ResultSet)
        self._status = None  # result
        self._stmt   = None  # statement
        ''' 
        Provides a tuple of 7 items, where the first item is the column name.
        (For compatibilty with Python sqlite3 module.)
        '''
        self.description = None

    def execute(self, sql):
        '''
        Executes an SQL command. Returns the cursor object (self).
        @param sql: a string with the SQL command
        '''
        try:
            self._result = None
            self._rmeta  = None
            self._stmt   = self._conn.createStatement()
            self._status = self._stmt.execute(sql)
            count = self._stmt.getUpdateCount()
            # print "count:", count
            # returns -1 if a result set is returned (after a SELECT command)
            # returns 0 for a CREATE, DROP, ... command
            # returns 1 for an INSERT, UPDATE command
            if count == -1: # result set returned
                self._result = self._stmt.getResultSet()
                self._rmeta  = self._result.getMetaData()
                li = []    
                for i in range(1, self._rmeta.getColumnCount() + 1):
                    name = self._rmeta.getColumnName(i)
                    li.append((name, None, None, None, None, None, None))
                    self.description = tuple(li)    
            else:
                self._result = count # just to inform _fetch() that we have no result set
        except:
            raise Error("Error while executing SQL command: " + sql + "\n" + str(sys.exc_value))
        return self
    
    def updateBlob(self, table, where, attribute, buffer):
        '''
        Updates the BLOB field with given attribute in all records of given table that fullfil the where condition.
        buffer contains the binary data (normally returned from the global function getBytes(), type array.array).
        '''
        sql = "UPDATE  " + table + " SET " + attribute + " = (?) WHERE " + where
        pstmt = self._conn.prepareStatement(sql)
        pstmt.setBytes(1, buffer)
        pstmt.executeUpdate()
    
    def getMetaData(self):
        '''
        Returns the cursor's MetaData instance after a SELECT command.
        (ResultSetMetaData from org.sqlite.jdbc4.JDBC4ResultSet)
        '''
        return self._rmeta
    
    def getColumnCount(self):
        '''
        Returns the number of columns.
        '''
        return self._rmeta.getColumnCount()

    def getColumnName(self, i):
        '''
        Returns the name of the column with given index.
        @param i: column index 1..number of columns (inclusive)
        '''
        return self._rmeta.getColumnName(i)
    
    def getColumnNames(self):
        '''
        Returns a list with the names of all columns.
        '''
        li = []
        count = self.getColumnCount()
        for i in range(1, count + 1):
            li.append(self.getColumnName(i))
        return li

    def fetchone(self):
        '''
        Fetches the next record of the query result set or None when no more records are available.
        The cursor points to the next record (or to outside the result set).
        '''
        try:
            return self._fetch("one")
        except:
            return None

    def fetchmany(self, n):
        '''
        Fetches the next set of records of a query result, returning a list. 
        The cursor pointer is advanced by n (or points to outside result set) 
        An empty list is returned when no more records are available.
        For n < 1 an empty list is returned. If n is greater the the number of 
        available records, the list of available records are returned.
        @param n: the number of next records to fetch
        '''
        if n < 1:
            return []
        try:
           return self._fetch("many", n - 1)
        except:
            return []

    def fetchall(self):
        '''
        Fetches all (remaining) records of a query result, returning a list. 
        An empty list is returned when no more records are available. 
        The cursor points to outside the result set.
        '''
        try:
            return self._fetch("all")
        except:
            return []

    def _fetch(self, size, n = 0):
        if self._result is None or self._result.next() == False:
            raise Error("Result set is empty")
        if size == "one":
            value = self._get_row()
        elif size == "many":
            value = [self._get_row()]
            for i in range(n):
                if self._result.next():
                    value.append(self._get_row())
                else:
                    break
        else:
            value = [self._get_row()]
            while self._result.next():
                value.append(self._get_row())
        return value

    def _get_row(self):
        row = list()
        n_columns = self._rmeta.getColumnCount()
        for i in range(1, (n_columns + 1)):
            row.append(self._result.getObject(i))
        return tuple(row)
    
        
# ---------- class Error -------------
class Error(Exception):
    pass
