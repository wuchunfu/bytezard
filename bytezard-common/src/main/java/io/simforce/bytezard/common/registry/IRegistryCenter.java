package io.simforce.bytezard.common.registry;

import java.util.Set;

/**
 * @author zixi0825
 */
public interface IRegistryCenter {

    /**
     * 获取所有的Master Node节点
     * @return
     */
    Set<String> getAllMasterNodes();

    String getActiveMaster(String path);

    String getActiveMaster();

}
