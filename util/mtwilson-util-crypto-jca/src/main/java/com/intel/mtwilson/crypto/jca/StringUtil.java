/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.crypto.jca;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;

/**
 * This class provides the string utility functions required by this package. By
 * defining `join` here we can avoid a dependency on commons-lang3 for just one
 * function.
 *
 * @author jbuhacoff
 */
public class StringUtil {

    public static final Charset UTF8 = Charset.forName("UTF-8"); // java runtime guarantees availability of utf-8 so UnsupportedEncodingException will never happen here

    public static String join(Collection<String> collection, String separator) {
        // quick results for null and empty collections
        if (collection == null) {
            return null;
        }
        if (collection.isEmpty()) {
            return "";
        }
        // if there is no separator the join is simple
        if (separator == null || separator.isEmpty()) {
            // compute final length of string
            int size = 0;
            for (String item : collection) {
                if (item != null) {
                    size += item.length();
                }
            }
            // join the items of the collection
            StringBuilder builder = new StringBuilder(size);
            for (String item : collection) {
                if (item != null) {
                    builder.append(item);
                }
            }
            return builder.toString();
        } else {
            // compute final length of string
            int size = 0;
            for (String item : collection) {
                if (item != null) {
                    size += item.length();
                }
            }
            size += separator.length() * (collection.size() - 1);
            // start with the first item, then join the rest with separators
            Iterator<String> it = collection.iterator();
            StringBuilder builder = new StringBuilder(size);
            if (it.hasNext()) {
                String item = it.next();
                if (item != null) {
                    builder.append(item);
                }
            }
            while (it.hasNext()) {
                String item = it.next();
                builder.append(separator);
                if (item != null) {
                    builder.append(item);
                }
            }
            return builder.toString();
        }
    }

}
