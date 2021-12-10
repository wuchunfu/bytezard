package io.simforce.bytezard.engine.api.env;

import java.util.List;

import io.simforce.bytezard.engine.api.component.Component;

public interface Execution<SR extends Component, TF extends Component, SK extends Component> {
    void execute(List<SR> sources, List<TF> transforms, List<SK> sinks);
}
