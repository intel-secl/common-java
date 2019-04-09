/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.util;


import com.intel.dcsg.cpg.i18n.Localizable;
import com.intel.mtwilson.i18n.ErrorMessage;
import javax.ws.rs.WebApplicationException;
import com.intel.mtwilson.i18n.ErrorCode;
import java.util.Locale;
/**
 *
 * @author dsmagadx
 */
public class MWException extends WebApplicationException implements Localizable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MWException.class);

    private ErrorMessage errorMessage;
    private Object[] parameters;

    private MWException(){
        
    }
        
    public MWException(ErrorCode code, Object... params) {
        super(400);
        errorMessage = new ErrorMessage(code, params);
        parameters = params;
    }

    public MWException(Throwable e,ErrorCode code, Object... params) {
        super(400);
        errorMessage = new ErrorMessage(code, params);
        parameters = params;
    }

    
    public MWException(ErrorCode code){
        super(400);
        errorMessage = new ErrorMessage(code);
        parameters = null;
    }
    
    public MWException(Throwable e){
        super(400);
        errorMessage = new ErrorMessage(ErrorCode.SYSTEM_ERROR, e.getMessage()); 
        parameters = null;
    }
    
    public String getErrorMessage(){
        log.debug("MWException getErrorMesage");
        return errorMessage.toString();
    }
    public ErrorCode getErrorCode(){
        log.debug("MWException getErrorCode");
        return errorMessage.getErrorCode();
    }

    public Object[] getParameters() {
        log.debug("MWException getParameters");
        return parameters;
    }

    @Override
    public void setLocale(Locale locale) {
        log.debug("MWException setLocale");
        errorMessage.setLocale(locale);
    }

    @Override
    public String toString() {
        log.debug("MWException toString: {}", errorMessage.toString());
        return errorMessage.toString();
    }
    
    
}
