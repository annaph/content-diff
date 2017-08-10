/*                     __                                               *\
**     ________ ___   / /  ___     content-diff                         **
**    / __/ __// _ | / /  / _ |    (c) 2017                             **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package org.content.diff;

/**
 * POJO used for conversion to JSON object to put/update left/right content.
 */
public class ContentJson {

    /**
     * Content to put/update
     */
    private final String data;

    /**
     * Empty constructor.
     */
    public ContentJson() {
        data = null;
    }

    /**
     * Creates new Content JSON object.
     *
     * @param data Content to put/update.
     */
    public ContentJson(String data) {
        this.data = data;
    }

    /**
     * Returns content.
     *
     * @return content.
     */
    public String getData() {
        return data;
    }

}
