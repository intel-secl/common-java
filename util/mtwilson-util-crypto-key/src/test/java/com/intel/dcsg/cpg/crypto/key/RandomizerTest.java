/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.dcsg.cpg.crypto.key;

import com.intel.dcsg.cpg.crypto.key.RandomSource;
import com.intel.dcsg.cpg.crypto.key.RandomUtil;
import java.security.SecureRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class RandomizerTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RandomizerTest.class);


    @Test
    public void testRandom() {
        RandomSource r = new RandomSource();
        log.debug("next(0) = {}", r.nextInt(1));
        log.debug("next(1) = {}", r.nextInt(1));
        log.debug("next(1) = {}", r.nextInt(1));
        log.debug("next(1) = {}", r.nextInt(1));
        log.debug("next(2) = {}", r.nextInt(2));
        log.debug("next(2) = {}", r.nextInt(2));
        log.debug("next(2) = {}", r.nextInt(2));
        log.debug("next(2) = {}", r.nextInt(2));
        log.debug("next(2) = {}", r.nextInt(2));
    }
    
    @Test
    public void testKnownSeed() {
        SecureRandom s = RandomUtil.getSecureRandom();
        s.setSeed(0);
        RandomSource r1 = new RandomSource(s);
        RandomSource r2 = new RandomSource(s);
        assertEquals(r1.nextBoolean(), r2.nextBoolean());
        assertEquals(r1.nextInt(), r2.nextInt()); // sometimes doesn't work...  java.lang.AssertionError: expected:<-659939107> but was:<1200926396>
        assertEquals(r1.nextLong(), r2.nextLong());
    }
    
    
}
