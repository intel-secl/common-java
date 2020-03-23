/*
 * Copyright (C) 2020 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
 
package com.intel.mtwilson.util.crypto.key2;

import com.intel.mtwilson.util.crypto.key2.CipherKey;

import java.util.Arrays;

public class KMIPCipherKey extends CipherKey {
    private String kmipKeyUUID;

    public void setKMIPKeyUUID(String kmipKeyUUID){
        this.kmipKeyUUID = kmipKeyUUID;
    }

    public String getKmipKeyUUID(){
        return this.kmipKeyUUID;
    }

    public void clear() {
        setAlgorithm(null);
        setKeyId(null);
        setKeyLength(null);
        setMode(null);
        setPaddingMode(null);
        setKMIPKeyUUID(null);
    }
}
