package gitlet;
import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {

    /** String that represents the contents of the actual file passed in. */
    private byte[] _contents;

    /** String that contains the sha1 code for a blob. */
    private String _blobName;

    /** String that contains the actual name of the passed in file. */
    private String _fileName;


    Blob(File name) {
        _contents = Utils.readContents(name);
        _fileName = name.getName();
        _blobName = Utils.sha1(_contents);
    }


    /** Getter method that returns the name
     * of this file.
     * @return is a string which represents the file name.
     */
    public String getFileName() {
        return this._fileName;
    }

    /** Getter method which returns the
     * sha 1 of this blob.
     * @return - the sha1 of the current blob.
     */
    public String getBlobCode() {
        return this._blobName;
    }


    /** Method that returns the contents of the file
     * in byte[].
     * @return - byte array which represents the contents of the file.
     */
    public byte[] getBlobContents() {
        return this._contents;
    }
}
