package org.example;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService
@SOAPBinding(style = Style.RPC)
public interface DSMSService {
    // Admin roles
    @WebMethod
    public String addShare(String shareID, String shareType, int capacity);

    @WebMethod
    public String removeShare(String shareID, String shareType);

    @WebMethod
    public String listShareAvailability(String shareType);

    // Buyer roles 
    @WebMethod
    public String purchaseShare(String buyerID, String shareID, String shareType, int shareCount);

    @WebMethod
    public String getShares(String buyerID);

    @WebMethod
    public String sellShare(String buyerID, String shareID, int shareCount);

    @WebMethod
    public String swapShares(String buyerID, String oldShareID, String oldShareType, String newShareID,
                             String newShareType);
}
