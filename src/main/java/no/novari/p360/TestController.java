package no.novari.p360;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.novari.p360.data.exception.EnterpriseNotFound;
import no.novari.p360.data.p360.AccessGroupService;
import no.novari.p360.data.p360.ContactService;
import no.novari.p360.data.p360.SupportService;
import no.p360.model.AccessGroupService.GetAccessGroupsArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private SupportService supportService;
    
    @Autowired
    private AccessGroupService accessGroupService;

    @Autowired
    private ContactService contactService;

    @GetMapping(value = "version", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getVersion() {
        return supportService.getSIFVersion();
    }

    @GetMapping(value = "codelist", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getCodelist(@RequestParam String id) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(supportService.getCodeTable(id));
    }
    
    @GetMapping(value = "accessgroup", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAccessGroup() throws JsonProcessingException {
        GetAccessGroupsArgs getAccessGroupsArgs = new GetAccessGroupsArgs();
        return new ObjectMapper().writeValueAsString(accessGroupService.getAccessGroups(getAccessGroupsArgs));
    }

    @GetMapping(path = "contact", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getContact(@RequestParam String name) throws JsonProcessingException, EnterpriseNotFound {
        return new ObjectMapper().writeValueAsString(contactService.getEnterprisesByName(name));
    }
}
