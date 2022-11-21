package nt.tuwien.ac.at.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(uniqueConstraints={
        @UniqueConstraint(columnNames = {"repeats", "volume", "packet_size", "rate", "sleep", "timeout"})
})
public class CruspSettings {
    @Id
    @Column(name = "settings_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long uid;

    @NotNull
    private boolean standard; // idendifies if standard/default CruspSettings should be used, ignored when saving or reading settings

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // following fields are the same as in CruspToken
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @NotNull
    @Max(100)
    @Min(1)
    private int repeats;

    @NotNull
    @Max(100000) //max 100 MB
    @Min(1) //min 1 KB
    private int volume; //in KB

    @Column(name = "packet_size")
    @NotNull
    @Max(100000000) //max 100 MB = 100000 KB = 100000000 Byte
    @Min(1)
    private int packetSize; // in Byte

    @NotNull
    @Max(10000) // 10.000 since backbone limitations at hossman.nt.tuwien.ac.at, 10.000 for 5G peak
    @Min(0) // 0 KBit/sec since @Min requires long value
    private float rate; //rate in MBit/sec

    @NotNull
    @Max(5000) // for sleep longer than 5 sec use the continuous measurement feature
    @Min(1)
    private int sleep; // sleep between repeats in millis

    @NotNull
    @Max(5000) // wait a max. of 5 sec
    @Min(1)
    private int timeout; // timeout to wait for eventually lost udp packets before ending the repeat.

    public static CruspSettings from(CruspToken token) {
        CruspSettingsBuilder builder = new CruspSettingsBuilder();
        return builder
                .uid(0)
                .standard(false)
                .packetSize(token.getPacketSize())
                .rate(token.getRate())
                .repeats(token.getRepeats())
                .sleep(token.getSleep())
                .timeout(token.getTimeout())
                .volume(token.getVolume())
                .build();
    }
}
