package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.TreeMap;

public class Commit implements Serializable {

    /** Message that was passed into git commit. */
    private String _message;


    /** String that holds the parent of the current commit,
     * if its the inital commit, will have a null parent
     * this does is the constructor with one parent, another
     * constrcutor with two parents will be made later. */
    private String _parent;


    /** String that holds a second parent if a commit was made
     * from a merge commit. */
    private String _parent2;


    /** String that holds the current date of the commit
     * that we are making. */
    private Date _time;

    /** String that holds the date into a string from date. */
    private String date;

    /** Yet another time variable to see if it works. */
    private String timeTester;

    /** A TreeMap that holds the String blob file name
     * and the blob sha1 name. */
    private TreeMap<String, String> commitSavedBlobs;

    /** String that represents THIS sha1, only should be
     * used in the date format part. */
    private String mySha1;

    /** Simple date formatter, for the commit dates. */
    private SimpleDateFormat dateT = new SimpleDateFormat(
            "EEE MMM d HH:mm:ss yyyy Z");


    /** Constructor that creates new commits.
     * @param message - the message passed into that commit.
     * @param parent - the parent sha1 of the new commit. */
    Commit(String message, String parent) {
        _message = message;
        _parent = parent;
        commitSavedBlobs = new TreeMap<String, String>();
        _parent2 = null;
        _time = new Date();
        timeTester = new SimpleDateFormat("EEE MMM dd"
                + "HH:mm:ss yyyy Z").format(new Date());
        date = dateT.format(_time);
    }

    /** Commit constructor which takes in a message, parent, and a boolean.
     * @param message - passed in message of the commit.
     * @param parent - string of the parents sha1.
     * @param firstCommit - boolean that seperates itself
     *                    from the other constructors. */
    Commit(String message, String parent, boolean firstCommit) {

        _message = message;
        //String newParent = parent;
        _message = message;
        _parent = parent;
        _parent2 = null;
        timeTester = new SimpleDateFormat("EEE MMM dd"
                 + " HH:mm:ss yyyy Z").format(new Date(0));
        _time = new Date(Instant.EPOCH.getEpochSecond());
        commitSavedBlobs = new TreeMap<String, String>();
        date = dateT.format(_time);
    }

    /** Commit constructor which seperates itself from
     * the other commits.
     * @param message - passed in message of user.
     * @param parent - sha1 which represents the parent sha1.
     * @param parent2 - if there is a second parent, their sha1. */
    Commit(String message, String parent, String parent2) {
        _message = message;
        _parent = parent;
        _parent2 = parent2;
        commitSavedBlobs = new TreeMap<String, String>();
        _time = new Date();
        timeTester = timeTester = new SimpleDateFormat("EEE MMM dd"
                + " HH:mm:ss yyyy Z").format(new Date());
        date = dateT.format(_time);
    }

    /**  Method that returns
     * the time of this commit.
     * @return - the timer in a string version.
     */
    public String getTimeTester() {
        return this.timeTester;
    }


    /** Getter method that returns this message
     * of the current commit.
     * @return - string representation of the passed in message.
     */
    public String getMessage() {
        return this._message;
    }

    /** Gets the parent of this
     * commit.
     * @return - String representation of the
     * commits parent sha1.
     */
    public String getParent() {
        return this._parent;
    }

    /**Getter method which returns the second parents sha1.
     * @return - String of the second parents sha1. */
    public String getParent2() {
        return this._parent2;
    }

    /** Method that checks to see if this commit
     * has a parent.
     * @return is a boolean that returns true
     * if this commit does in fact have a parent.
     */
    public boolean hasParent() {
        if (_parent == null) {
            return false;
        }
        return true;
    }

    /** Method which prints out the commit
     * in the form that fits the log and global log. */
    public void print() {
        System.out.println("===");
        System.out.println("commit " + getSha1());
        System.out.println("Date: " + dateT);
        System.out.println(_message);
        System.out.println();
    }

    /**  Methopd that acts as TreeMap put
     * where we can put values.
     * @param key - String file name.
     * @param value - String sha1 file.
     */
    public void put(String key, String value) {
        this.commitSavedBlobs.put(key, value);
    }

    /** Getter method which returns the getValue of the
     * current commit which corresponds to the key.
     * @param key - corresponding key in map.
     * @return - String fila sha1.
     */
    public String getValue(String key) {
        return this.commitSavedBlobs.get(key);
    }

    /** Method that takes in a key and returns whether
     * or not it has that key.
     * @param key - String file name.
     * @return - true if the file exists. */
    public boolean containsKey(String key) {
        return commitSavedBlobs.containsKey(key);
    }

    /** Method which returns true if the value is present in the set
     * which has a corresonding key value.
     * @param value - String sha1 of corresponding key file name.
     * @return returns true if the value is contained in TreeMap.
     */
    public boolean containsValue(String value) {
        return commitSavedBlobs.containsValue(value);
    }

    /** Getter method which gives back the sha1 of a commit
     * if there is one.
     * @return - a string that represents this commits
     * sha1 code of its contents. */
    public String getSha1() {
        mySha1 = Utils.sha1(_message, _time.toString(),
                commitSavedBlobs.toString(), _parent, _parent2);
        return mySha1;
    }

    /** Acts the same as the regular TreeMap get
     * method.
     * @param key - is a String file name.
     * @return - returns a string, file sha1. */
    public String get(String key) {
        return commitSavedBlobs.get(key);
    }

    /** Getter method that returns the inner
     * TreeMap of the commit method.
     * @return - returns the TreeMap which holds
     * the file names and blob shas that this commit is tracking. */
    public TreeMap<String, String> getSavedBlobs() {
        return this.commitSavedBlobs;
    }

    public void remove(String key) {
        this.commitSavedBlobs.remove(key);
    }
}

