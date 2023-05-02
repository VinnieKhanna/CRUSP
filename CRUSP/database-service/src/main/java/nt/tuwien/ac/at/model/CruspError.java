package nt.tuwien.ac.at.model;

public enum CruspError {
    CRUSP_ERROR, // default-error
    JNI_ERROR, // error when using JNI
    INPUT_ERROR, // Error on invalid input
    COMMUNICATION_ERROR, // UDP Error, POST Error, HTTP Error
    NO_ERROR;
}
