/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.rpc.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.rpc.v2.model.Rpc;
import com.intel.mtwilson.rpc.v2.model.RpcPriv;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * Characteristics of a Remote Procedure Call is that the input and output are
 * likely different types, that the client and server may negotiate for the
 * processing to occur immediately (synchronous) or to be queued (asynchronous),
 * that an RPC consists of a single method that is a verb phrase, and that the
 * server is not expected to keep track of past inputs (but it is allowed to
 * store them and index them as well as the outputs, of course) - if any storing
 * or indexing of RPCs takes places, the interface to that stored data would be
 * a resource.
 *
 * In contrast, a resource has one type and typically has several standard
 * operations are defined on that type: create, store (update), retrieve,
 * delete, and search which is defined on the collection of that type; a
 * resource operation typically happens immediately (but the server may still
 * choose to delay it and return an appropriate "accepted" http status code).
 *
 * @author jbuhacoff
 */
@V2
@Path("/rpc-async")
public class AsyncRpc extends AbstractRpc {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AsyncRpc.class);

    public AsyncRpc() {
    }

    @Path("/{name}")
    @POST
    @Consumes(MediaType.WILDCARD)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    public Rpc invokeAsyncRemoteProcedureCall(@PathParam("name") String name, @Context HttpServletRequest request, byte[] input) {
        // make sure we have an extension to handle this rpc
        RpcAdapter adapter = getAdapter(name);
        
        // convert the client's input into our internal format
        Object inputObject = getInput(input, adapter.getInputClass(), request);
        // now serialize the input object with xstream;  even though we're going to process immediately, we are still going to record the call in the RPC table so we need the xml
        byte[] inputXml = toXml(inputObject);



        // prepare the rpc task with the input
        RpcPriv rpc = new RpcPriv();
        rpc.setId(new UUID());
        rpc.setName(name);
        rpc.setInput(inputXml);
        rpc.setStatus(Rpc.Status.QUEUE);

        // store it
        repository.create(rpc);

        Rpc status = new Rpc();
        status.copyFrom(rpc);

        return status;
    }

}
