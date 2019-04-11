/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jaxrs2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.io.ByteArray;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * When using Jackson to serialize a Document subclass, the default behavior
 * is to omit null or empty fields. When using Jackson to de-serialize a
 * Document subclass, the default behavior is to ignore unknown fields. 
 * The combination is intended to facilitate backward-compatible future changes
 * in the API.
 * 
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY) // jackson 2.0
@JsonIgnoreProperties(ignoreUnknown=true)
public abstract class Document extends AbstractDocument {
    private URL href;
    private final HashMap<String,Object> meta = new HashMap<>();
    private final HashMap<String,Object> links = new HashMap<>();
    private String etag;
    private Date createdOn; 
    private Date modifiedOn;
    
    public URL getHref() {
        return href;
    }
    
    public void setHref(URL href) {
        this.href = href;
    }
    
    
    public Map<String, Object> getMeta() {
        return meta;
    }

    public Map<String, Object> getLinks() {
        return links;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    

    public String getEtag() {
        if( etag != null ) { return etag; }
        if( modifiedOn != null ) {
            String hex = Long.toHexString(modifiedOn.getTime());
            ByteArray byteArray = ByteArray.fromHex(hex);
            Sha1Digest digest = Sha1Digest.digestOf(byteArray.getBytes());
            return digest.toHexString();
        }
        return null;
    }
}
