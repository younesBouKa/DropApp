package server.services;

import server.data.IUser;
import server.data.Space;
import server.exceptions.CustomException;
import server.models.SpaceRequest;

import java.util.List;

public interface ISpaceService {

    List<Space> getSpaces(IUser user, int page, int size, String sortField, String direction, List<String> status, String search);

    List<Space> getSpaces(IUser user);

    Space getSpaceById(IUser user, String spaceId) throws CustomException;

    Space insertSpace(IUser user, SpaceRequest spaceRequest) throws CustomException;

    Space updateSpace(IUser user, String spaceId, SpaceRequest spaceRequest) throws CustomException;

    int deleteSpace(IUser user, String spaceId);
}
