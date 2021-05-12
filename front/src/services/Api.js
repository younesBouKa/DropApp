const axios = require('axios');
const _ = require('lodash');
const DEBUG = process.env.NODE_ENV === "development";
const AUTH_API_PATH = "/api/v0/auth/"
var refreshTimer = undefined;
var defaultDelayInMillis = 60*1000; // at least 1 minute
var tokenRefreshDelay = 60*1000;
const defaultOptions = {
    "timeout": 30000,    // 30 seconds timeout
    "Content-Type": "application/json",
    "headers": {
        "Authorization": ""
    }
}

const ResponseWrapper = function(response){
   this.currentResponse = response;
   this.isConnectionOk = function () {
        // statusText: "OK"
        return _.get(this.currentResponse,"statusText", "ko")==="OK";
    }
    this.isOk = function () {
        return _.get(this.currentResponse,"data.ok", null);
    }
    this.getData = function(){
        return _.get(this.currentResponse, "data.data",null);
    }
    this.getDevMessage = function(){
        return _.get(this.currentResponse, "data.devMessage",null);
    }
    this.getUserMessage = function () {
        return _.get(this.currentResponse, "data.userMessage",null);
    }
    this.getContentType = function () {
        return _.get(this.currentResponse, "Content-Type","");
    }
    this.getConfig = function () {
        return _.get(this.currentResponse, "config",{});
    }
    this.getHeaders = function () {
        return _.get(this.currentResponse, "headers",{});
    }
    this.getStatusCode = function () {
        return _.get(this.currentResponse, "status", -1);
    }
}

// axios interceptors
axios.interceptors.request.use((config) => {
    /// In dev, intercepts request and logs it into console for dev
    if (DEBUG) { console.info("axios.interceptors.request","✈️ ", config); }
    return config;
}, (error) => {
    if (DEBUG) { console.error("axios.interceptors.request", "✈️ ", error); }
    return Promise.reject(error);
});

axios.interceptors.response.use((response) => {
    let responseWrapper = new ResponseWrapper(response);
    if (DEBUG) { console.info("axios.interceptors.response","✉️ ", response, responseWrapper); }
    return responseWrapper;
}, (error) => {
    if (DEBUG) { console.info("axios.interceptors.response","✉️ ", error); }
    return Promise.reject(error.message);
});

const responseHandler = function (response, successCallback, errorCallback) {
    let isOk = response.isConnectionOk() && response.isOk();
    if (isOk){
        console.log("responseHandler ok",response.getConfig());
        if(successCallback)
            successCallback(response.getData());
    }
    else{
        console.error("responseHandler ko",response.getDevMessage()); // message to dev
        if(errorCallback)
            errorCallback(response.getUserMessage()); // message to user
    }
}

const postData = function (url, data, options, successCallback, errorCallback) {
    options = _.merge(defaultOptions, options);
    axios.post(url, data, options)
        .then(response=>{
            responseHandler(response, successCallback, errorCallback);
        })
        .catch(error=>{
            errorCallback("Technical error, check you're connection please!");
        })
}

const putData = function (url, data, options, successCallback, errorCallback) {
    options = _.merge(defaultOptions, options);
    axios.put(url, data, options)
        .then(response=>{
            responseHandler(response, successCallback, errorCallback);
        })
        .catch(error=>{
            errorCallback("Technical error, check you're connection please!");
        })
}

const deleteData = function (url, data, options, successCallback, errorCallback) {
    options = _.merge(defaultOptions, options);
    axios.put(url, data, options)
        .then(response=>{
            responseHandler(response, successCallback, errorCallback);
        })
        .catch(error=>{
            errorCallback("Technical error, check you're connection please!");
        })
}

const getData = function (url, data, options, successCallback, errorCallback, withWrapper) {
    options = _.merge(defaultOptions, options);
    if(data){
        url = url+ "?";
        Object.keys(data).forEach(key=>{
            url= url+key+"="+data[key]+"&";
        })
    }
    options.url = url; //+ "?" + encodeURI(JSON.stringify(data));
    console.log("getData", options);
    axios.get(options.url,options)
        .then(response=>{
            if(withWrapper){
                successCallback(response);
            }else{
                responseHandler(response, successCallback, errorCallback);
            }
        })
        .catch(errorCallback)
}

const setRefreshDelay = function (delayInMillis) {
    let newRefreshDelay =  delayInMillis ? delayInMillis : defaultDelayInMillis ;
    if(newRefreshDelay <= defaultDelayInMillis){
        console.warn("Token refresh delay is bellow 2 minutes",tokenRefreshDelay,newRefreshDelay);
    }
    tokenRefreshDelay = (3/4)*newRefreshDelay;
}

const refreshToken = function(){
    if(refreshTimer)
        clearInterval(refreshTimer);
    refreshTimer =  setInterval(()=>{
        console.log("refreshToken",tokenRefreshDelay);
        axios.post(AUTH_API_PATH+"refresh", {}, defaultOptions)
            .then(response=>{
                responseHandler(response,(data)=>{
                    console.log("refreshTokenEvery ok",data);
                    let userToken = data.token;
                    setRefreshDelay(data.refreshDelayInMillis);
                    if(userToken){
                        defaultOptions.headers.Authorization = "Bearer "+userToken;
                        console.log("refreshTokenEvery refreshed with success for delay", tokenRefreshDelay);
                    }
                }, (error)=>{
                    console.error("refreshTokenEvery ko",error);
                });
            })
            .catch(error=>{
                console.error("axios error: ", error);
            })
    }, tokenRefreshDelay);
}

const login = function (username, password, successCallback, errorCallback) {
    axios.post(AUTH_API_PATH+"login", {username, password}, {})
        .then(response=>{
            responseHandler(response,(data)=>{
                let userToken = data.token;
                setRefreshDelay(data.refreshDelayInMillis);
                if(userToken){
                    defaultOptions.headers.Authorization = "Bearer "+userToken;
                    refreshToken();
                    if(successCallback){
                        successCallback(data);
                    }
                }
            }, (error)=>{
                console.error("login responseHandler",error);
                if(errorCallback)
                    errorCallback("Authentication failed!"); // message to user
            });
        })
        .catch(error=>{
            console.error("axios error: ", error);
            errorCallback("Technical error, check you're connection please!");
        })
}

const register = function (data, successCallback, errorCallback) {
    axios.post(AUTH_API_PATH+"signup", data, {})
        .then(response=>{
            responseHandler(response,(data)=>{
                if(successCallback){
                    successCallback(data);
                }
            }, (error)=>{
                console.error("register responseHandler",error);
                if(errorCallback)
                    errorCallback("Register failed!"); // message to user
            });
        })
        .catch(error=>{
            errorCallback("Technical error, check you're connection please!");
        })
}


export default {
    postData,
    getData,
    putData,
    deleteData,
    login,
    register
};
