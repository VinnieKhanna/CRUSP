package nt.tuwien.ac.at.dtos.filtering;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import nt.tuwien.ac.at.exceptions.FilterValidationException;

@Data
@Builder
@AllArgsConstructor
public class StartEndDouble {
    private Double start;
    private Double end;

    public static StartEndDouble getStartEndDouble(String filterValue) throws FilterValidationException {
        if(filterValue == null || filterValue.isEmpty()) {
            return new StartEndDouble(null, null);
        }

        if(!filterValue.matches("^-?[0-9]+(.[0-9]+)?(--?[0-9]+(.[0-9]+)?)?$")) {
            throw new FilterValidationException();
        }

        String value = filterValue;

        int factorStart = 1;
        if(value.startsWith("-")) { //check if first value is negative
            factorStart = -1;
            value = value.substring(1);
        }

        try {
            int indexTill= value.indexOf('-'); // find minus
            if(indexTill < 0) {
                return new StartEndDouble(Double.parseDouble(value) * factorStart, null);
            } else {
                String startValueString = value.substring(0, indexTill);
                value = value.substring(indexTill+1);

                return new StartEndDouble(Double.parseDouble(startValueString) * factorStart,
                        Double.parseDouble(value)); //endValueString is remaining value
            }
        } catch (NumberFormatException e) {
            throw new FilterValidationException();
        }
    }
}
