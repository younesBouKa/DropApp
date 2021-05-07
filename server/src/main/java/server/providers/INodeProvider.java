package server.providers;

import org.springframework.data.domain.PageRequest;
import server.data.Node;
import server.exceptions.CustomException;

import java.util.List;
import java.util.Optional;

public interface INodeProvider {

    Node insertNode(String userId, Node node) throws CustomException;

    Node saveNode(String userId, Node node) throws CustomException;

    Optional<Node> getNodeById(String nodeId, String userId) throws CustomException;

    List<Node> getNodesByUserAndParentIdAndName(String userId, String parentId, String name, PageRequest pageRequest) throws CustomException;

    List<Node> getByOwnerIdAndParentId(String userId, String parentId) throws CustomException;

    Node getByNameAndOwnerIdAndParentId(String nodeName, String userId, String parentId) throws CustomException;

    int countById(String nodeId, String userId);

    void deleteById(String nodeId, String userId) throws CustomException;

}
