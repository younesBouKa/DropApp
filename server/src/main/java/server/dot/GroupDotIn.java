package server.dot;

import server.data.Group;

public class GroupDotIn {
    private String name;
    private String label;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Group toGroup(Group group) {
        if(getName()!=null)
            group.setName(getName());
        if(getLabel()!=null)
            group.setLabel(getLabel());
        if(getDescription()!=null)
            group.setDescription(getDescription());
        return group;
    }

    public Group toGroup() {
        Group group = new Group();
       return toGroup(group);
    }
}
