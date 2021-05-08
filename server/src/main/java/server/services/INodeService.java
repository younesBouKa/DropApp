package server.services;

import server.data.IUser;
import server.data.Node;
import server.exceptions.CustomException;
import server.models.NodeFtpRequest;
import server.models.NodeWebRequest;
import server.models.ZipRequest;

import java.util.List;

public interface INodeService {
    // web interface methods
    List<Node> getNodes(IUser user, String parentId, int page, int size, String sortField, String direction, List<String> status, String search) throws CustomException ;
    List<Node> getNodes(IUser user, String parentId) throws CustomException ;
    Node getNodeById(IUser user, String nodeId) throws CustomException;
    Node getNodeWithContent(IUser user, String nodeId) throws CustomException;
    Node getNodeWithContent(IUser user, String nodeId, int startPos, int endPos) throws CustomException;
    Node insertNode(IUser user, NodeWebRequest nodeWebRequest) throws CustomException;
    Node updateNode(IUser user, String nodeId, NodeWebRequest nodeRequest) throws CustomException;
    int deleteNode(IUser user, String nodeId) throws CustomException;
    // ftp interface methods
    Node insertNode(NodeFtpRequest nodeFtpRequest) throws CustomException;
    Node updateNode(NodeFtpRequest nodeFtpRequest) throws CustomException;
    int deleteNode(NodeFtpRequest nodeFtpRequest) throws CustomException;
    // common methods
    List<Node> getRootNodes(IUser user) throws CustomException;
    Node createZipNode(IUser user, ZipRequest zipRequest) throws CustomException;
    Node getNodeFromPath(IUser user, List<String> pathParts, boolean allowInsert) throws CustomException;
}
