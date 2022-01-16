package io.simforce.bytezard.common.parameter;

import java.util.List;

/**
 * @author zixi0825
 */
public interface IParameters {

    /**
     * check parameters
     * @return
     */
    boolean checkParameters();

    /**
     * get resource files
     * @return
     */
    List<ResourceInfo> getResourceFiles();
}
