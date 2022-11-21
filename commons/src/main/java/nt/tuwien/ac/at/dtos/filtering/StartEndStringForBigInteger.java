package nt.tuwien.ac.at.dtos.filtering;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import nt.tuwien.ac.at.exceptions.FilterValidationException;

@Data
@Builder
@AllArgsConstructor
public class StartEndStringForBigInteger {
    private String start;
    private String end;

    public static StartEndStringForBigInteger getStartEndString(String filterValue) throws FilterValidationException {
        if(filterValue == null || filterValue.isEmpty()) {
            return new StartEndStringForBigInteger(null, null);
        }

        if(!filterValue.matches("^-?[0-9]+(--?[0-9]+)?$")) {
            throw new FilterValidationException();
        }

        String value = filterValue;

        String factorStart = "";
        if(value.startsWith("-")) { //check if first value is negative
            factorStart = "-";
            value = value.substring(1);
        }

        int indexTill= value.indexOf('-'); // find minus
        if(indexTill < 0) {
            return new StartEndStringForBigInteger(factorStart+ value, null);
        } else {
            String startValueString = value.substring(0, indexTill);
            value = value.substring(indexTill+1);

            return new StartEndStringForBigInteger(factorStart + startValueString, value); //endValueString is remaining value
        }
    }
}
