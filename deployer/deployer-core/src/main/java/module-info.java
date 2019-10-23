module deployerModel {
	exports io.hyscale.deployer.core.model;

	requires jackson.annotations;
	requires client.java.api;
	requires gson;
	requires commons;
	requires joda.time;
	requires org.apache.commons.lang3;
}