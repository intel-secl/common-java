/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.util;

import com.intel.mtwilson.core.i18n.LocalizationUtil;
import com.intel.mtwilson.jaxrs2.server.Util;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.intel.mtwilson.repository.RepositoryCreateConflictException;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.util.PSQLException;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.stringtemplate.v4.*;

/**
 * If the throwable is localizable, it sets the locale and uses the localized
 * message directly. Otherwise, it attempts to use the throwable's class name as
 * a localization key for a localized message with no parameters. If that
 * doesn't work either, a localized "internal server error" message is returned.
 *
 * @author jbuhacoff
 */
@Provider
public class ThrowableMapper implements ExceptionMapper<Throwable> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ThrowableMapper.class);
    @Context
    protected HttpHeaders headers;
    private List<String> handledExcpetions = new ArrayList<>(Arrays.asList(
            RepositoryException.class.getName(), PSQLException.class.getName(), UnableToExecuteStatementException.class.getName()));

    @Override
    public Response toResponse(Throwable exception) {
        log.trace("ThrowableMapper toResponse", exception);
        Locale locale = Util.getAcceptableLocale(headers.getAcceptableLanguages(), LocalizationUtil.getAvailableLocales());
        log.trace("ThrowableMapper locale: {}", locale.getDisplayName()); // example: English (United States)
        
        String localizedMessage;
        if( exception instanceof MWException ) {
            log.trace("ThrowableMapper exception is a subclass of MWException");
            MWException mwe = (MWException)exception;
            mwe.setLocale(locale); // localizes output of getErrorMessage() below
            localizedMessage = mwe.getErrorMessage(); 
        }        
        else {
            log.trace("ThrowableMapper exception is NOT a subclass of MWException");
            localizedMessage = getLocalizedErrorMessage(exception, locale);
            log.trace("ThrowableMapper getLocalizedErrorMessage return ok");
        }
        
        log.debug("ThrowableMapper localizedMessage: {}", localizedMessage);
        
        int status = 400; // assume bad request unless we find out otherwise
        if( exception instanceof WebApplicationException ) {
            log.trace("ThrowableMapper exception is a subclass of WebApplicationException");
            status = ((WebApplicationException)exception).getResponse().getStatus();
            log.trace("WebApplicationException: {} ({})", exception.getClass().getName(), status);
        }
        else {
            log.trace("ThrowableMapper exception is NOT a subclass of WebApplicationException");
        }

        // Handled exceptions are added in MtWilsonErrors.properties
        for (String exceptionName : handledExcpetions) {
            try {
                if(Class.forName(exceptionName).isInstance(exception)) {
                    status = 500; // initialize to server exception
                    String field = getMissingField(exception);
                    if(!StringUtils.isEmpty(field)) { // if not null constraint thrown means, this field is missing in input
                        status = 400;
                        localizedMessage = getLocalizedErrorMessage(new RepositoryCreateException("Missing field '" + field + "' in the input"), locale);
                    } else if(!(exception instanceof RepositoryCreateConflictException) && exception instanceof RepositoryCreateException) { // RepositoryCreateConflictException has it's message specified
                        localizedMessage = getLocalizedErrorMessage(new RepositoryCreateException(null, exception), locale); // Respond with message specific to RepositoryCreateException
                    } else if (exception instanceof RepositoryInvalidInputException) { // Respond with message specific to RepositoryInvalidInputException
                        localizedMessage = getLocalizedErrorMessage(new RepositoryInvalidInputException(null, exception), locale);
                    } else { // Respond with message specific to handled Exceptions
                        localizedMessage = getLocalizedErrorMessage(exception, locale);
                    }
                    if(exception instanceof RepositoryException) {
                        status = 400; // All of the repository exceptions are related to user input
                    }
                    break;
                }
            } catch (ClassNotFoundException e) {
                log.trace("ThrowableMapper exception is NOT a subclass of {}", exceptionName);
            }
        }
        ResponseBuilder responseBuilder = Response.status(status).type("text/plain").entity(localizedMessage);
        return responseBuilder.build();
    }

    private String getMissingField(Throwable exception) {
        String message = exception.getMessage();
        String field = "";
        if(!StringUtils.isEmpty(message) && message.contains("violates not-null constraint")) {
            Pattern p = Pattern.compile("\"([^\"]*)\"");
            Matcher m = p.matcher(exception.getMessage());
            if(m.find()) {
                field = m.group(1);
            }
        }
        return field;
    }

    public static class CustomStatus implements Response.StatusType {
        private int statusCode;
        private Response.Status.Family family;
        private String reasonPhrase;

        public CustomStatus(int statusCode, String reasonPhrase) {
            this.statusCode = statusCode;
            this.family = Response.Status.Family.familyOf(statusCode);
            this.reasonPhrase = reasonPhrase;
        }
        public CustomStatus(int statusCode, Response.Status.Family family, String reasonPhrase) {
            this.statusCode = statusCode;
            this.family = family;
            this.reasonPhrase = reasonPhrase;
        }
        
        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public Response.Status.Family getFamily() {
            return family;
        }

        @Override
        public String getReasonPhrase() {
            return reasonPhrase;
        }
        
    }
    
    protected String getLocalizedErrorMessage(Throwable exception, Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("MtWilsonErrors", locale);
        log.trace("Message toString with locale: {}", locale.toString());
        log.trace("Message toString loaded resource bundle: {}", bundle.getLocale().toString());
        String key = exception.getClass().getName(); // for example "java.lang.IllegalArgumentException"
        try {
            String pattern = bundle.getString(key); // for example "Illegal argument: <message>" ; throws MissingResourceException
            ST template = new ST(pattern);
            Map<String, Object> sourceAttrs = PropertyUtils.describe(exception);// throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
            for (Map.Entry<String, Object> attr : sourceAttrs.entrySet()) {
                Object value = PropertyUtils.getSimpleProperty(exception, attr.getKey());
                template.add(attr.getKey(), value);
            }
            String result = template.render();
            log.trace("Rendered template: {}", result);
            return result;
        } catch (MissingResourceException e) {
            log.warn("No translation for key {} in bundle {}: {}", e.getKey(), e.getClassName(), e.getLocalizedMessage());
            return getLocalizedErrorMessage(new WebApplicationException(exception.getMessage(), exception), locale);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Cannot describe exception object", e);
            return key;
        }
    }
}
