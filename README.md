# ACS Java code executor

This project was initiated on DevCon 2019 Hacathon. The target of this project is to execute Java code on the running server (Alfresco Content Services). 

The project consists of two parts: ACS platform module and IntelliJ plugin.

## What does this project offer?

* Executing a piece of code on a running server
* Code is compiled on the server
* Compiled code isn't added to the main ClassLoader - new ClassLoader, that is the child of the main ClassLoader, is created, the code is executed and the reference to new ClassLoader is lost (child ClassLoader is used only for code execution)
* Debugger support
* Support for Spring annotations
* Messages writing using dedicated logger are accumulated and returned to the user (standard appenders are excluded)
* IntelliJ plugin allows you executing code directly from IDE

## Flow chart

1. Make **POST** request with your code (contained in **binary data**) on **/service/api/javaCodeExecutor/execute**. Example:
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
```
2. Receive a response in the following format:
```
{
    "messages": [
        "2019-02-07 19:28:29,733  DEBUG [scratchcodeexecutor.scratch.Scratch-60] [http-nio-8080-exec-3] First name: Administrator",
        "2019-02-07 19:28:29,734  DEBUG [scratchcodeexecutor.scratch.Scratch-60] [http-nio-8080-exec-3] End"
    ]
}
```
* Response code is 200 - everything went well. Messages array contains messages written using dedicated logger
* Response code isn't 200 - something went wrong. Messages array contains the cause of error (compilation problems, stack trace etc.)

## Requirements
* The file must have a package name (IntelliJ scratch module doesn't include package name by default) 
* The file must contain a class
* The class must contain the following method: **public void execute()** (it's input to your code) 
* *Optional* If you put logger without any value (for example **private Logger _logger;**), the dedicated logger is injected (output is redirected to user)

## Setup
You can use files from **Releases** or build it yourself
### ACS module
The module is based on SDK 4.0.0 so you can build the module and run ACS using
```
./run.sh build_start
```
from **mm/java-code-executor** path.

### IntelliJ plugin
Run
```
./gradlew runIde
```
from **plugin/java-code-executor** path.
