/**
 * IArapCustomerSynchServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.yc.axis;

public class IArapCustomerSynchServiceLocator extends org.apache.axis.client.Service implements com.yc.axis.IArapCustomerSynchService {

    public IArapCustomerSynchServiceLocator() {
    }


    public IArapCustomerSynchServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public IArapCustomerSynchServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for IArapCustomerSynchServiceSOAP11port_http
    private java.lang.String IArapCustomerSynchServiceSOAP11port_http_address = "http://127.0.0.1:65/uapws/service/nc.itf.arap.customer.IArapCustomerSynchService";

    public java.lang.String getIArapCustomerSynchServiceSOAP11port_httpAddress() {
        return IArapCustomerSynchServiceSOAP11port_http_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String IArapCustomerSynchServiceSOAP11port_httpWSDDServiceName = "IArapCustomerSynchServiceSOAP11port_http";

    public java.lang.String getIArapCustomerSynchServiceSOAP11port_httpWSDDServiceName() {
        return IArapCustomerSynchServiceSOAP11port_httpWSDDServiceName;
    }

    public void setIArapCustomerSynchServiceSOAP11port_httpWSDDServiceName(java.lang.String name) {
        IArapCustomerSynchServiceSOAP11port_httpWSDDServiceName = name;
    }

    public com.yc.axis.IArapCustomerSynchServicePortType getIArapCustomerSynchServiceSOAP11port_http() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(IArapCustomerSynchServiceSOAP11port_http_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getIArapCustomerSynchServiceSOAP11port_http(endpoint);
    }

    public com.yc.axis.IArapCustomerSynchServicePortType getIArapCustomerSynchServiceSOAP11port_http(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.yc.axis.IArapCustomerSynchServiceSOAP11BindingStub _stub = new com.yc.axis.IArapCustomerSynchServiceSOAP11BindingStub(portAddress, this);
            _stub.setPortName(getIArapCustomerSynchServiceSOAP11port_httpWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setIArapCustomerSynchServiceSOAP11port_httpEndpointAddress(java.lang.String address) {
        IArapCustomerSynchServiceSOAP11port_http_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.yc.axis.IArapCustomerSynchServicePortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.yc.axis.IArapCustomerSynchServiceSOAP11BindingStub _stub = new com.yc.axis.IArapCustomerSynchServiceSOAP11BindingStub(new java.net.URL(IArapCustomerSynchServiceSOAP11port_http_address), this);
                _stub.setPortName(getIArapCustomerSynchServiceSOAP11port_httpWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("IArapCustomerSynchServiceSOAP11port_http".equals(inputPortName)) {
            return getIArapCustomerSynchServiceSOAP11port_http();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://customer.arap.itf.nc/IArapCustomerSynchService", "IArapCustomerSynchService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://customer.arap.itf.nc/IArapCustomerSynchService", "IArapCustomerSynchServiceSOAP11port_http"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("IArapCustomerSynchServiceSOAP11port_http".equals(portName)) {
            setIArapCustomerSynchServiceSOAP11port_httpEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
