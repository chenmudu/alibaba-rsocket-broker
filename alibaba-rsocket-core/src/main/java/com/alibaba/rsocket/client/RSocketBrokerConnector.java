package com.alibaba.rsocket.client;

import com.alibaba.rsocket.metadata.RSocketMimeType;
import com.alibaba.rsocket.rpc.LocalReactiveServiceCaller;
import com.alibaba.rsocket.rpc.LocalReactiveServiceCallerImpl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * RSocket Broker Connector
 *
 * @author leijuan
 */
public class RSocketBrokerConnector {
    private char[] jwtToken;
    private List<String> brokers = Collections.singletonList("tcp://127.0.0.1:9999");
    private String appName = "MockApp";
    private RSocketMimeType dataMimeType = RSocketMimeType.Hessian;
    LocalReactiveServiceCaller serviceCaller = new LocalReactiveServiceCallerImpl();

    private RSocketBrokerConnector() {
    }

    public static RSocketBrokerConnector create() {
        return new RSocketBrokerConnector();
    }

    public RSocketBrokerConnector jwtToken(char[] jwtToken) {
        this.jwtToken = jwtToken;
        return this;
    }

    public RSocketBrokerConnector appName(String appName) {
        this.appName = appName;
        return this;
    }

    public RSocketBrokerConnector dataMimeType(RSocketMimeType dataMimeType) {
        this.dataMimeType = Objects.requireNonNull(dataMimeType);
        return this;
    }

    public RSocketBrokerConnector service(Class<?> serviceInterface, Object obj) {
        serviceCaller.addProvider("", serviceInterface.getCanonicalName(), "", serviceInterface, obj);
        return this;
    }

    public RSocketBrokerConnector brokers(List<String> brokers) {
        this.brokers = brokers;
        return this;
    }

    public RSocketBrokerClient connect() {
        return new RSocketBrokerClient(this.appName, this.brokers, this.dataMimeType, this.jwtToken, this.serviceCaller);
    }

}
