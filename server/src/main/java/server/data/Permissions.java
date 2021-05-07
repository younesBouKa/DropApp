package server.data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 0	No Permission	        ---
 * 1	read	                  r--
 * 2	write 	                    -w-
 * 4	share	                      --s
 * 6	Read +Write 	                rw-
 * 7	Read + Write + Share	            rws
 */
public enum Permissions {
    NO(0b0,"No"),
    READ(0b1<<1, "READ"),
    WRITE(0b1<<2, "WRITE"),
    SHARE(0b1<<3, "SHARE"),
    ;

    String name;
    int code;

    Permissions(int code, String name){
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isIn(int compoundCode){
        return (compoundCode & code) == code;
    }

    public int addTo(int compoundCode){
        return compoundCode | code;
    }

    public int removeFrom(int compoundCode){
        List<Permissions> permissions = getAllFrom(compoundCode);
        permissions.remove(this);
        int value = 0;
        for (Permissions per : permissions)
            value = value | per.getCode();
        return value;
    }

    public static List<Permissions> getAllFrom(int compoundCode){
        return Arrays.stream(values())
                .filter(permissions -> permissions.isIn(compoundCode))
                .collect(Collectors.toList());
    }

    public static int all(){
        int c = 0;
        for(Permissions p : values())
            c= c | p.getCode();
        return c;
    }

}
