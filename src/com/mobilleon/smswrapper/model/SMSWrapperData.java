/**
 * A generic class to hold
 * response from API/Server calls
 * */
package com.mobilleon.smswrapper.model;

/**
 * @author Atul Kaushik (kaushik.atul@gmail.com)
 *
 */
public class SMSWrapperData<Type> {

    public Type response;
    public int responseErrorCode;
    public boolean requestSuccessful;
}
