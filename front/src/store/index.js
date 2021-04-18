import Vue from 'vue'
import Vuex from 'vuex'

import api from '../services/api';
const _ = require('lodash');
Vue.use(Vuex)

const FILE_API_PATH = "/api/file/"
const NODE_API_PATH = "/api/node/"

export default new Vuex.Store({
    state: {
        user: {},
        rootPath : "/",
        currentNodeData: {},
        currentNodeElement: {},
        token: "",
        rootNodeElement : {}, // from tree
        loadedNodes : [], // from tree
        clipBoardNodes : [],
        clipBoardOperation : "COPY", // by default
        permissionOptions : {
            "read":{
                icon : "el-icon-view",
                label : "Read",
                type : "primary"
            },
            "write":{
                icon : "el-icon-edit",
                label : "Write",
                type : "success"
            },
            "none":{
                icon : "el-icon-circle-close",
                label : "None",
                type : "danger"
            },
        },
        sortByFoldersFirst : (a,b)=> a.type===b.type ? 0 : (a.type==="FOLDER" ? -1 : 1),
    },
    getters: {
        getCurrentNodeData : (state, getters) =>{
            return state.currentNodeData || {};
        },
        getCurrentNodeElement : (state, getters) =>{
            return state.currentNodeElement || {};
        },
        getRootNodeElement : (state, getters) =>{
            return state.rootNodeElement || {};
        },
        getLoadedNodes : (state, getters) =>{
            return state.loadedNodes || [];
        },
        getPermissionOptions : (state, getters) =>{
            return state.permissionOptions;
        },
        getSortByFolderFirst : (state, getters) => {
            return state.sortByFoldersFirst;
        },
        getNodesInClipBoard : (state, getters) => {
            return state.clipBoardNodes;
        }
    },
    mutations: {
        storeCurrentNodeData(state, data) {
            console.log("mutation [storeCurrentNodeData] ",data);
            state.currentNodeData = data ;
            if(state.loadedNodes.length>0){
                let index = state.loadedNodes.indexOf(elt=> elt.data.id === data.id);
                if(index!==-1)
                    state.currentNodeElement = state.loadedNodes[index];
            }
        },
        storeCurrentNodeElement(state, node){
            console.log("mutation [storeCurrentNodeElement] ",node);
            state.currentNodeElement = node ;
            if(!state.currentNodeData || state.currentNodeData.id!==node.data.id)
                state.currentNodeData = node.data
        },
        storeRootNodeElement(state, root){
            console.log("mutation [storeRootNodeElement] ",root);
            state.rootNodeElement = root ;
        },
        storeLoadedNodes(state, elements){
            console.log("mutation [storeLoadedNodes] ",elements);
            state.loadedNodes = elements;
        },
        setNodesInClipBoard(state, elements, operationType){
            console.log("mutation [setNodesInClipBoard] ",elements, operationType);
            state.clipBoardNodes = elements;
            state.clipBoardOperation = operationType || "COPY";
        },
    },
    actions: {
        storeRootElement({state, getters, commit, dispatch}, root) {
            return new Promise((resolve, reject) => {
                try {
                    commit("storeRootNodeElement", root);
                    let getChildren = function(parent){
                        let children= [parent];
                        if(parent && parent.childNodes){
                            (parent.childNodes||[]).forEach(child=>{
                                children = _.concat(children,getChildren(child));
                            });
                        }
                        return children;
                    }
                    let nodes = _.uniq(getChildren(root));
                    commit("storeLoadedNodes", nodes);
                    resolve({
                        "root": root,
                        "loadedNodes": nodes
                    });
                }catch(error){
                    reject(error);
                }
            });
        },
        getAllAvailablePathsFrom({state, getters, commit, dispatch}) {
            return new Promise((resolve, reject) => {
                try {
                    let paths = (getters.getLoadedNodes||[]).map(node=>{
                        if(node.data[0])
                            return node.data[0];
                        else
                            return node.data;
                    });
                    paths = _.uniq(paths);
                    resolve(paths);
                }catch(error){
                    reject(error);
                }
            });
        },

        pasteNodesIn({state, getters, commit, dispatch}, destNode) {
            return new Promise((resolve, reject) => {
                try {
                    destNode = destNode ? destNode : getters.getCurrentNodeData;
                    if(state.clipBoardNodes.length===0)
                      reject("no node was selected");
                    if(!destNode || !destNode.id)
                      reject("destination node is invalid");

                    let operation = state.clipBoardOperation==="CUT" ? "copyNodeById" : (state.clipBoardOperation==="COPY" ? "moveNodeById" : undefined);
                    if(!operation)
                        reject("operation undefined");
                    let srcId= state.clipBoardNodes[0].id,destId = destNode.id;
                    dispatch(operation,  {srcId,destId})
                        .then(response=> resolve(response))
                        .catch(error=> reject(error));
                }catch(error){
                    reject(error);
                }
            });
        },

        getNodeById({state, getters, commit, dispatch}, nodeId) {
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "getNodeById",
                    {nodeId},
                    {},
                    (response) => {
                        console.log("getNodeById",response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error("getNodeById",error);
                        reject(error);
                    }
                )
            })
        },
        getNodeInfoById({state, getters, commit, dispatch}, nodeId) {
            return new Promise((resolve, reject) => {
                api.getData(
                    NODE_API_PATH + "getNodeInfoById/"+nodeId,
                    undefined,
                    {},
                    (response) => {
                        console.log("getNodeInfoById",response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error("getNodeInfoById",error);
                        reject(error);
                    }
                )
            })
        },
        getNodeByPath({state, getters, commit, dispatch}, nodePath) {
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "getNodeByPath",
                    {nodePath},
                    {},
                    (response) => {
                        console.log("getNodeByPath",response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error("getNodeByPath",error);
                        reject(error);
                    }
                )
            })
        },
        getNodesByParentId({state, getters, commit, dispatch}, parentId) {
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "getNodesByParentId",
                    {parentId},
                    {},
                    (response) => {
                        console.log("getNodesByParentId",response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error("getNodesByParentId",error);
                        reject(error);
                    }
                )
            })
        },
        getNodesByParentPath({state, getters, commit, dispatch}, parentPath) {
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "getNodesByParentPath",
                    {parentPath},
                    {},
                    (response) => {
                        console.log( "getNodesByParentPath",response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error( "getNodesByParentPath",error);
                        reject(error);
                    }
                )
            })
        },
        getNodesByQuery({state, getters, commit, dispatch}, query) {
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "getNodesByQuery",
                    query,
                    {},
                    (response) => {
                        console.log("getNodesByQuery",response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error("getNodesByQuery",error);
                        reject(error);
                    }
                )
            })
        },

        createFolderNodeWithMetaData({state, getters, commit, dispatch}, metaData) {
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "createFolderNodeWithMetaData",
                    metaData,
                    {},
                    (response) => {
                        console.log("createFolderNodeWithMetaData",response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error("createFolderNodeWithMetaData",error);
                        reject(error);
                    }
                )
            })
        },

        deleteNodeByPath({state, getters, commit, dispatch}, {path, recursive}) {
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "deleteNodeByPath",
                    {path, recursive},
                    {},
                    (response) => {
                        console.log("deleteNodeByPath",response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error("deleteNodeByPath",error);
                        reject(error);
                    }
                )
            })
        },
        deleteNodeById({state, getters, commit, dispatch}, {nodeId, recursive}) {
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "deleteNodeById",
                    {nodeId, recursive},
                    {},
                    (response) => {
                        console.log("deleteNodeById",response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error("deleteNodeById",error);
                        reject(error);
                    }
                )
            })
        },
        deleteNodesById({state, getters, commit, dispatch}, {nodesId, recursive}) {
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "deleteNodesById",
                    {nodesId, recursive},
                    {},
                    (response) => {
                        console.log("deleteNodesById",response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error("deleteNodesById",error);
                        reject(error);
                    }
                )
            })
        },

        hasPermissionById({state, getters, commit, dispatch}, {nodeId, permission}) {
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "hasPermissionById",
                    {nodeId, permission},
                    {},
                    (response) => {
                        console.log("hasPermissionById",response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error("hasPermissionById",error);
                        reject(error);
                    }
                )
            })
        },
        hasPermissionByPath({state, getters, commit, dispatch}, {nodePath, permission}) {
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "hasPermissionByPath",
                    {nodePath, permission},
                    {},
                    (response) => {
                        console.log("hasPermissionByPath",response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error("hasPermissionByPath",error);
                        reject(error);
                    }
                )
            })
        },

        copyNodeByPath({state, getters, commit, dispatch}, {srcPath, destPath}) {
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "hasPermissionByPath",
                    {srcPath, destPath},
                    {},
                    (response) => {
                        console.log("hasPermissionByPath",response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error("hasPermissionByPath",error);
                        reject(error);
                    }
                )
            })
        },
        copyNodeById({state, getters, commit, dispatch}, {srcId, destId}) {
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "copyNodeById",
                    {srcId, destId},
                    {},
                    (response) => {
                        console.log("copyNodeById",response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error("copyNodeById",error);
                        reject(error);
                    }
                )
            })
        },

        moveNodeByPath({state, getters, commit, dispatch}, {srcPath, destPath}) {
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "moveNodeByPath",
                    {srcPath, destPath},
                    {},
                    (response) => {
                        console.log("moveNodeByPath",response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error("moveNodeByPath",error);
                        reject(error);
                    }
                )
            })
        },
        moveNodeById({state, getters, commit, dispatch}, {srcId, destId}) {
            return new Promise((resolve, reject) => {
                console.log("++++++",srcId,destId);
                api.postData(
                    NODE_API_PATH + "moveNodeById",
                    {srcId, destId},
                    {},
                    (response) => {
                        console.log("moveNodeById",response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error("moveNodeById",error);
                        reject(error);
                    }
                )
            })
        },

        uploadFile({state, getters, commit, dispatch}, formData) {
            return new Promise((resolve, reject) => {
                api.getData(
                    FILE_API_PATH + "uploadFile",
                    formData,
                    {},
                    (response) => {
                        console.log("uploadFile",response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error("uploadFile",error);
                        reject(error);
                    }
                )
            })
        },

        getCompressedNodes({state, getters, commit, dispatch}, nodesId) {
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "getCompressedNodes",
                    nodesId,
                    {},
                    (response) => {
                        console.log(response);
                        resolve({
                            contentType : response.headers["content-type"],
                            data : response.data
                        });
                    },
                    (error) => {
                        console.error(error);
                        reject(error);
                    }
                )
            })
        },
        getCompressedFolder({state, getters, commit, dispatch}, folderId) {
            return new Promise((resolve, reject) => {
                api.getData(
                    NODE_API_PATH + "getCompressedFolder/"+folderId,
                    undefined,
                    {},
                    (response) => {
                        console.log(response);
                        resolve({
                            contentType : response.headers["content-type"],
                            data : response.data
                        });
                    },
                    (error) => {
                        console.error(error);
                        reject(error);
                    }
                )
            })
        },
        streamNodeContent({state, getters, commit, dispatch}, nodeId) {
            return new Promise((resolve, reject) => {
                api.getData(
                    NODE_API_PATH + "streamContent/"+nodeId,
                    undefined,
                    {},
                    (response) => {
                        console.log(response);
                        resolve({
                            contentType : response.headers["content-type"],
                            data : response.data
                        });
                    },
                    (error) => {
                        console.error(error);
                        reject(error);
                    }
                )
            })
        },
        streamFileContent({state, getters, commit, dispatch}, fileId) {
            return new Promise((resolve, reject) => {
                api.getData(
                    FILE_API_PATH + "streamContent/"+fileId,
                    undefined,
                    {},
                    (response) => {
                        console.log(response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error(error);
                        reject(error);
                    }
                )
            })
        },
        getFileInfo({state, getters, commit, dispatch}, fileId) {
            return new Promise((resolve, reject) => {
                api.getData(
                    FILE_API_PATH + "getFileInfo/"+fileId,
                    undefined,
                    {},
                    (response) => {
                        console.log(response);
                        resolve(response.data);
                    },
                    (error) => {
                        console.error(error);
                        reject(error);
                    }
                )
            })
        },
    },
    modules: {}
})
