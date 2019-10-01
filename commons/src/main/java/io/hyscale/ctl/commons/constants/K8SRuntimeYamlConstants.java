package io.hyscale.ctl.commons.constants;

public class K8SRuntimeYamlConstants {

	public static final String STATEFULSET_NAME = "STATEFULSET_NAME";
	public static final String DEPLOYMENT_NAME = "DEPLOYMENT_NAME";
	public static final String RELEASE_VERSION = "RELEASE_VERSION";
	public static final String APP_NAME = "APP_NAME";
	public static final String ENVIRONMENT_NAME = "ENVIRONMENT_NAME";
	public static final String PLATFORM_DOMAIN = "PLATFORM_DOMAIN";
	public static final String SERVICE_NAME = "SERVICE_NAME";
	public static final String REPLICAS_COUNT = "REPLICAS_COUNT";
	public static final String CONTAINER_NAME = "CONTAINER_NAME";
	public static final String IMAGE_NAME = "IMAGE_NAME";
	public static final String IMAGE_PULL_POLICY = "IMAGE_PULL_POLICY";
	public static final String IMAGE_PULL_SECRET_NAME = "IMAGE_PULL_SECRET_NAME";
	public static final String K8S_SERVICE_NAME = "K8S_SERVICE_NAME";
	public static final String SERVICE_TYPE = "SERVICE_TYPE";

	public static final String CONTAINERS = "containers";
	public static final String VOLUME_CLAIM_TEMPLATES = "volumeClaimTemplates";
	public static final String ANNOTATIONS = "annotations";
	public static final String VOLUMES = "volumes";
	public static final String ENVS = "envs";
	public static final String VOLUME_MOUNTS = "volumeMounts";
	public static final String PORTS = "ports";

	public static final String READINESS_PROBE = "readinessProbe";
	public static final String LIVENESS_PROBE = "livenessProbe";
	public static final String RESOURCE_REQUESTS = "resourcesRequests";
	public static final String COMMAND = "command";
	public static final String ARGS = "args";

	public static final String DEFAULT_K8S_IMAGE_PULL_POILCY = "IfNotPresent";

	public static final String serviceports = "serviceports";
	public static final String SESSION_AFFINITY = "SESSION_AFFINITY";

	/* Secret */
	public static final String SECRET_NAME = "SECRET_NAME";
	public static final String SECRET_FILE_NAME = "SECRET_FILE_NAME";
	public static final String configSecrets = "configSecrets";
	public static final String SECRET_FILE_DATA = "SECRET_FILE_DATA";

	/* ConfigMap */
	public static final String CONFIGMAP_NAME = "CONFIGMAP_NAME";
	public static final String CONFIGPROPS_FILE = "CONFIGPROPS_FILE";
	public static final String CONFIGPROPS_FILEDATA = "CONFIGPROPS_FILEDATA";
	public static final String configProps = "configProps";
	public static final String fileProps = "fileProps";

	/* Ingress */
	public static final String INGRESS_NAME = "INGRESS_NAME";
	public static final String INGRESS_CLASS = "INGRESS_CLASS";
	public static final String INGRESS_COMPONENT = "INGRESS";
	public static final String DOMAIN_NAME = "DOMAIN_NAME";
	public static final String DOMAIN_SECRET_NAME = "DOMAIN_SECRET_NAME";
	public static final String rules = "rules";
	public static final String tls = "tls";
	public static final String CONFIGURATION_SNIPPET = "CONFIGURATION_SNIPPET";
	public static final String INGRESS_GROUP = "INGRESS_GROUP";
	public static final String ALLOW_HTTP = "ALLOW_HTTP";
	public static final String SSL_REDIRECT = "SSL_REDIRECT";
	public static final String STICKY = "STICKY";

	/* SSL Secret */
	public static final String TLS_KEY = "TLS_KEY";
	public static final String TLS_CERTIFICATE = "TLS_CERTIFICATE";

	/* Ingress Controller */
	public static final String INGRESS_CONTROLLER_NAME = "INGRESS_CONTROLLER_NAME";
	public static final String INGRESS_REPLICAS = "INGRESS_REPLICAS";
	public static final String INGRESS_NGINX_IMAGE = "INGRESS_NGINX_IMAGE";
	public static final String INGRESS_MEMORY_REQUEST = "INGRESS_MEMORY_REQUEST";
	public static final String INGRESS_MEMORY_LIMIT = "INGRESS_MEMORY_LIMIT";
	public static final String INGRESS_CONFIGMAP_NAME = "INGRESS_CONFIGMAP_NAME";
	public static final String INGRESS_CONTROLLER_BACKEND_GROUP = "INGRESS_CONTROLLER_BACKEND_GROUP";
	public static final String INGRESS_CONTROLLER_GROUP = "INGRESS_CONTROLLER_GROUP";
	public static final String HYSCALE_COMPONENT = "HYSCALE_COMPONENT";
	public static final String INGRESS_PROVIDER = "INGRESS_PROVIDER";

	/** Ingress ConfigMap **/

	public static final String INGRESS_CONFIGMAP_PROPS = "props";
	public static final String INGRESS_CONTROLLER_SVC_NAME = "INGRESS_CONTROLLER_SVC_NAME";

	/** Ingress Default Backend **/
	public static final String DEFAULT_BACKEND_IMAGE = "DEFAULT_BACKEND_IMAGE";
	public static final String DEFAULT_BACKEND_MEMORY_LIMIT = "DEFAULT_BACKEND_MEMORY_LIMIT";
	public static final String DEFAULT_BACKEND_REQUESTED_MEMORY = "DEFAULT_BACKEND_REQUESTED_MEMORY";
	public static final String DEFAULT_BACKEND_SERVICE_NAME = "DEFAULT_BACKEND_SERVICE_NAME";

	/* Image Pull Secret */
	public static final String NAME = "name";
	public static final String CONFIG_JSON = "configJson";

	/* Rbac */
	public static final String SERVICE_ACCOUNT_NAME = "SERVICE_ACCOUNT_NAME";
	public static final String CLUSTER_ROLE_NAME = "CLUSTER_ROLE_NAME";
	public static final String RBAC = "RBAC";
	public static final String CLUSTER_ROLE_BINDING_NAME = "CLUSTER_ROLE_BINDING_NAME";
	public static final String NAMESPACE = "NAMESPACE";
	public static final String ROLE_NAME = "ROLE_NAME";
	public static final String ROLE_BINDING_NAME = "ROLE_BINDING_NAME";

	/* Traefik */
	public static final String INGRESS_TRAEFIK_IMAGE = "INGRESS_TRAEFIK_IMAGE";
	public static final String FRONTEND_ENTRYPOINTS = "FRONTEND_ENTRYPOINTS";
	public static final String REDIRECT_ENTRYPOINTS = "REDIRECT_ENTRYPOINTS";
	public static final String HEADERS_EXPRESSION = "HEADERS_EXPRESSION";
	public static final String TRAEFIK_TOML_CONFIG = "TRAEFIK_TOML_CONFIG";

}