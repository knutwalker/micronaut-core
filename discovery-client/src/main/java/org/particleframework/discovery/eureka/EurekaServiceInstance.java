/*
 * Copyright 2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.particleframework.discovery.eureka;

import org.particleframework.core.convert.value.ConvertibleValues;
import org.particleframework.core.util.StringUtils;
import org.particleframework.discovery.ServiceInstance;
import org.particleframework.discovery.eureka.client.v2.AmazonInfo;
import org.particleframework.discovery.eureka.client.v2.DataCenterInfo;
import org.particleframework.discovery.eureka.client.v2.InstanceInfo;
import org.particleframework.health.HealthStatus;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Optional;

/**
 * A {@link ServiceInstance} implementation for Eureka
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public class EurekaServiceInstance implements ServiceInstance {
    private final InstanceInfo instanceInfo;
    private final URI uri;

    public EurekaServiceInstance(InstanceInfo instanceInfo) {
        this.instanceInfo = instanceInfo;
        this.uri = createURI(instanceInfo);
    }

    private URI createURI(InstanceInfo instanceInfo) {
        int securePort = instanceInfo.getSecurePort();
        if(securePort > 0) {
            int port = instanceInfo.getPort();
            String portStr = port > 0 ? ":" + port : "";
            return URI.create("https://" + instanceInfo.getHostName() + portStr);
        }
        else {
            int port = instanceInfo.getPort();
            String portStr = port > 0 ? ":" + port : "";
            return URI.create("http://" + instanceInfo.getHostName() + portStr);
        }
    }

    @Override
    public Optional<String> getInstanceId() {
        return Optional.ofNullable(instanceInfo.getInstanceId());
    }

    @Override
    public HealthStatus getHealthStatus() {
        InstanceInfo.@NotNull Status status = instanceInfo.getStatus();
        switch (status) {
            case UP:
                return HealthStatus.UP;
            case UNKNOWN:
                return HealthStatus.UNKNOWN;
            default:
                return HealthStatus.DOWN;
        }
    }

    @Override
    public Optional<String> getZone() {
        @NotNull DataCenterInfo dataCenterInfo = instanceInfo.getDataCenterInfo();
        if(dataCenterInfo instanceof AmazonInfo) {
            String availabilityZone = ((AmazonInfo) dataCenterInfo).get(AmazonInfo.MetaDataKey.availabilityZone);
            return Optional.ofNullable(availabilityZone);
        }
        return ServiceInstance.super.getZone();
    }

    @Override
    public Optional<String> getRegion() {
        @NotNull DataCenterInfo dataCenterInfo = instanceInfo.getDataCenterInfo();
        if(dataCenterInfo instanceof AmazonInfo) {
            String availabilityZone = ((AmazonInfo) dataCenterInfo).get(AmazonInfo.MetaDataKey.availabilityZone);
            return Optional.ofNullable(availabilityZone);
        }
        return ServiceInstance.super.getZone();
    }

    @Override
    public Optional<String> getGroup() {
        String asgName = instanceInfo.getAsgName();
        if(StringUtils.isNotEmpty(asgName)) {
            return Optional.of(asgName);
        }
        return ServiceInstance.super.getZone();
    }

    /**
     * @return The Eureka {@link InstanceInfo}
     */
    public InstanceInfo getInstanceInfo() {
        return instanceInfo;
    }

    @Override
    public String getId() {
        return instanceInfo.getId();
    }

    @Override
    public URI getURI() {
        return this.uri;
    }

    @Override
    public ConvertibleValues<String> getMetadata() {
        return ConvertibleValues.of(instanceInfo.getMetadata());
    }

}
