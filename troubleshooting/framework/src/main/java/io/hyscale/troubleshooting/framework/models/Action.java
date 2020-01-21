package io.hyscale.troubleshooting.framework.models;

public interface Action<R, C extends NodeContext> {

    public R act(C context);

}