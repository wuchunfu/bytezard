package io.simforce.bytezard.coordinator.repository.module;

import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.TypeHandler;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.guice.MyBatisModule;

import io.simforce.bytezard.coordinator.eunms.BaseEnumTypeHandler;
import io.simforce.bytezard.coordinator.repository.provider.DruidDataSourceProvider;
import io.simforce.bytezard.coordinator.repository.service.*;
import io.simforce.bytezard.coordinator.repository.service.impl.*;
import io.simforce.bytezard.coordinator.utils.PropertyUtils;

import com.github.pagehelper.PageInterceptor;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * @author zixi0825
 */
public class BytezardCoordinatorModule extends AbstractModule {

    @Override
    protected void configure() {
        Names.bindProperties(binder(), PropertyUtils.getProperties());
        bind(ExecutionJobService.class).to(ExecutionJobServiceImpl.class);
        bind(CommandService.class).to(CommandServiceImpl.class);
        bind(JobService.class).to(JobServiceImpl.class);
        bind(TaskService.class).to(TaskServiceImpl.class);
        bind(ProjectService.class).to(ProjectServiceImpl.class);
        this.install(new MyBatisModule() {
            @Override
            protected void initialize() {
                bindDataSourceProviderType(DruidDataSourceProvider.class);
                bindTransactionFactoryType(JdbcTransactionFactory.class);
                addMapperClasses("io.simforce.bytezard.coordinator.repository.mapper");
                addInterceptorClass(PageInterceptor.class);
                List<Class<? extends TypeHandler<?>>> typeHandlerList = new ArrayList<>();
                typeHandlerList.add(BaseEnumTypeHandler.class);
                addTypeHandlersClasses(typeHandlerList);
            }
        });
    }
}