package com.the6hours.reactorappengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

/**
 * Since 25.07.13
 *
 * @author Igor Artamonov, http://igorartamonov.com
 */
@Controller
public class ReactorController {

    private static final Logger log = LoggerFactory.getLogger(ReactorController.class);

    @Autowired
    private ProcessReactorCommand processReactorCommand;

    @RequestMapping(value = "/_ah/reactor")
    @ResponseBody
    public String reactor(HttpServletRequest request) throws Exception {
        ObjectInput oi = new ObjectInputStream(request.getInputStream());
        ReactorCommand command;
        try {
            command = (ReactorCommand) oi.readObject();
            log.info("Process command " + command.getKey());
        } catch (ClassNotFoundException e) {
            log.error("Classpath issue", e);
            throw e;
        }
        processReactorCommand.process(command);
        return "OK";
    }
}
