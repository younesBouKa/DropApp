package server.providers;

import io.jsonwebtoken.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import server.data.Node;
import server.exceptions.CustomException;
import server.repositories.INodeRepo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


import static server.data.Permissions.*;

@Component
public class NodeProvider implements INodeProvider{

    @Autowired
    IAccessProvider accessProvider;
    @Autowired
    INodeRepo nodeRepo;

    @Override
    public Node insertNode(String userId, Node node) throws CustomException {
        Assert.notNull(node.getName(), "Node name can't be null");
        Assert.notNull(userId, "user id can't be null");
        String parentId = node.getParentId();
        if(parentId!=null)
            accessProvider.assertPermission(parentId, userId, WRITE);
        Node insertedNode = nodeRepo.insert(node);
        accessProvider.addAllPermissions(insertedNode.getId(), userId); // TODO add Verification
        return insertedNode;
    }

    @Override
    public Node saveNode(String userId, Node node) throws CustomException {
        Assert.notNull(node.getName(), "Node name can't be null");
        Assert.notNull(userId, "user id can't be null");
        accessProvider.assertPermission(node.getId(), userId, WRITE);
        return nodeRepo.save(node);
    }

    @Override
    public Optional<Node> getNodeById(String nodeId, String userId) throws CustomException {
        accessProvider.assertPermission(nodeId, userId, READ);
        return nodeRepo.findById(nodeId);
    }

    @Override
    public List<Node> getNodesByUserAndParentIdAndName(String userId, String parentId, String name, PageRequest pageRequest) throws CustomException {
        return nodeRepo.findAllByParentIdAndNameContains(parentId, name, pageRequest)
                .stream()
                .filter(node -> accessProvider.hasPermission(node.getId(), userId, READ)) // TODO control pages size
                .collect(Collectors.toList());
    }

    @Override
    public List<Node> getByOwnerIdAndParentId(String userId, String parentId) throws CustomException {
        //accessProvider.assertGetResource(parentId, userId);
        List<Node> nodeList =  parentId!=null ? nodeRepo.findByParentId(parentId) : nodeRepo.findByParentIdNull();
        return nodeList
                .stream()
                .filter(node -> accessProvider.hasPermission(node.getId(), userId, READ))
                .collect(Collectors.toList());
    }

    @Override
    public Node getByNameAndOwnerIdAndParentId(String nodeName, String userId, String parentId) throws CustomException {
        Node node;
        if(parentId==null){
           node = nodeRepo.findByNameAndParentIdNull(nodeName);
        }else {
            node = nodeRepo.findByNameAndParentIdNotNull(nodeName, parentId);
        }
        if(node!=null)
            accessProvider.hasPermission(node.getId(), userId, READ);
        return node;
    }

    @Override
    public int countById(String nodeId, String userId){
        return nodeRepo.countById(nodeId);
    }

    @Override
    public void deleteById(String nodeId, String userId) throws CustomException {
        accessProvider.assertPermission(nodeId, userId, WRITE);
        nodeRepo.deleteById(nodeId);
    }

}
