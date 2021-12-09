package io.simforce.bytezard.function.api;

import io.simforce.bytezard.common.entity.ExecuteSql;
import io.simforce.bytezard.common.spi.SPI;

@SPI
public interface SqlFunction {

    String getName();

    String getType();

    ExecuteSql getInvalidateItems();

    ExecuteSql getActualValue();
}
