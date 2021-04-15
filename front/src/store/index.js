import Vue from 'vue'
import Vuex from 'vuex'

const axios = require('axios');
const _ = require('lodash');

Vue.use(Vuex)

const FILE_API_PATH = "/api/file/"
const NODE_API_PATH = "/api/node/"

const defaultOptions = {
    "timeout": 4000,    // 4 seconds timeout
    "Content-Type": "application/json",
    "headers": {
        'X-Custom-Header': 'value' // just example
    }
};

const postData = function (url, data, options, successCallback, errorCallback) {
    options = _.merge(defaultOptions, options);
    axios.post(url, data, options)
        .then(successCallback)
        .catch(errorCallback)
}

const getData = function (url, data, options, successCallback, errorCallback) {
    options = _.merge(defaultOptions, options);
    options.url = options.url + "?" + encodeURI(JSON.stringify(data));
    axios.get(options)
        .then(successCallback)
        .catch(errorCallback)
}

export default new Vuex.Store({
    state: {
        user: {},
        rootPath : "/",
        currentNodeData: {},
        currentNodeElement: {},
        token: "",
        rootNodeElement : {}, // from tree
        loadedNodes : [], // from tree
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
        }
    },
    mutations: {
        storeCurrentNodeData(state, data) {
            console.log("mutation [storeCurrentNodeData] : "+data);
            state.currentNodeData = data ;
            if(state.loadedNodes.length>0){
                let index = state.loadedNodes.indexOf(elt=> elt.data.id === data.id);
                if(index!==-1)
                    state.currentNodeElement = state.loadedNodes[index];
            }
        },
        storeCurrentNodeElement(state, node){
            console.log("mutation [storeCurrentNodeElement] : "+node);
            state.currentNodeElement = node ;
            if(!state.currentNodeData || state.currentNodeData.id!==node.data.id)
                state.currentNodeData = node.data
        },
        storeRootNodeElement(state, root){
            console.log("mutation [storeRootNodeElement] : "+root);
            state.rootNodeElement = root ;
        },
        storeLoadedNodes(state, elements){
            console.log("mutation [storeLoadedNodes] : "+elements);
            state.loadedNodes = elements;
        },
    },
    actions: {
        storeRootElement({state, getters, commit, dispatch}, root) {
            return new Promise((resolve, reject) => {
                try {
                    commit("storeRootNodeElement", root);
                    let getChildren = function(parent){
                        let children= [parent];
                        (parent.childNodes||[]).forEach(child=>{
                            children = _.concat(children,getChildren(child));
                        });
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

        getNodeById({state, getters, commit, dispatch}, nodeId) {
            return new Promise((resolve, reject) => {
                postData(
                    NODE_API_PATH + "getNodeById",
                    {nodeId},
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
        getNodeByPath({state, getters, commit, dispatch}, nodePath) {
            return new Promise((resolve, reject) => {
                postData(
                    NODE_API_PATH + "getNodeByPath",
                    {nodePath},
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
        getNodesByParentId({state, getters, commit, dispatch}, parentId) {
            return new Promise((resolve, reject) => {
                postData(
                    NODE_API_PATH + "getNodesByParentId",
                    {parentId},
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
        getNodesByParentPath({state, getters, commit, dispatch}, parentPath) {
            return new Promise((resolve, reject) => {
                postData(
                    NODE_API_PATH + "getNodesByParentPath",
                    {parentPath},
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
        getNodesByQuery({state, getters, commit, dispatch}, query) {
            return new Promise((resolve, reject) => {
                postData(
                    NODE_API_PATH + "getNodesByQuery",
                    query,
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

        createFolderNodeWithMetaData({state, getters, commit, dispatch}, metaData) {
            return new Promise((resolve, reject) => {
                postData(
                    NODE_API_PATH + "createFolderNodeWithMetaData",
                    metaData,
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

        deleteNodeByPath({state, getters, commit, dispatch}, path, recursive) {
            return new Promise((resolve, reject) => {
                postData(
                    NODE_API_PATH + "deleteNodeByPath",
                    {path, recursive},
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
        deleteNodeById({state, getters, commit, dispatch}, nodeId, recursive) {
            return new Promise((resolve, reject) => {
                postData(
                    NODE_API_PATH + "deleteNodeById",
                    {nodeId, recursive},
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
        deleteNodesById({state, getters, commit, dispatch}, nodesId, recursive) {
            return new Promise((resolve, reject) => {
                postData(
                    NODE_API_PATH + "deleteNodesById",
                    {nodesId, recursive},
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

        hasPermissionById({state, getters, commit, dispatch}, nodeId, permission) {
            return new Promise((resolve, reject) => {
                postData(
                    NODE_API_PATH + "hasPermissionById",
                    {nodeId, permission},
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
        hasPermissionByPath({state, getters, commit, dispatch}, nodePath, permission) {
            return new Promise((resolve, reject) => {
                postData(
                    NODE_API_PATH + "hasPermissionByPath",
                    {nodePath, permission},
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

        copyNodeByPath({state, getters, commit, dispatch}, srcPath, destPath) {
            return new Promise((resolve, reject) => {
                postData(
                    NODE_API_PATH + "hasPermissionByPath",
                    {srcPath, destPath},
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
        copyNodeById({state, getters, commit, dispatch}, srcId, destId) {
            return new Promise((resolve, reject) => {
                postData(
                    NODE_API_PATH + "copyNodeById",
                    {srcId, destId},
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

        moveNodeByPath({state, getters, commit, dispatch}, srcPath, destPath) {
            return new Promise((resolve, reject) => {
                postData(
                    NODE_API_PATH + "moveNodeByPath",
                    {srcPath, destPath},
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
        moveNodeById({state, getters, commit, dispatch}, srcId, destId) {
            return new Promise((resolve, reject) => {
                postData(
                    NODE_API_PATH + "moveNodeById",
                    {srcId, destId},
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

        uploadFile({state, getters, commit, dispatch}, formData) {
            return new Promise((resolve, reject) => {
                getData(
                    FILE_API_PATH + "uploadFile",
                    formData,
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

        streamContent({state, getters, commit, dispatch}, fileId) {
            return new Promise((resolve, reject) => {
                getData(
                    FILE_API_PATH + "streamContent",
                    {fileId},
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
                getData(
                    FILE_API_PATH + "getFileInfo",
                    {fileId},
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
