/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package gov.niarl.his.privacyca;

import gov.niarl.his.privacyca.TpmUtils.TpmBytestreamResouceException;
import gov.niarl.his.privacyca.TpmUtils.TpmUnsignedConversionException;

import java.io.ByteArrayInputStream;

/**
 *
 * @author zaaquino
 */
public class TpmuAttest {
    Tpm2bName tpm2bName;	 /* This corresponds to the TPMS_CERTIFY_INFO struct  */
    /*Following fields are currently ignored
    TpmsCreationInfo creation;	 
    TpmsQuoteInfo quote;	 
    TpmsCommandAuditInfo commandAudit;	 
    TpmsSessionAuditInfo sessionAudit;	 
    TpmsTimeAttestInfo time;	 
    TpmsNvCertifyInfo nv;	 
    */


   public TpmuAttest(ByteArrayInputStream bs) throws TpmBytestreamResouceException, TpmUnsignedConversionException {
        tpm2bName = new Tpm2bName(bs);    
    }
   
   public Tpm2bName getTpmsCertifyInfoBlob(){
       return tpm2bName;
   }
}
