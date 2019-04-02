/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.ms.common;

import javax.ws.rs.WebApplicationException;

import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.util.MWException;

/**
 *
 * @author dsmagadx
 */
public class MSException extends MWException {

    public MSException(ErrorCode errorCode, Object... params) {
    	super(errorCode,params);
    }
    public MSException(Throwable e,ErrorCode code,Object... msg  ){
        super(e,code,msg);
    }
    
    public MSException(Throwable e){
        super(e);
    }
    
    public MSException(ErrorCode errorCode){
        super(errorCode);
    }

}
