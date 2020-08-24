package no.fint.p360.service;

import no.fint.event.model.Event;
import no.fint.event.model.Problem;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.FintLinks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValidationService {

    @Autowired
    private ValidatorFactory validatorFactory;

    public List<Problem> getProblems(Object resource) {
        Validator validator = validatorFactory.getValidator();
        return validator.validate(resource)
                .stream()
                .map(violation -> new Problem() {{
                    setField(violation.getPropertyPath().toString());
                    setMessage(violation.getMessage());
                }})
                .collect(Collectors.toList());
    }

    public boolean validate(Event<FintLinks> event, Object resource) {
        if (resource == null) {
            event.setResponseStatus(ResponseStatus.REJECTED);
            event.setMessage("RESOURCE WAS NULL");
            return false;
        }
        final List<Problem> problems = getProblems(resource);
        if (problems.isEmpty()) {
            return true;
        }
        event.setProblems(problems);
        event.setResponseStatus(ResponseStatus.REJECTED);
        event.setStatusCode("INVALID");
        return false;
    }
    
}
