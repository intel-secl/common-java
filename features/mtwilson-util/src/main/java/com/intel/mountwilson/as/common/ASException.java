/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mountwilson.as.common;


import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.util.MWException;
/**
 *
 * @author dsmagadx
 */
public class ASException extends MWException {
    private String message;
    public ASException(ErrorCode errorCode, Object... params) {
    	super(errorCode,params);
        message = String.format(errorCode.getMessage(), params);
    }
    public ASException(Throwable e,ErrorCode errorCode,Object... params  ){
        super(e,errorCode,params);
        message = String.format("%s: "+errorCode.getMessage(),e.toString(), params);
    }
    
    public ASException(Throwable e){
        super(e);
        message = e.toString();
    }
    
    public ASException(ErrorCode errorCode){
        super(errorCode);
        message = errorCode.getMessage();
    }
    
    @Override
    public String toString() {
        return message;
    }
}
