package nt.tuwien.ac.at.exceptions;

public class MeasurementNotFoundException extends RuntimeException {
    private Long id;

    public MeasurementNotFoundException(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
