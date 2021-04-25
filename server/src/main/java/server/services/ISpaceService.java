package server.services;

import server.data.Space;
import server.exceptions.CustomException;
import server.models.SpaceRequest;
import server.user.data.User;

import java.util.List;

public interface ISpaceService {

    List<Space> getSpaces(User user, int page, int size, String sortField, String direction, List<String> status, String search);

    List<Space> getSpaces(User user);

    Space getSpaceById(User user, String spaceId) throws CustomException;

    Space insertSpace(User user, SpaceRequest spaceRequest) throws CustomException;

    Space updateSpace(User user, String spaceId, SpaceRequest spaceRequest) throws CustomException;

    int deleteSpace(User user, String spaceId);
}
