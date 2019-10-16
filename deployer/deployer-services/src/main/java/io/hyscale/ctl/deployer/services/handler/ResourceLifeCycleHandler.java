package io.hyscale.ctl.deployer.services.handler;

import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.logger.ActivityContext;
import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.commons.utils.ThreadPoolUtil;
import io.hyscale.ctl.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.ctl.deployer.services.model.ResourceStatus;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.models.V1DeleteOptions;

/**
 * Behaviour: Throws wrapped exception
 */
public interface ResourceLifeCycleHandler<T> {

    public static final String TRUE = "true";
    public static final long DELETE_SLEEP_INTERVAL_IN_MILLIS = 3000;
    public static final long MAX_WAIT_TIME_IN_MILLISECONDS = 120000;

    static final Gson gson = new Gson();

    public T create(ApiClient apiClient, T resource, String namespace) throws HyscaleException;

    public boolean update(ApiClient apiClient, T resource, String namespace) throws HyscaleException;

    public T get(ApiClient apiClient, String name, String namespace) throws HyscaleException;

    public List<T> getBySelector(ApiClient apiClient, String selector, boolean label, String namespace)
            throws HyscaleException;

    public boolean patch(ApiClient apiClient, String name, String namespace, T body) throws HyscaleException;

    public boolean delete(ApiClient apiClient, String name, String namespace, boolean wait) throws HyscaleException;

    public boolean deleteBySelector(ApiClient apiClient, String selector, boolean label, String namespace, boolean wait)
            throws HyscaleException;

    public String getKind();

    public int getWeight();

    public boolean cleanUp();

    default V1DeleteOptions getDeleteOptions() {
        V1DeleteOptions deleteOptions = new V1DeleteOptions();
        deleteOptions.setKind("DeleteOptions");
        deleteOptions.setApiVersion("v1");
        deleteOptions.setPropagationPolicy("Foreground");
        return deleteOptions;
    }

    // TODO add events for every resource
    // TODO Changes to support name, field and label selectors
    default void waitForResourceDeletion(ApiClient apiClient, List<String> pendingResources, String namespace,
                                         ActivityContext activityContext) throws HyscaleException {
        if (pendingResources == null) {
            return;
        }
        long startTime = System.currentTimeMillis();
        while (!pendingResources.isEmpty()
                && (System.currentTimeMillis() - startTime < MAX_WAIT_TIME_IN_MILLISECONDS)) {
            Iterator<String> deletePendingResourceIterator = pendingResources.iterator();
            WorkflowLogger.continueActivity(activityContext);
            while (deletePendingResourceIterator.hasNext()) {
                String pendingResource = deletePendingResourceIterator.next();
                try {
                    get(apiClient, pendingResource, namespace);
                } catch (HyscaleException e) {
                    if (e.getHyscaleErrorCode().getErrorMessage() == DeployerErrorCodes.RESOURCE_NOT_FOUND
                            .getErrorMessage()) {
                        deletePendingResourceIterator.remove();
                    }
                }
            }
            ThreadPoolUtil.sleepSilently(DELETE_SLEEP_INTERVAL_IN_MILLIS);
        }
        // Fail case
        if (!pendingResources.isEmpty()) {
            throw new HyscaleException(DeployerErrorCodes.FAILED_TO_DELETE_RESOURCE, pendingResources.toString());
        }

    }

    default ResourceStatus status(T liveObject) {
        return ResourceStatus.STABLE;
    }

}
