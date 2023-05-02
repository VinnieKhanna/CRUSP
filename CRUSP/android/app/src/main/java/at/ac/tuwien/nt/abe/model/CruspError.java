package at.ac.tuwien.nt.abe.model;

public enum CruspError {
    CRUSP_ERROR(1), // default-error
    JNI_ERROR(2), // error when using JNI
    INPUT_ERROR(3), // Error on invalid input
    COMMUNICATION_ERROR(4), // UDP Error, POST Error, HTTP Error
    NO_ERROR(0);

    private int code;

    CruspError(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
