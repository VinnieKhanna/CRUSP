package nt.tuwien.ac.at.exceptions;

public class SettingsNotFoundException extends RuntimeException {
    private Long id;

    public SettingsNotFoundException(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
