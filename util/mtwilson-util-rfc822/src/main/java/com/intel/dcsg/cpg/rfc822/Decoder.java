/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.rfc822;

import java.io.IOException;

/**
 *
 * @author jbuhacoff
 */
public interface Decoder {
    byte[] decode(byte[] encoded) throws IOException;
}
