package io.hyscale.generator.services.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Agent;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AgentHelper {

    private static final Logger logger = LoggerFactory.getLogger(AgentHelper.class);

    public List<Agent> getAgents(ServiceSpec serviceSpec){
        TypeReference<List<Agent>> agentsList = new TypeReference<List<Agent>>() {
        };
        try {
            List<Agent> agents = serviceSpec.get(HyscaleSpecFields.agents, agentsList);
            return  agents;
        } catch (HyscaleException e) {
            logger.error("Error while fetching agents from service spec, returning null.",e);
            return null;
        }
    }
}
