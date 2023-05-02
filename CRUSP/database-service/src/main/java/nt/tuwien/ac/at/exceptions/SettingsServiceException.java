package nt.tuwien.ac.at.exceptions;

public class SettingsServiceException extends RuntimeException {
    private Long id;

    public SettingsServiceException(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
