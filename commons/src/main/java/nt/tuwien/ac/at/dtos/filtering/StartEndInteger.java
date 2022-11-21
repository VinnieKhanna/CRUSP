package nt.tuwien.ac.at.dtos.filtering;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import nt.tuwien.ac.at.exceptions.FilterValidationException;

@Data
@Builder
@AllArgsConstructor
public class StartEndInteger {
    private Integer start;
    private Integer end;

    public static StartEndInteger getStartEndInteger(String filterValue) throws FilterValidationException {
        if(filterValue == null || filterValue.isEmpty()) {
            return new StartEndInteger(null, null);
        }

        if(!filterValue.matches("^-?[0-9]+(--?[0-9]+)?$")) {
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
                return new StartEndInteger(Integer.parseInt(value) * factorStart, null);
            } else {
                String startValueString = value.substring(0, indexTill);
                value = value.substring(indexTill+1);

                return new StartEndInteger(Integer.parseInt(startValueString) * factorStart,
                        Integer.parseInt(value)); //endValueString is remaining value
            }
        } catch (NumberFormatException e) {
            throw new FilterValidationException();
        }
    }
}
