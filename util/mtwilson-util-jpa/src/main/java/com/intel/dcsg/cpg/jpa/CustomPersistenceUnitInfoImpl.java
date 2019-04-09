/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.jpa;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

/**
 * This class represents the contents of persistence.xml
 * @author jbuhacoff
 */
public class CustomPersistenceUnitInfoImpl implements PersistenceUnitInfo {
    protected URL url;
    protected DataSource ds;
    protected Properties jpaProperties;
    protected String persistenceUnitName; // ex: ASDataPU
    protected String persistenceUnitProvider; // ex: org.eclipse.persistence.jpa.PersistenceProvider
    protected String transactionType; // ex: RESOURCE_LOCAL, JTA (enum PersistenceUnitTransactionType)
    protected List<String> classList; // ex: com.mtwilson.as.data.MwCertificate, com.mtwilson.as.data.MwOem

    @Override
    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    @Override
    public String getPersistenceProviderClassName() {
        return persistenceUnitProvider;
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return PersistenceUnitTransactionType.valueOf(transactionType);
    }
    
    @Override
    public DataSource getJtaDataSource() {
        return ds;
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return ds;
    }

    @Override
    public List<String> getMappingFileNames() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<URL> getJarFileUrls() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        if( url != null ) { return url; }
        try {
            return new URL("http://localhost");
        }
        catch(MalformedURLException e) {
            throw new IllegalArgumentException("Invalid persistence unit root url: "+e.getLocalizedMessage());
        }
    }

    @Override
    public List<String> getManagedClassNames() {
        return classList;
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return true;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return SharedCacheMode.NONE;
    }

    @Override
    public ValidationMode getValidationMode() {
        return ValidationMode.NONE;
    }

    @Override
    public Properties getProperties() {
        return jpaProperties;
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        return "2.0";
    }

    @Override
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }

    /**
     * XXX currently we do not support this feature; our usage of EclipseLink
     * appears to be working well without it. 
     * @param ct 
     */
    @Override
    public void addTransformer(ClassTransformer ct) {
    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        return getClass().getClassLoader();
    }
    
}
