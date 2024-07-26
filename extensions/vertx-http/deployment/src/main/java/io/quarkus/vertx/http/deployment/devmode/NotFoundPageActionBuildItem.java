package io.quarkus.vertx.http.deployment.devmode;

import io.quarkus.builder.item.MultiBuildItem;
import io.quarkus.vertx.http.runtime.devmode.NotFoundAction;
import java.util.List;

/**
 * Allows extensions to contribute an action (button) to the 404 page.
 */
final public class NotFoundPageActionBuildItem extends MultiBuildItem {
    private final List<NotFoundAction> actions;

    public NotFoundPageActionBuildItem(String name, String url) {
        this(new NotFoundAction(name, url));
    }
    
    public NotFoundPageActionBuildItem(NotFoundAction notFoundAction) {
        this(List.of(notFoundAction));
    }
    
    public NotFoundPageActionBuildItem(List<NotFoundAction> notFoundActions) {
        this.actions = notFoundActions;
    }

    public List<NotFoundAction> getActions() {
        return actions;
    }
}
