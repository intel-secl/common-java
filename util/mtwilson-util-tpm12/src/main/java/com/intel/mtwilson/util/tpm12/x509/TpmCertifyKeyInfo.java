/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.tpm12.x509;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;

/**
 * OID: 2.5.4.789.2
 * @author jbuhacoff
 */
public class TpmCertifyKeyInfo extends ASN1Object {
    public final static String OID = "2.5.4.133.3.2.41";
    private DEROctetString bytes;
    
    public TpmCertifyKeyInfo(byte[] bytes) {
        this.bytes = new DEROctetString(bytes);
    }
    
    public static TpmCertifyKeyInfo valueOf(byte[] asn1) throws IOException {
        ASN1Object object = ASN1Primitive.fromByteArray(asn1);
        DEROctetString der = (DEROctetString)object.toASN1Primitive();
        return new TpmCertifyKeyInfo(der.getOctets());
    }
    
    public byte[] getBytes() {
        return bytes.getOctets();
    }
    
    @Override
    public ASN1Primitive toASN1Primitive() {
        return bytes;
    }
    
}
