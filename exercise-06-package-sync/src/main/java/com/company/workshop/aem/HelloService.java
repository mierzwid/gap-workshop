package com.company.workshop.aem;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Activate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hashids.Hashids;

@Component(service = HelloService.class)
public class HelloService {

    private static final Logger LOG = LoggerFactory.getLogger(HelloService.class);

    private static final Hashids HASHER = new Hashids();

    @Activate
    protected void activate() {
        LOG.info("Hello service called!");
        LOG.info("Hash ID for current timestamp is '{}'", HASHER.encode(System.currentTimeMillis()));
    }
}
