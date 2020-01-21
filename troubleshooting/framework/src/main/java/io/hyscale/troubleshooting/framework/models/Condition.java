package io.hyscale.troubleshooting.framework.models;

import java.util.function.Predicate;

public class Condition<C extends NodeContext> {

    private Predicate<C> predicate;

    public Condition(Predicate<C> predicate) {
        this.predicate = predicate;
    }

    public Predicate<C> getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate<C> predicate) {
        this.predicate = predicate;
    }
}
