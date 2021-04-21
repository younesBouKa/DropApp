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
                //.map(this::addChildren) // overloading response
                .map(this::addParent) // adding parents
                .collect(Collectors.toList());
    }

    public NodeNew getNodeById(String spaceId, String nodeId) throws CustomException {
        // splicing it to give more details in error cas
        Space space = spaceRepo.findById(spaceId).orElseThrow(()->new CustomException(NO_SPACE_WITH_GIVEN_ID, spaceId));
        NodeNew node = nodeRepo.findById(nodeId).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID,nodeId));
        return addChildren(addParent(node));
    }

    public NodeNew insertNode(String spaceId, NodeIncomingDto nodeIncomingDto) throws CustomException {
        Space space = spaceRepo.findById(spaceId).orElseThrow(()->new CustomException(NO_SPACE_WITH_GIVEN_ID, spaceId));
        String parentId = nodeIncomingDto.getParentId();
        // try to create a root folder (no parentId and type=FOLDER)
        if(parentId==null && NodeType.FOLDER.equals(nodeIncomingDto.getType())){
            return createRootFolder(space, nodeIncomingDto);
        }
        // else we must have parentId
        NodeNew parentNode = nodeRepo.findById(parentId).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_PARENT_ID,parentId));
        NodeNew node = NodeNew.from(nodeIncomingDto);
        node.setCreationDate(Instant.now());
        node.setModificationDate(Instant.now());
        node = prepareNodeToSave(space, parentNode, node, nodeIncomingDto);
        try {
            NodeNew createdNode = nodeRepo.insert(node);
            return addParent(createdNode); // just more details
        }catch (org.springframework.dao.DuplicateKeyException e){
            throw new CustomException(e, NODE_ALREADY_EXISTS_WITH_SAME_KEYS, node.getName(), space.getId(), parentNode.getId());
        }
    }

    public NodeNew updateNode(String spaceId, String nodeId, NodeIncomingDto nodeIncomingDto) throws CustomException {
        // splicing validation for more details in error cas
        Space space = spaceRepo.findById(spaceId).orElseThrow(()->new CustomException(NO_SPACE_WITH_GIVEN_ID, spaceId));
        NodeNew node = nodeRepo.findById(nodeId).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID, nodeId));
        String parentId = nodeIncomingDto.getParentId();
        // try to updating a root folder
        if(parentId==null && NodeType.FOLDER.equals(nodeIncomingDto.getType())){
            return updateRootFolder(node, space, nodeIncomingDto);
        }
        // else we update a node
        NodeNew parentNode = nodeRepo.findById(parentId).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_PARENT_ID,parentId));
        NodeNew.updateWith(node,nodeIncomingDto);
        node.setModificationDate(Instant.now());
        node = prepareNodeToSave(space, parentNode, node, nodeIncomingDto);
        try { // catch duplicate key exception
            NodeNew createdNode = nodeRepo.save(node);
            return addParent(createdNode);
        }catch (org.springframework.dao.DuplicateKeyException e){
          throw new CustomException(e, NODE_ALREADY_EXISTS_WITH_SAME_KEYS, node.getName(), space.getId(), parentNode.getId());
        }
    }

    public int deleteNode(String spaceId, String nodeId) throws CustomException {
        // just counting before and after deleting
        Space space = spaceRepo.findById(spaceId).orElseThrow(()->new CustomException(NO_SPACE_WITH_GIVEN_ID, spaceId));
        int count = nodeRepo.countById(nodeId);
        nodeRepo.deleteById(nodeId);
        return count - nodeRepo.countById(nodeId);
    }

    public NodeNew createRootFolder(Space space, NodeIncomingDto nodeInfo) throws CustomException {
        // custom process for root folders creation
        if(space==null || space.getId()==null)
            throw new CustomException(NO_SPACE_WITH_GIVEN_ID,null);
        nodeInfo.setParentId(null);
        NodeNew node = NodeNew.from(nodeInfo);
        node.setSpaceId(space.getId());
        node.setSpace(space);
        node.setType(NodeType.FOLDER);
        try {
            return nodeRepo.insert(node);
        }catch (org.springframework.dao.DuplicateKeyException e){
            throw new CustomException(e, NODE_ALREADY_EXISTS_WITH_SAME_KEYS, node.getName(), space.getId(), null);
        }
    }

    private NodeNew updateRootFolder(NodeNew node, Space space, NodeIncomingDto nodeInfo) throws CustomException {
        // also for update
        if(space==null || space.getId()==null)
            throw new CustomException(NO_SPACE_WITH_GIVEN_ID,null);
        nodeInfo.setParentId(null);
        NodeNew.updateWith(node, nodeInfo);
        node.setSpaceId(space.getId());
        node.setSpace(space);
        node.setType(NodeType.FOLDER);
        try {
            return nodeRepo.save(node);
        }catch (org.springframework.dao.DuplicateKeyException e){
            throw new CustomException(e, NODE_ALREADY_EXISTS_WITH_SAME_KEYS, node.getName(), space.getId(), null);
        }
    }

    public List<NodeNew> getRootNodes(String spaceId){
        return nodeRepo.findBySpaceIdAndParentId(spaceId,null);
    }

    /********** Tools *******************/

    public NodeNew prepareNodeToSave(Space space, NodeNew parent, NodeNew node, NodeIncomingDto nodeIncomingDto) throws CustomException {
        // adding some fresh information to node before saving it in db
        node.setSpaceId(space.getId());
        node.setSpace(space);
        node.setParent(parent);
        MultipartFile file = nodeIncomingDto.getFile();
        if(NodeType.FILE.equals(nodeIncomingDto.getType()) && file!=null && file.getSize()>0){
            // information of file
            node.setType(NodeType.FILE);
            ObjectId fileId = fileRepo.saveFile(file, node);
            node.setFileId(fileId.toString());
            node.setContentType(file.getContentType());
            node.setFileSize(file.getSize());
        }
        return node;
    }

    public NodeNew addChildren(NodeNew node){
        // add children if exist
        List<NodeNew> children = nodeRepo.findByParentId(node.getId());
        node.setChildren(children);
        return node;
    }

    public NodeNew addParent(NodeNew node){
        // add parent if exist
        if(node.getParentId()==null)
            return node;
        NodeNew parent = nodeRepo.findById(node.getParentId()).orElse(null);
        node.setParent(parent);
        return node;
    }
}
