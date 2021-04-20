package server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import server.data.Space;
import server.exceptions.CustomException;
import server.models.NodeIncomingDto;
import server.data.NodeNew;
import server.repositories.INodeRepo;
import server.repositories.ISpaceRepo;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

import static server.exceptions.Message.*;

@Service
public class NodeServiceNew {
    private final Logger logger = Logger.getLogger(NodeServiceNew.class.getName());

    @Autowired
    private INodeRepo nodeRepo;
    @Autowired
    private ISpaceRepo spaceRepo;

    public List<NodeNew> getNodes(String parentId, int page, int size,String sortField,String direction,List<String> status, String search) throws CustomException {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.valueOf(direction), sortField);
        return nodeRepo.findAllByParentIdAndNameContains(parentId, search, pageRequest);
    }

    public NodeNew getNodeById(String spaceId, String nodeId) throws CustomException {
        Space space = spaceRepo.findById(spaceId).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID, spaceId));
        return nodeRepo.findById(nodeId).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID,nodeId));
    }

    public NodeNew insertNode(String spaceId, NodeIncomingDto nodeIncomingDto) throws CustomException {
        // using unique key in mongo
        //if(nodeRepo.existsByNameAndParentId(nodeIncomingDto.getName(), nodeIncomingDto.getParentId()))
        //    throw new CustomException(FILE_ALREADY_EXISTS_WITH_SAME_NAME, nodeIncomingDto.getName());
        Space space = spaceRepo.findById(spaceId).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID, spaceId));
        String parentId = nodeIncomingDto.getParentId();
        NodeNew parentNode = nodeRepo.findById(parentId).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID,parentId));
        NodeNew node = NodeNew.from(nodeIncomingDto);
        node.setSpaceId(spaceId);
        node.setCreationDate(Instant.now());
        node.setSpace(space);
        node.setParent(parentNode);
        try {
            return nodeRepo.insert(node);
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
        node.setSpaceId(spaceId);
        node.setCreationDate(Instant.now());
        node.setSpace(space);
        node.setParent(parentNode);
        node.setModificationDate(Instant.now());
        try {
            return nodeRepo.save(node);
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

    /********** additional fields *******************/
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
