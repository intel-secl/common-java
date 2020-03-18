/*
 * Copyright (C) 2020 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
 
package com.intel.mtwilson.util.crypto.key2;

import com.intel.mtwilson.util.crypto.key2.CipherKey;

public class KMIPCipherKey extends CipherKey {
    private String kmipKeyUUID;
    public void setKMIPKeyUUID(String kmipKeyUUID){
        this.kmipKeyUUID = kmipKeyUUID;
    }

    public String getKmipKeyUUID(){
        return this.kmipKeyUUID;
    }
}
