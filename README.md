# Alfresco Java code executor

This project was made on DevCon 2019 Hacathon. The target of this project is to execute Java code on the running server (Alfresco). 

The project consists of two parts: Alfresco platform module and IntelliJ plugin.

## What does this project offer?

* executing any piece of code on running server
* code is compiled on the server
* compiled code isn't added to main ClassLoader
* debugger support
* support for Spring annotations
* messages writing using System.out are accumulated and returned to user
* IntelliJ plugin allows you executing code directly from IDE

## How does it is work?

1. You have a piece of code. The only requirement is to put **public void execute()** method because it is input to your code. Example:
```
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

    @Autowired
    private NodeService nodeService;

    @Autowired
    private PersonService personService;

    private Logger logger = LoggerFactory.getLogger(Scratch.class);

    public void execute() {
        NodeRef person = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());

        System.out.println(nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME));
    }

}
```
2. Make POST request with your code (contained in binary data) on /service/api/javaCodeExecutor/execute
3. Receive response in the following format:
```
{
    "messages": [
        "message one",
        "message two"
    ]
}
```
If response code is 200 - everything went well. Messages array contains messages written using System.out.

If response code doesn't equal to 200 - something went wrong. Messages array contains the cause of error.

## Setup
### ACS module
Module is based on SDK 4.0.0 so you can build module and run ACS using
```
./run.sh build_start
```
from **mm/java-code-executor** path.

### IntelliJ plugin
Open **plugin/java-code-executor** in IntelliJ and run **Build -> Prepare Plugin Module 'java-code-executor' For Deployment**.
After it, install **java-code-executor.jar** as IntelliJ plugin.