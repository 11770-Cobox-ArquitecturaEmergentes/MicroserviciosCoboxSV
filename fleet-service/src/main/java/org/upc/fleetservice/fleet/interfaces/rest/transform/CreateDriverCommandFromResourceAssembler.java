package org.upc.fleetservice.fleet.interfaces.rest.transform;


import org.upc.fleetservice.fleet.domain.model.commands.CreateDriverCommand;
import org.upc.fleetservice.fleet.interfaces.rest.resources.CreateDriverResource;

public class CreateDriverCommandFromResourceAssembler {
    public static CreateDriverCommand toCommandFromResource(CreateDriverResource resource){
        return new CreateDriverCommand(
                resource.email(),
                resource.licenceNumber()
        );
    }
}
