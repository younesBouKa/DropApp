const axios = require('axios');
const _ = require('lodash');
const DEBUG = process.env.NODE_ENV === "development";

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
    if (DEBUG) { console.info("axios.interceptors.response","✉️ ", response); }
    return response;
}, (error) => {
    if (DEBUG) { console.info("axios.interceptors.response","✉️ ", error); }
    return Promise.reject(error.message);
});

const defaultOptions = {
    "timeout": 4000,    // 4 seconds timeout
    "Content-Type": "application/json",
    "headers": {
        'X-Custom-Header': 'value' // just example
    }
};

const responseHandler = function (response, successCallback, errorCallback) {
    console.log("responseHandler",response);
    if (response && response.data && response.data.ok)
        if(successCallback)
            successCallback(response.data);
    else{
        console.error("responseHandler",response.data); // message to dev
        if(errorCallback)
            errorCallback(_.get(response, data.data.message, "Technical error!")); // message to user
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

const getData = function (url, data, options, successCallback, errorCallback) {
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
            responseHandler(response, successCallback, errorCallback);
        })
        .catch(errorCallback)
}

export default {postData, getData};
