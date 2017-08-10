/*                     __                                               *\
**     ________ ___   / /  ___     content-diff                         **
**    / __/ __// _ | / /  / _ |    (c) 2017                             **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package org.content.diff;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO used for conversion to JSON object representing difference between left and right content with same ID.
 */
public class DiffsJson {

    /**
     * Difference type.
     **/
    private final String diffResultType;

    /**
     * List of differences.
     */
    private final List<DiffJson> diffs;

    /**
     * Empty constructor.
     */
    public DiffsJson() {
        diffResultType = null;
        diffs = new ArrayList<>();
    }

    /**
     * Creates new Differences JSON object.
     *
     * @param diffResultType Difference type.
     * @param diffs          List of differences.
     */
    public DiffsJson(String diffResultType, List<DiffJson> diffs) {
        this.diffResultType = diffResultType;
        this.diffs = diffs;
    }

    /**
     * Returns difference type.
     *
     * @return difference type.
     */
    public String getDiffResultType() {
        return diffResultType;
    }

    /**
     * Returns list of differences.
     *
     * @return differences.
     */
    public List<DiffJson> getDiffs() {
        return diffs;
    }

}
