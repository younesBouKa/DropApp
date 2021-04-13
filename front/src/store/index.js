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
        currentNode: {},
        token: "",
    },
    getters: {
        getCurrentNode : (state, getters) =>{
            return state.currentNode;
        },
    },
    mutations: {
        storeCurrentNode(state, params) {
            console.log("mutation : "+params);
            state.currentNode = params ;
        },
    },
    actions: {
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
