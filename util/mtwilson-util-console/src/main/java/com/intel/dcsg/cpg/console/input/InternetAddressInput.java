/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.console.input;

import com.intel.dcsg.cpg.net.InternetAddress;
import com.intel.dcsg.cpg.validation.InputModel;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author jbuhacoff
 */
public class InternetAddressInput extends InputModel<InternetAddress> {

    @Override
    protected InternetAddress convert(String input) {
        if( input.isEmpty() ) { return null; }
        try {
            InternetAddress address = new InternetAddress(input);
            if( address.isValid() ) {
                InetAddress inet = InetAddress.getByName(address.toString());
                if( inet.isReachable(5000) ) {
                    return address;
                }
                else {
                    fault("Not reachable: %s", input);
                }
            }
            fault("Unrecognized internet address: %s", input);
        }
        catch(UnknownHostException e) {
            fault(e, "Unknown host: %s", input);
        }
        catch(IOException e) {
            fault(e, "Network error: %s", input);
        }
        return null;
    }
    

}
