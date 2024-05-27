package com.nari.iot.vendorinfo.common;

public class IdConvert {

    public static int getTableId(long keyId) {
        if (keyId < 0L) {
            return -1;
        }
        return (int) (keyId >> 48);
    }

    public static int getColumnId(long keyId) {
        if (keyId < 0L) {
            return -1;
        }
        return (int) (keyId >> 32 & 0xFFFF);
    }

    public static int getRecordId(long keyId) {
        if (keyId < 0L) {
            return -1;
        }
        return (int) (keyId & 0xFFFFFFFF);
    }

    public static int getAreaID(long keyId) {
        int areaId = (int) (keyId >> 24 & 0xFF);
        return areaId;
    }

    public static long getId(Integer tableId, int recordId) {
        if ((tableId <= 0) || (recordId <= 0)) {
            return -1L;
        }
        return tableId.longValue() << 48 | recordId;
    }

    public static long getKeyId(Integer tableId, Integer columnId, int recordId) {
        if ((tableId <= 0) || (columnId <= 0) || (recordId <= 0)) {
            return -1L;
        }
        return tableId.longValue() << 48 | columnId.longValue() << 32 | recordId;
    }

    public static long getKeyId(long id, int columnId) {
        int tableId = getTableId(id);
        if (tableId < 0) {
            return -1L;
        }
        int recordId = getRecordId(id);
        if (recordId < 0) {
            return -1L;
        }
        if (columnId < 0) {
            return -1L;
        }
        return getKeyId(tableId, columnId, recordId);
    }

    public static long getIdByKeyId(long keyId) {
        if (keyId < 0L) {
            return -1L;
        }
//	    return keyId & 0xFF00FFFF;
        return keyId & 0xFFFF0000FFFFFFFFL;
    }

    public static long getStatusKeyId(long keyId) {
        int tableId = getTableId(keyId);
        if (tableId < 0) {
            return -1L;
        }
        int recordId = getRecordId(keyId);
        if (recordId < 0) {
            return -1L;
        }
        int columnId = getColumnId(keyId);
        if (columnId < 0) {
            return -1L;
        }
        return getKeyId(tableId, columnId + 1, recordId);
    }

    public static void main(String[] args) {

//		  System.out.println(getKeyId(3300012626955730948L, 25));
//		  System.out.println(getKeyId(3300294101932441603L, 22));
//		  System.out.println(getKeyId(3300294101932441606L, 22));
//
//		  System.out.println(getKeyId(3300012626955730953L, 25));

//		  System.out.println(getKeyId(432, 20, 30));

      //  System.out.println(getIdByKeyId(116812330083290025L));
        System.out.println(getIdByKeyId(3801320316492415636L));
		 /* System.out.println(getColumnId(116812287133617064L));
		  System.out.println(getRecordId(116812287133617064L));
		  System.out.println(getTableId(116812287133617064L));*/

    }

}
