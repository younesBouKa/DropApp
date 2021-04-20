package server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import server.data.NodeNew;
import server.data.NodeType;
import server.data.Space;
import server.exceptions.CustomException;
import server.models.NodeIncomingDto;
import server.models.SpaceIncomingDto;
import server.repositories.INodeRepo;
import server.repositories.ISpaceRepo;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

import static server.exceptions.Message.*;

@Service
public class SpaceService {
    private final Logger logger = Logger.getLogger(SpaceService.class.getName());

    @Autowired
    private ISpaceRepo spaceRepo;

    @Autowired
    private INodeRepo nodeRepo;

    public List<Space> getSpaces(int page, int size,String sortField,String direction,List<String> status, String search) throws CustomException {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.valueOf(direction), sortField);
        return spaceRepo.findAllByOwnerIdAndNameContains("ownerid" , search, pageRequest);
    }

    public Space getSpaceById(String id) throws CustomException {
        return spaceRepo.findById(id).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID,id));
    }

    public Space insertSpace(SpaceIncomingDto spaceIncomingDto) throws CustomException {
        // using unique key in mongo
        //if(spaceRepo.existsByNameAndOwnerId(spaceIncomingDto.getName(), spaceIncomingDto.getOwnerId()))
        //    throw new CustomException(FILE_ALREADY_EXISTS_WITH_SAME_NAME, spaceIncomingDto.getName());
        Space space = from(spaceIncomingDto);
        space.setCreationDate(Instant.now());
        Space createdSpace;
        try {
            createdSpace = spaceRepo.insert(space);
        }catch (org.springframework.dao.DuplicateKeyException e){
            throw new CustomException(FILE_ALREADY_EXISTS_WITH_SAME_NAME, space.getName());
        }
        NodeIncomingDto node = new NodeIncomingDto();
        node.setName("root");
        node.setType(NodeType.FOLDER);
        createRootNode(createdSpace, node);
        return createdSpace;
    }

    public Space updateSpace(String spaceId, SpaceIncomingDto spaceIncomingDto) throws CustomException {
        Space space = spaceRepo.findById(spaceId).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID, spaceId));
        updateWith(space, spaceIncomingDto);
        space.setLastModificationDate(Instant.now());
        try {
            return spaceRepo.save(space);
        }catch (org.springframework.dao.DuplicateKeyException e){
            throw new CustomException(FILE_ALREADY_EXISTS_WITH_SAME_NAME, space.getName());
        }
    }

    public int deleteSpace(String spaceId) throws CustomException {
        int count = spaceRepo.countById(spaceId);
        spaceRepo.deleteById(spaceId);
        return count - spaceRepo.countById(spaceId);
    }

    /**************** tools *************************/
    public NodeNew createRootNode(String spaceId, NodeIncomingDto nodeInfo) throws CustomException {
        Space space = getSpaceById(spaceId);
        return createRootNode(space, nodeInfo);
    }

    public NodeNew createRootNode(Space space, NodeIncomingDto nodeInfo) throws CustomException {
        if(space==null || space.getId()==null)
            throw new CustomException(NO_NODE_WITH_GIVEN_ID,null);
        NodeNew node = NodeNew.from(nodeInfo);
        node.setSpaceId(space.getId());
        node.setSpace(space);
        try {
            return nodeRepo.insert(node);
        }catch (org.springframework.dao.DuplicateKeyException e){
            throw new CustomException(FILE_ALREADY_EXISTS_WITH_SAME_NAME, node.getName());
        }
    }

    public Space from(SpaceIncomingDto spaceIncomingDto) {
        Space space = updateWith(new Space(), spaceIncomingDto);
        return space;
    }

    public static Space updateWith(Space space, SpaceIncomingDto spaceIncomingDto) {
        space.setName(spaceIncomingDto.getName());
        space.setOwnerId(spaceIncomingDto.getOwnerId());
        space.setRootPath(spaceIncomingDto.getRootPath());
        return space;
    }

    public Space addRoots(Space space){
        List<NodeNew> roots = nodeRepo.findBySpaceIdAndParentId(space.getId(),null);
        space.setRoots(roots);
        return space;
    }

}
