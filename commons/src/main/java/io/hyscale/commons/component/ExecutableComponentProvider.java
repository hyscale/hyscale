package io.hyscale.commons.component;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;

public class ExecutableComponentProvider {

	private List<ComponentInvoker> componentInvokers;

	public ExecutableComponentProvider(List<ComponentInvoker> componentInvokers) {
		this.componentInvokers = componentInvokers;
	}

	public List<ComponentInvoker> getComponentInvokers() {
		return Collections.unmodifiableList(componentInvokers);
	}

	@PreDestroy
	protected void cleanUp() {
		if (componentInvokers != null) {
			componentInvokers.clear();
			componentInvokers = null;
		}
	}
}
