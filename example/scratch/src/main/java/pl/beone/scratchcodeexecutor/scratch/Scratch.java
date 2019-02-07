package pl.beone.scratchcodeexecutor.scratch;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class Scratch {

    private Logger _logger;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private PersonService personService;

    private Logger logger = LoggerFactory.getLogger(Scratch.class);

    public void execute() {
        NodeRef person = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());

        _logger.debug("First name: {}", nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME));
        _logger.debug("End");

        logger.error("End");
    }

}
