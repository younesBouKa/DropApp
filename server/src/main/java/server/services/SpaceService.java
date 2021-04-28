package server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import server.data.IUser;
import server.data.NodeNew;
import server.data.NodeType;
import server.data.Space;
import server.exceptions.CustomException;
import server.models.NodeWebRequest;
import server.models.SpaceRequest;
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

    @Override
    public List<Space> getSpaces(IUser user) {
        // TODO here i must add owner id from PrincipalUser (later)
        return spaceRepo.findAllByOwnerId(user.getId())
                .stream()
                .map(this::addRoots) // add roots for more details
                .collect(Collectors.toList());
    }

    @Override
    public List<Space> getSpaces(IUser user, int page, int size,String sortField,String direction,List<String> status, String search) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.valueOf(direction), sortField);
        // TODO here i must add owner id from PrincipalUser (later)
        return spaceRepo.findAllByOwnerIdAndNameContains(user.getId() , search, pageRequest)
                .stream()
                .map(this::addRoots) // add roots for more details
                .collect(Collectors.toList());
    }

    @Override
    public Space getSpaceById(IUser user, String spaceId) throws CustomException {
        Space space = spaceRepo.findByIdAndOwnerId(spaceId, user.getId()).orElseThrow(()->new CustomException(NO_SPACE_WITH_GIVEN_ID,spaceId));
        return addRoots(space);
    }

    @Override
    public Space insertSpace(IUser user, SpaceRequest spaceRequest) throws CustomException {
        Space space = SpaceRequest.spaceFromRequest(user, spaceRequest);
        space.setCreationDate(Instant.now());
        Space createdSpace;
        try {
            createdSpace = spaceRepo.insert(space);
        }catch (org.springframework.dao.DuplicateKeyException e){
            throw new CustomException(e, SPACE_ALREADY_EXISTS_WITH_SAME_KEYS, space.getName());
        }
        NodeWebRequest node = new NodeWebRequest();
        node.setName(user.getHomeDirectory()); // TODO the default root folder in each space we call it 'root'
        node.setType(NodeType.FOLDER);
        nodeService.createRootFolder(createdSpace, node);
        return addRoots(createdSpace);
    }

    @Override
    public Space updateSpace(IUser user, String spaceId, SpaceRequest spaceRequest) throws CustomException {
        Space space = spaceRepo.findById(spaceId).orElseThrow(()->new CustomException(NO_SPACE_WITH_GIVEN_ID, spaceId));
        SpaceRequest.updateSpaceWithRequest(space, user, spaceRequest);
        space.setLastModificationDate(Instant.now());
        try {
            return spaceRepo.save(space);
        }catch (org.springframework.dao.DuplicateKeyException e){
            throw new CustomException(e, SPACE_ALREADY_EXISTS_WITH_SAME_KEYS, space.getName(), space.getOwnerId());
        }
    }

    @Override
    public int deleteSpace(IUser user, String spaceId) {
        int count = spaceRepo.countById(spaceId);
        spaceRepo.deleteByIdAndOwnerId(spaceId, user.getId());
        return count - spaceRepo.countById(spaceId);
    }

    /**************** tools *************************/
    public Space addRoots(Space space){
        List<NodeNew> roots = nodeService.getRootNodes(space.getId());
        space.setRoots(roots);
        return space;
    }
}
