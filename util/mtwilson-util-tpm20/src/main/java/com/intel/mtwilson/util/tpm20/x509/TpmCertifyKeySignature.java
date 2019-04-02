/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util.tpm20.x509;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;

/**
 * OID: 2.5.4.789.2
 * @author jbuhacoff
 */
public class TpmCertifyKeySignature extends ASN1Object {
    public final static String OID = "2.5.4.133.3.2.41.1";
    private DEROctetString bytes;
    
    public TpmCertifyKeySignature(byte[] bytes) {
        this.bytes = new DEROctetString(bytes);
    }
    
    public static TpmCertifyKeySignature valueOf(byte[] asn1) throws IOException {
        ASN1Object object = ASN1Primitive.fromByteArray(asn1);
        DEROctetString der = (DEROctetString)object.toASN1Primitive();
        return new TpmCertifyKeySignature(der.getOctets());
    }
    
    public byte[] getBytes() {
        return bytes.getOctets();
    }
    
    @Override
    public ASN1Primitive toASN1Primitive() {
        return bytes;
    }
    
}
