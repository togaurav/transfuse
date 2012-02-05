package org.androidrobotics.processor;

import org.junit.Before;
import org.junit.Test;

import java.security.PrivilegedActionException;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * @author John Ericksen
 */
public class MergerTest {

    private Merger merger;

    public static class MergeableRoot implements Mergable<String> {
        String id;
        String mergeTag;
        String dontMerge;
        @Merge
        String stringValue;
        @Merge
        int intValue;
        @MergeCollection
        Set<SubMergable> subMergables = new HashSet<SubMergable>();

        public MergeableRoot(String id, String dontMerge, String stringValue, int intValue, Set<SubMergable> subMergables, String tag) {
            this.dontMerge = dontMerge;
            this.stringValue = stringValue;
            this.intValue = intValue;
            this.subMergables = subMergables;
            this.id = id;
            this.mergeTag = tag;
        }

        public String getDontMerge() {
            return dontMerge;
        }

        public String getStringValue() {
            return stringValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public Set<SubMergable> getSubMergables() {
            return subMergables;
        }

        @Override
        public String getIdentifier() {
            return id;
        }

        @Override
        public void setMergeTag(String tag) {
            this.mergeTag = tag;
        }

        @Override
        public String getMergeTag() {
            return mergeTag;
        }
    }

    public static class SubMergable implements Mergable<String> {
        @Merge
        String value;

        String dontMergeValue;

        String mergeTag;
        String id;

        public SubMergable(String id, String value, String dontMergeValue, String tag) {
            this.value = value;
            this.dontMergeValue = dontMergeValue;
            this.id = id;
            this.mergeTag = tag;
        }

        public String getValue() {
            return value;
        }

        public String getDontMergeValue() {
            return dontMergeValue;
        }

        @Override
        public String getIdentifier() {
            return id;
        }

        @Override
        public void setMergeTag(String tag) {
            this.mergeTag = tag;
        }

        @Override
        public String getMergeTag() {
            return mergeTag;
        }
    }

    @Before
    public void setup() {
        merger = new Merger();
    }

    @Test
    public void testMerge() throws IllegalAccessException, PrivilegedActionException {
        Set<SubMergable> subMergablesOne = new HashSet<SubMergable>();
        Set<SubMergable> subMergablesTwo = new HashSet<SubMergable>();

        subMergablesOne.add(new SubMergable("1", "five", "six", "tag"));
        subMergablesTwo.add(new SubMergable("1", "seven", "eight", "tag"));

        MergeableRoot one = new MergeableRoot("2", "one", "two", 5, subMergablesOne, "tag");
        MergeableRoot two = new MergeableRoot("2", "three", "four", 6, subMergablesTwo, "tag");

        MergeableRoot merged = merger.merge(one, two);

        assertEquals("one", merged.getDontMerge());
        assertEquals("four", merged.getStringValue());
        assertEquals(6, merged.getIntValue());
        assertEquals(1, merged.getSubMergables().size());
        SubMergable subMergable = merged.getSubMergables().iterator().next();
        assertEquals("seven", subMergable.getValue());
        assertEquals("six", subMergable.getDontMergeValue());
    }

    @Test
    public void testNonMatchingMerge() throws IllegalAccessException, PrivilegedActionException {
        Set<SubMergable> subMergablesOne = new HashSet<SubMergable>();
        Set<SubMergable> subMergablesTwo = new HashSet<SubMergable>();

        subMergablesOne.add(new SubMergable("1", "five", "six", "tag"));
        subMergablesTwo.add(new SubMergable("2", "seven", "eight", "tag"));

        MergeableRoot one = new MergeableRoot("3", "one", "two", 5, subMergablesOne, "tag");
        MergeableRoot two = new MergeableRoot("4", "three", "four", 6, subMergablesTwo, "tag");

        MergeableRoot merged = merger.merge(one, two);

        assertEquals("one", merged.getDontMerge());
        assertEquals("four", merged.getStringValue());
        assertEquals(6, merged.getIntValue());
        assertEquals(1, merged.getSubMergables().size());
        SubMergable subMergable = merged.getSubMergables().iterator().next();
        assertEquals("seven", subMergable.getValue());
        assertEquals("eight", subMergable.getDontMergeValue());
    }

    @Test
    public void testNullMerge() throws IllegalAccessException, PrivilegedActionException {
        Set<SubMergable> subMergablesOne = new HashSet<SubMergable>();
        Set<SubMergable> subMergablesTwo = new HashSet<SubMergable>();

        subMergablesOne.add(new SubMergable("1", null, null, "tag"));
        subMergablesTwo.add(new SubMergable("1", "seven", "eight", "tag"));

        MergeableRoot one = new MergeableRoot("3", "one", "two", 5, subMergablesOne, "tag");
        MergeableRoot two = new MergeableRoot("3", null, null, 6, subMergablesTwo, "tag");

        MergeableRoot merged = merger.merge(one, two);

        assertEquals("one", merged.getDontMerge());
        assertNull(merged.getStringValue());
        assertEquals(6, merged.getIntValue());
        assertEquals(1, merged.getSubMergables().size());
        SubMergable subMergable = merged.getSubMergables().iterator().next();
        assertEquals("seven", subMergable.getValue());
        assertNull(subMergable.getDontMergeValue());
    }

}