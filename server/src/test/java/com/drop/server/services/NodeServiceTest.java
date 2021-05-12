package com.drop.server.services;


import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;
import server.data.IUser;
import server.data.Node;
import server.exceptions.CustomException;
import server.services.INodeService;
import server.services.NodeService;
import server.user.services.IUserService;
import server.user.services.UserMongoService;

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class NodeServiceTest {

    private INodeService nodeService ;
    private IUserService userService ;
    private IUser user;

    @BeforeEach
    void initUseCase() {
        user = userService.getUserByUsername("younes");
    }

    @Test
    public void getRootNodes() throws CustomException {
        List<Node> nodeList = nodeService.getRootNodes(user);
        Assert.notEmpty(nodeList, "no root nodes was found");
    }
}
