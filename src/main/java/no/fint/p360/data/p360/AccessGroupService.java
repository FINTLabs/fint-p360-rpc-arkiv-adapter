package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.p360.service.FilterSetService;
import no.p360.model.AccessGroupService.AccessGroup;
import no.p360.model.AccessGroupService.GetAccessGroupsArgs;
import no.p360.model.AccessGroupService.GetAccessGroupsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AccessGroupService extends P360Service {

    @Autowired
    private FilterSetService filterSetService;

    public List<AccessGroup> getAccessGroups(GetAccessGroupsArgs getAccessGroupsArgs) {

        log.debug("GetAccessGroups query: {}", getAccessGroupsArgs);
        GetAccessGroupsResponse getAccessGroupsResponse = call(filterSetService.getDefaultFilterSet(), "AccessGroupService/GetAccessGroups", getAccessGroupsArgs, GetAccessGroupsResponse.class);
        log.debug("GetAccessGroupsResponse result: {}", getAccessGroupsResponse);
        return getAccessGroupsResponse.getAccessGroups();
    }
}
