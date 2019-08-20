/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.common.utils;

import org.apache.dubbo.common.config.Configuration;
import org.apache.dubbo.common.config.Environment;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public class SerialDetector extends ObjectInputStream {

    private static final Logger logger = LoggerFactory.getLogger(SerialDetector.class);

    private static final BlacklistConfiguration configuration = new BlacklistConfiguration();

    /**
     * Wrapper Constructor.
     *
     * @param inputStream The original InputStream, used by your service to receive serialized objects
     * @throws java.io.IOException   File I/O exception
     * @throws IllegalStateException Invalid configuration exception
     */
    public SerialDetector(final InputStream inputStream) throws IOException {
        super(inputStream);
    }

    @Override
    protected Class<?> resolveClass(final ObjectStreamClass serialInput) throws IOException, ClassNotFoundException {
        // Enforce SerialDetector's blacklist
        Iterable<Pattern> blacklistIterable = configuration.blacklist();
        if (blacklistIterable == null) {
            return super.resolveClass(serialInput);
        }

        for (Pattern blackPattern : blacklistIterable) {
            Matcher blackMatcher = blackPattern.matcher(serialInput.getName());

            if (blackMatcher.find()) {
                if (!configuration.shouldCheck()) {
                    // Reporting mode
                    logger.info(String.format("Blacklist match: '%s'", serialInput.getName()));
                } else {
                    // Blocking mode
                    logger.error(String.format("Blocked by blacklist '%s'. Match found for '%s'", new Object[]{blackPattern.pattern(), serialInput.getName()}));
                    throw new InvalidClassException(serialInput.getName(), "Class blocked from deserialization (blacklist)");
                }
            }
        }

        return super.resolveClass(serialInput);
    }

    static final class BlacklistConfiguration {
        private static final String DUBBO_SECURITY_SERIALIZATION_CHECK = "dubbo.security.serialization.check";
        private static final String DUBBO_SECURITY_SERIALIZATION_BLACKLIST = "dubbo.security.serialization.blacklist";
        private static final String DUBBO_SECURITY_SERIALIZATION_BLACKLIST_FILE = "dubbo.registry.serialization.blacklist.file";

        private boolean check;
        private PatternList blacklistPattern;

        BlacklistConfiguration() {
            try {
                Configuration config = Environment.getInstance().getConfiguration();
                check = Boolean.valueOf(config.getString(DUBBO_SECURITY_SERIALIZATION_CHECK, "false"));
                String blacklist = config.getString(DUBBO_SECURITY_SERIALIZATION_BLACKLIST);
                if (StringUtils.isEmpty(blacklist)) {
                    String blacklistFile = config.getString(DUBBO_SECURITY_SERIALIZATION_BLACKLIST_FILE);
                    if (StringUtils.isNotEmpty(blacklistFile)) {
                        blacklist = loadBlacklistFile(blacklistFile);
                    }
                }
                blacklistPattern = new PatternList(CommonConstants.COMMA_SPLIT_PATTERN.split(blacklist));
            } catch (Throwable t) {
                logger.warn("Failed to initialize the Serialization Security Checker component!", t);
            }
        }

        Iterable<Pattern> blacklist() {
            return blacklistPattern;
        }

        boolean shouldCheck() {
            return check;
        }

        private String loadBlacklistFile(String fileName) {
            StringBuilder blacklist = new StringBuilder();
            InputStream is = null;

            File file = new File(fileName);
            if (file.exists()) {
                try {
                    is = new FileInputStream(fileName);
                } catch (Throwable e) {
                    logger.warn("Failed to load " + fileName + " file " + e.getMessage(), e);
                }
            } else {
                is = this.getClass().getClassLoader().getResourceAsStream(fileName);
            }

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    blacklist.append(line);
                    blacklist.append(",");
                }
            } catch (Throwable e) {
                logger.warn("Failed to read from file " + fileName + e.getMessage(), e);
            }
            return blacklist.toString();
        }
    }

    static final class PatternList implements Iterable<Pattern> {
        private final Pattern[] patterns;

        PatternList(final String... regExps) {

            requireNonNull(regExps, "regExps");

            this.patterns = new Pattern[regExps.length];
            for (int i = 0; i < regExps.length; i++) {
                patterns[i] = Pattern.compile(regExps[i]);
            }
        }

        @Override
        public Iterator<Pattern> iterator() {
            return new Iterator<Pattern>() {
                int index = 0;

                @Override
                public boolean hasNext() {
                    return index < patterns.length;
                }

                @Override
                public Pattern next() {
                    return patterns[index++];
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("remove");
                }
            };
        }

        @Override
        public String toString() {
            return Arrays.toString(patterns);
        }

    }
}