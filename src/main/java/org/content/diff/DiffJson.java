/*                     __                                               *\
**     ________ ___   / /  ___     content-diff                         **
**    / __/ __// _ | / /  / _ |    (c) 2017                             **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package org.content.diff;

/**
 * POJO used for conversion to JSON object representing single content difference.
 * <p>
 * Difference is defined with offset and length.
 */
public class DiffJson {

    /**
     * Offset.
     */
    private final String offset;

    /**
     * Length
     */
    private final String length;

    /**
     * Empty constructor.
     */
    public DiffJson() {
        offset = null;
        length = null;
    }

    /**
     * Creates new Difference JSON object.
     *
     * @param offset Offset.
     * @param length Length.
     */
    public DiffJson(String offset, String length) {
        this.offset = offset;
        this.length = length;
    }

    /**
     * Returns offset.
     *
     * @return offset.
     */
    public String getOffset() {
        return offset;
    }

    /**
     * Returns length.
     *
     * @return length.
     */
    public String getLength() {
        return length;
    }

}
