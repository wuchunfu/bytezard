package io.simforce.bytezard.metadata.repository.service;

import java.util.List;

import io.simforce.bytezard.metadata.model.instance.Entity;

public interface QueryService {

    //根据guid查询到实体
    Entity queryByGuid(String guid);

    //根据guid查询该实体的关联实体，可以指定关联关系的类型
    List<Entity> queryRelationShip(String guid, String relationShipType);
    
    //搜索的时候必须指定类型，同时可以添加classification,Tag,Term等条件
    List<Entity> searchEntity(String type, List<String> classification);
}
