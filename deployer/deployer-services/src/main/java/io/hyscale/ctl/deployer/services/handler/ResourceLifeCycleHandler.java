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
 * Defines operation for K8s resources
 * Each K8s resource has its own implementation
 *
 * @param <T>
 */
public interface ResourceLifeCycleHandler<T> {

    public static final String TRUE = "true";
    public static final long DELETE_SLEEP_INTERVAL_IN_MILLIS = 3000;
    public static final long MAX_WAIT_TIME_IN_MILLISECONDS = 120000;

    static final Gson gson = new Gson();

    /**
     * Create resource on cluster
     *
     * @param apiClient
     * @param resource
     * @param namespace
     * @return resource
     * @throws HyscaleException
     */
    public T create(ApiClient apiClient, T resource, String namespace) throws HyscaleException;

    /**
     * Fetch resource from cluster, if not found create resource
     * On fetched resource populate required fields like resource version and call update
     *
     * @param apiClient
     * @param resource
     * @param namespace
     * @return true if resource updated, else false
     * @throws HyscaleException
     */
    public boolean update(ApiClient apiClient, T resource, String namespace) throws HyscaleException;

    /**
     * Get Cluster resource
     *
     * @param apiClient
     * @param name
     * @param namespace
     * @return resource
     * @throws HyscaleException
     */
    public T get(ApiClient apiClient, String name, String namespace) throws HyscaleException;

    /**
     * @param apiClient
     * @param selector  could be field or label selector
     * @param label     - true if selector is label selector, else field selector
     * @param namespace
     * @return List of resource
     * @throws HyscaleException
     */
    public List<T> getBySelector(ApiClient apiClient, String selector, boolean label, String namespace)
            throws HyscaleException;

    /**
     * Patch resource
     * 1. Fetch resource from cluster, if not found create
     * 2. Resource source formed through deserializing last applied configuration annotation in cluster resource
     * 3. Creates JSON patch from target and source resource
     * 4. Call patch api with the JSON patch
     *
     * @param apiClient
     * @param name
     * @param namespace
     * @param body
     * @return true if patched else false
     * @throws HyscaleException
     */
    public boolean patch(ApiClient apiClient, String name, String namespace, T body) throws HyscaleException;

    /**
     * Delete resource and wait if enabled
     *
     * @param apiClient
     * @param name
     * @param namespace
     * @param wait
     * @return true if resource deleted else false
     * @throws HyscaleException
     */
    public boolean delete(ApiClient apiClient, String name, String namespace, boolean wait) throws HyscaleException;

    /**
     * Get resources based on selector than delete individual resource
     *
     * @param apiClient
     * @param selector  - label selector if label is true else field selector
     * @param label     - whether selector is label or field
     * @param namespace
     * @param wait
     * @return true if all are deleted else false
     * @throws HyscaleException
     */
    public boolean deleteBySelector(ApiClient apiClient, String selector, boolean label, String namespace, boolean wait)
            throws HyscaleException;

    public String getKind();

    /**
     * @return weight of resource
     */
    public int getWeight();

    /**
     * @return true if resource needs to be removed, else false
     */
    public boolean cleanUp();

    /**
     * Required while deleting resource
     *
     * @return K8s delete option
     */
    default V1DeleteOptions getDeleteOptions() {
        V1DeleteOptions deleteOptions = new V1DeleteOptions();
        deleteOptions.setKind("DeleteOptions");
        deleteOptions.setApiVersion("v1");
        deleteOptions.setPropagationPolicy("Foreground");
        return deleteOptions;
    }

    /**
     * Wait until resource is no longer available
     * or timeout, in which case throws exception
     *
     * @param apiClient
     * @param pendingResources
     * @param namespace
     * @param activityContext  for displaying continuation
     * @throws HyscaleException
     */
    default void waitForResourceDeletion(ApiClient apiClient, List<String> pendingResources, String namespace,
                                         ActivityContext activityContext) throws HyscaleException {
        // TODO add events for every resource
        // TODO Changes to support name, field and label selectors
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

    /**
     * Get Status from resource
     *
     * @param liveObject
     * @return {@link ResourceStatus}
     */
    default ResourceStatus status(T liveObject) {
        return ResourceStatus.STABLE;
    }

}
