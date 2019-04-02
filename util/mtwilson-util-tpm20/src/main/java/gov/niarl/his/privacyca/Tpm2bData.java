/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package gov.niarl.his.privacyca;

import java.io.ByteArrayInputStream;

/**
 *
 * @author zaaquino
 */
public class Tpm2bData {

    private short size;
    byte[] buffer;
    /*============================================================================
     Parameter                       Type                    Description
     size                            UINT16                  size in octets of the buffer field; may be 0
     buffer[size]{:sizeof(TPMT_HA)}  BYTE                    the buffer area that contains the algorithm ID and the digest 
     ============================================================================*/

    public Tpm2bData(ByteArrayInputStream source)
            throws TpmUtils.TpmUnsignedConversionException,
            TpmUtils.TpmBytestreamResouceException {
        size = TpmUtils.getUINT16(source);
        buffer = TpmUtils.getBytes(source, (int) size);
    }
}
