package gitlet;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collections;
import java.util.List;




public class DoCommands {

    public static void doInit() {
        Stage theStage = new Stage();
        Remove theRemoval = new Remove();
        File gitletDir = new File(".gitlet");
        File stagingDir = new File(".gitlet/staging");
        File objectsDir = new File(".gitlet/objects");
        File branchesDir = new File(".gitlet/branches");
        gitletDir.mkdir();
        stagingDir.mkdir();
        objectsDir.mkdir();
        branchesDir.mkdir();
        Commit firstCommit = new Commit("initial commit", "", true);
        String commitId = "c" + Utils.sha1(Utils.serialize(firstCommit));
        String branchPath = ".gitlet/branches";
        Utils.writeObject(Utils.join(".gitlet/objects", commitId), firstCommit);
        Utils.writeContents(Utils.join(branchPath, "master"), commitId);
        String masterPath = ".gitlet/branches/master";
        Utils.writeContents(Utils.join(".gitlet", "HEAD"), masterPath);
        Utils.writeObject(Utils.join(stagingDir, "stages"), theStage);
        Utils.writeObject(Utils.join(stagingDir, "removal"), theRemoval);
    }


    /** Replacement for the add command which was buggy.
     * Takes in a file, checks to see if that file is in the
     * current commit, if it is then we cannot add it in.
     * If it isnt then we add it into the staging area so that
     * in the commit class it is added to the next commit permananetly.
     * @param fileName --represents the name of the file being passed
     *                 in that we have to add.
     */
    public static void theOtherAdd(String fileName) {
        Stage theStage = Utils.readObject(Utils.join(
                ".gitlet/staging/stages"), Stage.class);
        Remove theRemoval = Utils.readObject(Utils.join(
                ".gitlet/staging/removal"), Remove.class);
        String currCommitPath = Utils.readContentsAsString(Utils.join(
                ".gitlet/HEAD"));
        String currCommitId = Utils.readContentsAsString(Utils.join(
                currCommitPath));
        Commit currCommit = Utils.readObject(Utils.join(
                ".gitlet/objects", currCommitId), Commit.class);
        if (!Utils.join(fileName).exists()) {
            System.out.println("File does not exist.");
            return;
        }
        File potentialAdd = new File(fileName);
        Blob fileBlobbed = new Blob(potentialAdd);
        if (theStage.containsKey(fileName)) {
            String blobSha = theStage.get(fileName);
            if (!blobSha.equals(fileBlobbed.getBlobCode())) {
                theStage.replace(fileName, fileBlobbed.getBlobCode());
                Utils.writeObject(Utils.join(
                        ".gitlet/objects", fileBlobbed.getBlobCode()),
                        fileBlobbed);
            }
        } else {
            theStage.put(fileName, fileBlobbed.getBlobCode());
            Utils.writeObject(Utils.join(
                    ".gitlet/objects", fileBlobbed.getBlobCode()),
                    fileBlobbed);
        }
        if (theRemoval.containsValue(fileName)) {
            if (theStage.containsKey(fileName)) {
                theRemoval.remove(fileName);
            }
        }
        if (currCommit.containsKey(fileName)) {
            String commitsBlobSha = currCommit.get(fileName);
            if (commitsBlobSha.equals(fileBlobbed.getBlobCode())) {
                if (theStage.containsValue(commitsBlobSha)) {
                    theStage.remove(fileName);
                }
            }
        }
        Utils.writeObject(Utils.join(
                ".gitlet/staging/stages"), theStage);
        Utils.writeObject(Utils.join(
                ".gitlet/staging", "removal"), theRemoval);
    }


    /** Add method which takes in a string file name to
     * add, then it puts it in the staging area to
     * be added in the next commit.
     * @param addFile - file name to add into the next commit.
     */
    public static void doAdd(String addFile) {
        Stage theStage = Utils.readObject(Utils.join(
                ".gitlet/staging/stages"), Stage.class);
        Remove theRemoval = Utils.readObject(Utils.join(
                ".gitlet/staging", "removal"), Remove.class);
        if (theRemoval.containsValue(addFile)) {
            theRemoval.remove(addFile);
        }
        String filePassed = addFile;
        File file = new File(filePassed);
        if (file.exists()) {
            Blob fileBlobbed = new Blob(file);
            String blobFileName = fileBlobbed.getFileName();
            String newSha1 = fileBlobbed.getBlobCode();
            if (theStage.containsKey(blobFileName)) {
                String stagedBlobSha = theStage.get(blobFileName);
                if (!stagedBlobSha.equals(newSha1)) {
                    theStage.replace(blobFileName, newSha1);
                    if (!Utils.join(".gitlet/objects", newSha1).exists()) {
                        Utils.writeObject(Utils.join(
                                ".gitlet/objects", newSha1), fileBlobbed);
                    }
                }
            } else {
                theStage.put(blobFileName, newSha1);
                if (!Utils.join(".gitlet/objects",
                        newSha1).exists()) {
                    Utils.writeObject(Utils.join(
                            ".gitlet/objects", newSha1), fileBlobbed);
                }
            }
            String currCommitCodePath = Utils.readContentsAsString(
                    Utils.join(".gitlet/HEAD"));
            String currCommitCode = Utils.readContentsAsString(
                    Utils.join(currCommitCodePath));
            Commit currCommit = Utils.readObject(
                    Utils.join(".gitlet/objects", currCommitCode),
                    Commit.class);
            if (currCommit.containsValue(newSha1)) {
                if (theStage.containsKey(blobFileName)) {
                    theStage.remove(blobFileName);
                }
            }
            Utils.writeObject(Utils.join(
                    ".gitlet/staging", "stages"),
                    theStage);
            Utils.writeObject(Utils.join(
                    ".gitlet/staging", "removal"),
                    theRemoval);

        } else {
            System.out.print("File does not exist.");
            return;
        }
    }


    /** Commit method that takes in an argument args
     * where the first element is the message that
     * is passed in by the user and that is all there
     * is inside of it.
     * @param args - message passed in by the user.
     */
    public static void doCommit(String... args) {
        String message = args[0];
        String commitCodePath = Utils.readContentsAsString(
                Utils.join(".gitlet/HEAD"));
        String parentCode = Utils.readContentsAsString(
                Utils.join(commitCodePath));
        Commit parent = Utils.readObject(Utils.join(
                ".gitlet/objects", parentCode), Commit.class);
        Commit current = new Commit(message, parentCode, "");
        Remove theRemoval = Utils.readObject(
                Utils.join(".gitlet/staging", "removal"), Remove.class);
        Stage theStage = Utils.readObject(
                Utils.join(".gitlet/staging", "stages"), Stage.class);
        if (theStage.getStage().size() == 0
                && theRemoval.getList().size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        Set<String> parentBlobs = parent.getSavedBlobs().keySet();
        for (String blobName : parentBlobs) {
            String blobSha1 = parent.getValue(blobName);
            current.put(blobName, blobSha1);
        }
        Set<String> stageFileNames = theStage.getStage().keySet();
        for (String stagedFile : stageFileNames) {
            if (current.containsKey(stagedFile)) {
                String stagedFileSha1 = theStage.get(stagedFile);
                if (!current.get(stagedFile).equals(stagedFileSha1)) {
                    current.remove(stagedFile);
                    current.put(stagedFile, theStage.get(stagedFile));
                }
            } else {
                current.put(stagedFile, theStage.get(stagedFile));
            }
        }
        theStage.clear();
        for (String toBeRemoved : theRemoval.getList()) {
            if (current.containsKey(toBeRemoved)) {
                current.remove(toBeRemoved);
            }
        }
        theRemoval.clear();
        String newCommitCode = "c" + Utils.sha1(Utils.serialize(current));
        Utils.writeObject(Utils.join(
                ".gitlet/staging", "removal"), theRemoval);
        Utils.writeObject(Utils.join(
                ".gitlet/staging/stages"), theStage);
        Utils.writeObject(Utils.join(
                ".gitlet/objects", newCommitCode), current);
        Utils.writeContents(Utils.join(
                commitCodePath), newCommitCode);
        Utils.writeContents(Utils.join(
                ".gitlet/HEAD"), commitCodePath);
    }

    /** Method that is used to remove files from the current working
     * direcotry. Also removes them from the staging area if need be
     * @param args
     */
    public static void doRemove(String... args) {
        String theFileName = args[1];
        Remove theRemoval = Utils.readObject(Utils.join(
                ".gitlet/staging/removal"), Remove.class);
        String currCommitPath = Utils.readContentsAsString(
                Utils.join(".gitlet/HEAD"));
        String currCommitCode = Utils.readContentsAsString(
                Utils.join(currCommitPath));
        Commit currentCommit = Utils.readObject(Utils.join(
                ".gitlet/objects", currCommitCode), Commit.class);
        Stage theStage = Utils.readObject(Utils.join(
                ".gitlet/staging/stages"), Stage.class);
        boolean anythingRemoved;
        anythingRemoved = false;
        File tempFile = new File(theFileName);
        if (theStage.containsKey(theFileName)
                || currentCommit.containsKey(theFileName)) {
            if (theStage.containsKey(theFileName)) {
                theStage.remove(theFileName);
                anythingRemoved = true;
            }
            if (isTracked(theFileName)) {
                theRemoval.add(theFileName);
                Utils.restrictedDelete(Utils.join(theFileName));
            } else {
                if (!anythingRemoved) {
                    System.out.println("No reason to remove the file.");
                }
            }
        } else {
            System.out.println("No reason to remove the file.");
        }
        Utils.writeObject(Utils.join(
                ".gitlet/staging", "stages"), theStage);
        Utils.writeObject(Utils.join(
                ".gitlet/staging", "removal"), theRemoval);
    }


    /** Helper method to see if a paramater file is tracked.
     * Built off of the idea that a file that is slightly changed
     * is not tracked anymore, that when its contents are changed,
     * it is no longer tracked. If that idea is wrong then this
     * method is wrong.
     * @param name - the name of the file being passed in as a String
     * @return true if the file is being currently being tracked.
     */
    public static boolean isTracked(String name) {
        String currCommitCodePath = Utils.readContentsAsString(
                Utils.join(".gitlet/HEAD"));
        String currCommitCode = Utils.readContentsAsString(
                Utils.join(currCommitCodePath));
        Commit currentCommit = Utils.readObject(Utils.join(
                ".gitlet/objects", currCommitCode), Commit.class);
        if (currentCommit.containsKey(name)) {
            return true;
        }
        return false;
    }

    /** Method that starts off by printing the current commit, and continues
     * to print all of the parents in this branch.
     * First commit will be the closest to the bottom while our most
     * recent commit will be at the top because it was the first printed.
     * Calls on the print method from our Commit class. */
    public static void log() {
        String currCommitCodePath = Utils.readContentsAsString(
                Utils.join(".gitlet/HEAD"));
        String currCommitCode = Utils.readContentsAsString(
                Utils.join(currCommitCodePath));
        String commitPath = ".gitlet/objects";
        while (Utils.join(commitPath, currCommitCode).exists()) {
            File testPath = Utils.join(commitPath, currCommitCode);
            Commit temp = Utils.readObject(testPath, Commit.class);
            System.out.println("===");
            System.out.println("commit " + currCommitCode);
            System.out.println("Date: " + temp.getTimeTester());
            System.out.println(temp.getMessage());
            System.out.println();
            currCommitCode = temp.getParent();
            if (currCommitCode.equals("")) {
                return;
            }
        }
    }


    /** Resets all of the files in one commit to the files
     * in the given commit. It is quite dangerous,
     * almost acts as the checkout branch.
     * @param args
     */
    public static void reset(String... args) {
        String commitId = args[1];
        String objectPath = ".gitlet/objects";
        if (!Utils.join(objectPath, commitId).exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        File cwd = new File(System.getProperty("user.dir"));
        List<String> currentWorkingFiles = Utils.plainFilenamesIn(cwd);
        for (String file : currentWorkingFiles) {
            if (!isTracked(file)) {
                System.out.println(
                        "There is an untracked file in the way; "
                                + "delete it, or add and commit it first.");
            }
        }
        Commit givenCommit = Utils.readObject(
                Utils.join(objectPath, commitId), Commit.class);
        for (String file : currentWorkingFiles) {
            if (!givenCommit.containsKey(file)) {
                Utils.restrictedDelete(file);
            } else {
                String[] arguments = {"checkout", commitId, "--", file};
                checkOut(arguments);
            }
        }
        Stage theStage = Utils.readObject(Utils.join(
                ".gitlet/staging", "stages"),
                Stage.class);
        theStage.clear();
        Utils.writeObject(Utils.join(".gitlet/staging/stages"),
                theStage);
        String otherIdea = ".gitlet/branches/" + commitId + "/";
        Utils.writeContents(Utils.join(otherIdea), commitId);
        Utils.writeContents(Utils.join(".gitlet", "HEAD"),
                ".gitlet/branches/" + commitId);
    }

    /** Method that checks out to either the givnen file of the
     * current commit, the file of a previous commit,
     * or checks out all of the files to a given branch.
     * @param args - a set of either commits, files, or branches. */
    public static void checkOut(String... args) {
        if (args.length == 3) {
            if (args[1].equals("--")) {
                String fileName = args[2];
                File cwdFile = new File(fileName);
                Blob fileBlobbed = new Blob(cwdFile);
                String currentCommitCodePath = Utils.readContentsAsString(
                        Utils.join(".gitlet/HEAD"));
                String currentCommitCode = Utils.readContentsAsString(
                        Utils.join(currentCommitCodePath));
                Commit currCommit = Utils.readObject(
                        Utils.join(".gitlet/objects", currentCommitCode),
                        Commit.class);
                if (currCommit.containsKey(fileName)) {
                    String currBlobId = currCommit.get(fileName);
                    if (!currBlobId.equals(fileBlobbed.getBlobCode())) {
                        Blob b = Utils.readObject(Utils.join(
                                ".gitlet/objects", currBlobId), Blob.class);
                        Utils.writeContents(cwdFile, b.getBlobContents());
                    }
                } else {
                    System.out.println("File does not exist in that commit.");
                    return;
                }
            } else {
                System.out.println("Incorrect Operands.");
                return;
            }
        } else if (args.length == 4) {
            if (args[2].equals("--")) {
                String commitId = args[1];
                String fileName = args[3];
                if (!Utils.join(".gitlet/objects", commitId).exists()) {
                    System.out.println("No commit with that id exists.");
                    return;
                }
                Commit prevCommit = Utils.readObject(Utils.join(
                        ".gitlet/objects", commitId), Commit.class);
                if (!prevCommit.containsKey(fileName)) {
                    System.out.println("File does not exist in that commit.");
                    return;
                }
                String prevBlobSha = prevCommit.get(fileName);
                Blob prevBlob = Utils.readObject(Utils.join(".gitlet/objects",
                        prevBlobSha), Blob.class);
                Utils.writeContents(Utils.join(fileName),
                        prevBlob.getBlobContents());
            } else {
                System.out.println("Incorrect Operands.");
            }
        } else
            if (args.length == 2) {
                if (args[0].equals("checkout")) {
                    bigCheckout(args);
                    return;
                }
            } else {
                System.out.println("Incorrect Operands");
            }
    }

    /** Second checkout that checks out to an entire branch
     * with its paramaters being a branch name.
     * @param args a branch name. */
    public static void bigCheckout(String... args) {
        String loneBranch = args[1];
        String givenBranch = ".gitlet/branches/" + loneBranch;
        String currBranch = Utils.readContentsAsString(
                Utils.join(".gitlet/HEAD"));

        if (!Utils.join(givenBranch).exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        if (givenBranch.equals(currBranch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        String givenCommitCode = Utils.readContentsAsString(
                Utils.join(givenBranch));
        Commit givenCommit = Utils.readObject(Utils.join(
                ".gitlet/objects", givenCommitCode), Commit.class);

        for (String cwdFileName : Utils.plainFilenamesIn(
                System.getProperty("user.dir"))) {
            if (!isTracked(cwdFileName)) {
                System.out.println("There is an untracked file in the way; "
                         + "delete it, or add and commit it first.");
                return;
            } else if (!givenCommit.containsKey(cwdFileName)) {
                Utils.restrictedDelete(Utils.join(
                        System.getProperty("user.dir"), cwdFileName));
            } else {
                String givenBlobCode = givenCommit.get(cwdFileName);
                Blob prevBlob = Utils.readObject(Utils.join(
                        ".gitlet/objects", givenBlobCode), Blob.class);
                Utils.writeContents(Utils.join(
                        System.getProperty("user.dir"), cwdFileName),
                        prevBlob.getBlobContents());
            }
        }
        Utils.writeContents(Utils.join(
                ".gitlet", "HEAD"), givenBranch);
        Stage theStage = Utils.readObject(Utils.join(
                ".gitlet/staging", "stages"), Stage.class);
        theStage.clear();
        Utils.writeObject(Utils.join(
                ".gitlet/staging", "stages"), theStage);
    }


    /** Creates a new branch at the current branches location
     * and does not automaticall switch them.
     * @param args - branch name. */
    public static void branch(String... args) {
        String newBranch = args[1];
        String branchPath = ".gitlet/branches";
        if (Utils.join(branchPath, newBranch).exists()) {
            System.out.println(
                    "A branch with that name already exists");
            return;
        } else {
            String currentCommitCodePath = Utils.readContentsAsString(
                    Utils.join(".gitlet/HEAD"));
            String currentCommitId = Utils.readContentsAsString(
                    Utils.join(currentCommitCodePath));
            Utils.writeContents(Utils.join(branchPath, newBranch),
                    currentCommitId);
        }
    }

    /** Method that removes a given branch without
     * removing the commit that it holds.
     * @param args - the branch name that we are trying to remove.
     */
    public static void rmBranch(String... args) {
        String branchToRemove = args[1];
        String branchPath = ".gitlet/branches";
        String headPath = Utils.readContentsAsString(Utils.join(
                ".gitlet", "HEAD"));
        String givenPath = ".gitlet/branches/" + branchToRemove;
        if (Utils.join(branchPath, branchToRemove).exists()) {
            if (givenPath.equals(headPath)) {
                System.out.println("Cannot remove the current branch.");
            } else {
                Utils.join(branchPath, branchToRemove).delete();
            }
        } else {
            System.out.println(
                    "A branch with that name does not exist.");
        }
    }

    /** Method that gives the status of our
     * current directory and takes in no paramaters. */
    public static void status() {
        String currCommitCodePath = Utils.readContentsAsString(
                Utils.join(".gitlet", "HEAD"));
        String currCommitCode = Utils.readContentsAsString(
                Utils.join(currCommitCodePath));

        List<String> allBranches = Utils.plainFilenamesIn(
                ".gitlet/branches");
        System.out.println("=== Branches ===");
        Collections.sort(allBranches);
        for (String branch : allBranches) {
            String branchPath = ".gitlet/branches/" + branch;
            if (branchPath.equals(currCommitCodePath)) {
                System.out.println("*" + branch);
                System.out.println();
            } else {
                System.out.println(branch);
                System.out.println();
            }
        }
        System.out.println("=== Staged Files ===");
        Stage theStage = Utils.readObject(Utils.join(
                ".gitlet/staging", "stages"),
                Stage.class);
        List<String> files = new ArrayList<String>(
                theStage.getStage().keySet());
        Collections.sort(files);
        for (String fileName : files) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        Remove theRemoval = Utils.readObject(Utils.join(
                ".gitlet/staging", "removal"),
                Remove.class);
        for (String removedFile : theRemoval.getList()) {
            System.out.println(removedFile);
        }
        System.out.println();
        System.out.println(
                "=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
    }


    /** Same thing as log but only for all of the commits. */
    public static void globalLog() {
        String objectPath = ".gitlet/objects";
        List<String> allObjectsFile = Utils.plainFilenamesIn(
                objectPath);
        for (String objectSha : allObjectsFile) {
            if (objectSha.charAt(0) == ('c')) {
                Commit tempCommit = Utils.readObject(
                        Utils.join(objectPath, objectSha),
                        Commit.class);
                System.out.println("===");
                System.out.println("commit " + objectSha);
                System.out.println("Date: " + tempCommit.getTimeTester());
                System.out.println(tempCommit.getMessage());
                System.out.println();
            }
        }
    }

    /** finds all of the commits with whose message is the inputted message. *
     * @param args */
    public static void find(String... args) {
        String message = args[1];
        String objectsPath = ".gitlet/objects";
        int counter = 0;
        List<String> allObjects = Utils.plainFilenamesIn(objectsPath);
        for (String objectShas : allObjects) {
            if (Utils.join(objectsPath, objectShas).exists()) {
                if (objectShas.charAt(0) == 'c') {
                    Commit temp = Utils.readObject(Utils.join(
                            objectsPath, objectShas), Commit.class);
                    if (temp.getMessage().equals(message)) {
                        System.out.println(objectShas);
                        counter += 1;
                    }
                }
            }
        }
        if (counter == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

}
