/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.util;

// Original License terms from the Apache project
/*
 * Copyright 2002-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p>Provides XML entity utilities.</p>
 *
 * This code has been copied from the jakarta commons project from an Apache license.
 * It has been modified to just support XML escaping.
 *
 * @see <a href="http://hotwired.lycos.com/webmonkey/reference/special_characters/">ISO Entities</a>
 *
 * @author <a href="mailto:alex@purpletech.com">Alexander Day Chaffee</a>
 * @author <a href="mailto:ggregory@seagullsw.com">Gary Gregory</a>
 * @author Alan Hudson
 *
 * @version $Revision: 1.3 $
 */
public class XMLTools {

    private static final String[][] BASIC_ARRAY = {
        {"quot", "34"}, // " - double-quote
        {"amp", "38"}, // & - ampersand
        {"lt", "60"}, // < - less-than
        {"gt", "62"}, // > - greater-than
    };

    private static final String[][] APOS_ARRAY = {
        {"apos", "39"}, // XML apostrophe
    };

    /**
     * <p>The set of entities supported by standard XML.</p>
     */
    public static final XMLTools XML;

    static {
        XML = new XMLTools();
        XML.addEntities(BASIC_ARRAY);
        XML.addEntities(APOS_ARRAY);
    }

    static interface EntityMap {
        void add(String name, int value);

        String name(int value);

        int value(String name);
    }

    static class PrimitiveEntityMap implements EntityMap {
        private Map mapNameToValue = new HashMap();
        private IntHashMap mapValueToName = new IntHashMap();

        public void add(String name, int value) {
            mapNameToValue.put(name, new Integer(value));
            mapValueToName.put(value, name);
        }

        public String name(int value) {
            return (String) mapValueToName.get(value);
        }

        public int value(String name) {
            Object value = mapNameToValue.get(name);
            if (value == null) {
                return -1;
            }
            return ((Integer) value).intValue();
        }
    }


    static abstract class MapIntMap implements XMLTools.EntityMap {
        protected Map mapNameToValue;
        protected Map mapValueToName;

        public void add(String name, int value) {
            mapNameToValue.put(name, new Integer(value));
            mapValueToName.put(new Integer(value), name);
        }

        public String name(int value) {
            return (String) mapValueToName.get(new Integer(value));
        }

        public int value(String name) {
            Object value = mapNameToValue.get(name);
            if (value == null) {
                return -1;
            }
            return ((Integer) value).intValue();
        }
    }

    static class HashEntityMap extends MapIntMap {
        public HashEntityMap() {
            mapNameToValue = new HashMap();
            mapValueToName = new HashMap();
        }
    }

    static class TreeEntityMap extends MapIntMap {
        public TreeEntityMap() {
            mapNameToValue = new TreeMap();
            mapValueToName = new TreeMap();
        }
    }

    static class LookupEntityMap extends PrimitiveEntityMap {
        private String[] lookupTable;
        private int LOOKUP_TABLE_SIZE = 256;

        public String name(int value) {
            if (value < LOOKUP_TABLE_SIZE) {
                return lookupTable()[value];
            }
            return super.name(value);
        }

        private String[] lookupTable() {
            if (lookupTable == null) {
                createLookupTable();
            }
            return lookupTable;
        }

        private void createLookupTable() {
            lookupTable = new String[LOOKUP_TABLE_SIZE];
            for (int i = 0; i < LOOKUP_TABLE_SIZE; ++i) {
                lookupTable[i] = super.name(i);
            }
        }
    }

    static class ArrayEntityMap implements EntityMap {
        protected int growBy = 100;
        protected int size = 0;
        protected String[] names;
        protected int[] values;

        public ArrayEntityMap() {
            names = new String[growBy];
            values = new int[growBy];
        }

        public ArrayEntityMap(int growBy) {
            this.growBy = growBy;
            names = new String[growBy];
            values = new int[growBy];
        }

        public void add(String name, int value) {
            ensureCapacity(size + 1);
            names[size] = name;
            values[size] = value;
            size++;
        }

        protected void ensureCapacity(int capacity) {
            if (capacity > names.length) {
                int newSize = Math.max(capacity, size + growBy);
                String[] newNames = new String[newSize];
                System.arraycopy(names, 0, newNames, 0, size);
                names = newNames;
                int[] newValues = new int[newSize];
                System.arraycopy(values, 0, newValues, 0, size);
                values = newValues;
            }
        }

        public String name(int value) {
            for (int i = 0; i < size; ++i) {
                if (values[i] == value) {
                    return names[i];
                }
            }
            return null;
        }

        public int value(String name) {
            for (int i = 0; i < size; ++i) {
                if (names[i].equals(name)) {
                    return values[i];
                }
            }
            return -1;
        }
    }

    static class BinaryEntityMap extends ArrayEntityMap {

        public BinaryEntityMap() {
        }

        public BinaryEntityMap(int growBy) {
            super(growBy);
        }

        // based on code in java.util.Arrays
        private int binarySearch(int key) {
            int low = 0;
            int high = size - 1;

            while (low <= high) {
                int mid = (low + high) >> 1;
                int midVal = values[mid];

                if (midVal < key) {
                    low = mid + 1;
                } else if (midVal > key) {
                    high = mid - 1;
                } else {
                    return mid; // key found
                }
            }
            return -(low + 1);  // key not found.
        }

        public void add(String name, int value) {
            ensureCapacity(size + 1);
            int insertAt = binarySearch(value);
            if (insertAt > 0) {
                return;    // note: this means you can't insert the same value twice
            }
            insertAt = -(insertAt + 1);  // binarySearch returns it negative and off-by-one
            System.arraycopy(values, insertAt, values, insertAt + 1, size - insertAt);
            values[insertAt] = value;
            System.arraycopy(names, insertAt, names, insertAt + 1, size - insertAt);
            names[insertAt] = name;
            size++;
        }

        public String name(int value) {
            int index = binarySearch(value);
            if (index < 0) {
                return null;
            }
            return names[index];
        }
    }

    // package scoped for testing
    EntityMap map = new XMLTools.LookupEntityMap();

    public void addEntities(String[][] entityArray) {
        for (int i = 0; i < entityArray.length; ++i) {
            addEntity(entityArray[i][0], Integer.parseInt(entityArray[i][1]));
        }
    }

    public void addEntity(String name, int value) {
        map.add(name, value);
    }

    public String entityName(int value) {
        return map.name(value);
    }


    public int entityValue(String name) {
        return map.value(name);
    }

    /**
     * <p>Escapes the characters in a <code>String</code>.</p>
     *
     * <p>For example, if you have called addEntity(&quot;foo&quot;, 0xA1),
     * escape(&quot;\u00A1&quot;) will return &quot;&amp;foo;&quot;</p>
     *
     * @param str The <code>String</code> to escape.
     * @return A new escaped <code>String</code>.
     */
    public String escape(String str) {
        //todo: rewrite to use a Writer
        StringBuffer buf = new StringBuffer(str.length() * 2);
        int i;
        for (i = 0; i < str.length(); ++i) {
            char ch = str.charAt(i);
            String entityName = this.entityName(ch);
            if (entityName == null) {
                if (ch > 0x7F) {
                    int intValue = ch;
                    buf.append("&#");
                    buf.append(intValue);
                    buf.append(';');
                } else {
                    buf.append(ch);
                }
            } else {
                buf.append('&');
                buf.append(entityName);
                buf.append(';');
            }
        }
        return buf.toString();
    }

    /**
     * <p>Unescapes the entities in a <code>String</code>.</p>
     *
     * <p>For example, if you have called addEntity(&quot;foo&quot;, 0xA1),
     * unescape(&quot;&amp;foo;&quot;) will return &quot;\u00A1&quot;</p>
     *
     * @param str The <code>String</code> to escape.
     * @return A new escaped <code>String</code>.
     */
    public String unescape(String str) {
        StringBuffer buf = new StringBuffer(str.length());
        int i;
        for (i = 0; i < str.length(); ++i) {
            char ch = str.charAt(i);
            if (ch == '&') {
                int semi = str.indexOf(';', i + 1);
                if (semi == -1) {
                    buf.append(ch);
                    continue;
                }
                String entityName = str.substring(i + 1, semi);
                int entityValue;
                if (entityName.length() == 0) {
                    entityValue = -1;
                } else if (entityName.charAt(0) == '#') {
                    if (entityName.length() == 1) {
                        entityValue = -1;
                    } else {
                        char charAt1 = entityName.charAt(1);
                        try {
                            if (charAt1 == 'x' || charAt1=='X') {
                                entityValue = Integer.valueOf(entityName.substring(2), 16).intValue();
                            } else {
                                entityValue = Integer.parseInt(entityName.substring(1));
                            }
                        } catch (NumberFormatException ex) {
                            entityValue = -1;
                        }
                    }
                } else {
                    entityValue = this.entityValue(entityName);
                }
                if (entityValue == -1) {
                    buf.append('&');
                    buf.append(entityName);
                    buf.append(';');
                } else {
                    buf.append((char) (entityValue));
                }
                i = semi;
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }

}
