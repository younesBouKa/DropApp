package server.services;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import server.data.NodeType;
import server.data.Space;
import server.exceptions.CustomException;
import server.models.NodeIncomingDto;
import server.data.NodeNew;
import server.repositories.IFileRepo;
import server.repositories.INodeRepo;
import server.repositories.ISpaceRepo;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static server.exceptions.Message.*;

@Service
public class NodeServiceNew implements INodeService{
    private final Logger logger = Logger.getLogger(NodeServiceNew.class.getName());

    @Autowired
    private INodeRepo nodeRepo;
    @Autowired
    private ISpaceRepo spaceRepo;
    @Autowired
    private IFileRepo fileRepo;

    public List<NodeNew> getNodes(String parentId, int page, int size,String sortField,String direction,List<String> status, String search) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.valueOf(direction), sortField);
        return nodeRepo.findAllByParentIdAndNameContains(parentId, search, pageRequest)
                .stream()
                //.map(this::addChildren)
                .map(this::addParent)
                .collect(Collectors.toList());
    }

    public NodeNew getNodeById(String spaceId, String nodeId) throws CustomException {
        Space space = spaceRepo.findById(spaceId).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID, spaceId));
        NodeNew node = nodeRepo.findById(nodeId).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID,nodeId));
        return addChildren(addParent(node));
    }

    public NodeNew insertNode(String spaceId, NodeIncomingDto nodeIncomingDto) throws CustomException {
        Space space = spaceRepo.findById(spaceId).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID, spaceId));
        String parentId = nodeIncomingDto.getParentId();
        if(parentId==null && nodeIncomingDto.getType().equals(NodeType.FOLDER)){
            return createRootFolder(space, nodeIncomingDto);
        }
        NodeNew parentNode = nodeRepo.findById(parentId).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID,parentId));
        NodeNew node = NodeNew.from(nodeIncomingDto);
        node.setCreationDate(Instant.now());
        node.setModificationDate(Instant.now());
        node = prepareNodeToSave(space, parentNode, node, nodeIncomingDto);
        try {
            NodeNew createdNode = nodeRepo.insert(node);
            return addParent(createdNode);
        }catch (org.springframework.dao.DuplicateKeyException e){
            throw new CustomException(FILE_ALREADY_EXISTS_WITH_SAME_NAME, node.getName());
        }
    }

    public NodeNew updateNode(String spaceId, String nodeId, NodeIncomingDto nodeIncomingDto) throws CustomException {
        Space space = spaceRepo.findById(spaceId).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID, spaceId));
        NodeNew node = nodeRepo.findById(nodeId).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID, nodeId));
        String parentId = nodeIncomingDto.getParentId();
        NodeNew parentNode = nodeRepo.findById(parentId).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID,parentId));
        NodeNew.updateWith(node,nodeIncomingDto);
        node.setModificationDate(Instant.now());
        node = prepareNodeToSave(space, parentNode, node, nodeIncomingDto);
        try {
            NodeNew createdNode = nodeRepo.save(node);
            return addParent(createdNode);
        }catch (org.springframework.dao.DuplicateKeyException e){
          throw new CustomException(FILE_ALREADY_EXISTS_WITH_SAME_NAME, node.getName());
        }
    }

    public int deleteNode(String spaceId, String nodeId) throws CustomException {
        Space space = spaceRepo.findById(spaceId).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID, spaceId));
        int count = nodeRepo.countById(nodeId);
        nodeRepo.deleteById(nodeId);
        return count - nodeRepo.countById(nodeId);
    }

    public NodeNew createRootFolder(Space space, NodeIncomingDto nodeInfo) throws CustomException {
        if(space==null || space.getId()==null)
            throw new CustomException(NO_NODE_WITH_GIVEN_ID,null);
        nodeInfo.setParentId(null);
        NodeNew node = NodeNew.from(nodeInfo);
        node.setSpaceId(space.getId());
        node.setSpace(space);
        try {
            return nodeRepo.insert(node);
        }catch (org.springframework.dao.DuplicateKeyException e){
            throw new CustomException(FILE_ALREADY_EXISTS_WITH_SAME_NAME, node.getName());
        }
    }

    public List<NodeNew> getRootNodes(String spaceId){
        return nodeRepo.findBySpaceIdAndParentId(spaceId,null);
    }

    /********** Tools *******************/

    public NodeNew prepareNodeToSave(Space space, NodeNew parent, NodeNew node, NodeIncomingDto nodeIncomingDto) throws CustomException {
        node.setSpaceId(space.getId());
        node.setCreationDate(Instant.now());
        node.setSpace(space);
        node.setParent(parent);
        MultipartFile file = nodeIncomingDto.getFile();
        if(file!=null && file.getSize()>0){
            node.setType(NodeType.FILE);
            ObjectId fileId = fileRepo.saveFile(file, node);
            node.setFileId(fileId.toString());
            node.setContentType(file.getContentType());
            node.setFileSize(file.getSize());
        }
        return node;
    }

    public NodeNew addChildren(NodeNew node){
        List<NodeNew> children = nodeRepo.findByParentId(node.getId());
        node.setChildren(children);
        return node;
    }

    public NodeNew addParent(NodeNew node){
        if(node.getParentId()==null)
            return node;
        NodeNew parent = nodeRepo.findById(node.getParentId()).orElse(null);
        node.setParent(parent);
        return node;
    }
}
