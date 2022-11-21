package at.ac.tuwien.nt.abe.viewmodels;

import at.ac.tuwien.nt.abe.model.network.ITelephonyInfo;

public interface TelephonyInfoCallback {
    void execute(ITelephonyInfo result);
}
