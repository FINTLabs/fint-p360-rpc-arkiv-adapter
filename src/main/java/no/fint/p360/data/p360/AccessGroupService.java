package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.p360.model.AccessGroupService.AccessGroup;
import no.p360.model.AccessGroupService.GetAccessGroupsArgs;
import no.p360.model.AccessGroupService.GetAccessGroupsResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AccessGroupService extends P360Service {

    public List<AccessGroup> getAccessGroups(GetAccessGroupsArgs getAccessGroupsArgs) {

        log.info("GetAccessGroups query: {}", getAccessGroupsArgs);
        GetAccessGroupsResponse getAccessGroupsResponse = call("AccessGroupService/GetAccessGroups", getAccessGroupsArgs, GetAccessGroupsResponse.class);
        log.info("GetAccessGroupsResponse result: {}", getAccessGroupsResponse);
        return getAccessGroupsResponse.getAccessGroups();
    }
}
