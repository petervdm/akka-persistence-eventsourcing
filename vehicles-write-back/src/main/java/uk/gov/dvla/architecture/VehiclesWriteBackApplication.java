package uk.gov.dvla.architecture;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.dvla.architecture.health.VehiclesWriteBackHealthCheck;
import uk.gov.dvla.architecture.resources.VehiclesResource;

public class VehiclesWriteBackApplication extends Application<VehiclesWriteBackConfiguration> {
    public static void main(String[] args) throws Exception {
        new VehiclesWriteBackApplication().run(args);
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<VehiclesWriteBackConfiguration> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(VehiclesWriteBackConfiguration configuration,
                    Environment environment) {
        final VehiclesResource resource = new VehiclesResource(
                configuration.getTemplate(),
                configuration.getDefaultName()
        );

        final VehiclesWriteBackHealthCheck healthCheck =
                new VehiclesWriteBackHealthCheck(configuration.getTemplate());

        environment.healthChecks().register("template", healthCheck);
        environment.jersey().register(resource);
    }

}