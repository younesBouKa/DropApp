package server.services;

import server.data.NodeNew;
import server.data.Space;
import server.exceptions.CustomException;
import server.models.NodeIncomingDto;

import java.util.List;

public interface INodeService {

    List<NodeNew> getNodes(String parentId, int page, int size, String sortField, String direction, List<String> status, String search) throws CustomException ;

    NodeNew getNodeById(String spaceId, String nodeId) throws CustomException;

    NodeNew insertNode(String spaceId, NodeIncomingDto nodeIncomingDto) throws CustomException;

    NodeNew updateNode(String spaceId, String nodeId, NodeIncomingDto nodeIncomingDto) throws CustomException;

    int deleteNode(String spaceId, String nodeId) throws CustomException;

    NodeNew createRootFolder(Space space, NodeIncomingDto nodeInfo) throws CustomException;

    List<NodeNew> getRootNodes(String spaceId);
}
