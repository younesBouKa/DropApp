package server.services;

import server.data.IUser;
import server.data.Node;
import server.dot.ZipDotIn;
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

    Node getNodeByIdAndOptions(IUser user, String nodeId, boolean addParent, boolean addChildren, boolean addContent) throws CustomException;

    Node getNodeWithContent(IUser user, String nodeId) throws CustomException;
    Node getNodeWithContent(IUser user, String nodeId, int startPos, int endPos) throws CustomException;

    Node getZippedNodes(IUser user, ZipDotIn zipDotIn) throws CustomException;

    Node createZipNode(IUser user, ZipDotIn zipDotIn) throws CustomException;

    Node copyNode(IUser user, String srcNodeId, String destNodeId, boolean andRemove) throws CustomException;

    List<Node> copyNodes(IUser user, List<String> srcNodeIds, String destNodeId, boolean andRemove) throws CustomException;

    Node insertNode(IUser user, NodeWebRequest nodeWebRequest) throws CustomException;
    Node updateNode(IUser user, String nodeId, NodeWebRequest nodeRequest) throws CustomException;
    int deleteNode(IUser user, String nodeId) throws CustomException;
    // ftp interface methods
    Node insertNode(NodeFtpRequest nodeFtpRequest) throws CustomException;
    Node updateNode(NodeFtpRequest nodeFtpRequest) throws CustomException;
    int deleteNode(NodeFtpRequest nodeFtpRequest) throws CustomException;
    // common methods
    List<Node> getRootNodes(IUser user) throws CustomException;
    Node getNodeFromPath(IUser user, List<String> pathParts, boolean allowInsert) throws CustomException;
}
