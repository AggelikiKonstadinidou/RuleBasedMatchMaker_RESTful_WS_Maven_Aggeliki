package com.gpii.ontology;

/**
 *
 * @author nkak
 */
public class Setting 
{
    public static final int UNKNOWN = -1;
    public static final int STRING = 0;
    public static final int FLOAT = 1;
    public static final int BOOLEAN = 2;
    public static final int INT = 3;
    public static final int TIME = 4;
    public static final int DATE = 5;
    public static final int DATETIME = 6;
    
    public String instanceName;
    public int type;
    public String value;
    public boolean ignoreSetting;
    
    public String hasID;
    public String hasName;
    public String hasDescription;
    public String hasValueSpace;
    public String hasConstraints;
    public String isMappedToRegTerm;
    public boolean isExactMatching;
    public String hasCommentsForMapping;
    
    public Setting()
    {
        instanceName = "";
        type = UNKNOWN;
        value = "";
        ignoreSetting = false;
        
        hasID = "";
        hasName = "";
        hasDescription = "";
        hasValueSpace = "";
        hasConstraints = "";
        isMappedToRegTerm = "";
        isExactMatching = true;
        hasCommentsForMapping = "";
    }
    
    @Override
    public String toString()
    {
        String res = "";
        String typeStr = "";
        if(type == STRING)
            typeStr = "string";
        else if(type == FLOAT)
            typeStr = "float";
        else if(type == BOOLEAN)
            typeStr = "boolean";
        else if(type == INT)
            typeStr = "int";
        else if(type == TIME)
            typeStr = "time";
        else if(type == DATE)
            typeStr = "date";
        else if(type == DATETIME)
            typeStr = "dateTime";
        else
            typeStr = "UNKNOWN";
        if(ignoreSetting == false)
            res = "-SETTING- NAME: " + instanceName + ", TYPE: " + typeStr + ", VALUE: " + value + ", hasID: " + hasID + ", hasName: " + hasName + ", hasDescription: " + hasDescription + ", hasValueSpace: " + hasValueSpace + ", hasConstraints: " + hasConstraints + ", isMappedToRegTerm: " + isMappedToRegTerm + ", isExactMatching: " + isExactMatching + ", hasCommentsForMapping: " + hasCommentsForMapping;
        else
            res = "  -SETTING- IGNORED -> NAME: " + instanceName + ", TYPE: " + typeStr + ", VALUE: " + value + ", hasID: " + hasID + ", hasName: " + hasName + ", hasDescription: " + hasDescription + ", hasValueSpace: " + hasValueSpace + ", hasConstraints: " + hasConstraints + ", isMappedToRegTerm: " + isMappedToRegTerm + ", isExactMatching: " + isExactMatching + ", hasCommentsForMapping: " + hasCommentsForMapping;;
        return res;
    }
}
