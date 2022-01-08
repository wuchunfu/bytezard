package io.simforce.bytezard.coordinator.repository.module;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * MangerInjector
 * @author zixi0825
 */
public class BytezardCoordinatorInjector {

    private static final Injector INJECTOR;

    static {
        List<Module> modules = Lists.newArrayList();
        modules.add(new BytezardCoordinatorModule());
        INJECTOR = Guice.createInjector(modules);
    }

    private BytezardCoordinatorInjector() {}

    public static Injector getInjector() {
        return INJECTOR;
    }
}
