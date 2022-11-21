package nt.tuwien.ac.at.dtos.filtering;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import nt.tuwien.ac.at.exceptions.FilterValidationException;

@Data
@Builder
@AllArgsConstructor
public class StartEndFloat {
    private Float start;
    private Float end;

    public static StartEndFloat getStartEndFloat(String filterValue) throws FilterValidationException {
        if(filterValue == null || filterValue.isEmpty()) {
            return new StartEndFloat(null, null);
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
                return new StartEndFloat(Float.parseFloat(value) * factorStart, null);
            } else {
                String startValueString = value.substring(0, indexTill);
                value = value.substring(indexTill+1);

                return new StartEndFloat(Float.parseFloat(startValueString) * factorStart,
                        Float.parseFloat(value)); //endValueString is remaining value
            }
        } catch (NumberFormatException e) {
            throw new FilterValidationException();
        }
    }
}
