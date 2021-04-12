package server.data;

public enum Permission {
    READ("read",0b00001),
    WRITE("write",0b00010),
    READ_WRITE("read & write",0b00011);
    String name;
    int code;

    Permission(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static Permission getByCode(int permission) {
        for (Permission p: Permission.values()) {
            if(p.getCode() == permission)
                return p;
        }
        return null;
    }

    public static Permission getFrom(Object permission){
        Permission per = null;
        if(permission instanceof Integer)
            per = Permission.getByCode((int)permission);
        else if(permission instanceof String)
            per = Permission.valueOf((String) permission);
        else
            per = (Permission)permission;
        return per;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }
}
