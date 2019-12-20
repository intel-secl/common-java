/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto.key2;

import java.util.Arrays;

/**
 *brief: This class provides support for RSA/EC algotithm.The key attributes + public-private key pai
 *       is stored in the object to be stored in the repository.
 * @author skamal 
 */
public class AsymmetricKey extends CipherKeyAttributes {
    private byte[] privateKey;
    private byte[] publicKey;
    private String curveType;
    

//    @Override
    public void clear() {
        if( privateKey != null ) {
            Arrays.fill(privateKey, (byte)0);
            publicKey = null;
        }
        // cipher attributes
        setAlgorithm(null);
        setKeyId(null);
        setKeyLength(null);
        setMode(null);
        setPaddingMode(null);
    }
    
    /**
     * @return the privateKey , in the format specified by its attributes
     */
    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(byte[] key) {
        this.privateKey = key;
    }
    
    /**
     * @return the publicKey.
     */
    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] key) {
        this.publicKey = key;
    }

    /**
     * @return the curveType.
     */
    public String getCurveType() {
	return curveType;
    }

    public void setCurveType(String curveType) {
	this.curveType = curveType;
    }

    @Override
    public AsymmetricKey copy() {
        AsymmetricKey newInstance = new AsymmetricKey();
        newInstance.copyFrom(this);
        return newInstance;
    }
    
    public void copyFrom(AsymmetricKey source) {
        super.copyFrom(source);
        this.privateKey = source.privateKey;
        this.publicKey = source.publicKey;
    }
}
