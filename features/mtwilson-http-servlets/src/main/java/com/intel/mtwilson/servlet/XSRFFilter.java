/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.servlet;

import com.intel.dcsg.cpg.validation.RegexPatterns;
import static com.intel.mtwilson.configuration.ConfigurationFactory.getConfiguration;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author anjani
 */
public class XSRFFilter implements Filter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(XSRFFilter.class);
    private String[] targetOrigin;
    private static final String JETTY_TLS_CERT_IP = "jetty.tls.cert.ip";
    private static final String JETTY_PORT = "jetty.secure.port";
    private static final String PROT_IP_PORT_REGEX = "(?:(https://)" + "(" + RegexPatterns.IPADDR_FQDN + "(:\\d*)))";
    private static final String IP_PORT_REGEX = "(" + RegexPatterns.IPADDR_FQDN + "(:\\d*))";

    @Override
    public void init(FilterConfig fc) throws ServletException {
        //To easier the configuration, we load the target expected origin from an Component's JETTY TLS property 
        //Reconfiguration only require an application restart that is generally acceptable 
        try {

            //load server ips
            this.targetOrigin = getConfiguration().get(JETTY_TLS_CERT_IP, "").split("[,]");

        } catch (MalformedURLException e) {
            LOG.error("Cannot init the filter ", e);
            throw new ServletException(e);
        } catch (IOException e) {
            LOG.error("Cannot init the filter ", e);
            throw new ServletException(e);
        }
        LOG.info("XSRFValidationFilter: Filter init, set expected target origin to {}", Arrays.toString(this.targetOrigin));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String fault;

        String remote = request.getRemoteAddr();
        LOG.debug("Remote address {}", remote);

        /* Verifying Same Origin with Standard Headers 
            //Try to get the source from the "Origin" header
            The origin information will be sent by the browser. If the browser supports
            `Origin` we need to use that. Otherwise, look for `Referer`. 
         */
        String source = httpRequest.getHeader("Origin");
        if (this.isBlank(source)) {
            //If empty then fallback on "Referer" header
            /*When checking the `Referer`header, 
                strip off everything after the scheme, host, and port in order to
                obtain a value similar to that of the `Origin` header.
             */
            source = httpRequest.getHeader("Referer");
            //If this one is empty too then we trace the event and we block the request
            if (this.isBlank(source)) {
                fault = "CSRFValidationFilter: ORIGIN and REFERER request headers are both absent/empty request is BLOCKED!";
                LOG.warn(fault);
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        //Compare the source against the expected target origin
        /*
        The value to compare the Origin header to, in order of priority:

            1. server-side configuration (same value that is in the server's TLS certificate as common name)
            2. `X-Forwarded-Host` header value (would be set by a proxy)
            3. `Host` header value (will match when there is no proxy, will not match when
            there is a proxy).

         */
        URL sourceURL = new URL(source);
        String origin = getEndpoint(sourceURL.getHost(), sourceURL.getProtocol(), String.valueOf(sourceURL.getPort()));
        sourceURL = new URL(origin);

        boolean match = false;

        //match with server side listioning ip
        //if matches any one of targetOrigin its a valid request
        for (String ip : targetOrigin) {
            if (ip.equals(sourceURL.getHost())) {
                match = true;
                break;
            }
        }
        if (!match) {
            fault = String.format("CSRFValidationFilter: Host do not fully matches so request BLOCKED! (%s != %s) ",
                    Arrays.toString(this.targetOrigin), sourceURL);
            LOG.warn(fault);
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        //One the part do not match so we trace the event and we block the request, port 443 will not show up in request
        if (!request.getScheme().equals(sourceURL.getProtocol()) || !request.getLocalAddr().equals(sourceURL.getHost())
                || request.getLocalPort() != sourceURL.getPort()) {
            fault = String.format("CSRFValidationFilter: Protocol/Host/Port do not fully matches so request BLOCKED! (%s != %s) ",
                    request.getLocalAddr(), sourceURL);
            LOG.warn(fault);
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;

        }
        if (!isPortValid(origin)) {
            fault = String.format("CSRFValidationFilter:Port is invalid, so request BLOCKED!");
            LOG.warn(fault);
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
        }

        String host = httpRequest.getHeader("Host");
        if (this.isBlank(host)) {
            fault = String.format("CSRFValidationFilter: Host do not fully matches so request BLOCKED! (%s != %s) ", Arrays.toString(this.targetOrigin), host);
            LOG.warn(fault);
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } else {
            String forwardedHost = httpRequest.getHeader("X-Forwarded-Host"); //would be set by a proxy
            //URL hostURL = new URL(host);
            if (this.isBlank(forwardedHost)) {
                //One the part do not match so we trace the event and we block the request
                //Pattern pattern = Pattern.compile("(https?://)(RegexPatterns.IPADDRESS)(:RegexPatterns.PORT)?(.*)?");
                Pattern pattern = Pattern.compile(PROT_IP_PORT_REGEX);
                Matcher matcher = pattern.matcher(host);
                String originHost;

                if (matcher.find()) { // has protocol, domain and port
                    originHost = getDefaultEndpoint(matcher.group(2), matcher.group(1));
                } else {
                    pattern = Pattern.compile(IP_PORT_REGEX);
                    matcher = pattern.matcher(host);
                    if (matcher.find()) { //has only domain and port
                        originHost = getDefaultEndpoint(matcher.group(1), request.getScheme());
                    } else {
                        originHost = getEndpoint(host, "", "");
                    }
                }
                URL hostURL = new URL(originHost);
                if (!sourceURL.getHost().equals(hostURL.getHost()) || sourceURL.getPort() != hostURL.getPort()) {
                    fault = String.format("CSRFValidationFilter: Protocol/Host/Port do not fully matches so request BLOCKED! (%s != %s) ", Arrays.toString(this.targetOrigin), host);
                    LOG.warn(fault);
                    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            } else {
                Pattern pattern = Pattern.compile(PROT_IP_PORT_REGEX);
                Matcher matcher = pattern.matcher(forwardedHost);
                String originHost;

                if (matcher.find()) { // has protocol, domain and port
                    originHost = getDefaultEndpoint(matcher.group(2), matcher.group(1));
                } else {
                    pattern = Pattern.compile(IP_PORT_REGEX);
                    matcher = pattern.matcher(host);
                    if (matcher.find()) { //has only domain and port
                        originHost = getDefaultEndpoint(matcher.group(1), request.getScheme());
                    } else {
                        originHost = getEndpoint(host, "", "");
                    }
                }
                URL hostURL = new URL(originHost);
                if (!sourceURL.getHost().equals(hostURL.getHost()) || sourceURL.getPort() != hostURL.getPort()) {
                    //One the part do not match so we trace the event and we block the request
                    fault = String.format("CSRFValidationFilter: Forwarded Protocol/Host/Port do not fully matches so request BLOCKED! (%s != %s) ", Arrays.toString(this.targetOrigin), forwardedHost);
                    LOG.warn(fault);
                    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    private String getEndpoint(String endpointHost, String protocol, String port) throws IOException {
        String jettyPort = getConfiguration().get(JETTY_PORT, "");
        switch (jettyPort) {
            case "80":
                return String.format("%s://%s:%s", "http", endpointHost, jettyPort);
            case "443":
                return String.format("%s://%s:%s", "https", endpointHost, jettyPort);
            default:
                return String.format("%s://%s:%s", protocol, endpointHost, port);
        }
    }
    private String getDefaultEndpoint(String endpointHost, String protocol) {
        return String.format("%s://%s", protocol, endpointHost);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
    private  int getPort(String urlPart){
        int portNum=0;
        String[] urlSplit=urlPart.split(":");
        if(urlSplit[2]!=null){
             portNum=Integer.valueOf(urlSplit[2]);
        }
        return portNum;
    }
    
    private boolean isPortValid(String urlPart) {
        boolean isValid;
        int portNum = getPort(urlPart);
        /**Checking port number is in range [0,65535] **/
        if (portNum <= 0 || portNum > 65535) {
            isValid = false;
            LOG.error("CSRFValidationFilter:Port is invalid :-" + portNum);
        } else {
            isValid = true;
        }
        return isValid;
    }


    @Override
    public void destroy() {
        LOG.debug("destroy");
    }

}
