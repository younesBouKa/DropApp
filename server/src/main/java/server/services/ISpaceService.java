package server.services;

import server.data.Space;
import server.exceptions.CustomException;
import server.models.SpaceIncomingDto;

import java.util.List;

public interface ISpaceService {

    List<Space> getSpaces(int page, int size, String sortField, String direction, List<String> status, String search);

    Space getSpaceById(String id) throws CustomException;

    Space insertSpace(SpaceIncomingDto spaceIncomingDto) throws CustomException;

    Space updateSpace(String spaceId, SpaceIncomingDto spaceIncomingDto) throws CustomException;

    int deleteSpace(String spaceId);
}
