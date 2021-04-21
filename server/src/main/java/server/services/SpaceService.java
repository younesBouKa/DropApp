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
import server.repositories.ISpaceRepo;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static server.exceptions.Message.*;

@Service
public class SpaceService implements ISpaceService{
    private final Logger logger = Logger.getLogger(SpaceService.class.getName());

    @Autowired
    private ISpaceRepo spaceRepo;
    @Autowired
    private INodeService nodeService;

    public List<Space> getSpaces(int page, int size,String sortField,String direction,List<String> status, String search) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.valueOf(direction), sortField);
        // TODO here i must add owner id from PrincipalUser (later)
        return spaceRepo.findAllByOwnerIdAndNameContains("ownerid" , search, pageRequest)
                .stream()
                .map(this::addRoots) // add roots for more details
                .collect(Collectors.toList());
    }

    public Space getSpaceById(String id) throws CustomException {
        Space space = spaceRepo.findById(id).orElseThrow(()->new CustomException(NO_SPACE_WITH_GIVEN_ID,id));
        return addRoots(space);
    }

    public Space insertSpace(SpaceIncomingDto spaceIncomingDto) throws CustomException {
        Space space = Space.from(spaceIncomingDto);
        space.setCreationDate(Instant.now());
        Space createdSpace;
        try {
            createdSpace = spaceRepo.insert(space);
        }catch (org.springframework.dao.DuplicateKeyException e){
            throw new CustomException(e, SPACE_ALREADY_EXISTS_WITH_SAME_KEYS, space.getName());
        }
        NodeIncomingDto node = new NodeIncomingDto();
        node.setName("root"); // TODO the default root folder in each space we call it 'root'
        node.setType(NodeType.FOLDER);
        nodeService.createRootFolder(createdSpace, node);
        return addRoots(createdSpace);
    }

    public Space updateSpace(String spaceId, SpaceIncomingDto spaceIncomingDto) throws CustomException {
        Space space = spaceRepo.findById(spaceId).orElseThrow(()->new CustomException(NO_SPACE_WITH_GIVEN_ID, spaceId));
        Space.updateWith(space, spaceIncomingDto);
        space.setLastModificationDate(Instant.now());
        try {
            return spaceRepo.save(space);
        }catch (org.springframework.dao.DuplicateKeyException e){
            throw new CustomException(e, SPACE_ALREADY_EXISTS_WITH_SAME_KEYS, space.getName(), space.getOwnerId());
        }
    }

    public int deleteSpace(String spaceId) {
        int count = spaceRepo.countById(spaceId);
        spaceRepo.deleteById(spaceId);
        return count - spaceRepo.countById(spaceId);
    }

    /**************** tools *************************/
    public Space addRoots(Space space){
        List<NodeNew> roots = nodeService.getRootNodes(space.getId());
        space.setRoots(roots);
        return space;
    }

}
