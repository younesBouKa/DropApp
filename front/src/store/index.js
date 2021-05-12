import Vue from 'vue'
import Vuex from 'vuex'

import api from '../services/Api';
const _ = require('lodash');
Vue.use(Vuex)

const NODE_API_PATH = "/api/v0/nodes/"
const GROUP_API_PATH = "/api/v0/groups/"
const ACCESS_API_PATH = "/api/v0/access/"

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
        },
        getClipBoardAction : (state, getters) => {
            return state.clipBoardOperation;
        }
    },
    mutations: {
        setUser(state, data) {
            console.log("mutation [setUser] ",data);
            state.user = data ;
        },
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
        login({state, getters, commit, dispatch}, data) {
            return new Promise((resolve, reject) => {
                api.login(
                    data.username,
                    data.password,
                    (response) => {
                        console.log("login with success",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("login failled ",error);
                        reject(error);
                    }
                )
            })
        },
        register({state, getters, commit, dispatch}, data) {
            return new Promise((resolve, reject) => {
                api.register(
                    data,
                    (response) => {
                        console.log("register with success",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("register failled ",error);
                        reject(error);
                    }
                )
            })
        },

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

        createNode({state, getters, commit, dispatch}, parentId,nodeInfo, file) {
            let formData = new FormData();
            formData.append("file",file);
            formData.append("nodeInfo",nodeInfo);
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "/"+parentId,
                    formData,
                    {},
                    (response) => {
                        console.log("createNode",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("createNode",error);
                        reject(error);
                    }
                )
            })
        },
        deleteNodeById({state, getters, commit, dispatch}, nodeId) {
            return new Promise((resolve, reject) => {
                api.deleteData(
                    NODE_API_PATH + nodeId,
                    {},
                    {},
                    (response) => {
                        console.log("deleteNodeById",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("deleteNodeById",error);
                        reject(error);
                    }
                )
            })
        },
        updateNode({state, getters, commit, dispatch}, nodeId, file, chunk, nodeInfo, query) {
            query = query ? query : {};
            let options = {
                headers : {
                    "x-chunk-start-pos" : query.startPos || 0
                }
            };
            let formData = new FormData();
            formData.append("file",file);
            formData.append("nodeInfo",nodeInfo);
            formData.append("chunk",chunk);
            return new Promise((resolve, reject) => {
                api.putData(
                    NODE_API_PATH + nodeId,
                    formData,
                    options,
                    (response) => {
                        console.log("updateNode",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("updateNode",error);
                        reject(error);
                    }
                )
            })
        },
        getRootNodes({state, getters, commit, dispatch}) {
            return new Promise((resolve, reject) => {
                api.getData(
                    NODE_API_PATH ,
                    undefined,
                    {},
                    (response) => {
                        console.log("getRootNodes",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("getRootNodes",error);
                        reject(error);
                    }
                )
            })
        },
        getNodeById({state, getters, commit, dispatch}, nodeId) {
            return new Promise((resolve, reject) => {
                api.getData(
                    NODE_API_PATH + nodeId,
                    {},
                    {},
                    (response) => {
                        console.log("getNodeById",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("getNodeById",error);
                        reject(error);
                    }
                )
            })
        },
        getNodesByParentIdAndQuery({state, getters, commit, dispatch}, parentId, query) {
            query = query ? query : {};
            let queryObj = {
                search : query.search || "",
                page : query.page || 0,
                size : query.size || 10,
                sortDirection : query.sortDirection || "DESC",
                sortField : query.sortField || "creationDate",
            }
            return new Promise((resolve, reject) => {
                api.getData(
                    NODE_API_PATH +parentId+"/children",
                    queryObj,
                    {},
                    (response) => {
                        console.log("getNodesByQuery",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("getNodesByQuery",error);
                        reject(error);
                    }
                )
            })
        },

        copyNodesFromStore({state, getters, commit, dispatch}, destId) {
            let nodeIds = (state.clipBoardNodes||[]).map(elt=> elt.id);
            let remove = state.clipBoardOperation === "CUT";
            let inf = {
                nodeIds : nodeIds,
                remove : remove,
                destId : destId
            }
            return dispatch("copyNodes",nodeIds, inf);
        },
        copyNodes({state, getters, commit, dispatch}, payload) {
            let nodeIds = payload.nodeIds,
                remove = payload.remove,
                destId = payload.destId;
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "copy/"+destId+"?romove="+remove,
                    nodeIds,
                    {},
                    (response) => {
                        console.log("copyNodes",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("copyNodes",error);
                        reject(error);
                    }
                )
            })
        },

        createZipNode({state, getters, commit, dispatch}, compressInfo) {
            compressInfo = compressInfo ? compressInfo : {};
            let zipDot = {
                name: compressInfo.name || undefined ,
                description : compressInfo.description || "",
                label : compressInfo.label || "",
                parentId: compressInfo.parentId || undefined,
                nodesId : compressInfo.nodesId || [], // array
                fields: compressInfo.fields || {}, // object
            }
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "zip/create",
                    zipDot,
                    {},
                    (response) => {
                        console.log("createZipNode",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("createZipNode",error);
                        reject(error);
                    }
                )
            })
        },
        getZippedNodes({state, getters, commit, dispatch}, compressInfo) {
            compressInfo = compressInfo ? compressInfo : {};
            let zipDot = {
                name: compressInfo.name || undefined ,
                description : compressInfo.description || "",
                label : compressInfo.label || "",
                parentId: compressInfo.parentId || undefined,
                nodesId : compressInfo.nodesId || [], // array
                fields: compressInfo.fields || {}, // object
            }
            return new Promise((resolve, reject) => {
                api.postData(
                    NODE_API_PATH + "zip",
                    zipDot,
                    {},
                    (response) => {
                        console.log("getZippedNodes",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("getZippedNodes",error);
                        reject(error);
                    },
                    true
                )
            })
        },
        streamNodeContent({state, getters, commit, dispatch}, nodeId, query) {
            query = query ? query : {};
            return new Promise((resolve, reject) => {
                // <day-name>, <jour> <mois> <année> <heure>:<minute>:<seconde> GMT
                let formatDate = function(date){
                    let dayStrArray=['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
                    let monthStrArray=['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
                    let dayInWeek = date.getDay();
                    let dayStr = dayStrArray[dayInWeek];
                    let monthStr = monthStrArray[date.getMonth()];
                    return `${dayStr}, ${date.getDate()} ${monthStr} ${date.getFullYear()} ${date.getHours()}:${date.getMinutes()}:${date.getSeconds()} GMT`;
                }
                let ifRange = formatDate(new Date()); // Wed, 21 Oct 2015 07:28:00 GMT
                let options = {
                    headers : {
                        "if-range" : query["if-range"] || ifRange,
                        "range": query.range || "bytes=0-", //bytes=200-1000
                    }
                };
                api.getData(
                    NODE_API_PATH + nodeId+"/stream",
                    undefined,
                    options,
                    (responseWrapper) => {
                        console.log("streamNodeContent",responseWrapper);
                        resolve(responseWrapper);
                    },
                    (error) => {
                        console.error("streamNodeContent",error);
                        reject(error);
                    },
                    true
                )
            })
        },

        listOwnGroups({state, getters, commit, dispatch}) {
            return new Promise((resolve, reject) => {
                api.getData(
                    GROUP_API_PATH ,
                    {},
                    {},
                    (response) => {
                        console.log("getGroups",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("getGroups",error);
                        reject(error);
                    }
                )
            })
        },
        listGroupMembers({state, getters, commit, dispatch}, groupId) {
            return new Promise((resolve, reject) => {
                api.getData(
                    GROUP_API_PATH+ groupId ,
                    {},
                    {},
                    (response) => {
                        console.log("listGroupMembers",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("listGroupMembers",error);
                        reject(error);
                    }
                )
            })
        },
        addGroup({state, getters, commit, dispatch}, groupInfo) {
            return new Promise((resolve, reject) => {
                api.postData(
                    GROUP_API_PATH ,
                    groupInfo,
                    {},
                    (response) => {
                        console.log("addGroup",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("addGroup",error);
                        reject(error);
                    }
                )
            })
        },
        updateGroup({state, getters, commit, dispatch}, groupId, groupInfo) {
            return new Promise((resolve, reject) => {
                api.putData(
                    GROUP_API_PATH+ groupId ,
                    groupInfo,
                    {},
                    (response) => {
                        console.log("addGroup",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("addGroup",error);
                        reject(error);
                    }
                )
            })
        },
        deleteGroup({state, getters, commit, dispatch}, groupId) {
            return new Promise((resolve, reject) => {
                api.deleteData(
                    GROUP_API_PATH+ groupId ,
                    {},
                    {},
                    (response) => {
                        console.log("addGroup",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("addGroup",error);
                        reject(error);
                    }
                )
            })
        },
        addMemberToGroup({state, getters, commit, dispatch}, groupId, membershipInfo) {
            return new Promise((resolve, reject) => {
                api.postData(
                    GROUP_API_PATH+ groupId+"/addMember" ,
                    membershipInfo,
                    {},
                    (response) => {
                        console.log("addGroup",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("addGroup",error);
                        reject(error);
                    }
                )
            })
        },
        removeMemberFromGroup({state, getters, commit, dispatch}, groupId, memberId) {
            return new Promise((resolve, reject) => {
                api.deleteData(
                    GROUP_API_PATH+ groupId+"/"+memberId ,
                    {},
                    {},
                    (response) => {
                        console.log("removeMemberFromGroup",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("removeMemberFromGroup",error);
                        reject(error);
                    }
                )
            })
        },

        getPermission({state, getters, commit, dispatch}, resourceId) {
            return new Promise((resolve, reject) => {
                api.getData(
                    ACCESS_API_PATH+ resourceId ,
                    {},
                    {},
                    (response) => {
                        console.log("getPermission",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("getPermission",error);
                        reject(error);
                    }
                )
            })
        },
        generateToken({state, getters, commit, dispatch}, resourceId, accessDetails) {
            let access = {
                resourceId : resourceId,
                requesterId : accessDetails.requesterId | null,
                permission : accessDetails.permission | 0,
                delay : accessDetails.delay | -1,
            }
            return new Promise((resolve, reject) => {
                api.postData(
                    ACCESS_API_PATH+ "generateToken" ,
                    access,
                    {},
                    (response) => {
                        console.log("generateToken",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("generateToken",error);
                        reject(error);
                    }
                )
            })
        },
        addPermission({state, getters, commit, dispatch}, resourceId, accessDetails) {
            let access = {
                resourceId : resourceId,
                requesterId : accessDetails.requesterId | null,
                permission : accessDetails.permission | 0,
                delay : accessDetails.delay | -1,
            }
            return new Promise((resolve, reject) => {
                api.postData(
                    ACCESS_API_PATH+ resourceId ,
                    access,
                    {},
                    (response) => {
                        console.log("addPermission",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("addPermission",error);
                        reject(error);
                    }
                )
            })
        },
        removePermission({state, getters, commit, dispatch}, resourceId, accessDetails) {
            let access = {
                resourceId : resourceId,
                requesterId : accessDetails.requesterId | null,
                permission : accessDetails.permission | 0,
                delay : accessDetails.delay | -1,
            }
            return new Promise((resolve, reject) => {
                api.deleteData(
                    ACCESS_API_PATH+ resourceId ,
                    access,
                    {},
                    (response) => {
                        console.log("removePermission",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("removePermission",error);
                        reject(error);
                    }
                )
            })
        },
        removeAllPermissions({state, getters, commit, dispatch}, resourceId, accessDetails) {
            let access = {
                resourceId : resourceId,
                requesterId : accessDetails.requesterId | null,
                permission : accessDetails.permission | 0,
                delay : accessDetails.delay | -1,
            }
            return new Promise((resolve, reject) => {
                api.deleteData(
                    ACCESS_API_PATH+ resourceId+"/removeAll" ,
                    access,
                    {},
                    (response) => {
                        console.log("removePermission",response);
                        resolve(response);
                    },
                    (error) => {
                        console.error("removePermission",error);
                        reject(error);
                    }
                )
            })
        },

    },
    modules: {}
})
