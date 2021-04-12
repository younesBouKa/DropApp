import Vue from 'vue'
import Vuex from 'vuex'
const axios = require('axios');
const _ = require('lodash');

Vue.use(Vuex)

const FILE_API_PATH = "/api/file"
const NODE_API_PATH = "/api/node"

const defaultOptions = {
  "timeout": 4000,    // 4 seconds timeout
  "Content-Type" : "application/json",
  "headers": {
    'X-Custom-Header': 'value'
  }
};

const postData = function (url,data,options, successCallback, errorCallback) {
  options = _.merge(defaultOptions, options);
  axios.post(url,data, options)
      .then(successCallback)
      .catch(errorCallback)
}

const getData = function (url, data, options, successCallback, errorCallback) {
  options = _.merge(defaultOptions, options);
  options.url = options.url + "?"+encodeURI(JSON.stringify(data));
  axios.get(options)
      .then(successCallback)
      .catch(errorCallback)
}

export default new Vuex.Store({
  state: {
    user : {},
    currentSpace : {},
    currentFolder : {},
    currentContent : {},
    token : "",
  },
  mutations: {
  },
  actions: {
    getNodeById({ commit, state }, nodeId){
      return new Promise((resolve, reject) => {
        postData(
            NODE_API_PATH+"/getNodeById",
            {nodeId},
            {},
            (response)=>{
              console.log(response);
              resolve(response.data);
            },
            (error)=>{
              console.error(error);
              reject(error);
            }
        )
      })
    },
    getNodeByPath({ commit, state }, nodePath){
      return new Promise((resolve, reject) => {
        postData(
            NODE_API_PATH+"/getNodeByPath",
            {nodePath},
            {},
            (response)=>{
              console.log(response);
              resolve(response.data);
            },
            (error)=>{
              console.error(error);
              reject(error);
            }
        )
      })
    },
    getNodesByParentId({ commit, state }, parentId){
      return new Promise((resolve, reject) => {
        postData(
            NODE_API_PATH+"/getNodesByParentId",
            {parentId},
            {},
            (response)=>{
              console.log(response);
              resolve(response.data);
            },
            (error)=>{
              console.error(error);
              reject(error);
            }
        )
      })
    },
    getNodesByParentPath({ commit, state }, parentPath){
      return new Promise((resolve, reject) => {
        postData(
            NODE_API_PATH+"/getNodesByParentPath",
            {parentPath},
            {},
            (response)=>{
              console.log(response);
              resolve(response.data);
            },
            (error)=>{
              console.error(error);
              reject(error);
            }
        )
      })
    },
    getNodesByQuery({ commit, state }, query){
      return new Promise((resolve, reject) => {
        postData(
            NODE_API_PATH+"/getNodesByQuery",
            query,
            {},
            (response)=>{
              console.log(response);
              resolve(response.data);
            },
            (error)=>{
              console.error(error);
              reject(error);
            }
        )
      })
    },
    createFolderNodeWithMetaData({ commit, state }, metaData){
      return new Promise((resolve, reject) => {
        postData(
            NODE_API_PATH+"/createFolderNodeWithMetaData",
            metaData,
            {},
            (response)=>{
              console.log(response);
              resolve(response.data);
            },
            (error)=>{
              console.error(error);
              reject(error);
            }
        )
      })
    },
    deleteNodeByPath({ commit, state }, path, recursive){
      return new Promise((resolve, reject) => {
        postData(
            NODE_API_PATH+"/deleteNodeByPath",
            {path, recursive},
            {},
            (response)=>{
              console.log(response);
              resolve(response.data);
            },
            (error)=>{
              console.error(error);
              reject(error);
            }
        )
      })
    },
    deleteNodeById({ commit, state }, nodeId, recursive){
      return new Promise((resolve, reject) => {
        postData(
            NODE_API_PATH+"/deleteNodeById",
            {nodeId, recursive},
            {},
            (response)=>{
              console.log(response);
              resolve(response.data);
            },
            (error)=>{
              console.error(error);
              reject(error);
            }
        )
      })
    },
    deleteNodesById({ commit, state }, nodesId, recursive){
      return new Promise((resolve, reject) => {
        postData(
            NODE_API_PATH+"/deleteNodesById",
            {nodesId, recursive},
            {},
            (response)=>{
              console.log(response);
              resolve(response.data);
            },
            (error)=>{
              console.error(error);
              reject(error);
            }
        )
      })
    },
    hasPermissionById({ commit, state }, nodeId, permission){
      return new Promise((resolve, reject) => {
        postData(
            NODE_API_PATH+"/hasPermissionById",
            {nodeId, permission},
            {},
            (response)=>{
              console.log(response);
              resolve(response.data);
            },
            (error)=>{
              console.error(error);
              reject(error);
            }
        )
      })
    },
    hasPermissionByPath({ commit, state }, nodePath, permission){
      return new Promise((resolve, reject) => {
        postData(
            NODE_API_PATH+"/hasPermissionByPath",
            {nodePath, permission},
            {},
            (response)=>{
              console.log(response);
              resolve(response.data);
            },
            (error)=>{
              console.error(error);
              reject(error);
            }
        )
      })
    },
    copyNodeByPath({ commit, state }, srcPath, destPath){
      return new Promise((resolve, reject) => {
        postData(
            NODE_API_PATH+"/hasPermissionByPath",
            {srcPath, destPath},
            {},
            (response)=>{
              console.log(response);
              resolve(response.data);
            },
            (error)=>{
              console.error(error);
              reject(error);
            }
        )
      })
    },
    copyNodeById({ commit, state }, srcId, destId){
      return new Promise((resolve, reject) => {
        postData(
            NODE_API_PATH+"/copyNodeById",
            {srcId, destId},
            {},
            (response)=>{
              console.log(response);
              resolve(response.data);
            },
            (error)=>{
              console.error(error);
              reject(error);
            }
        )
      })
    },
    moveNodeByPath({ commit, state }, srcPath, destPath){
      return new Promise((resolve, reject) => {
        postData(
            NODE_API_PATH+"/moveNodeByPath",
            {srcPath, destPath},
            {},
            (response)=>{
              console.log(response);
              resolve(response.data);
            },
            (error)=>{
              console.error(error);
              reject(error);
            }
        )
      })
    },
    moveNodeById({ commit, state }, srcId, destId){
      return new Promise((resolve, reject) => {
        postData(
            NODE_API_PATH+"/moveNodeById",
            {srcId, destId},
            {},
            (response)=>{
              console.log(response);
              resolve(response.data);
            },
            (error)=>{
              console.error(error);
              reject(error);
            }
        )
      })
    },

    uploadFile({ commit, state }, formData){
      return new Promise((resolve, reject) => {
        getData(
            FILE_API_PATH+"/uploadFile",
            formData,
            {},
            (response)=>{
              console.log(response);
              resolve(response.data);
            },
            (error)=>{
              console.error(error);
              reject(error);
            }
        )
      })
    },

    streamContent({ commit, state }, fileId){
      return new Promise((resolve, reject) => {
        getData(
            FILE_API_PATH+"/streamContent",
            {fileId},
            {},
            (response)=>{
              console.log(response);
              resolve(response.data);
            },
            (error)=>{
              console.error(error);
              reject(error);
            }
        )
      })
    },
    getFileInfo({ commit, state }, fileId){
      return new Promise((resolve, reject) => {
        getData(
            FILE_API_PATH+"/getFileInfo",
            {fileId},
            {},
            (response)=>{
              console.log(response);
              resolve(response.data);
            },
            (error)=>{
              console.error(error);
              reject(error);
            }
        )
      })
    },
  },
  modules: {
  }
})
