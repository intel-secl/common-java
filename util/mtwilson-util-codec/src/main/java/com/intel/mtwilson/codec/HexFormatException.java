/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.codec;

/**
 * This descendant of IllegalArgumentException is for classes that work with hex strings and
 * expect their inputs to be valid hex strings. In other words, any user input must
 * already be validated before passing it into such classes, and they cannot recover
 * from an invalid input because they are not operating at a level where they can 
 * request alternative data. In that situation, they throw this HexFormatException.
 * 
 * Classes that handle user input directly should use the Hex class in Apache Commons Codec
 * and handle DecoderExceptions directly.
 * 
 *
 * @since 0.1
 * @author jbuhacoff
 */
public class HexFormatException extends IllegalArgumentException {
    public HexFormatException() {
        super();
    }
    public HexFormatException(Throwable cause) {
        super(cause);
    }
    public HexFormatException(String message, Throwable cause) {
        super(message, cause);
    }
    public HexFormatException(String message) {
        super(message);
    }
    public HexFormatException(String format, Object... args) {
        super(String.format(format, args));
    }
}
