/**
 * IArapSupplierSynchServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.yc.axis;

public class IArapSupplierSynchServiceLocator extends org.apache.axis.client.Service implements com.yc.axis.IArapSupplierSynchService {

    public IArapSupplierSynchServiceLocator() {
    }


    public IArapSupplierSynchServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public IArapSupplierSynchServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for IArapSupplierSynchServiceSOAP11port_http
    private java.lang.String IArapSupplierSynchServiceSOAP11port_http_address = "http://127.0.0.1:65/uapws/service/nc.itf.arap.supplier.IArapSupplierSynchService";

    public java.lang.String getIArapSupplierSynchServiceSOAP11port_httpAddress() {
        return IArapSupplierSynchServiceSOAP11port_http_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String IArapSupplierSynchServiceSOAP11port_httpWSDDServiceName = "IArapSupplierSynchServiceSOAP11port_http";

    public java.lang.String getIArapSupplierSynchServiceSOAP11port_httpWSDDServiceName() {
        return IArapSupplierSynchServiceSOAP11port_httpWSDDServiceName;
    }

    public void setIArapSupplierSynchServiceSOAP11port_httpWSDDServiceName(java.lang.String name) {
        IArapSupplierSynchServiceSOAP11port_httpWSDDServiceName = name;
    }

    public com.yc.axis.IArapSupplierSynchServicePortType getIArapSupplierSynchServiceSOAP11port_http() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(IArapSupplierSynchServiceSOAP11port_http_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getIArapSupplierSynchServiceSOAP11port_http(endpoint);
    }

    public com.yc.axis.IArapSupplierSynchServicePortType getIArapSupplierSynchServiceSOAP11port_http(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.yc.axis.IArapSupplierSynchServiceSOAP11BindingStub _stub = new com.yc.axis.IArapSupplierSynchServiceSOAP11BindingStub(portAddress, this);
            _stub.setPortName(getIArapSupplierSynchServiceSOAP11port_httpWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setIArapSupplierSynchServiceSOAP11port_httpEndpointAddress(java.lang.String address) {
        IArapSupplierSynchServiceSOAP11port_http_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.yc.axis.IArapSupplierSynchServicePortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.yc.axis.IArapSupplierSynchServiceSOAP11BindingStub _stub = new com.yc.axis.IArapSupplierSynchServiceSOAP11BindingStub(new java.net.URL(IArapSupplierSynchServiceSOAP11port_http_address), this);
                _stub.setPortName(getIArapSupplierSynchServiceSOAP11port_httpWSDDServiceName());
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
        if ("IArapSupplierSynchServiceSOAP11port_http".equals(inputPortName)) {
            return getIArapSupplierSynchServiceSOAP11port_http();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://supplier.arap.itf.nc/IArapSupplierSynchService", "IArapSupplierSynchService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://supplier.arap.itf.nc/IArapSupplierSynchService", "IArapSupplierSynchServiceSOAP11port_http"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("IArapSupplierSynchServiceSOAP11port_http".equals(portName)) {
            setIArapSupplierSynchServiceSOAP11port_httpEndpointAddress(address);
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
