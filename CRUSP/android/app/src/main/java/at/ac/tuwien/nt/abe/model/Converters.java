package at.ac.tuwien.nt.abe.model;

import androidx.room.TypeConverter;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Converters {
    @TypeConverter
    public static BigInteger toBigInteger(String str) {
        if(str == null) {
            return null;
        }

        return new BigInteger(str);
    }

    @TypeConverter
    public static String fromBigInteger(BigInteger number) {
        if(number == null) {
            return null;
        }

        return number.toString();
    }

    @TypeConverter
    public static Integer fromCruspError(CruspError error) {
        if(error == null) {
            return null;
        }

        return error.getCode();
    }

    @TypeConverter
    public static CruspError toCruspError(Integer code) {
        for (CruspError value : CruspError.values()) {
            if(value.getCode() == code) {
                return value;
            }
        }

        return null;
    }

    /**
     * Converts nanoseconds since epoch to milliseconds since epoch and then to a LocalDateTime Object.
     * Looses nanoseconds-accuracy
     * @param nanoSinceEpoch
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(BigInteger nanoSinceEpoch) {
        long startTimeSinceEpochInMillis = nanoSinceEpoch
                .divide(BigInteger.valueOf(1000000))
                .abs()
                .longValue();

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimeSinceEpochInMillis), ZoneId.systemDefault());
    }

}
